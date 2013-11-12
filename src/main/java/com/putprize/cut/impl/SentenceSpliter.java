package com.putprize.cut.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class SentenceSpliter implements Serializable {

	private static final long serialVersionUID = -6176108485085156554L;
	private SentenceSpliter() {}
	
	public static List<Sentence> split(String text){
		return split(text,false);
	}
	
	public static List<Sentence> split(String text, boolean omitSpace){
		
		if (omitSpace){
			text = text.replace(" ", "");
		}
		
		List<Sentence> res = null;
		
		if (text != null){
			res = new ArrayList<Sentence>();
			
			StringBuffer line = new StringBuffer();
			
			for (int i = 0; i < text.length(); ++i){
				char ch = text.charAt(i);
				
				if ( SEPERATOR_C_SENTENCE.indexOf(ch) >= 0 ||
						SEPERATOR_E_SENTENCE.indexOf(ch) >= 0 ||
						SEPERATOR_C_SUB_SENTENCE.indexOf(ch) >= 0 ||
						SEPERATOR_E_SUB_SENTENCE.indexOf(ch) >= 0 ){
					line.append(ch);
					if (line.length() > 0){
						res.add(new Sentence(line.toString(),true));
					}
					line.setLength(0);
				} else if ( SEPERATOR_LINK.indexOf(ch) >= 0 ){
					if (line.length() > 0){
						res.add(new Sentence(line.toString(),true));
						line.setLength(0);
					} else {
						res.add(new Sentence(text.substring(i,i+1),false));
					}
				} else {
					line.append(ch);
				}
			}
			
			if (line.length() > 0){
				res.add(new Sentence(line.toString(),true));
			}
		}
		
		return res;
		
	}
	
	private static final String SEPERATOR_C_SENTENCE = "。！？：；…"; 
	private static final String SEPERATOR_C_SUB_SENTENCE = "、，（）“”‘’＂＂";
	private static final String SEPERATOR_E_SENTENCE = "!?:;";
	private static final String SEPERATOR_E_SUB_SENTENCE = ",()\"'";
	private static final String SEPERATOR_LINK = "\n\r\t ";

	
}
