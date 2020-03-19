package net.sf.dframe.greed.config;

import java.io.File;

import org.apache.commons.io.FileUtils;

import com.alibaba.fastjson.JSONObject;

import net.sf.dframe.greed.pojo.GreedConfig;

/**
 * 读取配置信息
 * @author dy02
 *
 */
public class LoadJsonConfig {
	
	public static GreedConfig readConfig(String url) throws Exception {
		String cfgContent = FileUtils.readFileToString(new File(url),"utf8");
		return JSONObject.parseObject(cfgContent,GreedConfig.class);
	}
}
