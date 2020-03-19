package net.sf.dframe.greed.pojo;


import com.alibaba.fastjson.JSONObject;

/**
 * 同步事件数据
 * @author Dody
 *
 */
public class EventData {

	private String table;
	
	private String schema;
	
	private JSONObject rowData; 
	
	public JSONObject beforeRowData;

	public JSONObject getRowData() {
		return rowData;
	}
	
	
	public String getSchema() {
		return schema;
	}
	
	public String getTable() {
		return table;
	}
	
	public void setRowData(JSONObject rowData) {
		this.rowData = rowData;
	}
	
	public void setSchema(String schema) {
		this.schema = schema;
	}
	
	public void setTable(String table) {
		this.table = table;
	}
	
	public void setBeforeRowData(JSONObject beforeRowData) {
		this.beforeRowData = beforeRowData;
	}
	
	public JSONObject getBeforeRowData() {
		return beforeRowData;
	}
}
