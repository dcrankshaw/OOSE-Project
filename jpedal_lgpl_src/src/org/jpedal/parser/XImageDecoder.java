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
  * ImageDecoder.java
  * ---------------
 */
package org.jpedal.parser;

import org.jpedal.PdfDecoder;
import org.jpedal.color.*;
import org.jpedal.exception.PdfException;
import org.jpedal.external.ImageHandler;
import org.jpedal.images.ImageTransformerDouble;
import org.jpedal.io.*;
import org.jpedal.objects.GraphicsState;
import org.jpedal.objects.PdfImageData;
import org.jpedal.objects.PdfPageData;
import org.jpedal.objects.raw.*;
import org.jpedal.utils.LogWriter;

import java.awt.*;
import java.awt.image.*;
import java.util.HashMap;
import java.util.Map;

/**
 * decode images
 */
public class XImageDecoder extends IDImageDecoder{

    private static boolean rejectSuperimposedImages=true;

    private String formName;


    public XImageDecoder(boolean isType3Font,ImageHandler customImageHandler, boolean useHiResImageForDisplay,ObjectStore objectStoreStreamRef,
                         boolean renderDirectly, PdfImageData pdfImages,int formLevel, PdfPageData pageData, String imagesInFile) {

        this.mode=ImageDecoder.XOBJECT;
        this.isType3Font=isType3Font;
        this.customImageHandler=customImageHandler;
        this.useHiResImageForDisplay=useHiResImageForDisplay;
        this.objectStoreStreamRef=objectStoreStreamRef;
        this.renderDirectly=renderDirectly;
        this.pdfImages=pdfImages;
        this.formLevel=formLevel;
        this.pageData=pageData;
        this.imagesInFile=imagesInFile;
    }

    public XImageDecoder(String formName, boolean isType3Font, ImageHandler customImageHandler, boolean useHiResImageForDisplay, ObjectStore objectStoreStreamRef, boolean renderDirectly, PdfImageData pdfImages, int formLevel, PdfPageData pageData, String imagesInFile) {

        this.mode=ImageDecoder.ID;

        this.formName=formName;

        this.isType3Font=isType3Font;
        this.customImageHandler=customImageHandler;
        this.useHiResImageForDisplay=useHiResImageForDisplay;
        this.objectStoreStreamRef=objectStoreStreamRef;
        this.renderDirectly=renderDirectly;

        this.pdfImages=pdfImages;
        this.formLevel=formLevel;
        this.pageData=pageData;

        this.imagesInFile=imagesInFile;

    }

    static{
        String operlapValue=System.getProperty("org.jpedal.rejectsuperimposedimages");
        if(operlapValue!=null)
            rejectSuperimposedImages=(operlapValue!=null && operlapValue.toLowerCase().indexOf("true")!=-1);

    }


    private GenericColorSpace setupXObjectColorspace(PdfObject XObject,int depth, int width, int height,byte[] objectData) {

        PdfObject ColorSpace=XObject.getDictionary(PdfDictionary.ColorSpace);

        //handle colour information
        GenericColorSpace decodeColorData=new DeviceRGBColorSpace();

        if(ColorSpace!=null){
            decodeColorData= ColorspaceFactory.getColorSpaceInstance(currentPdfFile, ColorSpace, cache.XObjectColorspaces);

            decodeColorData.setPrinting(isPrinting);

            //track colorspace use
            cache.put(PdfObjectCache.ColorspacesUsed, new Integer(decodeColorData.getID()).intValue(),"x");

            if(depth==1 && decodeColorData.getID()== ColorSpaces.DeviceRGB && XObject.getDictionary(PdfDictionary.Mask)==null){

                byte[] data=decodeColorData.getIndexedMap();

                //no index or first colour is white so use grayscale
                if(decodeColorData.getIndexedMap()==null || (data.length==6 && data[0]==0 && data[1]==0 && data[2]==0))
                    decodeColorData=new DeviceGrayColorSpace();
            }
        }

        //fix for odd itext file (/PDFdata/baseline_screens/debug3/Leistung.pdf)
        byte[] indexData=decodeColorData.getIndexedMap();
        if(depth==8 && indexData!=null && decodeColorData.getID()==ColorSpaces.DeviceRGB && width*height==objectData.length){

            PdfObject newMask=XObject.getDictionary(PdfDictionary.Mask);
            if(newMask!=null){

                int[] maskArray=newMask.getIntArray(PdfDictionary.Mask);

                //this specific case has all zeros
                if(maskArray!=null && maskArray.length==2 && maskArray[0]==255 && maskArray[0]==maskArray[1] && decodeColorData.getIndexedMap()!=null && decodeColorData.getIndexedMap().length==768){

                    //see if index looks corrupt (ie all zeros) We exit as soon as we have disproved
                    boolean isCorrupt=true;
                    for(int jj=0;jj<768;jj++){
                        if(indexData[jj]!=0){
                            isCorrupt=false;
                            jj=768;
                        }
                    }

                    if(isCorrupt)
                        decodeColorData=new DeviceGrayColorSpace();
                }
            }
        }

        //pass through decode params
        PdfObject parms=XObject.getDictionary(PdfDictionary.DecodeParms);
        if(parms!=null)
            decodeColorData.setDecodeParms(parms);

        //set any intent
        decodeColorData.setIntent(XObject.getName(PdfDictionary.Intent));

        return decodeColorData;
    }



