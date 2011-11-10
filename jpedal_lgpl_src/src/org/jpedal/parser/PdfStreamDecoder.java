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
  * PdfStreamDecoder.java
  * ---------------
 */
package org.jpedal.parser;

import org.jpedal.PdfDecoder;
import org.jpedal.color.*;
import org.jpedal.constants.PDFImageProcessing;
import org.jpedal.constants.PageInfo;
import org.jpedal.exception.PdfException;
import org.jpedal.external.*;
import org.jpedal.fonts.PdfFont;
import org.jpedal.fonts.StandardFonts;
import org.jpedal.fonts.glyph.T3Size;
import org.jpedal.images.SamplingFactory;
import org.jpedal.io.*;
import org.jpedal.objects.*;
import org.jpedal.objects.layers.PdfLayerList;
import org.jpedal.objects.raw.*;

import org.jpedal.render.*;
import org.jpedal.utils.LogWriter;
import org.jpedal.utils.Matrix;
import org.jpedal.utils.repositories.Vector_Int;
import org.jpedal.utils.repositories.Vector_Rectangle;

import java.awt.*;
import java.awt.geom.Area;
import java.awt.image.*;
import java.util.*;

/**
 * Contains the code which 'parses' the commands in
 * the stream and extracts the data (images and text).
 * Users should not need to call it.
 */
public class PdfStreamDecoder extends BaseDecoder{

    protected GraphicsState newGS=null;

    protected byte[] pageStream=null;

    protected boolean getSamplingOnly=false;

    private boolean isTTHintingRequired=false;

    private Vector_Int textDirections=new Vector_Int();

    private Vector_Rectangle textAreas=new Vector_Rectangle();

    /**shows if t3 glyph uses internal colour or current colour*/
    public boolean ignoreColors=false;

    //trap for recursive loop of xform calling itself
    int lastDataPointer=-1;
    
    private T3Decoder t3Decoder=null;

    /**flag to show if we REMOVE shapes*/
    private boolean removeRenderImages=false;

    /**flags to show we need colour data as well*/
    private boolean textColorExtracted=false,colorExtracted=false;

    /**flag to show text is being extracted*/
    private boolean textExtracted=true;

    /**flag to show content is being rendered*/
    private boolean renderText=false;

    /**
     * if forms flattened, different calculation needed
     */
    private boolean isFlattenedForm=false;
    private float flattenX=0, flattenY=0;

    /**list of images used for display*/
    private String imagesInFile=null;

    //set threshold - value indicates several possible values
    public static float currentThreshold=0.595f;

    private boolean flattenXFormToImage=false;

    private boolean requestTimeout =false;

    private int timeoutInterval=-1;

    protected ImageHandler customImageHandler=null;

    private FontDecoder fontDecoder;


    /**interactive display*/
    private StatusBar statusBar=null;

    private boolean markedContentExtracted=false;

    /**store text data and can be passed out to other classes*/
    private PdfData pdfData = new PdfData();

    /**store image data extracted from pdf*/
    private PdfImageData pdfImages=new PdfImageData();

    /**used to debug*/
    protected static String indent="";

    /**show if possible error in stream data*/
    protected boolean isDataValid=true;

    /**used to store font information from pdf and font functionality*/
    private PdfFont currentFontData;

    /**flag to show we use hi-res images to draw onscreen*/
    protected boolean useHiResImageForDisplay=false;

    protected ObjectStore objectStoreStreamRef;

    private String formName="";

    protected boolean isType3Font;

    public static boolean useTextPrintingForNonEmbeddedFonts=false;

    static{
        SamplingFactory.setDownsampleMode(null);
    }
    
    public PdfStreamDecoder(PdfObjectReader currentPdfFile,boolean useHires){

        this.useHiResImageForDisplay=useHires;

        init(currentPdfFile);
    }

    public PdfStreamDecoder(PdfObjectReader currentPdfFile){

        init(currentPdfFile);

    }

    public PdfStreamDecoder(PdfObjectReader currentPdfFile, PdfObject pageResources){

        init(currentPdfFile);

        fontDecoder.setRes(pageResources);

    }

    /**
     * create new StreamDecoder to create screen display with hires images
     */
    public PdfStreamDecoder(PdfObjectReader currentPdfFile, boolean useHiResImageForDisplay, PdfLayerList layers, PdfObject pageResources) {

        this.layers=layers;


        init(currentPdfFile);

        fontDecoder.setRes(pageResources);

    }

    private void init(PdfObjectReader currentPdfFile) {

        cache=new PdfObjectCache();
        gs=new GraphicsState();
        layerDecoder=new LayerDecoder();
        errorTracker=new ErrorTracker();
        pageData = new PdfPageData();

        StandardFonts.checkLoaded(StandardFonts.STD);

        this.currentPdfFile=currentPdfFile;
        fontDecoder=new FontDecoder(currentPdfFile);

        fontDecoder.setHandlerValue(Options.ErrorTracker,errorTracker);
    }


    /**
     *
     *  objects off the page, stitch into a stream and
     * decode and put into our data object. Could be altered
     * if you just want to read the stream
     * @param pageStream
     * @throws PdfException
     */
    public final T3Size decodePageContent(GraphicsState newGS, byte[] pageStream) throws PdfException{

        this.newGS=newGS;
        this.pageStream=pageStream;

        return decodePageContent(null);
    }

    /**
     *
     *  objects off the page, stitch into a stream and
     * decode and put into our data object. Could be altered
     * if you just want to read the stream
     * @param pdfObject
     * @throws PdfException
     */
    public final T3Size decodePageContent(PdfObject pdfObject) throws PdfException{

        try{

            //check switched off
            imagesProcessedFully=true;

            //reset count
            imageCount=0;

            isTimeout=false;

            //reset count
            imagesInFile=null; //also reset here as good point as syncs with font code

            if(!this.renderDirectly && statusBar!=null)
                statusBar.percentageDone=0;

            if(newGS!=null)
                gs = newGS;
            else
                gs = new GraphicsState(0,0);

            //save for later
            if (renderPage){

                /**
                 * check setup and throw exception if null
                 */
                if(current==null)
                    throw new PdfException("DynamicVectorRenderer not setup PdfStreamDecoder setStore(...) should be called");

                current.drawClip(gs,defaultClip, false) ;
            }

            //get the binary data from the file
            byte[] b_data = null;

            byte[][] pageContents= null;
            if(pdfObject !=null){
                pageContents= pdfObject.getKeyArray(PdfDictionary.Contents);
                isDataValid= pdfObject.streamMayBeCorrupt();
            }

            //get any page grouping obj
            if(pdfObject==null)
                cache.pageGroupingObj=null;
            else
                cache.pageGroupingObj=pdfObject.getDictionary(PdfDictionary.Group);

            if(pdfObject !=null && pageContents==null)
                b_data =currentPdfFile.readStream(pdfObject,true,true,false, false,false, pdfObject.getCacheName(currentPdfFile.getObjectReader()));
            else if(pageStream!=null)
                b_data =pageStream;
            else
                b_data =currentPdfFile.getObjectReader().readPageIntoStream(pdfObject);

            //trap for recursive loop of xform calling itself
            lastDataPointer=-1;

            //if page data found, turn it into a set of commands
            //and decode the stream of commands
            if (b_data!=null && b_data.length > 0)
                decodeStreamIntoObjects(b_data, false);

            //flush fonts
            if(!isType3Font)
                cache.resetFonts();

            T3Size t3=new T3Size();
            if(t3Decoder!=null){
	            t3.x = t3Decoder.T3maxWidth;
	            t3.y = t3Decoder.T3maxHeight;
	            ignoreColors=t3Decoder.ignoreColors;
	            t3Decoder=null;
            }

            return t3;


        }catch(Error err){


            errorTracker.addPageFailureMessage("Problem decoding page " + err);

        }

        return null;
    }

