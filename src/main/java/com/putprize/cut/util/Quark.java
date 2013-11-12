package com.putprize.cut.util;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.putprize.cut.trie.CharSequenceKeyAnalyzer;
import com.putprize.cut.trie.PatriciaTrie;
import com.putprize.cut.trie.Trie;


// 目前是Trie+Array的实现。

public class Quark implements Serializable{
	
	private static final long serialVersionUID = -2898633922760880818L;
	private Trie<String,Integer> trie;
	private List<String> list;
	//private boolean lock;
	
	public Quark(){
		trie = new PatriciaTrie<String,Integer>(CharSequenceKeyAnalyzer.INSTANCE);
		list = new ArrayList<String>();
		//lock = false;
	}
	
	public int count(){	
		return list.size();
	}
	
	public String getKey(Integer i){
		if (i < list.size())
			return list.get(i);
		else
			return null;
	}
	
	public Integer getValue(String s){
		if (trie.containsKey(s))
			return trie.get(s);
		else
			return null;
	}
	
	public void put(String s){
		trie.put(s, list.size());
		list.add(s);
	}
	
}
