package com.codesmellws.app.controller;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.lang.ProcessBuilder.Redirect;
import java.nio.file.Path;
import java.util.Base64;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.kotlin.App;
import com.kotlin.ScanOptionsKt;

@RestController
class MainController {
		private String projectName = "";
		private com.kotlin.ScanOptions so = null;
		private com.kotlin.App app = new App();
		private String analysisId = "";

	    @RequestMapping("/analyseRevision")
	    public String analyseRevision(@RequestParam(value="url") String url, 
	    		@RequestParam(value="conf") String conf,
	    		@RequestParam(value="projectName") String projectName,
	    		@RequestParam(value="analysis") String analysisId,
	    		@RequestParam(value="sha") String sha)  {
		    String result = "";
		    	boolean theSame = false;    
		   
		    	if (!new File(projectName).exists()) {
		    		String workingDir = System.getProperty("user.dir");
		    		try {
						this.execute("git clone "+url+" "+ projectName,new File(workingDir));
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
		    		
		    	}
		    	else{
		    		String workingDir = System.getProperty("user.dir");
		    		try {
						this.execute("git pull",new File(workingDir+"/"+projectName));
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
		    	}
		    	
		    
			this.projectName = projectName;
			this.analysisId = analysisId;
			
			try {
				if (!conf.isEmpty()){
				File file = new File(projectName + ".properties");
				if (!file.exists()) {
					byte[] decodedString = Base64.getUrlDecoder().decode(conf.replace("%3D",""));
					conf = new String(decodedString, "UTF-8");
					PrintWriter writer = new PrintWriter(file);
					writer.println(conf);
					writer.close();
				}}
				String args[] = { "--git", url, "--properties", projectName + ".properties" };
			    so = ScanOptionsKt.parseOptions(args);
			    Git git = app.openLocalRepository(projectName);
			    result = app.analyseRevision(git, so, sha);
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		return result;
	    }
	
	    @RequestMapping("/getActualError")
	    public String getError(){
	    		return app.getActualError();
	    }
	    
	    @RequestMapping("/deleteProject")
	    public String deleteProject(@RequestParam(value="projectName") String projectName){
	    	for (File f : new File(".").listFiles()) {
	    	    if (f.getName().startsWith(projectName)) {
	    	        f.delete();
	    	    }
	    	}
	    		return "OK";
	    }
	    
		private void execute(String command, File directory)
				throws Exception {
			System.out.println("$ " + command);
			ProcessBuilder pb = new ProcessBuilder(command.split(" "));
			pb.directory(directory);
			pb.redirectErrorStream(true);
	        pb.redirectOutput(Redirect.INHERIT);
	        Process p = pb.start();
	        p.waitFor();
		}
	    
	    @RequestMapping("/updateConfFile")
	    public String updateConfFile( @RequestParam(value="conf") String conf,
	    		@RequestParam(value="projectName") String projectName) {
	  
		   new File(projectName + ".properties").deleteOnExit();

	    		byte[] decodedString = Base64.getUrlDecoder().decode(conf.replace("%3D",""));
				try {
					conf = new String(decodedString, "UTF-8");
					PrintWriter writer = new PrintWriter(new File(projectName + ".properties"));
					writer.println(conf);
					writer.close();
				} catch (UnsupportedEncodingException | FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

	     	return "OK";
	    }




}
