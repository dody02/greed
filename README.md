# greed
simple way to synchronized mysql data
简单的mysql数据库监听服务，支持集群
package net.sf.dframe.greed;

import net.sf.dframe.cluster.hazelcast.HazelcastClusterCreater;
import net.sf.dframe.cluster.hazelcast.HazelcastMasterSlaveCluster;
import net.sf.dframe.greed.config.LoadJsonConfig;
import net.sf.dframe.greed.pojo.SynchronizedEvent;
import net.sf.dframe.greed.service.SynchronizedListenerAdapter;
import net.sf.dframe.greed.service.impl.ConnectorSyncServer;

public class Exmaple {
public static void main(String[] args) throws Exception {
		
		//create a server instance by json config file or can set GreedConfig after new ConnectorSyncServer();
		ConnectorSyncServer cs = new ConnectorSyncServer(LoadJsonConfig.readConfig("greed.json"));
		//use cluster  @see another project 
		HazelcastClusterCreater cc = new HazelcastClusterCreater();
		HazelcastMasterSlaveCluster c = (HazelcastMasterSlaveCluster) cc.getCluster("cluster.json");
		cs.setCluster(c);
		
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
