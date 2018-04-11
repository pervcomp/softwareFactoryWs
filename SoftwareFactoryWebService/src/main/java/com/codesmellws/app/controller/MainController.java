package com.codesmellws.app.controller;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.lang.ProcessBuilder.Redirect;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


import com.kotlin.App;
import com.kotlin.ScanOptionsKt;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.Mongo;
import com.mongodb.MongoClient;

@RestController
class MainController {

	
	private String projectName = "";
	private com.kotlin.ScanOptions so = null;
	private com.kotlin.App app = new App();
	private String analysisId = "";

	@RequestMapping("/newProject")
	public String newProject(@RequestParam(value = "url") String url,
			@RequestParam(value = "projectName") String projectName) {
		if (!new File(projectName).exists()) {
			try {
				Git git = Git.cloneRepository()
						  .setURI( url )
						  .setDirectory(new File(projectName))
						  .call();
			} catch (GitAPIException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		return "OK";
	}

	@RequestMapping("/analyseRevision")
	public List<String> analyseRevision(@RequestParam(value = "url") String url,
			@RequestParam(value = "conf") String conf, @RequestParam(value = "projectName") String projectName,
			@RequestParam(value = "analysis") String analysisId, @RequestParam(value = "date") Long date) {
		 Mongo mongo = new MongoClient("54.202.210.49", 27017);
		 DB db = mongo.getDB("admin"); 
		 DBCollection collection = db.getCollection("commitAnalysis");
		 BasicDBObject searchQuery = new BasicDBObject().append("idProject", projectName)
				 .append("idSerial", analysisId);
		 BasicDBObject update = new BasicDBObject();
		 update.append("status", "Processing"); 
		 BasicDBObject setQuery = new BasicDBObject();
		 setQuery.append("$set", update);
		 collection.update(searchQuery, update);
		List<String> result = null;
		boolean theSame = false;
		Git git = null;
		if (!new File(projectName).exists()) {
			String workingDir = System.getProperty("user.dir");
			try {
				git = Git.cloneRepository().setURI(url).setDirectory(new File(projectName)).call();
				Thread.sleep(2000);
				while (!hasAtLeastOneReference(git.getRepository())) {
					Thread.sleep(2000);
				}
				// this.execute("git clone "+url+" "+ projectName,new
				// File(workingDir));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			String workingDir = System.getProperty("user.dir");
			try {

				File d = new File(projectName + "/.git");
				try {
					git = Git.open(d);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				git.pull();
				Thread.sleep(2000);
				while (!hasAtLeastOneReference(git.getRepository())) {
					Thread.sleep(2000);
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		this.projectName = projectName;
		this.analysisId = analysisId;

		try

		{
			if (!conf.isEmpty()) {
				File file = new File(projectName + ".properties");
				
					byte[] decodedString = Base64.getUrlDecoder().decode(conf.replace("%3D", ""));
					conf = new String(decodedString, "UTF-8");
					PrintWriter writer = new PrintWriter(file);
					writer.println(conf);
					writer.close();
				
			}
			String args[] = { "--git", url, "--properties", projectName + ".properties" };
			so = ScanOptionsKt.parseOptions(args);
			// Git git = app.openLocalRepository(projectName + "/.git");
			result = app.analyseRevision(git, so, date, analysisId, projectName);
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
		
		  searchQuery = new BasicDBObject().append("idProject", projectName);
		  searchQuery.append("idSerial", analysisId);
		  update = new BasicDBObject();
		  update.append("status", "Finished"); 
		  setQuery = new BasicDBObject();
		  setQuery.append("$set", update);
		  collection.update(searchQuery, update);

		return result;
	}

	@RequestMapping("/getActualError")
	public String getError() {
		return app.getActualError();
	}

	@RequestMapping("/deleteProject")
	public String deleteProject(@RequestParam(value = "projectName") String projectName) {
		for (File f : new File(".").listFiles()) {
			if (f.getName().startsWith(projectName)) {
				f.delete();
			}
		}
		return "OK";
	}

	private void execute(String command, File directory) throws Exception {
		System.out.println("$ " + command);
		ProcessBuilder pb = new ProcessBuilder(command.split(" "));
		pb.directory(directory);
		pb.redirectErrorStream(true);
		pb.redirectOutput(Redirect.INHERIT);
		Process p = pb.start();
		p.waitFor();
	}

	private static boolean hasAtLeastOneReference(Repository repo) {

		for (Ref ref : repo.getAllRefs().values()) {
			if (ref.getObjectId() == null)
				continue;
			return true;
		}

		return false;
	}

	@RequestMapping("/updateConfFile")
	public String updateConfFile(@RequestParam(value = "conf") String conf,
			@RequestParam(value = "projectName") String projectName) {

		new File(projectName + ".properties").deleteOnExit();

		byte[] decodedString = Base64.getUrlDecoder().decode(conf.replace("%3D", ""));
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
