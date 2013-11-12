package com.putprize.cut.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class TokenExtractor implements SentenceCutter,Serializable {
	
	private static final long serialVersionUID = -5100741225667745504L;
	public TokenExtractor() {}

	private static final String CT_SINGLE = "._-";
	private static boolean isContinual(char c){
		
		int t = Character.getType(c);
		if (t == Character.UPPERCASE_LETTER ||
				t == Character.LOWERCASE_LETTER ||
				t == Character.TITLECASE_LETTER ||
				t == Character.MODIFIER_LETTER ||
				t == Character.DECIMAL_DIGIT_NUMBER ||
				CT_SINGLE.indexOf(c) >= 0)
			return true;
		
		return false;
		
	}
	
	
	//@Override
	public List<String> cut(String line){
		
		List<String> vs = new ArrayList<String>();
		
		if (line != null && line.length() > 0){
			
			StringBuffer sAtom = new StringBuffer();
			
			boolean last = true;
			
			for (int i = 0; i < line.length(); ++i){
				
				char c = line.charAt(i);
				boolean current = isContinual(c);
				
				if ( ((last != current) || !current) && sAtom.length() > 0){
					vs.add(sAtom.toString());
					sAtom.setLength(0);
				} 
				last = current;
				sAtom.append(c);
				
			}
			
			if (sAtom.length() > 0)
				vs.add(sAtom.toString());
			
		}
		
		return vs;
		
	}
	
	
	//@Override
	public Map<String,Integer> extract(String line){
		
		Map<String,Integer> map = new TreeMap<String,Integer>();
		
		if (line != null && line.length() > 0){
			
			StringBuffer sAtom = new StringBuffer();
			
			boolean last = true;
			
			for (int i = 0; i < line.length(); ++i){
				
				char c = line.charAt(i);
				boolean current = isContinual(c);
				
				if ( ((last != current) || !current) && sAtom.length() > 0){
					//vs.add(sAtom.toString());
					String s = sAtom.toString();
					if (s.length() > 1){
						Integer o = map.get(s);
						if (o == null) o = 0;
						map.put(s, o+1);
					}
					sAtom.setLength(0);
				} 
				last = current;
				sAtom.append(c);
				
			}
			
			if (sAtom.length() > 1){
				String s = sAtom.toString();
				Integer o = map.get(s);
				if (o == null) o = 0;
				map.put(s, o+1);
			}
				
		}
		
		return map;
		
	}
	
}
