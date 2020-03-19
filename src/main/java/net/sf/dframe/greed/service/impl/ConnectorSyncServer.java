package net.sf.dframe.greed.service.impl;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.github.shyiko.mysql.binlog.BinaryLogClient;
import com.github.shyiko.mysql.binlog.BinaryLogClient.EventListener;
import com.github.shyiko.mysql.binlog.event.Event;
import com.github.shyiko.mysql.binlog.event.deserialization.EventDeserializer;

import net.sf.dframe.cluster.hazelcast.HazelcastMasterSlaveCluster;
import net.sf.dframe.greed.pojo.GreedConfig;
import net.sf.dframe.greed.pojo.LogPosition;
import net.sf.dframe.greed.service.ISyncService;
import net.sf.dframe.greed.service.SynchronizedListenerAdapter;




/**
 * 同步服务,支持集群
 * 主从模式，确保每一个时间点只有主服务运行。
 * 
 * @author Dody
 *
 */
public class ConnectorSyncServer implements ISyncService {

	private static Logger log = LoggerFactory.getLogger(ConnectorSyncServer.class);

	private static final String POSITION = "POSITION";
	
	private BinaryLogClient client;

	private HazelcastMasterSlaveCluster cluster;

//	private String hostname = "localhost";
//	private int port = 3307;
//	private String schema = "testbinlog";
//	private String username = "root";
//	private String password = "root";
//	private String dbDriver = "com.mysql.cj.jdbc.Driver";
	private GreedConfig config;
	
	private ConnectorSyncEventDataParsing parsing = null;
	
	private SynchronizedListenerAdapter listener;
	
	public ConnectorSyncServer(GreedConfig config) {
		this.config = config;
	}
	
	public ConnectorSyncServer() {}
	
	/**
	 * Start a synchronized data server
	 */
	@Override
	public void start() throws Exception {
		//create a clinet 
		client = new BinaryLogClient(config.getHost(), config.getPort(), config.getSchema(), config.getUser(), config.getPassword());
		//get columns info 
//		schemaInfo = initSchema();
		//check log
		checkLogPosition();
		//init listener 
		parsing = new ConnectorSyncEventDataParsing(this, listener);
		
		EventDeserializer eventDeserializer = new EventDeserializer();
		eventDeserializer.setCompatibilityMode(EventDeserializer.CompatibilityMode.DATE_AND_TIME_AS_LONG);
		client.setEventDeserializer(eventDeserializer);
		
		
		client.registerEventListener(new EventListener() {

			@Override
			public void onEvent(Event event) {
				log.info(" receiver event: " + event.getHeader() + "," + event.getData());

				if (cluster.isMeActive()) {
					log.info("current node is master node, do process");
					
					//to do event data process
					parsing.parsingEvent(event);
					
					//record the position
					updateLogPosition();

				} else {
					log.info("current node is not master node, do nothing! master node is :"+cluster.getActive());
				}
			}
			
		});
		client.connect();
	}
	
	
	
	/**
	 * record the current position
	 */
	public void updateLogPosition() {
		LogPosition  lp  = new LogPosition();
		lp.setLogfile(client.getBinlogFilename());
		lp.setPosition(client.getBinlogPosition());
		cluster.getArributesMap().put(POSITION, JSONObject.toJSONString(lp));
	}

	/**
	 * check the binlog position
	 */
	private void checkLogPosition() {
		// 处理未处理数据
		log.info("check log postion");
		try {
			LogPosition lp = getPosition();
			if (lp != null) {
				client.setBinlogFilename(lp.getLogfile());
				client.setBinlogPosition(lp.getPosition());
				log.info("set filename to :" + lp.getLogfile() + " and position to :" + lp.getPosition());
			}
		} catch (Exception e) {
			log.error("get log position exception", e);
		}
	}
	
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

	/**
	 * get log position
	 * @return
	 */
	private LogPosition getPosition() {
		LogPosition lp = null;
		String strLp = cluster.getArributesMap().get(POSITION);
		if (strLp != null && (!strLp.isEmpty())) {
			lp = JSONObject.parseObject(strLp, LogPosition.class);
		}
		return lp;
	}
	
	/**
	 * stop server
	 */
	@Override
	public void stop() {
		try {
			client.disconnect();
		} catch (IOException e) {
			log.error("stop server exception",e);
		}
	}

	public HazelcastMasterSlaveCluster getCluster() {
		return cluster;
	}

	public void setCluster(HazelcastMasterSlaveCluster cluster) {
		this.cluster = cluster;
	}
	
	public void setListener(SynchronizedListenerAdapter listener) {
		this.listener = listener;
	}

	public void setConfig(GreedConfig config) {
		this.config = config;
	}
	public GreedConfig getConfig() {
		return config;
	}
}
