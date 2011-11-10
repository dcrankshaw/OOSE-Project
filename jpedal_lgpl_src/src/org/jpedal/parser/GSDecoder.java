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

  * GSDecoder.java
  * ---------------
  * (C) Copyright 2007, by IDRsolutions and Contributors.
  *
  *
  * --------------------------
 */
package org.jpedal.parser;

import org.jpedal.color.ColorSpaces;
import org.jpedal.color.GenericColorSpace;
import org.jpedal.objects.GraphicsState;
import org.jpedal.objects.TextState;
import org.jpedal.objects.raw.PdfObject;
import org.jpedal.utils.LogWriter;
import org.jpedal.utils.Matrix;
import org.jpedal.utils.repositories.Vector_Object;

import java.util.Map;

public class GSDecoder extends BaseDecoder{

    private TextState currentTextState;

    /**flag to show if stack setup*/
    private boolean isStackInitialised=false;

    /**stack for graphics states*/
    private Vector_Object graphicsStateStack;

    /**stack for graphics states*/
    private Vector_Object strokeColorStateStack;

    /**stack for graphics states*/
    private Vector_Object nonstrokeColorStateStack;

    /**stack for graphics states*/
    private Vector_Object textStateStack;


    public GraphicsState processToken(int commandID, boolean getSamplingOnly, GraphicsState gs,TextState currentTextState) {

        this.currentTextState=currentTextState;

        switch(commandID){

            case Cmd.cm :
                        //create temp Trm matrix to update Tm
                        float[][] Trm = new float[3][3];

                        //set Tm matrix
                        Trm[0][0] = parser.parseFloat(5);
                        Trm[0][1] = parser.parseFloat(4);
                        Trm[0][2] = 0;
                        Trm[1][0] = parser.parseFloat(3);
                        Trm[1][1] = parser.parseFloat(2);
                        Trm[1][2] = 0;
                        Trm[2][0] = parser.parseFloat(1);
                        Trm[2][1] = parser.parseFloat(0);
                        Trm[2][2] = 1;

                        CM(Trm,gs);
                        break;

                    case Cmd.q :
                        gs = Q(gs,true);
                        break;

                    case Cmd.Q :
                        gs = Q(gs, false);
                        break;

            case Cmd.gs :
                if(!getSamplingOnly)
                    gs(parser.generateOpAsString(0, true),gs);
                break;

        }

        return gs;
    }

    /**
     * put item in graphics stack
     */
    private void pushGraphicsState(GraphicsState gs) {

        if(!isStackInitialised){
            isStackInitialised=true;

            graphicsStateStack = new Vector_Object(10);
            textStateStack = new Vector_Object(10);
            strokeColorStateStack= new Vector_Object(20);
            nonstrokeColorStateStack= new Vector_Object(20);
            //clipStack=new Vector_Object(20);
        }

        //store
        graphicsStateStack.push(gs.clone());

        //store clip
        //		Area currentClip=gs.getClippingShape();
        //		if(currentClip==null)
        //			clipStack.push(null);
        //		else{
        //			clipStack.push(currentClip.clone());
        //		}
        //store text state (technically part of gs)
        textStateStack.push(currentTextState.clone());

        //save colorspaces
        nonstrokeColorStateStack.push(gs.nonstrokeColorSpace.clone());
        strokeColorStateStack.push(gs.strokeColorSpace.clone());

        current.resetOnColorspaceChange();

    }

/**
     * restore GraphicsState status from graphics stack
     */
    private GraphicsState restoreGraphicsState(GraphicsState gs) {

        boolean hasClipChanged=false;

        if(!isStackInitialised){

            if(LogWriter.isOutput())
                LogWriter.writeLog("No GraphicsState saved to retrieve");

            //reset to defaults
            gs=new GraphicsState();
            currentTextState = new TextState();

        }else{

            //see if clip changed
            hasClipChanged=gs.hasClipChanged();

            gs = (GraphicsState) graphicsStateStack.pull();
            currentTextState = (TextState) textStateStack.pull();

            //@remove all caching?
            gs.strokeColorSpace=(GenericColorSpace) strokeColorStateStack.pull();
            gs.nonstrokeColorSpace=(GenericColorSpace) nonstrokeColorStateStack.pull();

            if(gs.strokeColorSpace.getID()== ColorSpaces.Separation)
                gs.strokeColorSpace.restoreColorStatus();

            if(gs.nonstrokeColorSpace.getID()==ColorSpaces.Separation)
                gs.nonstrokeColorSpace.restoreColorStatus();
        }
        //20101122 removed by MS as not apparently needed
        //Object currentClip=clipStack.pull();


        /**
         if(hasClipChanged){
         //if(!renderDirectly && hasClipChanged){
         if(currentClip==null){

         if(gs.current_clipping_shape!=null){
         System.out.println("1shape="+gs.current_clipping_shape);
         throw new RuntimeException();
         }
         gs.setClippingShape(null);
         }else{

         if(!gs.current_clipping_shape.equals((Area)currentClip)){
         System.out.println("2shape="+gs.current_clipping_shape);
         //  throw new RuntimeException();
         }
         gs.setClippingShape((Area)currentClip);
         }
         }
         /**/
        ////////////////////////////////////

        //copy last CM
        for(int i=0;i<3;i++)
            System.arraycopy(gs.CTM, 0, gs.lastCTM, 0, 3);

        //save for later
        if (renderPage){

            if(hasClipChanged){

                current.drawClip(gs,defaultClip,true) ;
            }
            current.resetOnColorspaceChange();

            current.drawFillColor(gs.getNonstrokeColor());
            current.drawStrokeColor(gs.getStrokeColor());

            /**
             * align display
             */
            current.setGraphicsState(GraphicsState.FILL,gs.getAlpha(GraphicsState.FILL));
            current.setGraphicsState(GraphicsState.STROKE,gs.getAlpha(GraphicsState.STROKE));

            //current.drawTR(currentGraphicsState.getTextRenderType()); //reset TR value

        }

        return gs;
    }


    private GraphicsState Q(GraphicsState gs, boolean isLowerCase) {

        //save or retrieve
        if (isLowerCase)
            pushGraphicsState(gs);
        else{
            gs = restoreGraphicsState(gs);

            //flag font has changed
            currentTextState.setFontChanged(true);

        }

        return gs;
    }


    private static void CM(float[][] Trm, GraphicsState gs) {


        //copy last CM
        for(int i=0;i<3;i++)
            System.arraycopy(gs.CTM, 0, gs.lastCTM, 0, 3);

        //multiply to get new CTM
        gs.CTM = Matrix.multiply(Trm, gs.CTM);

        //remove slight sheer
        if(gs.CTM[0][0]>0 && gs.CTM[1][1]>0 && gs.CTM[1][0]>0 && gs.CTM[1][0]<0.01 && gs.CTM[0][1]<0){
            gs.CTM[0][1]=0;
            gs.CTM[1][0]=0;
        }
    }

    private void gs(Object key, GraphicsState gs) {

        PdfObject GS= (PdfObject) cache.GraphicsStates.get(key);

        /**
         * set gs
         */
        gs.setMode(GS);

        current.setGraphicsState(GraphicsState.FILL,gs.getAlpha(GraphicsState.FILL));
        current.setGraphicsState(GraphicsState.STROKE,gs.getAlpha(GraphicsState.STROKE));
    }

    public TextState getTextState() {
        return this.currentTextState;
    }
}