    /**
     * clip image as MAC has nasty bug :-(
     */
    private BufferedImage clipForMac(BufferedImage image) {

        //if valid image then process
        if (image != null) {

            /**
             * scale the raw image to correct page size (at 72dpi)
             */

            //object to scale and clip. Creating instance does the scaling
            ImageTransformerDouble image_transformation =new ImageTransformerDouble(PdfDecoder.dpi,gs,image,createScaledVersion ,false);

            //extract images either scaled/clipped or scaled then clipped

            image_transformation.doubleScaleTransformShear(true);

            //get intermediat eimage and save
            image = image_transformation.getImage();

        }
        return image;
    }



    public BufferedImage processImageXObject(PdfObject XObject, String image_name,
                                             byte[] objectData, boolean saveRawData, String details) throws PdfException {

        boolean imageMask = false;

        BufferedImage image=null;

        //add filename to make it unique
        image_name = fileName+ '-' + image_name;

        int depth=1;
        int width = XObject.getInt(PdfDictionary.Width);
        int height = XObject.getInt(PdfDictionary.Height);
        int newDepth = XObject.getInt(PdfDictionary.BitsPerComponent);
        if(newDepth!=PdfDictionary.Unknown)
            depth=newDepth;

        isMask= XObject.getBoolean(PdfDictionary.ImageMask);
        imageMask= isMask;

        GenericColorSpace decodeColorData = setupXObjectColorspace(XObject, depth, width, height, objectData);

        //tell user and log
        if(LogWriter.isOutput())
            LogWriter.writeLog("Processing XObject: "+ image_name+ ' ' +XObject.getObjectRefAsString()+ " width="+ width+ " Height="+ height+
                    " Depth="+ depth+ " colorspace="+ decodeColorData);

        /**
         * allow user to process image
         */
        if(customImageHandler!=null)
            image= customImageHandler.processImageData(gs,XObject);

        /**
         * fix for add case where image blank and actual image on SMask
         * (customer-June2011/10664.pdf)
         */
        PdfObject newSMask=XObject.getDictionary(PdfDictionary.SMask);
        byte[] index=decodeColorData.getIndexedMap();
        if(newSMask!=null && index!=null && index.length==3){ //swap out the image with inverted SMask if empty

            XObject=newSMask;
            XObject.setFloatArray(PdfDictionary.Decode, new float[]{1,0});
            objectData = currentPdfFile.readStream(XObject, true, true, false, false, false, null);

            depth=1;
            width = XObject.getInt(PdfDictionary.Width);
            height = XObject.getInt(PdfDictionary.Height);
            newDepth = XObject.getInt(PdfDictionary.BitsPerComponent);
            if(newDepth!=PdfDictionary.Unknown)
                depth=newDepth;

            decodeColorData = setupXObjectColorspace(XObject, depth, width, height, objectData);
        }

        //extract and process the image
        if(customImageHandler==null ||(image==null && !customImageHandler.alwaysIgnoreGenericHandler()))
            image= processImage(decodeColorData,
                    objectData,
                    image_name,
                    width,
                    height,
                    depth,
                    imageMask,
                    XObject, saveRawData);


        //add details to string so we can pass back
        if(trackImages && image!=null && details!=null){

            //work out effective dpi
            float dpi = gs.CTM[0][0];
            if(dpi ==0)
                dpi = gs.CTM[0][1];
            if(dpi <0)
                dpi =-dpi;

            dpi =(int)(width/dpi*100);

            //add details to string
            StringBuffer imageInfo=new StringBuffer(details);
            imageInfo.append(" w=");
            imageInfo.append(String.valueOf(width));
            imageInfo.append(" h=");
            imageInfo.append(String.valueOf(height));
            imageInfo.append(' ');
            imageInfo.append(String.valueOf((int)dpi));
            imageInfo.append(' ');
            imageInfo.append(ColorSpaces.IDtoString(decodeColorData.getID()));

            imageInfo.append(" (");
            imageInfo.append(String.valueOf(image.getWidth()));
            imageInfo.append(' ');
            imageInfo.append(String.valueOf(image.getHeight()));
            imageInfo.append(" type=");
            imageInfo.append(String.valueOf(image.getType()));
            imageInfo.append(")");

            if(imagesInFile.length()==0)
                imagesInFile=imageInfo.toString();
            else {
                imageInfo.append('\n');
                imageInfo.append(imagesInFile);
                imagesInFile=imageInfo.toString();
            }

        }

        return image;



    }

