package net.sf.dframe.greed.pojo;
/**
 * 服务配置信息
 * @author dy02
 *
 */
public class GreedConfig {
	
	//mysql host
	private String host;
	//mysql port 
	private int port ;
	//mysql user
	private String user;
	//mysql password
	private String password;
	//driver name 
	private String drivername;
	//schema
	private String schema;
	//log position info
	private LogPosition logposition;
	// connect time out
	private long conntimeout;
	// when log position error retry ,default true
	private boolean retrylogerr = true;
	// reconnect when connect cut , default true
	private boolean autoreconn = true;
	// auto reconnect timer
	private long reconntimer = 5000;
	
	private long serverid = 0L;
	
	
	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getDrivername() {
		return drivername;
	}

	public void setDrivername(String drivername) {
		this.drivername = drivername;
	}

	public String getSchema() {
		return schema;
	}

	public void setSchema(String schema) {
		this.schema = schema;
	}

	public LogPosition getLogposition() {
		return logposition;
	}

	public void setLogposition(LogPosition logposition) {
		this.logposition = logposition;
	}

	public long getConntimeout() {
		return conntimeout;
	}

	public void setConntimeout(long conntimeout) {
		this.conntimeout = conntimeout;
	}

	public boolean isRetrylogerr() {
		return retrylogerr;
	}

	public void setRetrylogerr(boolean retrylogerr) {
		this.retrylogerr = retrylogerr;
	}

	public boolean isAutoreconn() {
		return autoreconn;
	}

	public void setAutoreconn(boolean autoreconn) {
		this.autoreconn = autoreconn;
	}
	
	public long getReconntimer() {
		return reconntimer;
	}
	public void setReconntimer(long reconntimer) {
		this.reconntimer = reconntimer;
	}

	public long getServerid() {
		return serverid;
	}

	public void setServerid(long serverid) {
		this.serverid = serverid;
	}
	
}
