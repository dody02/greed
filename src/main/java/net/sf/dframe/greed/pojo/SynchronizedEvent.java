package net.sf.dframe.greed.pojo;

import com.alibaba.fastjson.JSONObject;

/**
 * 
 * 同步事件
 * @author Dody
 *
 */
public class SynchronizedEvent {
	
	private EventType eventType;
	
	private EventData eventData;

	public LogPosition getLogPosition() {
		return logPosition;
	}

	public void setLogPosition(LogPosition logPosition) {
		this.logPosition = logPosition;
	}

	private long timestamp;

	private LogPosition logPosition;
	
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

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
	
}
