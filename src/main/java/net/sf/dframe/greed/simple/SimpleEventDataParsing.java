package net.sf.dframe.greed.simple;

import com.alibaba.fastjson.JSONObject;
import com.github.shyiko.mysql.binlog.event.*;
import net.sf.dframe.greed.pojo.*;
import net.sf.dframe.greed.pojo.EventData;
import net.sf.dframe.greed.pojo.EventType;
import net.sf.dframe.greed.service.AbstractSyncServer;
import net.sf.dframe.greed.service.SynchronizedListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;


/**
 * 处理事件解析及传递逻辑 
 * @author dy02
 *
 */
public class SimpleEventDataParsing {

	private static Logger log = LoggerFactory.getLogger(SimpleEventDataParsing.class);

	//cache schemaInfo
	private Map<String, Map<Integer, String>> schemaInfo;
	//cache event table map
	private Map<String, String> tableMap = new HashMap<String, String>();

	private static final String TABLE_MAP = "TABLEMAP";

	private String schemaName;

	//listener
	private SimpleDataListener listener;


	public SimpleEventDataParsing( String schemaName,Map<String, Map<Integer,String>> schemoInfo, SimpleDataListener listener) throws Exception {
//		this.server = server;
		this.schemaName = schemaName;
		this.listener = listener;
		this.schemaInfo = schemoInfo;////get columns info
	}

	/**
	 * 解析事件
	 * @param event
	 */
	public void parsingEvent(Event event, LogPosition lp ,String serviceId) {
		
		// change the Event and data
		com.github.shyiko.mysql.binlog.event.EventData data = event.getData();
		// 过滤掉不必要的事件
		// 过滤掉不必要事件
		if (event.getHeader().getEventType().name().endsWith("_ROWS")) {
			return; // 不处理
		}
		// record event info
		if (event.getHeader().getEventType() == com.github.shyiko.mysql.binlog.event.EventType.TABLE_MAP) {
			TableMapEventData tableData = event.getData();
			String db = tableData.getDatabase();
			String table = tableData.getTable();
			tableMap.put(TABLE_MAP, JSONObject.toJSONString(new TableMap(db, table)));
		}

		// 只处理添加删除更新三种操作

		if (data instanceof UpdateRowsEventData) {
			log.debug("*******update event:" + event.getHeader() + ";;;;" + data.toString());
			String tableMate = tableMap.get(TABLE_MAP);
			if (tableMate == null || tableMate.isEmpty()) {
				log.error(" !ERROR!  not tableMap event before; " + event.getHeader());
			} else {
				TableMap tablemap = JSONObject.parseObject(tableMate, TableMap.class);
				//not the aim schema,in mysql schema == database
				if (!schemaName.equals(tablemap.getDatabase())) {
					log.debug("not aim schema,do nothing!");
					return;
				} else {
					processUpdateEvent(event, tablemap ,lp ,serviceId);
				}
			}
		} else if (data instanceof WriteRowsEventData) {
			log.debug("***********insert event:" + event.getHeader() + ";;;;" + data.toString());
			String tableMate = tableMap.get(TABLE_MAP);
			if (tableMate == null || tableMate.isEmpty()) {
				log.error(" !ERROR!  not tableMap event before; " + event.getHeader());
			} else {
				TableMap tablemap = JSONObject.parseObject(tableMate, TableMap.class);
				//not the aim schema,in mysql schema == database
				if (!schemaName.equals(tablemap.getDatabase())) {
					log.debug("not aim schema,do nothing!");
					return;
				} else {
					processInsertEvent(event, tablemap ,lp ,serviceId);
				}
			}

		} else if (data instanceof DeleteRowsEventData) {
			log.debug("*********delete event:" + event.getHeader() + ";;;;" + data.toString());
			String tableMate = tableMap.get(TABLE_MAP);
			if (tableMate == null || tableMate.isEmpty()) {
				log.error(" !ERROR!  not tableMap event before; " + event.getHeader());
			} else {
				TableMap tablemap = JSONObject.parseObject(tableMate, TableMap.class);
				//not the aim schema,in mysql schema == database
				if (!schemaName.equals(tablemap.getDatabase())) {
					log.debug("not aim schema,do nothing!");
					return;
				} else {
					processDelete(event, tablemap,lp ,serviceId);
				}
			}
		}
	}

	/**
	 * delete Event Data
	 * 
	 * @param event
	 * @param tablemap
	 */
	private void processDelete(Event event, TableMap tablemap , LogPosition lp,String serviceId) {
		EventType eventType = EventType.DELETE;
		EventData eventData = new EventData();
		eventData.setSchema(tablemap.getDatabase());
		eventData.setTable(tablemap.getTable());
		JSONObject rowBefore = null;

		List<Serializable[]> rowdata = ((DeleteRowsEventData) event.getData()).getRows();
		String eventTable = tablemap.getTable();
		for (int i = 0; i < rowdata.size(); i++) {
			if (schemaInfo.get(eventTable) != null) {
				rowBefore = getInsertOrDeleteRowData(rowdata, eventTable, i);
			} else {
				log.error("No schemaInfo data!" + eventTable);
			}
			eventData.setRowData(rowBefore);
			SynchronizedEvent syncevent = new SynchronizedEvent();
			syncevent.setTimestamp(event.getHeader().getTimestamp());
			syncevent.setEventData(eventData);
			syncevent.setEventType(eventType);
			syncevent.setLogPosition(lp);
			syncevent.setSerivceId(serviceId);
//			listener.onDelete(syncevent);
			listener.onData(syncevent);
			 
		}
	}

