package org.jpedal.parser;

import org.jpedal.color.ColorSpaces;
import org.jpedal.io.PdfArray;
import org.jpedal.objects.GraphicsState;
import org.jpedal.objects.PdfShape;
import org.jpedal.objects.raw.PdfArrayIterator;
import org.jpedal.objects.raw.PdfDictionary;
import org.jpedal.objects.raw.PdfObject;

import java.awt.*;
import java.awt.geom.Area;

/**
 * ===========================================
 * Java Pdf Extraction Decoding Access Library
 * ===========================================
 * <p/>
 * Project Info:  http://www.jpedal.org
 * (C) Copyright 1997-2011, IDRsolutions and Contributors.
 * <p/>
 * This file is part of JPedal
 *
 *     This library is free software; you can redistribute it and/or
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


 * ---------------
 * ShapeDecoder.java
 * ---------------
 */
public class ShapeDecoder extends BaseDecoder {

    private PdfObject groupObj=null;

    /**current shape object being drawn note we pass in handle on pageLines*/
    private final PdfShape currentDrawShape=new PdfShape();

    /**flag to show if image is for clip*/
    private boolean isClip = false;


    public int processToken(int commandID, int dataPointer, boolean removeRenderImages) {

        switch(commandID){

            case Cmd.B :
                    if(!removeRenderImages)
                        B(false,false);
                    break;
                case Cmd.b :
                    if(!removeRenderImages)
                        B(false,true);
                    break;
                case Cmd.bstar :
                    if(!removeRenderImages)
                        B(true,true);
                    break;
                case Cmd.Bstar :
                    if(!removeRenderImages)
                        B(true,false);
                    break;

            case Cmd.c :
                    float x3 =parser.parseFloat(1);
                    float y3 = parser.parseFloat(0);
                    float x2 =parser.parseFloat(3);
                    float y2 = parser.parseFloat(2);
                    float x = parser.parseFloat(5);
                    float y = parser.parseFloat(4);
                    currentDrawShape.addBezierCurveC(x, y, x2, y2, x3, y3);
                    break;

            case Cmd.d :
                D();
                break;

            case Cmd.F :
                if(!removeRenderImages)
                    F(false);
                break;

            case Cmd.f :
                if(!removeRenderImages)
                    F(false);
                break;

            case Cmd.Fstar :
                if(!removeRenderImages)
                    F(true);
                break;

            case Cmd.fstar :
                if(!removeRenderImages)
                    F(true);
                break;

            case Cmd.h :
                H();
                break;

            //case Cmd.i:
              //  I();
                //break;

            case Cmd.J :
                J(false,parser.parseInt(0));
                break;

            case Cmd.j :
                J(true,parser.parseInt(0));
                break;

            case Cmd.l :
                L(parser.parseFloat(1),parser.parseFloat(0));
                break;

            case Cmd.M:
                mm((int) (parser.parseFloat(0)));
                break;

            case Cmd.m :
                M(parser.parseFloat(1),parser.parseFloat(0));
                break;

            case Cmd.n :
                N();
                break;

            case Cmd.re :
                RE(parser.parseFloat(3),parser.parseFloat(2),parser.parseFloat(1),parser.parseFloat(0));
                break;

            case Cmd.S :
                if(!removeRenderImages)
                    S(false);
                break;
            case Cmd.s :
                if(!removeRenderImages)
                    S(true);
                break;

            case Cmd.v :
                V(parser.parseFloat(1),parser.parseFloat(0),parser.parseFloat(3),parser.parseFloat(2));
                break;

            case Cmd.w:
                width(parser.parseFloat(0));
                break;

            case Cmd.Wstar :
                    W(true);
                    break;

                case Cmd.W :
                    W(false);
                    break;
            case Cmd.y :
                Y(parser.parseFloat(1),parser.parseFloat(0),parser.parseFloat(3),parser.parseFloat(2));
                break;
        }

        return dataPointer;
    }

