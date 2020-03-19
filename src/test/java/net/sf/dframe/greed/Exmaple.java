package net.sf.dframe.greed;

import net.sf.dframe.cluster.hazelcast.HazelcastClusterCreater;
import net.sf.dframe.cluster.hazelcast.HazelcastMasterSlaveCluster;
import net.sf.dframe.greed.config.LoadJsonConfig;
import net.sf.dframe.greed.pojo.LogPosition;
import net.sf.dframe.greed.pojo.SynchronizedEvent;
import net.sf.dframe.greed.service.SynchronizedListenerAdapter;
import net.sf.dframe.greed.service.impl.ConnectorSyncServer;

public class Exmaple {
public static void main(String[] args) throws Exception {
		
		//create a server instance by json config file or can set GreedConfig after new ConnectorSyncServer();
		ConnectorSyncServer cs = new ConnectorSyncServer(LoadJsonConfig.readConfig("greed2.json"));
		//use cluster  @see another project 
		HazelcastClusterCreater cc = new HazelcastClusterCreater();
		HazelcastMasterSlaveCluster c = (HazelcastMasterSlaveCluster) cc.getCluster("cluster.json");
		cs.setCluster(c);
		
//		new Thread() {
//			public void run() {
//				try {
//					Thread.sleep(30000);
//				} catch (InterruptedException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//				System.err.println("开始设置位置！！！！！！！！！！！！！！！！！！！！！");
//				LogPosition lp = new LogPosition();
//				lp.setLogfile("mysql-bin.000006");
//				lp.setPosition(14167);
//				cs.setPosition(lp);
//			}
//		}.start();
		
		//set an listener
		cs.setListener(new SynchronizedListenerAdapter () {

			@Override
			public void onDelete(SynchronizedEvent event) {
				// TODO Auto-generated method stub
				System.out.println("on deleteEvent :" +event);
			}

			@Override
			public void onInsert(SynchronizedEvent event) {
				// TODO Auto-generated method stub
				System.out.println("on insertEvent :" +event);
			}

			@Override
			public void onUpdate(SynchronizedEvent event) {
				// TODO Auto-generated method stub
				System.out.println("on updateEvent :" +event);
			}
			
		});
		//start 
		try {
			cs.start();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
}
