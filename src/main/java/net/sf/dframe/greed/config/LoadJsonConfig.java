package net.sf.dframe.greed.config;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;

import net.sf.dframe.greed.pojo.GreedConfig;
import net.sf.dframe.greed.service.impl.ConnectorSyncServer;

/**
 * 读取配置信息
 * @author dy02
 *
 */
public class LoadJsonConfig {
	private static Logger log = LoggerFactory.getLogger(LoadJsonConfig.class);
	
	public static GreedConfig readConfig(String url) throws Exception {
		String cfgContent = FileUtils.readFileToString(new File(url),"utf8");
		log.info("load config:\n"+cfgContent);
		return JSONObject.parseObject(cfgContent,GreedConfig.class);
	}
}
