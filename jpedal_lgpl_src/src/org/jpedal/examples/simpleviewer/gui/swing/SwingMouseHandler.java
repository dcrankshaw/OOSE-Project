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
* SwingMouseHandler.java
* ---------------
*/
package org.jpedal.examples.simpleviewer.gui.swing;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Area;
import java.awt.geom.AffineTransform;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Iterator;
import java.util.Map;

import javax.swing.*;

import org.jpedal.*;
import org.jpedal.io.ObjectStore;
import org.jpedal.io.PdfObjectReader;
import org.jpedal.examples.simpleviewer.Commands;
import org.jpedal.examples.simpleviewer.Values;
import org.jpedal.examples.simpleviewer.gui.SwingGUI;
import org.jpedal.examples.simpleviewer.gui.generic.GUIMouseHandler;
import org.jpedal.external.Options;

import org.jpedal.objects.raw.FormObject;
import org.jpedal.objects.raw.PdfDictionary;
import org.jpedal.objects.raw.PdfObject;
import org.jpedal.render.DynamicVectorRenderer;


/**handles all mouse activity in GUI using Swing classes*/
public class SwingMouseHandler implements GUIMouseHandler{

	private PdfDecoder decode_pdf;
	private SwingGUI currentGUI;
	private Values commonValues;

	private Commands currentCommands;
	
	//Is the mouse currently being dragged
	//private boolean dragged = false;

	/**tells user if we enter a link*/
	private String message="";

	/** cursor rectangle handles */
	private Rectangle[] boxes = new Rectangle[8];

	/** the extra gap for the cursorBox handlers highlighting */
	private int handlesGap = 5;

	/** old x and y values for where drag original location was */
	private int oldX=-1,oldY=-1;

	/** flag to tell whether drag altering currentRectangle */
	private boolean dragAltering=false;

	/** which handle box is being altered */
	private int boxContained = -1;

	/** to allow new cursor box to be drawn */
	private boolean drawingCursorBox=false;

	/**used to track changes when dragging rectangle around*/
	private int old_m_x2=-1,old_m_y2=-1;

    /** allow turning page to be drawn */
    private boolean drawingTurnover=false;

    /** show turning page when hovering over corner */
    private boolean previewTurnover=false;

    /** whether page turn is currently animating */
    private boolean pageTurnAnimating =false;

    /**current page being hovered over in pageFlow*/
    private int pageFlowCurrentPage;

    /**middle drag panning values*/
    private double middleDragStartX,middleDragStartY,xVelocity,yVelocity;
    private Timer middleDragTimer;

	/**current cursor position*/
	private int cx,cy;

    public int[] getCursorLocation() {
        return new int[]{cx,cy};
    }

    /**
	 * picks up clicks so we can draw an outline on screen
	 */
	protected class mouse_clicker extends MouseAdapter {
		
		private mouse_mover mover;
        private long lastPress;
		
