package com.putprize.cut.impl;

import java.util.List;
import java.util.Map;

public interface SentenceCutter {
	
	public List<String> cut(String sentence);
	
	public Map<String,Integer> extract(String sentence);

}