    private void B(boolean isStar,boolean isLowerCase) {

           if(layerDecoder.isLayerVisible()){
               //set Winding rule
               if (isStar)
                   currentDrawShape.setEVENODDWindingRule();
               else
                   currentDrawShape.setNONZEROWindingRule();

               //close for s command
               if (isLowerCase)
                   currentDrawShape.closeShape();

               Shape currentShape =currentDrawShape.generateShapeFromPath(gs.CTM,isClip,gs.getLineWidth());

               //hack which fixes blocky text on Customers3/demo_3.pdf
               if(currentShape!=null && currentShape.getBounds2D().getWidth()<1 && currentShape.getBounds2D().getHeight()<1){
                   return;
               }

               if(!isLowerCase && formLevel > 0 &&  currentShape!=null && gs.getClippingShape()!=null && gs.nonstrokeColorSpace.getID()== ColorSpaces.DeviceCMYK && gs.nonstrokeColorSpace.getColor().getRGB()==-1){

                   //System.out.println(currentShape.getPathIterator(null).)
                   Area a=gs.getClippingShape();
                   a.subtract(new Area(currentShape));
                   currentShape=a;


               }


               //save for later
               if (renderPage && currentShape!=null){

                   gs.setStrokeColor(gs.strokeColorSpace.getColor());
                   gs.setNonstrokeColor(gs.nonstrokeColorSpace.getColor());

                   if(gs.nonstrokeColorSpace.getColor().getRGB()==-16777216 && (gs.getAlpha(GraphicsState.STROKE)==0)){
                       gs.setFillType(GraphicsState.STROKE);
                   }else
                       gs.setFillType(GraphicsState.FILLSTROKE);

                   current.drawShape( currentShape,gs) ;

               }
           }
           //always reset flag
           isClip = false;

           currentDrawShape.resetPath(); // flush all path ops stored
       }



    private void D() {


        String values=""; //used to combine values

        //and the dash array
        int items = parser.getOperandCount();

        if(items==1)
            values=parser.generateOpAsString(0, false);
        else{
            //concat values
        	 //StringBuilder list = new StringBuilder(15);
            for (int i = items - 1; i > -1; i--){
                values+=(parser.generateOpAsString(i, false));
                values+=(' ');
            }
            //values=list.toString();
        }

        //allow for default
        if ((values.equals("[ ] 0 "))|| (values.equals("[]0"))|| (values.equals("[] 0 "))) {
            gs.setDashPhase(0);
            gs.setDashArray(new float[0]);
        } else {

            //get dash pattern
            int pointer=values.indexOf(']');

            String dash=values.substring(0,pointer);
            int phase=(int)Float.parseFloat(values.substring(pointer+1,values.length()).trim());

            //put into dash array
            float[] dash_array = PdfArray.convertToFloatArray(dash);

            for(int aa=0;aa<dash_array.length;aa++){
                // System.out.println(aa+" "+dash_array[aa]);

                if(dash_array[aa]<0.001)
                    dash_array[aa]=0;
            }
            //put array into global value
            gs.setDashArray(dash_array);

            //last value is phase
            gs.setDashPhase(phase);

        }
    }


