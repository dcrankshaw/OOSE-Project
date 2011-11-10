package org.jpedal.examples.simpleviewer.gui.swing;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;

import javax.swing.JComponent;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import org.jpedal.Display;
import org.jpedal.PdfDecoder;
import org.jpedal.examples.simpleviewer.Commands;
import org.jpedal.examples.simpleviewer.Values;
import org.jpedal.examples.simpleviewer.gui.SwingGUI;
import org.jpedal.examples.simpleviewer.gui.generic.GUIMouseHandler;
import org.jpedal.examples.simpleviewer.gui.swing.SwingMouseSelection.AutoScrollThread;
import org.jpedal.external.Options;
import org.jpedal.io.PdfObjectReader;

public class SwingMouseListener implements GUIMouseHandler, MouseListener, MouseMotionListener, MouseWheelListener {

	private PdfDecoder decode_pdf;
	private SwingGUI currentGUI;
	private Values commonValues;
	private Commands currentCommands;
	
	SwingMouseSelector selectionFunctions;
	SwingMousePanMode panningFunctions;
	SwingMousePageTurn pageTurnFunctions;

	private long timeOfLastPageChange;
	
	/**current cursor position*/
	private int cx,cy;
	
	/**tells user if we enter a link*/
	private String message="";
	
	private AutoScrollThread scrollThread = new AutoScrollThread();
	
	public SwingMouseListener(PdfDecoder decode_pdf, SwingGUI currentGUI,
			Values commonValues,Commands currentCommands) {

		this.decode_pdf=decode_pdf;
		this.currentGUI=currentGUI;
		this.commonValues=commonValues;
		this.currentCommands=currentCommands;
		
		selectionFunctions = new SwingMouseSelector(decode_pdf, currentGUI, commonValues, currentCommands);
		panningFunctions = new SwingMousePanMode(decode_pdf);
		pageTurnFunctions = new SwingMousePageTurn(decode_pdf, currentGUI, commonValues, currentCommands);

		if (SwingUtilities.isEventDispatchThread()){
            scrollThread.init();
        }else {
            final Runnable doPaintComponent = new Runnable() {
                public void run() {
                    scrollThread.init();
                }
            };
            SwingUtilities.invokeLater(doPaintComponent);
        }
		
		decode_pdf.addExternalHandler(this, Options.SwingMouseHandler);
	}

	public void setupExtractor() {
		System.out.println("Set up extractor called");
		decode_pdf.addMouseMotionListener(this);
		decode_pdf.addMouseListener(this);

	}

	public void setupMouse() {
		/**
		 * track and display screen co-ordinates and support links
		 */
		decode_pdf.addMouseMotionListener(this);
		decode_pdf.addMouseListener(this);
		decode_pdf.addMouseWheelListener(this);

		//set cursor
		currentGUI.setCursor(SwingGUI.DEFAULT_CURSOR);

	}

	public void updateRectangle() {
		switch(decode_pdf.getMouseMode()){

		case PdfDecoder.MOUSE_MODE_TEXT_SELECT : 
			selectionFunctions.updateRectangle();
			break;

		case PdfDecoder.MOUSE_MODE_PANNING : 
			break;

		}
	}

	public void mouseClicked(MouseEvent e) {
		switch(decode_pdf.getMouseMode()){

		case PdfDecoder.MOUSE_MODE_TEXT_SELECT : 
			selectionFunctions.mouseClicked(e);
			break;

		case PdfDecoder.MOUSE_MODE_PANNING : 
			//Does Nothing so ignore
			break;

		}
		
		if (decode_pdf.turnoverOn && decode_pdf.getDisplayView()==Display.FACING){
			pageTurnFunctions.mouseClicked(e);
		}
	}

	public void mouseEntered(MouseEvent e) {
		switch(decode_pdf.getMouseMode()){

		case PdfDecoder.MOUSE_MODE_TEXT_SELECT : 
			//Text selection does nothing here
			//selectionFunctions.mouseEntered(e);
			break;

		case PdfDecoder.MOUSE_MODE_PANNING : 
			//Does Nothing so ignore
			break;

		}
		
		if (decode_pdf.turnoverOn && decode_pdf.getDisplayView()==Display.FACING){
			pageTurnFunctions.mouseEntered(e);
		}
	}

