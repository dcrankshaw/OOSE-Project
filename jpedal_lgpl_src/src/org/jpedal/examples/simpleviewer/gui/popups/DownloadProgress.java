package org.jpedal.examples.simpleviewer.gui.popups;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;

import org.jpedal.utils.LogWriter;
import org.jpedal.examples.simpleviewer.gui.SwingGUI;
import org.jpedal.io.ObjectStore;

public class DownloadProgress {
	//Load file from URL into file then open file
	File tempURLFile;

	boolean isDownloading=true;

	int progress = 0;

	SwingGUI gui;

    private String pdfUrl;

	public DownloadProgress(SwingGUI gui, String pdfUrl){

		this.gui = gui;
        this.pdfUrl = pdfUrl;

	}

    public void startDownload() {
        
        URL url;
        InputStream is;

        try {
            int fileLength = 0;
            int fileLengthPercent = 0;

            progress = 0;

            String str= "file.pdf";
            if(pdfUrl.startsWith("jar:/")) {
                is=this.getClass().getResourceAsStream(pdfUrl.substring(4));
            }else{
                url = new URL(pdfUrl);

                is = url.openStream();

                str=url.getPath().substring(url.getPath().lastIndexOf('/')+1);
                fileLength = url.openConnection().getContentLength();
                fileLengthPercent = fileLength/100;
            }
            final String filename = str;

            tempURLFile=ObjectStore.createTempFile(filename);
            
            
            FileOutputStream fos = new FileOutputStream(tempURLFile);

            // Download buffer
            byte[] buffer = new byte[4096];

            // Download the PDF document
            int read;
            int current = 0;

            while ((read = is.read(buffer)) != -1) {
                current = current + read;
                progress = current/fileLengthPercent;
                fos.write(buffer, 0, read);
            }
            fos.flush();

            // Close streams
            is.close();
            fos.close();
            progress = 100;

        } catch (Exception e) {
            LogWriter.writeLog("[PDF] Exception " + e + " opening URL "+ pdfUrl);
            e.printStackTrace();
            progress = 100;
        }

        isDownloading=false;

    }
	
	public File getFile(){
		return tempURLFile;
	}

	public boolean isDownloading() {
		return isDownloading; 
	}

	public int getProgress(){
		return progress;
	}
}
