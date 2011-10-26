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
 * DeviceCMYKColorSpace.java
 * ---------------
 */
package org.jpedal.color;

import java.awt.color.ColorSpace;
// <start-me>
import java.awt.color.ICC_ColorSpace;
import java.awt.color.ICC_Profile;
// <end-me>
import java.awt.image.*;

import java.io.InputStream;
import java.io.FileInputStream;
import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Map;

import org.jpedal.utils.LogWriter;

import org.jpedal.exception.PdfException;
import org.jpedal.io.ColorSpaceConvertor;

import javax.imageio.ImageReader;
import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;

/**
 * handle DeviceCMYKColorSpace
 */
public class DeviceCMYKColorSpace extends  GenericColorSpace{

	private static final long serialVersionUID = 4054062852632000027L;

	private float lastC = -1, lastM=-1, lastY=-1, lastK=-1;

	private  static ColorSpace CMYK=null;

	private static Map cache=new HashMap();

	private static boolean cacheCMYK=false;

    /**
     * ensure next setColor will not match with old color as value may be out of sync
     */
    public void clearCache(){
      lastC=-1;
    }

	/**
	 * initialise CMYK profile
	 */
	private void initColorspace() {

		/**load the cmyk profile - I am using the Adobe version from the web. There are lots
		 * out there.*/
		try {

			String profile=System.getProperty("org.jpedal.profile");

			InputStream stream;
			if(profile==null)
				stream=this.getClass().getResourceAsStream("/org/jpedal/res/cmm/cmyk.icm");
			else{
				try{
					stream=new FileInputStream(profile);
				}catch(Exception ee){
					throw new PdfException("PdfException attempting to use user profile "+profile+" Message="+ee);
				}
			}

			// <start-me>
			ICC_Profile p =ICC_Profile.getInstance(stream);
			CMYK = new ICC_ColorSpace(p);
			// <end-me>
			stream.close();

		} catch (Exception e) {
			if(LogWriter.isOutput())
				LogWriter.writeLog("Exception "+e);

			throw new RuntimeException("Problem setting CMYK Colorspace with message "+e+" Possible cause file cmyk.icm corrupted");
		}
	}

	/**setup colorspaces*/
	public DeviceCMYKColorSpace(){

		componentCount=4;

		if(CMYK==null)
			initColorspace();

		cs = CMYK;

		value = ColorSpaces.DeviceCMYK;
	}

	/**
	 * set CalRGB color (in terms of rgb)
	 */
	final public void setColor(String[] number_values,int items) {

		float[] colValues=new float[items];

		for(int ii=0;ii<items;ii++)
			colValues[ii]=Float.parseFloat(number_values[ii]);

		setColor(colValues,items);
	}

