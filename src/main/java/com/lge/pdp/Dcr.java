
package com.lge.pdp;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.events.XMLEvent;

import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;

public class Dcr
{
	
	public static void main(String[] args) {
		try {
			Properties p = new Properties();
			Connection conn = null;
			PreparedStatement pstmt = null;
			PreparedStatement pstmt2 = null;
        	ResultSet rs = null;
        	String pageName = "";
        	
        	Map<String,String> dcrMap = new HashMap<String,String>();
//        	p.load(new FileInputStream("C:\\test.properties"));
        	p.load(new FileInputStream(args[0]));
        	
        	String dbArea = p.getProperty("dbArea");
        	String url = p.getProperty("db."+dbArea+".url");
        	String locales = p.getProperty("locale");
			conn = DriverManager.getConnection(url, p.getProperty("db."+dbArea+".id"), p.getProperty("db."+dbArea+".pw"));
			
            SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
            long start = System.currentTimeMillis();
            System.out.println("======================== upload START ========================");
            System.out.println("시작시간 :: " + sf.format(start));
        	System.out.println(start);
        	System.out.println("======================== upload START ========================");
        	String defaultDCRPath = getMessage("defaultDCRPath",p);
        	System.out.println(defaultDCRPath);
        	if(defaultDCRPath!=null && !defaultDCRPath.equals("") ) {
	        	for(String locale : locales.split(",")) {
	        		defaultDCRPath = defaultDCRPath.replace("{1}", locale.toUpperCase());
	        		String selectMd = "select sku from mkt_model_m where locale_code = '"+locale.toUpperCase()+"' and model_id = ? ;";
	        		String selectBd = "select sku from mkt_model_m where locale_code = '"+locale.toUpperCase()+"' and original_model_id = ? ;";
	        		pstmt = conn.prepareStatement(selectMd);
	        		pstmt2 = conn.prepareStatement(selectBd);
	        		String dcrName = "";
	        		String dcrType = "";
	        		 
	        		String[] dcrPath = p.getProperty("dcrPathList").split(",");
	        		for(String dp : dcrPath) {
	        			ArrayList<Map<String,String>> dcrList = new ArrayList<Map<String,String>>();
	        			if(p.getProperty(dp) != null && !p.getProperty(dp).equals("")) {
		        			dcrType = p.getProperty(dp);
		        			dp = dp.replace("{1}", locale.toUpperCase());
		        			dp = dp.replace("{2}", locale.toLowerCase());
		        			List<Path> dcr = Files.walk(Paths.get(defaultDCRPath+"/"+dp)).filter(Files::isRegularFile).collect(Collectors.toList());
						for( Path pdp : dcr) {
							try {
							dcrName = StringUtils.replace(pdp.toString(),defaultDCRPath,"");
							dcrName = RegExUtils.replaceAll(dcrName, "\\\\", "/");
							dcrName = RegExUtils.replaceFirst(dcrName, "[\\\\/]", "");
							System.out.println("--------------------------------------------------------------"+pageName+"------------------------------START-----------------------------------");
							
							XMLInputFactory xmlInFact = XMLInputFactory.newInstance();
							
							Pattern pattern2 = Pattern.compile("[mM][dD]\\d{8}|[bB][dD]\\d{8}");
							Matcher matcher2 = null;
							XMLEvent dcrXml = null;
							int pb = 0;
							int fs = 0;
							int b2b2c = 0;
							int sup = 0;
							int i = 0;
							
							FileInputStream dcrFis = new FileInputStream(pdp.toString());
							XMLEventReader reader2 = xmlInFact.createXMLEventReader(dcrFis);
							String id = "";
							String pr = "N";
							String did = "";
							String dval = "";
							String type = "";
							if(dcrType.equals("stepup")) {
								while(reader2.hasNext()) {
									dcrXml = reader2.nextEvent();
									
									if(dcrXml.isStartElement()) {
										if(id.equals("")) {
											id += dcrXml.asStartElement().getName().toString().equals("Root") ? "" : dcrXml.asStartElement().getName().toString();
										}else {
											id += "/"+dcrXml.asStartElement().getName().toString();
										}
										if(id.equals("productBasic")) {
											pb++;
											pr = "Y";
										}
									}
									if(p.getProperty(id)!= null) {
										if(dcrXml.isStartElement()) {
											if(dcrXml.asStartElement().getName().toString().equals("featureSpec")) {
												fs++;
											}
											did = p.getProperty(id);
										}else if(dcrXml.isCharacters()) {
											dval += dcrXml.toString();
										}else {
											dcrMap = new HashMap<String,String>();
											did = StringUtils.replace(did, "{1}", "item"+(pb-1));
											did = StringUtils.replace(did, "{2}", "item"+(fs-1));
											matcher2 = pattern2.matcher(dval);
											while(matcher2.find()) {
												String mid= matcher2.group();
												if(mid.toLowerCase().indexOf("md")>-1) {
													pstmt.setString(1, mid.toUpperCase());
													rs = pstmt.executeQuery();
													if(rs.next()) {
														dval = RegExUtils.replaceAll(dval,mid,rs.getString(1));
													}
												}else {
													pstmt2.setString(1, mid.toUpperCase());
													rs = pstmt2.executeQuery();
													if(rs.next()) {
														dval = RegExUtils.replaceAll(dval,mid,rs.getString(1));
													}
												}
											}
											dcrMap.put("locale", locale.toUpperCase());
											dcrMap.put("dcrName", dcrName);
											dcrMap.put("attr", did);
											if(did.indexOf("imageRef")>-1) {
												try {
													Path imgPath = Paths.get(dval);
													String fName = imgPath.getFileName().toString();
													String path = imgPath.getParent().toString();
													path = RegExUtils.replaceAll(path, "[\\\\]", "/");
													path = RegExUtils.replaceAll(path, "[\\\\%#{}^;+:*?.|&\\t\\[\\] ]", "_");
													path = path.toLowerCase();
													fName = RegExUtils.replaceAll(fName, "[*/:\\[\\]\\\\|#%{}?&]", "_");
													if(fName.lastIndexOf(".")>-1) {
														String exp = fName.substring(fName.lastIndexOf("."),fName.length());
														fName = fName.substring(0,fName.lastIndexOf("."))+exp.toLowerCase();
													}
													dval = path +"/"+ fName;
												}catch(Exception e) {
													System.out.println("dval :: " + dval);
												}
											}
											dcrMap.put("val", dval);
											dcrMap.put("ukey", locale+"_"+dcrType+"_"+Paths.get(dcrName).getFileName().toString().trim()+"_"+i);
											did="";
											dval="";
											dcrList.add(dcrMap);
											i++;
										}
									}
									
									if(dcrXml.isEndElement()) {
										if(id.equals("productBasic")) {
											pr = "N";
											fs = 0;
										}
										if(id.lastIndexOf("/")>0) {
											id = StringUtils.replace(id, id.substring(id.lastIndexOf("/"), id.length()), "");
										}else {
											id = "";
										}
									}
									
								}
							}else if(dcrType.equals("supportProduct")) {
								while(reader2.hasNext()) {
									dcrXml = reader2.nextEvent();
									if(dcrXml.isStartElement()) {
										if(id.equals("")) {
											id += dcrXml.asStartElement().getName().toString().equals("Root") ? "" : dcrXml.asStartElement().getName().toString();
										}else {
											id += "/"+dcrXml.asStartElement().getName().toString();
										}
									}
									
									if(p.getProperty(id)!= null) {
										if(dcrXml.isStartElement()) {
											if(id.equals("b2cSupportLink")) {
												type = "b2c";
											}else if(id.equals("b2bSupportLink")) {
												type = "b2b";
											}else if(id.equals("supportOption")) {
												type = "sup";
											}
										}else if(dcrXml.isCharacters()) {
											dval += dcrXml.toString();
										}else {
											dcrMap = new HashMap<String,String>();
											did = p.getProperty(id);
											
											if(type.equals("b2c")) {
												did = StringUtils.replace(did, "{1}", "item"+b2b2c);
												did = StringUtils.replace(did, "{2}", (b2b2c+1)+"");
											}else if(type.equals("b2b")) {
												did = StringUtils.replace(did, "{1}", "item"+b2b2c);
												did = StringUtils.replace(did, "{2}", (b2b2c+1)+"");
											}else if(type.equals("sup")) {
												did = StringUtils.replace(did, "{1}", "item"+sup);
												did = StringUtils.replace(did, "{2}", (sup+1)+"");
											}
											
											
											if(id.equals("b2cSupportLink")) {
												dval="b2c";
												b2b2c++;
											}else if(id.equals("b2bSupportLink")) {
												dval="b2b";
												b2b2c++;
											}else if(id.equals("supportOption")) {
												sup++;
											}
											
											if(!did.equals("")) {
												dcrMap.put("locale", locale.toUpperCase());
												dcrMap.put("dcrName", dcrName);
												dcrMap.put("attr", did);
												dcrMap.put("val", dval);
												dcrMap.put("ukey", locale+"_"+dcrType+"_"+Paths.get(dcrName).getFileName().toString().trim()+"_"+i);
												dcrList.add(dcrMap);
												i++;
											}
											dval = "";
											did = "";
										}
									}
									
									if(dcrXml.isEndElement()) {
										if(id.lastIndexOf("/")>0) {
											id = StringUtils.replace(id, id.substring(id.lastIndexOf("/"), id.length()), "");
										}else {
											id = "";
										}
									}
									
								}
								
							}
							System.out.println("dcrList size :::: " + dcrList.size());
							System.out.println("--------------------------------------------------------------"+pageName+"------------------------------END-----------------------------------");
							
							}catch(FileNotFoundException e) {
								e.printStackTrace();
								System.out.println("파일없음 :: ");
								continue;
							}
						} // For END
						/*
							for(Map<String,String> d : dcrList) {
								System.out.println(" name ::: " + d.get("dcrName") + " ::: attr ::: " + d.get("attr") + " ::: val :::: " + d.get("val"));
							}
						*/
						insertDB(conn,dcrList);
	        			}
	        		}
	        		defaultDCRPath = getMessage("defaultDCRPath",p);
	        	}
        	}else {
        		System.out.println("defaultDCRPath가 비어있습니다.");
        	}
        	
        	if (conn != null && !conn.isClosed()) {
        		if(pstmt!=null && !pstmt.isClosed()) {
        			pstmt.close();
        		}
        		if(pstmt2!=null && !pstmt2.isClosed()) {
        			pstmt2.close();
        		}
				conn.close();
			}
			System.out.println("Total Memory : "+Runtime.getRuntime().totalMemory());
        	System.out.println("Free Memory : "+Runtime.getRuntime().freeMemory());
        	System.out.println("Max Memory : "+Runtime.getRuntime().maxMemory());
            long end = System.currentTimeMillis();
            System.out.println("======================== upload END ========================");
        	System.out.println(end);
        	System.out.println("시작시간 :: " + sf.format(start) + " , 종료시간 :: " + sf.format(end));
        	System.out.println("수행시간 :: " + (end - start) + " ms");
        	System.out.println("======================== upload END ========================");
        	
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	
    public static String getMessage(String code, Properties pro) {
        String result="";
        try
        {
            result = new String(pro.getProperty(code).getBytes("ISO-8859-1"), "utf-8");
            
        }catch(Exception e){
            e.printStackTrace();
        }
        return result;
    }
	

	
	public static void insertDB(Connection conn, ArrayList<Map<String,String>> dcrList) {
		try {
			System.out.println(" DB 시작 !");
			String insertDCRSql = "INSERT INTO mig_pdp_dcr(locale, path, attr, val, create_date, ukey) VALUES(?, ?, ?, ?, SYSDATE(),?) ON DUPLICATE KEY UPDATE locale=?, attr=?, val=?,modify_date=SYSDATE();";
			PreparedStatement pstmt = conn.prepareStatement(insertDCRSql);
			for(Map<String,String> dcr : dcrList) {
				pstmt.setString(1, dcr.get("locale"));
				pstmt.setString(2, dcr.get("dcrName"));
				pstmt.setString(3, dcr.get("attr"));
				pstmt.setString(4, dcr.get("val"));
				pstmt.setString(5, dcr.get("ukey"));
				pstmt.setString(6, dcr.get("locale"));
				pstmt.setString(7, dcr.get("attr"));
				pstmt.setString(8, dcr.get("val"));
				pstmt.addBatch();
				pstmt.clearParameters();
			}
			pstmt.executeBatch();
			pstmt.clearBatch();
		}catch(SQLException e) {
			e.printStackTrace();
		}
		System.out.println(" DB 끝 !");
	}

  
}