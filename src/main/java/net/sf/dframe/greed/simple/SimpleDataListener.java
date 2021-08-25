package net.sf.dframe.greed.simple;

import net.sf.dframe.greed.pojo.EventType;
import net.sf.dframe.greed.pojo.SynchronizedEvent;
import net.sf.dframe.greed.service.ISynchronizedListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author  dody
 */
public abstract class SimpleDataListener  implements ISynchronizedListener{

    private static Logger log = LoggerFactory.getLogger(SimpleDataListener.class);

    private SyncTableName syncTableName = null;


    public SyncTableName getSyncTableName(){
        return this.syncTableName;
    }

    public SimpleDataListener (SyncTableName syncTableName){
        this.syncTableName = syncTableName;
    }


    @Override
    public void onData(SynchronizedEvent event) {
        log.debug("event:"+event);
        if (syncTableName == null || syncTableName.needSync(event.getEventData().getTable())){
            if ( event.getEventType() == EventType.DELETE) {
                onDelete(event);
            }
            if (event.getEventType() == EventType.INSERT) {
                onInsert(event);
            }
            if (event.getEventType() == EventType.UPDATE) {
                onUpdate(event);
            }
        }else{
            log.info("table : "+event.getEventData().getTable() +" needn't synchronized data");
        }

    }

    /**
     * deleteEvent
     * @param event
     */
    public abstract void onDelete(SynchronizedEvent event);
    /**
     * insertEvent
     * @param event
     */
    public abstract void onInsert(SynchronizedEvent event);
    /**
     * updateEvent
     * @param event
     */
    public abstract void onUpdate(SynchronizedEvent event);

}
