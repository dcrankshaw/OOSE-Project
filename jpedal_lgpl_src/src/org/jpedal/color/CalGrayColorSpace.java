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
 * CalGrayColorSpace.java
 * ---------------
 */
package org.jpedal.color;

/**
 * handle Java version of CalGrayColorSpace in PDF
 */
public class CalGrayColorSpace extends GenericColorSpace {

	private static final long serialVersionUID = -6459433440483127497L;

	public CalGrayColorSpace(float[] whitepoint,float[] blackpoint,float[] gamma) {

		componentCount=1;

		setCIEValues(whitepoint,blackpoint,null,null,gamma);
		value = ColorSpaces.CalGray;

	}

	/**
	 * set CalGray color (in terms of rgb)
	 */
	final public void setColor(String[] number_values,int opCount) {

		float[] colValues=new float[1];
		colValues[0] = Float.parseFloat(number_values[0]);

		setColor(colValues,1);
	}

	/**
	 * set CalGray color (in terms of rgb)
	 */
	final public void setColor(float[] number_values,int opCount) {

		float A = number_values[0];

		//standard calculate (see pdf spec 1.3 page 170)
		float[] values = new float[3];
		float AG = (float) Math.pow(A, G[0]);

		values[0] = ((W[0]) * AG);
		values[1] = ((W[1]) * AG);
		values[2] = (W[2] * AG);

		/*<start-me>
		//<end-me>
		/*/  //me does not allow toRGB() so comment out
		//convert to rgb
		values = cs.toRGB(values);/**/

		//set color
		this.currentColor= new PdfColor(values[0],values[1],values[2]);

	}
}