	/**
	 * convert CMYK to RGB as defined by Adobe
	 * (p354 Section 6.2.4 in Adobe 1.3 spec 2nd edition)
	 * and set value
	 */
	final public void setColor(float[] operand,int length) {

		boolean newVersion=true;

		//default of black
		c=1;
		y=1;
		m=1;
		k=1;

		if(length>3){
			//get values
			c= operand[0];
			// the cyan
			m= operand[1];
			// the magenta
			y =operand[2];
			// the yellow
			k = operand[3];
		}else{
			//get values
			if(length>0)
				c= operand[0];
			// the cyan
			if(length>1)
				m= operand[1];
			// the magenta
			if(length>2)
				y = operand[2];
			// the yellow
			if(length>3)
				k = operand[3];

		}


		float r, g, b;
		if ((lastC == c) && (lastM == m) && (lastY == y) && (lastK == k)) {
		} else {

			//store values
			rawValues=new float[4];
			rawValues[0]=c;
			rawValues[1]=m;
			rawValues[2]=y;
			rawValues[3]=k;

			if(!newVersion){
				//convert the colours the old way
				r = (c + k);
				if (r > 1)
					r = 1;
				g = (m + k);
				if (g > 1)
					g = 1;
				b = (y + k);
				if (b > 1)
					b = 1;

				//set the colour
				this.currentColor= new PdfColor(
						(int) (255 * (1 - r)),
						(int) (255 * (1 - g)),
						(int) (255 * (1 - b)));
				
			}else if((c==0)&&(y==0)&&(m==0)&&(k==0)){
				this.currentColor=new PdfColor(1.0f,1.0f,1.0f);

			}else{
				if(c>.99)
					c=1.0f;
				else if(c<0.01)
					c=0.0f;
				if(m>.99)
					m=1.0f;
				else if(m<0.01)
					m=0.0f;
				if(y>.99)
					y=1.0f;
				else if(y<0.01)
					y=0.0f;
				if(k>.99)
					k=1.0f;
				else if(k<0.01)
					k=0.0f;

				//we store values to speedup operation
				float[] rgb=null;
				Integer key=null;

				if(cacheCMYK){
					key=new Integer((int)(c*255)+((int)(m*255)<<8)+((int)(y*255)<<16)+((int)(k*255)<<24));
					Object cachedValue= cache.get(key);

					rgb= (float[]) cachedValue;
				}

				if(rgb==null){
					float[] cmykValues = {c,m,y,k};
					// <start-me>
					rgb=CMYK.toRGB(cmykValues);
					/* <end-me>
				    //me does not allow toRGB
				    rgb=cmykValues;
				    /**/

					if(cacheCMYK)
						cache.put(key,rgb);

					//check rounding
					for(int jj=0;jj<3;jj++){
						if(rgb[jj]>.99)
							rgb[jj]=1.0f;
						else if(rgb[jj]<0.01)
							rgb[jj]=0.0f;
					}
				}
				currentColor=new PdfColor(rgb[0],rgb[1],rgb[2]);

			}
			lastC=c;
			lastM=m;
			lastY=y;
			lastK=k;
		}
	}

	// <start-me>
	/**
	 * <p>
	 * Convert DCT encoded image bytestream to sRGB
	 * </p>
	 * <p>
	 * It uses the internal Java classes and the Adobe icm to convert CMYK and
	 * YCbCr-Alpha - the data is still DCT encoded.
	 * </p>
	 * <p>
	 * The Sun class JPEGDecodeParam.java is worth examining because it contains
	 * lots of interesting comments
	 * </p>
	 * <p>
	 * I tried just using the new IOImage.read() but on type 3 images, all my
	 * clipping code stopped working so I am still using 1.3
	 * </p>
	 */
	final public BufferedImage JPEGToRGBImage(
			byte[] data,int w,int h,float[] decodeArray,int pX,int pY, boolean arrayInverted) {

		return nonRGBJPEGToRGBImage(data,w,h, decodeArray,pX,pY);

	}	
	// <end-me>