    private void F(boolean isStar) {

        //ignore transparent white if group set
        if(formLevel>0 && groupObj!=null && !groupObj.getBoolean(PdfDictionary.K) && gs.getAlphaMax(GraphicsState.FILL)>0.84f && (gs.nonstrokeColorSpace.getID() == ColorSpaces.DeviceCMYK)){

            PdfArrayIterator BMvalue = gs.getBM();

            //check not handled elsewhere
            int firstValue=PdfDictionary.Unknown;
            if(BMvalue !=null && BMvalue.hasMoreTokens()) {
                firstValue= BMvalue.getNextValueAsConstant(false);
            }

            if(gs.nonstrokeColorSpace.getColor().getRGB()==-1 || (firstValue==PdfDictionary.Multiply && gs.getAlpha(GraphicsState.FILL)==1f))
                return;
        }

        /**
         * if SMask with this color, we need to ignore
         *  (only case of white with BC of 1,1,1 at present for customers-june2011/12.pdf)
         */
        if(gs.SMask!=null && gs.nonstrokeColorSpace.getID() == ColorSpaces.DeviceCMYK){

            float[] BC=gs.SMask.getFloatArray(PdfDictionary.BC);
            if(gs.nonstrokeColorSpace.getColor().getRGB()==-16777216 && BC!=null && BC[0]==1.0f)
            return;
        }

        if(layerDecoder.isLayerVisible()){

            //set Winding rule
            if (isStar){
                currentDrawShape.setEVENODDWindingRule();
            }else
                currentDrawShape.setNONZEROWindingRule();

            currentDrawShape.closeShape();

            //generate shape and stroke and status
            Shape currentShape =currentDrawShape.generateShapeFromPath(gs.CTM,isClip,gs.getLineWidth());


            //simulate overPrint - may need changing to draw at back of stack
            if(gs.nonstrokeColorSpace.getID()==ColorSpaces.DeviceCMYK && gs.getOPM()==1.0f){

                PdfArrayIterator BMvalue = gs.getBM();

                //check not handled elsewhere
                int firstValue=PdfDictionary.Unknown;
                if(BMvalue !=null && BMvalue.hasMoreTokens()) {
                    firstValue= BMvalue.getNextValueAsConstant(false);
                }

                if(firstValue==PdfDictionary.Multiply){

                    float[] rawData=gs.nonstrokeColorSpace.getRawValues();

                    if(rawData!=null && rawData[3]==1){

                        //try to keep as binary if possible
                        //boolean hasObjectBehind=current.hasObjectsBehind(gs.CTM);
                        //if(hasObjectBehind){
                        currentShape=null;
                        //}
                    }
                }
            }


            if(currentShape!=null && gs.nonstrokeColorSpace.getID()==ColorSpaces.ICC && gs.getOPM()==1.0f){

                PdfArrayIterator BMvalue = gs.getBM();

                //check not handled elsewhere
                int firstValue=PdfDictionary.Unknown;
                if(BMvalue !=null && BMvalue.hasMoreTokens()) {
                    firstValue= BMvalue.getNextValueAsConstant(false);
                }

                if(firstValue==PdfDictionary.Multiply){

                    float[] rawData=gs.nonstrokeColorSpace.getRawValues();

                    /**if(rawData!=null && rawData[2]==1){

                     //try to keep as binary if possible
                     boolean hasObjectBehind=current.hasObjectsBehind(gs.CTM);
                     if(hasObjectBehind)
                     currentShape=null;
                     }else*/{ //if zero just remove
                        int elements=rawData.length;
                        boolean isZero=true;
                        for(int ii=0;ii<elements;ii++)
                            if(rawData[ii]!=0)
                                isZero=false;

                        if(isZero)
                            currentShape=null;
                    }
                }
            }


            //do not paint white CMYK in overpaint mode
            if(currentShape!=null && gs.getAlpha(GraphicsState.FILL)<1 &&
                    gs.nonstrokeColorSpace.getID()==ColorSpaces.DeviceN && gs.getOPM()==1.0f &&
                    gs.nonstrokeColorSpace.getColor().getRGB()==-16777216 ){

                //System.out.println(gs.getNonStrokeAlpha());
                //System.out.println(nonstrokeColorSpace.getAlternateColorSpace()+" "+nonstrokeColorSpace.getColorComponentCount()+" "+nonstrokeColorSpace.pantoneName);
                boolean ignoreTransparent =true; //assume true and disprove
                float[] raw=gs.nonstrokeColorSpace.getRawValues();

                if(raw!=   null){
                    int count=raw.length;
                    for(int ii=0;ii<count;ii++){

                        //System.out.println(ii+"="+raw[ii]+" "+count);

                        if(raw[ii]>0){
                            ignoreTransparent =false;
                            ii=count;
                        }
                    }
                }

                if(ignoreTransparent){
                    currentShape=null;
                }
            }

            //save for later
            if (currentShape!=null && renderPage){

                gs.setStrokeColor( gs.strokeColorSpace.getColor());
                gs.setNonstrokeColor(gs.nonstrokeColorSpace.getColor());
                gs.setFillType(GraphicsState.FILL);

                current.drawShape( currentShape,gs);
            }
        }
        //always reset flag
        isClip = false;
        currentDrawShape.resetPath(); // flush all path ops stored

    }


    private void H() {
        currentDrawShape.closeShape();
    }

    //private void I() {
        //if (currentToken.equals("i")) {
        //int value =
        //	(int) Float.parseFloat((String) operand.elementAt(0));

        //set value
        //currentGraphicsState.setFlatness(value);
        //}
    //}

