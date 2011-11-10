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

  * ColorDecoder.java
  * ---------------
  * (C) Copyright 2007, by IDRsolutions and Contributors.
  *
  *
  * --------------------------
 */
package org.jpedal.parser;

import org.jpedal.color.*;
import org.jpedal.objects.raw.ColorSpaceObject;
import org.jpedal.objects.raw.PdfObject;

import java.util.HashMap;
import java.util.Map;

public class ColorDecoder extends BaseDecoder{

    private PdfObjectCache cache;

    private boolean isPrinting;

    //used in CS to avoid miscaching
    private String csInUse,CSInUse;


    public void processToken(int commandID) {
        switch(commandID){

            case Cmd.cs :
                CS(true,parser.generateOpAsString(0, true));
                break;
            case Cmd.CS :
                CS(false,parser.generateOpAsString(0, true));
                break;

            case Cmd.rg :
                RG(true);
                break;
            case Cmd.RG :
                RG(false);
                break;
            case Cmd.SCN :
                SCN(false);
                break;
            case Cmd.scn :
                SCN(true);
                break;
            case Cmd.SC :
                SCN(false);
                break;
            case Cmd.sc :
                SCN(true);
                break;

            case Cmd.g :
                G(true);
                break;
            case Cmd.G :
                G(false);
                break;

            case Cmd.k :
                K(true);
                break;
            case Cmd.K :
                K(false);
                break;

        }
    }

    public void setCache(PdfObjectCache cache) {
        this.cache=cache;
    }

    public void setParameters(boolean isPageContent, boolean renderPage, int renderMode, int extractionMode, boolean isPrinting) {

        super.setParameters(isPageContent,renderPage, renderMode, extractionMode);

        this.isPrinting=isPrinting;

    }

    private void CS(boolean isLowerCase,String colorspaceObject) {

        //ensure color values reset
        current.resetOnColorspaceChange();

        //set flag for stroke
        boolean isStroke = !isLowerCase;

        //ensure if used for both Cs and cs simultaneously we only cache one version and do not overwrite
        boolean alreadyUsed=(!isLowerCase && colorspaceObject.equals(csInUse))||(isLowerCase && colorspaceObject.equals(CSInUse));

        if(isLowerCase)
            csInUse=colorspaceObject;
        else
            CSInUse=colorspaceObject;


        /**
         * work out colorspace
         */
        PdfObject ColorSpace=(PdfObject)cache.get(PdfObjectCache.Colorspaces,colorspaceObject);

        if(ColorSpace==null)
            ColorSpace=new ColorSpaceObject(colorspaceObject.getBytes());

        String ref=ColorSpace.getObjectRefAsString(), ref2=ref+ '-'+isLowerCase;

        GenericColorSpace newColorSpace= null;

        //(ms) 20090430 new code does not work so commented out

        //int ID=ColorSpace.getParameterConstant(PdfDictionary.ColorSpace);

        //        if(isLowerCase)
        //            System.out.println(" cs="+colorspaceObject+" "+alreadyUsed+" ref="+ref);
        //        else
        //            System.out.println(" CS="+colorspaceObject+" "+alreadyUsed+" ref="+ref);

        if(!alreadyUsed && cache.colorspacesObjects.containsKey(ref)){

            newColorSpace=(GenericColorSpace) cache.colorspacesObjects.get(ref);

            //reinitialise
            newColorSpace.reset();
        }else if(alreadyUsed && cache.colorspacesObjects.containsKey(ref2)){

            newColorSpace=(GenericColorSpace) cache.colorspacesObjects.get(ref2);

            //reinitialise
            newColorSpace.reset();
        }else{

            newColorSpace=ColorspaceFactory.getColorSpaceInstance(currentPdfFile, ColorSpace);

            newColorSpace.setPrinting(isPrinting);

            //use alternate as preference if CMYK
            //if(newColorSpace.getID()==ColorSpaces.ICC && ColorSpace.getParameterConstant(PdfDictionary.Alternate)==ColorSpaces.DeviceCMYK)
            //  newColorSpace=new DeviceCMYKColorSpace();

            //broken on calRGB so ignore at present
            //if(newColorSpace.getID()!=ColorSpaces.CalRGB)

            if((newColorSpace.getID()==ColorSpaces.ICC || newColorSpace.getID()==ColorSpaces.Separation)){
                //if(newColorSpace.getID()==ColorSpaces.Separation)

                if(!alreadyUsed){
                    cache.colorspacesObjects.put(ref, newColorSpace);
                }else
                    cache.colorspacesObjects.put(ref2, newColorSpace);

                // System.out.println("cache "+ref +" "+isLowerCase+" "+colorspaceObject);
            }

        }

        //pass in pattern arrays containing all values
        if(newColorSpace.getID()==ColorSpaces.Pattern){

            //at this point we only know it is Pattern so need to pass in WHOLE array
            newColorSpace.setPattern(cache.patterns,pageData.getMediaBoxWidth(pageNum), pageData.getMediaBoxHeight(pageNum),gs.CTM);
            newColorSpace.setGS(gs);
        }

        //track colorspace use
        cache.put(PdfObjectCache.ColorspacesUsed,new Integer(newColorSpace.getID()).intValue(),"x");

        if(isStroke)
            gs.strokeColorSpace=newColorSpace;
        else
            gs.nonstrokeColorSpace=newColorSpace;


    }



