package edu.jhu.cs.oose.biblio.model;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.*;

public class Watcher implements Runnable {
	private String dir;
	private Set<File> dirFiles;
	private Set<File> imported;
	private File t = new File("C:\\Users\\msi\\Desktop\\StudySpaceFall.pdf");
	
	public Watcher(String dirPath) {
		dir = dirPath;
		dirFiles = new HashSet<File>();
		imported = new HashSet<File>();		
		imported.add(t);
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		while (true) {
			try {
				this.watchDir();
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void watchDir() {
		this.getFiles();
		for (File f : dirFiles){
			if (imported.contains(f)){
				System.out.println("Old file.");
			}
			else{
				System.out.println("New file.");
			}
				
		}
	}

	public void getFiles() {
		String files;
		File folder = new File(dir);
		File[] listOfFiles = folder.listFiles();
		for (File f : listOfFiles )
			if (f.isFile()) {
				files = f.getName();
				if (files.endsWith(".pdf") || files.endsWith(".PDF")) {
					System.out.println(files);
					dirFiles.add(f);
				}
			}
	}
		
}
