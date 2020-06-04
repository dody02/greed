package net.sf.dframe.greed.service.impl;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.github.shyiko.mysql.binlog.BinaryLogClient;
import com.github.shyiko.mysql.binlog.BinaryLogClient.EventListener;
import com.github.shyiko.mysql.binlog.event.Event;
import com.github.shyiko.mysql.binlog.event.deserialization.EventDeserializer;

import net.sf.dframe.cluster.hazelcast.HazelcastMasterSlaveCluster;
import net.sf.dframe.greed.config.LoadJsonConfig;
import net.sf.dframe.greed.pojo.GreedConfig;
import net.sf.dframe.greed.pojo.LogPosition;
import net.sf.dframe.greed.service.AbstractSyncServer;
import net.sf.dframe.greed.service.ClientConnectionEventListener;
import net.sf.dframe.greed.service.IConnectionListener;
import net.sf.dframe.greed.service.SynchronizedListenerAdapter;




/**
 * 同步服务,支持集群
 * 主从模式，确保每一个时间点只有主服务运行。
 * 
 * @author Dody
 *
 */
public class ConnectorSyncServer extends AbstractSyncServer {

	private static Logger log = LoggerFactory.getLogger(ConnectorSyncServer.class);

	private static final String POSITION = "POSITION";
	
//	private BinaryLogClient client;

//	private HazelcastMasterSlaveCluster cluster;

	//private GreedConfig config;
	
	private ConnectorSyncEventDataParsing parsing = null;
	
	
	private String url = "greed.json";
	
	private ClientConnectionEventListener connlistener;
	
	
	public ConnectorSyncServer(GreedConfig config) throws Exception {
		if(config == null) {
			config = LoadJsonConfig.readConfig(url);
		} else {
			this.config = config;
		}
		this.connlistener = new ClientConnectionEventListener(this);
	}
	

	public ConnectorSyncServer(GreedConfig config,IConnectionListener clientlistener) {
		this.config = config;
		this.connlistener = new ClientConnectionEventListener(this);
		connlistener.setListener(clientlistener);
	}
	

	
	/**
	 * Start a synchronized data server
	 */
	@Override
	public void start() throws Exception {
		//create a clinet 
		client = new BinaryLogClient(config.getHost(), config.getPort(), config.getSchema(), config.getUser(), config.getPassword());
		//client.setBlocking(config.isBlock());
		//set id
		if (config.getServerid() != 0L) {
			client.setServerId(config.getServerid());
		}
		//check log
		if (config.getLogposition() != null && config.getLogposition().getLogfile() != null ) {
			this.setPosition(config.getLogposition());
			log.info("set position in config file");
		} else {
			checkLogPosition();
			log.info("set position in cluster cache");
		}
		//init listener 
		parsing = new ConnectorSyncEventDataParsing(this, listener);
		
		EventDeserializer eventDeserializer = new EventDeserializer();
		eventDeserializer.setCompatibilityMode(EventDeserializer.CompatibilityMode.DATE_AND_TIME_AS_LONG);
		client.setEventDeserializer(eventDeserializer);
		
		
		client.registerEventListener(new EventListener() {

			@Override
			public void onEvent(Event event) {
				log.debug(" receiver event: " + event.getHeader() + "," + event.getData());

				if (cluster.isMeActive()) {
					log.debug("current node is master node, do process");
					
					//to do event data process
					try {
						parsing.parsingEvent(event);
						//record the position
						updateLogPosition();
					} catch (Exception e) {
						log.error("Parsing Event data Exception",e);
					}
					

				} else {
					log.info("current node is not master node, do nothing! master node is :"+cluster.getActive());
				}
			}
			
		});
		
		client.registerLifecycleListener(connlistener);
		
		
		client.setKeepAlive(config.isAutoreconn());//reconnected by manual
		if (config.isAutoreconn())
			client.setKeepAliveInterval(config.getReconntimer());
		
		
		if (config.getConntimeout() >0 ) {
			log.debug("try to connect,time out setting :"+config.getConntimeout());
			client.connect(config.getConntimeout());
		}else {
			client.connect();
		}
		
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
		log.debug("check log postion");
		try {
			LogPosition lp = getPosition();
			if (lp != null) {
				client.setBinlogFilename(lp.getLogfile());
				client.setBinlogPosition(lp.getPosition());
				log.debug("set filename to :" + lp.getLogfile() + " and position to :" + lp.getPosition());
			}
		} catch (Exception e) {
			log.error("get log position exception", e);
		}
	}
	
//	

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
	 * 设置位置
	 * @param position
	 * @throws IOException 
	 */
	public void setPosition(LogPosition position) throws IOException {
		
		if ( client != null) {
			if (position == null) {
				client.setBinlogFilename(null);
				client.setBinlogPosition(0);
				cluster.getArributesMap().remove(POSITION);
			}else {
				client.setBinlogFilename(position.getLogfile());
				client.setBinlogPosition(position.getPosition());
				cluster.getArributesMap().put(POSITION, JSONObject.toJSONString(position));
				
			}
			if (client.isConnected()) {
					client.disconnect();
					if (!config.isAutoreconn()) {
						client.connect();
					}
				
			}
		} else {
			cluster.getArributesMap().put(POSITION, JSONObject.toJSONString(new LogPosition()));
		}
		
	}
	
	/**
	 * stop server
	 */
	@Override
	public void stop() {
		try {
			
			client.unregisterLifecycleListener(connlistener);
			if (client.isConnected())
				client.disconnect();
			if (config.isAutoreconn()) {  //自动连接时，需要关线程
				Thread ct = findThread(client.getConnectionId());
				if (ct != null) {
					try {
						ct.interrupt();
					}catch (Exception e ) {
						log.error("stop reconnetiong Exception");
					}
				}else {
					log.info("client Thread is null!");
				}
			}
			
			
		} catch (IOException e) {
			log.error("stop server exception",e);
		} finally {
			client = null;
		}
	}

//	public HazelcastMasterSlaveCluster getCluster() {
//		return cluster;
//	}
//
//	public void setCluster(HazelcastMasterSlaveCluster cluster) {
//		this.cluster = cluster;
//	}
//	
//	public void setListener(SynchronizedListenerAdapter listener) {
//		this.listener = listener;
//	}

//	public void setConfig(GreedConfig config) {
//		this.config = config;
//	}
//	public GreedConfig getConfig() {
//		return config;
//	}

	/**
	 * Find thread 
	 * @param threadId
	 * @return
	 */
	private Thread findThread(long threadId) {
	    ThreadGroup group = Thread.currentThread().getThreadGroup();
	    while(group != null) {
	        Thread[] threads = new Thread[(int)(group.activeCount() * 1.2)];
	        int count = group.enumerate(threads, true);
	        for(int i = 0; i < count; i++) {
	            if(threadId == threads[i].getId()) {
	                return threads[i];
	            }
	        }
	        group = group.getParent();
	    }
	    return null;
	}

	@Override
	public boolean isStarted() {
		if (this.client != null) {
			if (client.isConnected())
				return true;
		}
		return false;
	}
	
}
