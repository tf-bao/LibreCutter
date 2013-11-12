package com.putprize.cut;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import com.putprize.cut.SemanticCutter;
import com.putprize.cut.impl.CRF;
import com.putprize.cut.impl.Combinator;
import com.putprize.cut.impl.LanguageModel;
import com.putprize.cut.impl.NumberExtractor;
import com.putprize.cut.impl.Sentence;
import com.putprize.cut.impl.SentenceSpliter;
import com.putprize.cut.impl.TokenExtractor;
import com.putprize.cut.util.Gruon;

public class SemanticCutter implements Serializable {
	
	private static final long serialVersionUID = 4125795566431143268L;

	private static final Logger log = Logger.getLogger(SemanticCutter.class);
	
	private LanguageModel m;
	public LanguageModel getLanaugeModel(){
		return m;
	}
	
	private TokenExtractor m1;
	private NumberExtractor m2;
	
	private CRF m3;
	public CRF getCRF(){
		return m3;
	}
	
	
	private Combinator m4;
	
	private int DEFAULT_TOKEN_FREQUENCY = 1000000;
	public void setDefaultTokenFrequencey(int v){
		log.info("DEFAULT_TOKEN_FREQUENCY "+v);
		DEFAULT_TOKEN_FREQUENCY = v;
	}
	private int DEFAULT_NUMBER_FREQUENCY = 1000000;
	public void setDefaultNumberFrequencey(int v){
		log.info("DEFAULT_NUMBER_FREQUENCY "+v);
		DEFAULT_NUMBER_FREQUENCY = v;
	}
	private int NEW_WORD_MAX_LENGTH = 7;
	public void setNewWordMaxLength(int v){
		log.info("NEW_WORD_MAX_LENGTH "+v);
		NEW_WORD_MAX_LENGTH = v;
	}
	
	
	private boolean onCombinator = false;
	void setOnCombinator(boolean on){
		onCombinator = on;
	}
	private boolean omitSpace = false;	
	void setOmitSpace(boolean omit){
		omitSpace = omit;
	}
	
	// 初始化方式一：通过三个组件整合
	public SemanticCutter(LanguageModel m,
				   CRF mCRF,
				   Combinator mCom){
		this.m = m;
		this.m1 = new TokenExtractor();
		this.m2 = new NumberExtractor();
		this.m3 = mCRF;
		this.m4 = mCom;
		
		if (this.m == null){
			log.error("LanguageModel cannot be null!");
			System.exit(1);
		}
		if (this.m3 == null){
			log.error("CRF cannot be null!");
			System.exit(1);
		}
		if (this.m4 == null){
			log.error("Combinator cannot be null!");
			System.exit(1);
		}

	}
	
	// 这个是对外的通用接口
	// 初始化方式二：通过Prop来读取全部参数，然后初始化
	public SemanticCutter(Properties prop)  {
		
		long s = System.currentTimeMillis();
		log.info("Init LangageModel");
		
		// 反序列化得到Trie，然后初始化语言模型
		String locationTrie = prop.getProperty("LOCATION_TRIE");
		try {
			ObjectInputStream f = new ObjectInputStream(new FileInputStream(locationTrie));
			
			@SuppressWarnings("unchecked")
			Gruon<Integer> trie = (Gruon<Integer>)f.readObject();
			long sum = (Long)f.readObject();
			
			this.m = new LanguageModel(trie,sum);
			f.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e){
			e.printStackTrace();
		} catch (Exception e){
			e.printStackTrace();
		}
		
		
		this.m.setNewWordWeight( Double.valueOf(
				prop.getProperty("NEW_WORD_WEIGHT")));
		this.m.setDynamicWeight( Boolean.valueOf(
				prop.getProperty("NEW_WORD_DYNAMIC_WEIGHT")));
		this.m.setSingleWordFrequency( Integer.valueOf(
				prop.getProperty("DEFAULT_CHAR_FREQUENCY")));	
		log.info("Init LanguageModel Done "+(System.currentTimeMillis()-s));
		
		this.m1 = new TokenExtractor();
		this.m2 = new NumberExtractor();
		
		log.info("Init CRF");
		String location4 = prop.getProperty("LOCATION_MODEL");
		this.m3 = new CRF(location4);
		log.info("Init CRF Done "+(System.currentTimeMillis()-s));
		
		String location5 = prop.getProperty("LOCATION_COMBINATOR");
		this.m4 = new Combinator(location5);
		this.m4.setCombinatorThreshold( Double.valueOf(
				prop.getProperty("COMBINATOR_THRESHOLD")));
		
		onCombinator = Boolean.valueOf(
				prop.getProperty("COMBINATOR_ON"));
		
		omitSpace = Boolean.valueOf(
				prop.getProperty("OMIT_SPACE"));	
		
		this.setDefaultTokenFrequencey(Integer.valueOf(
				prop.getProperty("DEFAULT_TOKEN_FREQUENCY")));
		this.setDefaultNumberFrequencey(Integer.valueOf(
				prop.getProperty("DEFAULT_NUMBER_FREQUENCY")));
		this.setNewWordMaxLength(Integer.valueOf(
				prop.getProperty("NEW_WORD_MAX_LENGTH")));
		
	}
	
	// 对句分词
	public List<String> cut(String sentence){
		List<String> ns;
		
		// 若句子长度小于3，不再分词
		if (sentence.length() < 3){
			ns = new ArrayList<String>();
			ns.add(sentence);
			return ns;
		}
		
		Map<String,Integer> ts1 = m1.extract(sentence);
		Map<String,Integer> ts2 = m2.extract(sentence);
		Map<String,Integer> ts3 = m3.extract(sentence);
		
		Map<String,Integer> map = new TreeMap<String,Integer>();
		
		// 识别出数字、英文词
		for (String t : ts1.keySet()){
			Integer o = map.get(t);
			if (o == null) o = 0;
			map.put(t, o+DEFAULT_TOKEN_FREQUENCY);
		}
		
		// 识别出关于数字的词
		for (String t : ts2.keySet()){
			Integer o = map.get(t);
			if (o == null) o = 0;
			map.put(t, o+DEFAULT_NUMBER_FREQUENCY);
		}
		
		// CRF识别出的新词
		for (String t : ts3.keySet()){
			if (t.length() < NEW_WORD_MAX_LENGTH){
				//System.out.println(t);
				Integer o = map.get(t);
				if (o == null) o = 0;
				map.put(t, o+1);
			}
		}
		
		ns = m.cut(sentence, map);
		
		return ns;
	}
	
	// 先分句，再分词
	private List<String> _process(String text){
		
		
		List<String> ns = new ArrayList<String>();
		
		if (text != null && text.length() > 0){
			List<Sentence> list = SentenceSpliter.split(text,omitSpace);
			for (Sentence s : list){
				if (s.isCut()){
					List<String> vs = cut(s.getContent());
					for (String v : vs){
						ns.add(v);
					}
				} else {
					ns.add(s.getContent());
				}
			}
			// 后处理
			if (onCombinator)
				ns = m4.process(ns);
		}
		
		

		
		return ns;
		
	}
	
	// 先分句，再分词
	public List<String> process(String text){
		
		try {
			List<String> res = _process(text);
			return res;
			
		} catch (Exception e){
			log.error(e.getMessage());
			log.error(text);			
		}
		
		return null;
		
	}

}
