package com.putprize.cut.service;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.putprize.cut.impl.SentenceCutter;

public class SentenceCutterServlet extends HttpServlet {

	private static final long serialVersionUID = 7608598987509180525L;
	
	private SentenceCutter cut;
	
	public SentenceCutterServlet(SentenceCutter c){
		cut = c;
	}
	
	protected void doGet(HttpServletRequest rq, HttpServletResponse rp)
			throws ServletException,IOException{
		
		rp.setCharacterEncoding("UTF-8");
		rp.setContentType("text/html");
		
		String line = rq.getParameter("line");
		
		rp.setCharacterEncoding("UTF-8");
		PrintWriter out = rp.getWriter();
		
		StringBuffer buffer = new StringBuffer();
		
		List<String> ns = cut.cut(line);
		for (String n : ns)
			buffer.append(n + " ");
		
		out.println(buffer.toString().trim());
	}	
	

}
