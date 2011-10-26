package org.jpedal.examples.simpleviewer.gui.swing;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.Date;

import org.jpedal.Display;
import org.jpedal.PdfDecoder;
import org.jpedal.examples.simpleviewer.Commands;
import org.jpedal.examples.simpleviewer.SimpleViewer;
import org.jpedal.examples.simpleviewer.Values;
import org.jpedal.exception.PdfException;
import org.jpedal.external.Options;
import org.jpedal.examples.simpleviewer.gui.SwingGUI;
import org.jpedal.grouping.SearchType;
import org.jpedal.io.Speech;
import org.jpedal.render.DynamicVectorRenderer;
import org.jpedal.utils.Messages;
import org.jpedal.utils.Strip;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;

public final class SwingMouseSelection implements MouseListener, MouseMotionListener {

    private PdfDecoder decode_pdf;
    private int clickCount = 0;
    private long lastTime = -1;
    public final static boolean activateMultipageHighlight = true;
    private SwingGUI currentGUI;
    private Values commonValues;
    public Rectangle area = null;
    public int id = -1;
    public int lastId =-1;
    private JPopupMenu rightClick = new JPopupMenu();
    private boolean menuCreated = false;

    private AutoScrollThread scrollThread = new AutoScrollThread();

    private SwingMouseHandler smh;

    //Right click options
    JMenuItem copy;
    //======================================
    JMenuItem selectAll, deselectall;
    //======================================
    JMenu extract;
    JMenuItem extractText, extractImage;
    ImageIcon snapshotIcon;
    JMenuItem snapShot;
    //======================================
    JMenuItem find;
    //======================================
    JMenuItem speakHighlighted;
    JMenuItem speakEntirePage;


    public SwingMouseSelection(PdfDecoder decode_pdf, Values values, SwingMouseHandler mainHandler) {
        this.decode_pdf=decode_pdf;
        this.smh = mainHandler;
        currentGUI = ((SwingGUI)decode_pdf.getExternalHandler(Options.SwingContainer));
        commonValues = values;
//        currentCommands = currentGUI.currentCommands;

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

    }

    public void setupMouse() {
        /**
         * track and display screen co-ordinates and support links
         */
        decode_pdf.addMouseMotionListener(this);
        decode_pdf.addMouseListener(this);

        //set cursor
        currentGUI.setCursor(SwingGUI.DEFAULT_CURSOR);
    }

    public void mouseClicked(MouseEvent event) {

        if(decode_pdf.getDisplayView()==Display.SINGLE_PAGE || activateMultipageHighlight){
            long currentTime = new Date().getTime();

            if(lastTime+500 < currentTime)
                clickCount=0;

            lastTime = currentTime;

            if(event.getButton()==MouseEvent.BUTTON1){
                //Single mode actions
                if(clickCount!=4)
                    clickCount++;

                //highlight image on page if over
                int[] c = smh.getCursorLocation();
                id = decode_pdf.getDynamicRenderer().isInsideImage(c[0],c[1]);

                if(lastId!=id && id!=-1){
                    area = decode_pdf.getDynamicRenderer().getArea(id);


                    if(area!=null){
                        int h= area.height;
                        int w= area.width;

                        int x= area.x;
                        int y= area.y;
                        decode_pdf.getDynamicRenderer().setneedsHorizontalInvert(false);
                        decode_pdf.getDynamicRenderer().setneedsVerticalInvert(false);
                        //						Check for negative values
                        if(w<0){
                            decode_pdf.getDynamicRenderer().setneedsHorizontalInvert(true);
                            w =-w;
                            x =x-w;
                        }
                        if(h<0){
                            decode_pdf.getDynamicRenderer().setneedsVerticalInvert(true);
                            h =-h;
                            y =y-h;
                        }

                        if(decode_pdf.isImageExtractionAllowed()){
                            decode_pdf.setHighlightedImage(new int[]{x,y,w,h});
                        }

                    }
                    lastId = id;
                }else{
                    if(decode_pdf.isImageExtractionAllowed()){
                        decode_pdf.setHighlightedImage(null);
                    }
                    lastId = -1;
                }

                if(id==-1){
                    if(clickCount>1){
                        switch(clickCount){
                            case 1 : //single click adds caret to page
                                /**
                                 * Does nothing yet. IF above prevents this case from ever happening
                                 * Add Caret code here and add shift click code for selection.
                                 * Also remember to comment out "if(clickCount>1)" from around this switch to activate
                                 */
                                break;
                            case 2 : //double click selects line
                                Rectangle[] lines = decode_pdf.getLineAreas(commonValues.getCurrentPage());
                                Rectangle point = new Rectangle(c[0],c[1],1,1);

                                if(lines!=null) { //Null is page has no lines
                                    for(int i=0; i!=lines.length; i++){
                                        if(lines[i].intersects(point)){
                                            currentGUI.setRectangle(lines[i]);
                                            decode_pdf.updateCursorBoxOnScreen(lines[i],PdfDecoder.highlightColor);
                                            decode_pdf.addHighlights(new Rectangle[]{lines[i]}, false, commonValues.getCurrentPage());
                                            //decode_pdf.setMouseHighlightArea(lines[i]);
                                        }
                                    }
                                }
                                break;
                            case 3 : //triple click selects paragraph
                                Rectangle para = decode_pdf.setFoundParagraph(c[0], c[1], commonValues.getCurrentPage());
                                if(para!=null){
                                    currentGUI.setRectangle(para);
                                    decode_pdf.updateCursorBoxOnScreen(para,PdfDecoder.highlightColor);
                                    //decode_pdf.repaint();
                                    //decode_pdf.setMouseHighlightArea(para);
                                }
                                break;
                            case 4 : //quad click selects page
                                currentGUI.currentCommands.executeCommand(Commands.SELECTALL, null);
                                break;
                        }
                    }
                }
            }else if(event.getButton()==MouseEvent.BUTTON2){

            }else if(event.getButton()==MouseEvent.BUTTON3){

            }
        }
    }

