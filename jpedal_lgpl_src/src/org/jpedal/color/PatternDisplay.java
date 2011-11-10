package org.jpedal.color;

import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Set;

import org.jpedal.exception.PdfException;
import org.jpedal.external.ColorHandler;
import org.jpedal.external.ImageHandler;
import org.jpedal.fonts.PdfFont;
import org.jpedal.fonts.glyph.PdfGlyph;
import org.jpedal.io.ObjectStore;
import org.jpedal.objects.GraphicsState;
import org.jpedal.render.DynamicVectorRenderer;
import org.jpedal.render.T3Display;
import org.jpedal.render.T3Renderer;

public class PatternDisplay extends T3Display implements T3Renderer 
{


    private BufferedImage lastImg;

    public PatternDisplay(int i, boolean b, int j, ObjectStore localStore)
    {
        super(i,b,j,localStore);
        
        type = DynamicVectorRenderer.CREATE_PATTERN;
        
    }

    /* save image in array to draw */
    public int drawImage(int pageNumber,BufferedImage image,
                               GraphicsState currentGraphicsState,
                               boolean alreadyCached,String name, int optionsApplied, int previousUse) {

        lastImg=image;

        return super.drawImage(pageNumber, image, currentGraphicsState, alreadyCached, name,  optionsApplied, previousUse);
    }


    public BufferedImage getLastImageRenderer(){
        return lastImg;
    }

}
