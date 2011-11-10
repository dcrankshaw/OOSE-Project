package org.jpedal.objects.acroforms.overridingImplementations;

import java.awt.Image;

import javax.swing.ImageIcon;

public class CustomImageIcon extends ImageIcon {
	private static final long serialVersionUID = 5003778613900628453L;
	
	/** the maximum scaling factor difference between the rootImage and the current Form dimentions */
    protected static float MAXSCALEFACTOR = 1.5f;
    
    protected int iconWidth = -1;
    protected int iconHeight = -1;
    
    protected int iconRotation = 0;
    protected int iconOpp = 180;
	/** the page rotation required for this image */
    protected int pageRotate=0;
	
    /** used to tell praint method if we are displaying in single page mode, 
	 * if so we rotate here, if not rotate is handled elsewhere.
	 */
	protected boolean displaySingle = false;
	
	/** sets the scaling factor that the image has to change by before the root images are redraw to the current sizes.
	 * i.e if scaling factor is 1.5 start with 50% image, it wont redraw the image until its abot 75% or below 33%
	 * where as scaling factor of 1, means it will always redraw the image to the size required.
	 */
	public static void setMaxScaleFactor(float scaleFactor){
		MAXSCALEFACTOR = scaleFactor;
	}
	
	public CustomImageIcon(int iconRot){
		iconRotation = iconRot;
    	iconOpp = iconRotation - 180;
		if(iconOpp<0)
			iconOpp+=360;
	}
	
	public void setAttributes(int newWidth,int newHeight, int pageRotation,boolean displaySing){
    	//recalculate rotationVal
    	int finalRotation = validateRotationValue(pageRotation - iconRotation);
		
		pageRotate = pageRotation;
		
		if(finalRotation==iconRotation || finalRotation==iconOpp){
			iconWidth = newWidth;
			iconHeight = newHeight;
        }else {//the final rotation is out by 90 relative to the icon rotation
        	//turn the width and height round so that the bufferedimage is the correct orientation
        	//this is relative to the final rotation
        	iconWidth = newHeight;
        	iconHeight = newWidth;
        }
		
		displaySingle = displaySing;
    }
	
	protected static int validateRotationValue(int rotation) {
    	//make sure is between 0 and 360
		rotation = rotation%360;
    	//if negative make positive
		if(rotation<0)
			rotation+=360;
		
		return rotation;
	}
	
	public int getIconHeight() {
    	if(iconHeight==-1){
    		Image image = getImage();
        	
        	if(image==null)
    			return -1;
        	else
    			return image.getHeight(null);
    	}else
    		return iconHeight;
    }
	
	public int getIconWidth() {
    	if(iconWidth==-1){
    		Image image = getImage();
        	
        	if(image==null)
    			return -1;
        	else
    			return image.getWidth(null);
    	}else
    		return iconWidth;
    }
}