    public void mouseEntered(MouseEvent event) {

    }

    public void mouseExited(MouseEvent arg0) {
        //If mouse leaves viewer, stop scrolling
        scrollThread.setAutoScroll(false, 0, 0, 0);
    }

    public void mousePressed(MouseEvent event) {

        if(decode_pdf.getDisplayView()== Display.SINGLE_PAGE || activateMultipageHighlight){
            if(event.getButton()==MouseEvent.BUTTON1){
                /** remove any outline and reset variables used to track change */

                currentGUI.setRectangle(null);
                decode_pdf.updateCursorBoxOnScreen(null, null); //remove box
                decode_pdf.setHighlightedImage(null);// remove image highlight
                decode_pdf.clearHighlights();

                //Remove focus from form is if anywhere on pdf panel is clicked / mouse dragged
                decode_pdf.grabFocus();

                float scaling=currentGUI.getScaling();
                int inset=currentGUI.getPDFDisplayInset();
                int rotation=currentGUI.getRotation();

                //get co-ordinates of top point of outine rectangle
                int x=(int)(((currentGUI.AdjustForAlignment(event.getX()))-inset)/scaling);
                int y=(int)((event.getY()-inset)/scaling);

                //undo any viewport scaling (no crop assumed
                if(commonValues.maxViewY!=0){ // will not be zero if viewport in play
                    x=(int)(((x-(commonValues.dx*scaling))/commonValues.viewportScale));
                    y=(int)((currentGUI.mediaH-((currentGUI.mediaH-(y/scaling)-commonValues.dy)/commonValues.viewportScale))*scaling);
                }

                if (rotation == 90) {
                    commonValues.m_y1 = x+currentGUI.cropY;
                    commonValues.m_x1 = y+currentGUI.cropX;
                } else if ((rotation == 180)) {
                    commonValues.m_x1 = currentGUI.mediaW - (x+currentGUI.mediaW-currentGUI.cropW-currentGUI.cropX);
                    commonValues.m_y1 = y+currentGUI.cropY;
                } else if ((rotation == 270)) {
                    commonValues.m_y1 = currentGUI.mediaH - (x+currentGUI.mediaH-currentGUI.cropH-currentGUI.cropY);
                    commonValues.m_x1 = currentGUI.mediaW - (y+currentGUI.mediaW-currentGUI.cropW-currentGUI.cropX);
                } else {
                    commonValues.m_x1 = x+currentGUI.cropX;
                    commonValues.m_y1 = currentGUI.mediaH - (y+currentGUI.mediaH-currentGUI.cropH-currentGUI.cropY);
                }

            }
        }
    }