    private void J(boolean isLowerCase,int value) {

        int style = 0;
        if (!isLowerCase) {

            //map join style
            if (value == 0)
                style = BasicStroke.JOIN_MITER;
            if (value == 1)
                style = BasicStroke.JOIN_ROUND;
            if (value == 2)
                style = BasicStroke.JOIN_BEVEL;

            //set value
            gs.setJoinStyle(style);
        } else {
            //map cap style
            if (value == 0)
                style = BasicStroke.CAP_BUTT;
            if (value == 1)
                style = BasicStroke.CAP_ROUND;
            if (value == 2)
                style = BasicStroke.CAP_SQUARE;

            //set value
            gs.setCapStyle(style);
        }
    }

    private void L(float x,float y) {
        currentDrawShape.lineTo(x,y);
    }

    /**handle the m commands
     * @param x
     * @param y*/
    private void M(float x,float y) {

        //handle m command
        currentDrawShape.moveTo(x, y);

    }

    /**handle the M commands
     * @param mitre_limit*/
    private void mm(int mitre_limit) {

        //handle M command
        gs.setMitreLimit(mitre_limit);

    }

    private void N() {

        if (isClip) {

            //create clipped shape
            currentDrawShape.closeShape();

            Shape s=currentDrawShape.generateShapeFromPath(gs.CTM,false,0);

            //ignore huge shapes which will crash Java
            if(currentDrawShape.getSegmentCount()<5000){
                Area newClip=new Area(s);

                gs.updateClip(newClip);
            }

            if(formLevel==0)
            gs.checkWholePageClip(pageData.getMediaBoxHeight(pageNum)+pageData.getMediaBoxY(pageNum));

            //always reset flag
            isClip = false;


            //System.out.println(">>"+renderPage+" "+gs+" "+defaultClip);
            //save for later
            if (renderPage)
            	current.drawClip(gs,defaultClip,false) ;

        }

        currentDrawShape.resetPath(); // flush all path ops stored

    }

    private void RE(float x,float y,float w,float h) {

        //get values
        currentDrawShape.appendRectangle(x, y, w, h);
    }

    private void S(boolean isLowerCase) {

           if(layerDecoder.isLayerVisible()){

               //close for s command
               if (isLowerCase)
                   currentDrawShape.closeShape();

               Shape currentShape =currentDrawShape.generateShapeFromPath(gs.CTM,isClip,gs.getLineWidth());


               if(currentShape!=null){ //allow for the odd combination of crop with zero size
                   Area crop=gs.getClippingShape();

                   if(crop!=null && (crop.getBounds().getWidth()==0 || crop.getBounds().getHeight()==0 ))
                       currentShape=null;
               }

               if(currentShape!=null){ //allow for the odd combination of f then S

                   if(currentShape.getBounds().getWidth()<=1)// && currentGraphicsState.getLineWidth()<=1.0f){
                       currentShape=currentShape.getBounds2D();

                   //save for later
                   if (renderPage){

                       gs.setStrokeColor( gs.strokeColorSpace.getColor());
                       gs.setNonstrokeColor( gs.nonstrokeColorSpace.getColor());
                       gs.setFillType(GraphicsState.STROKE);

                       current.drawShape( currentShape,gs);

                   }
               }
           }

           //always reset flag
           isClip = false;
           currentDrawShape.resetPath(); // flush all path ops stored

       }


    private void Y(float x3,float y3,float x,float y) {
        currentDrawShape.addBezierCurveY(x, y, x3, y3);
    }

    private void V(float x3,float y3,float x2,float y2) {
        currentDrawShape.addBezierCurveV(x2, y2, x3, y3);
    }

    private void W(boolean isStar) {

        //set Winding rule
        if (isStar)
            currentDrawShape.setEVENODDWindingRule();
        else
            currentDrawShape.setNONZEROWindingRule();

        //set clipping flag
        isClip = true;

    }

    /**set width from lower case w
     * @param w*/
    private void width(float w) {

        gs.setLineWidth(w);

    }

    public void setDefaultClip(Shape defaultClip) {
        this.defaultClip=defaultClip;
    }

    public void setObjectValue(int key, Object  obj){

          switch(key){

              case GroupObj:
                  this.groupObj = (PdfObject) obj;
                  break;

              default:
                  super.setObjectValue(key,obj);

          }
      }
}