    /**
     * routine to decode an XForm stream
     */
    public void drawFlattenedForm(PdfObject form) throws  PdfException {

        isFlattenedForm=true;

        //check if this form should be displayed
        boolean[] characteristic = ((FormObject)form).getCharacteristics();
        if (characteristic[0] || characteristic[1] || characteristic[5] ||
                (form.getBoolean(PdfDictionary.Open)==false &&
                        form.getParameterConstant(PdfDictionary.Subtype)==PdfDictionary.Popup)){
            //this form should be hidden
            return;
        }

        PdfObject imgObj = null;
        PdfObject APobjN = form.getDictionary(PdfDictionary.AP).getDictionary(PdfDictionary.N);
        String defaultState = form.getName(PdfDictionary.AS);
        if (defaultState != null && defaultState.equals(((FormObject)form).getNormalOnState())) {
            //use the selected appearance stream
            if(APobjN.getDictionary(PdfDictionary.On) !=null){
                imgObj = APobjN.getDictionary(PdfDictionary.On);
            }else {
                Map otherValues=APobjN.getOtherDictionaries();
                if(otherValues!=null && !otherValues.isEmpty()){
                    Iterator keys=otherValues.keySet().iterator();
                    PdfObject val;
                    String key;
                    while(keys.hasNext()){
                        key=(String)keys.next();
                        val=(PdfObject)otherValues.get(key);
                        imgObj = val;
                    }
                }
            }
        }else {
            //use the normal appearance Stream
            if(APobjN!=null || form.getDictionary(PdfDictionary.MK).getDictionary(PdfDictionary.I) !=null){

                //if we have a root stream then it is the off value
                //check in order of N Off, MK I, then N
                //as N Off overrides others and MK I is in preference to N
                if(APobjN!=null && APobjN.getDictionary(PdfDictionary.Off) !=null){
                    imgObj = APobjN.getDictionary(PdfDictionary.Off);
                }else if(form.getDictionary(PdfDictionary.MK).getDictionary(PdfDictionary.I) !=null
                        && form.getDictionary(PdfDictionary.MK).getDictionary(PdfDictionary.IF)==null){
                    //@mark - look here for MK IF
                    //if we have an IF inside the MK then use the MK I as some files shown that this value is there
                    //only when the MK I value is not as important as the AP N.
                    imgObj = form.getDictionary(PdfDictionary.MK).getDictionary(PdfDictionary.I);

                }else if(APobjN!=null && APobjN.getDecodedStream()!=null){
                    imgObj = APobjN;
                }
            }
        }

        if(imgObj==null)
            return;

        currentPdfFile.checkResolved(imgObj);
        byte[] formData=imgObj.getDecodedStream(); //get from the AP
        //debug code for mark, for the flattern case 10295
//        System.out.println("ref="+form.getObjectRefAsString()+" stream="+new String(formData));

        //might be needed to pick up fonts
        PdfObject resources = imgObj.getDictionary(PdfDictionary.Resources);
        currentPdfFile.checkResolved(resources);
        readResources(resources,false);

        /**
         * see if bounding box and set
         */
        float[] BBox=form.getFloatArray(PdfDictionary.Rect);

        //if we flatten form objects with XForms, we need to use diff calculation
        if(isFlattenedForm){
            flattenX=BBox[0];
            flattenY=BBox[1];
        }

        Area clip=null;
        boolean clipChanged=false;

        //please dont delete through merge this fixes most of the flatten form positionsing.
        float[] matrix=imgObj.getFloatArray(PdfDictionary.Matrix);

        //we need to factor in this to calculations
        int pageRotation=pageData.getRotation(pageNum);

        float x = BBox[0],y = BBox[1];
        //check for null and then recalculate insets
        if(matrix!=null){
            switch(pageRotation){
                case 90:
                    x = BBox[2];
                    break;
                default:
                    x = BBox[0]+matrix[4];
                    break;
            }
            y = BBox[1]+matrix[5];
        }

        //set gs.CTM to form coords (probably {1,0,0}{0,1,0}{x,y,1} at a guess
        switch(pageRotation){

            case 90:
                gs.CTM = new float[][]{{0,1,0},{-1,0,0},{x,y,1}};
                break;

            default:
                gs.CTM = new float[][]{{1,0,0},{0,1,0},{x,y,1}};
                break;
        }

        //set clip to match bounds on form
        clip=null;

        Area newClip=new Area(new Rectangle((int)BBox[0],(int)BBox[1],(int)BBox[2],(int)BBox[3]));
        gs.updateClip(new Area(newClip));

        current.drawClip(gs, defaultClip,false) ;
        clipChanged=true;


        /**decode the stream*/
        setBooleanValue(IsFlattenedForm,isFlattenedForm);
        decodeStreamIntoObjects(formData, false);

        /**
         * we need to reset clip otherwise items drawn afterwards
         * like forms data in image or print will not appear.
         */
        gs.updateClip(null);
        current.drawClip(gs, null,true) ;


    }

    public void setObjectValue(int key, Object  obj){

        switch(key){

            case ValueTypes.Name:
                setName((String) obj);
                break;

            case ValueTypes.DynamicVectorRenderer:
                current=(DynamicVectorRenderer)obj;
                //flag OCR used
                boolean isOCR=(renderMode & PdfDecoder.OCR_PDF)==PdfDecoder.OCR_PDF;
                if(isOCR && current!=null)
                    current.setOCR(true);
                break;

            case ValueTypes.PDFData:
                pageData=(PdfPageData)obj;
                //flag if colour info being extracted
                if(textColorExtracted)
                    pdfData.enableTextColorDataExtraction();

                break;

            /**
             * pass in status bar object
             *
             */
            case ValueTypes.StatusBar:
                this.statusBar=(StatusBar)obj;
                break;

            case ValueTypes.PdfLayerList:
                this.layers=(PdfLayerList) obj;
                break;


            case ValueTypes.ImageHandler:
                this.customImageHandler = (ImageHandler)obj;
                if(customImageHandler!=null && current!=null)
                    current.setCustomImageHandler(this.customImageHandler);
                break;

            /**
             * setup stream decoder to render directly to g2
             * (used by image extraction)
             */
            case ValueTypes.DirectRendering:

                this.renderDirectly=true;
                Graphics2D g2 = (Graphics2D)obj;
                this.defaultClip=g2.getClip();

                break;

            /** should be called after constructor or other methods may not work
             * <p>Also initialises DynamicVectorRenderer*/
            case ValueTypes.ObjectStore:
                objectStoreStreamRef = (ObjectStore)obj;

                current=new ScreenDisplay(this.pageNum,objectStoreStreamRef,false);
                current.setHiResImageForDisplayMode(useHiResImageForDisplay);

                if(customImageHandler!=null && current!=null)
                    current.setCustomImageHandler(customImageHandler);

                break;

            default:
                super.setObjectValue(key,obj);

        }
    }




