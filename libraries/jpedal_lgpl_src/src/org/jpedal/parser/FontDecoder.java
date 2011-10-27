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
  * FontDecoder.java
  * ---------------
 */
package org.jpedal.parser;

import org.jpedal.PdfDecoder;
import org.jpedal.exception.PdfException;
import org.jpedal.exception.PdfFontException;
import org.jpedal.fonts.FontMappings;
import org.jpedal.fonts.PdfFont;
import org.jpedal.fonts.StandardFonts;
import org.jpedal.io.ErrorTracker;
import org.jpedal.io.ObjectStore;
import org.jpedal.io.PdfObjectReader;
import org.jpedal.objects.raw.PdfDictionary;
import org.jpedal.objects.raw.PdfObject;
import org.jpedal.utils.LogWriter;
import org.jpedal.utils.StringUtils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Convert font info into one of our supporting classes
 */
public class FontDecoder extends BaseDecoder {

    /**flag to show embedded fonts present*/
    private boolean hasEmbeddedFonts=false;

    /**flag to show if non-embedded CID fonts*/
    private boolean hasNonEmbeddedCIDFonts=false;

    /**list of fonts used for display*/
    private String fontsInFile;

    /**and the list of CID fonts*/
    private StringBuffer nonEmbeddedCIDFonts=new StringBuffer();

    private String baseFont="",rawFontName=null,subFont=null;
    private int origfontType;

    private PdfObject pageResources;

    //only load 1 instance of any 1 font
    private  HashMap fontsLoaded=new HashMap();

    public FontDecoder(PdfObjectReader currentPdfFile) {
        this.currentPdfFile=currentPdfFile;
    }

    public void setRes(PdfObject pageResources){

        this.pageResources=pageResources;

    }

    public PdfFont createFont(PdfObject pdfObject, String font_id, ObjectStore objectStoreStreamRef) throws PdfException {


        PdfFont currentFontData=null;
        /**
         * allow for no actual object - ie /PDFdata/baseline_screens/debug/res.pdf
         **/
        //found examples with no type set so cannot rely on it
        //int rawType=pdfObject.getParameterConstant(PdfDictionary.Type);
        //if(rawType!=PdfDictionary.Font)
        //	return null;

        baseFont="";
        rawFontName=null;
        subFont=null;

        int fontType= PdfDictionary.Unknown;
        origfontType=PdfDictionary.Unknown;

        PdfObject descendantFont=pdfObject.getDictionary(PdfDictionary.DescendantFonts);

        boolean isEmbedded= isFontEmbedded(pdfObject);
        boolean isFontBroken=true; //ensure enters once

        while(isFontBroken){ //will try to sub font if error in embedded
            isFontBroken=false;

            /**
             * handle any font remapping but not on CID fonts or Type3 and gets too messy
             **/
            if(FontMappings.fontSubstitutionTable!=null && !isEmbedded &&
                    pdfObject.getParameterConstant(PdfDictionary.Subtype)!= StandardFonts.TYPE3)
                fontType = getFontMapping(pdfObject, font_id, fontType, descendantFont);

            //get subtype if not set above
            if(fontType==PdfDictionary.Unknown){
                fontType=pdfObject.getParameterConstant(PdfDictionary.Subtype);

                /**handle CID fonts where /Subtype stored inside sub object*/
                if (fontType==StandardFonts.TYPE0) {

                    //get CID type and use in preference to Type0 on CID fonts
                    PdfObject desc=pdfObject.getDictionary(PdfDictionary.DescendantFonts);
                    fontType=desc.getParameterConstant(PdfDictionary.Subtype);

                    origfontType=fontType;

                    //track non-embedded, non-substituted CID fonts
                    if(!isEmbedded && subFont==null) {
                        hasNonEmbeddedCIDFonts=true;

                        //track list
                        if(nonEmbeddedCIDFonts.length()>0)
                            nonEmbeddedCIDFonts.append(',');
                        nonEmbeddedCIDFonts.append(baseFont);
                    }
                }
            }

            if(fontType==PdfDictionary.Unknown){

            	if(LogWriter.isOutput())
            		LogWriter.writeLog("Font type not supported");


                currentFontData=new PdfFont(currentPdfFile);
            }

            /**
             * check for OpenType fonts and reassign type
             */
            if(fontType==StandardFonts.TYPE1)
                fontType = scanForOpenType(pdfObject, currentPdfFile, fontType);

            try{
                currentFontData=FontFactory.createFont(fontType,currentPdfFile,subFont);

                /**set an alternative to Lucida*/
                if(PdfDecoder.defaultFont!=null)
                    currentFontData.setDefaultDisplayFont(PdfDecoder.defaultFont);

                //some ghostscript T3 fonts can reference Page resources
                if(fontType==StandardFonts.TYPE3)
                    currentFontData.setRes(pageResources);

                currentFontData.createFont(pdfObject, font_id,renderPage, objectStoreStreamRef, fontsLoaded);

                //save raw version
                currentFontData.setRawFontName(rawFontName);

                //fix for odd file
                if(fontType==StandardFonts.TYPE1 && currentFontData.is1C() && pdfObject.getInt(PdfDictionary.FirstChar)==32 &&
                        pdfObject.getInt(PdfDictionary.FirstChar)==pdfObject.getInt(PdfDictionary.LastChar)){

                    if(isEmbedded){
                        isFontBroken=true;
                        isEmbedded=false;
                    }else{
                        currentFontData.isFontEmbedded=false;
                    }
                }

                //see if we failed and loop round to substitute
                if(!currentFontData.isFontEmbedded && isEmbedded){
                    isFontBroken=true;
                    isEmbedded=false;
                }

            }catch(Exception e){

            	if(LogWriter.isOutput())
            		LogWriter.writeLog("[PDF] Problem "+e+" reading Font  type "+ StandardFonts.getFontypeAsString(fontType));

                errorTracker.addPageFailureMessage("Problem " + e + " reading Font type " + StandardFonts.getFontypeAsString(fontType));
            }
        }

        /**
         * add line giving font info so we can display or user access
         */
        setDetails(font_id, currentFontData, fontType, descendantFont);

        return currentFontData;
    }

