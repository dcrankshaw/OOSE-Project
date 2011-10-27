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

  * IDImageDecoder.java
  * ---------------
  * (C) Copyright 2007, by IDRsolutions and Contributors.
  *
  *
  * --------------------------
 */
package org.jpedal.parser;

import org.jpedal.PdfDecoder;
import org.jpedal.color.*;
import org.jpedal.constants.PDFImageProcessing;
import org.jpedal.constants.PDFflags;
import org.jpedal.exception.PdfException;
import org.jpedal.external.ImageHandler;
import org.jpedal.external.Options;
import org.jpedal.function.FunctionFactory;
import org.jpedal.function.PDFFunction;
import org.jpedal.images.ImageOps;
import org.jpedal.images.ImageTransformer;
import org.jpedal.images.ImageTransformerDouble;
import org.jpedal.images.SamplingFactory;
import org.jpedal.io.*;
import org.jpedal.objects.GraphicsState;
import org.jpedal.objects.PdfImageData;
import org.jpedal.objects.PdfPageData;
import org.jpedal.objects.raw.*;
import org.jpedal.render.RenderUtils;
import org.jpedal.utils.LogWriter;
import org.jpedal.utils.Matrix;

import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.image.*;

public class IDImageDecoder extends BaseDecoder implements ImageDecoder{

    int mode=ImageDecoder.XOBJECT;

    public static boolean trackImages=false;

    PdfImageData pdfImages=null;

    private boolean getSamplingOnly=false;

    /**flag to show if image transparent*/
    boolean isMask=true;

    String imagesInFile=null;

    boolean isPrinting;

    ImageHandler customImageHandler;

    boolean useHiResImageForDisplay;

    boolean isType3Font;

    boolean renderDirectly;

    int formLevel;

    PdfPageData pageData;

    ObjectStore objectStoreStreamRef;

    /**flag to show raw images extracted*/
    boolean clippedImagesExtracted=true;

    private boolean extractRawCMYK=false;

    /**flag to show raw images extracted*/
    boolean finalImagesExtracted=true;

    private boolean doNotRotate=false;

    //only show message once
    private static boolean JAImessageShow=false;

    /**flag to show if we physical generate a scaled version of the
     * images extracted*/
    boolean createScaledVersion = true;

    /**flag to show content is being rendered*/
    boolean renderImages=false;

    /**flag to show raw images extracted*/
    boolean rawImagesExtracted=true;

    //used internally to show optimisations
    private int optionsApplied= PDFImageProcessing.NOTHING;

    /**name of current image in pdf*/
    private String currentImage = "";

    private static boolean sharpenDownsampledImages=false;


    static{

        //hidden value to turn on function
        String imgSetting=System.getProperty("org.jpedal.trackImages");
        if(imgSetting!=null)
            IDImageDecoder.trackImages=(imgSetting!=null && imgSetting.toLowerCase().indexOf("true")!=-1);

        String nodownsamplesharpen=System.getProperty("org.jpedal.sharpendownsampledimages");
        if(nodownsamplesharpen!=null)
            sharpenDownsampledImages=(nodownsamplesharpen.toLowerCase().indexOf("true")!=-1);

    }

    private int startInlineStream=0;

    IDImageDecoder() {
    }

    public IDImageDecoder(int startInlineStream, boolean isType3Font, ImageHandler customImageHandler, boolean useHiResImageForDisplay, ObjectStore objectStoreStreamRef, boolean renderDirectly, PdfImageData pdfImages, int formLevel, PdfPageData pageData, String imagesInFile) {

        this.mode=ImageDecoder.ID;

        this.isType3Font=isType3Font;
        this.customImageHandler=customImageHandler;
        this.useHiResImageForDisplay=useHiResImageForDisplay;
        this.objectStoreStreamRef=objectStoreStreamRef;
        this.renderDirectly=renderDirectly;

        this.pdfImages=pdfImages;
        this.formLevel=formLevel;
        this.pageData=pageData;

        this.imagesInFile=imagesInFile;

        this.startInlineStream=startInlineStream;
    }

    public void setSamplingOnly(boolean getSamplingOnly){

        this.getSamplingOnly=getSamplingOnly;

    }

    public String getImagesInFile() {
        return this.imagesInFile;
    }

    public void setParameters(boolean isPageContent, boolean renderPage, int renderMode, int extractionMode, boolean isPrinting) {

        super.setParameters(isPageContent,renderPage, renderMode, extractionMode);

        this.isPrinting=isPrinting;

        renderImages=renderPage &&(renderMode & PdfDecoder.RENDERIMAGES )== PdfDecoder.RENDERIMAGES;

        finalImagesExtracted=(extractionMode & PdfDecoder.FINALIMAGES) == PdfDecoder.FINALIMAGES;

        extractRawCMYK=(extractionMode &PdfDecoder.CMYKIMAGES)==PdfDecoder.CMYKIMAGES;

        clippedImagesExtracted=(extractionMode &PdfDecoder.CLIPPEDIMAGES)==PdfDecoder.CLIPPEDIMAGES;

        rawImagesExtracted=(extractionMode & PdfDecoder.RAWIMAGES) == PdfDecoder.RAWIMAGES;

        createScaledVersion = finalImagesExtracted || renderImages;

    }

