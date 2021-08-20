package net.sf.dframe.greed.simple;

import net.sf.dframe.greed.pojo.LogPosition;

public class SimpleSyncMysqlConfig {
    /**
     * mysql jdbc driver name
     */
    private String drivername;
//    private String host;
//    private int port;
    private String url;
    private LogPosition logPosition;
//    private String schema;
    private String user;
    private String password;
    private long serverid;
    private boolean autoreconn = true;
    private boolean getHisPosition = false;

    public SimpleSyncMysqlConfig(long serverid,String drivername,String url, String user,String password,boolean autoreconn,boolean getHisPosition){
        this.serverid = serverid;
        this.drivername = drivername;
//        this.host = host;
//        this.port = port;
//        this.schema = schema;
        this.user = user;
        this.password = password;
        this.autoreconn = autoreconn;
        this.getHisPosition = getHisPosition;
        this.url = url;
    }

    public SimpleSyncMysqlConfig(long serverid,String drivername,String url ,String user,String password,boolean autoreconn){
        this.serverid = serverid;
        this.drivername = drivername;
//        this.host = host;
//        this.port = port;
        this.url =url;
//        this.schema = schema;
        this.user = user;
        this.password = password;
        this.autoreconn = autoreconn;
    }

    public SimpleSyncMysqlConfig(long serverid,String drivername,String url ,String user,String password){
        this.serverid = serverid;
        this.drivername = drivername;
//        this.host = host;
//        this.port = port;
        this.url = url;
//        this.schema = schema;
        this.user = user;
        this.password = password;
    }

    public SimpleSyncMysqlConfig(String drivername,String url,String user,String password){
        this.serverid = 1L;
        this.drivername = drivername;
//        this.host = host;
//        this.port = port;
//        this.schema = schema;
        this.user = user;
        this.password = password;
        this.url = url;
    }

    public SimpleSyncMysqlConfig(String url,String user,String password){
        this.serverid = 1L;
        this.drivername = "com.mysql.jdbc.Driver";
//        this.drivername = drivername;
//        this.host = host;
//        this.port = port;
//        this.schema = schema;
        this.user = user;
        this.password = password;
        this.url = url;
    }



    public void setDrivername(){
        this.drivername = drivername;
    }
    public String getDrivername() {
        return drivername;
    }

//    public String getHost() {
//        return host;
//    }
//
//    public int getPort() {
//        return port;
//    }


//    public void setHost(String host) {
//        this.host = host;
//    }
//
//    public void setPort(int port) {
//        this.port = port;
//    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public LogPosition getLogPosition() {
        return logPosition;
    }

    public void setLogPosition(LogPosition logPosition) {
        this.logPosition = logPosition;
    }

//    public void setSchema(String schema) {
//        this.schema = schema;
//    }

    public void setUser(String user) {
        this.user = user;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setServerid(long serverid) {
        this.serverid = serverid;
    }

    public void setAutoreconn(boolean autoreconn) {
        this.autoreconn = autoreconn;
    }


    public void setDrivername(String drivername) {
        this.drivername = drivername;
    }

    /**
     * 数据库记录位置
     * @param logPosition
     */
    public void setLogposition(LogPosition logPosition) {
        this.logPosition = logPosition;
    }

//    /**
//     * 数据库名称
//     * @return
//     */
//    public String getSchema() {
//        return schema;
//    }

    /**
     * 数据库用户
     * @return
     */
    public String getUser() {
        return user;
    }

    public boolean isGetHisPosition() {
        return getHisPosition;
    }

    public void setGetHisPosition(boolean getHisPosition) {
        this.getHisPosition = getHisPosition;
    }

    /**
     * 数据库密码
     * @return
     */
    public String getPassword() {
        return password;
    }

    /**
     * 客户端监听ID
     * @return
     */
    public long getServerid() {
        return serverid;
    }

    /**
     * 是否自动连接
     * @return
     */
    public boolean isAutoreconn() {
        return autoreconn;
    }


    public LogPosition getLogposition() {
        return this.logPosition;
    }
}
