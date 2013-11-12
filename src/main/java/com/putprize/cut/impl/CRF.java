package com.putprize.cut.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import com.putprize.cut.util.Gxion;
import com.putprize.cut.util.Pattern;
import com.putprize.cut.util.Quark;




public class CRF implements SentenceCutter,Serializable {
	
	private static final long serialVersionUID = 6459329875268063669L;

	private static final Logger log = Logger.getLogger(CRF.class);
	
	// Label and Observation Data
	private Quark zs;
	private Quark vs;
	
	// Feature Pattern Data
	private List<Pattern> ps;
	int nui;
	int nbi;
	
	// Model Data
	private int[] us;
	private int[] bs;
	
	private Gxion ts;
	
	public CRF(String locationModel){
		init(locationModel);
	}
	
	private static final String Pset = "!\"#$%&\'()*+,-./:;<=>?@[\\]^_`{|}~"+"！“”￥‘’、（），。：；《》？【】… ╗╚┐└—.";
	
	private String isP(char c){
		if (Pset.indexOf(c) >= 0){
			return "1";
		}
		return "0";
	}
	
	private static final String C_NUM = "0123456789";
	private static final String C_LETTER = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
	private static final String C_DATE = "年月日时分秒";
	
	private String indexC(char c){
		if (C_NUM.indexOf(c) >= 0)
			return "0";
		if (C_DATE.indexOf(c) >= 0)
			return "1";
		if (C_LETTER.indexOf(c) >= 0)
			return "2";
		return "3";
	}
	
