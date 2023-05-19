
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

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.events.XMLEvent;

import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;

import com.lge.pdp.model.PdpExtract;

public class Migration
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
        	
        	String url = p.getProperty("db.url");
        	String locales = p.getProperty("locale");
			conn = DriverManager.getConnection(url, p.getProperty("db.id"), p.getProperty("db.pw"));
			
            SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
            long start = System.currentTimeMillis();
            System.out.println("======================== upload START ========================");
            System.out.println("시작시간 :: " + sf.format(start));
        	System.out.println(start);
        	System.out.println("======================== upload START ========================");
        	
        	int startCount = 1;
        	int count = Integer.parseInt(p.getProperty("count"));
        	int endCount = startCount * count;
        	startCount = (startCount==1 ? 0 : ((startCount-1)*count));
        	
        	ArrayList<Map<String,String>> dcrList = new ArrayList<Map<String,String>>();
        	for(String locale : locales.split(",")) {
//        		String selectMd = "select \n"
//            			+ " concat(sales_model_code\n"
//            			+ "			,if(concat('.',sales_suffix_code)<>'.'\n"
//            			+ "				,concat('.',sales_suffix_code)\n"
//            			+ "				,'')\n"
//            			+ "			,if(concat('.',affiliate_code)<>'.'\n"
//            			+ "				,concat('.',affiliate_code)\n"
//            			+ "				,'')\n"
//            			+ "			,'.'\n"
//            			+ "			,locale_code\n"
//            			+ "			,'.'\n"
//            			+ "			,if(biz_type = 'B2C','C','B')) as sku \n"
//            			+ "from mkt_model_m mmm \n"
//            			+ "where mmm.locale_code = '"+locale.toUpperCase()+"'\n"
//            			+ "and mmm.sales_model_code <> '' and upper(mmm.model_id) = ? or upper(mmm.original_model_id) = ?;";
        		String selectMd = "select sku from mig_sku_mapping where locale_code = '"+locale.toUpperCase()+"' and model_id = ? ;";
        		String selectBd = "select sku from mig_sku_mapping where locale_code = '"+locale.toUpperCase()+"' and original_model_id = ? ;";
        		pstmt = conn.prepareStatement(selectMd);
        		pstmt2 = conn.prepareStatement(selectBd);
        		String defaultDCRPath = "/default/main/LGE/"+locale.toUpperCase()+"/WORKAREA/wa/"; 
    			List<String> line = Files.readAllLines(Paths.get(p.getProperty("dcrList")));
    			if(endCount>line.size()) {
    				endCount = line.size();
    			}
    			System.out.println("START ::: " + startCount);
    			System.out.println("END ::: " + endCount);
    			String dcrName = "";
    			startCount = 0;
//    			endCount = 1;
    			String dcrType = "";
				for( int c = startCount ; c < endCount; c++) {
					try {
					Path pdp = Paths.get(line.get(c));
					
//					Path pdp = Paths.get("C:\\default.xml");
					dcrName = StringUtils.replace(pdp.toString(),defaultDCRPath,"");
					if(dcrName.lastIndexOf("/")>0) {
					dcrType = p.getProperty(dcrName.substring(0,dcrName.lastIndexOf("/")));
					}else{
						dcrType = p.getProperty(dcrName.replace("C:\\", ""));
					}
					System.out.println(dcrName);
					System.out.println(dcrType);
					System.out.println("--------------------------------------------------------------"+pageName+"------------------------------START-----------------------------------");
					
					XMLInputFactory xmlInFact = XMLInputFactory.newInstance();
					
					// Component ID를 뽑기 위한 패턴
					Pattern pattern2 = Pattern.compile("[mM][dD]\\d{8}|[bB][dD]\\d{8}");
					Matcher matcher2 = null;
					XMLEvent dcrXml = null;
					int pb = 0;
					int fs = 0;
					int b2b2c = 0;
					int sup = 0;
					int i = 0;
					System.out.println(pdp.toString());
					System.out.println(pdp.toString().replace(defaultDCRPath, ""));
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
											pstmt.setString(2, mid.toUpperCase());
											rs = pstmt.executeQuery();
											if(rs.next()) {
												dval = RegExUtils.replaceAll(dval,mid,rs.getString(1));
											}
										}else {
											pstmt2.setString(1, mid.toUpperCase());
											pstmt2.setString(2, mid.toUpperCase());
											rs = pstmt2.executeQuery();
											if(rs.next()) {
												dval = RegExUtils.replaceAll(dval,mid,rs.getString(1));
											}
										}
									}
									dcrMap.put("locale", locale.toUpperCase());
									dcrMap.put("dcrName", dcrName);
									dcrMap.put("attr", did);
									dcrMap.put("val", dval);
									did="";
									dval="";
									dcrList.add(dcrMap);
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
										dcrList.add(dcrMap);
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
//						insertHistoryDB(pstmt2,pageName,"N",locale);
						continue;
					}
				} // For END
				for(Map<String,String> d : dcrList) {
					System.out.println(" name ::: " + d.get("dcrName") + " ::: attr ::: " + d.get("attr") + " ::: val :::: " + d.get("val"));
				}
				
				insertDB(conn,dcrList);
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
	
	public static void insertDB(Connection conn, ArrayList<Map<String,String>> dcrList) {
		try {
			System.out.println(" DB 시작 !");
			String insertDCRSql = "INSERT INTO mig_pdp_dcr(locale, path, attr, val, create_date) VALUES(?, ?, ?, ?, SYSDATE());";
			PreparedStatement pstmt = conn.prepareStatement(insertDCRSql);
			for(Map<String,String> dcr : dcrList) {
				pstmt.setString(1, dcr.get("locale"));
				pstmt.setString(2, dcr.get("dcrName"));
				pstmt.setString(3, dcr.get("attr"));
				pstmt.setString(4, dcr.get("val"));
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
	
	public static void insertHistoryDB(PreparedStatement pstmt, String pageName, String status, String locale) {
//		String sql = "INSERT INTO MIG_PDP_EXTRACT_STATUS(page,status,locale,creation_date) VALUES(?,?,?,SYSDATE()) ON DUPLICATE KEY UPDATE page=?,status=?,locale=?,modify_date=SYSDATE();";
		try {
//			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, pageName);
			pstmt.setString(2, status);
			pstmt.setString(3, locale);
			pstmt.setString(4, pageName);
			pstmt.setString(5, status);
			pstmt.setString(6, locale);
			pstmt.execute();
		}catch(SQLException e) {
			e.printStackTrace();
		}
	}
  
}