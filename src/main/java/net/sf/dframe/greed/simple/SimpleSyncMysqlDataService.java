package net.sf.dframe.greed.simple;

import com.alibaba.fastjson.JSONObject;
import com.github.shyiko.mysql.binlog.BinaryLogClient;
import com.github.shyiko.mysql.binlog.event.*;
import com.github.shyiko.mysql.binlog.event.deserialization.*;
import com.github.shyiko.mysql.binlog.io.ByteArrayInputStream;
import net.sf.dframe.cluster.simple.SimpleMasterSlaveCluster;
import net.sf.dframe.greed.pojo.LogPosition;
import net.sf.dframe.greed.service.ISyncService;
import net.sf.dframe.greed.service.SynchronizedListenerAdapter;
import net.sf.dframe.greed.utils.ReadUrl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

/**
 * 简化mysql数据监听同步服务，默认支持主从集群
 * @author dody
 */
public class SimpleSyncMysqlDataService implements ISyncService {

    private static Logger log = LoggerFactory.getLogger(SimpleSyncMysqlDataService.class);
    private static final String POSITION = "POSITION";

    private String serviceId ="_" ;
    /**
     * 监听客户端
     */
    private BinaryLogClient client = null;
    /**
     * 主从集群
     */
    private SimpleMasterSlaveCluster cluster = null;
    /**
     * 同步事件监听器
     */
    private SimpleDataListener sla = null;
    /**
     * 配置信息
     */
    private SimpleSyncMysqlConfig config= null;
    /**
     * 数据解析器
     */
    private SimpleEventDataParsing parsing = null;
    /**
     * 客气端连接监听
     */
    private SimpleConnectionEventListener connlistener;

    /**
     * 数据库名
     */
    private String schema;
    /**
     * 主机
     */
    private String host;
    /**
     * 端口
     */
    private int port;

    /**
     * 构建监听同步服务
     * @param config 配置信息
     * @param listener 事件监听器
     * @throws Exception
     */
    public SimpleSyncMysqlDataService(SimpleSyncMysqlConfig config,SimpleDataListener listener) throws Exception {
        this(config,listener,new SimpleMasterSlaveCluster());
    }

    /**
     * 构建监听同步服务
     * @param config 配置信息
     * @param listener 事件监听器
     * @throws Exception
     */
    public SimpleSyncMysqlDataService(String serviceId ,SimpleSyncMysqlConfig config,SimpleDataListener listener) throws Exception {
        this(serviceId,config,listener,new SimpleMasterSlaveCluster());
    }


    /**
     * 构建监听同步服务
     * @param config 配置信息
     * @param listener 事件监听
     * @param cluster 主从集群
     * @throws Exception
     */
    public SimpleSyncMysqlDataService(String serviceId,SimpleSyncMysqlConfig config,SimpleDataListener listener,SimpleMasterSlaveCluster cluster) throws Exception {
        this.serviceId = "_"+serviceId;
        this.cluster = cluster;
        this.config = config;
        this.sla=listener;
        this.connlistener = new SimpleConnectionEventListener(this);
        JSONObject mysqlInfo = ReadUrl.readMysqlUrl(config.getUrl());
        this.host = mysqlInfo.getString(ReadUrl.HOST);
        this.port = mysqlInfo.getInteger(ReadUrl.PORT);
        this.schema = mysqlInfo.getString(ReadUrl.SCHEMA);
    }


    /**
     * 构建监听同步服务
     * @param config 配置信息
     * @param listener 事件监听
     * @param cluster 主从集群
     * @throws Exception
     */
    public SimpleSyncMysqlDataService(SimpleSyncMysqlConfig config,SimpleDataListener listener,SimpleMasterSlaveCluster cluster) throws Exception {
        this.cluster = cluster;
        this.config = config;
        this.sla=listener;
        this.connlistener = new SimpleConnectionEventListener(this);
        JSONObject mysqlInfo = ReadUrl.readMysqlUrl(config.getUrl());
        this.host = mysqlInfo.getString(ReadUrl.HOST);
        this.port = mysqlInfo.getInteger(ReadUrl.PORT);
        this.schema = mysqlInfo.getString(ReadUrl.SCHEMA);
    }

    /**
     * reset client position
     */
    public void resetPosition () {
        if (config != null) {
            config.setLogposition(new LogPosition());
        }
        if (client != null) {
            client.setBinlogFilename(null);
            client.setBinlogPosition(0);
        }
    }


