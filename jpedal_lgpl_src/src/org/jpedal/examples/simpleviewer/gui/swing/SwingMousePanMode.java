package org.jpedal.examples.simpleviewer.gui.swing;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;

import javax.swing.SwingUtilities;

import org.jpedal.PdfDecoder;
import org.jpedal.examples.simpleviewer.gui.SwingGUI;
import org.jpedal.external.Options;

public class SwingMousePanMode  {

	private Point currentPoint;
	private PdfDecoder decode_pdf;
	private Rectangle currentView;
	
	public SwingMousePanMode(PdfDecoder decode_pdf) {
		this.decode_pdf=decode_pdf;
	}

	public void setupMouse() {
		/**
		 * track and display screen co-ordinates and support links
		 */

        //set cursor
        SwingGUI gui = ((SwingGUI)decode_pdf.getExternalHandler(Options.SwingContainer));
        gui.setCursor(gui.GRAB_CURSOR);
	}
	
	public void mouseClicked(MouseEvent arg0) {
		
	}

	public void mouseEntered(MouseEvent arg0) {

	}
	
	public void mouseExited(MouseEvent arg0) {

	}

	public void mousePressed(MouseEvent arg0) {
        if (arg0.getButton()==MouseEvent.BUTTON1) {
            currentPoint = arg0.getPoint();
            currentView = decode_pdf.getVisibleRect();

            //set cursor
            SwingGUI gui = ((SwingGUI)decode_pdf.getExternalHandler(Options.SwingContainer));
            gui.setCursor(gui.GRABBING_CURSOR);
        }
	}

	public void mouseReleased(MouseEvent arg0) {
        //reset cursor
        SwingGUI gui = ((SwingGUI)decode_pdf.getExternalHandler(Options.SwingContainer));
        gui.setCursor(gui.GRAB_CURSOR);
	}

	public void mouseDragged(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e)) {
            final Point newPoint = e.getPoint();

            int diffX = currentPoint.x-newPoint.x;
            int diffY = currentPoint.y-newPoint.y;


            Rectangle view = currentView;

            view.x +=diffX;

            view.y +=diffY;



            if(!view.contains(decode_pdf.getVisibleRect()))
                decode_pdf.scrollRectToVisible(view);
        }
    }

	public void mouseMoved(MouseEvent e) {
		
	}

}