	//@Override
	public List<String> cut(String sentence){
		
		List<String> ns = new ArrayList<String>();
		
		List<String[]> vs = new ArrayList<String[]>();
		
		// 这里是获取Feature的地方。
		for (int i = 0; i < sentence.length(); ++i){
			char c = sentence.charAt(i);
			vs.add(new String[] { sentence.substring(i,i+1),isP(c),indexC(c) });
		}
		
		List<String> zs = process(vs);
		
		for (int i = 0; i < zs.size(); ++i){
			if (zs.get(i).equals("s") || zs.get(i).equals("b"))
				zs.set(i, "1");
			else
				zs.set(i, "0");
		}
		
		int m = 0;
		for (int i = 1; i < sentence.length(); ++i){
			String pos = zs.get(i);
			if (pos.equals("1")){
				ns.add(sentence.substring(m,i));
				m = i;
				
			}
			
		}
		if (m < sentence.length()){
			ns.add(sentence.substring(m));
		}
		return ns;
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
	
	// =======================================================================
	// =======================================================================
	// =======================================================================	
	
	public List<String> process(List<String[]> vs){
		SeqT seq = convert(vs);
		int[] res = new int[seq.pos.length];
		
		viterbi(seq, res);
		
		List<String> zs = new ArrayList<String>();
		for (int i = 0; i < res.length; ++i){
			zs.add(this.zs.getKey(res[i]));
		}
		
		return zs;
		
	}
	
	// =======================================================================
	// =======================================================================
	// =======================================================================
	
	private void init(String locationModel){
		
		File file = new File(locationModel);
		
		try {
			Scanner scanner = new Scanner(file,"UTF-8");
			
			int ncat = initModelSize(scanner);
			initPatternData(scanner);
			zs = initQuarkData(scanner);
			vs = initQuarkData(scanner);
			initModel(scanner, ncat);
			
		} catch (FileNotFoundException e){
			e.printStackTrace();
			log.error(e.getMessage());
			System.exit(1);

		}
		
	}
	
	private int initModelSize(Scanner scanner){
		String line;
		String[] ns;
		
		line = scanner.nextLine();
		ns = line.trim().split("#");
		int ncat = Integer.valueOf(ns[ns.length-1]);
		return ncat;
	}
	
	private int initPatternData(Scanner scanner){
		ps = new ArrayList<Pattern>();
		nui = 0;
		nbi = 0;		
		
		String line = scanner.nextLine();
		String[] ns = line.trim().split("#");
		ns = ns[ns.length-1].split("/");
		int npts = Integer.valueOf(ns[0]);
		
		int i = 0;
		while (i < npts){
			line = scanner.nextLine();
			line = line.substring(0, line.length()-1);
			line = line.split(":",2)[1];
			ps.add(new Pattern(line));
			if (line.charAt(0) == 'u')
				nui += 1;
			if (line.charAt(0) == 'b')
				nbi += 1;
			if (line.charAt(0) == '*'){
				nui += 1;
				nbi += 1;
			}
			i += 1;
		}
		
		return npts;		
	}
	
	private Quark initQuarkData(Scanner scanner){
		Quark quark = new Quark();
		String line = scanner.nextLine();
		String[] ns = line.trim().split("#");
		int cnt = Integer.valueOf(ns[ns.length-1]);	
		int i = 0;
		while (i < cnt){
			line = scanner.nextLine();
			line = line.substring(0,line.length()-1);
			line = line.trim().split(":",2)[1];
			quark.put(line);
			i += 1;
		}
		return quark;
	}
	
	private void initModel(Scanner scanner, int n){
		int Y = zs.count();
		int O = vs.count();
		
		us = new int[O];
		bs = new int[O];
		
		int F = 0;
		for (int i = 0; i < vs.count(); ++i){
			String line = vs.getKey(i);
			int kind = 0;
			if (line.charAt(0) == 'u')
				kind = 1;
			if (line.charAt(0) == 'b')
				kind = 2;
			if (line.charAt(0) == '*')
				kind = 3;
			
			if ((kind & 1) == 1){
				us[i] = F;
				F += Y;
			}
			if ((kind & 2) == 2){
				bs[i] = F;
				F += Y*Y;
			}
		}

		ts = new Gxion(F);
		
		int i = 0;
		String line;
		String[] ns;
		while (i < n){
			line = scanner.nextLine();
			ns = line.trim().split("=");
			int c = Integer.valueOf(ns[0]);
			double t = Double.valueOf(ns[1]);
			ts.put(c, t);
			i += 1;
		}
		
		//ts.buildIndex();
	}
	
	
	// =======================================================================
	// =======================================================================
	// =======================================================================
	
	class PosT implements Serializable{
		
		private static final long serialVersionUID = -4025296063990691395L;
		
		int lbl;
		int ucnt;
		int bcnt;
		int[] uobs;
		int[] bobs;
		
		@Override
		public String toString(){
			return "ucnt:"+ucnt+
					" bcnt:"+bcnt;
		}
	}
	
	class SeqT implements Serializable{	
		
		private static final long serialVersionUID = 5625021740314891379L;
		
		PosT[] pos;
	}
	
	private SeqT convert(List<String[]> vs){
		int T = vs.size();
		
		SeqT seq = new SeqT();
		seq.pos = new PosT [T];
		for (int t = 0; t < T; ++t){
			seq.pos[t] = new PosT();
			seq.pos[t].lbl = 0;
			seq.pos[t].uobs = new int [this.nui];
			seq.pos[t].bobs = new int [this.nbi];
		}
		
		for (int t = 0; t < T; ++t){
			PosT pos = seq.pos[t];
			pos.ucnt = 0;
			pos.bcnt = 0;
			for (int x = 0; x < this.ps.size(); ++x){
				String obs = this.ps.get(x).execute(vs, t);
				Integer id = this.vs.getValue(obs);
				if (id == null)
					continue;
				int kind = 0;
				switch (obs.charAt(0)){
					case 'u': kind = 1; break;
					case 'b': kind = 2; break;
					case '*': kind = 3; break;
				}
				if ((kind & 1) == 1)
					pos.uobs[pos.ucnt++] = id;
				if ((kind & 2) == 2)
					pos.bobs[pos.bcnt++] = id;
			}
		}
		
		return seq;
		
	}
		
	private double viterbi(SeqT seq, int[] res){

		Gxion x = this.ts;
		
		int Y = this.zs.count();
		int T = seq.pos.length;
		
		double[][][] psi = new double [T][Y][Y];
		int[][] back = new int [T][Y];
		double[] cur = new double [Y];
		double[] old = new double [Y];
		
		
		// 1. Compute psi
		for (int t = 0; t < T; t++){
			PosT pos = seq.pos[t];
			for (int y = 0; y < Y; y++){
				double sum = 0.0;
				for (int n = 0; n < pos.ucnt; n++){
					int o = pos.uobs[n];
					double p = x.get(this.us[o]+y);
					sum += p;
					
				}
				for (int yp = 0; yp < Y; yp++){
					psi[t][yp][y] = sum;
				}
			}
		}
		for (int t = 1; t < T; t++){
			PosT pos = seq.pos[t];
			for (int yp = 0,d = 0; yp < Y; yp++){
				for (int y = 0; y < Y; y++,d++){
					double sum = 0;
					for (int n = 0; n < pos.bcnt; n++){
						int o = pos.bobs[n];
						double p = x.get(this.bs[o]+d);
						sum += p;
					}
					psi[t][yp][y] += sum;
				}
			}

		}
		
		// 2. Dynamic Programming
		for (int y = 0; y < Y; y++)
			cur[y] = psi[0][0][y];
		for (int t = 1; t < T; t++){
			for (int y = 0; y < Y; y++)
				old[y] = cur[y];
			for (int y = 0; y < Y; y++){
				double bst = -1.0;
				int idx = 0;
				for (int yp = 0; yp < Y; yp++){
					double val = psi[t][yp][y]+old[yp];
					if (val > bst){
						bst = val;
						idx = yp;
					}
				}
				back[t][y] = idx;
				cur[y] = bst;
			}	
		}
		
		// 3. BackTrack
		int bst = 0;
		for (int y = 1; y < Y; y++){
			if (cur[y] > cur[bst])
				bst = y;
		}
		double sc = cur[bst];
		for (int t = T; t > 0; t--){
			res[t-1] = bst;
			bst = back[t-1][bst];
		}
		return sc;
	}	
}

