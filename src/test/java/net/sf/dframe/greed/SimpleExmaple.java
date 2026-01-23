package net.sf.dframe.greed;

import net.sf.dframe.greed.pojo.SynchronizedEvent;
import net.sf.dframe.greed.service.SynchronizedListenerAdapter;
import net.sf.dframe.greed.simple.SimpleDataListener;
import net.sf.dframe.greed.simple.SimpleSyncMysqlConfig;
import net.sf.dframe.greed.simple.SimpleSyncMysqlDataService;
import net.sf.dframe.greed.simple.SyncTableName;

import java.util.List;

public class SimpleExmaple {

    static class Listener extends SimpleDataListener {


        public Listener() {

        }

        public Listener(SyncTableName syncTableName) {
            super(syncTableName);
        }

        @Override
        public void onDelete(SynchronizedEvent event) {
            System.out.println("有进入删除*************************************");
            if (this.getSyncTableName().needSync(event.getEventData().getTable())){
                System.out.println("**********delete :"+event.getEventData().getTable());
                System.out.println("**********事件类型："+event.getEventType());
                System.out.println("**********删除前数据："+event.getEventData().getBeforeRowData());
                System.out.println("**********删除后数据："+event.getEventData().getRowData());

            } else{
                System.out.println("这个表不需要同步");
            }
        }

        @Override
        public void onInsert(SynchronizedEvent event) {
            System.out.println("**********insert :"+event.getEventData().getTable());
            System.out.println("**********事件类型："+event.getEventType());
            System.out.println("**********插入前数据："+event.getEventData().getBeforeRowData());
            System.out.println("**********插入后数据："+event.getEventData().getRowData());
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


    public static void main(String[] arg) throws Exception {

        String user ="root";
        String password = "asdf";
        String url = "jdbc:mysql://localhost:3306/etl?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
//        SimpleSyncMysqlConfig ssmc = new SimpleSyncMysqlConfig( url,user,password);
        SimpleSyncMysqlConfig ssmc = new SimpleSyncMysqlConfig( url,user,password,10000,true);
//        SyncTableName stn = new SyncTableName("*");
//        SyncTableName stn = new SyncTableName("wl_*");
        SyncTableName stn = new SyncTableName("*");
        System.out.println("过滤的表名：");
        List<String> tables = stn.getTableList();
        for (String t:tables){
            System.out.println(t);
        }
        SimpleSyncMysqlDataService ssds = new SimpleSyncMysqlDataService(ssmc,new Listener(stn));

        ssds.start(); //这个方法是阻塞的。

        System.out.println("&&&&&&&&&&是否是阻塞？？？");
    }
}