    /**
     * flag to show interrupted by user
     */
    private boolean isTimeout=false;

    boolean isPrinting=false;

    /**
     * NOT PART OF API
     * tells software to generate glyph when first rendered not when decoded.
     * Should not need to be called in general usage
     */
    public void setBooleanValue(int key,boolean value) {

        switch(key){


            case IsPrinting:
                isPrinting=value;
                break;

            case ValueTypes.XFormFlattening:
                flattenXFormToImage=value;
                break;

            default:
                super.setBooleanValue(key,value);
        }
    }

    /**/

    /**used internally to allow for colored streams*/
    public void setDefaultColors(PdfPaint strokeCol, PdfPaint nonstrokeCol) {

        gs.strokeColorSpace.setColor(strokeCol);
        gs.nonstrokeColorSpace.setColor(nonstrokeCol);
        gs.setStrokeColor(strokeCol);
        gs.setNonstrokeColor(nonstrokeCol);
    }

    /**return the data*/
    public Object getObjectValue(int key){

        switch(key){
            case ValueTypes.PDFData:
                return  pdfData;

            case ValueTypes.PDFImages:
                return  pdfImages;

            case ValueTypes.TextAreas:
                return textAreas;

            case ValueTypes.TextDirections:
                return textDirections;

            case ValueTypes.DynamicVectorRenderer:
                return current;

            case PdfDictionary.Font:
                return  fontDecoder.getFontsInFile();

            case PdfDictionary.Image:
                return imagesInFile;

            case DecodeStatus.NonEmbeddedCIDFonts:
                return fontDecoder.getnonEmbeddedCIDFonts();

            case PageInfo.COLORSPACES:
                return cache.iterator(PdfObjectCache.ColorspacesUsed);

            default:
                if(LogWriter.isOutput())
                    LogWriter.writeLog("Unknown parameter "+key);


        }

        return null;
    }

    /**
     * read page header and extract page metadata
     * @throws PdfException
     */
    public final void readResources(PdfObject Resources,boolean resetList) throws PdfException {

        if(resetList)
            fontDecoder.resetfontsInFile();

        cache.readResources(Resources, resetList,currentPdfFile);

    }


