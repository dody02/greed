package net.sf.dframe.greed.pojo;

import com.alibaba.fastjson.JSONObject;

/**
 * 
 * 同步事件
 * @author Dody
 *
 */
public class SynchronizedEvent {
	
	EventType eventType;
	
	EventData eventData;

	public EventType getEventType() {
		return eventType;
	}

	public void setEventType(EventType eventType) {
		this.eventType = eventType;
	}

	public EventData getEventData() {
		return eventData;
	}

	public void setEventData(EventData eventData) {
		this.eventData = eventData;
	}
	
	public String toString() {
		return JSONObject.toJSONString(this);
	}
	
}
