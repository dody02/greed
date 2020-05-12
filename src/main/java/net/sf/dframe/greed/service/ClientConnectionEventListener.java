package net.sf.dframe.greed.service;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.shyiko.mysql.binlog.BinaryLogClient;
import com.github.shyiko.mysql.binlog.BinaryLogClient.LifecycleListener;

import net.sf.dframe.greed.service.impl.ConnectorSyncServer;
/**
 * Connection event listener
 * @author Dody
 *
 */
public class ClientConnectionEventListener implements LifecycleListener{

	private static Logger log = LoggerFactory.getLogger(ConnectorSyncServer.class);
	
	private long timeout = 20000; 
	
	private IConnectionListener listener = null;
	
	private ConnectorSyncServer server ;
	
	
	public ClientConnectionEventListener (ConnectorSyncServer server) {
		this.server = server;
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
	}

	@Override
	public void onEventDeserializationFailure(BinaryLogClient client, Exception ex) {
		log.warn("Communication Issue , try to disconnected and reConnect",ex);
		try {
			client.disconnect();
		} catch (IOException e) {
			log.error("client disconnected error",e);
		}
	}

	@Override
	public void onDisconnect(BinaryLogClient client) {
		if (listener == null) {
			try {
				client.connect(timeout);
			} catch (IOException e) {
				log.error("retry to connected mysql db exception",e);
			} catch (TimeoutException e) {
				log.error("retry to connected mysql db exception",e);
			}
		} else {
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
	
}