    /**
     * decode the actual 'Postscript' stream into text and images by extracting
     * commands and decoding each.
     */
    public String decodeStreamIntoObjects(byte[] stream,boolean returnText) {

        if(stream.length==0)
            return null;

        int commandID=-1;


        //start of Dictionary on Inline image
        int startInlineStream=0;

        long startTime=System.currentTimeMillis();

        CommandParser parser=new CommandParser(stream);
        this.parser=parser;

        int streamSize=stream.length,dataPointer = 0,startCommand=0;

        /**
         * setup local objects
         */
        TextState currentTextState = new TextState();

        GSDecoder gsDecoder=new GSDecoder();
        gsDecoder.setParameters(isPageContent,renderPage, renderMode, extractionMode);
        gsDecoder.setRenderer(current);
        gsDecoder.setRes(cache);

        ShapeDecoder shapeDecoder=null;
        if(!getSamplingOnly){
            shapeDecoder=new ShapeDecoder();
            shapeDecoder.setRenderer(current);
            shapeDecoder.setLayerValues(layers, layerDecoder);
            shapeDecoder.setPdfData(pageData);
            shapeDecoder.setIntValue(FormLevel, formLevel);
            shapeDecoder.setIntValue(PageNumber, pageNum);
            shapeDecoder.setParameters(isPageContent,renderPage, renderMode, extractionMode);

            if(cache.groupObj!=null)
                shapeDecoder.setObjectValue(GroupObj,cache.groupObj);

        }

        ColorDecoder colorDecoder=null;
        if(!getSamplingOnly && (renderPage || textColorExtracted || colorExtracted)){

            colorDecoder=new ColorDecoder();
            colorDecoder.setParameters(isPageContent,renderPage, renderMode, extractionMode, isPrinting);
            colorDecoder.setPdfData(pageData);

            colorDecoder.setIntValue(PageNumber, pageNum);
            colorDecoder.setCache(cache);
            colorDecoder.setRenderer(current);
            colorDecoder.setFileHandler(currentPdfFile);

        }

        ShadingDecoder shadingDecoder=null; //on lazy init as not used often

        //setup textDecoder
        TextDecoder textDecoder;
        if(markedContentExtracted)
            textDecoder= new StructuredTextDecoder();
        else{
            textDecoder= new TextDecoder(pdfData);
            textDecoder.setReturnText(returnText);
        }

        if(errorTracker!=null)
            textDecoder.setHandlerValue(Options.ErrorTracker, errorTracker);
        textDecoder.setParameters(isPageContent,renderPage, renderMode, extractionMode, isPrinting);
        textDecoder.setFileHandler(currentPdfFile);
        textDecoder.setIntValue(FormLevel, formLevel);
        textDecoder.setIntValue(TextPrint, textPrint);
        textDecoder.setBooleanValue(RenderDirectly, renderDirectly);
        textDecoder.setBooleanValue(GenerateGlyphOnRender, generateGlyphOnRender);
        textDecoder.setLayerValues(layers, layerDecoder);
        textDecoder.setRenderer(current);

        if(!renderDirectly && statusBar!=null){
            statusBar.percentageDone=0;
            statusBar.resetStatus("stream");
        }

        /**
         * loop to read stream and decode
         */
        while (true) {

            //allow user to request exit and fail page
            if(requestTimeout || (timeoutInterval!=-1 && System.currentTimeMillis()-startTime >timeoutInterval)){
                requestTimeout =false;
                timeoutInterval=-1;
                isTimeout=true;

                break;
            }

            if(!renderDirectly && statusBar!=null)
                statusBar.percentageDone=(90*dataPointer)/streamSize;

            dataPointer=parser.getCommandValues(dataPointer,streamSize,tokenNumber);
            commandID=parser.getCommandID();

            //use negative flag to show commands found
            if(dataPointer<0){

                dataPointer=-dataPointer;
                try{

                    /**
                     * call method to handle commands
                     */
                    int commandType=Cmd.getCommandType(commandID);

                    /**text commands first and all other
                     * commands if not found in first
                     **/
                    switch(commandType){

                        case Cmd.TEXT_COMMAND:

                            if((commandID ==Cmd.EMC || layerDecoder.isLayerVisible()) && !getSamplingOnly &&(renderText || textExtracted)){

                                textDecoder.setCommands(parser);
                                textDecoder.setGS(gs);
                                textDecoder.setTextState(currentTextState);
                                textDecoder.setIntValue(TokenNumber, tokenNumber);

                                if(renderPage && commandID ==Cmd.BT){
                                    //save for later and set TR
                                    current.drawClip(gs,defaultClip,true) ;
                                    current.drawTR(GraphicsState.FILL);

                                    //flag text block started
                                    current.flagCommand(Cmd.BT,tokenNumber);
                                }

                                if(commandID ==Cmd.Tj || commandID ==Cmd.TJ || commandID ==Cmd.quote || commandID ==Cmd.doubleQuote){

                                    //flag which TJ command we are on
                                    current.flagCommand(Cmd.Tj,tokenNumber);

                                    if(currentTextState.hasFontChanged()){

                                        //switch to correct font
                                        String fontID=currentTextState.getFontID();
                                        PdfFont restoredFont = resolveFont(fontID);
                                        if(restoredFont!=null){
                                            currentFontData=restoredFont;
                                            current.drawFontBounds(currentFontData.getBoundingBox());
                                        }
                                    }

                                    if(currentFontData==null)
                                        currentFontData=new PdfFont(currentPdfFile);

                                    if(currentTextState.hasFontChanged()){
                                        textDecoder.resetFont();
                                        currentTextState.setFontChanged(false);
                                    }

                                    textDecoder.setFont(currentFontData);
                                }

                                dataPointer =textDecoder.processToken(currentTextState, commandID, startCommand, dataPointer);

                            }
                            break;

                        case Cmd.SHAPE_COMMAND:

                            if(!getSamplingOnly){
                                shapeDecoder.setCommands(parser);
                                shapeDecoder.setGS(gs);
                                shapeDecoder.setDefaultClip(defaultClip);
                                shapeDecoder.processToken(commandID,dataPointer,removeRenderImages);
                            }

                            break;

                        case Cmd.SHADING_COMMAND:

                            if(!getSamplingOnly && (renderPage || textColorExtracted || colorExtracted)){

                                if(shadingDecoder==null){
                                    shadingDecoder=new ShadingDecoder();
                                    shadingDecoder.setParameters(isPageContent,renderPage, renderMode, extractionMode, isPrinting);
                                    shadingDecoder.setPdfData(pageData);
                                    shadingDecoder.setIntValue(PageNumber, pageNum);
                                    shadingDecoder.setRenderer(current);
                                    shadingDecoder.setCache(cache);
                                    shadingDecoder.setFileHandler(currentPdfFile);
                                }
                                shadingDecoder.setGS(gs);
                                shadingDecoder.sh(parser.generateOpAsString(0, true));
                            }

                            break;

                        case Cmd.COLOR_COMMAND:

                            if(!getSamplingOnly && (renderPage || textColorExtracted || colorExtracted)){
                                colorDecoder.setGS(gs);

                                colorDecoder.setCommands(parser);

                                colorDecoder.processToken(commandID);

                            }

                            break;

                        case Cmd.GS_COMMAND:

                            gsDecoder.setCommands(parser);

                            gs = gsDecoder.processToken(commandID, getSamplingOnly, gs, currentTextState);

                            //may have changed so read back and reset
                            currentTextState= gsDecoder.getTextState();
                            if(commandID ==Cmd.cm && textDecoder!=null)
                                textDecoder.reset();

                            break;

                        case Cmd.IMAGE_COMMAND:

                            if(commandID==Cmd.BI){
                                startInlineStream= dataPointer;
                            }else{

                                ImageDecoder imageDecoder=null;
                                PdfObject XObject=null;
                                int subtype=1;
                                if(commandID==Cmd.Do){

                                    String name=parser.generateOpAsString(0, true);
                                    byte[] rawData=null;

                                    XObject = cache.getXObjects(name);
                                    if (XObject != null){

                                        rawData=XObject.getUnresolvedData();

                                        currentPdfFile.checkResolved(XObject);

                                       subtype = XObject.getParameterConstant(PdfDictionary.Subtype);
                                    }

                                    if (subtype == PdfDictionary.Form){

                                    	if(formLevel>10 &&  dataPointer==lastDataPointer){
                                    		//catch for odd files like customers-June2011/results.pdf
                                    	}else{
                                    	    lastDataPointer=dataPointer;  
                                    	    
                                            processXForm(dataPointer, XObject, defaultClip);

                                            //if lots of objects in play turn back to ref to save memory
                                            if(rawData!=null && cache.getXObjectCount()>30){
                                                String ref=XObject.getObjectRefAsString();

                                                cache.resetXObject(name,ref,rawData);
                                                XObject=null;

                                            }
                                        }
                                    }else
                                        imageDecoder= new XImageDecoder(formName,isType3Font,customImageHandler,useHiResImageForDisplay,objectStoreStreamRef, renderDirectly,pdfImages, formLevel,pageData,imagesInFile);

                                }else
                                    imageDecoder= new IDImageDecoder(startInlineStream,isType3Font,customImageHandler,useHiResImageForDisplay,objectStoreStreamRef,renderDirectly,pdfImages, formLevel,pageData,imagesInFile);

                                if (subtype != PdfDictionary.Form){
                                    imageDecoder.setIntValue(PageNumber, pageNum);
                                    imageDecoder.setIntValue(FormLevel, formLevel);
                                    imageDecoder.setHandlerValue(Options.ErrorTracker, errorTracker);
                                    imageDecoder.setRes(cache);
                                    imageDecoder.setGS(gs);
                                    imageDecoder.setSamplingOnly(getSamplingOnly);
                                    imageDecoder.setIntValue(ValueTypes.StreamType, streamType);
                                    imageDecoder.setName(fileName);
                                    imageDecoder.setFloatValue(Multiplier, multiplyer);
                                    imageDecoder.setFloatValue(SamplingUsed, samplingUsed);
                                    imageDecoder.setFileHandler(currentPdfFile);
                                    imageDecoder.setLayerValues(layers, layerDecoder);
                                    imageDecoder.setRenderer(current);
                                    imageDecoder.setIntValue(IsImage,imageStatus);

                                    imageDecoder.setParameters(isPageContent,renderPage, renderMode, extractionMode, isPrinting);

                                    imageDecoder.setCommands(parser);
                                    imageDecoder.setIntValue(ImageCount,imageCount);

                                    dataPointer =imageDecoder.processImage(dataPointer,XObject);

                                    samplingUsed=imageDecoder.getFloatValue(SamplingUsed);

                                    imageCount=imageDecoder.getIntValue(ImageCount);

                                    imagesInFile=imageDecoder.getImagesInFile();

                                    if(imageDecoder.getBooleanValue(HasYCCKimages))
                                        hasYCCKimages=true;

                                    if(imageDecoder.getBooleanValue(ImagesProcessedFully))
                                        imagesProcessedFully=true;
                                }
                            }
                            break;

                        case Cmd.T3_COMMAND:

                            if(!getSamplingOnly &&(renderText || textExtracted)) {

                            	if(t3Decoder==null)
                            		t3Decoder=new T3Decoder();

                                t3Decoder.setCommands(parser);
                                t3Decoder.setCommands(parser);
                            	t3Decoder.processToken(commandID);
                               
                            }
                            break;
                    }
                } catch (Exception e) {


                    if(LogWriter.isOutput())
                        LogWriter.writeLog("[PDF] "+ e+" Processing token >" + Cmd.getCommandAsString(commandID)+ "<>" + fileName+" <"+pageNum);

                    //only exit if no issue with stream
                    if(isDataValid){
                    }else
                        dataPointer=streamSize;

                } catch (OutOfMemoryError ee) {
                    errorTracker.addPageFailureMessage("Memory error decoding token stream");

                    if(LogWriter.isOutput())
                        LogWriter.writeLog("[MEMORY] Memory error - trying to recover");
                }

                //save for next command
                startCommand=dataPointer;

                //reset array of trailing values
                parser.reset();

                //increase pointer
                tokenNumber++;
            }

            //break at end
            if (streamSize <= dataPointer)
                break;
        }

        if(!renderDirectly && statusBar!=null)
            statusBar.percentageDone=100;

        //pick up TextDecoder values
        isTTHintingRequired=textDecoder.isTTHintingRequired();
        textAreas= (Vector_Rectangle) textDecoder.getObjectValue(ValueTypes.TextAreas);
        textDirections= (Vector_Int) textDecoder.getObjectValue(ValueTypes.TextDirections);

            return "";
    }