	public void mouseExited(MouseEvent e) {

        //Ensure mouse coords don't display when mouse not over PDF
        int[] flag = new int[]{SwingGUI.CURSOR, 0};
        currentGUI.setMultibox(flag);

		//If mouse leaves viewer, stop scrolling
        scrollThread.setAutoScroll(false, 0, 0, 0);
        
		switch(decode_pdf.getMouseMode()){

		case PdfDecoder.MOUSE_MODE_TEXT_SELECT : 
			selectionFunctions.mouseExited(e);
			break;

		case PdfDecoder.MOUSE_MODE_PANNING : 
			//Does Nothing so ignore
			break;

		}
		
		if (decode_pdf.turnoverOn && decode_pdf.getDisplayView()==Display.FACING){
			pageTurnFunctions.mouseExited(e);
		}
	}

	public void mousePressed(MouseEvent e) {
		switch(decode_pdf.getMouseMode()){

		case PdfDecoder.MOUSE_MODE_TEXT_SELECT : 
			selectionFunctions.mousePressed(e);
			break;

		case PdfDecoder.MOUSE_MODE_PANNING : 
			panningFunctions.mousePressed(e);
			break;

		}
		
		if (decode_pdf.turnoverOn && decode_pdf.getDisplayView()==Display.FACING){
			pageTurnFunctions.mousePressed(e);
		}
	}

	public void mouseReleased(MouseEvent e) {
		switch(decode_pdf.getMouseMode()){

		case PdfDecoder.MOUSE_MODE_TEXT_SELECT :
			selectionFunctions.mouseReleased(e);
			break;

		case PdfDecoder.MOUSE_MODE_PANNING : 
			panningFunctions.mouseReleased(e);
			break;

		}
		
		if (decode_pdf.turnoverOn && decode_pdf.getDisplayView()==Display.FACING){
			pageTurnFunctions.mouseReleased(e);
		}
	}

	public void mouseDragged(MouseEvent e) {
		scrollAndUpdateCoords(e);
		
		switch(decode_pdf.getMouseMode()){

		case PdfDecoder.MOUSE_MODE_TEXT_SELECT : 
			selectionFunctions.mouseDragged(e);
			break;

		case PdfDecoder.MOUSE_MODE_PANNING : 
			panningFunctions.mouseDragged(e);
			break;

		}
		
		if (decode_pdf.turnoverOn && decode_pdf.getDisplayView()==Display.FACING){
			pageTurnFunctions.mouseDragged(e);
		}
	}

	public void mouseMoved(MouseEvent e) {
		
		int page = commonValues.getCurrentPage();
		
		Point p = selectionFunctions.getCoordsOnPage(e.getX(), e.getY(), page);
		int x = (int)p.getX();
		int y = (int)p.getY();
		updateCoords(x, y, e.isShiftDown());

		int[] values = selectionFunctions.updateXY(e.getX(), e.getY());
		x =values[0];
		y =values[1];
		decode_pdf.getObjectUnderneath(x, y);

		/*
		 * Mouse mode specific code
		 */
		switch(decode_pdf.getMouseMode()){

		case PdfDecoder.MOUSE_MODE_TEXT_SELECT :
			selectionFunctions.mouseMoved(e);
			break;

		case PdfDecoder.MOUSE_MODE_PANNING : 
			//Does Nothing so ignore
			break;

		}

		if (decode_pdf.turnoverOn && decode_pdf.getDisplayView()==Display.FACING){
			pageTurnFunctions.mouseMoved(e);
		}
	}
	
	class AutoScrollThread implements Runnable{

        Thread scroll;
        boolean autoScroll = false;
        int x = 0;
        int y = 0;
        int interval = 0;

        public AutoScrollThread(){
            scroll = new Thread(this);
        }

        public void setAutoScroll(boolean autoScroll, int x, int y, int interval){
            this.autoScroll = autoScroll;
            this.x = currentGUI.AdjustForAlignment(x);
            this.y = y;
            this.interval = interval;
        }

        public void init(){
            scroll.start();
        }

        int usedX,usedY;