    public BufferedImage processImageXObject(PdfObject XObject, String image_name, byte[] objectData, boolean saveRawData, String details) throws PdfException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }


    public void setImageName(String currentImage) {
        this.currentImage =currentImage;
    }

    public int getOptionsApplied() {
        return optionsApplied;
    }





    public int processImage(int dataPointer,PdfObject XImageObject) throws Exception{

        byte[] stream=parser.getStream();

        /**
         * read Dictionary
         */
        PdfObject XObject=new XObject(PdfDictionary.ID);

        IDObjectDecoder objectDecoder=new IDObjectDecoder(currentPdfFile.getObjectReader());
        objectDecoder.setEndPt(dataPointer-2);

        objectDecoder.readDictionaryAsObject(XObject,startInlineStream,stream);

        BufferedImage image =   null;

        boolean inline_imageMask;

        //store pointer to current place in file
        int inline_start_pointer = dataPointer + 1,i_w = 0, i_h = 0, i_bpc = 0;

        //find end of stream
        int i = inline_start_pointer,streamLength=stream.length;

        //find end
        while (true) {
            //look for end EI

            //handle Pdflib variety
            if (streamLength-i>3 &&  stream[i + 1] == 69 && stream[i + 2] == 73 && stream[i+3] == 10)
                break;

            //general case
            if ((streamLength-i>3)&&(stream[i] == 32 || stream[i] == 10 || stream[i] == 13 ||  (stream[i+3] == 32 && stream[i+4] == 'Q'))
                    && (stream[i + 1] == 69)
                    && (stream[i + 2] == 73)
                    && ( stream[i+3] == 32 || stream[i+3] == 10 || stream[i+3] == 13))
                break;



            i++;

            if(i==streamLength)
                break;
        }

        if(layerDecoder.isLayerVisible() && renderImages || finalImagesExtracted || clippedImagesExtracted || rawImagesExtracted){

            //load the data
            //		generate the name including file name to make it unique
            String image_name =fileName+ "-IN-" + tokenNumber;

            int endPtr=i;
            //hack for odd files
            if(i<stream.length && stream[endPtr] != 32 && stream[endPtr] != 10 && stream[endPtr] != 13)
                endPtr++;

            //correct data (ie idoq/FC1100000021259.pdf )
            if(stream[inline_start_pointer]==10)
                inline_start_pointer++;

            /**
             * put image data in array
             */
            byte[] i_data = new byte[endPtr - inline_start_pointer];
            System.arraycopy(
                    stream,
                    inline_start_pointer,
                    i_data,
                    0,
                    endPtr - inline_start_pointer);

            //System.out.print(">>");
            //for(int ss=inline_start_pointer-5;ss<endPtr+15;ss++)
            //System.out.print((char)stream[ss]);
            //System.out.println("<<"+i_data.length+" end="+endPtr);
            //pass in image data
            XObject.setStream(i_data);

            /**
             * work out colorspace
             */
            PdfObject ColorSpace=XObject.getDictionary(PdfDictionary.ColorSpace);

            //check for Named value
            if(ColorSpace!=null){
                String colKey=ColorSpace.getGeneralStringValue();

                if(colKey!=null){
                    Object col=cache.get(PdfObjectCache.Colorspaces,colKey);

                    if(col!=null)
                        ColorSpace=(PdfObject) col;
                    else{
                        //throw new RuntimeException("error with "+colKey+" on ID "+colorspaces);
                    }
                }
            }

            if(ColorSpace!=null && ColorSpace.getParameterConstant(PdfDictionary.ColorSpace)==PdfDictionary.Unknown)
                ColorSpace=null; //no values set

            /**
             * allow user to process image
             */
            if(customImageHandler!=null)
                image=customImageHandler.processImageData(gs,XObject);

            PdfArrayIterator filters = XObject.getMixedArray(PdfDictionary.Filter);

            //check not handled elsewhere
            int firstValue=PdfDictionary.Unknown;
            boolean needsDecoding=false;
            if(filters!=null && filters.hasMoreTokens()){
                firstValue=filters.getNextValueAsConstant(false);

                needsDecoding=(firstValue!= PdfFilteredReader.JPXDecode && firstValue!=PdfFilteredReader.DCTDecode);
            }

            i_w=XObject.getInt(PdfDictionary.Width);
            i_h=XObject.getInt(PdfDictionary.Height);
            i_bpc=XObject.getInt(PdfDictionary.BitsPerComponent);
            inline_imageMask=XObject.getBoolean(PdfDictionary.ImageMask);

            //handle filters (JPXDecode/DCT decode is handle by process image)
            if(needsDecoding){
                PdfFilteredReader filter=new PdfFilteredReader();
                i_data=filter.decodeFilters(ObjectUtils.setupDecodeParms(XObject, currentPdfFile.getObjectReader()), i_data, filters, i_w, i_h, null);
            }

            //handle colour information
            GenericColorSpace decodeColorData=new DeviceRGBColorSpace();
            if(ColorSpace!=null){
                decodeColorData= ColorspaceFactory.getColorSpaceInstance(currentPdfFile, ColorSpace);
                decodeColorData.setPrinting(isPrinting);

                //track colorspace use
                cache.put(PdfObjectCache.ColorspacesUsed,new Integer(decodeColorData.getID()).intValue(),"x");

                //use alternate as preference if CMYK
                //if(newColorSpace.getID()==ColorSpaces.ICC && ColorSpace.getParameterConstant(PdfDictionary.Alternate)==ColorSpaces.DeviceCMYK)
                //newColorSpace=new DeviceCMYKColorSpace();
            }
            if(i_data!=null){

                if(customImageHandler==null ||(image==null && !customImageHandler.alwaysIgnoreGenericHandler())){


                    image=processImage(decodeColorData,
                            i_data,
                            image_name,
                            i_w,
                            i_h,
                            i_bpc,
                            inline_imageMask,
                            XObject,false);

                    //generate name including filename to make it unique
                    currentImage = image_name;

                }

                //skip if smaller than zero as work around for JPS bug
                if(isPrinting && image!=null && gs!=null && image.getHeight()==1
                        && gs.CTM[1][1]<1){
                    image=null;
                }

                if (image != null){
                    if(renderDirectly || this.useHiResImageForDisplay){

                        gs.x=gs.CTM[2][0];
                        gs.y=gs.CTM[2][1];

                        current.drawImage(pageNum, image, gs, false, image_name, optionsApplied, -1);
                    }else{
                        if(clippedImagesExtracted)
                            generateTransformedImage(image,image_name);
                        else
                            generateTransformedImageSingle(image, image_name);
                    }

                    if(image!=null)
                        image.flush();
                }
            }
        }

        dataPointer = i + 3;

        return dataPointer;

    }

    /**
     * save the current image, clipping and
     *  resizing. This gives us a
     * clipped hires copy. In reparse, we don't
     * need to repeat some actions we know already done.
     */
    public void generateTransformedImage(BufferedImage image,String image_name) {

        float x = 0, y = 0, w = 0, h = 0;

        //if valid image then process
        if ((image != null)) {

            /**
             * scale the raw image to correct page size (at 72dpi)
             */

            //object to scale and clip. Creating instance does the scaling
            ImageTransformerDouble image_transformation =new ImageTransformerDouble(PdfDecoder.dpi,gs,image,createScaledVersion,true);

            //extract images either scaled/clipped or scaled then clipped

            image_transformation.doubleScaleTransformShear(false);

            //get intermediate image and save
            image = image_transformation.getImage();

            //save the scaled/clipped version of image if allowed
            {//if(currentPdfFile.isExtractionAllowed()){

                /**make sure the right way*/
                /*
               int dx=1,dy=1,iw=0,ih=0;
               if(currentGraphicsState.CTM[0][0]<0){
                   dx=-dx;
                   iw=image.getWidth();
               }

               if(currentGraphicsState.CTM[1][1]<0){
                   dy=-dy;
                   ih=image.getHeight();
               }
               if((dy<0)|(dx<0)){

                   AffineTransform image_at =new AffineTransform();
                   image_at.scale(dx,dy);
                   image_at.translate(-iw,-ih);
                   AffineTransformOp invert= new AffineTransformOp(image_at,  ColorSpaces.hints);
                   image = invert.filter(image,null);

               }

                */

                String image_type = objectStoreStreamRef.getImageType(currentImage);
                if(image_type==null)
                    image_type="tif";

                if (PdfDecoder.inDemo) {
                    Graphics2D g2 = image.createGraphics();
                    g2.setColor(Color.red);
                    int cw = image.getWidth();
                    int ch = image.getHeight();
                    g2.drawLine(0, 0, cw, ch);
                    g2.drawLine(0, ch, cw, 0);
                }

                // <start-me>
                if(objectStoreStreamRef.saveStoredImage(
                        "CLIP_"+currentImage,
                        addBackgroundToMask(image),
                        false,
                        false,
                        image_type))
                    errorTracker.addPageFailureMessage("Problem saving " + image);
                // <end-me>

            }

            if(finalImagesExtracted || renderImages)
                image_transformation.doubleScaleTransformScale();

            //complete the image and workout co-ordinates
            image_transformation.completeImage();

            //get initial values
            x = image_transformation.getImageX();
            y = image_transformation.getImageY();
            w = image_transformation.getImageW();
            h = image_transformation.getImageH();

            //get final image to allow for way we draw 'upside down'
            image = image_transformation.getImage();

            image_transformation = null; //flush

            //allow for null image returned (ie if too small)
            if (image != null) {

                //store  final image on disk & in memory
                if(renderImages || finalImagesExtracted || clippedImagesExtracted || rawImagesExtracted){
                    pdfImages.setImageInfo(currentImage, pageNum, x, y, w, h,null);
                }

                //add to screen being drawn
                if ((renderImages || !isPageContent) && image != null) {
                    gs.x=x;
                    gs.y=y;
                    current.drawImage(pageNum,image,gs,false,image_name,optionsApplied, -1);
                }

                /**save if required*/
                if(!renderDirectly && isPageContent && finalImagesExtracted) {

                    if (PdfDecoder.inDemo) {
                        Graphics2D g2 = image.createGraphics();
                        g2.setColor(Color.red);
                        int cw = image.getWidth();
                        int ch = image.getHeight();
                        g2.drawLine(0, 0, cw, ch);
                        g2.drawLine(0, ch, cw, 0);
                    }

                    // <start-me>
                    //save the scaled/clipped version of image if allowed
                    if(isExtractionAllowed()){
                        String image_type = objectStoreStreamRef.getImageType(currentImage);
                        objectStoreStreamRef.saveStoredImage(
                                currentImage,
                                addBackgroundToMask(image),
                                false,
                                false,
                                image_type);
                    }
                    // <end-me>
                }

            }
        } else if(LogWriter.isOutput())
            //flag no image and reset clip
            LogWriter.writeLog("NO image written");

    }

    private boolean isExtractionAllowed() {

        PdfFileReader objectReader=currentPdfFile.getObjectReader();

        DecryptionFactory decryption=objectReader.getDecryptionObject();

        return decryption==null || decryption.getBooleanValue(PDFflags.IS_EXTRACTION_ALLOWED);

    }

    private BufferedImage addBackgroundToMask(BufferedImage image) {

        if(isMask){

            int cw = image.getWidth();
            int ch = image.getHeight();

            BufferedImage background=new BufferedImage(cw,ch,BufferedImage.TYPE_INT_RGB);
            Graphics2D g2 = background.createGraphics();
            g2.setColor(Color.white);
            g2.fillRect(0, 0, cw, ch);
            g2.drawImage(image,0,0,null);
            image=background;

        }
        return image;
    }

    /**
     * save the current image, clipping and resizing. Id reparse, we don't
     * need to repeat some actions we know already done.
     */
    public void generateTransformedImageSingle(BufferedImage image,String image_name) {

        float x = 0, y = 0, w = 0, h = 0;

        //if valid image then process
        if (image != null) {

            // get clipped image and co-ords
            Area clipping_shape = gs.getClippingShape();

            /**
             * scale the raw image to correct page size (at 72dpi)
             */
            //object to scale and clip. Creating instance does the scaling
            ImageTransformer image_transformation;

            //object to scale and clip. Creating instance does the scaling
            image_transformation =new ImageTransformer(PdfDecoder.dpi,gs,image,true,PdfDecoder.isDraft);

            //get initial values
            x = image_transformation.getImageX();
            y = image_transformation.getImageY();
            w = image_transformation.getImageW();
            h = image_transformation.getImageH();

            //get back image, which will become null if TOO small
            image = image_transformation.getImage();

            //apply clip as well if exists and not inline image
            if (image != null && customImageHandler!=null && clipping_shape != null && clipping_shape.getBounds().getWidth()>1 &&
                    clipping_shape.getBounds().getHeight()>1 && !customImageHandler.imageHasBeenScaled()) {

                //see if clip is wider than image and ignore if so
                boolean ignore_image = clipping_shape.contains(x, y, w, h);

                if (!ignore_image) {
                    //do the clipping
                    image_transformation.clipImage(clipping_shape);

                    //get ALTERED values
                    x = image_transformation.getImageX();
                    y = image_transformation.getImageY();
                    w = image_transformation.getImageW();
                    h = image_transformation.getImageH();
                }
            }

            //alter image to allow for way we draw 'upside down'
            image = image_transformation.getImage();

            image_transformation = null; //flush

            //allow for null image returned (ie if too small)
            if (image != null) {

                /**turn correct way round if needed*/
                //if((currentGraphicsState.CTM[0][1]!=0 )&&(currentGraphicsState.CTM[1][0]!=0 )&&(currentGraphicsState.CTM[0][0]>=0 )){

                /*if((currentGraphicsState.CTM[0][1]>0 )&&(currentGraphicsState.CTM[1][0]>0 )&&(currentGraphicsState.CTM[0][0]>=0 )){
                       double dx=1,dy=1,scaleX=0,scaleY=0;

                       if(currentGraphicsState.CTM[0][1]>0){
                           dx=-1;
                           scaleX=image.getWidth();
                       }
                       if(currentGraphicsState.CTM[1][0]>0){
                           dy=-1;
                           scaleY=image.getHeight();
                       }

                       AffineTransform image_at =new AffineTransform();
                       image_at.scale(dx,dy);
                       image_at.translate(-scaleX,-scaleY);
                       AffineTransformOp invert= new AffineTransformOp(image_at,  ColorSpaces.hints);
                       image = invert.filter(image,null);


                   }
                */
                //store  final image on disk & in memory
                if(finalImagesExtracted || rawImagesExtracted){
                    pdfImages.setImageInfo(currentImage, pageNum, x, y, w, h,null);

                    //					if(includeImagesInData){
                    //
                    //						float xx=x;
                    //						float yy=y;
                    //
                    //						if(clipping_shape!=null){
                    //
                    //							int minX=(int)clipping_shape.getBounds().getMinX();
                    //							int maxX=(int)clipping_shape.getBounds().getMaxX();
                    //
                    //							int minY=(int)clipping_shape.getBounds().getMinY();
                    //							int maxY=(int)clipping_shape.getBounds().getMaxY();
                    //
                    //							if((xx>0 && xx<minX)||(xx<0))
                    //								xx=minX;
                    //
                    //							float currentW=xx+w;
                    //							if(xx<0)
                    //								currentW=w;
                    //							if(maxX<(currentW))
                    //								w=maxX-xx;
                    //
                    //							if(yy>0 && yy<minY)
                    //								yy=minY;
                    //
                    //							if(maxY<(yy+h))
                    //								h=maxY-yy;
                    //
                    //						}
                    //
                    //						pdfData.addImageElement(xx,yy,w,h,currentImage);
                    //					}
                }
                //add to screen being drawn
                if (renderImages || !isPageContent) {

                    //check it is not null
                    if (image != null) {
                        gs.x=x;
                        gs.y=y;
                        current.drawImage(pageNum,image,gs,false,image_name, optionsApplied, -1);

                    }
                }

                // <start-me>
                /**save if required*/
                if(isPageContent && finalImagesExtracted) {


                    if (PdfDecoder.inDemo) {
                        int cw = image.getWidth();
                        int ch = image.getHeight();

                        Graphics2D g2 = image.createGraphics();
                        g2.setColor(Color.red);
                        g2.drawLine(0, 0, cw, ch);
                        g2.drawLine(0, ch, cw, 0);
                    }

                    //save the scaled/clipped version of image if allowed
                    if(isExtractionAllowed()){// && !PdfStreamDecoder.runningStoryPad){

                        String image_type = objectStoreStreamRef.getImageType(currentImage);
                        objectStoreStreamRef.saveStoredImage(
                                currentImage,
                                addBackgroundToMask(image),
                                false,
                                false,
                                image_type);
                    }
                }
                // <end-me>
            }
        } else if(LogWriter.isOutput())
            //flag no image and reset clip
            LogWriter.writeLog("NO image written");


    }

    /**
     * read in the image and process and save raw image
     */
    BufferedImage processImage(GenericColorSpace decodeColorData,
                               byte[] data,String name,
                               int w,int h,int d,boolean imageMask,
                               PdfObject XObject, boolean saveRawData ) throws PdfException {

        //track its use
        cache.put(PdfObjectCache.ColorspacesUsed,new Integer(decodeColorData.getID()).intValue(),"x");

        int rawd=d;

        int sampling=1,newW=0,newH=0;

        float[] decodeArray=XObject.getFloatArray(PdfDictionary.Decode);

        if (LogWriter.debug && decodeArray!=null){
            String val="";
            for(int jj=0;jj<decodeArray.length;jj++)
                val=val+ ' '+decodeArray[jj];
        }

        PdfArrayIterator Filters = XObject.getMixedArray(PdfDictionary.Filter);

        boolean isDCT=false,isJPX=false;
        //check not handled elsewhere
        int firstValue=PdfDictionary.Unknown;
        if(Filters!=null && Filters.hasMoreTokens()){
            while(Filters.hasMoreTokens()){
                firstValue=Filters.getNextValueAsConstant(true);
                isDCT=firstValue==PdfFilteredReader.DCTDecode;
                isJPX=firstValue==PdfFilteredReader.JPXDecode;
            }

        }else
            Filters=null;

        boolean removed=false, isDownsampled=false;

        BufferedImage image = null;
        String type = "jpg";

        int colorspaceID=decodeColorData.getID();

        int compCount = decodeColorData.getColorSpace().getNumComponents();

        int pX=0,pY=0;

        /**setup any imageMask*/
        byte[] maskCol =new byte[4];
        if (imageMask)
            getMaskColor(maskCol);

        byte[] index=decodeColorData.getIndexedMap();

        /**setup sub-sampling*/
        if(renderPage && streamType!= ValueTypes.PATTERN){

            if(isPrinting && SamplingFactory.isPrintDownsampleEnabled && w<4000){
                pX=(int)(pageData.getCropBoxWidth(this.pageNum)*4);
                pY=(int)(pageData.getCropBoxHeight(this.pageNum)*4 );

            }else if(SamplingFactory.downsampleLevel== SamplingFactory.high || getSamplingOnly){// && w>500 && h>500){ // ignore small items

                //ensure all positive for comparison
                float[][] CTM=new float[3][3];
                for(int ii=0;ii<3;ii++){
                    for(int jj=0;jj<3;jj++){
                        if(gs.CTM[ii][jj]<0)
                            CTM[ii][jj]=-gs.CTM[ii][jj];
                        else
                            CTM[ii][jj]=gs.CTM[ii][jj];
                    }
                }

                if(CTM[0][0]==0 || CTM[0][0]<CTM[0][1])
                    pX=(int) (CTM[0][1]);
                else
                    pX=(int) (CTM[0][0]);

                if(CTM[1][1]==0 || CTM[1][1]<CTM[1][0])
                    pY=(int) (CTM[1][0]);
                else
                    pY=(int) (CTM[1][1]);


                //don't bother on small itemsS
                if(!getSamplingOnly &&(w<500 || (h<600 && (w<1000 || isJPX)))){ //change??


                    pX=0;//pageData.getCropBoxWidth(this.pageNum);
                    pY=0;//pageData.getCropBoxHeight(this.pageNum);
                }

            }else if(SamplingFactory.downsampleLevel==SamplingFactory.medium){
                pX=pageData.getCropBoxWidth(this.pageNum);
                pY=pageData.getCropBoxHeight(this.pageNum);
            }
        }



        //needs to be factored in or images poor on hires modes
        if((isDCT || isJPX) && multiplyer>1){

            pX= (int) (pX* multiplyer);
            pY= (int) (pY* multiplyer);
        }


        if(PdfDecoder.debugHiRes)
            System.out.println("pX="+pX+" pY="+pY+" w="+w+" h="+h+" *PdfDecoder.multiplyer="+multiplyer);

        PdfObject DecodeParms=XObject.getDictionary(PdfDictionary.DecodeParms), newMask=null, newSMask=null;

        newMask=XObject.getDictionary(PdfDictionary.Mask);
        newSMask=XObject.getDictionary(PdfDictionary.SMask);


        //avoid for scanned text
        if(d==1 && (newSMask!=null || XObject.getObjectType()!=PdfDictionary.Mask) &&
                decodeColorData.getID()== ColorSpaces.DeviceGray && h<300){

            //System.out.println("XObject="+XObject.getObjectType());
            //System.out.println("newSMask="+newSMask);
            pX=0;
            pY=0;
        }

        //flag masks
        if((newMask!=null || newSMask!=null) && LogWriter.isOutput())
            LogWriter.writeLog("newMask= "+ newMask + " newSMask="+ newSMask);

        //work out if inverted (assume true and disprove)
        //work this into saved data @mariusz so 125% works
        boolean arrayInverted=false;
        if(decodeArray!=null){

            arrayInverted=true;
            int count=decodeArray.length;
            for(int aa=0;aa<count;aa=aa+2){
                if(decodeArray[aa]==1f && decodeArray[aa+1]==0f){
                    //okay
                }else{
                    arrayInverted=false;
                    aa=count;
                }
            }
        }

        /**
         * also needs to be inverted in this case
         * see Customers3/Architectural_Specification.pdf page 31 onwards
         * 20100816 - no longer seems needed and removed by MS to fix abacus file (see Rog email 13th august 2010)
         */
        //if(!arrayInverted && decodeColorData.getID()==ColorSpaces.DeviceGray && index!=null){// && index[0]==-1 && index[1]==-1 && index[2]==-1){
        //    arrayInverted=true;
        //}

        /**
         * down-sample size if displaying (some cases excluded at present)
         */
        if(renderPage &&
                newMask==null &&
                decodeColorData.getID()!=ColorSpaces.ICC &&
                (arrayInverted || decodeArray==null || decodeArray.length==0)&&
                (d==1 || d==8)
                && pX>0 && pY>0 && (SamplingFactory.isPrintDownsampleEnabled || !isPrinting)){
            //			@speed - debug

            //see what we could reduce to and still be big enough for page
            newW=w;
            newH=h;

            //limit size (allow bigger grayscale
            if(multiplyer<=1 && !isPrinting){

                int maxAllowed=1000;
                if(decodeColorData.getID()==ColorSpaces.DeviceGray){
                    maxAllowed=4000;
                }
                if(pX>maxAllowed)
                    pX=maxAllowed;
                if(pY>maxAllowed)
                    pY=maxAllowed;
            }

            int smallestH=pY<<2; //double so comparison works
            int smallestW=pX<<2;

            //cannot be smaller than page
            while(newW>smallestW && newH>smallestH){
                sampling=sampling<<1;
                newW=newW>>1;
                newH=newH>>1;
            }

            int scaleX=w/pX;
            if(scaleX<1)
                scaleX=1;

            int scaleY=h/pY;
            if(scaleY<1)
                scaleY=1;

            //choose smaller value so at least size of page
            sampling=scaleX;
            if(sampling>scaleY)
                sampling=scaleY;

        }


        //get sampling and exit from this code as we don't need to go further
        if(getSamplingOnly){// && pX>0 && pY>0){

            float scaleX=(((float)w)/pX);
            float scaleY=(((float)h)/pY);

            if(PdfDecoder.debugHiRes)
                System.out.println("sampling="+sampling+ ' '+scaleX+ ' '+scaleY);

            if(scaleX<scaleY){
                samplingUsed=scaleX;
            }else{
                samplingUsed=scaleY;
            }
            //we may need to check mask as well

            boolean checkMask=false;
            if(newSMask!=null){

                /**read the stream*/
                byte[] objectData =currentPdfFile.readStream(newSMask,true,true,false, false,false, newSMask.getCacheName(currentPdfFile.getObjectReader()));

                if(objectData!=null){

                    if(DecodeParms==null)
                        DecodeParms=newSMask.getDictionary(PdfDictionary.DecodeParms);

                    int maskW=newSMask.getInt(PdfDictionary.Width);
                    int maskH=newSMask.getInt(PdfDictionary.Height);

                    //if all white image with mask, use mask
                    boolean isDownscaled=maskW/2>w && maskH/2>h;

                    boolean ignoreMask=isDownscaled &&
                            DecodeParms!=null &&
                            DecodeParms.getInt(PdfDictionary.Colors)!=-1 &&
                            DecodeParms.getInt(PdfDictionary.Predictor)!=15;


                    //ignoreMask is hack to fix odd Visuality files
                    if(!ignoreMask)
                        checkMask=true;
                }

            }

            if(!checkMask){

                //getSamplingOnly=false;

                return null;
            }
        }

        if(PdfDecoder.debugHiRes)
            System.out.println("sampling = "+sampling);

        {


            if(sampling>1 && multiplyer>1){

                //samplingUsed= sampling;

                sampling = (int) (sampling/ multiplyer);

                if(PdfDecoder.debugHiRes)
                    System.out.println("reset sampling to "+sampling);
            }


            //switch to 8 bit and reduce bw image size by averaging
            if(sampling>1){

                isDownsampled=true;

                newW=w/sampling;
                newH=h/sampling;

                if(PdfDecoder.debugHiRes)
                    System.out.println("width="+w+ ' '+newW+"sampling="+sampling+" d="+d);

                boolean saveData=false;
                //flatten out high res raw data in this case so we can store and resample (see deebug3/DOC002.PDF and DOC003.PDF
                if(imageMask && w>2000  && h>2000 && d==1 && decodeColorData.getID()==ColorSpaces.DeviceRGB && gs.CTM[0][0]>0 && gs.CTM[1][1]>0){
                    saveData=true;
                }

                if(d==1 && (decodeColorData.getID()!=ColorSpaces.DeviceRGB || index==null)){

                    //save raw 1 bit data
                    //code in DynamicVectorRenderer may need alignment if it changes
                    //20090929 - re-enabled by Mark with deviceGray limit for Abacus files
                    //breaks if form rotated so only use at top level
                    // (sample file breaks so we added this as hack for fattura 451-10 del 31.10.10.pdf in customers3)
                    if(formLevel<2 && (saveData ||(!imageMask && saveRawData && decodeColorData.getID()==ColorSpaces.DeviceGray ))){

                        //copy and turn upside down first
                        int count=data.length;

                        byte[] turnedData=new byte[count];
                        System.arraycopy(data,0,turnedData,0,count);

                        //						turnedData=ImageOps.invertImage(turnedData, w, h, d, 1, null);

                        boolean isInverted=!saveData && !doNotRotate && (renderDirectly || useHiResImageForDisplay) && RenderUtils.isInverted(gs.CTM);
                        boolean isRotated=!saveData && !doNotRotate && (renderDirectly || useHiResImageForDisplay) && RenderUtils.isRotated(gs.CTM);

                        if(renderDirectly){
                            isInverted=false;
                            isRotated=false;
                        }

                        Integer pn = new Integer(this.pageNum);
                        Integer iC = new Integer(imageCount);
                        String key = pn.toString() + iC.toString();

                        if(isRotated){ //rotate at byte level with copy New Code still some issues
                            turnedData= ImageOps.rotateImage(turnedData, w, h, d, 1, index);

                            //reset
                            int temp = h;
                            h=w;
                            w=temp;

                            temp = pX;
                            pX=pY;
                            pY=temp;

                        }

                        if(isInverted)//invert at byte level with copy
                            turnedData=ImageOps.invertImage(turnedData, w, h, d, 1, index);


                        //invert all the bits if needed before we store
                        if(arrayInverted){
                            for(int aa=0;aa<count;aa++)
                                turnedData[aa]= (byte) (turnedData[aa]^255);
                        }

                        if((w<4000 && h<4000) || decodeColorData.getID()==ColorSpaces.DeviceGray){ //limit added after silly sizes on Customers3/1773_A2.pdf
                            if(saveData){
                                current.getObjectStore().saveRawImageData(key,turnedData,w,h,pX,pY,maskCol,decodeColorData.getID());
                            }else{
                                current.getObjectStore().saveRawImageData(key,turnedData,w,h,pX,pY,null,decodeColorData.getID());
                            }
                        }

                        if(isRotated){
                            //reset
                            int temp = h;
                            h=w;
                            w=temp;

                            temp = pX;
                            pX=pY;
                            pY=temp;
                        }
                    }

                    //make 1 bit indexed flat
                    if(index!=null)
                        index=decodeColorData.convertIndexToRGB(index);

                    int size=newW*newH;

                    if(imageMask){
                        size=size*4;
                        maskCol[3]=(byte)255;
                    }else if(index!=null)
                        size=size*3;

                    byte[] newData=new byte[size];

                    final int[] flag={1,2,4,8,16,32,64,128};

                    int origLineLength= (w+7)>>3;

                    int bit;
                    byte currentByte;

                    //scan all pixels and down-sample
                    for(int y=0;y<newH;y++){
                        for(int x=0;x<newW;x++){

                            int bytes=0,count=0;

                            //allow for edges in number of pixels left
                            int wCount=sampling,hCount=sampling;
                            int wGapLeft=w-x;
                            int hGapLeft=h-y;
                            if(wCount>wGapLeft)
                                wCount=wGapLeft;
                            if(hCount>hGapLeft)
                                hCount=hGapLeft;

                            //count pixels in sample we will make into a pixel (ie 2x2 is 4 pixels , 4x4 is 16 pixels)
                            for(int yy=0;yy<hCount;yy++){
                                for(int xx=0;xx<wCount;xx++){

                                    currentByte=data[((yy+(y*sampling))*origLineLength)+(((x*sampling)+xx)>>3)];


                                    if(imageMask && !arrayInverted)
                                        currentByte=(byte) (currentByte ^ 255);

                                    bit=currentByte & flag[7-(((x*sampling)+xx)& 7)];

                                    if(bit!=0)
                                        bytes++;
                                    count++;
                                }
                            }

                            //set value as white or average of pixels
                            int offset=x+(newW*y);

                            if(count>0){
                                if(imageMask){
                                    //System.out.println("xx");
                                    for(int ii=0;ii<4;ii++){
                                        if(arrayInverted)
                                            newData[(offset*4)+ii]=(byte)(255-(((maskCol[ii] & 255)*bytes)/count));
                                        else
                                            newData[(offset*4)+ii]=(byte)((((maskCol[ii] & 255)*bytes)/count));
                                        //System.out.println(newData[(offset*4)+ii]+" "+(byte)(((maskCol[ii] & 255)*bytes)/count);

                                    }
                                }else if(index!=null && decodeColorData.getID()==ColorSpaces.Separation){


                                    for(int ii=0;ii<3;ii++)
                                        if((bytes/count)>0.5f){
                                            newData[(offset*3)+ii]=(byte)(((index[3+ii] & 255)));
                                        }else{
                                            newData[(offset*3)+ii]=(byte)(((index[ii] & 255)));

                                        }

                                }else if(index!=null && d==1){
                                    int av;

                                    for(int ii=0;ii<3;ii++){
                                        av=(index[ii] & 255) +(index[ii+3] & 255);
                                        //can be in either order so look at index
                                        if((decodeColorData.getID()==ColorSpaces.DeviceRGB || decodeColorData.getID()==ColorSpaces.DeviceGray) &&
                                                index[0]==-1 && index[1]==-1 && index[2]==-1){
                                            newData[(offset*3)+ii]=(byte)(255-((av *bytes)/count));
                                        }else{
                                            newData[(offset*3)+ii]=(byte)((av *bytes)/count);
                                        }
                                    }
                                }else if(index!=null){
                                    for(int ii=0;ii<3;ii++)
                                        newData[(offset*3)+ii]=(byte)(((index[ii] & 255)*bytes)/count);
                                }else
                                    newData[offset]=(byte)((255*bytes)/count);
                            }else{

                                if(imageMask){
                                    for(int ii=0;ii<3;ii++)
                                        newData[(offset*4)+ii]=(byte)0;

                                }else if(index!=null){
                                    for(int ii=0;ii<3;ii++)
                                        newData[((offset)*3)+ii]=0;
                                }else
                                    newData[offset]=(byte) 255;
                            }
                        }
                    }

                    data=newData;

                    if(index!=null)
                        compCount=3;

                    h=newH;
                    w=newW;
                    decodeColorData.setIndex(null, 0);

                    //remap Separation as already converted here
                    if(decodeColorData.getID()==ColorSpaces.Separation){
                        decodeColorData=new DeviceRGBColorSpace();

                        //needs to have these settings if 1 bit not indexed
                        if(d==1 && index==null){
                            compCount=1;

                            int count=data.length;
                            for(int aa=0;aa<count;aa++)
                                data[aa]= (byte) (data[aa]^255);
                        }
                    }

                    d=8;

                    //imageMask=false;

                }else if(d==8 && (Filters==null || (!isDCT && !isJPX))){

                    boolean hasIndex=decodeColorData.getIndexedMap()!=null && (decodeColorData.getID()==ColorSpaces.DeviceRGB || decodeColorData.getID()==ColorSpaces.CalRGB || decodeColorData.getID()==ColorSpaces.ICC);

                    int oldSize=data.length;


                    int x=0,y=0,xx=0,yy=0,jj=0,comp=0,origLineLength=0,indexCount=1;
                    try{

                        if(hasIndex){ //convert to sRGB
                            comp=1;

                            compCount=3;
                            indexCount=3;
                            index=decodeColorData.convertIndexToRGB(index);

                            decodeColorData.setIndex(null,0);
                        }else{
                            comp=decodeColorData.getColorComponentCount();
                        }

                        //black and white
                        if(w*h==oldSize || decodeColorData.getID()==ColorSpaces.DeviceGray)
                            comp=1;


                        byte[] newData;
                        if(hasIndex){ //hard-coded to 3 values
                            newData=new byte[newW*newH*indexCount];
                            origLineLength= w;
                        }else{
                            newData=new byte[newW*newH*comp];
                            origLineLength= w*comp;
                        }
                        //System.err.println(w+" "+h+" "+data.length+" comp="+comp+" scaling="+sampling+" "+decodeColorData);


                        //System.err.println("size="+w*h*comp+" filter"+filter+" scaling="+sampling+" comp="+comp);
                        //System.err.println("w="+w+" h="+h+" data="+data.length+" origLineLength="+origLineLength+" sampling="+sampling);
                        //scan all pixels and down-sample
                        for(y=0;y<newH;y++){
                            for(x=0;x<newW;x++){

                                //allow for edges in number of pixels left
                                int wCount=sampling,hCount=sampling;
                                int wGapLeft=w-x;
                                int hGapLeft=h-y;
                                if(wCount>wGapLeft)
                                    wCount=wGapLeft;
                                if(hCount>hGapLeft)
                                    hCount=hGapLeft;

                                for(jj=0;jj<comp;jj++){
                                    int byteTotal=0,count=0,ptr,newPtr;
                                    int[] indexAv=new int[indexCount];
                                    //count pixels in sample we will make into a pixel (ie 2x2 is 4 pixels , 4x4 is 16 pixels)
                                    for(yy=0;yy<hCount;yy++){
                                        for(xx=0;xx<wCount;xx++){

                                            ptr=((yy+(y*sampling))*origLineLength)+(((x*sampling*comp)+(xx*comp)+jj));
                                            if(ptr<oldSize){
                                                if(!hasIndex){
                                                    byteTotal=byteTotal+(data[ptr] & 255);
                                                }else{
                                                    for(int aa=0;aa<indexCount;aa++)
                                                        indexAv[aa]=indexAv[aa]+(index[(((data[ptr] & 255)*indexCount)+aa)] & 255);

                                                }

                                                count++;
                                            }
                                        }
                                    }

                                    //set value as white or average of pixels
                                    if(hasIndex){
                                        newPtr=jj+(x*indexCount)+(newW*y*indexCount);
                                        for(int aa=0;aa<indexCount;aa++)
                                            newData[newPtr+aa]=(byte)((indexAv[aa])/count);
                                    }else if(count>0){
                                        newPtr=jj+(x*comp)+(newW*y*comp);
                                        newData[newPtr]=(byte)((byteTotal)/count);
                                    }
                                }
                            }
                        }

                        data=newData;
                        h=newH;
                        w=newW;

                    }catch(Exception e){

                    }
                }else if(!isDCT && !isJPX && index==null){
                }
            }
        }

        /**handle any decode array*/
        if(decodeArray==null || decodeArray.length == 0){
        }else if(Filters!=null &&(isJPX||isDCT)){ //don't apply on jpegs
        }else
            applyDecodeArray(data, d, decodeArray,colorspaceID);

        if (imageMask) {/** create an image from the raw data*/

            //allow for 1 x 1 pixel
            /**
             * allow for 1 x 1 pixels scaled up or fine lines
             */
            float ratio=((float)h)/(float)w;

            if((isPrinting && ratio<0.1f && w>4000 && h>1) || (ratio<0.001f && w>4000 && h>1)  || (w==1 && h==1)){// && data[0]!=0){

                float ix=gs.CTM[2][0];
                float iy=gs.CTM[2][1];

                float ih=gs.CTM[1][1];
                if(ih==0)
                    ih=gs.CTM[1][0];
                if(ih<0){
                    iy=iy+ih;
                    ih=-ih;
                }

                float iw=gs.CTM[0][0];
                if(iw==0)
                    iw=gs.CTM[0][1];
                if(iw<0){
                    ix=ix+iw;
                    iw=-iw;
                }

                //factor in GS rotation and swap w and h
                if(gs.CTM[0][0]==0 && gs.CTM[0][1]>0 && gs.CTM[1][0]!=0 && gs.CTM[1][1]==0){
                    float tmp=ih;
                    ih=iw;
                    iw=tmp;
                }

                //allow for odd values less than 1 and ensure minimum width
                if(iw<1)
                    iw=1;
                if(ih<1)
                    ih=1;

                int lwidth=-1;

                //for thin lines, use line width to ensure appears
                if(ih<3){

                    lwidth=(int)ih;
                    ih=1;
                }else if(iw<3){
                    lwidth=(int)iw;
                    iw=1;
                }

                GeneralPath currentShape =new GeneralPath(GeneralPath.WIND_NON_ZERO);

                currentShape.moveTo(ix,iy);
                currentShape.lineTo(ix,iy+ih);
                currentShape.lineTo(ix+iw,iy+ih);
                currentShape.lineTo(ix+iw,iy);
                currentShape.closePath();

                //save for later
                if (renderPage && currentShape!=null){

                    float lastLineWidth=gs.getLineWidth();

                    if(lwidth>0)
                        gs.setLineWidth(lwidth);

                    gs.setNonstrokeColor(gs.nonstrokeColorSpace.getColor());
                    gs.setFillType(GraphicsState.FILL);

                    current.drawShape(currentShape,gs) ;

                    //restore after draw
                    if(lwidth>0)
                        gs.setLineWidth(lastLineWidth);

                }
                return null;

            }
            else if(h==2 && d==1 && isRepeatingLine(data, h)) {
                /* Takes account of ef1603e.pdf.  A thin horizontal dotted line is not scaled properly, therefore its converted to a shape. */
                /* Condition is only executed if line is uniform along vertical axis*/

                float ix = gs.CTM[2][0];
                float iy = gs.CTM[2][1];
                float ih=gs.CTM[1][1];
                float iw=gs.CTM[0][0];

                //factor in GS rotation and swap w and h
                if(gs.CTM[0][0]==0 && gs.CTM[0][1]>0 && gs.CTM[1][0]!=0 && gs.CTM[1][1]==0){
                    float tmp=ih;
                    ih=iw;
                    iw=tmp;
                }

                double byteWidth = iw / (data.length / h);
                double bitWidth = byteWidth / 8;

                for(int col = 0; col < data.length / h; col++) {
                    int currentByte = (int) data[col] & 0xff;
                    currentByte = ~currentByte & 0xff;

                    int bitCount = 8;
                    double endX = 0, startX = 0;
                    boolean draw = false;

                    while(currentByte != 0 || draw) {
                        bitCount--;

                        if((currentByte & 0x1) == 1) {
                            if(!draw) {
                                endX = ((bitCount + 0.5) * bitWidth) + (col * byteWidth);
                                draw = true;
                            }
                        }
                        else if(draw) {
                            draw = false;
                            startX = ((bitCount + 0.5) * bitWidth) + (col * byteWidth);
                            GeneralPath currentShape = new GeneralPath(GeneralPath.WIND_NON_ZERO);

                            currentShape.moveTo((float) (ix + startX), iy);
                            currentShape.lineTo((float) (ix + startX), iy + ih);
                            currentShape.lineTo((float) (ix + endX), iy + ih);
                            currentShape.lineTo((float) (ix + endX),iy);
                            currentShape.closePath();

                            //save for later
                            if (renderPage && currentShape!=null){
                                gs.setNonstrokeColor(gs.nonstrokeColorSpace.getColor());
                                gs.setFillType(GraphicsState.FILL);

                                current.drawShape(currentShape,gs) ;

                            }
                        }
                        currentByte = currentByte >>> 1;
                    }
                }
                return null;
            }
            else {

                //see if black and back object

                if(isDownsampled){
                    //<start-me>
                    /** create an image from the raw data*/
                    DataBuffer db = new DataBufferByte(data, data.length);

                    int[] bands = {0,1,2,3};
                    image =new BufferedImage(w,h,BufferedImage.TYPE_INT_ARGB);
                    Raster raster =Raster.createInterleavedRaster(db,w,h,w * 4,4,bands,null);
                    image.setData(raster);

                    // System.out.println("w="+w+" h="+h);
                    // ShowGUIMessage.showGUIMessage("x",image,"x");
                    //<end-me>
                }else{

                    //try to keep as binary if possible
                    boolean hasObjectBehind=true;

                    if(h<20)//added as found file with huge number of tiny tiles
                        hasObjectBehind=true;
                    else if(mode!=ImageDecoder.ID) //not worth it for inline image
                        hasObjectBehind=current.hasObjectsBehind(gs.CTM);


                    //remove empty images in some files
                    boolean isBlank=false;
                    if(imageMask && d==1 && decodeColorData.getID()==ColorSpaces.DeviceRGB && maskCol[0]==0 && maskCol[1]==0 && maskCol[2]==0){

                        //see if blank (assume true and disprove) and remove as totally see-through
                        isBlank=true;
                        for(int aa=0;aa<data.length;aa++){
                            if(data[aa]!=-1){
                                isBlank=false;
                                aa=data.length;
                            }
                        }

                        if(isBlank){
                            image=null;
                            removed=true;

                        }else{
                            byte[] newIndex={(maskCol[0]),(maskCol[1]), (maskCol[2]),(byte)255,(byte)255,(byte)255};
                            image = ColorSpaceConvertor.convertIndexedToFlat(d,w, h, data, newIndex,true,true);
                        }

                    }

                    if(isBlank){
                        //done above so ignore
                    }else if(!isPrinting && maskCol[0]==0 && maskCol[1]==0 && maskCol[2]==0 && !hasObjectBehind && !this.isType3Font && decodeColorData.getID()!=ColorSpaces.DeviceRGB){


                        //<start-me>
                        if(d==1){
                            WritableRaster raster =Raster.createPackedRaster(new DataBufferByte(data, data.length), w, h, 1, null);
                            image =new BufferedImage(w,h,BufferedImage.TYPE_BYTE_BINARY);
                            image.setData(raster);

                        }else{ //down-sampled above //never called
                            final int[] bands = {0};

                            //WritableRaster raster =Raster.createPackedRaster(new DataBufferByte(newData, newData.length), newW, newH, 1, null);
                            Raster raster =Raster.createInterleavedRaster(new DataBufferByte(data, data.length),w,h,w,1,bands,null);

                            image =new BufferedImage(w,h,BufferedImage.TYPE_BYTE_GRAY);
                            image.setData(raster);

                        }
                        //<end-me>

                    }else{

                        //if(hasObjectBehind){
                        //image=ColorSpaceConvertor.convertToARGB(image);
                        if(d==8 && isDownsampled){ //never called

                            byte[] newIndex={(maskCol[0]),(maskCol[1]), (maskCol[2]),(byte)255,(byte)255,(byte)255};
                            image = ColorSpaceConvertor.convertIndexedToFlat(d,w, h, data, newIndex, true,true);
                            // }else if(isType3Font){
                            //   WritableRaster raster =Raster.createPackedRaster(new DataBufferByte(data, data.length), w, h, 1, null);
                            // image =new BufferedImage(w,h,BufferedImage.TYPE_BYTE_BINARY);
                            //image.setData(raster);
                            // System.out.println(image.getType()+" "+image);
                        }else if((w<4000 && h<4000)|| hasObjectBehind){   //needed for hires
                            byte[] newIndex={maskCol[0],maskCol[1],maskCol[2],(byte)255,(byte)255,(byte)255};
                            image = ColorSpaceConvertor.convertIndexedToFlat(1,w, h, data, newIndex, true,false);
                            //}

                        }else{
                            //WritableRaster raster =Raster.createPackedRaster(new DataBufferByte(data, data.length), w, h, d, null);
                            //ismage = new BufferedImage(new IndexColorModel(d, 1, maskCol, 0, false), raster, false, null);
                            /**/
                        }
                    }
                }
            }
        }else if (Filters == null) { //handle no filters

            //save out image
            if(LogWriter.isOutput())
                LogWriter.writeLog("Image "+ name+ ' ' + w+ "W * "+ h+ "H with No Compression at BPC "+ d);

            image =makeImage(decodeColorData,w,h,d,data,compCount);

        } else if (isDCT) { //handle JPEGS

            if(LogWriter.isOutput())
                LogWriter.writeLog("JPeg Image "+ name+ ' ' + w+ "W * "+ h+ 'H'+" arrayInverted="+arrayInverted);

            /**
             * get image data,convert to BufferedImage from JPEG & save out
             */
            if(colorspaceID== ColorSpaces.DeviceCMYK && extractRawCMYK){

                if(LogWriter.isOutput())
                    LogWriter.writeLog("Raw CMYK image " + name + " saved.");

                if(!objectStoreStreamRef.saveRawCMYKImage(data, name))
                    errorTracker.addPageFailureMessage("Problem saving Raw CMYK image " + name);

            }

            /**
             try {
             java.io.FileOutputStream a =new java.io.FileOutputStream("/Users/markee/Desktop/"+ name + ".jpg");

             a.write(data);
             a.flush();
             a.close();

             } catch (Exception e) {
             LogWriter.writeLog("Unable to save jpeg " + name);

             }  /**/


            //if ICC with Alt RGB, use alternative
            if(decodeColorData.getID()==ColorSpaces.ICC){
                int alt=decodeColorData.getAlternateColorSpace();

                if(alt==ColorSpaces.DeviceRGB)
                    decodeColorData=new DeviceRGBColorSpace();
                else if(alt==ColorSpaces.DeviceCMYK)
                    decodeColorData=new DeviceCMYKColorSpace();
            }

            //separation, renderer
            try{
                image=decodeColorData.JPEGToRGBImage(data, w, h, decodeArray,pX , pY , arrayInverted);

                //flag if YCCK
                if(decodeColorData.isImageYCCK())
                    hasYCCKimages=true;

                removed=ColorSpaceConvertor.wasRemoved;

                //image=simulateOP(image);
            }catch(Exception e){
                errorTracker.addPageFailureMessage("Problem converting " + name + " to JPEG");
                e.printStackTrace();
                image.flush();
                image=null;
            }/**catch(Error err){
             addPageFailureMessage("Problem converting "+name+" to JPEG");
             //e.printStackTrace();
             image=null;
             }/**/

            type = "jpg";

            //set in makeImage so not set for JPEGS - we do it explicitly here
            setRotationOptionsOnJPEGImage();

        }else if(isJPX){ //needs imageio library

            if(LogWriter.isOutput())
                LogWriter.writeLog("JPeg 2000 Image "+ name+ ' ' + w+ "W * "+ h+ 'H');

            /**
             try {
             java.io.FileOutputStream a =new java.io.FileOutputStream("/Users/markee/Desktop/"+ name + ".jpg");

             a.write(data);
             a.flush();
             a.close();

             } catch (Exception e) {
             LogWriter.writeLog("Unable to save jpeg " + name);

             }  /**/

            if(JAIHelper.isJAIused()){

                image = decodeColorData.JPEG2000ToRGBImage(data,w,h,decodeArray,pX,pY);

                type = "jpg";
            }else{
                if(System.getProperty("org.jpedal.jai")!=null && System.getProperty("org.jpedal.jai").toLowerCase().equals("true")){
                    if(!JAImessageShow){
                        JAImessageShow=true;
                        System.err.println("JPeg 2000 Images need both JAI and imageio.jar on classpath");
                    }
                    throw new RuntimeException("JPeg 2000 Images need both JAI and imageio.jar on classpath");
                }else{
                    System.err.println("JPeg 2000 Images needs the VM parameter -Dorg.jpedal.jai=true switch turned on");
                    throw new RuntimeException("JPeg 2000 Images needs the VM parameter -Dorg.jpedal.jai=true switch turned on");
                }
            }

            //set in makeImage so not set for JPEGS - we do it explicitly here
            setRotationOptionsOnJPEGImage();

        } else { //handle other types

            if(LogWriter.isOutput())
                LogWriter.writeLog(name+ ' ' + w+ "W * "+ h+ "H BPC="+d+ ' '+decodeColorData);


            image =makeImage(decodeColorData,w,h,d,data,compCount);

            //choose type on basis of size and avoid ICC as they seem to crash the Java class
            if (d == 8 || gs.nonstrokeColorSpace.getID()== ColorSpaces.DeviceRGB || gs.nonstrokeColorSpace.getID()== ColorSpaces.ICC)
                type = "jpg";
        }

        if (image != null) {

            if(newSMask!=null && DecodeParms==null)
                DecodeParms=newSMask.getDictionary(PdfDictionary.DecodeParms);

            /**handle any soft mask*/
            if(newSMask!=null)
                image = addSMaskObject(decodeColorData, data, name, w, h,XObject, isDCT, isJPX, image, DecodeParms, newSMask);
            else if(newMask!=null)
                image = addMaskObject(decodeColorData, d, isDCT, isJPX, image,colorspaceID, index, newMask);

            image = simulateOverprint(decodeColorData, data, isDCT, isJPX,image, colorspaceID, newMask, newSMask);

            if(image==null)
                return null;

            //free up memory
            data = null;

            if(!renderDirectly)
            saveImage(name, createScaledVersion, image, type);

        }

        if(image == null && !removed){
            imagesProcessedFully=false;
        }

        //apply any transfer function
        PdfObject TR=gs.getTR();
        if(TR!=null) //array of values
            image=applyTR(image, TR);

        //try to simulate some of blend by removing white if not bottom image
        if(DecodeParms!=null  && DecodeParms.getInt(PdfDictionary.Blend)!=PdfDictionary.Unknown && current.hasObjectsBehind(gs.CTM))
            image= makeBlackandWhiteTransparent(image);

        //sharpen 1 bit
        if(pX>0 && pY>0 && rawd==1 && sharpenDownsampledImages && (decodeColorData.getID()==ColorSpaces.DeviceGray || decodeColorData.getID()==ColorSpaces.DeviceRGB)){

            Kernel kernel = new Kernel(3, 3,
                    new float[] {
                            -1, -1, -1,
                            -1, 9, -1,
                            -1, -1, -1});
            BufferedImageOp op = new ConvolveOp(kernel);
            image = op.filter(image, null);

        }

        //number of images used for caching
        imageCount++;

        return image;
    }

    /**
     * needs to be explicitly set for JPEG images if getting image from object
     */
    private void setRotationOptionsOnJPEGImage() {
        if(imageStatus>0 && gs.CTM[0][0]>0 && gs.CTM[0][1]>0 && gs.CTM[1][1]>0 && gs.CTM[1][0]<0){
/**/
            //we need a different op for Image and viewer as we handle in diff ways
            if(imageStatus==IMAGE_getImageFromPdfObject){
                //optionsApplied=optionsApplied+ PDFImageProcessing.IMAGE_INVERTED;
            	gs.CTM[0][1]=-gs.CTM[0][1];
            	//gs.CTM[1][0]=gs.CTM[1][0];
            	gs.CTM[1][1]=-gs.CTM[1][1];
            	gs.CTM[2][1]=gs.CTM[2][1]-gs.CTM[1][1];
            	
            }else if(imageStatus==SCREEN_getImageFromPdfObject)
                optionsApplied=optionsApplied+ PDFImageProcessing.IMAGE_INVERTED;
    /**/
        }
    }

    /**
     * make transparent
     */
    private static BufferedImage makeBlackandWhiteTransparent(BufferedImage image) {

        Raster ras=image.getRaster();

        int w=ras.getWidth();
        int h=ras.getHeight();

        //image=ColorSpaceConvertor.convertToARGB(image);
        BufferedImage newImage=new BufferedImage(w,h,BufferedImage.TYPE_INT_ARGB);

        boolean validPixelsFound=false;
        int[] values=new int[3];

        int[] transparentPixel={255,0,0,0};
        for(int y=0;y<h;y++){
            for(int x=0;x<w;x++){

                //get raw color data
                ras.getPixels(x,y,1,1,values);

                //see if white
                boolean transparent=(values[0]>245 && values[1]>245 && values[2]>245);
                boolean isBlack=(values[0]<10 && values[1]<10 && values[2]<10);


                //if it matched replace and move on
                if(transparent || isBlack) {
                    newImage.getRaster().setPixels(x,y,1,1,transparentPixel);
                }else{
                    validPixelsFound=true;

                    int[] newPixel=new int[4];

                    newPixel[3]=255;
                    newPixel[0]=values[0];
                    newPixel[1]=values[1];
                    newPixel[2]=values[2];

                    newImage.getRaster().setPixels(x,y,1,1,newPixel);
                }
            }
        }

        if(validPixelsFound)
            return newImage;
        else
            return null;

    }

    /**
     * add MASK to image
     */
    private static BufferedImage overlayImage(BufferedImage image, byte[] maskData, PdfObject newMask, boolean needsInversion) {

        image= ColorSpaceConvertor.convertToRGB(image);

        Raster ras=image.getRaster();

        int maskW=newMask.getInt(PdfDictionary.Width);
        int maskH=newMask.getInt(PdfDictionary.Height);

        int width=image.getWidth();
        int height=image.getHeight();

        boolean isScaled=(width!=maskW || height!=maskH);
        float scaling=0;

        if(isScaled){
            float scalingW=(float)width/(float)maskW;
            float scalingH=(float)height/(float)maskH;

            if(scalingW>scalingH)
                scaling=scalingW;
            else
                scaling=scalingH;
        }

        BufferedImage newImage=new BufferedImage(maskW, maskH, BufferedImage.TYPE_INT_ARGB);

        WritableRaster output=newImage.getRaster();

        //workout y offset (remember needs to be factor of 8)
        int lineBytes=maskW;
        if((lineBytes & 7)!=0)
            lineBytes=lineBytes+8;

        lineBytes=lineBytes>>3;

        int bytes=0,x,y;

        final int[] bit={128,64,32,16,8,4,2,1};

        for(int rawy=0;rawy<maskH;rawy++){

            if(isScaled){
                y=(int)(scaling*rawy);

                if(y>height)
                    y=height;
            }else
                y=rawy;

            boolean isTransparent;
            int xOffset;
            byte b;
            for(int rawx=0;rawx<maskW;rawx++){

                if(isScaled){
                    x=(int)(scaling*rawx);

                    if(x>width)
                        x=height;
                }else
                    x=rawx;

                xOffset=(rawx>>3);

                b=maskData[bytes+xOffset];

                //invert if needed
                if(needsInversion)
                    isTransparent=(b & bit[rawx & 7])==0;
                else
                    isTransparent=(b & bit[rawx & 7])!=0;

                //System.out.println("co-ords="+rawx+" "+rawy+" xOffset="+xOffset+" byte="+b+" bit="+bit[rawx & 7]+" isTransparent="+isTransparent);


                //<start-me>
                //if it matched replace and move on
                if(!isTransparent && x<width && y<height){
                    int[] values=new int[3];
                    values=ras.getPixel(x,y,values); //get pixel from data
                    output.setPixel(rawx,rawy,new int[]{values[0],values[1],values[2],255});
                    //output.setPixel(rawx,rawy,new int[]{255,0,0,255});

                }
                //<end-me>
            }

            bytes=bytes+lineBytes;

        }

        return newImage;
    }




    /**
     * apply TR
     */
    private BufferedImage applyTR(BufferedImage image,PdfObject TR) {

        /**
         * get TR function first
         **/
        PDFFunction[] functions =new PDFFunction[4];

        int total=0;

        byte[][] kidList = TR.getKeyArray(PdfDictionary.TR);

        if(kidList!=null)
            total=kidList.length;

        //get functions
        for(int count=0;count<total;count++){

            if(kidList[count]==null)
                continue;

            String ref=new String(kidList[count]);
            PdfObject Function=new FunctionObject(ref);

            //handle /Identity as null or read
            byte[] possIdent=kidList[count];
            if(possIdent!=null && possIdent.length>4 && possIdent[0]==47 &&  possIdent[1]==73 && possIdent[2]==100 &&  possIdent[3]==101)//(/Identity
                Function=null;
            else
                currentPdfFile.readObject(Function);

            /** setup the translation function */
            if(Function!=null)
                functions[count] = FunctionFactory.getFunction(Function, currentPdfFile);

        }

        /**
         * apply colour transform
         */
        Raster ras=image.getRaster();
        //image=ColorSpaceConvertor.convertToARGB(image);

        int[] values=new int[4];

        for(int y=0;y<image.getHeight();y++){
            for(int x=0;x<image.getWidth();x++){


                //get raw color data
                ras.getPixels(x,y,1,1,values);

                for(int a=0;a<3;a++){
                    float[] raw={values[a]/255f};

                    if(functions[a]!=null){
                        float[] processed=functions[a].compute(raw);

                        values[a]= (int) (255*processed[0]);
                    }
                }

                image.getRaster().setPixels(x,y,1,1,values);
            }
        }

        return image;

    }


    /**
     * apply DecodeArray
     */
    private static void applyDecodeArray(byte[] data, int d, float[] decodeArray,int type) {

        int count = decodeArray.length;

        int maxValue=0;
        for(int i=0;i<count;i++) {
            if(maxValue<decodeArray[i])
                maxValue=(int) decodeArray[i];
        }

        /**
         * see if will not change output
         * and ignore if unnecessary
         */
        boolean isIdentify=true; //assume true and disprove
        int compCount=decodeArray.length;

        for(int comp=0;comp<compCount;comp=comp+2){
            if((decodeArray[comp]!=0.0f)||((decodeArray[comp+1]!=1.0f)&&(decodeArray[comp+1]!=255.0f))){
                isIdentify=false;
                comp=compCount;
            }
        }

        if(isIdentify)
            return ;

        if(d==1){ //gray or bw straight switch

            int byteCount=data.length;
            for(int ii=0;ii<byteCount;ii++){
                data[ii]=(byte) ~data[ii];

            }
            /**
             * handle rgb
             */
        }else if((d==8 && maxValue>1)&&(type==ColorSpaces.DeviceRGB || type==ColorSpaces.CalRGB || type==ColorSpaces.DeviceCMYK)){

            int j=0;

            for(int ii=0;ii<data.length;ii++){
                int currentByte=(data[ii] & 0xff);
                if(currentByte<decodeArray[j])
                    currentByte=(int) decodeArray[j];
                else if(currentByte>decodeArray[j+1])
                    currentByte=(int)decodeArray[j+1];

                j=j+2;
                if(j==decodeArray.length)
                    j=0;
                data[ii]=(byte)currentByte;
            }
        }else{
            /**
             * apply array
             *
             * Assumes black and white or gray colorspace
             * */
            maxValue = (d<< 1);
            int divisor = maxValue - 1;

            for(int ii=0;ii<data.length;ii++){
                byte currentByte=data[ii];

                int dd=0;
                int newByte=0;
                int min=0,max=1;
                for(int bits=7;bits>-1;bits--){
                    int current=(currentByte >> bits) & 1;

                    current =(int)(decodeArray[min]+ (current* ((decodeArray[max] - decodeArray[min])/ (divisor))));

                    /**check in range and set*/
                    if (current > maxValue)
                        current = maxValue;
                    if (current < 0)
                        current = 0;

                    current=((current & 1)<<bits);

                    newByte=newByte+current;

                    //rotate around array
                    dd=dd+2;

                    if(dd==count){
                        dd=0;
                        min=0;
                        max=1;
                    }else{
                        min=min+2;
                        max=max+2;
                    }
                }

                data[ii]=(byte)newByte;

            }
        }
    }




    private void saveImage(String name, boolean createScaledVersion,
                           BufferedImage image, String type) {
        if (image!=null && image.getSampleModel().getNumBands() == 1)
            type = "tif";

        // <start-me>
        if(isPageContent &&(renderImages || finalImagesExtracted || clippedImagesExtracted || rawImagesExtracted)){

            //save the raw image or blank if demo or encryption enabled
            if (isExtractionAllowed()){

                if(PdfDecoder.inDemo){

                    int imageType=image.getType();
                    if(imageType==0)
                        imageType=BufferedImage.TYPE_INT_RGB;
                    BufferedImage newImage=new BufferedImage(image.getWidth(),image.getHeight(),imageType);
                    Graphics2D g2= newImage.createGraphics();
                    g2.drawImage(image,null,null);

                    int x=image.getWidth();
                    int y=image.getHeight();

                    //add demo cross
                    g2.setColor(Color.red);
                    g2.drawLine(0, 0, x,y);
                    g2.drawLine(0, y, x, 0);

                    objectStoreStreamRef.saveStoredImage(name,addBackgroundToMask(newImage),false,createScaledVersion,type);
                }else{
                    //if(!PdfStreamDecoder.runningStoryPad)
                    //    objectStoreStreamRef.saveStoredImage(name,image,false,createScaledVersion,type);
                    //else
                    objectStoreStreamRef.saveStoredImage(name,addBackgroundToMask(image),false,createScaledVersion,type);
                }
            }else{

                /**create copy and scale if required*/
                if(PdfDecoder.dpi!=72){

                    int imageType=image.getType();
                    if(imageType==0)
                        imageType=BufferedImage.TYPE_INT_RGB;
                    BufferedImage newImage=new BufferedImage(image.getWidth(),image.getHeight(),imageType);
                    newImage.createGraphics().drawImage(image,null,null);
                    float s=((float)PdfDecoder.dpi)/72;
                    AffineTransform scale = new AffineTransform();
                    scale.scale(s, s);
                    AffineTransformOp scalingOp =new AffineTransformOp(scale, ColorSpaces.hints);
                    newImage =scalingOp.filter(newImage, null);
                    objectStoreStreamRef.saveStoredImage(name,addBackgroundToMask(newImage),false,createScaledVersion,type);

                }else{
                    objectStoreStreamRef.saveStoredImage(name,addBackgroundToMask(image),false,createScaledVersion,type);
                }
            }
        }
        // <end-me>
    }




    /**
     * turn raw data into a BufferedImage
     */
    private BufferedImage makeImage(GenericColorSpace decodeColorData,int w,int h,int d,
                                    byte[] data,int comp) {

        //ensure correct size
        if(decodeColorData.getID()== ColorSpaces.DeviceGray && d==8){
            int requiredSize=w*h;
            int oldSize=data.length;
            if(oldSize<requiredSize){
                byte[] oldData=data;
                data=new byte[requiredSize];
                System.arraycopy(oldData,0,data,0,oldSize);
            }
        }

        /**
         * put data into separate array. If we keep in PdfData then on pages where same image reused
         * such as adobe/Capabilities and precisons, its flipped each time as its an object :-(
         */
        //int byteCount=rawData.length;
        //byte[] data=new byte[byteCount];
        //System.arraycopy(rawData, 0, data, 0, byteCount);


        ColorSpace cs=decodeColorData.getColorSpace();
        int ID=decodeColorData.getID();

        BufferedImage image = null;
        byte[] index =decodeColorData.getIndexedMap();

        optionsApplied=PDFImageProcessing.NOTHING;


        /**fast op on data to speed up image manipulation*/
        //optimse rotations here as MUCH faster and flag we have done this
        //something odd happens if CTM[2][1] is negative so factor ignore this case
        boolean isInverted=!doNotRotate && useHiResImageForDisplay && RenderUtils.isInverted(gs.CTM);
        boolean isRotated=!doNotRotate && useHiResImageForDisplay && RenderUtils.isRotated(gs.CTM);

        //This fix was masking miscalculation.
        //fix for image wrong on Customers3/ImageQualitySample_v0.2.docx.pdf
//        if(isInverted && gs.CTM[1][1]>0 && gs.lastCTM[1][1]<0)
//            isInverted=false;
//
//        if(renderDirectly && ! this.isType3Font){
//            isInverted=false;
//            isRotated=false;
//        }

        //I optimised the code slightly - you were setting booleans are they had been
        //used - I removed so it keeps code shorter

        if(isInverted){//invert at byte level with copy

            //needs to be 1 for sep
            int count=comp;
            if(ID==ColorSpaces.Separation)
                count=1;
            else if(ID==ColorSpaces.DeviceN)
                count=decodeColorData.getColorComponentCount();

            byte[] processedData= ImageOps.invertImage(data, w, h, d, count, index);

            if(processedData!=null){

                data=processedData;
                optionsApplied=optionsApplied+PDFImageProcessing.IMAGE_INVERTED;

            }
        }



        if(isRotated){ //rotate at byte level with copy New Code still some issues
            byte[] processedData=ImageOps.rotateImage(data, w, h, d, comp, index);

            if(processedData!=null){
                data=processedData;

                optionsApplied=optionsApplied+PDFImageProcessing.IMAGE_ROTATED;

                //reset
                int temp = h;
                h=w;
                w=temp;
            }
        }

        //data=ColorSpaceConvertor.convertIndexedToFlat(d,w, h, data, index, 255);

        //System.out.println("index="+index);

        if (index != null) { //indexed images

            if(LogWriter.isOutput())
                LogWriter.writeLog("Indexed "+w+ ' ' +h);


            /**convert index to rgb if CMYK or ICC*/
            if (comp == 4)
                comp=3;


            if(!decodeColorData.isIndexConverted()){
                index=decodeColorData.convertIndexToRGB(index);
            }

            //workout size and check in range
            int size =decodeColorData.getIndexSize()+1;

            //pick out draft setting of totally empty iamge and ignore
            if(d==8 && decodeColorData.getIndexSize()==0 && decodeColorData.getID()==ColorSpaces.DeviceRGB){

                boolean hasPixels=false;

                int indexCount=index.length;
                for(int ii=0;ii<indexCount;ii++){
                    if(index[ii]!=0){
                        hasPixels=true;
                        ii=indexCount;
                    }
                }

                if(!hasPixels){
                    int pixelCount=data.length;

                    for(int ii=0;ii<pixelCount;ii++){
                        if(data[ii]!=0){
                            hasPixels=true;
                            ii=pixelCount;
                        }
                    }
                }
                if(!hasPixels){
                    return new BufferedImage(1,1,BufferedImage.TYPE_INT_ARGB);
                }
            }
            //allow for half bytes (ie bootitng.pdf)
            if(d==4 && size>16)
                size=16;

            //			WritableRaster raster =Raster.createPackedRaster(db, w, h, d, null);

            //			ColorModel cm=new IndexColorModel(d, size, index, 0, false);
            //			image = new BufferedImage(cm, raster, false, null);

            //if(debugColor)
            //System.out.println("xx d="+d+" w="+w+" data="+data.length+" index="+index.length+" size="+size);

            try{
                if(d==1 && index.length==6 && index[0]== index[3] && index[1]== index[4] && index[2]== index[5]){
                    image=null;//remove image in Itext which is white on white
                }else
                    image = ColorSpaceConvertor.convertIndexedToFlat(d,w, h, data, index,false,false);
            }catch(Exception e){
                e.printStackTrace();
            }

            //if(debugColor)
            //throw new RuntimeException("xx");

        } else if (d == 1) { //bitmaps next

            image =new BufferedImage(w,h,BufferedImage.TYPE_BYTE_BINARY);
            // <start-me>
            /** create an image from the raw data*/
            DataBuffer db = new DataBufferByte(data, data.length);

            WritableRaster raster =Raster.createPackedRaster(db, w, h, d, null);
            /* <end-me>
            WritableRaster raster = image.getRaster();
            int[] intrgb = new int[data.length];
            for(int i=0;i<data.length;i++){
                intrgb[i] = (int)data[i];
            }
            raster.setPixels(0, 0, w, h, intrgb);
            /**/
            image.setData(raster);

        }else if(ID==ColorSpaces.Separation || ID==ColorSpaces.DeviceN){
            if(LogWriter.isOutput())
                LogWriter.writeLog("Converting Separation/DeviceN colorspace to sRGB ");

            image=decodeColorData.dataToRGB(data,w,h);

        }else if(ID==6){
            if(LogWriter.isOutput())
                LogWriter.writeLog("Converting lab colorspace to sRGB ");

            image=decodeColorData.dataToRGB(data,w,h);

            //direct images
        } else if (comp == 4) { //handle CMYK or ICC

            if(LogWriter.isOutput())
                LogWriter.writeLog("Converting ICC/CMYK colorspace to sRGB ");

            //ICC (note CMYK uses ICC so check which type and check enough data)
            if((ID==3)) //&((w*h*4)==data.length)) /**CMYK*/
                image =ColorSpaceConvertor.algorithmicConvertCMYKImageToRGB(data,w,h);
            else
                image =ColorSpaceConvertor.convertFromICCCMYK(w,h,data, cs);

            //ShowGUIMessage.showGUIMessage("y",image,"y");
        } else if (comp == 3) {

            if(LogWriter.isOutput())
                LogWriter.writeLog("Converting 3 comp colorspace to sRGB index="+index);

            //work out from size what sort of image data we have
            if (w * h == data.length) {
                if (d == 8 && index!=null){
                    image = ColorSpaceConvertor.convertIndexedToFlat(d,w, h, data, index, false,false);

                    //image =new BufferedImage(w,h,BufferedImage.TYPE_BYTE_INDEXED);

                    //WritableRaster raster =Raster.createPackedRaster(db,w,h,d,null);
                    //image.setData(raster);
                }else{

                    // <start-me>
                    /** create an image from the raw data*/
                    DataBuffer db = new DataBufferByte(data, data.length);

                    int[] bands = {0};

                    image =new BufferedImage(w,h,BufferedImage.TYPE_BYTE_GRAY);
                    Raster raster =Raster.createInterleavedRaster(db,w,h,w,1,bands,null);
                    /* <end-me>
                    WritableRaster raster = new java.awt.image.BufferedImage(w,h,java.awt.image.BufferedImage.TYPE_BYTE_INDEXED).getRaster();
                    int[] intrgb = new int[data.length];
                    for(int i=0;i<data.length;i++){
                        intrgb[i] = (int)data[i];
                    }
                    raster.setPixels(0, 0, w, h, intrgb);
                    /**/
                    image.setData(raster);

                }
            } else{

                if(LogWriter.isOutput())
                    LogWriter.writeLog("Converting data to sRGB ");

                //expand out 4 bit raster as does not appear to be easy way
                if(d==4){
                    int origSize=data.length;
                    int newSize=w*h*3;

                    byte[] newData=new byte[newSize];
                    byte rawByte;
                    int ptr=0,currentLine=0;
                    for(int ii=0;ii<origSize;ii++){
                        rawByte=data[ii];

                        currentLine=currentLine+2;
                        newData[ptr]=(byte) (rawByte & 240);
                        if(newData[ptr]==-16)   //fix for white
                            newData[ptr]=(byte)255;
                        ptr++;


                        newData[ptr]=(byte) ((rawByte & 15) <<4);
                        if(newData[ptr]==-16)  //fix for white
                            newData[ptr]=(byte)255;

                        ptr++;

                        if(ptr==newSize)
                            ii=origSize;
                    }
                    data=newData;

                }

                image =new BufferedImage(w,h,BufferedImage.TYPE_INT_RGB);

                data=ImageOps.checkSize(data,w,h,3);

                Raster raster = ColorSpaceConvertor.createInterleavedRaster(data, w, h);
                image.setData(raster);


            }
        } else if (comp == 1 &&(d == 8|| d==4)) {

            if(LogWriter.isOutput())
                LogWriter.writeLog("comp=1 and d= "+d);

            //expand out 4 bit raster as does not appear to be easy way
            if(d==4){
                int origSize=data.length;
                int newSize=w*h;

                byte[] newData=new byte[newSize];
                byte rawByte;
                int ptr=0,currentLine=0;
                boolean oddValues=((w & 1)==1);
                for(int ii=0;ii<origSize;ii++){
                    rawByte=data[ii];

                    currentLine=currentLine+2;
                    newData[ptr]=(byte) (rawByte & 240);
                    if(newData[ptr]==-16)   //fix for white
                        newData[ptr]=(byte)255;
                    ptr++;

                    if(oddValues && currentLine>w){ //ignore second value if odd as just packing
                        currentLine=0;
                    }else{
                        newData[ptr]=(byte) ((rawByte & 15) <<4);
                        if(newData[ptr]==-16)  //fix for white
                            newData[ptr]=(byte)255;
                        ptr++;
                    }

                    if(ptr==newSize)
                        ii=origSize;
                }
                data=newData;

            }

            // <start-me>
            /** create an image from the raw data*/
            DataBuffer db = new DataBufferByte(data, data.length);

            int[] bands ={0};
            image =new BufferedImage(w,h,BufferedImage.TYPE_BYTE_GRAY);
            Raster raster =Raster.createInterleavedRaster(db,w,h,w,1,bands,null);
            /* <end-me>
            image =new BufferedImage(w,h,BufferedImage.TYPE_BYTE_INDEXED);
            WritableRaster raster = image.getRaster();
            int[] intrgb = new int[data.length];
            for(int i=0;i<data.length;i++){
                intrgb[i] = (int)data[i];
            }
            raster.setPixels(0, 0, w, h, intrgb);
            /**/
            image.setData(raster);

        } else if(LogWriter.isOutput())
            LogWriter.writeLog("Image "+ cs.getType()+ " not currently supported with components "+ comp);

        //convert type 0 to rgb (as do work with other ops)
        //if (image.getType() == 0)
        //image = ColorSpaceConvertor.convertToRGB(image);

        return image;
    }

    /**
     * @param maskCol
     */
    private void getMaskColor(byte[] maskCol) {
        int foreground =gs.nonstrokeColorSpace.getColor().getRGB();
        maskCol[0]=(byte) ((foreground>>16) & 0xFF);
        maskCol[1]=(byte) ((foreground>>8) & 0xFF);
        maskCol[2]=(byte) ((foreground) & 0xFF);
    }

    /**
     * Test whether the data representing a line is uniform along it height
     */
    private static boolean isRepeatingLine(byte[] lineData, int height)
    {
        if(lineData.length % height != 0) return false;

        int step = lineData.length / height;

        for(int x = 0; x < (lineData.length / height) - 1; x++) {
            int targetIndex = step;
            while(targetIndex < lineData.length - 1) {
                if(lineData[x] != lineData[targetIndex]) {
                    return false;
                }
                targetIndex += step;
            }
        }
        return true;
    }



    /**
     * apply soft mask
     **/
    public static BufferedImage applySmask(BufferedImage image, BufferedImage smask,
                                           PdfObject newSMask, boolean isForm, boolean isRGB, PdfObject ColorSpace) {

        int imageType=image.getType();

        int[] gray={0};
        int[] val={0,0,0,0};
        int[] transparentPixel={0,0,0,0};

        //get type as need different handling
        PdfArrayIterator maskFilters = newSMask.getMixedArray(PdfDictionary.Filter);

        boolean maskIsDCT=false;//,maskIsJPX=false;

        int firstValue=PdfDictionary.Unknown;
        if(maskFilters!=null && maskFilters.hasMoreTokens()){
            while(maskFilters.hasMoreTokens()){
                firstValue=maskFilters.getNextValueAsConstant(true);
                maskIsDCT=firstValue==PdfFilteredReader.DCTDecode;
                //isJPX=firstValue==PdfFilteredReader.JPXDecode;
            }
        }

        boolean isDeviceGray=ColorSpace!=null && ColorSpace.getParameterConstant(PdfDictionary.ColorSpace)== ColorSpaces.DeviceGray;
        boolean needsConversion=(maskIsDCT) && isDeviceGray;

        int type=-1;
        if(ColorSpace!=null)
            type=ColorSpace.getParameterConstant(PdfDictionary.ColorSpace);

        needsConversion=!needsConversion && isForm && ColorSpace!=null && (type== ColorSpaces.DeviceCMYK || type== ColorSpaces.ICC);

        //fix for Smask encoded with DCTDecode but not JPX
        if(needsConversion){
            // <start-me>
            smask=ColorSpaceConvertor.convertColorspace(smask,BufferedImage.TYPE_BYTE_GRAY);
            /* <end-me>
            smask=ColorSpaceConvertor.convertColorspace(smask,BufferedImage.TYPE_BYTE_INDEXED);
            /**/
            val=gray;
        }

        Raster mask=smask.getRaster();
        WritableRaster imgRas=null;

        boolean isConverted=false;

        /**
         * allow for scaled mask
         */
        int imageW=image.getWidth();
        int imageH=image.getHeight();

        int smaskW=smask.getWidth();
        int smaskH=smask.getHeight();
        float ratioW=0,ratioH=0;

        if(imageW!=smaskW || imageH!=smaskH){
            ratioW=(float)imageW/(float)smaskW;
            ratioH=(float)imageH/(float)smaskH;

            //resize if half size to improve image quality on RGB
            if(isRGB && ratioW==0.5 && ratioH==0.5){

                BufferedImage resizedImage = new BufferedImage(smaskW, smaskH, image.getType());
                Graphics2D g = resizedImage.createGraphics();

                g.dispose();
                g.setComposite(AlphaComposite.Src);
                g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY);
                //g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);

                g.drawImage(image, 0, 0, smaskW, smaskH, null);

                image=resizedImage;

                imageW=smaskW;
                imageH=smaskH;
                ratioW=1;
                ratioH=1;
            }

        }

        int colorComponents=smask.getColorModel().getNumComponents();
        int[] values=new int[colorComponents];
        int[] pix=new int[4];


        //apply smask
        for(int y=0;y<imageH;y++){
            for(int x=0;x<imageW;x++){

                //get raw color data
                if(ratioW==0)
                    mask.getPixels(x,y,1,1,values);
                else
                    mask.getPixels((int)(x/ratioW),(int)(y/ratioH),1,1,values);

                //see if we have a match (assume it matches)
                boolean noMatch=false;

                //test assumption
                if(colorComponents==1){  //hack to filter out DCTDecode stream
                    if(values[0]>127 && maskIsDCT)
                        noMatch=true;
                }else{

                    for(int comp=0;comp<colorComponents;comp++){
                        if(values[comp]!=val[comp]){
                            comp=colorComponents;
                            noMatch=true;
                        }
                    }
                }

                //if it matched replace and move on
                if(!noMatch){
                    if(!isConverted){ // do it first time needed
                        image=ColorSpaceConvertor.convertToARGB(image);
                        imgRas=image.getRaster();
                        isConverted=true;
                    }

                    //handle 8bit gray, not DCT
                    if(colorComponents==1){

                        imgRas.getPixels(x,y,1,1,pix);

                        //remove what appears invisible in Acrobat
                        if(values[0]==pix[0]){//pix[0]>32 && pix[1]>32 && pix[2]>32 && values[0]<32)
                            if(pix[0]==255 && imageType==1 && isDeviceGray) { //white is different in diff Buffered images
                                imgRas.setPixels(x,y,1,1,new int[]{(pix[0]),(pix[1]),(pix[2]),values[0]});
                            }else
                                imgRas.setPixels(x,y,1,1,transparentPixel);


                        }else
                            imgRas.setPixels(x,y,1,1,new int[]{(pix[0]),(pix[1]),(pix[2]),values[0]});
                    }else
                       imgRas.setPixels(x,y,1,1,transparentPixel);
                }
            }
        }

        return image;
    }




    private BufferedImage simulateOverprint(GenericColorSpace decodeColorData,
                                            byte[] data, boolean isDCT, boolean isJPX, BufferedImage image,
                                            int colorspaceID, PdfObject newMask, PdfObject newSMask) {
        //simulate overPrint //currentGraphicsState.getNonStrokeOP() &&
        if(colorspaceID==ColorSpaces.DeviceCMYK && gs.getOPM()==1.0f){
            //if((colorspaceID==ColorSpaces.DeviceCMYK || colorspaceID==ColorSpaces.ICC) && gs.getOPM()==1.0f){

            //try to keep as binary if possible
            boolean hasObjectBehind=current.hasObjectsBehind(gs.CTM);
            boolean isBlank=false;

            //see if allblack
            if(hasObjectBehind){

                isBlank=true; //assume true and disprove
                for(int ii=0;ii<data.length;ii++){
                    if(data[ii]!=0){
                        ii=data.length;
                        isBlank=false;
                    }
                }
            }

            //if so reject
            if(isBlank){
                image.flush();
                image=null;
            }

            else if((isDCT || isJPX) && gs.getNonStrokeOP()){
                image=simulateOP(image,false);
            }else if(isDCT && newSMask==null &&  newMask==null && decodeColorData.isImageYCCK() && decodeColorData.getIntent()!=null && decodeColorData.getIntent().equals("RelativeColorimetric") ){
                image=simulateOP(image,true);
            }else if(gs.getNonStrokeOP())
                image=simulateOP(image,false);
            //}else if(decodeColorData.isIndexConverted())  //hack for odd file Randy-Binder1
            //      image=null;


        }
        return image;
    }





    private BufferedImage addSMaskObject(GenericColorSpace decodeColorData,
                                         byte[] data, String name, int w, int h, PdfObject XObject,
                                         boolean isDCT, boolean isJPX, BufferedImage image,
                                         PdfObject DecodeParms, PdfObject newSMask) throws PdfException {
        {

            BufferedImage smaskImage=null;

            /**read the stream*/
            byte[] objectData =currentPdfFile.readStream(newSMask,true,true,false, false,false, newSMask.getCacheName(currentPdfFile.getObjectReader()));

            if(objectData!=null){

                boolean ignoreMask=DecodeParms!=null &&  DecodeParms.getInt(PdfDictionary.Colors)!=-1
                        &&  DecodeParms.getInt(PdfDictionary.Predictor)!=15 && decodeColorData.getID()!=ColorSpaces.ICC;

                //special case
                PdfObject maskColorSpace=newSMask.getDictionary(PdfDictionary.ColorSpace);
                if(ignoreMask && (decodeColorData.getID()==ColorSpaces.DeviceRGB || decodeColorData.getID()==ColorSpaces.DeviceCMYK) && maskColorSpace.getParameterConstant(PdfDictionary.ColorSpace)==ColorSpaces.DeviceGray)
                    ignoreMask=false;

                //ignore in this case of blank Smask on jpeg with grayscale
                if(isDCT && maskColorSpace.getParameterConstant(PdfDictionary.ColorSpace)==ColorSpaces.DeviceGray){
                    int len=objectData.length;
                    ignoreMask=true;
                    for(int aa=0;aa<len;aa++){
                        if(objectData[aa]!=-1){
                            ignoreMask=false;
                            aa=len;
                        }
                    }
                }

                //ignoreMask is hack to fix odd Visuality files
                if(!ignoreMask){

                    int rawOptions=optionsApplied;

                    if(optionsApplied==PDFImageProcessing.NOTHING)
                        doNotRotate=true;

                    int maskW=newSMask.getInt(PdfDictionary.Width);
                    int maskH=newSMask.getInt(PdfDictionary.Height);

                    boolean isWhiteAndDownscaled=false;


                    boolean isIndexed=false;
                    if(isWhiteAndDownscaled){

                        PdfObject XObjectColorSpace=XObject.getDictionary(PdfDictionary.ColorSpace);
                        //PdfObject maskColorSpace=newSMask.getDictionary(PdfDictionary.ColorSpace);

                        PdfArrayIterator maskFilters = newSMask.getMixedArray(PdfDictionary.Filter);

                        boolean isJBIG2=false;

                        //only needed for this case
                        if(XObjectColorSpace.getParameterConstant(PdfDictionary.ColorSpace)== ColorSpaces.DeviceRGB){
                            int maskFirstValue=PdfDictionary.Unknown;
                            if(maskFilters!=null && maskFilters.hasMoreTokens()){
                                while(maskFilters.hasMoreTokens()){
                                    maskFirstValue=maskFilters.getNextValueAsConstant(true);
                                    isJBIG2=maskFirstValue== PdfFilteredReader.JBIG2Decode;
                                }
                            }
                        }

                        //special case customers3/si_test.pdf
                        isIndexed=data.length==2 && XObjectColorSpace.getParameterConstant(PdfDictionary.ColorSpace)== ColorSpaces.Indexed;

                        isWhiteAndDownscaled=XObjectColorSpace!=null &&
                                ((XObjectColorSpace.getParameterConstant(PdfDictionary.ColorSpace)== ColorSpaces.DeviceRGB && (!isDCT || isJBIG2)) ||
                                        (isIndexed && XObjectColorSpace.getDictionary(PdfDictionary.Indexed).getParameterConstant(PdfDictionary.ColorSpace)== ColorSpaces.DeviceRGB)) &&
                                maskColorSpace.getParameterConstant(PdfDictionary.ColorSpace)== ColorSpaces.DeviceGray;

                    }

                    if((isWhiteAndDownscaled  && (isDCT || isJPX || isIndexed))){

                        //invert and get image
                        int c=objectData.length;
                        for(int ii=0;ii<c;ii++)
                            objectData[ii]= (byte) (((byte)255)-objectData[ii]);

                        ImageDecoder imageDecoder= new XImageDecoder(isType3Font, customImageHandler, useHiResImageForDisplay,objectStoreStreamRef,renderDirectly,pdfImages, formLevel,pageData,null);

                        imageDecoder.setGS(gs);
                        imageDecoder.setIntValue(PageNumber, pageNum);
                        imageDecoder.setHandlerValue(Options.ErrorTracker,errorTracker);
                        imageDecoder.setRes(cache);
                        imageDecoder.setSamplingOnly(getSamplingOnly);
                        imageDecoder.setIntValue(ValueTypes.StreamType,streamType);
                        imageDecoder.setIntValue(ImageCount,imageCount);
                        imageDecoder.setName(fileName);
                        imageDecoder.setFloatValue(Multiplier, multiplyer);
                        imageDecoder.setFloatValue(SamplingUsed, samplingUsed);
                        imageDecoder.setFileHandler(currentPdfFile);
                        imageDecoder.setLayerValues(layers, layerDecoder);
                        imageDecoder.setRenderer(current);
                        imageDecoder.setParameters(isPageContent,renderPage, renderMode, extractionMode, isPrinting);

                        image = imageDecoder.processImageXObject(newSMask,name, objectData,true,null);

                        samplingUsed=imageDecoder.getFloatValue(SamplingUsed);

                        if(imageDecoder.getBooleanValue(HasYCCKimages))
                            hasYCCKimages=true;

                        if(imageDecoder.getBooleanValue(ImagesProcessedFully))
                            imagesProcessedFully=true;

                        imageCount=imageDecoder.getIntValue(ImageCount);

                    }else{

                        //process the image and save raw version
                        ImageDecoder imageDecoder= new XImageDecoder(isType3Font, customImageHandler, useHiResImageForDisplay,objectStoreStreamRef,renderDirectly, pdfImages, formLevel,pageData,null);

                        imageDecoder.setGS(gs);
                        imageDecoder.setIntValue(PageNumber, pageNum);
                        imageDecoder.setHandlerValue(Options.ErrorTracker,errorTracker);
                        imageDecoder.setRes(cache);
                        imageDecoder.setSamplingOnly(getSamplingOnly);
                        imageDecoder.setIntValue(ValueTypes.StreamType,streamType);
                        imageDecoder.setIntValue(ImageCount,imageCount);
                        imageDecoder.setName(fileName);
                        imageDecoder.setFloatValue(Multiplier, multiplyer);
                        imageDecoder.setFloatValue(SamplingUsed, samplingUsed);

                        imageDecoder.setFileHandler(currentPdfFile);

                        imageDecoder.setLayerValues(layers, layerDecoder);
                        imageDecoder.setRenderer(current);

                        imageDecoder.setParameters(isPageContent,renderPage, renderMode, extractionMode, isPrinting);

                        smaskImage = imageDecoder.processImageXObject(newSMask,name, objectData,true,null);

                        samplingUsed=imageDecoder.getFloatValue(SamplingUsed);

                        if(imageDecoder.getBooleanValue(HasYCCKimages))
                            hasYCCKimages=true;

                        if(imageDecoder.getBooleanValue(ImagesProcessedFully))
                            imagesProcessedFully=true;

                        imageCount=imageDecoder.getIntValue(ImageCount);

                        //restore
                        doNotRotate=false;
                        optionsApplied=rawOptions;

                        //apply mask
                        if(smaskImage!=null){
                            image=applySmask(image, smaskImage, newSMask, false, decodeColorData.getID() == ColorSpaces.DeviceRGB,newSMask.getDictionary(PdfDictionary.ColorSpace));
                            smaskImage.flush();
                            smaskImage=null;
                        }
                    }
                }
            }

            /**handle any mask*/
        }
        return image;
    }



    private BufferedImage addMaskObject(GenericColorSpace decodeColorData,
                                        int d, boolean isDCT, boolean isJPX, BufferedImage image,
                                        int colorspaceID, byte[] index, PdfObject newMask) {
        {

            int[] maskArray=newMask.getIntArray(PdfDictionary.Mask);

            //fix for odd file
            if(maskArray!=null && maskArray.length==2 && maskArray[0]==maskArray[1] && maskArray[0]>0 &&
                    index!=null && index[maskArray[0]]==0 &&
                    decodeColorData.getIndexedMap().length==768){

                //if(index!=null)
                //	System.out.println(maskArray[0]+" "+index[maskArray[0]]);

                maskArray=null;
            }


            //see if object or colors
            if(maskArray!=null){

                int colorComponents=decodeColorData.getColorComponentCount();
                //byte[] index=decodeColorData.getIndexedMap();

                if(index!=null){

                    int itemCount=maskArray.length,indexValue;
                    int[] newIndex=new int[colorComponents*itemCount];
                    for(int jj=0;jj<itemCount;jj++){
                        indexValue=maskArray[jj];
                        for(int i=0;i<colorComponents;i++)
                            newIndex[i+(jj*colorComponents)]=index[(indexValue*colorComponents)+i] & 255;
                    }

                    maskArray=newIndex;
                }

                image = convertPixelsToTransparent(image, maskArray);

            }else{

                byte[] objectData=currentPdfFile.readStream(newMask,true,true,false, false,false, newMask.getCacheName(currentPdfFile.getObjectReader()));

                int maskW=newMask.getInt(PdfDictionary.Width);
                int maskH=newMask.getInt(PdfDictionary.Height);

                //include Decode if present
                float[] maskDecodeArray=newMask.getFloatArray(PdfDictionary.Decode);

                if(maskDecodeArray!=null && (colorspaceID==ColorSpaces.DeviceRGB || colorspaceID==ColorSpaces.Separation))
                    applyDecodeArray(objectData, maskDecodeArray.length/2, maskDecodeArray,colorspaceID);


                /**fast op on data to speed up image manipulation*/
                int both=PDFImageProcessing.IMAGE_INVERTED+ PDFImageProcessing.IMAGE_ROTATED;

                if((optionsApplied & both)==both){
                    byte[] processedData=ImageOps.rotateImage(objectData, maskW, maskH, 1, 1, null);
                    if(processedData!=null){
                        int temp = maskH;
                        maskH=maskW;
                        maskW=temp;
                        processedData=ImageOps.rotateImage(processedData, maskW, maskH, d, 1, null);
                        if(processedData!=null){
                            temp = maskH;
                            maskH=maskW;
                            maskW=temp;
                        }
                    }

                    objectData=processedData;

                }else if((optionsApplied & PDFImageProcessing.IMAGE_INVERTED)==PDFImageProcessing.IMAGE_INVERTED){//invert at byte level with copy
                    objectData=ImageOps.invertImage(objectData, maskW, maskH, 1, 1, null);
                }

                if((optionsApplied & PDFImageProcessing.IMAGE_ROTATED)==PDFImageProcessing.IMAGE_ROTATED){ //rotate at byte level with copy New Code still some issues
                    objectData=ImageOps.rotateImage(objectData, maskW, maskH, 1, 1, null);
                }

                if(objectData!=null){

                    /**
                     * java stroes images in different ways so we need to work out which and handle differently
                     **/
                    boolean needsConversion=decodeColorData!=null && (decodeColorData.getID()== ColorSpaces.DeviceGray || decodeColorData.getID()== ColorSpaces.CalRGB) && !isJPX;
                    boolean isRGB=decodeColorData!=null && decodeColorData.getID()== ColorSpaces.DeviceRGB;

                    if((needsConversion && (decodeColorData.getID()== ColorSpaces.DeviceGray || decodeColorData.getID()== ColorSpaces.CalRGB)) ||
                            (!needsConversion && !isRGB && isDCT)){


                        PdfArrayIterator maskFilters = newMask.getMixedArray(PdfDictionary.Filter);

                        //get type as need different handling
                        boolean maskNeedsInversion =false;

                        int firstMaskValue=PdfDictionary.Unknown;
                        if(maskFilters!=null && maskFilters.hasMoreTokens()){
                            while(maskFilters.hasMoreTokens()){
                                firstMaskValue=maskFilters.getNextValueAsConstant(true);
                                maskNeedsInversion =firstMaskValue==PdfFilteredReader.CCITTFaxDecode || firstMaskValue==PdfFilteredReader.JBIG2Decode;
                            }
                        }

                        //need to invert value in this case to make it work
                        if(decodeColorData.getID()== ColorSpaces.CalRGB)
                            maskNeedsInversion=!maskNeedsInversion;

                        if(!maskNeedsInversion){
                            needsConversion=false;
                        }else if(needsConversion &&decodeColorData.getID()== ColorSpaces.DeviceGray){
                            needsConversion=false;
                        }

                        //needed to make this case work
                        if(maskDecodeArray!=null && decodeColorData.getID()== ColorSpaces.DeviceGray && maskDecodeArray[0]==1 && maskDecodeArray[1]==0)
                            needsConversion=!needsConversion;
                    }

                    image=overlayImage(image,objectData,newMask,needsConversion);
                }
            }
        }
        return image;
    }



    /**
     * add MASK to image by scanning all pixels and seeing if each colour in range
     */
    private static BufferedImage convertPixelsToTransparent(BufferedImage image, int[] maskArray) {

        //raster we read original pixels from
        Raster ras=image.getRaster();

        //number of colours in image
        int compCount=ras.getNumBands();

        //new image to add non-transparent pixels onto
        image=new BufferedImage(image.getWidth(),image.getHeight(),BufferedImage.TYPE_INT_ARGB);

        for(int y=0;y<image.getHeight();y++){
            for(int x=0;x<image.getWidth();x++){

                int[] values=new int[compCount];
                //<start-me>
                //get raw color data
                ras.getPixel(x,y,values);
                //<end-me>

                //assume true and see if false
                boolean isMatch=true;
                for(int aa=0;aa<compCount;aa++){
                    if(maskArray[2*aa]<=values[aa] && values[aa]<=maskArray[(2*aa)+1]){
                    }else{
                       isMatch=false;
                        aa=compCount;
                    }
                }


                //see if we do not have a match copy through
                if(!isMatch){

                    //<start-me>
                    if(compCount==1)
                        image.getRaster().setPixel(x,y,new int[]{values[0],values[0],values[0],255});
                    else
                        image.getRaster().setPixel(x,y,new int[]{values[0],values[1],values[2],255});
                    //<end-me>
                }
            }
        }

        return image;
    }

    /**
     * CMYK overprint mode
     */
    private static BufferedImage simulateOP(BufferedImage image, boolean isDCT) {

        Raster ras=image.getRaster();
        image=ColorSpaceConvertor.convertToARGB(image);
        int w=image.getWidth();
        int h=image.getHeight();

        boolean hasNoTransparent=false;// pixelsSet=false;

        //reset
        //minX=w;
        //minY=h;
        //maxX=-1;
        //maxY=-1;

        int[] transparentPixel={255,0,0,0};
        int[] values=new int[4];

        boolean transparent=false;

        for(int y=0;y<h;y++){
            for(int x=0;x<w;x++){

                //<start-me>
                //get raw color data
                ras.getPixel(x,y,values);
                //<end-me>

                //see if black
                if(isDCT){
                    transparent=values[0]>243 && values[1]>243 && values[2]>243;

                    // if(!transparent)
                    //     System.out.println(values[0]+" "+values[1]+" "+values[2]+" "+values[3]);

                }else{
                    transparent=values[1]<3 && values[2]<3 && values[3]<3;
                }

                //if it matched replace and move on
                if(transparent){
                    //<start-me>
                    image.getRaster().setPixel(x,y,transparentPixel);
                    //<end-me>
                }else{
                    hasNoTransparent=true;

                    //see if we can reduce in size by working out size needed
                    //                    if(minX>x)
                    //                    minX=x;
                    //                    if(maxX<x)
                    //                    maxX=x;
                    //                    if(minY>y)
                    //                    minY=y;
                    //                    if(maxY<y)
                    //                    maxY=y;
                    //                    pixelsSet=true;
                }
            }
        }

        if(hasNoTransparent){
            //trim to size
            //            if(pixelsSet && (minX>0 || minY>0)){
            //                try{
            //                    //System.out.println("before="+image);
            //                image=image.getSubimage(minX,minY,maxX-minX,maxY-minY);
            //                    //System.out.println("after="+image);
            //
            //                }catch(Exception ee){
            //                    ee.printStackTrace();
            //                }
            //            }
            return image;
        }else
            return null;

    }




}