    /**
     * decode or get font
     * @param fontID
     */
    private PdfFont resolveFont(String fontID) {

        PdfFont restoredFont=(PdfFont) cache.resolvedFonts.get(fontID);

        //check it was decoded
        if(restoredFont==null){

            String ref=(String)cache.unresolvedFonts.get(fontID);
            PdfObject newFont;

            if(ref!=null){

                //remove from list
                cache.unresolvedFonts.remove(fontID);

                newFont=new FontObject(ref);
                newFont.setStatus(PdfObject.UNDECODED_REF);
                newFont.setUnresolvedData(ref.getBytes(), PdfDictionary.Font);
                currentPdfFile.checkResolved(newFont);

                //currentPdfFile.readObject(newFont);
            }else{
                newFont=(PdfObject)cache.directFonts.get(fontID);

                //remove from list
                if(newFont!=null)
                    cache.directFonts.remove(fontID);
            }

            if(newFont!=null){
                try {
                    restoredFont = fontDecoder.createFont(newFont, fontID,  objectStoreStreamRef);

                    //<start-pro>
                    /**
                    //<end-pro>
                    
                    //<start-std>
                    if(current.getType()== HTMLDisplay.CREATE_HTML && current.getValue(HTMLDisplay.FontMode)== org.jpedal.examples.html.HTMLFontMapper.EMBED_ALL){

                        PdfObject pdfFontDescriptor=newFont.getDictionary(PdfDictionary.FontDescriptor);

                        //if null check to see if it is a CIF font and get data from DescendantFonts obj
                        if (pdfFontDescriptor== null ) {
                            PdfObject Descendent=newFont.getDictionary(PdfDictionary.DescendantFonts);
                            if(Descendent!=null)
                                pdfFontDescriptor=Descendent.getDictionary(PdfDictionary.FontDescriptor);
                        }

                        //write out any embedded font file data

                        if (pdfFontDescriptor!= null ) {

                            byte[] stream;
                            PdfObject FontFile2=pdfFontDescriptor.getDictionary(PdfDictionary.FontFile2);

                            if(FontFile2!=null){ //truetype fonts
                                stream=currentPdfFile.readStream(FontFile2,true,true,false, false,false, FontFile2.getCacheName(currentPdfFile.getObjectReader()));
                                current.writeCustom(HTMLDisplay.SAVE_EMBEDDED_FONT, new Object[]{restoredFont.getFontName(),stream,"ttf"});
//                            }else{
//                                PdfObject FontFile3=pdfFontDescriptor.getDictionary(PdfDictionary.FontFile3);
//                                if(FontFile3!=null){ //type1 fonts
//                                    stream=currentPdfFile.readStream(FontFile3,true,true,false, false,false, FontFile3.getCacheName(currentPdfFile.getObjectReader()));
//                                    current.writeCustom(HTMLDisplay.SAVE_EMBEDDED_FONT, new Object[]{restoredFont.getFontName(),stream,"pfb"});
//                                }else{
//
//                                    PdfObject FontFile=pdfFontDescriptor.getDictionary(PdfDictionary.FontFile);
//                                    if(FontFile!=null){ //type1 fonts
//                                        stream=currentPdfFile.readStream(FontFile,true,true,false, false,false, FontFile.getCacheName(currentPdfFile.getObjectReader()));
//                                        current.writeCustom(HTMLDisplay.SAVE_EMBEDDED_FONT, new Object[]{restoredFont.getFontName(),stream,"pfb"});
//                                    }
//                                }
                            }
                        }
                    }
                    
                    //<end-std>
                    /**/
                } catch (PdfException e) {
                    e.printStackTrace();
                }
            }

            //store
            if(restoredFont!=null)
                cache.resolvedFonts.put(fontID,restoredFont);

        }

        return restoredFont;
    }

    /**
     return boolean flags with appropriate ket
     */
    public boolean getBooleanValue(int key) {

        switch(key){

            case ValueTypes.EmbeddedFonts:
                return fontDecoder.hasEmbeddedFonts();


            case DecodeStatus.PageDecodingSuccessful:
                return errorTracker.pageSuccessful;

            case DecodeStatus.NonEmbeddedCIDFonts:
                return fontDecoder.hasNonEmbeddedCIDFonts();

            case DecodeStatus.ImagesProcessed:
                return imagesProcessedFully;

            case DecodeStatus.YCCKImages:
                return hasYCCKimages;

            case DecodeStatus.Timeout:
                return isTimeout;

            case DecodeStatus.TTHintingRequired:
                return isTTHintingRequired;

            default:
                throw new RuntimeException("Unknown value "+key);
        }
    }

    public void dispose() {

        if(pdfData!=null)
            this.pdfData.dispose();

        //this.pageLines=null;

    }
    /**
     private class TestShapeTracker implements ShapeTracker {
     public void addShape(int tokenNumber, int type, Shape currentShape, PdfPaint nonstrokecolor, PdfPaint strokecolor) {

     //use this to see type
     //Cmd.getCommandAsString(type);

     //print out details
     if(type==Cmd.S || type==Cmd.s){ //use stroke color to draw line
     System.out.println("-------Stroke-------PDF cmd="+Cmd.getCommandAsString(type));
     System.out.println("tokenNumber="+tokenNumber+" "+currentShape.getBounds()+" stroke color="+strokecolor);

     }else if(type==Cmd.F || type==Cmd.f || type==Cmd.Fstar || type==Cmd.fstar){ //uses fill color to fill shape
     System.out.println("-------Fill-------PDF cmd="+Cmd.getCommandAsString(type));
     System.out.println("tokenNumber="+tokenNumber+" "+currentShape.getBounds()+" fill color="+nonstrokecolor);

     }else{ //not yet implemented (probably B which is S and F combo)
     System.out.println("Not yet added");
     System.out.println("tokenNumber="+tokenNumber+" "+currentShape.getBounds()+" type="+type+" "+Cmd.getCommandAsString(type));
     }
     }
     }

     /**
     * request exit from main loop
     */
    public void reqestTimeout(Object value) {

        if(value==null)
            requestTimeout =true;
        else if(value instanceof Integer){
            timeoutInterval=((Integer)value).intValue();
        }
    }

    public void setIntValue(int key, int value) {

        switch(key){

            /**
             * currentPage number
             */
            case ValueTypes.PageNum:
                this.pageNum=value;
                break;

            /**
             * tells program to try and use Java's font printing if possible
             * as work around for issue with PCL printing
             */
            case TextPrint:
                this.textPrint = value;
                break;

            default:
                super.setIntValue(key,value);
        }
    }

    public void setParameters(boolean isPageContent, boolean renderPage, int renderMode, int extractionMode) {

        super.setParameters(isPageContent,renderPage, renderMode, extractionMode);

        fontDecoder.setParameters(isPageContent,renderPage, renderMode, extractionMode);

        /**
         * flags
         */

        renderText=renderPage &&(renderMode & PdfDecoder.RENDERTEXT) == PdfDecoder.RENDERTEXT;

        textExtracted=(extractionMode & PdfDecoder.TEXT)==PdfDecoder.TEXT;

        textColorExtracted=(extractionMode & PdfDecoder.TEXTCOLOR) == PdfDecoder.TEXTCOLOR;

        colorExtracted=(extractionMode & PdfDecoder.COLOR) == PdfDecoder.COLOR;

        removeRenderImages=renderPage &&(renderMode & PdfDecoder.REMOVE_RENDERSHAPES )== PdfDecoder.REMOVE_RENDERSHAPES;

    }

