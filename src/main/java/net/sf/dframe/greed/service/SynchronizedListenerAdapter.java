package net.sf.dframe.greed.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.dframe.greed.pojo.EventType;
import net.sf.dframe.greed.pojo.SynchronizedEvent;


/**
 * 
 * @author dy02
 *
 */
public abstract class SynchronizedListenerAdapter implements ISynchronizedListener{
		
private static Logger log = LoggerFactory.getLogger(SynchronizedListenerAdapter.class);
	
	@Override
	public void onData(SynchronizedEvent event) {
		log.debug("event:"+event);
		if ( event.getEventType() == EventType.DELETE) {
			onDelete(event);
		}
		if (event.getEventType() == EventType.INSERT) {
			onInsert(event);
		}
		if (event.getEventType() == EventType.UPDATE) {
			onUpdate(event);
		}
	}
	
	
	/**
	 * deleteEvent
	 * @param event
	 */
	public abstract void onDelete(SynchronizedEvent event);
	/**
	 * insertEvent
	 * @param event
	 */
	public abstract void onInsert(SynchronizedEvent event);
	/**
	 * updateEvent
	 * @param event
	 */
	public abstract void onUpdate(SynchronizedEvent event);
	
}
