package net.sf.dframe.greed.simple;



import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 配置需要同步的表名
 */
public class SyncTableName {

    Set<String> tables = new HashSet<String>();
    Set<String> tablePrex = new HashSet<String>();

    public SyncTableName (String... tablename){
        if (tablename != null ){
            for(String name:tablename){
                if (!name.equals("*") && name.endsWith("*")){//前缀的表名
                    tablePrex.add(name.substring(0,name.indexOf("*")));
                } else {
                    tables.add(name);
                }

            }
        }
    }

    /**
     * 是否需要同步
     * @param tablename
     * @return
     */
    public boolean needSync(String tablename){
        if ( tables.isEmpty() && tablePrex.isEmpty() ){
            return true;
        } else if ( tables.contains("*")) {//默认所有都进行同步
            return true;
        } else if ( tables.contains(tablename)){
            return true;
        } else { //查找过滤表名
            for ( String prex : tablePrex){
                if (tablename.startsWith(prex)){
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 移除掉一个同步的表
     * @param tablename
     */
    public void removeSyncTable (String tablename){
        if (!tablename.equals("*") && tablename.endsWith("*")){
          tablePrex.remove(tablename);
        } else {
            tables.remove(tablename);
        }
    }

    /**
     * 添加一个同步的表
     * @param tablename
     */
    public void addSyncTable(String tablename){
        if (!tablename.equals("*") && tablename.endsWith("*")){
            tablePrex.add(tablename);
        } else {
            tables.add(tablename);
        }

    }

    /**
     * 获取过滤表名单
     * @return
     */
    public List<String> getTableList(){
        List<String> tableList = new ArrayList<String>(tables);
//        tableList.addAll(tablePrex);
        for (String t: tablePrex){
            tableList.add(t+"*");
        }
        return tableList;
    }
}
