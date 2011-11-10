package org.jpedal.examples.simpleviewer.gui.swing;

import java.awt.Cursor;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Iterator;
import java.util.Map;

import javax.swing.JFileChooser;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import org.jpedal.Display;
import org.jpedal.PageOffsets;
import org.jpedal.PdfDecoder;
import org.jpedal.examples.simpleviewer.Commands;
import org.jpedal.examples.simpleviewer.Values;
import org.jpedal.examples.simpleviewer.gui.SwingGUI;
import org.jpedal.examples.simpleviewer.gui.swing.SwingMouseHandler.mouse_mover;
import org.jpedal.external.Options;
import org.jpedal.io.ObjectStore;
import org.jpedal.io.PdfObjectReader;
import org.jpedal.objects.raw.FormObject;
import org.jpedal.objects.raw.PdfDictionary;
import org.jpedal.objects.raw.PdfObject;

public class SwingMousePageTurn {

	private PdfDecoder decode_pdf;
	private SwingGUI currentGUI;
	private Values commonValues;
	private Commands currentCommands;

	private mouse_mover mover;
	private long lastPress;

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

	long timeOfLastPageChange;

	boolean altIsDown = false;

	public SwingMousePageTurn(PdfDecoder decode_pdf, SwingGUI currentGUI,
			Values commonValues,Commands currentCommands) {

		this.decode_pdf=decode_pdf;
		this.currentGUI=currentGUI;
		this.commonValues=commonValues;
		this.currentCommands=currentCommands;

		//        SwingMouseSelection sms = new SwingMouseSelection(decode_pdf, commonValues, this);
		//        sms.setupMouse();
		//        decode_pdf.setMouseMode(PdfDecoder.MOUSE_MODE_TEXT_SELECT);
		//
		//		decode_pdf.addExternalHandler(this, Options.SwingMouseHandler);

	}

	public void updateRectangle() {
		// TODO Auto-generated method stub

	}

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
				currentGUI.addUniqueIconToFileAttachment){
			int[] pos = updateXY(event.getX(), event.getY());
			checkLinks(true,decode_pdf.getIO(),pos[0], pos[1]);
		}
	}

	public void mouseEntered(MouseEvent event) {
		// TODO Auto-generated method stub

	}

	public void mouseExited(MouseEvent event) {
		//Do nothing
	}

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

//				MouseMotionListener[] listeners = decode_pdf.getMouseMotionListeners();
//				if (mover==null) {
//					for (int i=0; i< listeners.length; i++) {
//						if (listeners[i] instanceof mouse_mover) {
//							mover = ((mouse_mover)listeners[i]);
//						}
//					}
//				}
//				mover.testFall(corner,event.getPoint(),dragLeft);
				
				testFall(corner,event.getPoint(),dragLeft);
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

	public void mouseDragged(MouseEvent event) {
		if(SwingUtilities.isLeftMouseButton(event)){
			altIsDown = event.isAltDown();
			//dragged = true;
			//			int[] values = updateXY(event);
			//			commonValues.m_x2=values[0];
			//			commonValues.m_y2=values[1];

			//			if(commonValues.isPDF())
			//				generateNewCursorBox();

			if(currentGUI.addUniqueIconToFileAttachment){
				int[] pos = updateXY(event.getX(), event.getY());
				checkLinks(true,decode_pdf.getIO(),pos[0], pos[1]);
			}

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
				!pageTurnAnimating) {
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
					//                    System.out.println("drawing left live "+decode_pdf.drawLeft);
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
				int x = (int)p.getX();
				int y = (int)p.getY();

				float scaling = decode_pdf.getScaling();

				double pageHeight = scaling*rawH;
				double pageWidth = scaling*rawW;
				int yStart = decode_pdf.getInsetH();

				//move so relative to center
				double left = (decode_pdf.getWidth()/2) - (pageWidth/2);

				if (x >= left && x <= left+pageWidth &&
						y >= yStart && y <= yStart + pageHeight)
					//set displayed
					flag[1] = 1;
				else
					//set not displayed
					flag[1] = 0;


			} else {
				//set not displayed
				flag[1] = 0;
			}
			currentGUI.setMultibox(flag);
		}
		//<end-adobe>

		if(currentGUI.addUniqueIconToFileAttachment){
			int[] pos = updateXY(event.getX(), event.getY());
			checkLinks(false,decode_pdf.getIO(),pos[0], pos[1]);
		}


	}

	public void mouseWheelMoved(MouseWheelEvent event) {
		if(decode_pdf.getDisplayView() == Display.PAGEFLOW3D)
			return;

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
	}

	/**
	 * checks the link areas on the page and allow user to save file
	 **/
	public void checkLinks(boolean mouseClicked, PdfObjectReader pdfObjectReader, int x, int y){


		//get 'hotspots' for the page
		Map objs=currentGUI.getHotspots();

		//look for a match
		if(objs!=null){

			//new code to check for match
			Iterator objKeys=objs.keySet().iterator();
			FormObject annotObj=null;
			while(objKeys.hasNext()){
				annotObj=(FormObject) objKeys.next();
				if(annotObj.getBoundingRectangle().contains(x,y)){
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

	/**
	 * get raw co-ords and convert to correct scaled units
	 * @return int[] of size 2, [0]=new x value, [1] = new y value
	 */
	protected int[] updateXY(int originalX, int originalY) {

		float scaling=currentGUI.getScaling();
		int inset=currentGUI.getPDFDisplayInset();
		int rotation=currentGUI.getRotation();

		//get co-ordinates of top point of outine rectangle
		int x=(int)(((currentGUI.AdjustForAlignment(originalX))-inset)/scaling);
		int y=(int)((originalY-inset)/scaling);

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
