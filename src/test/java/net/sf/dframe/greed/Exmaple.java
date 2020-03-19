package net.sf.dframe.greed;

import net.sf.dframe.cluster.hazelcast.HazelcastClusterCreater;
import net.sf.dframe.cluster.hazelcast.HazelcastMasterSlaveCluster;
import net.sf.dframe.greed.config.LoadJsonConfig;
import net.sf.dframe.greed.pojo.SynchronizedEvent;
import net.sf.dframe.greed.service.SynchronizedListenerAdapter;
import net.sf.dframe.greed.service.impl.ConnectorSyncServer;

public class Exmaple {
public static void main(String[] args) throws Exception {
		
		ConnectorSyncServer cs = new ConnectorSyncServer(LoadJsonConfig.readConfig("greed.json"));
		HazelcastClusterCreater cc = new HazelcastClusterCreater();
		HazelcastMasterSlaveCluster c = (HazelcastMasterSlaveCluster) cc.getCluster("cluster.json");
		cs.setCluster(c);
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
		try {
			cs.start();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
}
