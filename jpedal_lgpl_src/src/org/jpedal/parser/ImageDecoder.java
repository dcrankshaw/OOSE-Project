package org.jpedal.parser;

import org.jpedal.exception.PdfException;
import org.jpedal.external.GlyphTracker;
import org.jpedal.fonts.PdfFont;
import org.jpedal.io.ErrorTracker;
import org.jpedal.io.PdfObjectReader;
import org.jpedal.objects.GraphicsState;
import org.jpedal.objects.PdfData;
import org.jpedal.objects.TextState;
import org.jpedal.objects.layers.PdfLayerList;
import org.jpedal.objects.raw.PdfObject;
import org.jpedal.render.DynamicVectorRenderer;

import java.awt.*;
import java.awt.image.BufferedImage;

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
public interface ImageDecoder {


    public static final int ID=0;

	//public static final int TYPE3FONT=1;

	public static final int XOBJECT=2;

    void setIntValue(int pageNumber, int pageNum);

    void setHandlerValue(int type, Object handler);

    void setSamplingOnly(boolean getSamplingOnly);

    void setCommands(CommandParser parser);

    void setRes(PdfObjectCache cache);

    void setFloatValue(int multiplier, float multiplyer);

    void setName(String fileName);

    void setFileHandler(PdfObjectReader currentPdfFile);

    void setLayerValues(PdfLayerList layers, LayerDecoder layerDecoder);

    void setParameters(boolean pageContent, boolean renderPage, int renderMode, int extractionMode, boolean printing);

    void setRenderer(DynamicVectorRenderer current);

    boolean getBooleanValue(int imagesProcessedFully);

    float getFloatValue(int samplingUsed);

    int getIntValue(int imageCount);

    BufferedImage processImageXObject(PdfObject XObject, String image_name,byte[] objectData, boolean saveRawData, String details) throws PdfException;

    String getImagesInFile();

    int processImage(int dataPointer,PdfObject XObject) throws Exception;

    void setGS(GraphicsState gs);

    void setBooleanValue(int isImage, boolean image);
}
