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
* RandomAccessDataBuffer.java
* ---------------
*/

package org.jpedal.io;

import java.io.FileInputStream;
import java.io.IOException;
// <start-me>
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
// <end-me>

public class RandomAccessFCTest implements RandomAccessBuffer {

    //private byte[] data;
    private long pointer;

    private int length=0;

    // <start-me>
    private FileChannel fc;

    private MappedByteBuffer mb;
    // <end-me>

     /**/
    public RandomAccessFCTest(FileInputStream inFile)
    {

        try{

            length=inFile.available();
// <start-me>
            fc= (inFile).getChannel();

            mb = fc.map( FileChannel.MapMode.READ_ONLY, 0L, fc.size( ) );
// <end-me>

        }catch(Exception e){
            e.printStackTrace();
        }
    }   /**/


    public long getFilePointer() throws IOException {
        return pointer;
    }

    public void seek(long pos) throws IOException {
        if ( checkPos(pos) ) {
            this.pointer = pos;
        } else {
            throw new IOException("Position out of bounds");
        }
    }

    public void close() throws IOException {
// <start-me>
        if(fc !=null){

            fc.close();
            fc =null;
        }

        if(mb !=null){

            //mb.();
            mb =null;
        }
// <end-me>

        this.pointer = -1;

    }

     /**/public void finalize(){

        try {
            super.finalize();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }

        //ensure removal actual file
        try {
            close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    } /**/

    public long length() throws IOException {

    	// <start-me>
        if (mb !=null) {
            return length;
        } else
        	// <end-me>
        {
            throw new IOException("Data buffer not initialized.");
        }
    }

    public int read() throws IOException {
    	// <start-me>
        if (checkPos(this.pointer)) {

            mb.position((int)pointer);

            pointer++;

            return mb.get();
        } else 
        // <end-me>
        	{
            return -1;
        }
    }

    private int peek() throws IOException {
    	// <start-me>
        if (checkPos(this.pointer)) {

            mb.position((int)pointer);

            return mb.get();
        } else 
        // <end-me>
        	{
            return -1;
        }
    }

    /**
     * return next line (returns null if no line)
     */
    public String readLine() throws IOException {

        if (this.pointer >= this.length - 1) {
            return null;
        } else {

            StringBuffer buf = new StringBuffer();
            int c;
            while ((c = read()) >= 0) {
                if ((c == 10) || (c == 13)) {
                    if (((peek() == 10) || (peek() == 13)) && (peek() != c))
                        read();
                    break;
                }
                buf.append((char) c);
            }
            return buf.toString();
        }
    }

    public int read(byte[] b) throws IOException {
// <start-me>
        if (mb ==null) throw new IOException("Data buffer not initialized.");
        // <end-me>

        if (pointer<0 || pointer>=length)
            return -1;

        int length=this.length-(int)pointer;
        if(length>b.length)
            length=b.length;

        // <start-me>
        for (int i=0; i<length; i++) {

            mb.position((int)pointer);

            pointer++;
            b[i] = mb.get();

        }
        // <end-me>
        return length;
    }

    private static int b2i(byte b) {
        if (b>=0) return b;
        return 256+b;
    }

    private boolean checkPos(long pos) throws IOException {
        return ( (pos>=0) && (pos<length()) );
    }

    /* returns the byte data*/
    public byte[] getPdfBuffer(){

        byte[] bytes=new byte[length];

        // <start-me>
        mb.position(0);
        mb.get(bytes);
// <end-me>
        
        return bytes;
    }
}