package net.sf.dframe.greed.pojo;

/**
 * 表信息
 * @author dy02
 *
 */
public class TableMap {
	
	private String database;
	
	private String table;

	public TableMap(String database,String table) {
		this.database = database;
		this.table = table;
	}
	
	public String getDatabase() {
		return database;
	}

	public void setDatabase(String database) {
		this.database = database;
	}

	public String getTable() {
		return table;
	}

	public void setTable(String table) {
		this.table = table;
	}
	
	
}
