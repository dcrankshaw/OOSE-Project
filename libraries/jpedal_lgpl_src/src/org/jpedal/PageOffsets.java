/**
* ===========================================
* Java Pdf Extraction Decoding Access Library
* ===========================================
*
* Project Info:  http://www.jpedal.org
* (C) Copyright 1997-2008, IDRsolutions and Contributors.
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
* PageOffsets.java
* ---------------
*/
package org.jpedal;

import org.jpedal.objects.PdfPageData;


/**
 * holds offsets for all multiple pages
 */
public final class PageOffsets {

    /**width of all pages*/
    protected int totalSingleWidth=0,totalDoubleWidth=0,gaps=0,doubleGaps=0;

    /**height of all pages*/
    protected int totalSingleHeight=0,totalDoubleHeight=0;

    protected int maxW = 0, maxH = 0;

    /**gap between pages*/
    protected static final int pageGap=10;

    /**max widths and heights for facing and continuous pages*/
    protected int doublePageWidth=0,doublePageHeight=0,biggestWidth=0,biggestHeight=0,widestPageNR,widestPageR;

	protected boolean hasRotated;

    /**
     * PageFlow Constants
     */
    public static int PAGEFLOW_PAGES = 8;					    //Number of pages per side
    public static int PAGEFLOW_EXTRA_CACHE = 10;                 //Number of extra pages per side to keep in cache
	public static double PAGEFLOW_SIDE_SIZE = 0.75; 		        //Overall size of side pages

    //Reflection control
    public static boolean PAGEFLOW_REFLECTION = true;            //Whether to show a reflection
    public static double PAGEFLOW_EXTRA_HEIGHT_FOR_REFLECTION = 0.35;     //Percentage of height to add as space at the bottom

    //Shadow control
    public static boolean PAGEFLOW_SHADOW = true;                //Whether to show shadows
    public static float PAGEFLOW_SHADOW_DARKNESS = 0.5f;          //Darkness of shadow

    //Perspective control
	public static final double PAGEFLOW_WIDTH_RATIO = 0.5; 		//Proportion of width for perspective
    public static final double PAGEFLOW_HEIGHT_RATIO = 0.7; 		//Proportion of height for perspective

    //Display width control
    public static final int SIDE_PAGE_DISPLAY_MIN = 110;			//Minimum width to display of side pages (-1 disables)
    public static final double SIDE_PAGE_DISPLAY_PROPORTION = 0.5;	//Default proportion to display of side pages
    public static final int SIDE_PAGE_DISPLAY_MAX = 200;			//Maximum width to display of side pages (-1 disables)

    public PageOffsets(int pageCount, PdfPageData pageData) {


			/** calulate sizes for continuous and facing page modes */
            int pageH, pageW,rotation;
            int facingW = 0, facingH = 0;
            int greatestW = 0, greatestH = 0;
            totalSingleHeight = 0;
            totalSingleWidth = 0;
			hasRotated=false;

			int widestLeftPage=0,widestRightPage=0,highestLeftPage=0,highestRightPage=0;

			widestPageR=0;
			widestPageNR=0;

			totalDoubleWidth = 0;
            totalDoubleHeight = 0;
            gaps=0;
            doubleGaps=0;

            biggestWidth = 0;
            biggestHeight = 0;

			for (int i = 1; i < pageCount + 1; i++) {

				//get page sizes
                pageW = pageData.getCropBoxWidth(i);
                pageH = pageData.getCropBoxHeight(i);
				rotation = pageData.getRotation(i);

				//swap if this page rotated and flag
				if((rotation==90|| rotation==270)){
	                int tmp=pageW;
	                pageW=pageH;
	                pageH=tmp;
				}

                if (pageW > maxW)
                    maxW = pageW;

                if (pageH > maxH)
                    maxH = pageH;

				gaps=gaps+pageGap;


				totalSingleWidth = totalSingleWidth + pageW;
				totalSingleHeight = totalSingleHeight + pageH;

				//track page sizes
				if(( i & 1)==1){//odd
					if(widestRightPage<pageW)
						widestRightPage=pageW;
					if(highestRightPage<pageH)
						highestRightPage=pageH;
				}else{
					if(widestLeftPage<pageW)
						widestLeftPage=pageW;
					if(highestLeftPage<pageH)
						highestLeftPage=pageH;
				}

				if(widestPageNR<pageW)
				widestPageNR=pageW;

				if(widestPageR<pageH)
				widestPageR=pageH;

				if (pageW > biggestWidth)
					biggestWidth = pageW;
				if (pageH > biggestHeight)
					biggestHeight = pageH;

				// track widest and highest combination of facing pages
                if ((i & 1) == 1) {

					if (greatestW < pageW)
                        greatestW = pageW;
                    if (greatestH < pageH)
                        greatestH = pageH;

					if (i == 1) {// first page special case
						totalDoubleWidth = pageW;
						totalDoubleHeight = pageH;
					} else {
                        totalDoubleWidth = totalDoubleWidth + greatestW;
                        totalDoubleHeight = totalDoubleHeight + greatestH;
					}

					doubleGaps=doubleGaps+pageGap;

					facingW = pageW;
                    facingH = pageH;

                } else {

					facingW = facingW + pageW;
                    facingH = facingH + pageH;

					greatestW = pageW;
					greatestH = pageH;

                    if (i == pageCount) { // allow for even number of pages
                        totalDoubleWidth = totalDoubleWidth + greatestW + pageGap;
                        totalDoubleHeight = totalDoubleHeight + greatestH + pageGap;
                    }
                }

                //choose largest (to allow for rotation on specific pages)
                //int max=facingW;
                //if(max<facingH)
                //	max=facingH;

            }

			doublePageWidth=widestLeftPage+widestRightPage+pageGap;
			doublePageHeight=highestLeftPage+highestRightPage+pageGap;

            // subtract pageGap to make sum correct
            totalSingleWidth = totalSingleWidth - pageGap;
			totalSingleHeight = totalSingleHeight - pageGap;

		}

    /**
     * Return the scaled width of side page to display, based on the
     * constants defined in this class.
     * @param imageWidth
     * @param scaling
     * @return
     */
    public static double getPageFlowPageWidth(int imageWidth, float scaling) {
        double generatedWidth = imageWidth*PAGEFLOW_SIDE_SIZE*PAGEFLOW_WIDTH_RATIO;
        double result = generatedWidth*SIDE_PAGE_DISPLAY_PROPORTION;
        if (SIDE_PAGE_DISPLAY_MAX != -1 && result > scaling*SIDE_PAGE_DISPLAY_MAX)
            result = scaling*SIDE_PAGE_DISPLAY_MAX;
        if (SIDE_PAGE_DISPLAY_MIN != -1 && result < scaling*SIDE_PAGE_DISPLAY_MIN)
            result = scaling*SIDE_PAGE_DISPLAY_MIN;
        if (result > generatedWidth)
            result = generatedWidth;
        return result;
    }

    /**
     * Add the required height for pageFlow.
     * @param height
     * @return
     */
    public static int getPageFlowExtraHeight(int height) {
        if (PAGEFLOW_REFLECTION)
            return (int)(height*(1+PAGEFLOW_EXTRA_HEIGHT_FOR_REFLECTION));
        else
            return height;
    }

    public int getMaxH() {
        return maxH;
    }

    public int getMaxW() {
        return maxW;
    }

	public int getWidestPageR() {
		return widestPageR;
	}

	public int getWidestPageNR() {
		return widestPageNR;
	}
}

