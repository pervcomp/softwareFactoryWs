package com.codesmellws.app.controller;

import org.eclipse.jgit.api.Git;
import com.kotlin.App;
import com.kotlin.ScanOptionsKt;

public class ThreadAnalyser extends Thread {
	private String projectName;
	private Git git;
	private String analysisId;
	private Long date;
	private com.kotlin.ScanOptions so = null;

	
	public ThreadAnalyser(Git git,  com.kotlin.ScanOptions so, Long date, String analysisId, String projectName){
		this.projectName = projectName;
		this.git = git;
		this.analysisId = analysisId;
		this.date = date;
		this.so = so;
		
	}
	
	@Override
	public void run(){
		com.kotlin.App app = new App();
		app.analyseRevision(git, so, date, analysisId, projectName);
	}
}
