package com.lge.pdp.model;


public class PdpExtract {
	
	// Page 명
	private String page;
	
	// Component ID
	private String comp;
	
	// Attribute 명 ( GroupID + DatumID )
	private String attr;
	
	// Value 
	private String val;
	
	// Page XML내에 배치 된 Component 순서
	private String compOrder;
	
	// Replicant를 사용하는 Data일 때 순서 Default 값 1
	private String repliOrder;
	
	// 국가코드
	private String locale;
	
	// Datum의 Type Attribute 값
	private String type;
	
	// Datum의 label 값
	private String label;
	
	private int excelOrder;
	
	
	public PdpExtract() {
		this.page = "";
		this.comp = "";
		this.attr = "";
		this.val = "";
		this.compOrder = "";
		this.repliOrder = "";
		this.locale = "";
		this.type = "";
		this.label = "";
	}
	
	
	
	public int getExcelOrder() {
		return excelOrder;
	}

	public void setExcelOrder(int excelOrder) {
		this.excelOrder = excelOrder;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getPage() {
		return page;
	}

	public void setPage(String page) {
		this.page = page;
	}

	public String getComp() {
		return comp;
	}

	public void setComp(String comp) {
		this.comp = comp;
	}

	public String getAttr() {
		return attr;
	}

	public void setAttr(String attr) {
		this.attr = attr;
	}

	public String getVal() {
		return val;
	}

	public void setVal(String val) {
		this.val = val;
	}

	public String getCompOrder() {
		return compOrder;
	}

	public void setCompOrder(String compOrder) {
		this.compOrder = compOrder;
	}

	public String getRepliOrder() {
		return repliOrder;
	}

	public void setRepliOrder(String repliOrder) {
		this.repliOrder = repliOrder;
	}

	public String getLocale() {
		return locale;
	}

	public void setLocale(String locale) {
		this.locale = locale;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
	public void printString() {
		System.out.println("Locale :: " + this.locale + " :: Page 명 :: " + this.page + " :: Component ID :: " + this.comp + " :: Component 순서 :: " + this.compOrder + " :: Replicant 순서 :: " + this.repliOrder + " :: Type :: " + this.type + " :: Attr :: " + this.attr + " :: Value :: " + this.val + " :: Label :: " + this.label + " :: excelOrder :: " + this.excelOrder);
	}
	
}
