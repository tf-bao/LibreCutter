package com.putprize.cut;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.putprize.cut.SemanticCutterProperty;
import com.putprize.cut.util.Gruon;

public class SemanticCutterSystem {

	private static final Logger log = Logger.getLogger(SemanticCutterSystem.class);
	
	@Parameter(description="Dict", names="-dict")
	private String locationDict = SemanticCutterProperty.get("LOCATION_DICT");
	
	@Parameter(description="User", names="-user")
	private String locationUser = SemanticCutterProperty.get("LOCATION_USER");
	
	@Parameter(description="Trie", names="-trie")
	private String locationTrie = SemanticCutterProperty.get("LOCATION_TRIE");
	
	@Parameter(description="Model", names="-model")
	private String locationModel = SemanticCutterProperty.get("LOCATION_MODEL");
	
	@Parameter(description="Combinator", names="-combinator")
	private String locationCombinator = SemanticCutterProperty.get("LOCATION_COMBINATOR");
	
	@Parameter(description="Serialization",names="-serialize")
	private String locationSerialize = SemanticCutterProperty.get("LOCATION_SERIALIZE");
	
	@Parameter(description="Port", names="-port")
	private int port = Integer.valueOf(SemanticCutterProperty.get("SERVER_PORT"));
	
	@Parameter(description="Mode", names="-mode")
	private String mode = "convert"; // "convert" "server" "segment" "serialize"
	
	@Parameter(description="Data1", names="-data1")
	private String locationData1 = "data1.txt";
	
	@Parameter(description="Data2", names="-data2")
	private String locationData2 = "data2.txt";
	
	public void syncProp(Properties prop){
		prop.setProperty("LOCATION_DICT", locationDict);
		prop.setProperty("LOCATION_USER", locationUser);
		prop.setProperty("LOCATION_TRIE", locationTrie);
		prop.setProperty("LOCATION_MODEL", locationModel);
		prop.setProperty("LOCATION_COMBINATOR", locationCombinator);
		prop.setProperty("LOCATION_SERIALIZE", locationSerialize);
		prop.setProperty("SERVER_PORT", String.valueOf(port));
	}
	
	// 根据默认词典和用户词典生成Trie。
	public static void convert(String n1, String n2, String n3){
		log.info("Convertion Start");
		Map<String,Integer> map = new TreeMap<String,Integer>();
		
		Long sum = 0L;
		
		sum += Gruon.initMap(n1,map);
		log.info("Init Dict Done");
		sum += Gruon.initMap(n2,map);
		log.info("Init User Done");
		
		Gruon<Integer> p = new Gruon<Integer>(map);
		log.info("Build Trie Done");
		//p.saveTrie(n3);
		
		try {
			ObjectOutputStream f = new ObjectOutputStream(new FileOutputStream(n3));
			f.writeObject(p); // First Trie
			f.writeObject(sum); // Then Long
			f.close();
		} catch (IOException e){
			log.error(e);
			System.exit(1);
		}
		
		log.info("Convertion Done");
	}
	
	// 对SemanticCutter序列化
	public static void serialize(String location){
		SemanticCutter cut = new SemanticCutter(SemanticCutterProperty.getProperties());
		
		ObjectOutputStream f;
		try {
			f = new ObjectOutputStream(new FileOutputStream(location));  
			f.writeObject(cut);
			f.close();
		} catch (IOException e){
			log.error(e);
			System.exit(1);
		}
	}
	
	private static void _segment(SemanticCutter cut, String x1, String x2){
		File f1 = new File(x1);
		File f2 = new File(x2);
		
		BufferedReader p1;
		BufferedWriter p2;
		
		try {
			p1 = new BufferedReader(new FileReader(f1));
			p2 = new BufferedWriter(new FileWriter(f2));
			
			String line;
			int count = 0;
			while ( (line=p1.readLine()) != null){
				count += 1;
				if (count % 1000 == 0){
					log.info(count);
				}
				line = line.trim();
				StringBuffer buffer = new StringBuffer();
				List<String> vs = cut.process(line);
				for (String v : vs){
					buffer.append(v+" ");
				}
				line = buffer.toString().trim();
				line = line.replaceAll("[\\pP]", " ");
				p2.write(line+"\n");
			} 
		} catch (IOException e){
			e.printStackTrace();
			log.error(e.getMessage());
			System.exit(1);
		}
	}
	
	public static void segment(Properties prop, String x1, String x2){
		SemanticCutter cut = new SemanticCutter(prop);
		_segment(cut,x1,x2);
	}
	
	
	public static void main(String[] args) {
		
		SemanticCutterSystem u = new SemanticCutterSystem();
		JCommander com = new JCommander(u);
		
		try {
			com.parse(args);
		} catch (ParameterException e){
			System.err.println(e.getMessage());
			com.usage();
			return;
		}
		
		Properties prop = SemanticCutterProperty.getProperties();
		u.syncProp(prop);
		
		if (u.mode.equals("convert")) {
			convert(u.locationDict, u.locationUser, u.locationTrie);
		} 
		else if (u.mode.equals("server")) {
			SemanticCutterServer.run(prop);
		}
		else if (u.mode.equals("segment")) {
			segment(prop, u.locationData1, u.locationData2);
		}
		else if (u.mode.equals("serialize")){
			serialize(u.locationSerialize);
		}
		else {
			System.out.println("Mode could be convert|server|segment");
		}
	}
	
	
}
