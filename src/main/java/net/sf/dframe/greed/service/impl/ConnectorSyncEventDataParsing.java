package net.sf.dframe.greed.service.impl;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.github.shyiko.mysql.binlog.event.DeleteRowsEventData;
import com.github.shyiko.mysql.binlog.event.Event;
import com.github.shyiko.mysql.binlog.event.TableMapEventData;
import com.github.shyiko.mysql.binlog.event.UpdateRowsEventData;
import com.github.shyiko.mysql.binlog.event.WriteRowsEventData;

import net.sf.dframe.greed.pojo.EventData;
import net.sf.dframe.greed.pojo.EventType;
import net.sf.dframe.greed.pojo.SynchronizedEvent;
import net.sf.dframe.greed.pojo.TableMap;
import net.sf.dframe.greed.service.SynchronizedListenerAdapter;



/**
 * 处理事件解析及传递逻辑 
 * @author dy02
 *
 */
public class ConnectorSyncEventDataParsing {

	private static Logger log = LoggerFactory.getLogger(ConnectorSyncEventDataParsing.class);
	
	//cache schemaInfo
	private Map<String, Map<Integer, String>> schemaInfo;
	//cache event table map 
	private Map<String, String> tableMap = new HashMap<String, String>();

	private static final String TABLE_MAP = "TABLEMAP";
	
	//listener
	private SynchronizedListenerAdapter listener;
	//server
	private ConnectorSyncServer server;

	public ConnectorSyncEventDataParsing(ConnectorSyncServer server,SynchronizedListenerAdapter listener) throws Exception {
		this.server = server;
		this.listener = listener;
		schemaInfo = server.initSchema();////get columns info 
	}

	/**
	 * 解析事件
	 * @param event
	 */
	public void parsingEvent(Event event) {
		
		// change the Event and data
		com.github.shyiko.mysql.binlog.event.EventData data = event.getData();

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
				processUpdateEvent(event, tablemap);
			}
		} else if (data instanceof WriteRowsEventData) {
			log.debug("***********insert event:" + event.getHeader() + ";;;;" + data.toString());
			String tableMate = tableMap.get(TABLE_MAP);
			if (tableMate == null || tableMate.isEmpty()) {
				log.error(" !ERROR!  not tableMap event before; " + event.getHeader());
			} else {
				TableMap tablemap = JSONObject.parseObject(tableMate, TableMap.class);
				processInsertEvent(event, tablemap);
			}

		} else if (data instanceof DeleteRowsEventData) {
			log.debug("*********delete event:" + event.getHeader() + ";;;;" + data.toString());
			String tableMate = tableMap.get(TABLE_MAP);
			if (tableMate == null || tableMate.isEmpty()) {
				log.error(" !ERROR!  not tableMap event before; " + event.getHeader());
			} else {
				TableMap tablemap = JSONObject.parseObject(tableMate, TableMap.class);
				processDelete(event, tablemap);
			}
		}
	}

	/**
	 * delete Event Data
	 * 
	 * @param data
	 * @param tablemap
	 */
	private void processDelete(Event event, TableMap tablemap) {
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
				log.error("No schemaInfo data in cache,try to reload……  , tablename :" + eventTable);
				try {
					schemaInfo = server.initSchema();
					if (schemaInfo.get(eventTable) != null) {
						rowBefore = getInsertOrDeleteRowData(rowdata, eventTable, i);
					} else {
						log.error("No schemaInfo data after reload : " + eventTable);
					}
				} catch (Exception e) {
					log.error("reload schemaInfo exception", e);
				}
			}
			eventData.setRowData(rowBefore);
			SynchronizedEvent syncevent = new SynchronizedEvent();
			syncevent.setTimestamp(event.getHeader().getTimestamp());
			syncevent.setEventData(eventData);
			syncevent.setEventType(eventType);

			listener.onDelete(syncevent);
			 
		}
	}

	/**
	 * insert 事件处理
	 * 
	 * @param data
	 * @param tablemap
	 */
	private void processInsertEvent(Event event, TableMap tablemap) {
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
				log.error("No schemaInfo data in cache,try to reload……  , tablename :" + eventTable);
				try {
					schemaInfo = server.initSchema();
					if (schemaInfo.get(eventTable) != null) {
						rowData = getInsertOrDeleteRowData(rowdata, eventTable, i);
					} else {
						log.error("No schemaInfo data after reload : " + eventTable);
					}
				} catch (Exception e) {
					log.error("reload schemaInfo exception", e);
				}
			}
			eventData.setRowData(rowData);
			SynchronizedEvent syncevent = new SynchronizedEvent();
			syncevent.setTimestamp(event.getHeader().getTimestamp());
			syncevent.setEventData(eventData);
			syncevent.setEventType(eventType);

			listener.onInsert(syncevent);
		}
	}

	/**
	 * 处理更新数据
	 * 
	 * @param data
	 * @param tablemap
	 */
	private void processUpdateEvent(Event event, TableMap tablemap) {
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
				log.error("No schemaInfo data in cache,try to reload……");
				try {
					schemaInfo = server.initSchema();
					if (schemaInfo.get(eventTable) != null) {
						rowBefore = getUpdateBeforeRowData(rowdata, eventTable, i);
						rowData = getUpdateRowData(rowdata, eventTable, i);
					} else {
						log.error("No schemaInfo data after reload : " + eventTable);
					}
				} catch (Exception e) {
					log.error("reload schemaInfo exception", e);
				}
			}

			eventData.setRowData(rowData);
			eventData.setBeforeRowData(rowBefore);
			SynchronizedEvent syncevent = new SynchronizedEvent();
			syncevent.setTimestamp(event.getHeader().getTimestamp());
			syncevent.setEventData(eventData);
			syncevent.setEventType(eventType);

			listener.onUpdate(syncevent);
		}
	}

	/**
	 * get update after row data
	 * 
	 * @param rowData
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
	 * @param rowData
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
	 * @param rowBefore
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
