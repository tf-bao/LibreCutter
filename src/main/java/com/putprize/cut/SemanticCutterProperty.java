package com.putprize.cut;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Logger;

public class SemanticCutterProperty {
	
	private static final Logger log = Logger.getLogger(SemanticCutterProperty.class);
	
	private static Properties prop;
	
	static {
		prop = new Properties();
		
		try {
			File f = new File("SemanticCutter.properties");
			if (f.exists()){
				log.info("Init From Local.");
				prop.load(new FileInputStream(f));
			} else {
				// 需要保证这个文件必需存在
				log.info("Init From Jar.");
				prop.load(SemanticCutterProperty.class.getResourceAsStream("/SemanticCutter.properties"));
			}
		} catch (IOException e){
			prop = null;
			log.error("", e);
			System.exit(1);
		} 	
	}
	
	public static Properties getProperties(){
		return prop;
	}
	
	public static String get(String key){
		return prop.getProperty(key);
	}

}

