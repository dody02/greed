package net.sf.dframe.greed.utils;


import com.alibaba.fastjson.JSONObject;

/**
 * 读取JDBC连接URL信息
 */
public class ReadUrl {

    public static String SCHEMA = "schema";
    public static String HOST ="host";
    public static String PORT = "port";

    /**
     * 将诸如：jdbc:mysql://localhost:3307/testbinlog?useSSL=false&serverTimezone=UTC的连接信息，读取出schema，host，port
     * @param url ,例如jdbc:mysql://localhost:3307/testbinlog?useSSL=false&serverTimezone=UTC
     * @return
     */
    public static JSONObject readMysqlUrl(String url) throws Exception {
        if (url == null || url.isEmpty()){
            throw new Exception("Invalid JDBC url ");
        }
        String div = "://";
        String[] tmpInfo = url.split(div);
        String[] info = tmpInfo[1].split(":");
        String host = info[0];
        String[] portinfo = info[1].split("/");
        String port = portinfo[0];
        String[] schemaInfo = portinfo[1].split("[?]");
        String schema = schemaInfo[0];

        JSONObject mysqlInfo = new JSONObject();
        mysqlInfo.put(SCHEMA,schema);
        mysqlInfo.put(HOST,host);
        mysqlInfo.put(PORT,port);
        return mysqlInfo;
    }
}
