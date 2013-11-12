package com.putprize.cut;

import java.util.Properties;

import javax.servlet.Servlet;

//import org.apache.log4j.Logger;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import com.putprize.cut.impl.CRF;
import com.putprize.cut.impl.LanguageModel;

import com.putprize.cut.service.SemanticCutterServlet;
import com.putprize.cut.service.SentenceCutterServlet;

public class SemanticCutterServer {
	
	//private static final Logger log = Logger.getLogger(SemanticCutterServer.class);
	
	public static void run(Properties prop){
		
		SemanticCutter cut = new SemanticCutter(prop);
		LanguageModel mCut = cut.getLanaugeModel();
		CRF cCut = cut.getCRF();
		
		int port = Integer.valueOf(prop.getProperty("SERVER_PORT"));
		Server server = new Server(port);
		ServletContextHandler root = new ServletContextHandler(server, "");
		
		Servlet cutServlet = new SemanticCutterServlet(cut);
		Servlet segmentServlet = new SemanticCutterServlet(cut);
		Servlet mCutServlet = new SentenceCutterServlet(mCut);
		Servlet cCutServlet = new SentenceCutterServlet(cCut);
		
		root.addServlet(new ServletHolder(cutServlet), "/cut/*");
		root.addServlet(new ServletHolder(segmentServlet), "/segment/*");
		root.addServlet(new ServletHolder(mCutServlet), "/l/*");
		root.addServlet(new ServletHolder(cCutServlet), "/c/*");
		
		try {
			server.start();
		} catch (Exception e){
			e.printStackTrace();
		}
		
	}

}
