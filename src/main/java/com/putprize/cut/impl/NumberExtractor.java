package com.putprize.cut.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class NumberExtractor implements SentenceCutter,Serializable {
	
	private static final long serialVersionUID = -4489780187769010822L;
	public static final Pattern datePattern = Pattern.compile("\\d+[年|月|日|天|次|个|张|万|元|朵]");

	//@Override
	public List<String> cut(String line) {
		
		List<String> vs = new ArrayList<String>();
		if (line != null){
			int i = 0;
			Matcher m = datePattern.matcher(line);
			boolean one = false;
			while (m.find()){
				one = true;
				int x = m.start();
				int y = m.end();
				String v = m.group();
				while (i < x){
					vs.add(line.substring(i,i+1));
					i += 1;
				}
				vs.add(v);
				i = y;
			}
			if (i < line.length() || !one){
				for (;i < line.length();++i)
					vs.add(line.substring(i,i+1));
			}
		}
		return vs;
	}
	
	//@Override
	public Map<String,Integer> extract(String line){
		
		Map<String,Integer> map = new TreeMap<String,Integer>();
		
		if (line != null){
			Matcher m = datePattern.matcher(line);
			while (m.find()){
				String s = m.group();
				Integer o = map.get(s);
				if (o == null) o = 0;
				map.put(s, o+1);
			}
		}
		
		return map;
		
	}
	
		
}