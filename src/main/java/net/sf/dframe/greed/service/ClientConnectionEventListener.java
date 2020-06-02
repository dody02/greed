package net.sf.dframe.greed.service;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.shyiko.mysql.binlog.BinaryLogClient;
import com.github.shyiko.mysql.binlog.BinaryLogClient.LifecycleListener;

import net.sf.dframe.greed.pojo.LogPosition;
import net.sf.dframe.greed.service.impl.ConnectorSyncServer;
import net.sf.dframe.greed.service.impl.FreeConnectorSyncServer;
/**
 * Connection event listener
 * @author Dody
 *
 */
public class ClientConnectionEventListener implements LifecycleListener{

	private static Logger log = LoggerFactory.getLogger(ClientConnectionEventListener.class);
	
	private long timeout = 60000; 
	
	private IConnectionListener listener = null;
	
	private AbstractSyncServer server ;
	
	private boolean retrylogerr = false;
	
	
	private int maxRetryTime = 3;
	
	
	
	
	public ClientConnectionEventListener (AbstractSyncServer server) {
		this.server = server;
		retrylogerr= server.getConfig().isRetrylogerr();
	}
	
	
	public void onConnect(BinaryLogClient client) {
		log.info("Client Connected! ");
		if (listener != null) {
			listener.onConnected(server);
		}
	}

	@Override
	public void onCommunicationFailure(BinaryLogClient client, Exception ex) {
		log.error("Communication Faulure:",ex);
		
		if (ex instanceof  com.github.shyiko.mysql.binlog.network.ServerException) {
			if (ex.getMessage().contains("Could not find first log file name in binary log index file")) {
				log.warn("Could not find first log file name in binary log index file",ex);
				if (server instanceof ConnectorSyncServer) {
					if (retrylogerr ) { // reset and try again
						log.info("binlog file position not synchronized, try to reset posion and reconnected");
						try {
							((ConnectorSyncServer)server).setPosition(new LogPosition());
						} catch (IOException e) {
							log.error("re connection exception ",e);
						}
					}	
				} else {
					log.info("reset posistion");
					client.setBinlogFilename(null);
					client.setBinlogPosition(0);
				}
			} else {
				log.error("CommunicationFailure",ex);
			}
				
		}
		
		if (listener != null)
			listener.onError(server,ex);
	
		
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
		if (server instanceof FreeConnectorSyncServer) {
			server.resetPosition();
//			client.setBinlogFilename(null);
//			client.setBinlogPosition(0);
		}
		if (listener != null) {
			listener.onDisConnected(server);
		} 
		
	}

	
	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}
	public long getTimeout() {
		return timeout;
	}
	
	public IConnectionListener getListener() {
		return listener;
	}
	public void setListener(IConnectionListener listener) {
		this.listener = listener;
	}


	public int getMaxRetryTime() {
		return maxRetryTime;
	}
	
	

	public void setMaxRetryTime(int maxRetryTime) {
		this.maxRetryTime = maxRetryTime;
	}
	
	
	
//	class RestartThread extends Thread {
//		public void run () {
//			try {
//				server.start();
//			} catch (Exception e) {
//				log.error("RESTART SERVER ERROR ",e);
//			}
//			while (! server.isStarted()) {
//				try {
//					server.stop();
//					Thread.sleep(timeout);
//					server.start();
//					} catch (Exception e1) {
//						log.error("server restart ERROR^  ",e1);
//					}
//			}
//
//		}
//	}


	
}