        public void run() {

            while (Thread.currentThread().equals(scroll)) {

                //New autoscroll code allow for diagonal scrolling from corner of viewer

                //@kieran - you will see if you move the mouse to right or bottom of page, repaint gets repeatedly called
                //we need to add 2 test to ensure only redrawn if on page (you need to covert x and y back to PDF and
                //check fit in width and height - see code in this class
                //if(autoScroll && usedX!=x && usedY!=y && x>0 && y>0){
                if(autoScroll){
                    final Rectangle visible_test=new Rectangle(x-interval,y-interval,interval*2,interval*2);
                    final Rectangle currentScreen=decode_pdf.getVisibleRect();

                    if(!currentScreen.contains(visible_test)){

                        if (SwingUtilities.isEventDispatchThread()){
                            decode_pdf.scrollRectToVisible(visible_test);
                        }else {
                            final Runnable doPaintComponent = new Runnable() {
                                public void run() {
                                    decode_pdf.scrollRectToVisible(visible_test);
                                }
                            };
                            SwingUtilities.invokeLater(doPaintComponent);
                        }

                        //Check values modified by (interval*2) as visible rect changed by interval
                        if(x-(interval*2)<decode_pdf.getVisibleRect().x)
                            x = x-interval;
                        else if((x+(interval*2))>(decode_pdf.getVisibleRect().x+decode_pdf.getVisibleRect().width))
                            x = x+interval;

                        if(y-(interval*2)<decode_pdf.getVisibleRect().y)
                            y = y-interval;
                        else if((y+(interval*2))>(decode_pdf.getVisibleRect().y+decode_pdf.getVisibleRect().height))
                            y = y+interval;

                        //thrashes box if constantly called

                        //System.out.println("redraw on scroll");
                        //decode_pdf.repaint();
                    }

                    usedX=x;
                    usedY=y;

                }

                //Delay to check for mouse leaving scroll edge)
                try {
                    Thread.sleep(250);
                } catch (InterruptedException e) {
                    break;
                }
            }
        }
    }

	public void mouseWheelMoved(MouseWheelEvent event) {

		switch(decode_pdf.getDisplayView()){
		case Display.PAGEFLOW3D : 
			break;

		case Display.FACING : 
			if(decode_pdf.turnoverOn)
				pageTurnFunctions.mouseWheelMoved(event);
			break;
		default :

			if(currentGUI.getProperties().getValue("allowScrollwheelZoom").toLowerCase().equals("true") && event.isControlDown()){
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

				float value = event.getWheelRotation();

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
						final double x = event.getX()/decode_pdf.getBounds().getWidth();
						final double y = event.getY()/decode_pdf.getBounds().getHeight();

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
						event.getUnitsToScroll() > 0 &&
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
						event.getUnitsToScroll() < 0 &&
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
					transform.translate(0, event.getUnitsToScroll() * decode_pdf.getScrollInterval());
					rect = rect.createTransformedArea(transform);
					decode_pdf.scrollRectToVisible(rect.getBounds());
				}
			}

			break;
		}
		//		//Not used in text selection or panning modes
		//		if (decode_pdf.turnoverOn && decode_pdf.getDisplayView()==Display.FACING){
		//			pageTurnFunctions.mouseWheelMoved(event);
		//		}
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

		
		int page = commonValues.getCurrentPage();
		
		Point p = selectionFunctions.getCoordsOnPage(event.getX(), event.getY(), page);
		int x = (int)p.getX();
		int y = (int)p.getY();
		updateCoords(x, y, event.isShiftDown());
    }
	
	/**update current page co-ordinates on screen
	 */
	public void updateCoords(/*MouseEvent event*/int x, int y, boolean isShiftDown){
		
		int rotation=currentGUI.getRotation();

		cx=(int)x;
		cy=(int)y;


		if(decode_pdf.getDisplayView()!=Display.SINGLE_PAGE){
			
			if(SwingMouseSelection.activateMultipageHighlight){
				
				//@kieran remove when facing is working correctly
				if(decode_pdf.getDisplayView()==Display.FACING){
					cx=0;
					cy=0;
				}
			}else{
				cx=0;
				cy=0;
			}
		}


		if((commonValues.isProcessing())|(commonValues.getSelectedFile()==null))
			currentGUI.setCoordText("  X: "+ " Y: " + ' ' + ' '); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		else
			currentGUI.setCoordText("  X: " + cx + " Y: " + cy+ ' ' + ' ' +message); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

	}
	
	 public int[] getCursorLocation() {
	        return new int[]{cx,cy};
	    }
	
	 public void checkLinks(boolean mouseClicked, PdfObjectReader pdfObjectReader){
		 //int[] pos = updateXY(event.getX(), event.getY());
		 pageTurnFunctions.checkLinks(mouseClicked, pdfObjectReader, cx, cy);
	 }

	 public void updateCordsFromFormComponent(MouseEvent e) {
		 JComponent component = (JComponent) e.getSource();

		 int x = component.getX() + e.getX();
		 int y = component.getY() + e.getY();
		 Point p = selectionFunctions.getCoordsOnPage(x, y, commonValues.getCurrentPage());
		 x = (int)p.getX();
		 y = (int)p.getY();
			
		 updateCoords(x, y, e.isShiftDown());
	 }
	 
	 public boolean getPageTurnAnimating() {
		 return pageTurnFunctions.getPageTurnAnimating();
	 }
	 
	 public void setPageTurnAnimating(boolean a) {
		 pageTurnFunctions.setPageTurnAnimating(a);
	 }
}
