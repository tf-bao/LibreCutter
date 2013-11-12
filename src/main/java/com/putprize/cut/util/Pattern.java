package com.putprize.cut.util;

import java.io.Serializable;
import java.util.List;


//尚未实现正则表达式的支持

public class Pattern implements Serializable {
	
	private static final long serialVersionUID = 682377967730673422L;
	String src;
	int ntoks;
	int nitems;
	PatternItem[] items;
	
	@Override
	public String toString(){
		return src;
	}
	
	class PatternItem implements Serializable{

		private static final long serialVersionUID = -2344210830493339913L;
		
		char type;
		boolean caps;
		String value;
		int offset;
		int column;
		
		@Override
		public String toString(){
			return "Type:"+type+
					" Caps:"+caps+
					" Offset:"+offset+
					" Column:"+column+
					" Value:"+value;
		}
	}
	
	
	public Pattern(String line){
		compile(line);
	}
	
	
	private void compile(String p){
		
		int mitems = 0;
		for (int pos = 0; pos < p.length(); pos++)
			if (p.charAt(pos) == '%')
				mitems++;
		mitems = mitems*2+1;
		
		items = new PatternItem[mitems];
		src = p;
		
		int nitems = 0;
		int ntoks = 0;
		int pos = 0;
		while (pos < p.length()){
			PatternItem item = new PatternItem();//items[nitems++];
			item.value = null;
			if (p.charAt(pos) == '%'){
				char t = p.charAt(pos+1);
				t = Character.toLowerCase(t);
				if (t != 'x' && t != 't' && t != 't'){
					System.exit(1);
				}
				item.type = t;
				item.caps = (p.charAt(pos+1) != t);
				pos += 2;
				
				int o,c;
				int qos = p.indexOf(']', pos);
				if (qos < 0){
					System.exit(1);
				}
				String s = p.substring(pos+1,qos);
				String[] ns = s.split(",");
				o = Integer.valueOf(ns[0].trim());
				c = Integer.valueOf(ns[1].trim());
				item.offset = o;
				item.column = c;
				ntoks = Math.max(ntoks,c);
				
				if (t == 't' || t == 'm'){
					item.value = ns[2];
				}
				pos = qos;
				pos++;
			} else {
				int start = pos;
				while ( pos < p.length() && p.charAt(pos) != '%')
					pos++;
				
				item.type = 's';
				item.caps = false;
				item.value = p.substring(start,pos);
				item.offset = 0;
				item.column = 0;
			}
			items[nitems++] = item;
			//System.out.println(item);
		}
		
		this.ntoks = ntoks;
		this.nitems = nitems;
		
	}
	
	
	public String execute(List<String[]> vs, int at){
		String[] bval = {"_x-1", "_x-2", "_x-3", "_x-4", "_x-#"};
		String[] eval = {"_x+1", "_x+2", "_x+3", "_x+4", "_x+#"};
		int T = vs.size();
		StringBuffer buffer = new StringBuffer();
		
		for(int it = 0; it < this.nitems; ++it){
			PatternItem item = this.items[it];
			String value = null;
			
			// First, if needed, we retrieve the token.
			if (item.type != 's'){
				int pos = at + item.offset;
				int col = item.column;
				if (pos < 0)
					value = bval[Math.min(-pos-1, 4)];
				else if (pos >= T)
					value = eval[Math.min(pos-T, 4)];
				else
					value = vs.get(pos)[col];
			}
			
			// Next, we handle the command.
			if (item.type == 's'){
				value = item.value;
			} else if (item.type == 'x'){
			} else if (item.type == 't'){
			} else if (item.type == 'm'){
			}
			
			buffer.append(value);
			
		}
		
		return buffer.toString();
	}
}