    private void G(boolean isLowerCase) {

        //ensure color values reset
        current.resetOnColorspaceChange();

        boolean isStroke=!isLowerCase;
        float[] operand=parser.getValuesAsFloat();
        int operandCount=operand.length;

        //set colour and colorspace
        if(isStroke){
            if (gs.strokeColorSpace.getID() != ColorSpaces.DeviceGray)
                gs.strokeColorSpace=new DeviceGrayColorSpace();

            gs.strokeColorSpace.setColor(operand,operandCount);

            //track colrspace use
            cache.put(PdfObjectCache.ColorspacesUsed,new Integer(gs.strokeColorSpace.getID()).intValue(),"x");

        }else{
            if (gs.nonstrokeColorSpace.getID() != ColorSpaces.DeviceGray)
                gs.nonstrokeColorSpace=new DeviceGrayColorSpace();

            gs.nonstrokeColorSpace.setColor(operand,operandCount);

            //track colorspace use
            cache.put(PdfObjectCache.ColorspacesUsed,new Integer(gs.nonstrokeColorSpace.getID()).intValue(),"x");

        }
    }

    private void K(boolean isLowerCase) {

        //ensure color values reset
        current.resetOnColorspaceChange();

        //set flag to show which color (stroke/nonstroke)
        boolean isStroke=!isLowerCase;

        float[] operand=parser.getValuesAsFloat();

        int operandCount=operand.length;


        /**allow for less than 4 values
         * (ie second mapping for device colourspace
         */
        if (operandCount > 3) {


            float[] tempValues=new float[operandCount];
            for(int ii=0;ii<operandCount;ii++)
                tempValues[operandCount-ii-1]=operand[ii];
            operand=tempValues;

            //set colour and make sure in correct colorspace
            if(isStroke){
                if (gs.strokeColorSpace.getID() != ColorSpaces.DeviceCMYK)
                    gs.strokeColorSpace=new DeviceCMYKColorSpace();

                gs.strokeColorSpace.setColor(operand,operandCount);

                //track colorspace use
                cache.put(PdfObjectCache.ColorspacesUsed,new Integer(gs.strokeColorSpace.getID()).intValue(),"x");

            }else{
                if (gs.nonstrokeColorSpace.getID() != ColorSpaces.DeviceCMYK)
                    gs.nonstrokeColorSpace=new DeviceCMYKColorSpace();

                gs.nonstrokeColorSpace.setColor(operand,operandCount);

                //track colorspace use
                cache.put(PdfObjectCache.ColorspacesUsed,new Integer(gs.nonstrokeColorSpace.getID()).intValue(),"x");

            }
        }
    }

    private void RG(boolean isLowerCase)  {

        //ensure color values reset
        current.resetOnColorspaceChange();

        //set flag to show which color (stroke/nonstroke)
        boolean isStroke=!isLowerCase;

        float[] operand=parser.getValuesAsFloat();

        int operandCount=operand.length;

        float[] tempValues=new float[operandCount];
        for(int ii=0;ii<operandCount;ii++)
            tempValues[operandCount-ii-1]=operand[ii];
        operand=tempValues;

        //set colour
        if(isStroke){
            if (gs.strokeColorSpace.getID() != ColorSpaces.DeviceRGB)
                gs.strokeColorSpace=new DeviceRGBColorSpace();

            gs.strokeColorSpace.setColor(operand,operandCount);

            //track colorspace use
            cache.put(PdfObjectCache.ColorspacesUsed, new Integer(gs.strokeColorSpace.getID()).intValue(),"x");

        }else{
            if (gs.nonstrokeColorSpace.getID() != ColorSpaces.DeviceRGB)
                gs.nonstrokeColorSpace=new DeviceRGBColorSpace();

            gs.nonstrokeColorSpace.setColor(operand,operandCount);

            //track colrspace use
            cache.put(PdfObjectCache.ColorspacesUsed,new Integer(gs.nonstrokeColorSpace.getID()).intValue(),"x");

        }
    }



    private void SCN(boolean isLowerCase)  {

        float[] values=null;

        if(isLowerCase){

            if(gs.nonstrokeColorSpace.getID()==ColorSpaces.Pattern){
                String[] vals=parser.getValuesAsString();
                gs.nonstrokeColorSpace.setColor(vals,vals.length);
            }else{
                values=parser.getValuesAsFloat();

                int operandCount=values.length;
                float[] tempValues=new float[operandCount];
                for(int ii=0;ii<operandCount;ii++)
                    tempValues[operandCount-ii-1]=values[ii];
                values=tempValues;

                //System.out.println(nonstrokeColorSpace);
                gs.nonstrokeColorSpace.setColor(values,operandCount);
            }

            //track colrspace use
            cache.put(PdfObjectCache.ColorspacesUsed,new Integer(gs.nonstrokeColorSpace.getID()).intValue(),"x");

        }else{
            if(gs.strokeColorSpace.getID()==ColorSpaces.Pattern){
                String[] vals=parser.getValuesAsString();
                gs.strokeColorSpace.setColor(vals,vals.length);
            }else{
                values=parser.getValuesAsFloat();

                int operandCount=values.length;
                float[] tempValues=new float[operandCount];
                for(int ii=0;ii<operandCount;ii++)
                    tempValues[operandCount-ii-1]=values[ii];
                values=tempValues;

                gs.strokeColorSpace.setColor(values,operandCount);
            }

            //track colrspace use
            cache.put(PdfObjectCache.ColorspacesUsed, new Integer(gs.strokeColorSpace.getID()).intValue(),"x");

        }
    }
}
