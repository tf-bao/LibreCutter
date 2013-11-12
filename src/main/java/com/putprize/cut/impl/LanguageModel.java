package com.putprize.cut.impl;

import java.io.Serializable;
import java.util.ArrayList;
//import java.util.Collections;
//import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import com.putprize.cut.util.Gruon;
import com.putprize.cut.util.Tuple2;

public class LanguageModel implements SentenceCutter,Serializable {
	
	private static final long serialVersionUID = -2999032611645953400L;

	private static final Logger log = Logger.getLogger(LanguageModel.class);
	
	private Gruon<Integer> pTrie;
	
	private long sum;
	
	
	// 通过文件初始化
	public LanguageModel(String locationDict){
		init(locationDict);
	}
	
	private void init(String locationDict){
		sum = 0;
		Map<String,Integer> map = new TreeMap<String,Integer>();
		sum += Gruon.initMap(locationDict, map);
		if (sum <= 0){
			log.error("Language Model cannot be empty.");
			System.exit(1);
		}
		pTrie = new Gruon<Integer>(map);
		
	}
	
	// 通过Map初始化
	public LanguageModel(Map<String,Integer> map, long n){
		sum = n;
		if (sum <= 0){
			log.error("Language Model cannot be empty.");
			System.exit(1);
		}
		pTrie = new Gruon<Integer>(map);
	}
	
	// 通过Gluon初始化
	public LanguageModel(Gruon<Integer> trie, long n){
		sum = n;
		if (sum <= 0){
			log.error("Language Model cannot be empty.");
			System.exit(1);
		}
		pTrie = trie;
	}
	
	public LanguageModel(String locationDict, String locationTrie){
		sum = 0;
		Map<String,Integer> map = new TreeMap<String,Integer>();
		sum += Gruon.initMap(locationDict,map);
		if (sum <= 0){
			log.error("Language Model cannot be empty.");
			System.exit(1);
		}
		pTrie = new Gruon<Integer>(map,locationTrie);
		
	}
	
	public LanguageModel(String locationDict, String locationUser, String locationTrie){
		sum = 0;
		Map<String,Integer> map = new TreeMap<String,Integer>();
		sum += Gruon.initMap(locationDict,map);
		sum += Gruon.initMap(locationUser,map);
		if (sum <= 0){
			log.error("Language Model cannot be empty.");
			System.exit(1);
		}
		pTrie = new Gruon<Integer>(map,locationTrie);
	}
	
	/*
	 *  以上提供的初始化方式已经足够了。
	 */
	
	private Double getTrieValue(String key){
		Double v = 1.0;
		if (pTrie.containsKey(key)){
			v = Double.valueOf(pTrie.get(key));
		}
		v = v/sum;
		v = Math.log(v);
		return v;
	}
	
	private Double mValue = 1.5;
	public void setNewWordWeight(double v){
		log.info("mValue "+v);
		mValue = v;
	}
	private int sValue = 100;
	public void setSingleWordFrequency(int v){
		log.info("sValue "+v);
		sValue = v;
	}
	private boolean mDynamic = true;
	public void setDynamicWeight(boolean v){
		log.info("mDynamic "+v);
		mDynamic = v;
	}
	
	private Double getValue(String key, Map<String,Integer> map){
		Double v = 0.0;
		if (pTrie.containsKey(key)){
			v += Double.valueOf(pTrie.get(key));
		}
		if (map.containsKey(key)){
			//  (2.0/key.length())*
			if (mDynamic){
				v +=(2.0/key.length())*mValue*Double.valueOf(map.get(key));
			} else {
				v +=mValue*Double.valueOf(map.get(key));
			}
		}
		if (v < 0.001)
			v = 0.001;
		if (key.length() == 1 && v < sValue)
			v = (double) sValue;
		v = v/sum;
		v = Math.log(v);
		return v;
	}
	
	private List<Set<Integer>> initDAG(String sentence, Map<String,Integer> map){
	
		int n = sentence.length();
		List<Set<Integer>> DAG = new ArrayList<Set<Integer>>();
		
		for (int x = 0; x < n; ++x){
			Set<Integer> l = new TreeSet<Integer>();
			l.add(x);
			DAG.add(l);
		}
		
		for (String v : map.keySet()){
			if (v.length() > 1){
				int j = 0;
				int i = sentence.indexOf(v, j);
				while (i >= 0){
					j = i+v.length();
					DAG.get(i).add(j-1);
					i = sentence.indexOf(v, j);
				}
			}
			
		}
		int i = 0;
		while (i < n){
			List<String> vs = pTrie.commonPrefixSearch(sentence,i);
			for (String v : vs){
				int j = i+v.length();
				if (v.length() > 1 && sentence.indexOf(v) >= 0 && j <= sentence.length()) 
					DAG.get(i).add(j-1);
			}
			i += 1;	
		}
		
		return DAG;
	}
		
