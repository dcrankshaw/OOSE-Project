/**
 * ===========================================
 * Java Pdf Extraction Decoding Access Library
 * ===========================================
 *
 * Project Info:  http://www.jpedal.org
 * (C) Copyright 1997-2008, IDRsolutions and Contributors.
 *
 * 	This file is part of JPedal
 *
     This library is free software; you can redistribute it and/or
    modify it under the terms of the GNU Lesser General Public
    License as published by the Free Software Foundation; either
    version 2.1 of the License, or (at your option) any later version.

    This library is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
    Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public
    License along with this library; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA


  *
  * ---------------
  * PdfReader.java
  * ---------------
 */
package org.jpedal.io;

import org.jpedal.exception.PdfException;
import org.jpedal.objects.Javascript;
import org.jpedal.objects.raw.FDFObject;
import org.jpedal.objects.raw.FormObject;
import org.jpedal.objects.raw.PdfDictionary;
import org.jpedal.objects.raw.PdfObject;
import org.jpedal.utils.LogWriter;
import org.jpedal.utils.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.security.PrivateKey;
import java.security.cert.Certificate;

/**
 *
 */
public class PdfReader implements PdfObjectReader, Serializable {

    private PdfFileReader objectReader=new PdfFileReader();

    /**file length*/
    private long eof =0;

    private String tempFileName=null;

    /**names lookup table*/
    private NameLookup nameLookup=null;

    public PdfReader() {}

    /**
     * set password as well
     * @param password
     */
    public PdfReader(String password) {
        super();

        if(password==null)
            password="";

        objectReader.setPassword(password);
    }

    public PdfReader(Certificate certificate, PrivateKey key) {

        objectReader.setCertificate(certificate,key);

    }

    /**
     * close the file
     */
    final public void closePdfFile()
    {
        try
        {
            objectReader.closeFile();


            //ensure temp file deleted
            if(tempFileName!=null){
                File fileToDelete=new File(tempFileName);
                fileToDelete.delete();
                tempFileName=null;
            }
        }catch( Exception e ){
        	if(LogWriter.isOutput())
        		LogWriter.writeLog( "Exception " + e + " closing file" );
        }

    }

    /**
     * allow user to access SOME PDF objects
     * currently PdfDictionary.Encryption
     */
    public PdfObject getPDFObject(int key) {

        if(key==PdfDictionary.Encrypt){
            return objectReader.encyptionObj;
        }else
            throw new RuntimeException("Access to "+key+" not supported");
    }

    public PdfFileReader getObjectReader() {
        return objectReader;
    }

    /**
     * convert name into object ref
     */
    public String convertNameToRef(String value) {

        //see if decoded
        if(nameLookup==null)
            return null;
        else
            return (String)nameLookup.get(value);

    }




///////////////////////////////////////////////////////////////////////////

    /**
     * read FDF
     */
    final public PdfObject readFDF() throws PdfException{


        PdfObject fdfObj;

        try{

            byte[] fileData=objectReader.readFDFData();

            fdfObj=new FDFObject("1 0 R");

            //find /FDF key
            int ii=0;
            while(ii<eof){
                if(fileData[ii]=='/' && fileData[ii+1]=='F'
                        && fileData[ii+2]=='D' && fileData[ii+3]=='F')
                    break;

                ii++;
            }

            ii=ii+4;

            //move beyond <<
            while(ii<eof){
                if(fileData[ii]=='<' && fileData[ii+1]=='<')
                    break;

                ii++;
            }
            ii=ii+2;
            ObjectDecoder objectDecoder=new ObjectDecoder(this.objectReader);
            objectDecoder.readDictionaryAsObject(fdfObj, ii, fileData);

        } catch (Exception e) {
            try {
                objectReader.closeFile();
            } catch (IOException e1) {
            	if(LogWriter.isOutput())
            		LogWriter.writeLog("Exception "+e+" closing file");
            }

            throw new PdfException("Exception " + e + " reading trailer");
        }

        return fdfObj;
    }





    /**
     * read any names into names lookup
     */
    public void readNames(PdfObject nameObject, Javascript javascript, boolean isKid){

        nameLookup=new NameLookup(this.objectReader);
        nameLookup.readNames(nameObject, javascript, isKid);
    }