        /**
         * get Schema info
         * @throws Exception
         */
    public  Map<String, Map<Integer,String>> initSchema() throws Exception {
        Class.forName(config.getDrivername());
//		Class.forName("com.mysql.jdbc.Driver ");
        Connection conn = null;
        Statement statement = null;
        ResultSet rs = null;
        Map<String,Map<Integer,String>> schemainfo = new HashMap<String,Map<Integer,String>>();
        try {
//            conn = DriverManager.getConnection("jdbc:mysql://"+config.getHost()+":"+config.getPort()+"/"+config.getSchema()+"?useSSL=false&serverTimezone=UTC",config.getUser(),config.getPassword());
//			conn = DriverManager.getConnection("jdbc:mysql://localhost:3307/testbinlog?useSSL=false&serverTimezone=UTC",this.username,this.password);
            conn = DriverManager.getConnection(config.getUrl(),config.getUser(),config.getPassword());

            statement = conn.createStatement();
            String sql = "select TABLE_SCHEMA, TABLE_NAME, COLUMN_NAME, ORDINAL_POSITION from INFORMATION_SCHEMA.COLUMNS  WHERE table_schema = '"+this.schema+"';";
            rs = statement.executeQuery(sql);
            while (rs.next()) {
                Map<Integer,String> columnsInfo = schemainfo.get(rs.getString("TABLE_NAME")) ;
                if (columnsInfo == null) {
                    columnsInfo = new HashMap<Integer,String>();
                }
                columnsInfo.put(rs.getInt("ORDINAL_POSITION"),rs.getString("COLUMN_NAME"));
                schemainfo.put(rs.getString("TABLE_NAME"), columnsInfo);//set table
            }

        }catch (Exception e ) {
            log.error("initSchema Exception",e);
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                    rs = null;
                } catch (Exception e) {
                    rs =null;
                }
            }
            if (statement != null) {
                try {
                    statement.close();
                    statement = null;
                } catch (Exception e) {
                    statement =null;
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                    conn = null;
                } catch (Exception e) {
                    conn =null;
                }
            }
        }
        return schemainfo;

    }

    @Override
    public void start() throws Exception {
        // create a clinet
        /**
         * 建立一个客户端
         */

        client = new BinaryLogClient(this.host, this.port,this.schema, config.getUser(),
                config.getPassword());
        // client.setBlocking(config.isBlock());
        // set id
        if (config.getServerid() != 0L) {
            client.setServerId(config.getServerid());
        }
        /**
         * 是否加载历史位置
         */
        if (!config.isGetHisPosition() ) { // 不需要历史数据

            this.setPosition(config.getLogposition());
            log.info("set position in config file");
        } else {
            LogPosition firstLp = getFirstLogPosition(config);
            this.setPosition(firstLp);
            log.info("set position in cluster cache");
        }
        //init listener
        /**
         * 设置监听器
         */
        parsing = new SimpleEventDataParsing( this.schema,initSchema(),sla);


        /**
         * 初始化Client
         */
        EventDeserializer eventDeserializer = new EventDeserializer();
        eventDeserializer.setCompatibilityMode(EventDeserializer.CompatibilityMode.DATE_AND_TIME_AS_LONG);


        client.setEventDeserializer(eventDeserializer);

        client.registerEventListener(new BinaryLogClient.EventListener() {
            @Override
            public void onEvent(Event event) {
                log.debug(" receiver event: " + event.getHeader() + "," + event.getData());
                if (cluster.isMeActive()) {
                    log.debug("current node is master node, do process");
                    //to do event data process
                    LogPosition lp = getPosition();
                    try {
                        //record the position
                        LogPosition newLp = updateLogPosition();
                        parsing.parsingEvent(event,newLp,serviceId);
                    } catch (Exception e) {
                        log.error("Parsing Event data Exception",e);
                        try {
                            setPosition(lp);
                        } catch (IOException ex) {
                            log.error("Rest position when Parsing Event data Exception",ex);
                        }
                    }
                } else {
                    log.info("current node is not master node, do nothing! master node is :"+cluster.getActive());
                }
            }
        });
        /**
         * 监听生命周期
         */
        client.registerLifecycleListener(connlistener);
        /**
         * 是否自动重连
         */
        client.setKeepAlive(config.isAutoreconn());
        /**
         * 自动重连时间间隔
         */
        client.setKeepAliveInterval(config.getMinsecInterval());
        /**
         * 连接至数据库，启动服务
         */
        client.connect();
    }

    /**
     * 获取初始的日志位置
     * @param config
     * @return LogPosition
     */
    private LogPosition getFirstLogPosition(SimpleSyncMysqlConfig config) {
        LogPosition lp = new LogPosition();
        try{
            Connection conn = DriverManager.getConnection(config.getUrl(), config.getUser(), config.getPassword());
            Statement stmt = conn.createStatement();
            ResultSet rs =  stmt.executeQuery("SHOW BINARY LOGS");
            if (rs.next()) {
                String filename = rs.getString("Log_name");
                long fileSize = rs.getLong("File_size");
                // 第一个binlog文件的起始位置通常是4（跳过文件头）
                lp.setLogfile(filename);
                lp.setPosition(4L); //固定值
            }
        }catch (Exception ex){
            log.error("获取日志位置失败：",ex);
        }
        return lp;
    }

    @Override
    public void stop() {
        try {

            client.unregisterLifecycleListener(connlistener);
            if (client.isConnected())
                client.disconnect();
            if (config.isAutoreconn()) {  //自动连接时，需要关线程
                Thread ct = findThread(client.getConnectionId());
                if (ct != null) {
                    try {
                        ct.interrupt();
                    }catch (Exception e ) {
                        log.error("stop reconnetiong Exception");
                    }
                }else {
                    log.info("client Thread is null!");
                }
            }


        } catch (IOException e) {
            log.error("stop server exception",e);
        } finally {
            client = null;
        }
    }

    @Override
    public boolean isStarted() {
        return false;
    }


    /**
     * check the binlog position
     */
    private void checkLogPosition() {
        // 处理未处理数据
        log.debug("check log postion");
        try {
            LogPosition lp = getPosition();
            if (lp != null) {
                client.setBinlogFilename(lp.getLogfile());
                client.setBinlogPosition(lp.getPosition());
                log.debug("set filename to :" + lp.getLogfile() + " and position to :" + lp.getPosition());
            }
        } catch (Exception e) {
            log.error("get log position exception", e);
        }
    }

    /**
     * get log position
     * @return
     */
    public LogPosition getPosition() {
        LogPosition lp = null;
        String strLp = cluster.getArributesMap().get(POSITION+serviceId);
        if (strLp != null && (!strLp.isEmpty())) {
            lp = JSONObject.parseObject(strLp, LogPosition.class);
        }
        return lp;
    }

    /**
     * 设置位置
     * @param position
     * @throws IOException
     */
    public void setPosition(LogPosition position) throws IOException {

        if ( client != null) {
            if (position == null) {
                client.setBinlogFilename(null);
                client.setBinlogPosition(0);
                cluster.getArributesMap().remove(POSITION+serviceId);
            }else {
                client.setBinlogFilename(position.getLogfile());
                client.setBinlogPosition(position.getPosition());
                cluster.getArributesMap().put(POSITION, JSONObject.toJSONString(position));

            }
            if (client.isConnected()) {
                client.disconnect();
                if (!config.isAutoreconn()) {
                    client.connect();
                }

            }
        } else {
            cluster.getArributesMap().put(POSITION+serviceId, JSONObject.toJSONString(new LogPosition()));
        }

    }

    /**
     * record the current position
     */
    public LogPosition updateLogPosition() {
        LogPosition  lp  = new LogPosition();
        lp.setLogfile(client.getBinlogFilename());
        lp.setPosition(client.getBinlogPosition());
        cluster.getArributesMap().put(POSITION+serviceId, JSONObject.toJSONString(lp));
        return lp;
    }


    /**
     * Find thread
     * @param threadId
     * @return
     */
    private Thread findThread(long threadId) {
        ThreadGroup group = Thread.currentThread().getThreadGroup();
        while(group != null) {
            Thread[] threads = new Thread[(int)(group.activeCount() * 1.2)];
            int count = group.enumerate(threads, true);
            for(int i = 0; i < count; i++) {
                if(threadId == threads[i].getId()) {
                    return threads[i];
                }
            }
            group = group.getParent();
        }
        return null;
    }

    /**
     * 获取ServiceID
     * @return serviceId
     */
    public String getServiceId() {
        return serviceId;
    }

    /**
     * setServiceID
     * @param serviceId
     */
    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }


}