    /**
     * recursive subroutine so in actual body of PdfStreamDecoder so it can recall decodeStream
     * @param dataPointer
     */
    private int processXForm(int dataPointer, PdfObject XObject, Shape defaultClip) throws PdfException {

        final boolean debug=false;

        if(debug)
            System.out.println("processImage "+dataPointer+" "+XObject.getObjectRefAsString()+" "+defaultClip);

        if(!layerDecoder.isLayerVisible() || !isVisible(XObject) || XObject==null)
            return dataPointer;

        String oldFormName=formName;

        String name=parser.generateOpAsString(0, true);

        //name is not unique if in form so we add form level to separate out
        if(formLevel>1)
            name= formName+'_'+ formLevel+'_'+name;

        //string to hold image details
        String details=name;

        try {

            if(IDImageDecoder.trackImages){
                //add details to string so we can pass back
                if(imagesInFile==null)
                    imagesInFile=details+" Form";
                else
                    imagesInFile=details+" Form\n"+ imagesInFile;
            }

            //reset operand
            parser.reset();

            //read stream for image
            byte[] objectData = currentPdfFile.readStream(XObject, true, true, false, false, false, XObject.getCacheName(currentPdfFile.getObjectReader()));
            if (objectData != null) {

                String oldIndent= PdfStreamDecoder.indent;
                PdfStreamDecoder.indent= PdfStreamDecoder.indent+"   ";

                //set value and see if Transform matrix
                float[] transformMatrix=new float[6];
                float[] matrix= XObject.getFloatArray(PdfDictionary.Matrix);
                boolean isIdentity=matrix==null || isIdentity(matrix);
                if(matrix!=null)
                    transformMatrix=matrix;

                float[][] CTM=null, oldCTM=null;

                //allow for stroke line width being altered by scaling
                float lineWidthInForm=-1; //negative values not used

                if(matrix!=null && !isIdentity) {

                    //save current
                    float[][] currentCTM=new float[3][3];
                    for(int i=0;i<3;i++)
                        System.arraycopy(gs.CTM[i], 0, currentCTM[i], 0, 3);

                    oldCTM = currentCTM;

                    CTM= gs.CTM;

                    float[][] scaleFactor={{transformMatrix[0],transformMatrix[1],0},
                            {transformMatrix[2],transformMatrix[3],0},
                            {transformMatrix[4],transformMatrix[5],1}};

                    scaleFactor= Matrix.multiply(scaleFactor, CTM);
                    gs.CTM=scaleFactor;

                    //work out line width
                    lineWidthInForm=transformMatrix[0]* gs.getLineWidth();

                    if (lineWidthInForm == 0)
                        lineWidthInForm=transformMatrix[1]* gs.getLineWidth();

                    if(lineWidthInForm<0)
                        lineWidthInForm=-lineWidthInForm;

                    if(debug)
                        System.out.println("setMatrix "+gs.CTM[0][0]+" "+gs.CTM[0][1]+" "+gs.CTM[1][0]+" "+gs.CTM[1][1]+" "+gs.CTM[2][0]+" "+gs.CTM[2][1]);
                }

                //track depth
                formLevel++;

                //track name so we can make unique key for image name
                if(formLevel==1)
                    formName= name;
                else
                    formName= formName+'_'+ name;

                //preserve colorspaces
                GenericColorSpace mainStrokeColorData=(GenericColorSpace) gs.strokeColorSpace.clone();
                GenericColorSpace mainnonStrokeColorData=(GenericColorSpace) gs.nonstrokeColorSpace.clone();

                //set form line width if appropriate
                if(lineWidthInForm>0)
                    gs.setLineWidth(lineWidthInForm);

                //set gs max to current so child gs values can not exceed
                float maxStrokeValue= gs.getAlphaMax(GraphicsState.STROKE);
                float maxFillValue= gs.getAlphaMax(GraphicsState.FILL);
                gs.setMaxAlpha(GraphicsState.STROKE, gs.getAlpha(GraphicsState.STROKE));

                if(formLevel==1)
                    gs.setMaxAlpha(GraphicsState.FILL, gs.getAlpha(GraphicsState.FILL));

                //make a copy s owe can restore to original state
                //we need to pass in and then undo any changes at end
                PdfObjectCache mainCache = cache.copy();   //setup cache
                cache.reset(mainCache);   //copy in data

                /**read any resources*/
                PdfObject Resources= XObject.getDictionary(PdfDictionary.Resources);

                /**read any resources*/
                cache.groupObj= XObject.getDictionary(PdfDictionary.Group);
                currentPdfFile.checkResolved(cache.groupObj);

                currentPdfFile.checkResolved(Resources);
                readResources(Resources, false);

                /**
                 * see if bounding box and set
                 */
                float[] BBox= XObject.getFloatArray(PdfDictionary.BBox);
                Area clip=null;
                boolean clipChanged=false;

                //this code breaks customers-june2011/169351.pdf so added
                //  as possible fix
               if(BBox!=null && BBox[0]==0 && BBox[1]==0 && BBox[2]>1 && BBox[3]>1   && (gs.CTM[0][0]>0.99 || gs.CTM[2][1]<-1) && (gs.CTM[2][0]<-1 || gs.CTM[2][0]>1) && gs.CTM[2][1]!=0 ){//)  && BBox[2]>1 && BBox[3]>1 ){//if(BBox!=null && matrix==null && BBox[0]==0 && BBox[1]==0){

                  if(debug)
                        System.out.println("setClip");

                    clip = setClip(defaultClip, BBox);
                    clipChanged=true;
              }
               
                /**decode the stream*/
                if(objectData.length>0){

                    PdfObject newSMask = getSMask(BBox); //check for soft mask we need to apply
                    int firstValue = getFirstValue(gs.getBM()); //see if multiply transparency

                    //isTransparent sees if this case happens (randomHouse/9781580082778_DistX.pdf)
                    //added formLevel to try and fix customer issue on customers3/Auktionsauftrag_45.9.33620.pdf (including printing)
                    if(!isTransparent(cache.groupObj) && (flattenXFormToImage || (isPrinting && (gs.CTM[2][0]==0) && gs.CTM[2][1]==0 && newSMask==null && firstValue!=PdfDictionary.Multiply && gs.getAlpha(GraphicsState.FILL)==1) ||
                            ((gs.getAlpha(GraphicsState.FILL)==1 || layerDecoder.layerLevel>0 || (formLevel==1 && gs.getAlpha(GraphicsState.FILL)<0.1f)) && newSMask==null && firstValue!=PdfDictionary.Multiply))){ //use if looks like marked text block

                        if(debug)
                            System.out.println("decode");

                        decodeStreamIntoObjects(objectData, false);

                    }else if(newSMask!=null || firstValue==PdfDictionary.Multiply){ //if an smask render to image and apply Smask to it - then write out as image

                        if(debug)
                            System.out.println("createMaskForm");

                        createMaskForm(XObject, name, newSMask, firstValue);

                    }else{

                        if(debug)
                            System.out.println("other");

                        //save renderer
                        DynamicVectorRenderer oldCurrent=current;
                        current=new ScreenDisplay(pageNum, objectStoreStreamRef,false);
                        current.setHiResImageForDisplayMode(useHiResImageForDisplay);

                        boolean oldRenderDirectly= renderDirectly;


                        //to draw image we need to use local 1
                        float strokeAlpha= gs.getAlpha(GraphicsState.STROKE);
                        float maxStroke= gs.getAlphaMax(GraphicsState.STROKE);
                        float fillAlpha= gs.getAlpha(GraphicsState.FILL);
                        float maxFill= gs.getAlphaMax(GraphicsState.FILL);

                        currentPdfFile.checkResolved(cache.pageGroupingObj);

                        if(cache.pageGroupingObj!=null && renderDirectly){
                            gs.setMaxAlpha(GraphicsState.STROKE,1);


                            //needed for /PDFdata/baseline_screens/customers1/Milkshake BusyTime DistX.pdf
                           // if(cache.pageGroupingObj.getDictionary(PdfDictionary.ColorSpace).getParameterConstant(PdfDictionary.ColorSpace)!=ColorSpaces.DeviceCMYK){
                            //   gs.setMaxAlpha(GraphicsState.FILL,1);
                           // }
                        }else{
                            gs.setMaxAlpha(GraphicsState.STROKE,strokeAlpha);
                            gs.setMaxAlpha(GraphicsState.FILL,fillAlpha);
                        }

                        if(renderDirectly && (cache.pageGroupingObj==null ||
                                (cache.pageGroupingObj!=null && cache.groupObj!=null &&
                                        (cache.groupObj.getDictionary(PdfDictionary.ColorSpace).getParameterConstant(PdfDictionary.ColorSpace)==ColorSpaces.ICC ||
                                        cache.groupObj.getDictionary(PdfDictionary.ColorSpace).getParameterConstant(PdfDictionary.ColorSpace)!=cache.pageGroupingObj.getDictionary(PdfDictionary.ColorSpace).getParameterConstant(PdfDictionary.ColorSpace)))))
                            gs.setMaxAlpha(GraphicsState.FILL,1);

                        if(!renderDirectly){
                            gs.setAlpha(GraphicsState.STROKE,1);
                            gs.setAlpha(GraphicsState.FILL,1);
                        }

                        renderDirectly=false;

                        //ensure drawn
                        oldCurrent.setGraphicsState(GraphicsState.STROKE,1f);
                        oldCurrent.setGraphicsState(GraphicsState.FILL,1f);


                        decodeStreamIntoObjects(objectData, false);

                        gs.setMaxAlpha(GraphicsState.STROKE,maxStroke);
                        gs.setMaxAlpha(GraphicsState.FILL,maxFill);

                        if(!renderDirectly){
                            gs.setAlpha(GraphicsState.STROKE,strokeAlpha);
                            gs.setAlpha(GraphicsState.FILL,fillAlpha);
                        }

                        oldCurrent.drawXForm(current, gs);

                        //restore
                        current=oldCurrent;
                        current.setGraphicsState(GraphicsState.STROKE,strokeAlpha);
                        current.setGraphicsState(GraphicsState.FILL,fillAlpha);

                        renderDirectly=oldRenderDirectly;
                    }
                }
                //restore clip if changed
                if(clipChanged){
                    gs.setClippingShape(clip);
                    current.drawClip(gs,clip,false) ;
                }

                //restore settings
                formLevel--;

                //restore old matrix
                if(oldCTM!=null)
                    gs.CTM=oldCTM;

                /**restore old colorspace and fonts*/
                gs.strokeColorSpace=mainStrokeColorData;
                gs.nonstrokeColorSpace=mainnonStrokeColorData;

                //put back original state
                cache.restore(mainCache);

                //restore gs max to current so child gs values can not exceed
                gs.setMaxAlpha(GraphicsState.STROKE, maxStrokeValue);
                gs.setMaxAlpha(GraphicsState.FILL, maxFillValue);

                PdfStreamDecoder.indent=oldIndent;
            }

        } catch (Error e) {
            e.printStackTrace();
            imagesProcessedFully=false;
            errorTracker.addPageFailureMessage("Error " + e + " in DO");
        } catch (Exception e) {

            if(LogWriter.isOutput())
                LogWriter.writeLog("Exception "+e);

            imagesProcessedFully=false;
            errorTracker.addPageFailureMessage("Error " + e + " in DO");
        }

        formName=oldFormName;


        return dataPointer;

    }

