package com.putprize.cut.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.apache.log4j.Logger;


public class Combinator implements Serializable {
	
	private static final long serialVersionUID = -8936778680569244981L;

	private static final Logger log = Logger.getLogger(Combinator.class);
	
	private Map<String,Double> map;
	
	private double P;
	public void setCombinatorThreshold(double p){
		P = p;
	}
	
	public Combinator(String location){
		map = new HashMap<String,Double>();
		init(location,map);
		P = 0.85;
	}
	
	public Combinator(String location, double threshold){
		map = new HashMap<String,Double>();
		init(location,map);
		P = threshold;
	}
	
	private void init(String location, Map<String,Double> map){
		File file = new File(location);
		Scanner scanner = null;
		String line = null;
		try {
			scanner = new Scanner(file, "UTF-8");
			while (scanner.hasNextLine()){
				line = scanner.nextLine();
				List<String> ts = Arrays.asList(line.split("\\s"));
				String v = ts.get(0);
				
				Double p = Double.valueOf(ts.get(1));
				map.put(v, p);
			}
		} catch (FileNotFoundException e){
			e.printStackTrace();
			log.error(e.getMessage());
			System.exit(1);
		}
	}
	
	public List<String> process(List<String> vs){
		List<String> ts = new ArrayList<String>();
		
		StringBuffer t = new StringBuffer();
		for (int i = 0; i < vs.size(); ++i){
			String v= vs.get(i);
			if (v.length() > 1){
				if (t.length() > 0){
					ts.add(t.toString());
					t.setLength(0);
				}
				ts.add(v);
			}
				
			else {
				Double p = map.get(v);
				if (p == null) p = 0.0;
				if (p > P){
					t.append(v);
				} else {
					if (t.length() > 0){
						ts.add(t.toString());
						t.setLength(0);
					}
					ts.add(v);
				}
			}
		}
		
		if (t.length() > 0)
			ts.add(t.toString());
		
		return ts;
	}

}
