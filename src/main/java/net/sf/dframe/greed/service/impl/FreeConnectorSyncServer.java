package net.sf.dframe.greed.service.impl;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.shyiko.mysql.binlog.BinaryLogClient;
import com.github.shyiko.mysql.binlog.BinaryLogClient.EventListener;
import com.github.shyiko.mysql.binlog.event.Event;
import com.github.shyiko.mysql.binlog.event.deserialization.EventDeserializer;

import net.sf.dframe.cluster.hazelcast.HazelcastMasterSlaveCluster;
import net.sf.dframe.greed.config.LoadJsonConfig;
import net.sf.dframe.greed.pojo.GreedConfig;
import net.sf.dframe.greed.service.AbstractSyncServer;
import net.sf.dframe.greed.service.ClientConnectionEventListener;
import net.sf.dframe.greed.service.IConnectionListener;
import net.sf.dframe.greed.service.SynchronizedListenerAdapter;

public class FreeConnectorSyncServer extends AbstractSyncServer {

	private static Logger log = LoggerFactory.getLogger(FreeConnectorSyncServer.class);

	private ConnectorSyncEventDataParsing parsing = null;

	private String url = "greed.json";

	private ClientConnectionEventListener connlistener;

	public FreeConnectorSyncServer(GreedConfig config) throws Exception {
		if (config == null) {
			config = LoadJsonConfig.readConfig(url);
		} else {
			this.config = config;
		}
		this.connlistener = new ClientConnectionEventListener(this);
	}

	public FreeConnectorSyncServer(GreedConfig config, IConnectionListener clientlistener) throws Exception {
		if (config == null) {
			config = LoadJsonConfig.readConfig(url);
		} else {
			this.config = config;
		}
		this.connlistener = new ClientConnectionEventListener(this);
		connlistener.setListener(clientlistener);
	}

	@Override
	public void start() throws Exception {
		// create a clinet
		client = new BinaryLogClient(config.getHost(), config.getPort(), config.getSchema(), config.getUser(),
				config.getPassword());
		// client.setBlocking(config.isBlock());
		// set id
		if (config.getServerid() != 0L) {
			client.setServerId(config.getServerid());
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
						
					} catch (Exception e) {
						log.error("Parsing Event data Exception",e);
					}
					

				} else {
					log.info("current node is not master node, do nothing! master node is :"+cluster.getActive());
				}
			}
			
		});
		
		client.registerLifecycleListener(connlistener);
		if (config.getConntimeout() >0 ) {
			log.debug("try to connect,time out setting :"+config.getConntimeout());
			client.connect(config.getConntimeout());
		}else {
			client.connect();
		}
	}

	@Override
	public void stop() {
		try {
			client.unregisterLifecycleListener(connlistener);
			if (client.isConnected())
				client.disconnect();
		} catch (IOException e) {
			log.error("stop server exception",e);
		} finally {
			client = null;
		}

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
