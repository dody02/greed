package net.sf.dframe.greed.simple;

import net.sf.dframe.greed.pojo.LogPosition;

/**
 * 构建监听基础配置信息
 * @author dody
 */
public class SimpleSyncMysqlConfig {
    /**
     * mysql jdbc driver name
     */
    private String drivername = "com.mysql.jdbc.Driver";
    /**
     * jdbc url
     */
    private String url;
    /**
     * 日志位置信息
     */
    private LogPosition logPosition;
    /**
     * 数据库用户
     */
    private String user;
    /**
     * 数据库密码
     */
    private String password;
    /**
     * 监听客户端ID
     */
    private long serverid = 1L;
    /**
     * 自动重连
     */
    private boolean autoreconn = true;
    /**
     * 获取历史记录
     */
    private boolean getHisPosition = false;
    /**
     * 自动重新连接时间间隔，单位毫秒
     */
    private long minsecInterval = 60000;

    /**
     * 构建同步服务配置信息
     * @param serverid 客户端id
     * @param drivername 数据库JDBC类名
     * @param url JDBC连接URL
     * @param user 数据库用户
     * @param password 数据库密码
     * @param autoreconn 自动重新连接
     * @param minsecInterval 自动重新连接时间间隔
     * @param getHisPosition 是否获取历史数据位置,获取历史数据则无视数据位置
     * @param position 历史数据位置
     */
    public SimpleSyncMysqlConfig(long serverid,String drivername,String url, String user,String password,boolean autoreconn,long minsecInterval,boolean getHisPosition,LogPosition position){
        this.serverid = serverid;
        this.drivername = drivername;
        this.url = url;
        this.user = user;
        this.password = password;
        this.autoreconn = autoreconn;
        this.minsecInterval = minsecInterval;
        this.getHisPosition = getHisPosition;
        this.logPosition = position;
    }

    /**
     * 构建同步服务配置信息
     * @param serverid 客户端ID
     * @param drivername 数据库驱动类名
     * @param url JDBC连接URL
     * @param user 数据库用户名
     * @param password 数据库密码
     * @param autoreconn 是否自动重连接
     * @param minsecInterval 自动重连接时间间隔
     */
    public SimpleSyncMysqlConfig(long serverid,String drivername,String url, String user,String password,boolean autoreconn,long minsecInterval) {
        this(serverid,drivername,url,user,password,autoreconn,minsecInterval,false,new LogPosition());
    }

    /**
     * 构建同步服务配置信息
     * @param serverid 客户端ID
     * @param drivername 数据库连接类
     * @param url JDBC连接URL
     * @param user 数据库用户名
     * @param password 数据库密码
     */
    public SimpleSyncMysqlConfig(long serverid,String drivername,String url, String user,String password) {
        this(serverid,drivername,url,user,password,true,60000,false,new LogPosition());
    }

    /**
     * 构建同步服务配置信息
     * @param serverid 客户端id
     * @param url JDBC连接URL
     * @param user 数据库用户名
     * @param password 数据库密码
     */
    public SimpleSyncMysqlConfig(long serverid,String url, String user,String password) {
        this(serverid,"com.mysql.jdbc.Driver",url,user,password,true,60000,false,new LogPosition());
    }

    /**
     * 构建同步服务配置信息
     * @param url 数据库JDBC连接URL
     * @param user 数据库用户
     * @param password 数据库密码
     */
    public SimpleSyncMysqlConfig(String url, String user,String password) {
        this(1L,"com.mysql.jdbc.Driver",url,user,password,true,60000,false,new LogPosition());
    }

    /**
     * 构建同步服务配置信息
     * @param serverid 客户端ID
     * @param url 数据库JDBC连接URL
     * @param user 数据库用户
     * @param password 数据库密码
     * @param minsecInterval 自动重连接时间间隔
     */
    public SimpleSyncMysqlConfig(long serverid,String url, String user,String password,long minsecInterval) {
        this(serverid,"com.mysql.jdbc.Driver",url,user,password,true,minsecInterval,false,new LogPosition());
    }

    /**
     * 构建同步服务配置信息
     * @param url 数据库JDBC连接URL
     * @param user 数据库用户
     * @param password 数据库密码
     * @param minsecInterval 自动重连接时间间隔
     */
    public SimpleSyncMysqlConfig(String url, String user,String password,long minsecInterval) {
        this(1L,"com.mysql.jdbc.Driver",url,user,password,true,minsecInterval,false,new LogPosition());
    }

    /**
     * 构建同步服务配置信息
     * @param url 数据库JDBC连接URL
     * @param user 数据库用户
     * @param password 数据库密码
     * @param minsecInterval 自动重连间隔
     * @param getHisPosition 是否获取历史数据
     */
    public SimpleSyncMysqlConfig(String url, String user,String password,long minsecInterval,boolean getHisPosition) {
        this(1L,"com.mysql.jdbc.Driver",url,user,password,true,minsecInterval,getHisPosition,new LogPosition());
    }

    /**
     * 构建简单同步服务配置
     * @param  serverid 客户端ID
     * @param url 数据库JDBC连接URL
     * @param user 数据库用户
     * @param password 自动重连间隔
     * @param minsecInterval 自动重连间隔
     * @param position 指定数据位置
     */
    public SimpleSyncMysqlConfig(long serverid,String url, String user,String password,long minsecInterval,LogPosition position) {
        this(1L,"com.mysql.jdbc.Driver",url,user,password,true,minsecInterval,false,position);
    }

    /**
     * 构建简单同步服务配置
     * @param url 数据库JDBC连接URL
     * @param user 数据库用户
     * @param password 自动重连间隔
     * @param minsecInterval 自动重连间隔
     * @param position 指定数据位置
     */
    public SimpleSyncMysqlConfig(String url, String user,String password,long minsecInterval,LogPosition position) {
        this(1L,"com.mysql.jdbc.Driver",url,user,password,true,minsecInterval,false,position);
    }

    /**
     * 构建简单同步服务配置
     * @param url 数据库JDBC连接URL
     * @param user 数据库用户
     * @param password 自动重连间隔
     * @param position 指定数据位置
     */
    public SimpleSyncMysqlConfig(String url, String user,String password,LogPosition position) {
        this(1L,"com.mysql.jdbc.Driver",url,user,password,true,60000,false,position);
    }

    public void setDrivername(){
        this.drivername = drivername;
    }
    public String getDrivername() {
        return drivername;
    }


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

    public long getMinsecInterval() {
        return minsecInterval;
    }

    public void setMinsecInterval(long minsecInterval) {
        this.minsecInterval = minsecInterval;
    }

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