	/**
	 * insert 事件处理
	 * 
	 * @param event
	 * @param tablemap
	 */
	private void processInsertEvent(Event event, TableMap tablemap,LogPosition lp ,String serviceId) {
		EventType eventType = EventType.INSERT;
		EventData eventData = new EventData();
		eventData.setSchema(tablemap.getDatabase());
		eventData.setTable(tablemap.getTable());
		JSONObject rowData = null;
		List<Serializable[]> rowdata = ((WriteRowsEventData) event.getData()).getRows();
		String eventTable = tablemap.getTable();
		for (int i = 0; i < rowdata.size(); i++) {
			if (schemaInfo.get(eventTable) != null) {
				rowData = getInsertOrDeleteRowData(rowdata, eventTable, i);
			} else {
				log.error("No schemaInfo data in cache , tablename :" + eventTable);

			}
			eventData.setRowData(rowData);
			SynchronizedEvent syncevent = new SynchronizedEvent();
			syncevent.setTimestamp(event.getHeader().getTimestamp());
			syncevent.setEventData(eventData);
			syncevent.setEventType(eventType);
			syncevent.setLogPosition(lp);
			syncevent.setSerivceId(serviceId);
//			listener.onInsert(syncevent);
			listener.onData(syncevent);
		}
	}

	/**
	 * 处理更新数据
	 * 
	 * @param event
	 * @param tablemap
	 */
	private void processUpdateEvent(Event event, TableMap tablemap ,LogPosition lp,String serviceId) {
		EventType eventType = EventType.UPDATE;
		EventData eventData = new EventData();
		eventData.setSchema(tablemap.getDatabase());
		eventData.setTable(tablemap.getTable());
		JSONObject rowData = new JSONObject();
		JSONObject rowBefore = new JSONObject();

		List<Entry<Serializable[], Serializable[]>> rowdata = ((UpdateRowsEventData) event.getData()).getRows();
		String eventTable = tablemap.getTable();
		for (int i = 0; i < rowdata.size(); i++) {
			if (schemaInfo.get(eventTable) != null) {
				rowBefore = getUpdateBeforeRowData(rowdata, eventTable, i);
				rowData = getUpdateRowData(rowdata, eventTable, i);

			} else {
				log.error("No schemaInfo data in cache , tablename :" + eventTable);
//
			}

			eventData.setRowData(rowData);
			eventData.setBeforeRowData(rowBefore);
			SynchronizedEvent syncevent = new SynchronizedEvent();
			syncevent.setTimestamp(event.getHeader().getTimestamp());
			syncevent.setEventData(eventData);
			syncevent.setEventType(eventType);
			syncevent.setLogPosition(lp);
			syncevent.setSerivceId(serviceId);
//			listener.onUpdate(syncevent);
			listener.onData(syncevent);
		}
	}

	/**
	 * get update after row data
	 * 
	 * @param rowdata
	 * @param eventTable
	 * @param i
	 */
	private JSONObject getUpdateRowData(List<Entry<Serializable[], Serializable[]>> rowdata, String eventTable, int i) {
		JSONObject rowData = new JSONObject();
		for (int index = 0; index < rowdata.get(i).getValue().length; index++) {
			String columnName = schemaInfo.get(eventTable).get(index + 1);
			Serializable k = rowdata.get(i).getValue()[index];
			rowData.put(columnName, k);
		}
		return rowData;
	}


	/**
	 * 
	 * @param rowdata
	 * @param eventTable
	 * @param i
	 */
	private JSONObject getInsertOrDeleteRowData(List<Serializable[]> rowdata, String eventTable, int i) {
		JSONObject rowData = new JSONObject();
		for (int index = 0; index < rowdata.get(i).length; index++) {
			String columnName = schemaInfo.get(eventTable).get(index + 1);
			Serializable k = rowdata.get(i)[index];
			rowData.put(columnName, k);
		}
		return rowData;
	}

	/**
	 * update before row data
	 * 
	 * @param rowdata
	 * @param eventTable
	 * @param i
	 */
	private JSONObject getUpdateBeforeRowData(List<Entry<Serializable[], Serializable[]>> rowdata, String eventTable,
			int i) {
		JSONObject rowBefore = new JSONObject();
		for (int index = 0; index < rowdata.get(i).getKey().length; index++) {
			String columnName = schemaInfo.get(eventTable).get(index + 1);
			Serializable k = rowdata.get(i).getKey()[index];
			rowBefore.put(columnName, k);
		}
		return rowBefore;
	}

}
