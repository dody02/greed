package net.sf.dframe.greed.service;

public interface IConnectionListener {
	public void onConnected(ISyncService server);
	public void onDisConnected(ISyncService server);
}
