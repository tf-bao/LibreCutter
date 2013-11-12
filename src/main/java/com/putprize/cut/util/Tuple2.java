package com.putprize.cut.util;

import java.io.Serializable;

public class Tuple2 <T1,T2> implements Serializable {
	
	private static final long serialVersionUID = -7532288782791336543L;
	
	private T1 v1;
	private T2 v2;
	
	public Tuple2() {
		v1 = null;
		v2 = null;
	}
	
	public Tuple2(T1 v1, T2 v2){
		this.v1 = v1;
		this.v2 = v2;
	}
	
	public T1 getValue1() {
		return v1;
	}
	
	public void setValue1(T1 v){
		this.v1 = v;
	}
	
	public T2 getValue2(){
		return v2;
	}
	
	public void setValue2(T2 v){
		this.v2 = v;
	}
	
	@Override
	public String toString(){
		return v1.toString()+":"+v2.toString();
	}	

}
