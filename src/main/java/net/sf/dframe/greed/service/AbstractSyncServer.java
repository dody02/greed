package net.sf.dframe.greed.service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.shyiko.mysql.binlog.BinaryLogClient;

import net.sf.dframe.greed.pojo.GreedConfig;
import net.sf.dframe.greed.pojo.LogPosition;

public abstract class AbstractSyncServer implements ISyncService{

	
	private static Logger log = LoggerFactory.getLogger(AbstractSyncServer.class);
	
	protected GreedConfig config;
	
	protected BinaryLogClient client = null;
	/**
	 * get Schema info
	 * @throws Exception 
	 */
	public  Map<String,Map<Integer,String>> initSchema() throws Exception {
		Class.forName(config.getDrivername());
//		Class.forName("com.mysql.jdbc.Driver ");
		Connection conn = null;
		Statement statement = null;
		ResultSet rs = null;
		Map<String,Map<Integer,String>> schemainfo = new HashMap<String,Map<Integer,String>>();
		try {
			conn = DriverManager.getConnection("jdbc:mysql://"+config.getHost()+":"+config.getPort()+"/"+config.getSchema()+"?useSSL=false&serverTimezone=UTC",config.getUser(),config.getPassword());
//			conn = DriverManager.getConnection("jdbc:mysql://localhost:3307/testbinlog?useSSL=false&serverTimezone=UTC",this.username,this.password);
			statement = conn.createStatement();
			String sql = "select TABLE_SCHEMA, TABLE_NAME, COLUMN_NAME, ORDINAL_POSITION from INFORMATION_SCHEMA.COLUMNS  WHERE table_schema = '"+config.getSchema()+"';";
			rs = statement.executeQuery(sql);
			while (rs.next()) {
				Map<Integer,String> columnsInfo = schemainfo.get(rs.getString("TABLE_NAME")) ;
				if (columnsInfo == null) {
					columnsInfo = new HashMap<Integer,String>();
				}
				columnsInfo.put(rs.getInt("ORDINAL_POSITION"),rs.getString("COLUMN_NAME"));
				schemainfo.put(rs.getString("TABLE_NAME"), columnsInfo);//set table
			}
			
		}catch (Exception e ) {
			log.error("initSchema Exception",e);
		} finally {
			if (rs != null) {
				try {
					rs.close();
					rs = null;
				} catch (Exception e) {
					rs =null;
				}
			}
			if (statement != null) {
				try {
					statement.close();
					statement = null;
				} catch (Exception e) {
					statement =null;
				}
			}
			if (conn != null) {
				try {
					conn.close();
					conn = null;
				} catch (Exception e) {
					conn =null;
				}
			}
		}
		return schemainfo;
		
	}
	
	public void setConfig(GreedConfig config) {
		this.config = config;
	}
	public GreedConfig getConfig() {
		return config;
	}
	
	/**
	 * reset client position
	 */
	public void resetPosition () {
		if (config != null) {
			config.setLogposition(new LogPosition());
		}
		if (client != null ) {
			client.setBinlogFilename(null);
			client.setBinlogPosition(0);	
		} 
		
	}


}