    public void dispose(){

        //this.objData=null;
        //this.lastRef=null;

        nameLookup=null;

        //this.fields=null;

        if(objectReader!=null)
            objectReader.dispose();
        objectReader=null;

    }

    /**
     * open pdf file<br> Only files allowed (not http)
     * so we can handle Random Access of pdf
     */
    final public void openPdfFile( InputStream in) throws PdfException
    {

        try
        {

            //use byte[] directly if small otherwise use Memory Map
            RandomAccessBuffer pdf_datafile = new RandomAccessMemoryMapBuffer(in );

            objectReader.init(pdf_datafile);

            this.eof = pdf_datafile.length();
            //pdf_datafile = new RandomAccessFile( filename, "r" );

        }catch( Exception e ){
        	
        	if(LogWriter.isOutput())
        		LogWriter.writeLog( "Exception " + e + " accessing file" );
        	
            throw new PdfException( "Exception " + e + " accessing file" );
        }

    }

    /**
     * open pdf file<br> Only files allowed (not http)
     * so we can handle Random Access of pdf
     */
    final public void openPdfFile( String filename ) throws PdfException
    {


        RandomAccessBuffer pdf_datafile=null;

        //isFDF=filename.toLowerCase().endsWith(".fdf");

        try
        {

            pdf_datafile = new RandomAccessFileBuffer( filename, "r" );
            //pdf_datafile = new RandomAccessFCTest( new FileInputStream(filename));

            objectReader.init(pdf_datafile);

            this.eof = pdf_datafile.length();

        }catch( Exception e ){
        	if(LogWriter.isOutput())
        		LogWriter.writeLog( "Exception " + e + " accessing file" );
        	
            throw new PdfException( "Exception " + e + " accessing file" );
        }

    }

    /**
     * open pdf file using a byte stream - By default files under 16384 bytes are cached to disk
     * but this can be altered by setting PdfFileReader.alwaysCacheInMemory to a maximimum size or -1 (always keep in memory)
     */
    final public void openPdfFile( byte[] data ) throws PdfException
    {

        RandomAccessBuffer pdf_datafile=null;

        try
        {
            //use byte[] directly if small otherwise use Memory Map
            if(PdfFileReader.alwaysCacheInMemory ==-1 || data.length<PdfFileReader.alwaysCacheInMemory)
                pdf_datafile = new RandomAccessDataBuffer( data );
            else{ //cache as file and access via RandomAccess

                //pdf_datafile = new RandomAccessMemoryMapBuffer( data ); old version very slow

                try {

                    File file=File.createTempFile("page",".bin", new File(ObjectStore.temp_dir));
                    tempFileName=file.getAbsolutePath();

                    //file.deleteOnExit();

                    java.io.FileOutputStream a =new java.io.FileOutputStream(file);

                    a.write(data);
                    a.flush();
                    a.close();

                    pdf_datafile = new RandomAccessFileBuffer( tempFileName,"r");
                } catch (Exception e) {
                    e.printStackTrace();
                    //LogWriter.writeLog("Unable to save jpeg " + name);

                }
            }

            objectReader.init(pdf_datafile);

            eof = pdf_datafile.length();
            //pdf_datafile = new RandomAccessFile( filename, "r" );

        }catch( Exception e ){

        	if(LogWriter.isOutput())
        		LogWriter.writeLog( "Exception " + e + " accessing file" );
        	
            throw new PdfException( "Exception " + e + " accessing file" );
        }
    }



    /**handle onto JS object*/
    private Javascript javascript;

    /**pass in Javascript object from JPedal*/
    public void setJavaScriptObject(Javascript javascript) {
        this.javascript=javascript;
    }

    public void checkResolved(PdfObject pdfObject) {
        ObjectDecoder objectDecoder=new ObjectDecoder(this.objectReader);
        objectDecoder.checkResolved(pdfObject);
    }


    public byte[] readStream(PdfObject obj, boolean cacheValue, boolean decompress, boolean keepRaw, boolean isMetaData, boolean isCompressedStream, String cacheFile) {

        return this.objectReader.readStream(obj, cacheValue, decompress, keepRaw, isMetaData, isCompressedStream, cacheFile);
    }

    public void readObject(PdfObject pdfObject) {
        objectReader.readObject(pdfObject);
    }


}