package org.jpedal.objects.acroforms.creation;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import org.jpedal.PdfDecoder;
import org.jpedal.objects.acroforms.actions.ActionHandler;

// <start-me>
import java.awt.image.ByteLookupTable;
import java.awt.image.LookupOp;
import org.jpedal.objects.raw.FormObject;
import org.jpedal.objects.raw.FormStream;
import org.jpedal.objects.raw.PdfDictionary;
import org.jpedal.objects.raw.PdfObject;
// <end-me>

public class GenericFormFactory {
	
	/**
     * handle on AcroRenderer needed for adding mouse listener
     */
    protected ActionHandler formsActionHandler;
    
    /* handle on PdfDecoder to alow us to use it */
    protected PdfDecoder decode_pdf;
    
    public void setDecoder(PdfDecoder pdfDecoder){
    	decode_pdf = pdfDecoder;
    }
    
    
    protected BufferedImage invertImage(BufferedImage image) {
        if (image == null)
            return null;

        BufferedImage ret = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());

        byte reverse[] = new byte[256];
        for (int j = 0; j < 200; j++) {
            reverse[j] = (byte) (256 - j);
        }

        //<start-me>
        ByteLookupTable blut = new ByteLookupTable(0, reverse);
        LookupOp lop = new LookupOp(blut, null);
        lop.filter(image, ret);
        //<end-me>

        return ret;
    }

    /**
     * create a pressed look of the <b>image</b> and return it
     */
    protected BufferedImage createPressedLook(Image image) {

        if(image==null)
        return null;
        
        BufferedImage pressedImage = new BufferedImage(image.getWidth(null) + 2, image.getHeight(null) + 2, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = (Graphics2D) pressedImage.getGraphics();
        g.drawImage(image, 1, 1, null);
        g.dispose();
        return pressedImage;
    }
    
    /**
     * does nothing (overriden by HTML implementation)
     */
    public void indexAllKids(){
    	
    }
}