    public void mouseReleased(MouseEvent event) {

        if(decode_pdf.getDisplayView()==Display.SINGLE_PAGE || activateMultipageHighlight){
            if(event.getButton()==MouseEvent.BUTTON1){
//					old_m_x2 = -1;
//					old_m_y2 = -1;

                decode_pdf.repaintArea(new Rectangle(commonValues.m_x1-currentGUI.cropX, commonValues.m_y2+currentGUI.cropY, commonValues.m_x2 - commonValues.m_x1+currentGUI.cropX,
                        (commonValues.m_y1 - commonValues.m_y2)+currentGUI.cropY), currentGUI.mediaH);//redraw
                decode_pdf.repaint();

                //dragged = false;

                if(decode_pdf.isExtractingAsImage()){

                    /** remove any outline and reset variables used to track change */
                    currentGUI.setRectangle(null);
                    decode_pdf.updateCursorBoxOnScreen(null, null); //remove box
                    decode_pdf.clearHighlights(); //remove highlighted text
                    decode_pdf.setHighlightedImage(null);// remove image highlight

                    decode_pdf.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

                    currentGUI.currentCommands.extractSelectedScreenAsImage();
                    decode_pdf.setExtractingAsImage(false);

                }
            } else if(event.getButton()==MouseEvent.BUTTON3){
                if(currentGUI.getProperties().getValue("allowRightClick").toLowerCase().equals("true")){
                    if (!menuCreated)
                        createRightClickMenu();

                    if(decode_pdf.getHighlightImage()==null)
                        extractImage.setEnabled(false);
                    else
                        extractImage.setEnabled(true);

                    if(decode_pdf.getHighlightedAreas(commonValues.getCurrentPage())==null){
                        extractText.setEnabled(false);
                        find.setEnabled(false);
                        speakHighlighted.setEnabled(false);
                        copy.setEnabled(false);
                    }else{
                        extractText.setEnabled(true);
                        find.setEnabled(true);
                        speakHighlighted.setEnabled(true);
                        copy.setEnabled(true);
                    }

                    //<start-wrap>
                    if(decode_pdf!=null && decode_pdf.isOpen())
                        rightClick.show(decode_pdf, event.getX(), event.getY());
                    //<end-wrap>
                }
            }
        }
    }

    public void mouseDragged(MouseEvent event) {
        int[] values = smh.updateXY(event);
        commonValues.m_x2=values[0];
        commonValues.m_y2=values[1];
    }



    public void mouseMoved(MouseEvent event) {
        //Update cursor for this position
        int[] values = smh.updateXY(event);
        int x =values[0];
        int y =values[1];
        decode_pdf.getObjectUnderneath(x, y);
    }


