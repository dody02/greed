package net.sf.dframe.greed.service;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.shyiko.mysql.binlog.BinaryLogClient;
import com.github.shyiko.mysql.binlog.BinaryLogClient.LifecycleListener;

import net.sf.dframe.greed.pojo.LogPosition;
import net.sf.dframe.greed.service.impl.ConnectorSyncServer;
/**
 * Connection event listener
 * @author Dody
 *
 */
public class ClientConnectionEventListener implements LifecycleListener{

	private static Logger log = LoggerFactory.getLogger(ConnectorSyncServer.class);
	
	private long timeout = 60000; 
	
	private IConnectionListener listener = null;
	
	private ConnectorSyncServer server ;
	
	private boolean retrylogerr = false;
	
	
	private int maxRetryTime = 3;
	
	
	
	
	public ClientConnectionEventListener (ConnectorSyncServer server) {
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
				if (retrylogerr ) { // reset and try again
					log.info("binlog file position not synchronized, try to reset posion and reconnected");
					try {
						server.setPosition(new LogPosition());
					} catch (IOException e) {
						log.error("re connection exception ",e);
					}
				} 
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
		
//		if (autoConnect) {
//			log.info("auto Connection processing ……");
//			//1. stop server 
//			server.stop();
//			log.info("stop synchronization server ……");
//			//2. restart server
//			RestartThread rt = new RestartThread();
//			rt.start();
//			log.info("append retry time !");
//				
//			try {
//				Thread.sleep(timeout);
//				while (!server.isStarted() && (this.currentRetryTime <= this.maxRetryTime)) {
//					log.info("checked restart server result : server is not started yet!");
//					retry();//叠加
//					Thread.sleep(timeout);
//				}
//					
//			} catch (InterruptedException e) {
//			}
//			if (!server.isStarted()) {
//				try {
//					server.stop();
//					rt.interrupt();
//				} catch(Exception e) {
//					log.warn("stop retry connected server exception ",e);
//				}
//				server.getConfig().setLogposition(new LogPosition());
//				RestartThread rt2 = new RestartThread();
//				rt2.start();
//			}
//			
//		} else {// END Auto reconnected
//			log.info("NO AUTO RECONNECTE CONFIG,DO NOTHING");
//		} 
		
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
