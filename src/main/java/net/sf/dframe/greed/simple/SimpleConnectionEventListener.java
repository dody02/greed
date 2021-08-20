package net.sf.dframe.greed.simple;

import com.github.shyiko.mysql.binlog.BinaryLogClient;
import com.github.shyiko.mysql.binlog.BinaryLogClient.LifecycleListener;
import net.sf.dframe.greed.service.IConnectionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Connection event listener
 * @author Dody
 *
 */
public class SimpleConnectionEventListener implements LifecycleListener{

	private static Logger log = LoggerFactory.getLogger(SimpleConnectionEventListener.class);

	private long timeout = 60000;

	private SimpleSyncMysqlDataService server ;





	public SimpleConnectionEventListener(SimpleSyncMysqlDataService server) {
		this.server = server;
	}
	
	
	public void onConnect(BinaryLogClient client) {
		log.info("Client Connected! ");

	}

	@Override
	public void onCommunicationFailure(BinaryLogClient client, Exception ex) {
		log.error("Communication Faulure:",ex);
		
		if (ex instanceof  com.github.shyiko.mysql.binlog.network.ServerException) {
			if (ex.getMessage().contains("Could not find first log file name in binary log index file")) {
				log.warn("Could not find first log file name in binary log index file",ex);
				log.info("binlog file position not synchronized, try to reset posion and reconnected");
				try {
					server.resetPosition();
				} catch (Exception e) {
					log.error("re connection exception ",e);
				}
			} else {
				log.error("CommunicationFailure",ex);
			}
				
		}

	}

	//
	@Override
	public void onEventDeserializationFailure(BinaryLogClient client, Exception ex) {
		log.warn("Communication Issue , try to disconnected and reConnect",ex);
		
		try {
			client.disconnect();
		} catch (Exception e) {
			log.error("client disconnected error",e);
		}
	}

	@Override
	public void onDisconnect(BinaryLogClient client) {
		server.resetPosition();
	}

	}
