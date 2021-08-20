package net.sf.dframe.greed;

import net.sf.dframe.greed.pojo.SynchronizedEvent;
import net.sf.dframe.greed.service.SynchronizedListenerAdapter;
import net.sf.dframe.greed.simple.SimpleSyncMysqlConfig;
import net.sf.dframe.greed.simple.SimpleSyncMysqlDataService;

public class SimpleExmaple {

    static class Listener extends SynchronizedListenerAdapter {

        @Override
        public void onDelete(SynchronizedEvent event) {
            System.out.println("**********delete :"+event);
            System.out.println("**********事件类型："+event.getEventType());
            System.out.println("**********删除前数据："+event.getEventData().getBeforeRowData());
            System.out.println("**********删除后数据："+event.getEventData().getRowData());
        }

        @Override
        public void onInsert(SynchronizedEvent event) {
            System.out.println("**********insert :"+event);
            System.out.println("**********事件类型："+event.getEventType());
            System.out.println("**********插入前数据："+event.getEventData().getBeforeRowData());
            System.out.println("**********插入后数据："+event.getEventData().getRowData());
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


    public static void main(String[] arg) throws Exception {

//        String host = "localhost";
//        int port = 3306;
//        String schema = "zib_wl";
        String user ="asdf";
        String password = "asdf";
        String url = "jdbc:mysql://localhost:3306/zib_wl?useSSL=false&serverTimezone=UTC";
        SimpleSyncMysqlConfig ssmc = new SimpleSyncMysqlConfig( url,user,password);

        SimpleSyncMysqlDataService ssds = new SimpleSyncMysqlDataService(ssmc,new Listener());
        ssds.start();
    }
}