    private PdfObject getSMask(float[] BBox) {
        PdfObject newSMask=null;

        if(gs.SMask!=null && BBox!=null && BBox[2]>0 ){ //see if SMask to cache to image & stop negative cases such as Milkshake StckBook Activity disX.pdf
            if(gs.SMask.getParameterConstant(PdfDictionary.Type)!=PdfDictionary.Mask || gs.SMask.getFloatArray(PdfDictionary.BC)!=null){ //fix for waves file
                newSMask= gs.SMask.getDictionary(PdfDictionary.G);
                currentPdfFile.checkResolved(newSMask);
            }
        }
        return newSMask;
    }

    private static int getFirstValue(PdfArrayIterator BMvalue) {

        int firstValue= PdfDictionary.Unknown;
        if(BMvalue !=null && BMvalue.hasMoreTokens()) {
            firstValue= BMvalue.getNextValueAsConstant(false);
        }
        return firstValue;
    }

    private void createMaskForm(PdfObject XObject, String name, PdfObject newSMask, int firstValue) throws PdfException {
        float[] BBox;//size
        BBox= XObject.getFloatArray(PdfDictionary.BBox);

        /**get form as an image*/
        int fx=(int)BBox[0];
        int fy=(int)BBox[1];
        int fw=(int)BBox[2];
        int fh=(int)(BBox[3]);

        //check x,y offsets and factor in
        if(fx<0)
            fx=0;

        //get the form
        BufferedImage image=null;

        // get smask if present and create as image for later
        if(newSMask!=null){

            image = getImageFromPdfObject(XObject, fx, fw, fy, fh);
            BufferedImage smaskImage = getImageFromPdfObject(newSMask, fx, fw, fy, fh);

            /**
             * get Mask colourspace as we need to process mask differently depending on value
             */
            PdfObject ColorSpace=null;
            PdfObject group=newSMask.getDictionary(PdfDictionary.Group);
            if(group!=null){
                currentPdfFile.checkResolved(group);
                ColorSpace=group.getDictionary(PdfDictionary.ColorSpace);
            }

            //apply SMask to image
            image= IDImageDecoder.applySmask(image, smaskImage, newSMask, true,true, ColorSpace);

            smaskImage.flush();
            smaskImage=null;
        }

        GraphicsState gs1 =new GraphicsState();
        gs1.CTM=new float[][]{{image.getWidth(),0,1},{0,image.getHeight(),1},{0,0,0}};

        //different formula needed if flattening forms
        if(isFlattenedForm){
            gs1.x= flattenX;
            gs1.y= flattenY;
        }else{
            gs1.x=fx;
            gs1.y=fy-image.getHeight();
        }

        //draw as image
        gs1.CTM[2][0]= gs1.x;
        gs1.CTM[2][1]= gs1.y;
        current.drawImage(pageNum,image, gs1,false, name, PDFImageProcessing.IMAGE_INVERTED, -1);
    }

