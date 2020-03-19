package net.sf.dframe.greed.pojo;
/**
 * 服务配置信息
 * @author dy02
 *
 */
public class GreedConfig {
	
	private String host;
	
	private int port ;
	
	private String user;
	
	private String password;
	
	private String drivername;
	
	private String schema;
	
	private LogPosition logposition;

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
	
	
}