    /**
     * process image in XObject (XForm handled in PdfStreamDecoder)
     */
    public int processImage(int dataPointer,PdfObject XObject) throws PdfException {

        if(!layerDecoder.isLayerVisible() || !isVisible(XObject) || XObject==null)
            return dataPointer;

        String name=parser.generateOpAsString(0, true);

        //name is not unique if in form so we add form level to separate out
        if(formLevel>0)
            name= formName+'_'+ formLevel+'_'+name;

        //set if we need
        String key = null;
        if (rejectSuperimposedImages) {
            key = ((int) gs.CTM[2][0]) + "-" + ((int) gs.CTM[2][1]) + '-'
                    + ((int) gs.CTM[0][0]) + '-' + ((int) gs.CTM[1][1]) + '-'
                    + ((int) gs.CTM[0][1]) + '-' + ((int) gs.CTM[1][0]);
        }

        try {
            processXImage(name, name, key, XObject);
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

        return dataPointer;

    }

    private void processXImage(String name, String details, String key, PdfObject XObject) throws PdfException {

        int previousUse = -1;

        if(trackImages){
            details=details+" Image";
            if(imagesInFile==null)
                imagesInFile="";
        }

        /**don't process unless needed*/
        if (renderImages || finalImagesExtracted || clippedImagesExtracted || rawImagesExtracted) {

            //read stream for image
            byte[] objectData = null;
            if (previousUse == -1) {

                objectData = currentPdfFile.readStream(XObject, true, true, false, false, false, XObject.getCacheName(currentPdfFile.getObjectReader()));

                //flag issue
                if(objectData==null)
                    imagesProcessedFully=false;
            }

            if (objectData != null || previousUse > 0) {

                boolean alreadyCached = false;//(useHiResImageForDisplay && current.isImageCached(this.pageNum));

                BufferedImage image = null;

                //generate name including filename to make it unique less /
                setImageName(fileName + '-' + name);

                //process the image and save raw version
                if (!alreadyCached && previousUse == -1) {

                    //last flag change from true to false to fix issue
                    image = processImageXObject(XObject, name, objectData, true, details);

                }

                //fix for oddity in Annotation
                if (image != null && image.getWidth() == 1 && image.getHeight() == 1 && isType3Font) {
                    image.flush();
                    image = null;
                }

                if (PdfDecoder.debugHiRes) {
                    System.out.println("final=" + image);
                }

                //save transformed image
                if (image != null || alreadyCached || previousUse > 0) {

                    //manipulate CTM to allow for image truncated
                    float[][] savedCMT = null;

                    if (renderDirectly || useHiResImageForDisplay || previousUse > 0) {

                        if (previousUse > 0 && PdfDecoder.clipOnMac && PdfDecoder.isRunningOnMac && !alreadyCached)
                            image = clipForMac(image);

                        gs.x = gs.CTM[2][0];
                        gs.y = gs.CTM[2][1];

                        if(renderDirectly){ //in own bit as other code not needed
                            current.drawImage(pageNum, image, gs, alreadyCached, name, getOptionsApplied(), -1);
                        }else if (image != null || alreadyCached || previousUse > 0) {

                            int id = current.drawImage(pageNum, image, gs, alreadyCached, name, getOptionsApplied(),previousUse);

                            /**
                             * delete previous used if this not transparent
                             */
                            /**
                             * ignore multiple overlapping images
                             */
                            if (rejectSuperimposedImages && image != null && image.getType() != BufferedImage.TYPE_INT_ARGB) {

                                if (cache.imposedImages == null)
                                    cache.imposedImages = new HashMap();

                                //delete under image....
                                Object lastRef = cache.imposedImages.get(key);
                                if (lastRef != null && gs.getClippingShape() == null)  //limit to avoid issues on other files
                                    current.flagImageDeleted(((Integer) lastRef).intValue());
                            }

                            /**
                             * store last usage in case it reappears unless it is transparent
                             */
                            if (rejectSuperimposedImages && key != null && cache.imposedImages != null)
                                cache.imposedImages.put(key, new Integer(id));
                        }
                    } else {

                        if (clippedImagesExtracted) {
                            generateTransformedImage(image, name);
                        } else {
                            try {
                                generateTransformedImageSingle(image, name);
                            } catch (Exception e) {

                                if(LogWriter.isOutput())
                                    LogWriter.writeLog("Exception " + e + " on transforming image in file");
                            }
                        }
                    }

                    if (image != null)
                        image.flush();

                    //restore
                    if (savedCMT != null)
                        gs.CTM = savedCMT;
                }
            }
        }
    }
}