		//user has pressed mouse button so we want to use this 
		//as one point of outline
		public void mousePressed(MouseEvent event) {

            //Activate turnover if pressed while preview on
            if (decode_pdf.turnoverOn && previewTurnover && decode_pdf.getDisplayView()==Display.FACING &&
                    event.getButton()==MouseEvent.BUTTON1) {
                drawingTurnover = true;
                //set cursor
                currentGUI.setCursor(SwingGUI.GRABBING_CURSOR);
                lastPress = System.currentTimeMillis();
            }

            //Start dragging
            if (event.getButton()==MouseEvent.BUTTON2) {
                middleDragStartX = event.getX() - decode_pdf.getVisibleRect().getX();
                middleDragStartY = event.getY() - decode_pdf.getVisibleRect().getY();
                currentGUI.setCursor(SwingGUI.PAN_CURSOR);

                //set up timer to refresh display
                if (middleDragTimer == null)
                    middleDragTimer = new Timer(100,new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            Rectangle r = decode_pdf.getVisibleRect();
                            r.translate((int)xVelocity,(int)yVelocity);
                            if (xVelocity<-2) {
                                if (yVelocity<-2)
                                    currentGUI.setCursor(SwingGUI.PAN_CURSORTL);
                                else if (yVelocity>2)
                                    currentGUI.setCursor(SwingGUI.PAN_CURSORBL);
                                else
                                    currentGUI.setCursor(SwingGUI.PAN_CURSORL);
                            } else if (xVelocity>2) {
                                if (yVelocity<-2)
                                    currentGUI.setCursor(SwingGUI.PAN_CURSORTR);
                                else if (yVelocity>2)
                                    currentGUI.setCursor(SwingGUI.PAN_CURSORBR);
                                else
                                    currentGUI.setCursor(SwingGUI.PAN_CURSORR);
                            } else {
                                if (yVelocity<-2)
                                    currentGUI.setCursor(SwingGUI.PAN_CURSORT);
                                else if (yVelocity>2)
                                    currentGUI.setCursor(SwingGUI.PAN_CURSORB);
                                else
                                    currentGUI.setCursor(SwingGUI.PAN_CURSOR);
                            }
                            decode_pdf.scrollRectToVisible(r);


                        }
                    });
                middleDragTimer.start();
            }
		}

		//show the description in the text box or update screen
		public void mouseClicked(MouseEvent event) {
            //pageFlow click to page
            if (decode_pdf.getDisplayView()==Display.PAGEFLOW && event.getButton()==MouseEvent.BUTTON1) {
                //instead of calculating if it's over a page, see if the cursor is a hand
                // - it's already calculated in mouse_mover.mouseMoved()
                if (decode_pdf.getCursor().getType() != Cursor.HAND_CURSOR)
                    return;

                int oldPage = commonValues.getCurrentPage();

                //set scrollbar to set page
                decode_pdf.getPageFlowBar().setValue(pageFlowCurrentPage-1);

                //update pageFlowCurrentPage due to page change
                pageFlowCurrentPage += (pageFlowCurrentPage-oldPage);

                //Force redraw
                decode_pdf.paintAll(decode_pdf.getGraphics());
            }

            if (decode_pdf.getDisplayView()==Display.SINGLE_PAGE &&
                    event.getButton()==MouseEvent.BUTTON1 &&
                    currentGUI.addUniqueIconToFileAttachment)
                        checkLinks(true,decode_pdf.getIO());
		}

		
		//user has stopped clicking so we want to remove the outline rectangle
		public void mouseReleased(MouseEvent event) {

            //Stop drawing turnover
            if (decode_pdf.turnoverOn && decode_pdf.getDisplayView()==Display.FACING) {
                drawingTurnover = false;

                boolean dragLeft = currentGUI.getDragLeft();
                boolean dragTop = currentGUI.getDragTop();

                if (lastPress+200 > System.currentTimeMillis()) {
                    if (dragLeft)
                        currentCommands.executeCommand(Commands.BACKPAGE, null);
                    else
                        currentCommands.executeCommand(Commands.FORWARDPAGE, null);
                    previewTurnover = false;
                    currentGUI.setCursor(SwingGUI.DEFAULT_CURSOR);
                } else {
                //Trigger fall
                Point corner = new Point();
                corner.y = decode_pdf.getInsetH();
                if (!dragTop)
                    corner.y+=((decode_pdf.getPdfPageData().getCropBoxHeight(1)*decode_pdf.getScaling()));

                if (dragLeft)
                    corner.x = (int) ((decode_pdf.getVisibleRect().getWidth()/2)-(decode_pdf.getPdfPageData().getCropBoxWidth(1)*decode_pdf.getScaling()));
                else
                    corner.x = (int) ((decode_pdf.getVisibleRect().getWidth()/2)+(decode_pdf.getPdfPageData().getCropBoxWidth(1)*decode_pdf.getScaling()));

                MouseMotionListener[] listeners = decode_pdf.getMouseMotionListeners();
                if (mover==null) {
                    for (int i=0; i< listeners.length; i++) {
                        if (listeners[i] instanceof mouse_mover) {
                            mover = ((mouse_mover)listeners[i]);
                        }
                    }
                }
                mover.testFall(corner,event.getPoint(),dragLeft);
                }
            }

            //stop middle click panning
            if (event.getButton() == MouseEvent.BUTTON2) {
                xVelocity = 0;
                yVelocity = 0;
                currentGUI.setCursor(SwingGUI.DEFAULT_CURSOR);
                middleDragTimer.stop();
                decode_pdf.repaint();
            }

		}

		public void mouseExited(MouseEvent arg0) {
		}
	}
	
	protected class mouse_wheel implements MouseWheelListener{
        long timeOfLastPageChange;
		public void mouseWheelMoved(MouseWheelEvent arg0) {
            if(decode_pdf.getDisplayView() == Display.PAGEFLOW3D)
                return;
            
			if(currentGUI.getProperties().getValue("allowScrollwheelZoom").toLowerCase().equals("true") && arg0.isControlDown()){
                //zoom
				int scaling = currentGUI.getSelectedComboIndex(Commands.SCALING);
				if(scaling!=-1){
					scaling = (int)decode_pdf.getDPIFactory().removeScaling(decode_pdf.getScaling()*100);
				}else{
                    String numberValue = (String)currentGUI.getSelectedComboItem(Commands.SCALING);
                    try{
						scaling= (int)Float.parseFloat(numberValue);
					}catch(Exception e){
						scaling=-1;
						//its got characters in it so get first valid number string
						int length=numberValue.length();
						int ii=0;
						while(ii<length){
							char c=numberValue.charAt(ii);
							if(((c>='0')&&(c<='9'))|(c=='.'))
								ii++;
							else
								break;
						}

						if(ii>0)
							numberValue=numberValue.substring(0,ii);

						//try again if we reset above
						if(scaling==-1){
							try{
								scaling = (int)Float.parseFloat(numberValue);
							}catch(Exception e1){scaling=-1;}
						}
					}
				}

				float value = arg0.getWheelRotation();

				if(scaling!=1 || value<0){
					if(value<0){
						value = 1.25f;
					}else{
						value = 0.8f;
					}
					if(!(scaling+value<0)){
						float currentScaling = (scaling*value);

                        //kieran - is this one of yours?
                        //
						if(((int)currentScaling)==(scaling))
							currentScaling=scaling+1;
						else
							currentScaling = ((int)currentScaling);

						if(currentScaling<1)
							currentScaling=1;

                        if(currentScaling>1000)
                            currentScaling=1000;

                        //store mouse location
                        final Rectangle r = decode_pdf.getVisibleRect();
                        final double x = arg0.getX()/decode_pdf.getBounds().getWidth();
                        final double y = arg0.getY()/decode_pdf.getBounds().getHeight();

                        //update scaling
                        currentGUI.snapScalingToDefaults(currentScaling);

                        //center on mouse location
                        Thread t = new Thread() {
                            public void run() {
                                try {
                                    decode_pdf.scrollRectToVisible(new Rectangle(
                                            (int)((x*decode_pdf.getWidth())-(r.getWidth()/2)),
                                            (int)((y*decode_pdf.getHeight())-(r.getHeight()/2)),
                                            (int)decode_pdf.getVisibleRect().getWidth(),
                                            (int)decode_pdf.getVisibleRect().getHeight()));
                                    decode_pdf.repaint();
                                } catch (Exception e) {e.printStackTrace();}
                            }
                        };
                        t.start();
                        SwingUtilities.invokeLater(t);
                    }
                }
			} else {

                final JScrollBar scroll = ((JScrollPane)decode_pdf.getParent().getParent()).getVerticalScrollBar();
                if ((scroll.getValue()>=scroll.getMaximum()-scroll.getHeight() || scroll.getHeight()==0) &&
                        arg0.getUnitsToScroll() > 0 &&
                        timeOfLastPageChange+700 < System.currentTimeMillis() &&
                        currentGUI.getPageNumber() < decode_pdf.getPageCount()) {

                    //change page
                    timeOfLastPageChange = System.currentTimeMillis();
                    currentCommands.executeCommand(Commands.FORWARDPAGE, null);

                    //update scrollbar so at top of page
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            scroll.setValue(scroll.getMinimum());
                        }
                    });

                } else if (scroll.getValue()==scroll.getMinimum() &&
                        arg0.getUnitsToScroll() < 0 &&
                        timeOfLastPageChange+700 < System.currentTimeMillis() &&
                        currentGUI.getPageNumber() > 1) {

                    //change page
                    timeOfLastPageChange = System.currentTimeMillis();
                    currentCommands.executeCommand(Commands.BACKPAGE, null);

                    //update scrollbar so at bottom of page
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            scroll.setValue(scroll.getMaximum());
                        }
                    });

                } else {
                    //scroll
                    Area rect = new Area(decode_pdf.getVisibleRect());
                    AffineTransform transform = new AffineTransform();
                    transform.translate(0, arg0.getUnitsToScroll() * decode_pdf.getScrollInterval());
                    rect = rect.createTransformedArea(transform);
                    decode_pdf.scrollRectToVisible(rect.getBounds());
                }
            }
        }
    }
	
	/**listener used to update display*/
	protected class mouse_mover implements MouseMotionListener {
		
		boolean altIsDown = false;

		public mouse_mover() {}

		public void mouseDragged(MouseEvent event) {
			if(SwingUtilities.isLeftMouseButton(event)){
				altIsDown = event.isAltDown();
				//dragged = true;
//				int[] values = updateXY(event);
//				commonValues.m_x2=values[0];
//				commonValues.m_y2=values[1];

				scrollAndUpdateCoords(event);

				if(commonValues.isPDF())
					generateNewCursorBox();

				if(currentGUI.addUniqueIconToFileAttachment)
					checkLinks(false,decode_pdf.getIO());


                //update mouse coords for turnover
                if (decode_pdf.turnoverOn && (drawingTurnover || previewTurnover) && decode_pdf.getDisplayView()==Display.FACING) {
                    currentGUI.setCursor(SwingGUI.GRABBING_CURSOR);

                    //update coords
                    if (currentGUI.getDragLeft()) {
                        if (currentGUI.getDragTop())
                            decode_pdf.setUserOffsets(event.getX(), event.getY(), org.jpedal.external.OffsetOptions.INTERNAL_DRAG_CURSOR_TOP_LEFT);
                        else
                            decode_pdf.setUserOffsets(event.getX(), event.getY(), org.jpedal.external.OffsetOptions.INTERNAL_DRAG_CURSOR_BOTTOM_LEFT);
                    } else {
                        if (currentGUI.getDragTop())
                            decode_pdf.setUserOffsets(event.getX(), event.getY(), org.jpedal.external.OffsetOptions.INTERNAL_DRAG_CURSOR_TOP_RIGHT);
                        else
                            decode_pdf.setUserOffsets(event.getX(), event.getY(), org.jpedal.external.OffsetOptions.INTERNAL_DRAG_CURSOR_BOTTOM_RIGHT);
                    }
                }



			} else if (SwingUtilities.isMiddleMouseButton(event)) {
                //middle drag - update velocity
                xVelocity = ((event.getX() - decode_pdf.getVisibleRect().getX()) - middleDragStartX)/4;
                yVelocity = ((event.getY() - decode_pdf.getVisibleRect().getY()) - middleDragStartY)/4;
            }

		}

		/**
		 * generate new  cursorBox and highlight extractable text,
		 * if hardware acceleration off and extraction on<br>
		 * and update current cursor box displayed on screen
		 */
		protected void generateNewCursorBox() {
			
			//redraw rectangle of dragged box onscreen if it has changed significantly
			if ((old_m_x2!=-1)|(old_m_y2!=-1)|(Math.abs(commonValues.m_x2-old_m_x2)>5)|(Math.abs(commonValues.m_y2-old_m_y2)>5)) {	

				//allow for user to go up
				int top_x = commonValues.m_x1;
				if (commonValues.m_x1 > commonValues.m_x2)
					top_x = commonValues.m_x2;
				int top_y = commonValues.m_y1;
				if (commonValues.m_y1 > commonValues.m_y2)
					top_y = commonValues.m_y2;
				int w = Math.abs(commonValues.m_x2 - commonValues.m_x1);
				int h = Math.abs(commonValues.m_y2 - commonValues.m_y1);

				//add an outline rectangle  to the display
				Rectangle currentRectangle=new Rectangle (top_x,top_y,w,h);
				currentGUI.setRectangle(currentRectangle);
				
                //tell JPedal to highlight text in this area (you can add other areas to array)
                decode_pdf.updateCursorBoxOnScreen(currentRectangle,PdfDecoder.highlightColor);
				if(!decode_pdf.isExtractingAsImage()){
					int type = decode_pdf.getDynamicRenderer().getObjectUnderneath(commonValues.m_x1, commonValues.m_y1);
					
					if((altIsDown &&
							(type!=DynamicVectorRenderer.TEXT && type!=DynamicVectorRenderer.TRUETYPE && 
									type!=DynamicVectorRenderer.TYPE1C && type!=DynamicVectorRenderer.TYPE3))){

						//Remove previous highlights to prevent overlaps
						decode_pdf.clearHighlights();
						
						if(decode_pdf.getDisplayView()!=Display.SINGLE_PAGE &&
								decode_pdf.getDisplayView()!=Display.PAGEFLOW &&
								decode_pdf.getDisplayView()!=Display.PAGEFLOW3D){
							int page = commonValues.getCurrentPage();
							while(currentRectangle.y<(decode_pdf.getPageOffsets(page).y/decode_pdf.getScaling()) && page>0){
								page--;
								decode_pdf.addHighlights(new Rectangle[]{currentRectangle}, true, page);
							}

							page = commonValues.getCurrentPage();
							while(currentRectangle.y>(decode_pdf.getPageOffsets(page).y/decode_pdf.getScaling())+decode_pdf.getPdfPageData().getCropBoxHeight(page) && page>0){
								page++;
								decode_pdf.addHighlights(new Rectangle[]{currentRectangle}, true, page);
							}
						}
						//Highlight all within the rectangle
						decode_pdf.addHighlights(new Rectangle[]{currentRectangle}, true, commonValues.getCurrentPage());
						//decode_pdf.setMouseHighlightArea(currentRectangle);

					}else{ //Find start and end locations and highlight all object in order in between
						Rectangle r = new Rectangle(commonValues.m_x1, commonValues.m_y1,commonValues.m_x2 - commonValues.m_x1, commonValues.m_y2-commonValues.m_y1);
						
						//When not in single page mode coords are handled backward
						if(decode_pdf.getDisplayView()!=Display.SINGLE_PAGE)
							r = new Rectangle(commonValues.m_x1, commonValues.m_y1,commonValues.m_x2 - commonValues.m_x1, commonValues.m_y1-commonValues.m_y2);
						
						if(decode_pdf.getDisplayView()!=Display.SINGLE_PAGE &&
								decode_pdf.getDisplayView()!=Display.PAGEFLOW &&
								decode_pdf.getDisplayView()!=Display.PAGEFLOW3D){
							int page = commonValues.getCurrentPage();
							while((-r.y)<(decode_pdf.getPageOffsets(page).y/decode_pdf.getScaling()) && page>0){
								page--;
								Rectangle highlight = new Rectangle((r.x-decode_pdf.getPageOffsets(page).x)+decode_pdf.getInsetW(), (r.y-decode_pdf.getPageOffsets(page).y)+decode_pdf.getInsetH(), r.width, -r.height);
								decode_pdf.addHighlights(new Rectangle[]{highlight}, false, page);
							}

							page = commonValues.getCurrentPage();
							while((-r.y)>(decode_pdf.getPageOffsets(page).y/decode_pdf.getScaling())+decode_pdf.getPdfPageData().getCropBoxHeight(page) && page<commonValues.getPageCount()){
								page++;
								Rectangle highlight = new Rectangle(r.x-decode_pdf.getPageOffsets(page).x, (r.y)-decode_pdf.getPageOffsets(page).y, r.width, -r.height);
								decode_pdf.addHighlights(new Rectangle[]{highlight}, false, page);
							}
							Rectangle highlight = new Rectangle((r.x-decode_pdf.getPageOffsets(page).x)+decode_pdf.getInsetW(), (r.y-decode_pdf.getPageOffsets(page).y)+decode_pdf.getInsetH(), r.width, -r.height);
							decode_pdf.addHighlights(new Rectangle[]{highlight}, false, commonValues.getCurrentPage());
							
						}else{
							decode_pdf.addHighlights(new Rectangle[]{r}, false, commonValues.getCurrentPage());
							//decode_pdf.setFoundTextPoints(new Point(commonValues.m_x1, commonValues.m_y1), new Point(commonValues.m_x2, commonValues.m_y2));
						}
					}
				}
				//reset tracking
				old_m_x2=commonValues.m_x2;
				old_m_y2=commonValues.m_y2;

			}
		}
		
		public void mouseMoved(MouseEvent event) {

            //click to page cursor type
            if (decode_pdf.getDisplayView()==Display.PAGEFLOW) {

                //get raw w and h
                int rawW,rawH;
                if (currentGUI.getRotation()%180==90) {
                    rawW = decode_pdf.getPdfPageData().getCropBoxHeight(1);
                    rawH = decode_pdf.getPdfPageData().getCropBoxWidth(1);
                } else {
                    rawW = decode_pdf.getPdfPageData().getCropBoxWidth(1);
                    rawH = decode_pdf.getPdfPageData().getCropBoxHeight(1);
                }

                Point p = event.getPoint();
                double x = (int)p.getX();
                int y = (int)p.getY();

                float scaling = decode_pdf.getScaling();

                double pageHeight = scaling*rawH;
                int yStart = decode_pdf.getInsetH();

                //move so relative to center
                x = x - (decode_pdf.getWidth()/2);

                //Remove main page & add displayWidth for offsetting
                double halfMainPage = (rawW*0.5*scaling);
                double displayWidth = PageOffsets.getPageFlowPageWidth((int)(rawW*scaling),scaling);

                if (x < -halfMainPage)
                    x = x + halfMainPage - displayWidth;
                else if (x >= halfMainPage)
                    x = x - halfMainPage + displayWidth;
                else {
                    //Main page
                    decode_pdf.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                    return;
                }
                //must be side page
                //get click position as proportion of page (including hidden bit) from large end
                double proportion;
                if (x > 0)
                    proportion = (displayWidth - (x % displayWidth)) / (rawW*scaling*PageOffsets.PAGEFLOW_SIDE_SIZE*PageOffsets.PAGEFLOW_WIDTH_RATIO);
                else
                    proportion = (displayWidth - (-x % displayWidth)) / (rawW*scaling*PageOffsets.PAGEFLOW_SIDE_SIZE*PageOffsets.PAGEFLOW_WIDTH_RATIO);
                //calculate vertical reduction due to perspective
                double inset = proportion * (PageOffsets.PAGEFLOW_SIDE_SIZE * pageHeight * ((1-PageOffsets.PAGEFLOW_HEIGHT_RATIO)/2));

                //check if Y is acceptable
                if (y < yStart + ((1-PageOffsets.PAGEFLOW_SIDE_SIZE)*pageHeight/2)+inset) {
                    //Above page
                    decode_pdf.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                    return;
                }
                if (y > yStart + pageHeight - ((1-PageOffsets.PAGEFLOW_SIDE_SIZE)*pageHeight/2)-inset) {
                    //below page
                    decode_pdf.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                    return;
                }

                //Divide by width and add to current page to get page
                x = currentGUI.getCurrentPage()+(int)(x / displayWidth);

                pageFlowCurrentPage = (int) x;

                //If page exists, change cursor
                if (x > 0 && x <= decode_pdf.getPdfPageData().getPageCount())
                    decode_pdf.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                else
                    decode_pdf.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            }

            else if (decode_pdf.getDisplayView() == Display.FACING &&
                    decode_pdf.turnoverOn &&
                    ((SwingGUI)decode_pdf.getExternalHandler(Options.SwingContainer)).getPageTurnScalingAppropriate() &&
                    !decode_pdf.getPdfPageData().hasMultipleSizes() &&
                    !getPageTurnAnimating()) {
                //show preview turnover

                //get width and height of page
                float pageH = (decode_pdf.getPdfPageData().getCropBoxHeight(1)*decode_pdf.getScaling())-1;
                float pageW = (decode_pdf.getPdfPageData().getCropBoxWidth(1)*decode_pdf.getScaling())-1;

                if ((decode_pdf.getPdfPageData().getRotation(1)+currentGUI.getRotation())%180==90) {
                    float temp = pageH;
                    pageH = pageW+1;
                    pageW = temp;
                }

                final Point corner = new Point();

                //right turnover
                if (commonValues.getCurrentPage()+1 < commonValues.getPageCount()) {
                    corner.x = (int)((decode_pdf.getVisibleRect().getWidth()/2)+pageW);
                    corner.y = (int) (decode_pdf.getInsetH()+ pageH);

                    final Point cursor = event.getPoint();

                    if (cursor.x > corner.x-30 && cursor.x <= corner.x &&
                            ((cursor.y > corner.y-30 && cursor.y <= corner.y) ||
                            (cursor.y >= corner.y-pageH && cursor.y <corner.y-pageH+30))) {
                        //if close enough display preview turnover

                        //set cursor
                        currentGUI.setCursor(SwingGUI.GRAB_CURSOR);

                        previewTurnover = true;
                        if (cursor.y >= corner.y-pageH && cursor.y <corner.y-pageH+30) {
                            corner.y = (int) (corner.y - pageH);
                            decode_pdf.setUserOffsets((int)cursor.getX(), (int)cursor.getY(), org.jpedal.external.OffsetOptions.INTERNAL_DRAG_CURSOR_TOP_RIGHT);
                        } else
                            decode_pdf.setUserOffsets((int)cursor.getX(), (int)cursor.getY(), org.jpedal.external.OffsetOptions.INTERNAL_DRAG_CURSOR_BOTTOM_RIGHT);

                    } else {
                        if (currentGUI.getDragTop())
                            corner.y = (int) (corner.y - pageH);
                        testFall(corner, cursor, false);
                    }
                }

                //left turnover
                if (commonValues.getCurrentPage() != 1) {
                    corner.x = (int)((decode_pdf.getVisibleRect().getWidth()/2)-pageW);
                    corner.y = (int) (decode_pdf.getInsetH()+pageH);

                    final Point cursor = event.getPoint();

                    if (cursor.x < corner.x+30 && cursor.x >= corner.x &&
                            ((cursor.y > corner.y-30 && cursor.y <= corner.y) ||
                            (cursor.y >= corner.y-pageH && cursor.y < corner.y-pageH+30))) {
                        //if close enough display preview turnover
//                        System.out.println("drawing left live "+decode_pdf.drawLeft);
                        //set cursor
                        currentGUI.setCursor(SwingGUI.GRAB_CURSOR);

                        previewTurnover = true;
                        if (cursor.y >= corner.y-pageH && cursor.y < corner.y-pageH+30) {
                            corner.y = (int) (corner.y - pageH);
                            decode_pdf.setUserOffsets((int)cursor.getX(), (int)cursor.getY(), org.jpedal.external.OffsetOptions.INTERNAL_DRAG_CURSOR_TOP_LEFT);
                        } else
                            decode_pdf.setUserOffsets((int)cursor.getX(), (int)cursor.getY(), org.jpedal.external.OffsetOptions.INTERNAL_DRAG_CURSOR_BOTTOM_LEFT);

                    } else {
                        if (currentGUI.getDragTop())
                            corner.y = (int) (corner.y - pageH);
                        testFall(corner,cursor, true);
                    }
                }

            }

            //<start-adobe>
            //Update cursor position if over page in single mode        
            if (currentGUI.useNewLayout) {
                int[] flag = new int[2];
                flag[0] = SwingGUI.CURSOR;

                if (decode_pdf.getDisplayView() == Display.SINGLE_PAGE || SwingMouseSelection.activateMultipageHighlight) {
                    
                	int page = 1;
                	
                	if(decode_pdf.getDisplayView()!= Display.SINGLE_PAGE && SwingMouseSelection.activateMultipageHighlight){
                		page = commonValues.getCurrentPage();
                	}
                	
                	//get raw w and h
                    int rawW,rawH;
                    if (currentGUI.getRotation()%180==90) {
                        rawW = decode_pdf.getPdfPageData().getCropBoxHeight(page);
                        rawH = decode_pdf.getPdfPageData().getCropBoxWidth(page);
                    } else {
                        rawW = decode_pdf.getPdfPageData().getCropBoxWidth(page);
                        rawH = decode_pdf.getPdfPageData().getCropBoxHeight(page);
                    }

                    Point p = event.getPoint();
                    int x = (int)p.getX();
                    int y = (int)p.getY();

                    float scaling = decode_pdf.getScaling();

                    double pageHeight = scaling*rawH;
                    double pageWidth = scaling*rawW;
                    int yStart = decode_pdf.getInsetH();

                  //move so relative to center
                    double left = (decode_pdf.getWidth()/2) - (pageWidth/2);
                    
                    if(decode_pdf.getDisplayView()==Display.CONTINUOUS && SwingMouseSelection.activateMultipageHighlight){
            				float cs=currentGUI.getScaling();
            				int inset=currentGUI.getPDFDisplayInset();
            				int rotation=currentGUI.getRotation();

            				int ex=currentGUI.AdjustForAlignment(x)-inset;
            				int ey=y-inset;

            				//undo any viewport scaling
            				if(commonValues.maxViewY!=0){ // will not be zero if viewport in play
            					ex=(int)(((ex-(commonValues.dx*cs))/commonValues.viewportScale));
            					ey=(int)((currentGUI.mediaH-((currentGUI.mediaH-(ey/cs)-commonValues.dy)/commonValues.viewportScale))*cs);
            				}

            				cx=(int)((ex)/cs);
            				cy=(int)((ey/cs));

            				int cp = commonValues.getCurrentPage();

            				if (x >= left && x <= left+pageWidth){
            					if(cy<((decode_pdf.getPageOffsets(cp).y/cs)-(inset/cs)) && cp > 1)
            						flag[1] = 1;
            					else if(cp < commonValues.getPageCount() && cy>((decode_pdf.getPageOffsets(cp+1).y/cs)-(inset/cs)))
            						flag[1] = 1;
            					else //if(cy>decode_pdf.getPageOffsets(cp).y && cy<decode_pdf.getPageOffsets(cp).y+rawH)
            						flag[1] = 1;
            				}else{
            					flag[1] = 0;
            				}
                    }else{

                    	if (x >= left && x <= left+pageWidth &&
                    			y >= yStart && y <= yStart + pageHeight)
                    		//set displayed
                    		flag[1] = 1;
                    	else
                    		//set not displayed
                    		flag[1] = 0;
                    }
                } else {
                    //set not displayed
                    flag[1] = 0;
                }
                currentGUI.setMultibox(flag);
            }
            //<end-adobe>

			updateCoords(event.getX(), event.getY(), event.isShiftDown());
			if(currentGUI.addUniqueIconToFileAttachment)
				checkLinks(false,decode_pdf.getIO());
			

		}

        public void testFall(final Point corner, final Point cursor, boolean testLeft) {
            if (!previewTurnover)
                return;

            float width = (decode_pdf.getPdfPageData().getCropBoxWidth(1)*decode_pdf.getScaling())-1;

            if ((decode_pdf.getPdfPageData().getRotation(1)+currentGUI.getRotation())%180==90) {
                width = decode_pdf.getPdfPageData().getCropBoxHeight(1)*decode_pdf.getScaling();
            }

            final float pageW = width;

            if (!testLeft) {
                if (!currentGUI.getDragLeft()) {
                    //reset cursor
                    decode_pdf.setCursor(Cursor.getDefaultCursor());

                    //If previously displaying turnover, animate to corner
                    Thread animation = new Thread() {
                        public void run() {

                            corner.x = (int)((decode_pdf.getVisibleRect().getWidth()/2)+pageW);
                            //work out if page change needed
                            boolean fallBack = true;
                            if (cursor.x < corner.x- pageW) {
                                corner.x = (int)(corner.x - (2* pageW));
                                fallBack = false;
                            }

                            // Fall animation
                            int velocity = 1;

                            //ensure cursor is not outside expected range
                            if (fallBack && cursor.x >= corner.x)
                                cursor.x = corner.x-1;
                            if (!fallBack && cursor.x <= corner.x)
                                cursor.x = corner.x+1;
                            if (!currentGUI.getDragTop() && cursor.y >= corner.y)
                                cursor.y = corner.y-1;
                            if (currentGUI.getDragTop() && cursor.y <= corner.y)
                                cursor.y = corner.y+1;

                            //Calculate distance required
                            double distX = (corner.x-cursor.x);
                            double distY = (corner.y-cursor.y);

                            //Loop through animation
                            while ((fallBack && cursor.getX() <= corner.getX()) ||
                                    (!fallBack && cursor.getX() >= corner.getX()) ||
                                    (!currentGUI.getDragTop() && cursor.getY() <= corner.getY()) ||
                                    (currentGUI.getDragTop() && cursor.getY() >= corner.getY())) {

                                //amount to move this time
                                double xMove = velocity*distX*0.002;
                                double yMove = velocity*distY*0.002;

                                //make sure always moves at least 1 pixel in each direction
                                if (Math.abs(xMove) < 1)
                                    xMove = xMove/Math.abs(xMove);
                                if (Math.abs(yMove) < 1)
                                    yMove = yMove/Math.abs(yMove);

                                cursor.setLocation(cursor.getX() + xMove, cursor.getY() + yMove);
                                if (currentGUI.getDragTop())
                                    decode_pdf.setUserOffsets((int)cursor.getX(), (int)cursor.getY(), org.jpedal.external.OffsetOptions.INTERNAL_DRAG_CURSOR_TOP_RIGHT);
                                else
                                    decode_pdf.setUserOffsets((int)cursor.getX(), (int)cursor.getY(), org.jpedal.external.OffsetOptions.INTERNAL_DRAG_CURSOR_BOTTOM_RIGHT);

                                //Double speed til moving 32/frame
                                if (velocity < 32)
                                    velocity = velocity*2;

                                //sleep til next frame
                                try {
                                    Thread.sleep(50);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                            }

                            if (!fallBack) {
                                //calculate page to turn to
                                int forwardPage = commonValues.getCurrentPage()+1;
                                if (decode_pdf.separateCover && forwardPage%2==1)
                                    forwardPage++;
                                else if (!decode_pdf.separateCover && forwardPage%2==0)
                                    forwardPage++;

                                //change page
                                commonValues.setCurrentPage(forwardPage);
                                currentGUI.setPageNumber();
                                decode_pdf.setPageParameters(currentGUI.getScaling(), commonValues.getCurrentPage());
                                currentGUI.decodePage(false);
                            }

                            //hide turnover
                            decode_pdf.setUserOffsets(0,0,org.jpedal.external.OffsetOptions.INTERNAL_DRAG_BLANK);
                            setPageTurnAnimating(false);
                        }
                    };

                    setPageTurnAnimating(true);
                    animation.start();
                    previewTurnover = false;
                }
            } else {
                if (previewTurnover && currentGUI.getDragLeft()) {
                    //reset cursor
                    decode_pdf.setCursor(Cursor.getDefaultCursor());

                    //If previously displaying turnover, animate to corner
                    Thread animation = new Thread() {
                        public void run() {

                            corner.x = (int)((decode_pdf.getVisibleRect().getWidth()/2)-pageW);
                            //work out if page change needed
                            boolean fallBack = true;
                            if (cursor.x > corner.x+pageW) {
                                corner.x = (int)(corner.x + (2*pageW));
                                fallBack = false;
                            }

                            // Fall animation
                            int velocity = 1;

                            //ensure cursor is not outside expected range
                            if (!fallBack && cursor.x >= corner.x)
                                cursor.x = corner.x-1;
                            if (fallBack && cursor.x <= corner.x)
                                cursor.x = corner.x+1;
                            if (!currentGUI.getDragTop() && cursor.y >= corner.y)
                                cursor.y = corner.y-1;
                            if (currentGUI.getDragTop() && cursor.y <= corner.y)
                                cursor.y = corner.y+1;

                            //Calculate distance required
                            double distX = (corner.x-cursor.x);
                            double distY = (corner.y-cursor.y);

                            //Loop through animation
                            while ((!fallBack && cursor.getX() <= corner.getX()) ||
                                    (fallBack && cursor.getX() >= corner.getX()) ||
                                    (!currentGUI.getDragTop() && cursor.getY() <= corner.getY()) ||
                                    (currentGUI.getDragTop() && cursor.getY() >= corner.getY())) {

                                //amount to move this time
                                double xMove = velocity*distX*0.002;
                                double yMove = velocity*distY*0.002;

                                //make sure always moves at least 1 pixel in each direction
                                if (Math.abs(xMove) < 1)
                                    xMove = xMove/Math.abs(xMove);
                                if (Math.abs(yMove) < 1)
                                    yMove = yMove/Math.abs(yMove);

                                cursor.setLocation(cursor.getX() + xMove, cursor.getY() + yMove);
                                if (currentGUI.getDragTop())
                                    decode_pdf.setUserOffsets((int)cursor.getX(), (int)cursor.getY(), org.jpedal.external.OffsetOptions.INTERNAL_DRAG_CURSOR_TOP_LEFT);
                                else
                                    decode_pdf.setUserOffsets((int)cursor.getX(), (int)cursor.getY(), org.jpedal.external.OffsetOptions.INTERNAL_DRAG_CURSOR_BOTTOM_LEFT);

                                //Double speed til moving 32/frame
                                if (velocity < 32)
                                    velocity = velocity*2;

                                //sleep til next frame
                                try {
                                    Thread.sleep(50);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                            }

                            if (!fallBack) {
                                //calculate page to turn to
                                int backPage = commonValues.getCurrentPage()-2;
                                if (backPage == 0)
                                    backPage = 1;

                                //change page
                                commonValues.setCurrentPage(backPage);
                                currentGUI.setPageNumber();
                                decode_pdf.setPageParameters(currentGUI.getScaling(), commonValues.getCurrentPage());
                                currentGUI.decodePage(false);
                            }

                            //hide turnover
                            decode_pdf.setUserOffsets(0, 0,org.jpedal.external.OffsetOptions.INTERNAL_DRAG_BLANK);
                            setPageTurnAnimating(false);
                        }
                    };

                    setPageTurnAnimating(true);
                    animation.start();
                    previewTurnover = false;
                }
            }
        }

    }


	/**listener used to update display*/
	protected class Extractor_mouse_clicker extends mouse_clicker {

		public void mousePressed(MouseEvent event){
			Rectangle currentRectangle=currentGUI.getRectangle();
			if(currentRectangle==null){
				//draw the first cursor box on screen
				super.mousePressed(event);

				//ensure we keep drawing the new cursor box
				drawingCursorBox = true;
			}else{
				int[] values = updateXY(event);

				//store current cursor point for use when dragging
				oldX=values[0];
				oldY=values[1];
			}
		}

		public void mouseReleased(MouseEvent event) {
			//turn off drawing new cursor box
			drawingCursorBox = false;
			
			old_m_x2 = -1;
			old_m_y2 = -1;

			updateCoords(event.getX(), event.getY(), event.isShiftDown());

			/* shuffle points to ensure cursorBox is setup correctly */
			int tmp;
			if(commonValues.m_x1>commonValues.m_x2){
				tmp=commonValues.m_x1;
				commonValues.m_x1=commonValues.m_x2;
				commonValues.m_x2=tmp;
			}
			if(commonValues.m_y1<commonValues.m_y2){
				tmp=commonValues.m_y1;
				commonValues.m_y1=commonValues.m_y2;
				commonValues.m_y2=tmp;
			}

            decode_pdf.repaint();//redraw

			//turn altering of current cursor box off
			dragAltering=false;
			//dragged = false;
		}
	}



	/**listener used to update display*/
	protected class Extractor_mouse_mover extends mouse_mover {

		public void mouseDragged(MouseEvent event) {
			//dragged = true;
			altIsDown = event.isAltDown();
			Rectangle currentRectangle=currentGUI.getRectangle();
			//if no rectangle or currently drawing a new rectangle
			//use simpleViewer mouseDragged
			if(currentRectangle==null || drawingCursorBox){
				decode_pdf.setDrawCrossHairs(true,boxContained,Color.red);
				super.mouseDragged(event);
				boxContained=-1;
				return;
			}

			int[] values = updateXY(event);

			//generate handle boxes
			boxes=createNewRectangles(currentRectangle);

			//test if cursor was in cursor box handles when drag started
			//if we already have a handle selected don't look again
			if(boxContained==-1){
				for(int i=0;i<boxes.length;i++){
					if(boxes[i].contains(oldX,oldY)){
						boxContained = i;
						break;
					}
				}
			}

			//if there is a selected handle or we are altering the current cursor box
			if(boxContained!=-1 || dragAltering){

				//turn new rectangle drawing off
				drawingCursorBox = false;


				//initialise box to be highlighted with current selected handle
				int highlightBox=boxContained;

				//get centre coords of selected box
				int boxCenterX = (int)boxes[boxContained].getCenterX();
				int boxCenterY = (int)boxes[boxContained].getCenterY();

//				boolean top=false,bottom=false,left=false,right=false;//Checking code
				/**check which line is to be altered in the x axis and change cursor box values*/
				if(currentRectangle.x==boxCenterX){//left
					commonValues.m_x1=values[0];
//					left =true;//Checking code
				}else if(currentRectangle.x+currentRectangle.width ==boxCenterX){//right
					commonValues.m_x2=values[0];
//					right =true;//Checking code
				}

				/**check which line is to be altered in the y axis and change cursor box values*/
				if(currentRectangle.y==boxCenterY){//bottom
					commonValues.m_y2=values[1];
//					bottom =true;//Checking code
				}else if(currentRectangle.y+currentRectangle.height ==boxCenterY){//top
					commonValues.m_y1=values[1];
//					top=true;//Checking code
				}

//				System.out.println("top="+top+" bottom="+bottom+" left="+left+" right="+right+" "+highlightBox);//Checking code
				/**
				 * work out whether the handle highlight should be changed
				 * and which way it should be changed
				 */
				boolean changeX=false,changeY=false;
				if(commonValues.m_x1>commonValues.m_x2){
					changeX=true;
				}
				if(commonValues.m_y2>commonValues.m_y1){
					changeY=true;
				}

				/**if a highlight should be changed, change it*/
				if(changeX || changeY){
					switch(highlightBox){
					case 0://left
						if(changeX)
							highlightBox = 3;//change to right
						//				    else if(!left)//Checking code
						//				        System.err.println("error 1");//Checking code
						break;

					case 1://bottom
						if(changeY)
							highlightBox = 2;//change to top
						//				    else if(!bottom)//Checking code
						//				        System.err.println("error 2");//Checking code
						break;

					case 2://top
						if(changeY)
							highlightBox = 1;//change to bottom
						//				    else if(!top)//Checking code
						//				        System.err.println("error 3");//Checking code
						break;

					case 3://right
						if(changeX)
							highlightBox = 0;//change to left
						//				    else if(!right)//Checking code
						//				        System.err.println("error 4");//Checking code
						break;

					case 4://bottom left
						if(changeX)
							highlightBox = 6;//change to bottom right
						else if(changeY)
							highlightBox = 5;//change to top left
						if(changeX && changeY)
							highlightBox = 7;//change to top right
						//				    if((!left) || (!bottom))//Checking code
						//				        System.err.println("error 5");//Checking code
						break;

					case 5://top left
						if(changeX)
							highlightBox = 7;//change to top right
						else if(changeY)
							highlightBox = 4;//change to bottom left
						if(changeX && changeY)
							highlightBox = 6;//change to bottom right
						//				    if((!left) || (!top))//Checking code
						//				        System.err.println("error 7");//Checking code
						break;

					case 6://bottom right
						if(changeX)
							highlightBox = 4;//change to bottom left
						else if(changeY)
							highlightBox = 7;//change to top right
						if(changeX && changeY)
							highlightBox = 5;//change to top left
						//			        if((!right) || (!bottom))//Checking code
						//				        System.err.println("error 9");//Checking code
						break;

					case 7://top right
						if(changeX)
							highlightBox = 5;//change to top left
						else if(changeY)
							highlightBox = 6;//change to bottom right
						if(changeX && changeY)
							highlightBox =4;//change to bottom left
						//		            if((!right) || (!top))//Checking code
						//				        System.err.println("error 11");//Checking code
						break;

						//				default://Checking code
						//	                System.out.println("ERROR default");//Checking code
					}
				}

				//ensure crosshairs are drawn, and set current highlighted box to be drawn red
				decode_pdf.setDrawCrossHairs(true,highlightBox,Color.red);

				/**
				 * we have now changed the cursor coords, commonValues.m_x1 commonValues.m_y1 commonValues.m_x2 commonValues.m_y2
				 * So now update displayed coords and cursor box on screen
				 */
				scrollAndUpdateCoords(event);
				generateNewCursorBox();

				//ensure we are altering the current cursor box and don't draw new one
				dragAltering=true;

				//store current cursor point for comparison next time
				oldX=values[0];
				oldY=values[1];

			}else{
				/**
				 * if there is no selected handle on drag, draw new cursorbox
				 */
				drawingCursorBox = true;

				//ensure highlight is not drawn
				boxContained=-1;

				decode_pdf.setDrawCrossHairs(true,boxContained,Color.red);

				//setup start point of new cursor box
				commonValues.m_x1=oldX;
				commonValues.m_y1=oldY;

				//setup current point for new cursor box
				commonValues.m_x2=values[0];
				commonValues.m_y2=values[1];

				scrollAndUpdateCoords(event);
				generateNewCursorBox();
			}
		}

		//variables used only in mouseMoved
		private boolean inRect=false;//whether cursor currently in cursor box
		private boolean handleChange=false;//whether the highlight should be changed

		public void mouseMoved(MouseEvent event) {

			super.mouseMoved(event);
			Rectangle currentRectangle=currentGUI.getRectangle();
			//generate handle boxes
			boxes=createNewRectangles(currentRectangle);

			//find which handle, if any cursor is in
			if(boxes!=null){
				int oldBox = boxContained;//save old selected value
				boxContained = -1;//reset current selected highlight

				for(int i=0;i<boxes.length;i++){
					if(boxes[i].contains(cx,cy)){
						boxContained = i;
						break;
					}
				}

				//if we find a handle and it is not already selected to highlight ensure redraw
				if(boxContained!=oldBox){
					handleChange = true;
				}
			}

			//if cursor in cursorbox or within handleGap pixels of it show crosshairs
			if(currentRectangle!=null){
				if((currentRectangle.x-handlesGap)<cx && (currentRectangle.x+currentRectangle.width+handlesGap)>cx &&
						(currentRectangle.y-handlesGap)<cy && (currentRectangle.y+currentRectangle.height+handlesGap)>cy){
					//cursor is in cursor box

					decode_pdf.setDrawCrossHairs(true,boxContained,Color.red);

					//if was not in rectangle repaint display
					if(!inRect || handleChange){
                        decode_pdf.repaint();
						handleChange=false;
						inRect=true;
					}
				}else{
					//cursor is NOT in cursor box

					decode_pdf.setDrawCrossHairs(false,boxContained,Color.red);

					//if was in rectangle repaint display
					if(inRect || handleChange){
                        decode_pdf.repaint();
						handleChange=false;
						inRect=false;
					}
				}
			}
		}

		/**
		 * creates the eight cursor box handles for the cursor box<br>
		 * returns Rectangle[] whos indexes are the same as those used to display them on screen<br>
		 */
		private Rectangle[] createNewRectangles(Rectangle currentRectangle) {
			if(currentRectangle!=null){

				int x1 = currentRectangle.x;
				int y1 = currentRectangle.y;
				int x2 = x1+currentRectangle.width;
				int y2 = y1+currentRectangle.height;

				Rectangle[] cursorBoxHandles = new Rectangle[8];
				//*draw centre of line handle boxs
				//left
				cursorBoxHandles[0] = new Rectangle(x1-handlesGap,(y1+(Math.abs(y2-y1))/2)-handlesGap,handlesGap*2,handlesGap*2);//0
				//bottom
				cursorBoxHandles[1] = new Rectangle((x1+(Math.abs(x2-x1))/2)-handlesGap,y1-handlesGap,handlesGap*2,handlesGap*2);//1
				//top
				cursorBoxHandles[2] = new Rectangle((x1+(Math.abs(x2-x1))/2)-handlesGap,y2-handlesGap,handlesGap*2,handlesGap*2);//2
				//right
				cursorBoxHandles[3] = new Rectangle(x2-handlesGap,(y1+(Math.abs(y2-y1))/2)-handlesGap,handlesGap*2,handlesGap*2);//3
				/**/

				//*draw corner handles
				//bottom left
				cursorBoxHandles[4] = new Rectangle(x1-handlesGap,y1-handlesGap,handlesGap*2,handlesGap*2);//4
				//top left
				cursorBoxHandles[5] = new Rectangle(x1-handlesGap,y2-handlesGap,handlesGap*2,handlesGap*2);//5
				//bottom right
				cursorBoxHandles[6] = new Rectangle(x2-handlesGap,y1-handlesGap,handlesGap*2,handlesGap*2);//6
				//top right
				cursorBoxHandles[7] = new Rectangle(x2-handlesGap,y2-handlesGap,handlesGap*2,handlesGap*2);//7
				/**/

				return cursorBoxHandles;
			}
			return null;
		}
	}



	public SwingMouseHandler(PdfDecoder decode_pdf, SwingGUI currentGUI,
			Values commonValues,Commands currentCommands) {

		this.decode_pdf=decode_pdf;
		this.currentGUI=currentGUI;
		this.commonValues=commonValues;
		this.currentCommands=currentCommands;

        SwingMouseSelection sms = new SwingMouseSelection(decode_pdf, commonValues, this);
        sms.setupMouse();
        decode_pdf.setMouseMode(PdfDecoder.MOUSE_MODE_TEXT_SELECT);

		decode_pdf.addExternalHandler(this, Options.SwingMouseHandler);
		
	}


	/**
	 * checks the link areas on the page and allow user to save file
	 **/
	public void checkLinks(boolean mouseClicked, PdfObjectReader pdfObjectReader){


		//get 'hotspots' for the page
		Map objs=currentGUI.getHotspots();

        //look for a match
		if(objs!=null){

			//new code to check for match
			Iterator objKeys=objs.keySet().iterator();
			FormObject annotObj=null;
			while(objKeys.hasNext()){
				annotObj=(FormObject) objKeys.next();
				if(annotObj.getBoundingRectangle().contains(cx,cy)){
					break;
                }

				//reset to null so when exits no match
				annotObj=null;
			}

			/**action for moved over of clicked*/
			if(annotObj!=null){

				/**
				 * get EF object containing file data
				 */
				//annotObj is now actual object (on lazy initialisation so EF has not been read).....

				System.out.println(mouseClicked+" obj="+annotObj+" "+annotObj.getObjectRefAsString()+" "+annotObj.getBoundingRectangle());

				//@annot - in my example, ignore if not clicked
				if(!mouseClicked)
					return;
				
				//FS obj contains an EF obj which contains an F obj with the data in
				//F can be various - we are only interested in it as a Dictionary with a stream
				PdfObject EFobj=null, FSobj=annotObj.getDictionary(PdfDictionary.FS);
				if(FSobj!=null)
					EFobj=FSobj.getDictionary(PdfDictionary.EF);
                
				/**
				 * create the file chooser to select the file name
				 **/
				JFileChooser chooser = new JFileChooser(commonValues.getInputDir());
				chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				int state = chooser.showSaveDialog(currentGUI.getFrame());

				
				/**
				 * save file and take the hit
				 */
				if(state==0){
					File fileTarget = chooser.getSelectedFile();

					//here is where we take the hit (Only needed if on lazy init - ie Unread Dictionary like EF)....			
					if(EFobj!=null)
						pdfObjectReader.checkResolved(EFobj);

					//contains the actual file data
					PdfObject Fobj=EFobj.getDictionary(PdfDictionary.F);

					//see if cached or decoded (cached if LARGE)
					//IMPORTANT NOTE!!! - if the object is in a compressed stream (a 'blob' of
					//objects which we need to read in one go, it will not be cached
					String nameOnDisk=Fobj.getCachedStreamFile(pdfObjectReader.getObjectReader());
					
					//if you get null, make sure you have enabled caching and
					//file is bigger than cache
					System.out.println("file="+nameOnDisk);
					
					if(nameOnDisk!=null){ //just copy
						ObjectStore.copy(nameOnDisk,fileTarget.toString());
					}else{ //save out
						byte[] fileData=Fobj.getDecodedStream();

						if(fileData!=null){ //write out if in memory
							FileOutputStream fos;
							try {
								fos = new FileOutputStream(fileTarget);
								fos.write(fileData);
								fos.close();
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
				}
			}
		}
	}

	public void setupExtractor() {
		decode_pdf.addMouseMotionListener(new Extractor_mouse_mover());
		decode_pdf.addMouseListener(new Extractor_mouse_clicker());


	}

	/**
	 * scroll to visible Rectangle and update Coords box on screen
	 */
	protected void scrollAndUpdateCoords(MouseEvent event) {
        //scroll if user hits side
        int interval=decode_pdf.getScrollInterval();
		Rectangle visible_test=new Rectangle(currentGUI.AdjustForAlignment(event.getX()),event.getY(),interval,interval);
		if((currentGUI.allowScrolling())&&(!decode_pdf.getVisibleRect().contains(visible_test)))
                decode_pdf.scrollRectToVisible(visible_test);

        updateCoords(event.getX(), event.getY(), event.isShiftDown());
    }

	public void updateCordsFromFormComponent(MouseEvent e) {
		JComponent component = (JComponent) e.getSource();
		
		int x = component.getX() + e.getX();
		int y = component.getY() + e.getY();
		
		updateCoords(x, y, e.isShiftDown());
	}
	
	/**update current page co-ordinates on screen
	 */
	public void updateCoords(/*MouseEvent event*/int x, int y, boolean isShiftDown){
		
		float scaling=currentGUI.getScaling();
		int inset=currentGUI.getPDFDisplayInset();
		int rotation=currentGUI.getRotation();

		int ex=currentGUI.AdjustForAlignment(x)-inset;
		int ey=y-inset;

		//undo any viewport scaling
		if(commonValues.maxViewY!=0){ // will not be zero if viewport in play
			ex=(int)(((ex-(commonValues.dx*scaling))/commonValues.viewportScale));
			ey=(int)((currentGUI.mediaH-((currentGUI.mediaH-(ey/scaling)-commonValues.dy)/commonValues.viewportScale))*scaling);
		}

		cx=(int)((ex)/scaling);
		cy=(int)((ey/scaling));



		if(decode_pdf.getDisplayView()!=Display.SINGLE_PAGE && SwingMouseSelection.activateMultipageHighlight){
			if(decode_pdf.getDisplayView()==Display.CONTINUOUS){
				int page = commonValues.getCurrentPage();
				
//				System.out.println("cy before=="+cy);
				
				if(cy<((decode_pdf.getPageOffsets(page).y/scaling)-(inset/scaling)) && page > 1)
					cy = (int)((cy-(decode_pdf.getPageOffsets(page-1).y/scaling))+(inset/scaling));
				else if(page < commonValues.getPageCount() && cy>((decode_pdf.getPageOffsets(page+1).y/scaling)-(inset/scaling)))
					cy = (int)((cy-(decode_pdf.getPageOffsets(page+1).y/scaling))+(inset/scaling));
				else
					cy = (int)((cy-(decode_pdf.getPageOffsets(page).y/scaling))+(inset/scaling));

//				System.out.println("cy after=="+cy);
				
			}else{
				cx=0;
				cy=0;
			}
			//cx=decode_pdf.getMultiPageOffset(scaling,cx,commonValues.getCurrentPage(),Display.X_AXIS);
			//cy=decode_pdf.getMultiPageOffset(scaling,cy,commonValues.getCurrentPage(),Display.Y_AXIS);
		} else if(rotation==90){
			int tmp=(cx+currentGUI.cropY);
			cx = (cy+currentGUI.cropX);
			cy =tmp;	
		}else if((rotation==180)){
			cx =(currentGUI.cropW+currentGUI.cropX)-cx;
			cy =(cy+currentGUI.cropY);
		}else if((rotation==270)){
			int tmp=(currentGUI.cropH+currentGUI.cropY)-cx;
			cx =(currentGUI.cropW+currentGUI.cropX)-cy;
			cy =tmp;
		}else{
			cx = (cx+currentGUI.cropX);
			cy =(currentGUI.cropH+currentGUI.cropY)-cy;
		}


		if((commonValues.isProcessing())|(commonValues.getSelectedFile()==null))
			currentGUI.setCoordText("  X: "+ " Y: " + ' ' + ' '); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		else
			currentGUI.setCoordText("  X: " + cx + " Y: " + cy+ ' ' + ' ' +message); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

	}


	public void updateRectangle() {

		Rectangle currentRectangle=currentGUI.getRectangle();

		if(currentRectangle!=null){
			Rectangle newRect = decode_pdf.getCombinedAreas(currentRectangle,false);
			if(newRect!=null){
				commonValues.m_x1=newRect.x;
				commonValues.m_y2=newRect.y;
				commonValues.m_x2=newRect.x+newRect.width;
				commonValues.m_y1=newRect.y+newRect.height;

				currentRectangle=newRect;
				decode_pdf.updateCursorBoxOnScreen(currentRectangle,PdfDecoder.highlightColor);
                decode_pdf.repaint();
			}
		}

	}

	public void setupMouse() {
		/**
		 * track and display screen co-ordinates and support links
		 */
		decode_pdf.addMouseMotionListener(new mouse_mover());
		decode_pdf.addMouseListener(new mouse_clicker());
		decode_pdf.addMouseWheelListener(new mouse_wheel());
	}

	/**
	 * get raw co-ords and convert to correct scaled units
	 * @return int[] of size 2, [0]=new x value, [1] = new y value
	 */
	protected int[] updateXY(MouseEvent event) {

		float scaling=currentGUI.getScaling();
		int inset=currentGUI.getPDFDisplayInset();
		int rotation=currentGUI.getRotation();

		//get co-ordinates of top point of outine rectangle
		int x=(int)(((currentGUI.AdjustForAlignment(event.getX()))-inset)/scaling);
		int y=(int)((event.getY()-inset)/scaling);

		//undo any viewport scaling
		if(commonValues.maxViewY!=0){ // will not be zero if viewport in play
			x=(int)(((x-(commonValues.dx*scaling))/commonValues.viewportScale));
			y=(int)((currentGUI.mediaH-((currentGUI.mediaH-(y/scaling)-commonValues.dy)/commonValues.viewportScale))*scaling);
		}

		int[] ret=new int[2];
		if(rotation==90){	        
			ret[1] = x+currentGUI.cropY;
			ret[0] =y+currentGUI.cropX;
		}else if((rotation==180)){
			ret[0]=currentGUI.mediaW- (x+currentGUI.mediaW-currentGUI.cropW-currentGUI.cropX);
			ret[1] =y+currentGUI.cropY;
		}else if((rotation==270)){
			ret[1] =currentGUI.mediaH- (x+currentGUI.mediaH-currentGUI.cropH-currentGUI.cropY);
			ret[0]=currentGUI.mediaW-(y+currentGUI.mediaW-currentGUI.cropW-currentGUI.cropX);
		}else{
			ret[0] = x+currentGUI.cropX;
			ret[1] =currentGUI.mediaH-(y+currentGUI.mediaH-currentGUI.cropH-currentGUI.cropY);    
		}
		return ret;
	}

    public void setPageTurnAnimating(boolean a) {
        pageTurnAnimating = a;

        //disable buttons during animation
        if (a == true) {
            currentGUI.forward.setEnabled(false);
            currentGUI.back.setEnabled(false);
            currentGUI.fforward.setEnabled(false);
            currentGUI.fback.setEnabled(false);
            currentGUI.end.setEnabled(false);
            currentGUI.first.setEnabled(false);
        } else {
            currentGUI.hideRedundentNavButtons();
        }
    }

    public boolean getPageTurnAnimating() {
        return pageTurnAnimating;
    }

}
