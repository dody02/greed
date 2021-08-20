# greed
simple way to synchronized mysql data
简单易用的mysql数据库监听服务，支持主从集群，可以单机使用，也可以快速构建集群进行数据同步。 

# 启动方式：
public static void main(String[] arg) throws Exception {

        String user ="user";
        String password = "password";
        String url = "jdbc:mysql://localhost:3306/db?useSSL=false&serverTimezone=UTC";
        SimpleSyncMysqlConfig ssmc = new SimpleSyncMysqlConfig( url,user,password);

        SimpleSyncMysqlDataService ssds = new SimpleSyncMysqlDataService(ssmc,new Listener());
        ssds.start();
    }
}

# listener 由具体应用实现数据操作
 class Listener extends SynchronizedListenerAdapter {

        @Override
        public void onDelete(SynchronizedEvent event) {
            System.out.println("**********delete :"+event);
            System.out.println("**********事件类型："+event.getEventType());
            System.out.println("**********删除的数据："+event.getEventData().getRowData());
        }

        @Override
        public void onInsert(SynchronizedEvent event) {
            System.out.println("**********insert :"+event);
            System.out.println("**********事件类型："+event.getEventType());
            System.out.println("**********插入的数据："+event.getEventData().getRowData());
        }

        @Override
        public void onUpdate(SynchronizedEvent event) {
            System.out.println("**********update :"+event);
            System.out.println("**********事件类型："+event.getEventType());
            System.out.println("**********修改前数据："+event.getEventData().getBeforeRowData());
            System.out.println("**********修改后数据："+event.getEventData().getRowData());

            System.out.println(event.getTimestamp());

        }
    }
    
   # 配置包括以下内容：
    /**
     * 构建同步服务配置信息
     * @param serverid 客户端id ，默认为1,多个监听时需要指定以区别。
     * @param drivername 数据库JDBC类名 ，默认为：com.mysql.jdbc.Driver
     * @param url JDBC连接URL，必填
     * @param user 数据库用户，必填
     * @param password 数据库密码，必填
     * @param autoreconn 自动重新连接，默认会进行自动重连
     * @param minsecInterval 自动重新连接时间间隔，单位毫秒，默认60000毫秒
     * @param getHisPosition 是否获取历史数据位置，默认不获取
     * @param position 历史数据位置，需要获取历史数据时，从指定的位置开始
     */
    public SimpleSyncMysqlConfig(long serverid,String drivername,String url, String user,String password,boolean autoreconn,long minsecInterval,boolean getHisPosition,LogPosition position)