	/**
	 * convert byte[] datastream JPEG to an image in RGB
	 * @throws PdfException
	 */
	public BufferedImage  JPEG2000ToRGBImage(byte[] data,int w,int h,float[] decodeArray,
			int pX,int pY) throws PdfException{

		BufferedImage image = null;

		ByteArrayInputStream in = null;
		
		Raster ras=null;

		try {
			in = new ByteArrayInputStream(data);

			ImageReader iir = (ImageReader) ImageIO.getImageReadersByFormatName("JPEG2000").next();
			ImageInputStream iin = ImageIO.createImageInputStream(in);

			
			iir.setInput(iin, true); 

			/**
			 * indexes are a completely different game 
			 * CMYK has 4 color components (C,M,Y,K) for each pixel
			 * indexed has 1 component pointing to value in lookup table 
			 * so need a totally different approach
			 */
			//<start-me>  //JAVAME does not allow createCompatibleWritableRaster NOR bufferedImage with one as param
			byte[] index=this.getIndexedMap();
			if(index!=null){
				
				//make it RGB
				if(!isIndexConverted()){
					index=convertIndexToRGB(index);
				}
				
				//get data for image (its an index so just refers to color numbers
				RenderedImage renderimage = iir.readAsRenderedImage(0,iir.getDefaultReadParam());
				ras=renderimage.getData();

				//and build a standard rgb image
				ColorModel cm=new IndexColorModel(8, index.length/3, index, 0, false);
				image = new BufferedImage(cm,(WritableRaster) ras.createCompatibleWritableRaster(), false, null);
				
				//downsample to reduce size if huge
				image=cleanupImage(image,pX,pY, image.getType());
				
			}else{ //non-indexed routine
				//<end-me>

				//This works except image is wrong so we read and convert
				image=iir.read(0);
				ras=image.getRaster();

				//apply if set
				if(decodeArray!=null){

					if((decodeArray.length==6 && decodeArray[0]==1f && decodeArray[1]==0f &&
							decodeArray[2]==1f && decodeArray[3]==0f &&
							decodeArray[4]==1f && decodeArray[5]==0f )||
							(decodeArray.length>2 &&
									decodeArray[0]==1f && decodeArray[1]==0)){

						DataBuffer buf=ras.getDataBuffer();

						int count=buf.getSize();

						/* <start-me>
                    	// <end-me>
                    	/*/ //me does not allow us to set or get the elements in the databuffer
						for(int ii=0;ii<count;ii++)
							buf.setElem(ii,255-buf.getElem(ii));
						/**/
					}else if(decodeArray.length==6 &&
							decodeArray[0]==0f && decodeArray[1]==1f &&
							decodeArray[2]==0f && decodeArray[3]==1f &&
							decodeArray[4]==0f && decodeArray[5]==1f){
					}else if(decodeArray!=null && decodeArray.length>0){
					}
				}

				ras=cleanupRaster(ras,pX,pY,4);
				w=ras.getWidth();
				h=ras.getHeight();

				//generate the rgb image
				WritableRaster rgbRaster =null;
			if(image.getType()==13){ //indexed variant
					rgbRaster = ColorSpaceConvertor.createCompatibleWritableRaaster(image.getColorModel(),w,h);
					// <start-me>
					//me does not allow colorconvertop
					CSToRGB = new ColorConvertOp(cs, image.getColorModel().getColorSpace(), ColorSpaces.hints);
					// <end-me>
					image =new BufferedImage(w,h,image.getType());
				}else{

					// <start-me>
					if(CSToRGB==null)
						initCMYKColorspace();
					// <end-me>
					rgbRaster=ColorSpaceConvertor.createCompatibleWritableRaaster(rgbModel,w, h);

					// <start-me>
					//me does not allow colorconvertop

					CSToRGB = new ColorConvertOp(cs, rgbCS, ColorSpaces.hints);
						CSToRGB.filter(ras, rgbRaster);
					// <end-me>

					image =new BufferedImage(w,h,BufferedImage.TYPE_INT_RGB);

				}

				image.setData(rgbRaster);
				//<start-me>
			}
			//<end-me>
			iir.dispose();
			iin.close();
			in.close();

			//image=cleanupImage(image,pX,pY);
			//image= ColorSpaceConvertor.convertToRGB(image);

		} catch (Exception ee) {
			image = null;
			if(LogWriter.isOutput())
				LogWriter.writeLog("Problem reading JPEG 2000: " + ee);
			
			ee.printStackTrace();
			throw new PdfException("Exception "+ee+" with JPEG2000 image - please ensure imageio.jar (from JAI library) on classpath");
		} catch (Error ee2) {
			image = null;
			ee2.printStackTrace();
			
			if(LogWriter.isOutput())
				LogWriter.writeLog("Problem reading JPEG 2000 with error " + ee2);

			throw new PdfException("Error with JPEG2000 image - please ensure imageio.jar (from JAI library) on classpath");
		}

		return image;
	}

	/**
	 * convert Index to RGB
	 */
	final public byte[] convertIndexToRGB(byte[] index){

		isConverted=true;

		return convert4Index(index);
	}	
}
