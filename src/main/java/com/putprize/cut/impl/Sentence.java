package com.putprize.cut.impl;

import java.io.Serializable;

public class Sentence implements Serializable{

	private static final long serialVersionUID = 6482490879462451045L;
	
	private String content;
	private boolean cut;
	
	public Sentence() {
	}
	
	public Sentence(String content){
		this.content = content;
	}
	
	public Sentence(String content, boolean isCut){
		this.content = content;
		this.cut = isCut;
	}
	
	public String getContent() {
		return content;
	}
	
	public void setContent(String content) {
		this.content = content;
	}
	
	public boolean isCut(){
		return cut;
	}
	
	public void setCut(boolean cut){
		this.cut = cut;
	}
	
	public String toString(){
		return this.content+":"+this.cut;
	}
	

}
