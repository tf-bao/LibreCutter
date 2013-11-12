package com.putprize.cut.util;

import java.io.Serializable;

public class Gxion implements Serializable {

	private static final long serialVersionUID = -7634310578892303528L;
	private double[][] mData;
	
	public Gxion(int size){
		int n = size>>8;
		mData = new double[n+1][];
		for (int j = 0; j < n+1; ++j){
			mData[j] = null;
		}
	}
	
	public void put(int i, double x){
		
		int j = i>>8;
		int k = i&0xFF;
		
		
		if (mData[j] != null){
			mData[j][k] = x;
			return;
		}
		
		double[] z= new double[256];
		for (int t = 0; t < 256; ++t)
			z[t] = 0.0;
		z[k] = x;
		mData[j] = z;
	}
	
	public double get(int i){
		
		int j = i>>8;
		if (mData[j] == null)
			return 0.0;
		
		int k = i&0xFF;
		
		return mData[j][k];
		
	}	
	

}
