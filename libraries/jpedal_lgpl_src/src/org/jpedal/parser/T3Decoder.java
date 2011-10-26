/**
 * ===========================================
 * Java Pdf Extraction Decoding Access Library
 * ===========================================
 *
 * Project Info:  http://www.jpedal.org
 *
 * (C) Copyright 2007, IDRsolutions and Contributors.
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

  * T3Decoder.java
  * ---------------
  * (C) Copyright 2011, by IDRsolutions and Contributors.
  *
  *
  * --------------------------
 */
package org.jpedal.parser;

import org.jpedal.color.ColorspaceFactory;
import org.jpedal.color.GenericColorSpace;
import org.jpedal.color.PdfPaint;
import org.jpedal.objects.GraphicsState;
import org.jpedal.objects.raw.PdfDictionary;
import org.jpedal.objects.raw.PdfObject;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class T3Decoder extends BaseDecoder{

	/**max width of type3 font*/
    int T3maxWidth;
    int T3maxHeight;

    //public void setParameters(boolean isPageContent, boolean renderPage, int renderMode, int extractionMode, boolean isPrinting) {

    //    super.setParameters(isPageContent,renderPage, renderMode, extractionMode);

    //}
    
    /**shows if t3 glyph uses internal colour or current colour*/
    boolean ignoreColors=false;

	
	private void d1(float urX,float llX,float wX,float urY,float llY,float wY) {

	    //flag to show we use text colour or colour in stream
	    ignoreColors=true;

	    /**/
	    //not fully implemented
	    //float urY = Float.parseFloat(generateOpAsString(0,characterStream));
	    //float urX = Float.parseFloat(generateOpAsString(1,characterStream));
	    //float llY = Float.parseFloat(generateOpAsString(2,characterStream));
	    //float llX = Float.parseFloat(generateOpAsString(3,characterStream));
	    //float wY = Float.parseFloat(generateOpAsString(4,characterStream));
	    //float wX = Float.parseFloat(generateOpAsString(5,characterStream));
	    /***/

	    //this.minX=(int)llX;
	    //this.minY=(int)llY;

	    //currentGraphicsState = new GraphicsState(0,0);/*remove values on contrutor%%*/

	    //setup image to draw on
	    //current.init((int)(wX),(int)(urY-llY+1));

	    //wH=urY;
	    //wW=llX;

	    T3maxWidth=(int)wX;
	    if(wX==0)
	        T3maxWidth=(int)(llX-urX);
	    else
	        T3maxWidth=(int)wX; //Float.parseFloat(generateOpAsString(5,characterStream));

	    T3maxHeight=(int)wY;
	    if(wY==0)
	        T3maxHeight=(int)(urY-llY);
	    else
	        T3maxHeight=(int)wY; //Float.parseFloat(generateOpAsString(5,characterStream));

	    /***/
	}
	////////////////////////////////////////////////////////////////////////
	private void d0(int w,int y) {

	    //flag to show we use text colour or colour in stream
	    ignoreColors=false;

	    //float glyphX = Float.parseFloat((String) operand.elementAt(0));
	    T3maxWidth=w;
	    T3maxHeight=y;

	    //setup image to draw on
	    //current.init((int)glyphX,(int)glyphY);

	}
	public void processToken(int commandID) {
		switch(commandID){

	        case Cmd.d0 :
	            d0((int) parser.parseFloat(0),(int) parser.parseFloat(1));
	            break;
	
	        case Cmd.d1 :
	            d1(parser.parseFloat(1),parser.parseFloat(3),parser.parseFloat(5),
	            		parser.parseFloat(0),parser.parseFloat(2),parser.parseFloat(4));
	            break;
	    }
		
	}

}