	private List<Tuple2<Double,Integer>> compute(String sentence, List<Set<Integer>> DAG){
		
		int n = sentence.length();
		
		List<Tuple2<Double,Integer>> route = new ArrayList<Tuple2<Double,Integer>>();
		for (int i = 0; i < n+1; ++i){
			route.add(null);
		}
		
		Tuple2<Double,Integer> t = new Tuple2<Double,Integer>(1.0, n);
		route.set(n, t);
		
		// 这里还能再快不少
		for (int idx = n-1; idx >= 0; --idx){
			Tuple2<Double,Integer> c = new Tuple2<Double,Integer>(Double.NEGATIVE_INFINITY,n-1);
			for (Integer x : DAG.get(idx)){
				Double v = getTrieValue(sentence.substring(idx,x+1));
				v += route.get(x+1).getValue1();
				if (v - c.getValue1() > 0){
					c.setValue1(v);
					c.setValue2(x);
				}
				
			};
			
			route.set(idx, c);
		}
		
		return route;
		
	}
	
	private List<Tuple2<Double,Integer>> compute( String sentence, 
												List<Set<Integer>> DAG,
												Map<String,Integer> map ){
		
		int n = sentence.length();
		
		List<Tuple2<Double,Integer>> route = new ArrayList<Tuple2<Double,Integer>>();
		for (int i = 0; i < n+1; ++i){
			route.add(null);
		}
		
		Tuple2<Double,Integer> t = new Tuple2<Double,Integer>(1.0, n);
		route.set(n, t);
		
		for (int idx = n-1; idx >= 0; --idx){
			Tuple2<Double,Integer> c = new Tuple2<Double,Integer>(Double.NEGATIVE_INFINITY,n-1);
			for (Integer x : DAG.get(idx)){
				Double v = getValue(sentence.substring(idx,x+1),map);
				v += route.get(x+1).getValue1();
				if (v - c.getValue1() > 0){
					c.setValue1(v);
					c.setValue2(x);
				}
			};
			
			route.set(idx, c);
		}
		
		return route;
		
	}
	
	//@Override
	public List<String> cut(String sentence){
		List<Set<Integer>> DAG = initDAG(sentence, new TreeMap<String,Integer>());
		
		List<Tuple2<Double,Integer>> route = compute(sentence,DAG);
		
		
		List<String> res = new ArrayList<String>();
		
		int x = 0;
		int n = sentence.length();
		
		while (x < n){
			int y = route.get(x).getValue2()+1;
			String v = sentence.substring(x,y);
			res.add(v);
			x = y;
		}
		
		return res;
		
	}
	
	//@Override
	public Map<String,Integer> extract(String sentence){
		Map<String,Integer> map = new TreeMap<String,Integer>();
		
		if (sentence != null){
			List<String> ts = cut(sentence);
			for (String t : ts){
				if (t.length() > 1){
					Integer o = map.get(t);
					if (o == null) o = 0;
					map.put(t, o+1);
				}
			}
		}
		
		return map;
	}
	
	private boolean containReduplication(List<String> vs){
		boolean i = false;
		if (vs.size() > 2){
			for (int k = 1; k < vs.size(); ++k){
				String v1 = vs.get(k);
				String v2 = vs.get(k-1);
				if (v1.length() == 1 && v2.length() == 2 && v1.equals(v2)){
					i = true;
					break;
				}
			}
		}
		return i;
	}
	
	private boolean containSingle(List<String> vs){
		boolean i = false;
		if (vs.size() > 1){
			for (String v : vs){
				if (v.length() <= 1){
					i = true;
					break;
				}
			}
		}		
		return i;
	}
	
	private Map<String,Integer> remove(Map<String,Integer> map){
		
		Map<String,Integer> m = new TreeMap<String,Integer>();
		
		for (String s : map.keySet()){
			// 若是频率大于3，一定是个好词，能加进去
			if (map.get(s) > 3){
				m.put(s, map.get(s));
				continue;
			}
			// 若是词表里包含这个词，也没问题
			if (pTrie.containsKey(s)){
				m.put(s, map.get(s));
				continue;
			}
			// 若是包含这个符号，就不要加进去了
			if (s.contains("·")){
				continue;
			}
				
			List<String> vs = cut(s);
			
			boolean i = containReduplication(vs);
			boolean j = containSingle(vs);
			// 包含单字而且单字不构成叠词
			if (j == true && i == false){
				m.put(s, map.get(s));
			}
		}
		
		return m;
	}

		
	public List<String> cut(String sentence, Map<String,Integer> map){
		
		List<String> res = new ArrayList<String>();
		map = remove(map);
		
		List<Set<Integer>> DAG = initDAG(sentence, map);
		
		List<Tuple2<Double, Integer>> route = compute(sentence,DAG,map);
		
		int x = 0;
		int n = sentence.length();
		
		while (x < n){
			int y = route.get(x).getValue2()+1;
			String v = sentence.substring(x,y);
			res.add(v);
			x = y;
		}

		return res;
	}
	

	
}
