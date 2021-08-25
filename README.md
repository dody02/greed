# greed
simple way to synchronized mysql data
简单易用的mysql数据库监听服务，支持主从集群，可以单机使用，也可以快速构建集群进行数据同步。 

# 启动方式：
public static void main(String[] arg) throws Exception {

        String user ="asdf";
        String password = "asdf";
        String url = "jdbc:mysql://localhost:3306/zib_wl?useSSL=false&serverTimezone=UTC";
        // 建立一个最简化的配置对象，给定要同步的数据库连接信息即可。
        //SimpleSyncMysqlConfig ssmc = new SimpleSyncMysqlConfig( url,user,password);
        // 建立一个简单的配置对象，指定中间网络出现断开后，自动重连接时间间隔为10秒； 上面一句的构造，默认也会自动重连接，但时间间隔为60秒。
        SimpleSyncMysqlConfig ssmc = new SimpleSyncMysqlConfig( url,user,password,10000,true);
       //指定同步的表名，支持多个，支持*号，支持前缀
       SyncTableName stn = new SyncTableName("wl_*","table_*","memeber_relation");
       //构建一个同步服务，指定监听器。
       SimpleSyncMysqlDataService ssds = new SimpleSyncMysqlDataService(ssmc,new Listener(stn));
       ssds.start();
    }
}

# listener 由具体应用实现数据操作
 class Listener extends SimpleDataListener {
        //默认监听器构造器，将不做过滤
        public Listener(){}
       // 指定同步表名列表的监听器。
        public Listener(SyncTableName syncTableName) {
            super(syncTableName);
        }
        @Override
        public void onDelete(SynchronizedEvent event) {
            System.out.println("**********delete :"+event.getEventData().getTable());
            System.out.println("**********事件类型："+event.getEventType());
            System.out.println("**********删除的数据："+event.getEventData().getRowData());
        }

        @Override
        public void onInsert(SynchronizedEvent event) {
            System.out.println("**********insert :"+event.getEventData().getTable());
            System.out.println("**********事件类型："+event.getEventType());
            System.out.println("**********插入的数据："+event.getEventData().getRowData());
        }

        @Override
        public void onUpdate(SynchronizedEvent event) {
            System.out.println("**********update :"+event.getEventData().getTable());
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
