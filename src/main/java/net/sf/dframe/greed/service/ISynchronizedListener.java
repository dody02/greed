package net.sf.dframe.greed.service;

import net.sf.dframe.greed.pojo.SynchronizedEvent;

/**
 * 同步事件监听
 * @author Dody
 *
 */
public interface ISynchronizedListener {
	
	/**
	 * 有同步事件数据
	 * @param event
	 */
	public void onData(SynchronizedEvent event);
	
}
