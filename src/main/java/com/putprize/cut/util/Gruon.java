package com.putprize.cut.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Map.Entry;




import org.apache.log4j.Logger;

import com.putprize.cut.trie.DoubleArrayTrie;

public class Gruon <T> implements Serializable {
	
	private static final long serialVersionUID = -3104493827559594070L;

	private static final Logger log = Logger.getLogger(Gruon.class);
	
	private DoubleArrayTrie trie;
	private List<String> ks;
	private List<T> vs;
	
	
	public Gruon(List<String> ks, List<T> vs){
		this.ks = ks;
		this.vs = vs;
		trie = new DoubleArrayTrie();
		trie.build(ks);
	}
	
	private void init(Map<String,T> map){
		List<Tuple2<String, T>> list = new ArrayList<Tuple2<String,T>>();
		for (Entry<String,T> e : map.entrySet()){
			list.add(new Tuple2<String,T>(e.getKey(),e.getValue()));
		}
		
		Collections.sort(list, new Comparator<Tuple2<String,T>>(){
				//@Override
				public int compare(Tuple2<String, T> arg0, Tuple2<String, T> arg1) {
					return arg0.getValue1().compareTo(arg1.getValue1());
				}
			}
		);		
		
		ks = new ArrayList<String>();
		vs = new ArrayList<T>();
		
		for (Tuple2<String,T> p : list){
			ks.add(p.getValue1());
			vs.add(p.getValue2());
		}		
	}
	
	public Gruon(Map<String,T> map){
		
		init(map);
		trie = new DoubleArrayTrie();
		trie.build(ks);
	}
	
	public Gruon(Map<String,T> map, String location) {
		init(map);
		trie = new DoubleArrayTrie();
		try {
			trie.open(location);
		} catch (IOException e){
			e.printStackTrace();
		}
	}
	
	public void saveTrie(String location){
		try {
			trie.save(location);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public List<String> commonPrefixSearch(String s){
		List<String> rs = new ArrayList<String> ();
		if (s != null && s.length() > 0){
			List<Integer> ns = trie.commonPrefixSearch(s);
			if (ns.size() > 0){
				for (Integer n : ns)
					rs.add(this.ks.get(n));
			}
		}
		return rs;
	}
	
	public List<String> commonPrefixSearch(String s, int pos){
		List<String> rs = new ArrayList<String> ();
		if (s != null && s.length() > 0){
			List<Integer> ns = trie.commonPrefixSearch(s, pos, s.length(), 0);
			if (ns.size() > 0){
				for (Integer n : ns)
					rs.add(this.ks.get(n));
			}
		}
		return rs;
	}
	
	public boolean containsKey(String s){
		
		if (s != null && s.length() > 0){
			Integer n = trie.exactMatchSearch(s);
			if (n >= 0){
				return true;
			}
		}
		
		return false;
	}
	
	public T get(String s){
		
		if (s != null && s.length() > 0){
			Integer n = trie.exactMatchSearch(s);
			if (n >= 0){
				return this.vs.get(n);
			}
		}
		
		return null;
	}
	
	
	static public long initMap(String location, Map<String,Integer> map){
		long n = 0;
		File file = new File(location);
		Scanner scanner;
		String line = null;
		try {
			scanner = new Scanner(file, "UTF-8");
			while (scanner.hasNextLine()) {
				line = scanner.nextLine();
				List<String> ts = Arrays.asList(line.split("\\s"));
				String v = ts.get(0);
				//if (map.containsKey(v))
				//	continue;
				Integer c = Integer.valueOf(ts.get(1));
				if (c <= 0 && map.containsKey(v)){
					map.remove(v);
				} else {
					map.put(v, c);
					n += c;
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			log.error(e.getMessage());
			System.exit(1);
		} catch (ArrayIndexOutOfBoundsException e){
			e.printStackTrace();
			log.error(e.getMessage()+":"+line);
			System.exit(1);
		}
		
		
		return n;
	}	

}
