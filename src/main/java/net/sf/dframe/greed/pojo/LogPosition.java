package net.sf.dframe.greed.pojo;

/**
 * 日志位置
 * @author dy02
 *
 */
public class LogPosition {
	
	private String logfile = null;
	
	private long position = 0;

	public String getLogfile() {
		return logfile;
	}

	public void setLogfile(String logfile) {
		this.logfile = logfile;
	}

	public long getPosition() {
		return position;
	}

	public void setPosition(long position) {
		this.position = position;
	}
	
	
}