    private BufferedImage createTransparentForm(PdfObject XObject, int fx, int fy, int fw, int fh) {
        BufferedImage image;
        byte[] objectData1 = currentPdfFile.readStream(XObject,true,true,false, false,false, XObject.getCacheName(currentPdfFile.getObjectReader()));

        ObjectStore localStore = new ObjectStore();
        DynamicVectorRenderer glyphDisplay=new ScreenDisplay(0,false,20,localStore);
        glyphDisplay.setHiResImageForDisplayMode(useHiResImageForDisplay);

        PdfStreamDecoder glyphDecoder=new PdfStreamDecoder(currentPdfFile, useHiResImageForDisplay); //switch to hires as well
        glyphDecoder.setParameters(isPageContent, renderPage, renderMode, extractionMode);
        glyphDecoder.setObjectValue(ValueTypes.ObjectStore, localStore);
        glyphDecoder.setIntValue(FormLevel,formLevel);
        glyphDecoder.setFloatValue(Multiplier, multiplyer);
        glyphDecoder.setFloatValue(SamplingUsed, samplingUsed);

        glyphDecoder.setObjectValue(ValueTypes.DynamicVectorRenderer,glyphDisplay);

        /**read any resources*/
        try{

            PdfObject SMaskResources = XObject.getDictionary(PdfDictionary.Resources);
            currentPdfFile.checkResolved(SMaskResources);
            if (SMaskResources != null)
                glyphDecoder.readResources(SMaskResources,false);

        }catch(Exception e){
            e.printStackTrace();
        }

        /**decode the stream*/
        if(objectData1 !=null)
            glyphDecoder.decodeStreamIntoObjects(objectData1,false);

        glyphDecoder=null;

        int hh= fh;
        if(fy > fh)
            hh= fy - fh;

        //get bit underneath and merge in
        image=new BufferedImage(fw,hh, BufferedImage.TYPE_INT_ARGB);

        Graphics2D formG2=image.createGraphics();

        if(!isFlattenedForm) //already allowed for in form flattening
            formG2.translate(-fx,-fh);

        //current.paint(formG2,null,null,null,false,true);

        formG2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));

        glyphDisplay.setG2(formG2);
        glyphDisplay.paint(null,null,null);

        localStore.flush();

        return image;
    }

    private static boolean isTransparent(PdfObject groupObj) {

        boolean isTransparentRGB=false;
        if(groupObj!=null){
            String S= groupObj.getName(PdfDictionary.S);
            PdfObject colspace= groupObj.getDictionary(PdfDictionary.ColorSpace);

            isTransparentRGB= S!=null && S.equals("Transparency") && colspace!= null &&
                    colspace.getParameterConstant(PdfDictionary.ColorSpace)== ColorSpaces.DeviceRGB;
        }

        return isTransparentRGB;
    }

    private Area setClip(Shape defaultClip, float[] BBox) {
        Area clip;
        float scaling= gs.CTM[0][0];
        if(scaling==0)
            scaling= gs.CTM[0][1];

        int x,y,w,h;

        if(gs.CTM[0][1]>0 && gs.CTM[1][0]<0){

            x=(int)(gs.CTM[2][0]-(BBox[3]));
            y=(int)(gs.CTM[2][1]+BBox[0]);
            w=(int)((BBox[3]-BBox[1])*scaling);
            h=(int)((BBox[2]-BBox[0])*scaling);

        }else if(gs.CTM[0][1]<0 && gs.CTM[1][0]>0){

            x=(int)(gs.CTM[2][0]+BBox[1]);
            y=(int)(gs.CTM[2][1]-BBox[2]);
            w=(int)((BBox[3]-BBox[1])*-scaling);
            h=(int)((BBox[2]-BBox[0])*-scaling);

        }else{
            x=(int)(gs.CTM[2][0]+BBox[0]);
            y=(int)(gs.CTM[2][1]+BBox[1]);
            w=(int)(1+(BBox[2]-BBox[0])*scaling);
            h=(int)(1+(BBox[3]-BBox[1])*scaling);

            //allow for inverted
            if(gs.CTM[1][1]<0){
                y=y-h;
            }
        }

        if(gs.getClippingShape()==null)
            clip=null;
        else
            clip= (Area) gs.getClippingShape().clone();

        Area newClip=new Area(new Rectangle(x,y,w,h));

        gs.updateClip(new Area(newClip));
        current.drawClip(gs, defaultClip,false) ;

        return clip;
    }

    final private static float[] matches={1f,0f,0f,1f,0f,0f};

    private static boolean isIdentity(float[] matrix) {

        boolean isIdentity=true;// assume right and try to disprove

        if(matrix!=null){

            //see if it matches if not set flag and exit
            for(int ii=0;ii<6;ii++){
                if(matrix[ii]!=matches[ii]){
                    isIdentity=false;
                    break;
                }
            }
        }

        return isIdentity;
    }

    private BufferedImage getImageFromPdfObject(PdfObject newSMask, int fx, int fw, int fy, int fh) throws PdfException {

        BufferedImage smaskImage;
        Graphics2D formG2;
        byte[] objectData =currentPdfFile.readStream(newSMask,true,true,false, false,false, newSMask.getCacheName(currentPdfFile.getObjectReader()));

        ObjectStore localStore = new ObjectStore();

        DynamicVectorRenderer glyphDisplay=new ScreenDisplay(0,false,20,localStore);

        PdfStreamDecoder glyphDecoder=new PdfStreamDecoder(currentPdfFile,useHiResImageForDisplay); //switch to hires as well
        glyphDecoder.setParameters(isPageContent,renderPage, renderMode, extractionMode);
        glyphDecoder.setObjectValue(ValueTypes.ObjectStore, localStore);
        glyphDisplay.setHiResImageForDisplayMode(useHiResImageForDisplay);
        glyphDecoder.setObjectValue(ValueTypes.DynamicVectorRenderer,glyphDisplay);
        glyphDecoder.setFloatValue(Multiplier, multiplyer);
        glyphDecoder.setFloatValue(SamplingUsed, samplingUsed);
        glyphDecoder.setBooleanValue(IsFlattenedForm,isFlattenedForm);
        glyphDecoder.setIntValue(FormLevel,formLevel);

        //flag to image decoder that called form here and whether screen or image
        if(renderDirectly)
            glyphDecoder.setIntValue(IsImage,IMAGE_getImageFromPdfObject);
        else
            glyphDecoder.setIntValue(IsImage,SCREEN_getImageFromPdfObject);

        /**read any resources*/
        try{

            PdfObject SMaskResources =newSMask.getDictionary(PdfDictionary.Resources);
            currentPdfFile.checkResolved(SMaskResources);
            if (SMaskResources != null)
                glyphDecoder.readResources(SMaskResources,false);

        }catch(Exception e){
            e.printStackTrace();
        }

        /**decode the stream*/
        if(objectData!=null)
            glyphDecoder.decodeStreamIntoObjects(objectData,false);

        glyphDecoder.dispose();

        int hh=fh;
        if(fy>fh)
            hh=fy-fh;

        if(fw==0)
            fw=1;
        smaskImage=new  BufferedImage(fw,hh,BufferedImage.TYPE_INT_ARGB);

        formG2=smaskImage.createGraphics();

        formG2.translate(-fx,-fh);
        glyphDisplay.setG2(formG2);
        glyphDisplay.paint(null,null,null);

        localStore.flush();
        return smaskImage;
    }

}
