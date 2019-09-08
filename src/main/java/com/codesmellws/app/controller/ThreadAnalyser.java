package com.codesmellws.app.controller;

import org.eclipse.jgit.api.Git;
import com.kotlin.App;
import com.kotlin.ScanOptionsKt;

public class ThreadAnalyser extends Thread {
	private String projectName;
	private Git git;
	private String analysisId;
	private Long startDate;
	private Long endDate;
	private String mongoURI;
	private int port;
	private com.kotlin.ScanOptions so = null;

	
	public ThreadAnalyser(Git git,  com.kotlin.ScanOptions so, Long startDate, Long endDate, String analysisId, String projectName, String mongoURI, int port){
		this.projectName = projectName;
		this.git = git;
		this.analysisId = analysisId;
		this.startDate = startDate;
		this.endDate = endDate;
		this.so = so;
		this.mongoURI = mongoURI;
		this.port = port;
		
	}
	
	@Override
	public void run(){
		com.kotlin.App app = new App();
		app.analyseRevision(git, so, startDate,endDate, analysisId, projectName,mongoURI,port);
	}
}