    private void createRightClickMenu(){

        copy = new JMenuItem(Messages.getMessage("PdfRightClick.copy"));
        selectAll = new JMenuItem(Messages.getMessage("PdfRightClick.selectAll"));
        deselectall = new JMenuItem(Messages.getMessage("PdfRightClick.deselectAll"));
        extract = new JMenu(Messages.getMessage("PdfRightClick.extract"));
        extractText = new JMenuItem(Messages.getMessage("PdfRightClick.extractText"));
        extractImage = new JMenuItem(Messages.getMessage("PdfRightClick.extractImage"));
        snapshotIcon = new ImageIcon(getClass().getResource("/org/jpedal/examples/simpleviewer/res/snapshot_menu.gif"));
        snapShot = new JMenuItem(Messages.getMessage("PdfRightClick.snapshot"), snapshotIcon);
        find = new JMenuItem(Messages.getMessage("PdfRightClick.find"));
        speakHighlighted = new JMenuItem(Messages.getMessage("PdfTextToSpeech.SpeakSelection"));
        speakEntirePage= new JMenuItem(Messages.getMessage("PdfTextToSpeech.SpeakAll"));

        rightClick.add(copy);
        copy.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                currentGUI.currentCommands.executeCommand(Commands.COPY, null);
            }
        });

        rightClick.addSeparator();


        rightClick.add(selectAll);
        selectAll.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                currentGUI.currentCommands.executeCommand(Commands.SELECTALL, null);
            }
        });

        rightClick.add(deselectall);
        deselectall.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                currentGUI.currentCommands.executeCommand(Commands.DESELECTALL, null);
            }
        });

        rightClick.addSeparator();

        rightClick.add(extract);

        extract.add(extractText);
        extractText.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                if(decode_pdf.getDisplayView()==1)
                    currentGUI.currentCommands.extractSelectedText();
                else{
                    if(SimpleViewer.showMessages)
                        JOptionPane.showMessageDialog(currentGUI.getFrame(),"Text Extraction is only avalible in single page display mode");
                }
            }
        });

        extract.add(extractImage);
        extractImage.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                if(decode_pdf.getHighlightImage()==null){
                    if(SimpleViewer.showMessages)
                        JOptionPane.showMessageDialog(decode_pdf, "No image has been selected for extraction.", "No image selected", JOptionPane.ERROR_MESSAGE);
                }else{
                    if(decode_pdf.getDisplayView()==1){
                        JFileChooser jf = new JFileChooser();
                        FileFilter ff1 = new FileFilter(){
                            public boolean accept(File f){
                                return f.isDirectory() || f.getName().toLowerCase().endsWith(".jpg") || f.getName().toLowerCase().endsWith(".jpeg");
                            }
                            public String getDescription(){
                                return "JPG (*.jpg)" ;
                            }
                        };
                        FileFilter ff2 = new FileFilter(){
                            public boolean accept(File f){
                                return f.isDirectory() || f.getName().toLowerCase().endsWith(".png");
                            }
                            public String getDescription(){
                                return "PNG (*.png)" ;
                            }
                        };
                        FileFilter ff3 = new FileFilter(){
                            public boolean accept(File f){
                                return f.isDirectory() || f.getName().toLowerCase().endsWith(".tif") || f.getName().toLowerCase().endsWith(".tiff");
                            }
                            public String getDescription(){
                                return "TIF (*.tiff)" ;
                            }
                        };
                        jf.addChoosableFileFilter(ff3);
                        jf.addChoosableFileFilter(ff2);
                        jf.addChoosableFileFilter(ff1);
                        jf.showSaveDialog(null);

                        File f = jf.getSelectedFile();
                        boolean failed = false;
                        if(f!=null){
                            String filename = f.getAbsolutePath();
                            String type = jf.getFileFilter().getDescription().substring(0,3).toLowerCase();

                            //Check to see if user has entered extension if so ignore filter
                            if(filename.indexOf('.')!=-1){
                                String testExt = filename.substring(filename.indexOf('.')+1).toLowerCase();
                                if(testExt.equals("jpg") || testExt.equals("jpeg"))
                                    type = "jpg";
                                else
                                if(testExt.equals("png"))
                                    type = "png";
                                else //*.tiff files using JAI require *.TIFF
                                    if(testExt.equals("tif") || testExt.equals("tiff"))
                                        type = "tiff";
                                    else{
                                        //Unsupported file format
                                        if(SimpleViewer.showMessages)
                                            JOptionPane.showMessageDialog(null, "Sorry, we can not currently save images to ."+testExt+" files.");
                                        failed = true;
                                    }
                            }

                            //JAI requires *.tiff instead of *.tif
                            if(type.equals("tif"))
                                type = "tiff";

                            //Image saved in All files filter, default to .png
                            if(type.equals("all"))
                                type = "png";

                            //If no extension at end of name, added one
                            if(!filename.toLowerCase().endsWith('.' +type))
                                filename = filename+ '.' +(type);

                            //If valid extension was choosen
                            if(!failed)
                                decode_pdf.getDynamicRenderer().saveImage(id, filename, type);
                        }
                    }
                }
            }
        });

        extract.add(snapShot);
        snapShot.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                currentGUI.currentCommands.executeCommand(Commands.SNAPSHOT, null);
            }
        });

        rightClick.addSeparator();

        rightClick.add(find);
        find.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {

                /**ensure co-ords in right order*/
                Rectangle coords= decode_pdf.getCursorBoxOnScreen();
                if(coords==null){
                    if(SimpleViewer.showMessages)
                        JOptionPane.showMessageDialog(decode_pdf, "There is no text selected.\nPlease highlight the text you wish to search.", "No Text selected", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                String textToFind=currentGUI.showInputDialog(Messages.getMessage("PdfViewerMessage.GetUserInput"));

                //if cancel return to menu.
                if(textToFind==null || textToFind.length()<1){
                    return;
                }


                int t_x1=coords.x;
                int t_x2=coords.x+coords.width;
                int t_y1=coords.y;
                int t_y2=coords.y+coords.height;

                if(t_y1<t_y2){
                    int temp = t_y2;
                    t_y2=t_y1;
                    t_y1=temp;
                }

                if(t_x1>t_x2){
                    int temp = t_x2;
                    t_x2=t_x1;
                    t_x1=temp;
                }

                if(t_x1<currentGUI.cropX)
                    t_x1 = currentGUI.cropX;
                if(t_x1>currentGUI.mediaW-currentGUI.cropX)
                    t_x1 = currentGUI.mediaW-currentGUI.cropX;

                if(t_x2<currentGUI.cropX)
                    t_x2 = currentGUI.cropX;
                if(t_x2>currentGUI.mediaW-currentGUI.cropX)
                    t_x2 = currentGUI.mediaW-currentGUI.cropX;

                if(t_y1<currentGUI.cropY)
                    t_y1 = currentGUI.cropY;
                if(t_y1>currentGUI.mediaH-currentGUI.cropY)
                    t_y1 = currentGUI.mediaH-currentGUI.cropY;

                if(t_y2<currentGUI.cropY)
                    t_y2 = currentGUI.cropY;
                if(t_y2>currentGUI.mediaH-currentGUI.cropY)
                    t_y2 = currentGUI.mediaH-currentGUI.cropY;

                //<start-demo>
                /**<end-demo>
                 if(SimpleViewer.showMessages)
                 JOptionPane.showMessageDialog(currentGUI.getFrame(),Messages.getMessage("PdfViewerMessage.FindDemo"));
                 textToFind=null;
                 /**/

                int searchType = SearchType.DEFAULT;

                int caseSensitiveOption=currentGUI.showConfirmDialog(Messages.getMessage("PdfViewercase.message"),
                        null,	JOptionPane.YES_NO_OPTION);

                if(caseSensitiveOption==JOptionPane.YES_OPTION)
                    searchType |= SearchType.CASE_SENSITIVE;

                int findAllOption=currentGUI.showConfirmDialog(Messages.getMessage("PdfViewerfindAll.message"),
                        null,	JOptionPane.YES_NO_OPTION);

                if(findAllOption==JOptionPane.NO_OPTION)
                    searchType |= SearchType.FIND_FIRST_OCCURANCE_ONLY;

                int hyphenOption=currentGUI.showConfirmDialog(Messages.getMessage("PdfViewerfindHyphen.message"),
                        null,	JOptionPane.YES_NO_OPTION);

                if(hyphenOption==JOptionPane.YES_OPTION)
                    searchType |= SearchType.MUTLI_LINE_RESULTS;

                if(textToFind!=null){
                    try {
                        float[] co_ords;

//                        if((searchType & SearchType.MUTLI_LINE_RESULTS)==SearchType.MUTLI_LINE_RESULTS)
//                        	co_ords = decode_pdf.getGroupingObject().findTextInRectangleAcrossLines(t_x1,t_y1,t_x2,t_y2,commonValues.getCurrentPage(),textToFind,searchType);
//                        else
//                        	co_ords = decode_pdf.getGroupingObject().findTextInRectangle(t_x1,t_y1,t_x2,t_y2,commonValues.getCurrentPage(),textToFind,searchType);

                        co_ords = decode_pdf.getGroupingObject().findText(new Rectangle(t_x1,t_y1,t_x2-t_x1,t_y2-t_y1),commonValues.getCurrentPage(),new String[]{textToFind},searchType);

                        if(co_ords!=null){
                            if(co_ords.length<3)
                                currentGUI.showMessageDialog(Messages.getMessage("PdfViewerMessage.Found")+ ' ' +co_ords[0]+ ',' +co_ords[1]);
                            else{
                                StringBuffer displayCoords = new StringBuffer();
                                String coordsMessage = Messages.getMessage("PdfViewerMessage.FoundAt");
                                for(int i=0;i<co_ords.length;i=i+5){
                                    displayCoords.append(coordsMessage).append(' ');
                                    displayCoords.append(co_ords[i]);
                                    displayCoords.append(',');
                                    displayCoords.append(co_ords[i+1]);

//										//Other two coords of text
//										displayCoords.append(',');
//										displayCoords.append(co_ords[i+2]);
//										displayCoords.append(',');
//										displayCoords.append(co_ords[i+3]);

                                    displayCoords.append('\n');
                                    if(co_ords[i+4]==-101){
                                        coordsMessage = Messages.getMessage("PdfViewerMessage.FoundAtHyphen");
                                    }else{
                                        coordsMessage = Messages.getMessage("PdfViewerMessage.FoundAt");
                                    }

                                }
                                currentGUI.showMessageDialog(displayCoords.toString());
                            }
                        }else
                            currentGUI.showMessageDialog(Messages.getMessage("PdfViewerMessage.NotFound"));

                    } catch (PdfException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }

                }
            }

        });

       
        
        menuCreated = true;
        decode_pdf.add(rightClick);
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


}
