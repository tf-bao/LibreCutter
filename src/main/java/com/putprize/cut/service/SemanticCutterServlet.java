package com.putprize.cut.service;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.putprize.cut.SemanticCutter;


public class SemanticCutterServlet extends HttpServlet {

	private static final long serialVersionUID = 4145718099487658971L;
	
	private SemanticCutter cut;
	
	public SemanticCutterServlet(SemanticCutter c){
		cut = c;
	}
	
	@Override
	protected void doGet(HttpServletRequest rq, HttpServletResponse rp)
			throws ServletException,IOException{
		process(rq,rp);
	}
	
	@Override
	protected void doPost(HttpServletRequest rq, HttpServletResponse rp)
			throws ServletException,IOException{
		process(rq,rp);
	}
	
	void process(HttpServletRequest rq, HttpServletResponse rp)
			throws ServletException,IOException{
		
		rp.setCharacterEncoding("UTF-8");
		rp.setContentType("text/html");
		
		String line = rq.getParameter("line");
		
		rq.setCharacterEncoding("UTF-8");
		PrintWriter out = rp.getWriter();

		StringBuffer buffer = new StringBuffer();
		List<String> ns = cut.process(line);
		for (String n : ns)
			buffer.append(n + " ");
		
		out.println(buffer.toString().trim());
	}
	

}
