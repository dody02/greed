package net.sf.dframe.greed.service;

public interface IConnectionListener {
	public void onConnected(ISyncService server);
	public void onDisConnected(ISyncService server);
	public void onError(ISyncService server,Exception ex);
}