    private void setDetails(String font_id, PdfFont currentFontData, int fontType, PdfObject descendantFont) {
        String name=currentFontData.getFontName();

        //deal with odd chars
        if(name.indexOf('#')!=-1)
            name= StringUtils.convertHexChars(name);

        String details;
        if(currentFontData.isFontSubstituted()){
            details=font_id+"  "+name+"  "+ StandardFonts.getFontypeAsString(origfontType)+"  Substituted ("+subFont+ ' ' +StandardFonts.getFontypeAsString(fontType)+ ')';
        }else if(currentFontData.isFontEmbedded){
            hasEmbeddedFonts=true;
            if(currentFontData.is1C() && descendantFont==null)
                details=font_id+"  "+name+" Type1C  Embedded";
            else
                details=font_id+"  "+name+"  "+StandardFonts.getFontypeAsString(fontType)+"  Embedded";
        }else
            details=font_id+"  "+name+"  "+StandardFonts.getFontypeAsString(fontType);

        if(fontsInFile==null)
            fontsInFile=details;
        else
            fontsInFile=details+'\n'+fontsInFile;
    }

    private static int scanForOpenType(PdfObject pdfObject, PdfObjectReader currentPdfFile, int fontType) {
        PdfObject FontDescriptor=pdfObject.getDictionary(PdfDictionary.FontDescriptor);
        if(FontDescriptor!=null){

            PdfObject FontFile3=FontDescriptor.getDictionary(PdfDictionary.FontFile3);
            if(FontFile3!=null){ //must be present for OTTF font

                //get data
                byte[] stream=currentPdfFile.readStream(FontFile3,true,true,false, false,false, FontFile3.getCacheName(currentPdfFile.getObjectReader()));

                //check first 4 bytes
                if(stream!=null && stream.length>3 && stream[0]==79 && stream[1]==84 && stream[2]==84 && stream[3]==79)
                    fontType= StandardFonts.TRUETYPE; //put it through our TT handler which also does OT

            }
        }
        return fontType;
    }

