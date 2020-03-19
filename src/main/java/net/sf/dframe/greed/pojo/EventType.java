package net.sf.dframe.greed.pojo;
/**
 * 事件类型
 * @author Dody
 *
 */
public enum EventType {
	
	INSERT {
		public String toString( ) {
			return "insert";
		}
	},
	
	UPDATE {
		public String toString( ) {
			return "update";
		}
	},
	
	DELETE {
		public String toString( ) {
			return "delete";
		}
	}
}
