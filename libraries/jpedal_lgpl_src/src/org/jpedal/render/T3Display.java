/**
 * ===========================================
 * Java Pdf Extraction Decoding Access Library
 * ===========================================
 *
 * Project Info:  http://www.jpedal.org
 * (C) Copyright 1997-2011, IDRsolutions and Contributors.
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
  * T3Display.java
  * ---------------
 */
package org.jpedal.render;

import org.jpedal.color.PdfColor;
import org.jpedal.color.PdfPaint;
import org.jpedal.io.ObjectStore;

import java.awt.*;
import java.util.Map;

public class T3Display extends ScreenDisplay implements T3Renderer{
	
	public T3Display(int pageNumber, ObjectStore newObjectRef, boolean isPrinting) {
		super(pageNumber, newObjectRef, isPrinting);
	}

    /**create instance and set flag to show if we draw white background*/
    public T3Display(int pageNumber, boolean addBackground, int defaultSize, ObjectStore newObjectRef) {

        this.pageNumber=pageNumber;
        this.objectStoreRef = newObjectRef;
        this.addBackground=addBackground;

        setupArrays(defaultSize);
    }

    public T3Display(byte[] dvr, Map map) {
        super(dvr,map);
    }

    /**
     * used internally - please do not use
     */
    public void setOptimisedRotation(boolean value) {
        optimisedTurnCode=value;
    }


    /**
     * use by type3 fonts to differentiate images in local store
     */
    public void setType3Glyph(String pKey) {
        this.rawKey=pKey;

        isType3Font=true;

    }

        /**
     * used by type 3 glyphs to set colour
     */
    public void lockColors(PdfPaint strokePaint, PdfPaint nonstrokePaint) {

        colorsLocked=true;
        Color strokeColor=Color.white,nonstrokeColor=Color.white;

        if(!strokePaint.isPattern())
            strokeColor=(Color) strokePaint;
        strokeCol=new PdfColor(strokeColor.getRed(),strokeColor.getGreen(),strokeColor.getBlue());

        if(!nonstrokePaint.isPattern())
            nonstrokeColor=(Color) nonstrokePaint;
        fillCol=new PdfColor (nonstrokeColor.getRed(),nonstrokeColor.getGreen(),nonstrokeColor.getBlue());

    }
}