    private int getFontMapping(PdfObject pdfObject, String font_id, int fontType, PdfObject descendantFont) throws PdfException {
        String rawFont;

        if(descendantFont==null)
            rawFont=pdfObject.getName(PdfDictionary.BaseFont);
        else
            rawFont=descendantFont.getName(PdfDictionary.BaseFont);

        if(rawFont==null)
            rawFont=pdfObject.getName(PdfDictionary.Name);

        if(rawFont==null)
            rawFont=font_id;


        String newSubtype = getFontSub(rawFont);

        if(newSubtype!=null && descendantFont==null){

            //convert String to correct int value
            if (newSubtype.equals("/Type1")|| newSubtype.equals("/Type1C")|| newSubtype.equals("/MMType1"))
                fontType= StandardFonts.TYPE1;
            else if (newSubtype.equals("/TrueType"))
                fontType=StandardFonts.TRUETYPE;
            else if (newSubtype.equals("/Type3"))
                fontType=StandardFonts.TYPE3;
            else
                throw new RuntimeException("Unknown font type "+newSubtype+" used for font substitution");

            origfontType=pdfObject.getParameterConstant(PdfDictionary.Subtype);

        }else if(PdfDecoder.enforceFontSubstitution){

            if(LogWriter.isOutput())
                LogWriter.writeLog("baseFont="+baseFont+" fonts added= "+FontMappings.fontSubstitutionTable);

            throw new PdfFontException("No substitute Font found for font="+baseFont+ '<');
        }
        return fontType;
    }

    public String getFontSub(String rawFont) throws PdfException {
        if(rawFont.indexOf('#')!=-1)
            rawFont= StringUtils.convertHexChars(rawFont);

        //save in case we need later
        rawFontName=rawFont;

        baseFont=(rawFont).toLowerCase();

        //strip any postscript
        int pointer=baseFont.indexOf('+');
        if(pointer==6)
            baseFont=baseFont.substring(7);

        String testFont=baseFont, nextSubType;

        subFont=(String) FontMappings.fontSubstitutionLocation.get(testFont);

        String newSubtype=(String)FontMappings.fontSubstitutionTable.get(testFont);

        //do not replace on MAC as default does not have certain values we need
        if(PdfDecoder.isRunningOnMac && testFont.equals("zapfdingbats"))
            testFont="No match found";

        //check aliases
        if(newSubtype==null){
            //check for mapping
            HashMap fontsMapped=new HashMap();
            String nextFont;
            while(true){
                nextFont=(String) FontMappings.fontSubstitutionAliasTable.get(testFont);

                if(nextFont==null)
                    break;

                testFont=nextFont;

                nextSubType=(String)FontMappings.fontSubstitutionTable.get(testFont);

                if(nextSubType!=null){
                    newSubtype=nextSubType;
                    subFont=(String) FontMappings.fontSubstitutionLocation.get(testFont);
                }

                if(fontsMapped.containsKey(testFont)){
                	//use string buffer and stringbuilder does not exist in java ME
                    StringBuffer errorMessage = new StringBuffer("[PDF] Circular font mapping for fonts");
                    Iterator i=fontsMapped.keySet().iterator();
                    while(i.hasNext()){
                        errorMessage.append(' ');
                        errorMessage.append(i.next());
                    }
                    throw new PdfException(errorMessage.toString());
                }
                fontsMapped.put(nextFont,"x");
            }
        }
        return newSubtype;
    }

    /**
     * check for embedded font file to see if font embedded
     */
    private static boolean isFontEmbedded(PdfObject pdfObject) {

        //ensure we are looking in DescendantFonts object if CID
        int fontType=pdfObject.getParameterConstant(PdfDictionary.Subtype);
        if (fontType==StandardFonts.TYPE0)
            pdfObject=pdfObject.getDictionary(PdfDictionary.DescendantFonts);


        PdfObject descFontObj=pdfObject.getDictionary(PdfDictionary.FontDescriptor);


        if(descFontObj==null)
            return false;
        else
            return descFontObj.hasStream();
    }


    public String getnonEmbeddedCIDFonts() {
        return nonEmbeddedCIDFonts.toString();
    }

    public String getFontsInFile() {
        return fontsInFile;
    }

    public void resetfontsInFile() {
        fontsInFile="";
    }

    public boolean hasEmbeddedFonts() {
        return hasEmbeddedFonts;
    }

    public boolean hasNonEmbeddedCIDFonts() {
        return hasNonEmbeddedCIDFonts;
    }

    public String getMapFont() {
        return subFont;
    }
}
