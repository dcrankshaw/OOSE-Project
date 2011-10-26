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
  * PdfDecoder.java
  * ---------------
 */

package org.jpedal;


import org.jpedal.constants.*;
import org.jpedal.fonts.tt.TTGlyph;
import org.jpedal.io.ColorSpaceConvertor;
import org.jpedal.io.LinearizedHintTable;
import org.jpedal.io.ObjectStore;
import org.jpedal.objects.raw.*;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
// <start-me>
import java.awt.print.PageFormat;
import java.awt.print.Pageable;
import java.awt.print.Paper;
import java.awt.print.Printable;
import java.awt.print.PrinterException;

import javax.crypto.*;
import javax.crypto.spec.*;
import javax.print.attribute.SetOfIntegerSyntax;

import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import org.w3c.dom.Document;
// <end-me>
import java.io.*;
import java.net.JarURLConnection;
import java.net.URL;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.util.*;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.swing.*;
import javax.swing.Timer;


import org.jpedal.objects.acroforms.creation.FormFactory;
import org.jpedal.objects.acroforms.rendering.AcroRenderer;
import org.jpedal.objects.acroforms.rendering.DefaultAcroRenderer;
//<start-adobe>
import org.jpedal.grouping.PdfGroupingAlgorithms;

//<start-thin><start-me>
import org.jpedal.examples.simpleviewer.Commands;
import org.jpedal.examples.simpleviewer.gui.SwingGUI;
import org.jpedal.examples.simpleviewer.gui.swing.SwingMouseListener;
//<end-thin><end-me>
//<end-adobe>


import org.jpedal.PdfPanel.ViewStack.Viewable;
import org.jpedal.color.ColorSpaces;

import org.jpedal.exception.PdfException;
import org.jpedal.exception.PdfFontException;

import org.jpedal.io.*;
import org.jpedal.objects.*;
import org.jpedal.objects.layers.PdfLayerList;

//<start-adobe><start-me>
import org.jpedal.objects.outlines.OutlineData;
import org.jpedal.objects.structuredtext.MarkedContentGenerator;
//<end-adobe><end-me>

import org.jpedal.parser.*;
import org.jpedal.utils.*;
import org.jpedal.utils.repositories.Vector_Int;
import org.jpedal.utils.repositories.Vector_Rectangle;
import org.jpedal.external.*;

import org.jpedal.render.*;
import org.jpedal.fonts.FontMappings;
import org.jpedal.fonts.StandardFonts;
import org.jpedal.fonts.objects.FontData;

/**
 * Provides an object to decode pdf files and provide a rasterizer if required -
 * Normal usage is to create instance of PdfDecoder and access via public
 * methods. Examples showing usage in org.jpedal.examples - Inherits indirectly
 * from JPanel so can be used as a standard Swing component -
 * <p/>
 * Extends other classes to separate out GUI and business logic but should be
 * regarded as ONE object and PdfPanel should not be instanced - We recommend
 * you access JPedal using only public methods listed in API
 */
public class PdfDecoder extends PdfPanel
{

    //flag to debug linearization
    static final boolean debugLinearization=false;

    //dev flag
    private static boolean java3DAvailable = true;
    public static boolean java3DTested = false;

    private static boolean fontsInitialised=false;

    private static void initFonts(){

        fontsInitialised=true;

        // pick up D options and use settings

        try {
            String fontMaps = System.getProperty("org.jpedal.fontmaps");

            if (fontMaps != null) {
                StringTokenizer fontPaths = new StringTokenizer(fontMaps, ",");

                while (fontPaths.hasMoreTokens()) {

                    String fontPath = fontPaths.nextToken();
                    StringTokenizer values = new StringTokenizer(fontPath, "=:");

                    int count = values.countTokens() - 1;
                    String nameInPDF[] = new String[count];
                    String key = values.nextToken();
                    for (int i = 0; i < count; i++)
                        nameInPDF[i] = values.nextToken();

                    setSubstitutedFontAliases(key, nameInPDF); //$NON-NLS-1$

                }
            }

        } catch (Exception e) {
        	if(LogWriter.isOutput())
        		LogWriter.writeLog("Unable to read org.jpedal.fontmaps " + e.getMessage());
        }

        // pick up D options and use settings

        try {
            String fontDirs = System.getProperty("org.jpedal.fontdirs");
            String failed = null;
            if (fontDirs != null)
                failed = addFonts(fontDirs, failed);
            if (failed != null && LogWriter.isOutput())
                LogWriter.writeLog("Could not find " + failed);
        } catch (Exception e) {
        	if(LogWriter.isOutput())
        		LogWriter.writeLog("Unable to read FontDirs " + e.getMessage());
        }
    }


    public static boolean usePageFlow3D = true;

    private int E=-1;

    private static final long serialVersionUID = 1107156907326450528L;


    public static final String version = "4.53b40-OS";
     /**/


    public static boolean debugHiRes=false;

    public static final boolean optimiseType3Rendering = false;


    private PageObject linObject=null;
    private Map linObjects=new Hashtable();

    boolean converToRGB =false;

    int mouseMode = 0;

    /**flag if we have tested - reset for every file*/
    boolean isLinearizationTested =false;

    private Certificate certificate;

    private PrivateKey key;

    /**present if file Linearized*/
    PdfObject linearObj=null;

    /**only used to count pages*/
    private int tempPageCount;

    //allow user to override code
    public static JPedalHelper Helper=null;//new org.jpedal.examples.ExampleHelper();

    public int getMouseMode() {
        return mouseMode;
    }

    public void setMouseMode(int mouseMode) {
        this.mouseMode = mouseMode;
    }

    public final static int MOUSE_MODE_TEXT_SELECT = 0;
    public final static int MOUSE_MODE_PANNING = 1;

    private int logicalPageOffset=0;
    /** @ kieran backed out as requested
     private boolean isLineAreasCreated = false;

     public boolean isLineAreasCreated() {
     return isLineAreasCreated;
     }

     public void setLineAreasCreated(boolean isHighlightsGenerated) {
     this.isLineAreasCreated = isHighlightsGenerated;
     }/**/

    //**listener for side-scroll scrollbar*/
    ScrollListener scrollListener;

    /**
     * allow printing of different sizes pages
     * (default is false as PrintJob does not support different page sizes whereas
     * DocPrintJob does)
     * @param allowDifferentPrintPageSizes
     */
    public void setAllowDifferentPrintPageSizes(boolean allowDifferentPrintPageSizes) {
        this.allowDifferentPrintPageSizes = allowDifferentPrintPageSizes;
    }

    private boolean allowDifferentPrintPageSizes=false;


    /**page we have outlines for*/
    private int OCpageNumber=-1;

    // flag to allow quick changing between old and new print turning alg.
    boolean oldSetup = false;

    // on/off switch for transparent images over text fix.
    boolean useARGBFix = true;

    private DPIFactory scalingdpi=new DPIFactory();

    // added for privt visbile
    boolean docIsLandscaped = false;

    // print only the visible area of the doc.
    boolean printOnlyVisible = false;

    //debug print visible

    private int duplexGapEven = 0;
    private int duplexGapOdd = 0;

    private boolean isPDf = false;
    private boolean isMultiPageTiff = false;

    private final Map overlayType = new HashMap();
    private final Map overlayColors = new HashMap();
    private final Map overlayObj = new HashMap();

    private final Map overlayTypeG = new HashMap();
    private final Map overlayColorsG = new HashMap();
    private final Map overlayObjG = new HashMap();

    //data from external FDF file


    private Javascript javascript = null;

    ImageHandler customImageHandler = null;

    ColorHandler customColorHandler=null;//new ExampleColorHandler();


    CustomMessageHandler customMessageHandler =null;

    CustomPrintHintingHandler customPrintHintingHandler=null;

    DynamicVectorRenderer customDVR=null;

    //copy for callback
    Object swingGUI=null;

    /**
     * provide access to pdf file objects
     */
    PdfObjectReader currentPdfFile;

    //used to debug font substitution
    private static boolean debugFonts=false;

    //stop scaling to silly figures
    static private Integer bestQualityMaxScaling=null;
    //non-static version
    private Integer instance_bestQualityMaxScaling=null;

    //force to generate images smaller than page size
    static private Boolean allowPagesSmallerThanPageSize=Boolean.FALSE;

    private int[] instance_formsNoPrint=null;

    private static int[] formsNoPrint=null;

    //page size for extraction
    static private String[] extactionPageSize=null;
    //non-static version
    private String[] instance_extactionPageSize=null;

//    //page size override
//    static private String overridePageSize="false";
//    //non-static version
//    private String instance_overridePageSize="false";

    //page size override
    static private Boolean overridePageSize=null;
    //non-static version
    private Boolean instance_overridePageSize=null;

    //non-static version
    private Boolean instance_allowPagesSmallerThanPageSize=Boolean.FALSE;

    //values on last decodePage
    private Iterator colorSpacesUsed;

    /**
     * given a ref, what is the page
     * @param ref - PDF object reference
     * @return - page number with  being first page
     */
    public int getPageFromObjectRef(String ref) {

        return pageLookup.convertObjectToPageNumber(ref);
    }

    /**
     * page lookup table using objects as key
     */
    private PageLookup pageLookup = new PageLookup();

    /**
     * flag to stop multiple access to background decoding
     */
    private boolean isBackgroundDecoding = false;

    //<start-adobe><start-me>
    /**
     * store outline data extracted from pdf
     */
    private OutlineData outlineData = null;

    //<end-adobe><end-me>

    /**objects read from root*/
    PdfObject metadataObj=null, nameObj=null,structTreeRootObj=null, markInfoObj=null,
            acroFormObj=null,OutlinesObj=null, PropertiesObj=null, OCProperties=null;

    //<start-adobe><start-me>
    /**
     * marked content
     */
    private final MarkedContentGenerator content = new MarkedContentGenerator();
    //<end-adobe><end-me>

    /**
     * store image data extracted from pdf
     */
    private PdfImageData pdfImages = new PdfImageData();

    /**
     * store image data extracted from pdf
     */
    private PdfImageData pdfBackgroundImages = new PdfImageData();

    /**
     * store text data and can be passed out to other classes
     */
    private PdfData pdfData;

    /**
     * store text data and can be passed out to other classes
     */
    private PdfData pdfBackgroundData;

    /**
     * flag to show if on mac so we can code around certain bugs
     */
    public static boolean isRunningOnMac = false;
    public static boolean isRunningOnWindows = false;
    public static boolean isRunningOnAIX = false;
    public static boolean isRunningOnLinux = false;

    /**
     * version number
     */
    public static float javaVersion=0f;

    public static boolean clipOnMac=false;

    /**
     * provide print debug feature - used for internal development only
     */
    public static boolean debugPrint = false;


    private boolean hasViewListener = false;

    //<start-adobe>
    private RefreshLayout viewListener = new RefreshLayout();
    //<end-adobe>

    private boolean oddPagesOnly = false, evenPagesOnly = false;

    private boolean pagesPrintedInReverse = false;
    private boolean stopPrinting = false;

    /**
     * PDF version
     */
    private String pdfVersion = "";

    /**
     * Used to calculate displacement
     */
    private int lastWidth;

    private int lastPage;

    public static boolean isDraft = true;

    /**
     * direct graphics 2d to render onto
     */
    private Graphics2D g2 = null;

    
    // <start-std><start-pro>
    /**
    // <end-pro><end-std>

    public static boolean isTesting = false;
    /**/

    /**
     * flag to show embedded fonts present
     */
    private boolean hasEmbeddedFonts = false;

    /**
     * hold all data in Linearized Obj
     */
    LinearizedHintTable linHintTable=null;

    /**
     * list of fonts for decoded page
     */
    private String fontsInFile = "";

    /**
     * list of images for decoded page
     */
    private String imagesInFile= "";

    /**
     * dpi for final images
     */
    public static int dpi = 72;

    /**
     * flag to tell software to embed x point after each character so we can
     * merge any overlapping text together
     */
    public static boolean embedWidthData = false;

    /**
     * flag to show outline
     */
    private boolean hasOutline = false;

    /**
     * actual page range to print
     */
    private int start = 0, end = -1;

    /**
     * id demo flag disables output in demo
     */
    public static final boolean inDemo = false;

    /**custom upscale val for JPedal settings*/
    private float multiplyer = 1;

    /**custom hi-res val for JPedal settings*/
    public static boolean hires = false;

    /**global value for IMAGE_UPSCALE param*/
    //private static int GLOBAL_IMAGE_UPSCALE = 1;

    /**
     * printing object
     */
    private PdfStreamDecoderForPrinting currentPrintDecoder = null;
    public  boolean	legacyPrintMode = true;

    /**
     * used by Canoo for printing
     */
    private DynamicVectorRenderer printRender = null;

    /**
     * last page printed
     */
    private int lastPrintedPage = -1;

    /**
     * flag to show extraction mode includes any text
     */
    public static final int TEXT = 1;

    /**
     * flag to show extraction mode includes original images
     */
    public static final int RAWIMAGES = 2;

    /**
     * flag to show extraction mode includes final scaled/clipped
     */
    public static final int FINALIMAGES = 4;

    /**
     * undocumented flag to allow shape extraction
     */
    protected static final int PAGEDATA = 8;

    /**
     * flag to show extraction mode includes final scaled/clipped
     */
    public static final int RAWCOMMANDS = 16;

    /**
     * flag to show extraction of clipped images at highest res
     */
    public static final int CLIPPEDIMAGES = 32;

    /**
     * flag to show extraction of clipped images at highest res
     */
    public static final int TEXTCOLOR = 64;

    /**
     * flag to show extraction of raw cmyk images
     */
    public static final int CMYKIMAGES = 128;

    /**
     * flag to show extraction of xforms metadata
     */
    public static final int XFORMMETADATA = 256;

    /**
     * flag to show extraction of color required (used in Storypad grouping)
     */
    public static final int COLOR = 512;

    /**
     * flag to show render mode includes any text
     */
    public static final int RENDERTEXT = 1;

    /**
     * flag to show render mode includes any images
     */
    public static final int RENDERIMAGES = 2;

    /**
     * flag to show render mode includes any images
     */
    public static final int REMOVE_RENDERSHAPES = 16;

/**
     * flag to show text highlights need to be done last
     */
    public static final int OCR_PDF = 32;


    /**
     * current extraction mode
     */
    private int extractionMode = 7;

    /**
     * current render mode
     */
    protected int renderMode = 7;

    /**
     * decodes page or image
     */
    private PdfStreamDecoder current,currentImageDecoder;



    /**
     * holds pdf id (ie 4 0 R) which stores each object
     */
    Map pagesReferences = new Hashtable();

    /**
     * flag to show if page read to stop multiple reads on Annots in multipage mode
     */
    //private Map pagesRead = new HashMap();

    PdfObject globalResources;


    /**
     * flag to show if there must be a mapping value (program exits if none
     * found)
     */
    public static boolean enforceFontSubstitution = false;

    /**
     * flag to show user wants us to display printable area when we print
     */
    private boolean showImageable = false;

    /**
     * font to use in preference to Lucida
     */
    public static String defaultFont = null;

    /**
     * holds pageformats
     */
    private Map pageFormats = new Hashtable();

    final private static String separator = System.getProperty("file.separator");

    /**
     * flag to show if data extracted as text or XML
     */
    private static boolean isXMLExtraction = true;

    /**
     * used by Storypad to include images in PDFData)
     */
    //private boolean includeImages;

    /**
     * interactive status Bar
     */
    private StatusBar statusBar = null;

    /**
     * flag to say if java 1.3 version should be used for JPEG conversion (new
     * JPEG bugs in Suns 1.4 code)
     */
    //public static boolean use13jPEGConversion = false;

    //<start-wrap>
    /**
     * tells JPedal to display screen using hires images
     */
    boolean useHiResImageForDisplay = false;

    /**
    //<end-wrap>
    boolean useHiResImageForDisplay = true;
    /**/

    /**
     * flag used to show if printing worked
     */
    private boolean operationSuccessful = true;

    /**
     * Any printer errors
     */
    private String pageErrorMessages = "";

    String filename;
     /**/

    private ObjectStore backgroundObjectStoreRef = new ObjectStore();
// <start-me>
    private SetOfIntegerSyntax range;
// <end-me>
    //list of pages in range for quick lookup
    private int[] listOfPages;

    /**
     * printing mode using inbuilt java fonts and getting java to rasterize
     * fonts using Java font if match found (added to get around limitations in
     * PCL printing via JPS) - this is the default off setting
     */
    public static final int NOTEXTPRINT = 0;

    /**
     * printing mode using inbuilt java fonts and getting java to rasterize
     * fonts using Java font if match found (added to get around limitations in
     * PCL printing via JPS)
     */
    public static final int TEXTGLYPHPRINT = 1;

    /**
     * printing mode using inbuilt java fonts and getting java to rasterize
     * fonts using Java font if match found (added to get around limitations in
     * PCL printing via JPS)
     */
    public static final int TEXTSTRINGPRINT = 2;

    /**
     * printing mode using inbuilt java fonts and getting java to rasterize
     * fonts using Java font if match found (added to get around limitations in
     * PCL printing via JPS) - overrides embedded fonts for standard fonts (ie Arial)
     */
    public static final int STANDARDTEXTSTRINGPRINT = 3;

    private LinearThread linearizedBackgroundReaderer =null;


    //flag to track if page decoded twice
    private int lastPageDecoded = -1;

    /**
     * used is bespoke version of JPedal - do not use
     */
    private boolean isCustomPrinting = false;

    public static final int SUBSTITUTE_FONT_USING_FILE_NAME = 1;
    public static final int SUBSTITUTE_FONT_USING_POSTSCRIPT_NAME = 2;
    public static final int SUBSTITUTE_FONT_USING_FAMILY_NAME = 3;
    public static final int SUBSTITUTE_FONT_USING_FULL_FONT_NAME = 4;
    public static final int SUBSTITUTE_FONT_USING_POSTSCRIPT_NAME_USE_FAMILY_NAME_IF_DUPLICATES= 5;

    /**
     * determine how font substitution is done
     */
    private static int fontSubstitutionMode = PdfDecoder.SUBSTITUTE_FONT_USING_FILE_NAME;
    //private static int fontSubstitutionMode=PdfDecoder.SUBSTITUTE_FONT_USING_POSTSCRIPT_NAME;
    //private static int fontSubstitutionMode=PdfDecoder.SUBSTITUTE_FONT_USING_FULL_FONT_NAME;
    //private static int fontSubstitutionMode=PdfDecoder.SUBSTITUTE_FONT_USING_FAMILY_NAME;
    //private static int fontSubstitutionMode=PdfDecoder.SUBSTITUTE_FONT_USING_POSTSCRIPT_NAME_USE_FAMILY_NAME_IF_DUPLICATES;

    /** the ObjectStore for this file for printing */
    ObjectStore objectPrintStoreRef = new ObjectStore();


    public static final int BORDER_SHOW=1;
    public static final int BORDER_HIDE=0;
    public static int CURRENT_BORDER_STYLE = 1;

    public static void setBorderStyle(int style){
        CURRENT_BORDER_STYLE = style;
    }

    public static int getBorderStyle(){
        return CURRENT_BORDER_STYLE;
    }


    //<start-adobe>

    /**
     * pass current locations into Renderer so it can draw forms on
     * other pages correctly offset
     *
     * @param xReached
     * @param yReached
     */
    protected void setMultiPageOffsets(int[] xReached, int[] yReached) {
        /**pass in values for forms/annots*/
        if (formRenderer != null)
            formRenderer.getCompData().setPageDisplacements(xReached, yReached);

    }
    //<end-adobe>


    /**
     * see if file open - may not be open if user interrupted open or problem
     * encountered
     */
    public boolean isOpen() {
        return isOpen;
    }

    //<start-adobe><start-me>
    /**
     * return markedContent object as XML Document
     * @return Document containing XML structure with data
     */
    public Document getMarkedContent() {

        /**
         * objects for structured content
         */
        content.setRootValues(structTreeRootObj, markInfoObj);

        return content.getMarkedContentTree(currentPdfFile, this, pageLookup);
    }
    // <end-me>

    /**
     * used by remote printing to pass in page metrics
     *
     * @param pageData
     */
    public void setPageData(PdfPageData pageData) {
        this.pageData = pageData;
    }

    //used by Storypad to create set of outlines - not part of API
    //and will change
    /**public void setAlternativeOutlines(Rectangle[] outlines, String altName) {
     this.alternateOutlines = outlines;
     this.altName = altName;

     this.repaint();
     }/**/

    /**
     * used by Storypad to display split spreads not part of API
     *
     public void flushAdditionalPages() {
     pages.clearAdditionalPages();
     xOffset = 0;
     additionalPageCount = 0;

     } /**/

    /**
     * used by Storypad to display split spreads not aprt of API
     */
    public void addAdditionalPage(DynamicVectorRenderer dynamicRenderer, int pageWidth, int origPageWidth) {

        //pageWidth=pageWidth+this.insetW+this.insetW;
        pages.addAdditionalPage(dynamicRenderer, pageWidth, origPageWidth);

        if (additionalPageCount == 0) {
            lastWidth = xOffset + origPageWidth;
            xOffset = xOffset + pageWidth;
        } else {
            xOffset = xOffset + pageWidth;
            lastWidth = lastWidth + lastPage;
        }
        additionalPageCount++;
        lastPage = pageWidth;
        // <start-me>
        this.updateUI();
        // <end-me>
    }

    public int getXDisplacement() {
        return lastWidth;
    }

    public int getAdditionalPageCount() {
        return additionalPageCount;
    }

    //<end-adobe>
    /**
     * used by Javascript to update page number
     */
    public void updatePageNumberDisplayed(int page) {

        //update page number
        if (page != -1 && customSwingHandle != null)
            ((org.jpedal.gui.GUIFactory) customSwingHandle).setPage(page);

    }

    /**
     * return page number for last page decoded (only use in SingleDisplay mode)
     */
    public int getlastPageDecoded() {
        return lastPageDecoded;  //To change body of created methods use File | Settings | File Templates.
    }

    /**
     * set page number for last page decoded (only use in SingleDisplay mode)
     * Only used when file is not PDf but has multiple pages (i.e multipaged tiff)
     */
    public void setlastPageDecoded(int page) {
        lastPageDecoded = page;
    }

    /**
     * initialise OC Content and other items before Page decoded but after Resources read
     * @param current
     */
    public void setupPage(PdfStreamDecoder current, boolean alwaysCheck) {

        // read any names
        try {

            if (nameObj != null){
                currentPdfFile.readNames(nameObj, javascript,false);
                nameObj=null;
            }
        } catch (Exception e) {
        	if(LogWriter.isOutput())
        		LogWriter.writeLog("Exception reading Names "  + ' ' + objectStoreRef.fullFileName);
        }

        /**
         * layers
         */
        if(OCProperties!=null && (this.pageNumber!=OCpageNumber || alwaysCheck)){

            currentPdfFile.checkResolved(OCProperties);

            if(layers==null)
                layers=new PdfLayerList();

            layers.init(OCProperties, PropertiesObj, currentPdfFile);

            OCpageNumber=pageNumber;
        }

        current.setObjectValue(ValueTypes.PdfLayerList,layers);
    }

    public static boolean isHires() {
        return hires;
    }

    /**
     * return details on page for type (defined in org.jpedal.constants.PageInfo) or null if no values
     * Unrecognised key will throw a RunTime exception
     *
     * null returned if JPedal not clear on result
     */
    public Iterator getPageInfo(int type) {
        switch(type){

            case PageInfo.COLORSPACES:
                return colorSpacesUsed;

            default:
                return null;
        }
    }

    /**handle forms drawing as not take care of in decodePage as in other modes*/
    public void resetFormsForPageFlow(int page) {

        //put on new forms
        if (formRenderer != null){

            formRenderer.removeDisplayComponentsFromScreen();
            add(scroll,BorderLayout.SOUTH);

            lastFormPage=-1; //reset so will appear on reparse

            formRenderer.displayComponentsOnscreen(page,page);

            //switch off if forms for this page found
            if(formRenderer.getCompData().getStartComponentCountForPage(page)!=-1)
                lastFormPage=page; //ensure not called too early
        }
    }

    //<start-adobe>diff -r.
    // <start-me>
    /**
     * provide direct access to outlineData object
     * @return  OutlineData
     */
    public OutlineData getOutlineData() {
        return outlineData;
    }


    // <end-me>


    /**
     * track if file still oaded in background
     * @return
     */
    public boolean isLoadingLinearizedPDF() {
         return false;
         /**/
    }

    /**
     * class to repaint multiple views
     */
    private class RefreshLayout extends ComponentAdapter {

        java.util.Timer t2 = null;

        /*
           * (non-Javadoc)
           *
           * @see java.awt.event.ComponentListener#componentMoved(java.awt.event.ComponentEvent)
           */
        public void componentMoved(ComponentEvent e) {
            startTimer();
            //	screenNeedsRedrawing=true;

        }

        /*
           */
        public void componentResized(ComponentEvent e) {
            startTimer();
            //	screenNeedsRedrawing=true;

        }

        private void startTimer() {

            //whatever else, stop current decode
            //pages.stopGeneratingPage();

            //turn if off if running
            if (t2 != null)
                t2.cancel();

            //restart - if its not stopped it will trigger page update
            TimerTask listener = new PageListener();
            t2 = new java.util.Timer();
            t2.schedule(listener, 500);

        }
        
        /**
         * fix submitted by Niklas Matthies
         */
        public void dispose() {
            t2.cancel();
        }

        /**
         * used to update statusBar object if exists
         */
        class PageListener extends TimerTask {

            public void run() {

                if (Display.debugLayout)
                    System.out.println("ActionPerformed " + pageCount);

                pages.stopGeneratingPage();
                
                //Ensure page range does not drop below one
                if(pageNumber<1)
                	pageNumber = 1;
                
                pages.decodeOtherPages(pageNumber, pageCount);


            }
        }
    }

    //<end-adobe>
    /**
     * work out machine type so we can call OS X code to get around Java bugs.
     */
    static {

        /**
         * see if mac
         */
        try {
            String name = System.getProperty("os.name");
            if (name.equals("Mac OS X"))
                PdfDecoder.isRunningOnMac = true;
            else if (name.startsWith("Windows")) {
                PdfDecoder.isRunningOnWindows = true;
            }else if (name.startsWith("AIX")) {
                PdfDecoder.isRunningOnAIX = true;
            } else {
                if (name.equals("Linux")) {
                    PdfDecoder.isRunningOnLinux = true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // <start-me>
        /**
         * get version number so we can avoid bugs in various versions
         */
        try{
            PdfDecoder.javaVersion=Float.parseFloat(System.getProperty("java.specification.version"));
        }catch(Exception e){
            e.printStackTrace();
        }
        //<end-me>
        


    }

    /**
     * allows a number of fonts to be mapped onto an actual font and provides a
     * way around slightly differing font naming when substituting fonts - So if
     * arialMT existed on the target machine and the PDF contained arial and
     * helvetica (which you wished to replace with arialmt), you would use the
     * following code -
     * <p/>
     * String[] aliases={"arial","helvetica"};
     * currentPdfDecoder.setSubstitutedFontAliases("arialmt",aliases); -
     * <p/>
     * comparison is case-insensitive and file type/ending should not be
     * included - For use in conjunction with -Dorg.jpedal.fontdirs options which allows
     * user to pass a set of comma separated directories with Truetype fonts
     * (directories do not need to exist so can be multi-platform setting)
     */
    public static void setSubstitutedFontAliases(String fontFileName, String[] aliases) {

        if (aliases != null) {

            String name = fontFileName.toLowerCase(), alias;
            int count = aliases.length;
            for (int i = 0; i < count; i++) {
                alias = aliases[i].toLowerCase();
                if (!alias.equals(name))
                    FontMappings.fontSubstitutionAliasTable.put(alias, name);
            }
        }
    }

    /**
     * takes a comma separated list of font directories and add to substitution
     */
    private static String addFonts(String fontDirs, String failed) {

        StringTokenizer fontPaths = new StringTokenizer(fontDirs, ",");

        while (fontPaths.hasMoreTokens()) {

            String fontPath = fontPaths.nextToken();

            if (!fontPath.endsWith("/") & !fontPath.endsWith("\\"))
                fontPath = fontPath + separator;

            //LogWriter.writeLog("Looking in " + fontPath + " for TT fonts");

            addTTDir(fontPath, failed);
        }

        return failed;
    }

    //<start-adobe>
    /**
     * turns off the viewable area, scaling the page back to original scaling
     * <br>
     * <br>
     * NOT RECOMMENDED FOR GENERAL USE (this has been added for a specific
     * client and we have found it can be unpredictable on some PDF files).
     */
    public void resetViewableArea() {

        if (viewableArea != null) {
            viewableArea = null;
            // @fontHandle currentDisplay.setOptimiseDrawing(true);
            setPageRotation(displayRotation);
            repaint();
        }
    }

    /**
     * allows the user to create a viewport within the displayed page, the
     * aspect ratio is keep for the PDF page <br>
     * <br>
     * Passing in a null value is the same as calling resetViewableArea()
     * <p/>
     * <br>
     * <br>
     * The viewport works from the bottom left of the PDF page <br>
     * The general formula is <br>
     * (leftMargin, <br>
     * bottomMargin, <br>
     * pdfWidth-leftMargin-rightMargin, <br>
     * pdfHeight-bottomMargin-topMargin)
     * <p/>
     * <br>
     * <br>
     * NOT RECOMMENDED FOR GENERAL USE (this has been added for a specific
     * client and we have found it can be unpredictable on some PDF files).
     * <p/>
     * <br>
     * <br>
     * The viewport will not be incorporated in printing <br>
     * <br>
     * Throws PdfException if the viewport is not totally enclosed within the
     * 100% cropped pdf
     */
    public AffineTransform setViewableArea(Rectangle viewport)
            throws PdfException {

        if (viewport != null) {

            double x = viewport.getX();
            double y = viewport.getY();
            double w = viewport.getWidth();
            double h = viewport.getHeight();

            // double crx = pageData.getCropBoxX(pageNumber);
            // double cry = pageData.getCropBoxY(pageNumber);
            double crw = pageData.getCropBoxWidth(pageNumber);
            double crh = pageData.getCropBoxHeight(pageNumber);

            // throw exception if viewport cannot fit in cropbox
            if (x < 0 || y < 0 || (x + w) > crw || (y + h) > crh) {
                throw new PdfException(
                        "Viewport is not totally enclosed within displayed panel.");
            }

            // if viewport exactlly matches the cropbox
            if (crw == w && crh == h) {
            } else {// else work out scaling ang apply

                viewableArea = viewport;
                setPageRotation(displayRotation);
                repaint();
            }
        } else {
            resetViewableArea();
        }

        return viewScaling;
    }
    //<end-adobe>

    /**
     * takes a String[] of font directories and adds to substitution - Can just
     * be called for each JVM - Should be called before file opened - this
     * offers an alternative to the call -DFontDirs - Passing a null value
     * flushes all settings
     *
     * @return String which will be null or list of directories it could not
     *         find
     */
    public static String setFontDirs(String[] fontDirs) {

        String failed = null;

        if (FontMappings.fontSubstitutionTable == null) {
            FontMappings.fontSubstitutionTable = new HashMap();
            FontMappings.fontSubstitutionFontID = new HashMap();
            FontMappings.fontPossDuplicates = new HashMap();
            FontMappings.fontPropertiesTable = new HashMap();
        }

        try {
            if (fontDirs == null) { // idiot safety test
            	if(LogWriter.isOutput())
            		LogWriter.writeLog("Null font parameter passed");
            	
                FontMappings.fontSubstitutionAliasTable.clear();
                FontMappings.fontSubstitutionLocation.clear();
                FontMappings.fontSubstitutionTable.clear();
                FontMappings.fontSubstitutionFontID.clear();
                FontMappings.fontPossDuplicates.clear();
                FontMappings.fontPropertiesTable.clear();
            } else {

                int count = fontDirs.length;

                for (int i = 0; i < count; i++) {

                    String fontPath = fontDirs[i];

                    // allow for 'wrong' separator
                    if (!fontPath.endsWith("/") & !fontPath.endsWith("\\"))
                        fontPath = fontPath + separator;

                    if(debugFonts)
                        System.out.println("Looking in " + fontPath
                                + " for fonts");
                    //LogWriter.writeLog("Looking in " + fontPath
                    //		+ " for TT fonts");

                    failed = addTTDir(fontPath, failed);
                }
            }
        } catch (Exception e) {
        	if(LogWriter.isOutput())
        		LogWriter.writeLog("Unable to run setFontDirs " + e.getMessage());
        }

        return failed;
    }

    /**
     * add a truetype font directory and contents to substitution
     */
    private static String addTTDir(String fontPath, String failed) {

        if (FontMappings.fontSubstitutionTable == null) {
            FontMappings.fontSubstitutionTable = new HashMap();
            FontMappings.fontSubstitutionFontID = new HashMap();
            FontMappings.fontPossDuplicates = new HashMap();
            FontMappings.fontPropertiesTable = new HashMap();
        }

        File currentDir = new File(fontPath);

        if ((currentDir.exists()) && (currentDir.isDirectory())) {

            String[] files = currentDir.list();

            if (files != null) {
                int count = files.length;

                for (int i = 0; i < count; i++) {
                    String currentFont = files[i];

                    addFontFile(currentFont, fontPath);

                }
            }
        } else {
            if (failed == null) {
                failed = fontPath;
            } else {
                failed = failed + ',' + fontPath;
            }
        }

        return failed;
    }


    /**
     * set mode to use when substituting fonts (default is to use Filename (ie arial.ttf)
     * Options are  SUBSTITUTE_* values from PdfDecoder
     */
    public static void setFontSubstitutionMode(int mode) {
        fontSubstitutionMode = mode;
    }

    /**
     * set mode to use when substituting fonts (default is to use Filename (ie arial.ttf)
     * Options are  SUBSTITUTE_* values from PdfDecoder
     */
    public static int getFontSubstitutionMode() {
        return fontSubstitutionMode;
    }

    //<start-demo>
    /**
    //<end-demo>


    /**
     * method to add a single file to the PDF renderer
     *
     * @param currentFont - actual font name we use to identify
     * @param fontPath    - full path to font file used for this font
     */
    public static void addFontFile(String currentFont, String fontPath) {

        if (FontMappings.fontSubstitutionTable == null) {
            FontMappings.fontSubstitutionTable = new HashMap();
            FontMappings.fontSubstitutionFontID = new HashMap();
            FontMappings.fontPossDuplicates = new HashMap();
            FontMappings.fontPropertiesTable = new HashMap();
        }

        //add separator if needed
        if (fontPath != null && !fontPath.endsWith("/") && !fontPath.endsWith("\\"))
            fontPath = fontPath + separator;

        String name = currentFont.toLowerCase();

        //decide font type
        int type = StandardFonts.getFontType(name);


        if(debugFonts)
            System.out.println(type+" "+name);

        InputStream in = null;

        if (type != StandardFonts.FONT_UNSUPPORTED) {
            // see if root dir exists

            boolean failed=false;

            try {
                in = new FileInputStream(fontPath + currentFont);

            } catch (Exception e) {
                e.printStackTrace();
                failed=true;
            } catch (Error err) {
                err.printStackTrace();
                failed=true;
            }

            // if it does, add
            if (!failed) {

                String fontName;

                //name from file
                int pointer = currentFont.indexOf('.');
                if (pointer == -1)
                    fontName = currentFont.toLowerCase();
                else
                    fontName = currentFont.substring(0, pointer).toLowerCase();

                if(debugFonts)
                    System.out.println("Looking at  "+fontName+" fontSubstitutionMode="+fontSubstitutionMode);

                //choose filename  or over-ride if OpenType
                if (fontSubstitutionMode == PdfDecoder.SUBSTITUTE_FONT_USING_FILE_NAME|| type == StandardFonts.OPENTYPE) {
                    if(type==StandardFonts.TYPE1)
                        FontMappings.fontSubstitutionTable.put(fontName, "/Type1");
                    else//TT or OTF
                        FontMappings.fontSubstitutionTable.put(fontName, "/TrueType");

                    FontMappings.fontSubstitutionLocation.put(fontName, fontPath + currentFont);

                    //store details under file
                    FontMappings.fontPropertiesTable.put(fontName+"_type",new Integer(type));
                    FontMappings.fontPropertiesTable.put(fontName+"_path",fontPath + currentFont);

                    if(debugFonts)
                        System.out.println("Added  PdfDecoder.SUBSTITUTE_FONT_USING_FILE_NAME "+StandardFonts.getFontDetails(type, fontPath + currentFont));

                } else if (type == StandardFonts.TRUETYPE_COLLECTION || type == StandardFonts.TRUETYPE) {

                    if(fontSubstitutionMode==PdfDecoder.SUBSTITUTE_FONT_USING_POSTSCRIPT_NAME_USE_FAMILY_NAME_IF_DUPLICATES){

                        //get both possible values
                        String[] postscriptNames=null;
                        try {
                            postscriptNames = StandardFonts.readNamesFromFont(type, fontPath + currentFont, PdfDecoder.SUBSTITUTE_FONT_USING_POSTSCRIPT_NAME);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        String[] familyNames =null;
                        try {
                            familyNames = StandardFonts.readNamesFromFont(type, fontPath + currentFont, PdfDecoder.SUBSTITUTE_FONT_USING_FAMILY_NAME);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        int fontCount=0;
                        if(postscriptNames!=null)
                            fontCount=postscriptNames.length;

                        for(int ii=0;ii<fontCount;ii++){

                            //allow for null and use font name
                            if (postscriptNames[ii] == null)
                                postscriptNames[ii] = Strip.stripAllSpaces(fontName);

                            //allow for null and use font name
                            if (familyNames[ii] == null)
                                familyNames[ii] = Strip.stripAllSpaces(fontName);

                            Object fontSubValue= FontMappings.fontSubstitutionTable.get(postscriptNames[ii]);
                            Object possDuplicate=FontMappings.fontPossDuplicates.get(postscriptNames[ii]);
                            if(fontSubValue==null && possDuplicate==null){ //first time so store and track

                                //System.out.println("store "+postscriptNames[ii]);

                                FontMappings.fontSubstitutionTable.put(postscriptNames[ii], "/TrueType");
                                FontMappings.fontSubstitutionLocation.put(postscriptNames[ii], fontPath + currentFont);
                                FontMappings.fontSubstitutionFontID.put(postscriptNames[ii], new Integer(ii));

                                //and remember in case we need to switch
                                FontMappings.fontPossDuplicates.put(postscriptNames[ii],familyNames[ii]);

                                if(debugFonts)
                                    System.out.println("Added  2");

                            }else if(!familyNames[ii].equals(postscriptNames[ii])){
                                //if no duplicates,add to mappings with POSTSCRIPT and log filename
                                //both lists should be in same order and name

                                //else save as FAMILY_NAME
                                FontMappings.fontSubstitutionTable.put(postscriptNames[ii], "/TrueType");
                                FontMappings.fontSubstitutionLocation.put(postscriptNames[ii], fontPath + currentFont);
                                FontMappings.fontSubstitutionFontID.put(postscriptNames[ii], new Integer(ii));

                                //store details under file
                                FontMappings.fontPropertiesTable.put(postscriptNames[ii]+"_type",new Integer(type));
                                FontMappings.fontPropertiesTable.put(postscriptNames[ii]+"_path",fontPath + currentFont);

                                if(debugFonts)
                                    System.out.println("Added  3");

                                //if second find change first match
                                if(!possDuplicate.equals("DONE")){

                                    //System.out.println("replace "+postscriptNames[ii]+" "+familyNames[ii]);

                                    //flag as done
                                    FontMappings.fontPossDuplicates.put(postscriptNames[ii],"DONE");

                                    //swap over
                                    FontMappings.fontSubstitutionTable.remove(postscriptNames[ii]);
                                    FontMappings.fontSubstitutionTable.put(familyNames[ii], "/TrueType");

                                    String font=(String)FontMappings.fontSubstitutionLocation.get(postscriptNames[ii]);
                                    FontMappings.fontSubstitutionLocation.remove(postscriptNames[ii]);
                                    FontMappings.fontSubstitutionLocation.put(familyNames[ii], font);

                                    FontMappings.fontSubstitutionFontID.remove(postscriptNames[ii]);
                                    FontMappings.fontSubstitutionFontID.put(familyNames[ii], new Integer(ii));

                                    //store details under file
                                    FontMappings.fontPropertiesTable.remove(familyNames[ii]+"_path");
                                    FontMappings.fontPropertiesTable.remove(familyNames[ii]+"_type");

                                    FontMappings.fontPropertiesTable.put(familyNames[ii]+"_type",new Integer(type));
                                    FontMappings.fontPropertiesTable.put(familyNames[ii]+"_path",fontPath + currentFont);

                                }
                            }
                        }

                    }else{ //easy version
                        //read 1 or more font mappings from file
                        String[] fontNames = new String[0];
                        try {
                            fontNames = StandardFonts.readNamesFromFont(type, fontPath + currentFont, fontSubstitutionMode);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        for (int i = 0; i < fontNames.length; i++) {

                            //allow for null and use font name
                            if (fontNames[i] == null)
                                fontNames[i] = Strip.stripAllSpaces(fontName);

                            FontMappings.fontSubstitutionTable.put(fontNames[i], "/TrueType");
                            FontMappings.fontSubstitutionLocation.put(fontNames[i], fontPath + currentFont);
                            FontMappings.fontSubstitutionFontID.put(fontNames[i], new Integer(i));

                            //store details under file
                            FontMappings.fontPropertiesTable.put(fontNames[i]+"_type",new Integer(type));
                            FontMappings.fontPropertiesTable.put(fontNames[i]+"_path",fontPath + currentFont);


                            if(debugFonts)
                                System.out.println("Added  4");

                        }
                    }
                }else if(type==StandardFonts.TYPE1){// || type == StandardFonts.OPENTYPE){ //type1

                    //read 1 or more font mappings from file
                    String[] fontNames = new String[0];
                    try {
                        fontNames = StandardFonts.readNamesFromFont(type, fontPath + currentFont, fontSubstitutionMode);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    for (int i = 0; i < fontNames.length; i++) {

                        //allow for null and use font name
                        if (fontNames[i] == null)
                            fontNames[i] = Strip.stripAllSpaces(fontName);

                        //System.out.println("font="+fontNames[i]);

                        FontMappings.fontSubstitutionTable.put(fontNames[i], "/Type1");
                        FontMappings.fontSubstitutionLocation.put(fontNames[i], fontPath + currentFont);
                        FontMappings.fontSubstitutionFontID.put(fontNames[i], new Integer(i));

                        //store details under file
                        FontMappings.fontPropertiesTable.put(fontNames[i]+"_type",new Integer(type));
                        FontMappings.fontPropertiesTable.put(fontNames[i]+"_path",fontPath + currentFont);


                        if(debugFonts)
                        System.out.println("Added  5");

                    }
                    //}
                }
            } else if(LogWriter.isOutput()){
                LogWriter.writeLog("No fonts found at " + fontPath);
            }
        }

        //finally close
        if(in!=null){
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
    }

    //<start-adobe>
    /**
     * return type of alignment for pages if smaller than panel
     * - see options in Display class.
     */
    public int getPageAlignment() {
        return alignment;
    }


    /**
     * This will be needed for text extraction as it paramter makes sure widths
     * included in text stream
     *
     * @param newEmbedWidthData -
     *                          flag to embed width data in text fragments for use by grouping
     *                          algorithms
     */
    public static void init(boolean newEmbedWidthData) {

        /** get local handles onto objects/data passed in */
        embedWidthData = newEmbedWidthData;

    }
    //<end-adobe>

    /**
     * Recommend way to create a PdfDecoder if no rendering of page may be
     * required<br>
     * Otherwise use PdfDecoder()
     *
     * @param newRender flag to show if pages being rendered for JPanel or extraction
     */
    public PdfDecoder(boolean newRender) {

        pages = new SingleDisplay(this);

        /** get local handles onto flag passed in */
        this.renderPage = newRender;

        setLayout(null);

        startup();
    }

    //<start-adobe>
    /**
     * Not part of API - internal IDR method subject to frequent change
     *
     public PdfDecoder(int mode, boolean newRender) {

     pages = new SingleDisplay(this);

     // get local handles onto flag passed in
     this.renderPage = newRender;
     //this.useHiResImageForDisplay=true;

     extractionMode = 1;

     setLayout(null);

     init(true);

     startup();

     PdfStreamDecoder.runningStoryPad = true;

     }/**/
    //<end-adobe>

    /**
     *
     */
    private void startup() {
//		System.out.println(this+" PdfDecoder.startup()");
//		ConvertToString.printStackTrace(5);

        formRenderer = new DefaultAcroRenderer();


        //pass in user handler if set
        formRenderer.resetHandler(null, this,Options.FormsActionHandler);

        //pass in user handler if set
        formRenderer.resetHandler(null, this, Options.LinkHandler);


        //once only setup for fonts (dispose sets flag to false just incase)
        if(!fontsInitialised)
                initFonts();

        // set global flags

        String debugFlag = System.getProperty("debug");

        if (debugFlag != null)
            LogWriter.setupLogFile(true, 1, "", "v", false);

        // needs to be set so we can over-ride
        if (renderPage) {
            //setToolTipText("image preview");

            // initialisation on font
            highlightFont = new Font("Lucida", Font.BOLD, size);

            setPreferredSize(new Dimension(100, 100));
        }
    }    /**/

    /**
     * flag to enable popup of error messages in JPedal
     */
    public static boolean showErrorMessages = false;

    protected int specialMode= SpecialOptions.NONE;


    /**
     * Recommend way to create a PdfDecoder for renderer only viewer (not
     * recommended for server extraction only processes)
     */
    public PdfDecoder() {

        pages = new SingleDisplay(this);

        this.renderPage = true;

        setLayout(null);

        startup();
    }

    private boolean isOpen = false;

    //<start-adobe>
    //<end-adobe>

    /**
     * remove all static elements - only do once completely finished with JPedal
     * as will not be reinitialised
     */
    static public void disposeAllStatic() {

        StandardFonts.dispose();

        FontMappings.dispose();

    }

    /**
     * convenience method to remove all items from memory
     * If you wish to clear all static objects as well, you will also need to call
     * disposeAllStatic()
     */
    final public void dispose() {

        fontsInitialised=false;

        //<start-adobe>
        //code fix from Niklas Matthies
        if(viewListener!=null)
            viewListener.dispose();
        //<end-adobe>

        if(pdfData!=null)
            pdfData.dispose();
        pdfData=null;

        if(pages!=null)
            pages.dispose();
        pages=null;

        defaultFont=null;

        if(currentDisplay!=null)
            currentDisplay.dispose();
        currentDisplay=null;

        if(current!=null)
            current.dispose();

        current=null;
        if(currentPdfFile!=null)
            currentPdfFile.dispose();
        currentPdfFile=null;

        if(javascript!=null)
            javascript.dispose();
        javascript=null;
        //dispose the javascript object before the formRenderer object as JS accesses the renderer object
        if(formRenderer!=null)
            formRenderer.dispose();
        formRenderer=null;

        if(pageLookup!=null)
            pageLookup.dispose();
        pageLookup=null;
        

    }

    /**
     * convenience method to close the current PDF file
     */
    final public void closePdfFile() {

        //<start-demo>
        /**
        //<end-demo>
        /**/

        if (!isOpen)
            return;

        isOpen = false;

        // ensure no previous file still being decoded
        if(linearObj!=null)
        waitForDecodingToFinish();

        //flush linearization objects
        E=-1;
        linearObj=null;
        isLinearizationTested =false;
        linObjects.clear();
        if(linearizedBackgroundReaderer!=null && linearizedBackgroundReaderer.isAlive()){
            linearizedBackgroundReaderer.interrupt();
        }

        displayScaling = null;

        lastPageDecoded = -1;



        // ensure getPageAsImage isn't be called
        checkImagesFinished();

        if(pages!=null)
            pages.stopGeneratingPage();

        //make sure we have stopped thread doing background linear reading
        while(linearizedBackgroundReaderer !=null && linearizedBackgroundReaderer.isAlive() && !linearizedBackgroundReaderer.isInterrupted()){
            try{
                Thread.sleep(500);
            }catch(Exception e){
            }
        }

        //we are closing the page so call the closeing page for forms actions.
        //@chris re - I think this is wrong anyway as PO called on open so have removed.
        //we need an example and then we can implement against
        //@mark - im sure we have an example some where in our library of files, but im not wusre which file,
        // we just have to find it hopefully attached to the page object
        //if (formRenderer != null)
        //	formRenderer.getActionHandler().PO(pageNumber);

        pages.disableScreen();

        //flag all pages unread
        //pagesRead.clear();


        //flush arrays
        overlayType.clear();
        overlayColors.clear();
        overlayObj.clear();

        //flush arrays
        overlayTypeG.clear();
        overlayColorsG.clear();
        overlayObjG.clear();

        // pass handle into renderer
        if (formRenderer != null) {
            formRenderer.openFile(pageCount);

            formRenderer.resetFormData(pageLookup, insetW, insetH, pageData, currentPdfFile, acroFormObj);

            formRenderer.removeDisplayComponentsFromScreen();
        }

        // remove listener if setup
        if (hasViewListener) {
            hasViewListener = false;

            //flush any cached pages
            pages.flushPageCaches();

            //<start-adobe>
            removeComponentListener(viewListener);
            //<end-adobe>

        }

        if(linHintTable!=null){
            linHintTable=null;
        }

        if (currentPdfFile != null){
            currentPdfFile.closePdfFile();

            currentPdfFile = null;
        }

        pages.disableScreen();
        currentDisplay.flush();
        objectStoreRef.flush();

        ObjectStore.flushPages();

        objectStoreRef.flush();
        objectPrintStoreRef.flush();

        oldScaling = -1;

        pageCount=0;

        //flush objects held
        metadataObj=null;
        nameObj=null;
        structTreeRootObj=null;
        markInfoObj=null;
        acroFormObj=null;
        OutlinesObj=null;
        PropertiesObj=null;


        //flush OCContentData
        layers=null;
        OCProperties=null;
        OCpageNumber=-1;

        //<start-adobe>
        this.setDisplayView(Display.SINGLE_PAGE, Display.DISPLAY_CENTERED);
        //<end-adobe>

        if (SwingUtilities.isEventDispatchThread()){
            validate();
        }else {
            final Runnable doPaintComponent = new Runnable() {
                public void run() {
                    validate();
                }
            };
            SwingUtilities.invokeLater(doPaintComponent);
        }
    }
    
    
    //<start-demo>
    /**
    //<end-demo>
    public void showExpiry(){
      }
   
    /**/
    

    private void checkImagesFinished() {
        while (isGeneratingPage) {
            // System.out.println("Waiting to die");
            try {
                Thread.sleep(100);
                // System.out.println("still generating page!");
            } catch (InterruptedException e) {
                // should never be called
                e.printStackTrace();

                //ensure will exit loop
                isGeneratingPage=false;
            }
        }
    }

    /**
     * convenience method to get the PDF data as a byte array - works however
     * file was opened.
     *
     * @return byte array containing PDF file
     */
    final public byte[] getPdfBuffer() {

        byte[] buf = null;
        if (currentPdfFile != null)
            buf = currentPdfFile.getObjectReader().getBuffer();

        return buf;
    }

    //<start-adobe>
    /**
     * Access should not generally be required to
     * this class. Please look at getBackgroundGroupingObject() - provide method
     * for outside class to get data object containing text and metrics of text. -
     * Viewer can only access data for finding on page
     *
     * @return PdfData object containing text content from PDF
     */
    final public PdfData getPdfBackgroundData() {

        return pdfBackgroundData;
    }

    /**
     * Access should not generally be required to
     * this class. Please look at getGroupingObject() - provide method for
     * outside class to get data object containing raw text and metrics of text<br> -
     * Viewer can only access data for finding on page
     *
     * @return PdfData object containing text content from PDF
     */
    final public PdfData getPdfData() throws PdfException {
        if ((extractionMode & PdfDecoder.TEXT) == 0)
            throw new PdfException(
                    "[PDF] Page data object requested will be empty as text extraction disabled. Enable with PdfDecoder method setExtractionMode(PdfDecoder.TEXT | other values");
        else
            return pdfData;
    }

    /**
     * <B>Not part of API</B> provide method for outside class to get data
     * object containing information on the page for calculating grouping <br>
     * Please note: Structure of PdfPageData is not guaranteed to remain
     * constant. Please contact IDRsolutions for advice.
     *
     *PdfPageData object
     *  from 2.50
     *
     final public PdfPageData getPdfBackgroundPageData() {
     return pageData;
     }/**/

    /**
     * flag to show if PDF document contains an outline
     */
    final public boolean hasOutline() {
        return hasOutline;
    }

    // <start-me>
    /**
     * return a DOM document containing the PDF Outline object as a DOM Document - may return null
     */
    final public Document getOutlineAsXML() {

        if (outlineData == null && OutlinesObj != null) {

            //check read as may be used for Dest
            if (nameObj != null){
                currentPdfFile.readNames(nameObj, javascript,false);
                nameObj=null;
            }

            try {
                currentPdfFile.checkResolved(OutlinesObj);

                outlineData = new OutlineData(pageCount);
                outlineData.readOutlineFileMetadata(OutlinesObj, currentPdfFile, pageLookup);

            } catch (Exception e) {
            	if(LogWriter.isOutput())
            		LogWriter.writeLog("Exception " + e + " accessing outline ");
                outlineData = null;
            }
        }

        if (outlineData != null)
            return outlineData.getList();
        else
            return null;
    }
    // <end-me>

    //<end-adobe>
    /**
     * Provides method for outside class to get data
     * object containing information on the page for calculating grouping <br>
     * Please note: Structure of PdfPageData is not guaranteed to remain
     * constant. Please contact IDRsolutions for advice.
     *
     * @return PdfPageData object
     */
    final public PdfPageData getPdfPageData() {
        return pageData;
    }

    public Point getPageOffsets(int page){
        return new Point(pages.getXCordForPage(page), pages.getYCordForPage(page));
    }

    /**
     * set page range (inclusive) -
     * If end is less than start it will print them
     * backwards (invalid range will throw PdfException)
     *
     * @throws PdfException
     */
    public void setPagePrintRange(int start, int end) throws PdfException {
        this.start = start;
        this.end = end;

        //all returns huge number not page end range
        if (end == 2147483647)
            end = pageCount;

        //if actually backwards, reverse order
        if (start > end) {
            int tmp = start;
            start = end;
            end = tmp;
        }
        if ((start < 1) || (end < 1) || (start > this.pageCount) || (end > this.pageCount))
            throw new PdfException(Messages.getMessage("PdfViewerPrint.InvalidPageRange") + ' ' + start + ' ' + end);

    }

    /**
     * allow user to select only odd or even pages to print
     */
    public void setPrintPageMode(int mode) {
        oddPagesOnly = (mode & PrinterOptions.ODD_PAGES_ONLY) == PrinterOptions.ODD_PAGES_ONLY;
        evenPagesOnly = (mode & PrinterOptions.EVEN_PAGES_ONLY) == PrinterOptions.EVEN_PAGES_ONLY;

        pagesPrintedInReverse = (mode & PrinterOptions.PRINT_PAGES_REVERSED) == PrinterOptions.PRINT_PAGES_REVERSED;

    }

    // <start-me>
    /**
     * set inclusive range to print (see SilentPrint.java and SimpleViewer.java
     * for sample print code (invalid range will throw PdfException)
     * can  take values such as  new PageRanges("3,5,7-9,15");
     */
    public void setPagePrintRange(SetOfIntegerSyntax range) throws PdfException {

        if (range == null)
            throw new PdfException("[PDF] null page range entered");

        this.range = range;
        this.start = range.next(0); // find first

        int rangeCount = 0;

        //get number of items
        for (int ii = 0; ii < this.pageCount; ii++) {
            if (range.contains(ii))
                rangeCount++;
        }


        //setup array
        listOfPages = new int[rangeCount + 1];

        // find last
        int i = start;
        this.end = start;
        if (range.contains(2147483647)) //allow for all returning largest int
            end = pageCount;
        else {
            while (range.next(i) != -1)
                i++;
            end = i;
        }

        //if actually backwards, reverse order
        if (start > end) {
            int tmp = start;
            start = end;
            end = tmp;
        }

        //populate table
        int j = 0;

        for (int ii = start; ii < end + 1; ii++) {
            if (range.contains(ii) && (!oddPagesOnly || (ii & 1) == 1) && (!evenPagesOnly || (ii & 1) == 0)) {
                listOfPages[j] = ii - start;
                j++;
            }
        }

        if ((start < 1) || (end < 1) || (start > this.pageCount) || (end > this.pageCount))
            throw new PdfException(Messages.getMessage("PdfViewerPrint.InvalidPageRange") + ' ' + start + ' ' + end);

    }
    // <end-me>

    /**
     * tells program to try and use Java's font printing if possible as work
     * around for issue with PCL printing - values are PdfDecoder.TEXTGLYPHPRINT
     * (use Java to rasterize font if available) PdfDecoder.TEXTSTRINGPRINT(
     * print as text not raster - fastest option) PdfDecoder.NOTEXTPRINT
     * (default - highest quality)
     */
    public void setTextPrint(int textPrint) {
        this.textPrint = textPrint;
    }

    /**
     * flag to use Java's inbuilt font renderer if possible
     */
    private int textPrint = 0;

    /**
     * the size above which objects stored on disk (-1 is off)
     */
    private int minimumCacheSize = -1;//20000;

    /**
     * return any messages on decoding
     */
    String decodeStatus = "";

    /**
     * current print page or -1 if finished
     */
    private int currentPrintPage = 0;

    private boolean imagesProcessedFully=true,hasNonEmbeddedCIDFonts,hasYCCKimages,ttHintingRequired,timeout=false;
    private String nonEmbeddedCIDFonts="";

    private Object customSwingHandle;
    private CustomFormPrint customFormPrint;

    private Object userExpressionEngine;

    private boolean generateGlyphOnRender;

    private boolean thumbnailsBeingDrawn;

    private float oldScaling = -1;

    /**
     * switch on Javascript
     */
    private boolean useJavascript = true;

    /** use this to turn javascript on and off, default is on. */
    public void setJavaScriptUsed(boolean jsEnabled){
    	useJavascript = jsEnabled;
    }

    private boolean centerOnScaling = true;

    public void setCenterOnScaling(boolean center){
        centerOnScaling = center;
    }

    /**
     * If you are printing PDFs using JPedal in your custom
     * code, you may find pages missing, because JPedal does
     * not know about these additional pages. This method
     * allows you to tell JPedal you have already printed pagesPrinted
     */
    public void useLogicalPrintOffset(int pagesPrinted){
        logicalPageOffset=pagesPrinted;
    }

    /**used to render to image and then print the image*/
    BufferedImage printImage =null;


    /**
     * turn list into Array
     * @param values
     * @return
     */
    private static Map toMap(int[] values) {

        if(values==null || values.length==0)
            return null;

        int count=values.length;
        Map newList=new HashMap();

        for(int ii=0;ii<count;ii++)
            newList.put(new Integer(values[ii]),"x");

        return newList;
    }

    private Rectangle workoutClipping(int displayRotation, float scaling,Rectangle vr, int print_x_size, int print_y_size) {

        Rectangle cRect = null;


        double x = vr.getX();
        double y = vr.getY();
        double w = vr.getWidth();
        double h = vr.getHeight();

        /**
         * g2.clipRect((int)((vr.x-insetW)/this.scaling),(int) ((print_y_size) - ((vr.y+vr.height-insetH)/this.scaling))
         * ,(int) (vr.width/this.scaling)-1,(int) (vr.height/this.scaling));
         */

        int newX = 0;
        int newY = 0;
        int newW = 0;
        int newH = 0;

        if(true || !docIsLandscaped){
            switch(displayRotation){
                case(0):
                    newX = (int) ((vr.x-insetW)/this.scaling);
                    newY = (int) ((print_y_size) - ((vr.y+vr.height-insetH)/this.scaling));
                    newW = (int) (vr.width/this.scaling-1);
                    newH = (int) (vr.height/this.scaling);
                    break;

                case(90):
                    newX = (int) (((y-insetH)/this.scaling)) ;
                    newY = (int) (((x-insetW)/this.scaling));
                    newW = (int) (h/this.scaling);
                    newH = (int) (w/this.scaling);
                    break;

                case(180):
                    newY = (int) ((y/this.scaling) - (insetH/this.scaling) );
                    newX = (int) (print_x_size - ((x+w-insetW)/this.scaling));
                    newW = (int) (w/this.scaling);
                    newH = (int) (h/this.scaling);
                    break;

                case(270):
                    newX = (int) ((print_x_size - (y+h-insetH)/this.scaling) );
                    newY = (int) ((print_y_size - (x+w-insetW)/this.scaling) );
                    newW = (int) (h/this.scaling);
                    newH = (int) (w/this.scaling);
                    break;
            }
        } else {
            switch(displayRotation){
                case(0):
                    newX = (int) ((vr.x-insetW)/this.scaling);
                    newY = (int) ((print_x_size) - ((vr.y+vr.height-insetH)/this.scaling));
                    newW = (int) (vr.width/this.scaling-1);
                    newH = (int) (vr.height/this.scaling);
                    break;

                case(90):
                    newX = (int) (((y-insetH)/this.scaling)) ;
                    newY = (int) (((x-insetW)/this.scaling));
                    newW = (int) (h/this.scaling);
                    newH = (int) (w/this.scaling);
                    break;

                case(180):
                    newY = (int) ((y/this.scaling) - (insetH/this.scaling) );
                    newX = (int) (print_y_size - ((x+w-insetW)/this.scaling));
                    newW = (int) (w/this.scaling);
                    newH = (int) (h/this.scaling);
                    break;

                case(270):
                    newX = (int) ((print_y_size - (y+h-insetH)/this.scaling) );
                    newY = (int) ((print_x_size - (x+w-insetW)/this.scaling) );
                    newW = (int) (h/this.scaling);
                    newH = (int) (w/this.scaling);
                    break;
            }
        }


        cRect = new Rectangle(newX,newY,newW,newH);

        return cRect;
    }

    private double[] workoutParameters(int rotation, float scale,Rectangle rect, int xSize, int ySize) {

        double[] nRect = new double[4];


        double x = rect.getX();
        double y = rect.getY();
        double w = rect.getWidth();
        double h = rect.getHeight();

        double newX = 0;
        double newY = 0;
        double newW = 0;
        double newH = 0;

        switch(rotation){
            case(0):
                newX =  -(((x-insetW)/this.scaling));
                newY =  -((ySize) - ((y+h-insetH)/this.scaling));
                newW =  (w/this.scaling);
                newH =  (h/this.scaling);
                break;

            case(90):
                newX =  -(((y-insetH)/this.scaling));
                newY =  -(((x-insetW)/this.scaling));
                newW =  (h/this.scaling);
                newH =  (w/this.scaling);
                break;

            case(180):
                newY =  -((y-insetH)/this.scaling);
                newX =  -(xSize - ((x+w-insetW)/this.scaling));
                newW =  (w/this.scaling);
                newH =  (h/this.scaling);
                break;

            case(270):
                newX =  -((xSize) - ((y+h-insetW)/this.scaling) );
                newY =  -((ySize) - ((x+w-insetH)/this.scaling) );
                newW =  (h/this.scaling);
                newH =  (w/this.scaling);
                break;
        }

        nRect[0] = newX;
        nRect[1] = newY;
        nRect[2] = newW;
        nRect[3] = newH;

        return nRect;
    }


    // <start-me>
    private void createCustomPaper(PageFormat pf, int clipW, int clipH) {

    	Paper customPaper = new Paper();

        //System.out.println("createCustomPaper "+pf+" "+clipW+" "+clipH);
        // Do not change this code, if you have check if it doesn't break
        // the barcode file (ABACUS) !
        // The barcode is to be printed horizontally on the page!

        if(this.pageCount==1 || allowDifferentPrintPageSizes){
            customPaper.setSize(clipW, clipH);
            customPaper.setImageableArea(0, 0, clipW, clipH);
        } else {

            // Due to the way printing (different sized pages in one go) works in Java
            // we work out the biggest for the printed selection and apply it to all
            // printed pages.
            int paperClipW = 0;
            int paperClipH = 0;

            for(int t=this.start;t<=this.end;t++){
                if(clipW <= (this.pageData.getMediaBoxWidth(t)+1) && clipH <= (this.pageData.getMediaBoxHeight(t)+1)){
                    paperClipW = this.pageData.getMediaBoxWidth(t)+1;
                    paperClipH = this.pageData.getMediaBoxHeight(t)+1;
                }
            }

            //System.err.println("-> (w/h) " + clipW + " " + clipH);
            customPaper.setSize(paperClipW, paperClipH);
            customPaper.setImageableArea(0, 0, clipW, clipH);

            //customPaper.setSize(595, 842);
            //customPaper.setImageableArea(0, 0, clipW, clipH);

        }

        //System.err.println("WIdth dims: " + customPaper.getWidth() + " " + customPaper.getHeight() + "\n");
        pf.setPaper(customPaper);

        //System.out.println("customPaper="+customPaper.getWidth()+" "+customPaper.getHeight());

    }
    // <end-me>



    /**
     * generate BufferedImage of a page in current file
     *
     * Page size is defined by CropBox
     */
    public BufferedImage getPageAsImage(int pageIndex) throws PdfException {
        return getPageAsImage(pageIndex, false);
    }




    /**
     * generate BufferedImage of a page in current file
     */
    private BufferedImage getPageAsImage(int pageIndex,
                                         boolean imageIsTransparent) throws PdfException {

        Graphics2D g2 ;

        BufferedImage image = null;


        // make sure in range
        if ((pageIndex > pageCount) | (pageIndex < 1)) {
        	if(LogWriter.isOutput())
        		LogWriter.writeLog("Page " + pageIndex + " not in range");
        } else {

            try{
                isGeneratingPage = true;
                //waitForDecodingToFinish();

                //reset timeout flags
                timeout=false;

                /**
                 * setup for decoding page
                 */

                /** get pdf object id for page to decode */
                String currentPageOffset = (String) pagesReferences.get(new Integer(pageIndex));

                if (currentPageOffset != null) {

                    if (currentPdfFile == null)
                        throw new PdfException(
                                "File not open - did you call closePdfFile() inside a loop and not reopen");

                    /** read page or next pages */
                    PdfObject pdfObject=new PageObject(currentPageOffset);
                    currentPdfFile.readObject(pdfObject);
                    PdfObject Resources=pdfObject.getDictionary(PdfDictionary.Resources);

                    ObjectStore localStore = new ObjectStore();
                    DynamicVectorRenderer imageDisplay = new ImageDisplay(pageNumber,false, 5000, localStore);

                    currentImageDecoder = new PdfStreamDecoder(currentPdfFile, Resources);
                    currentImageDecoder.setParameters(true, renderPage, renderMode,0);

                    currentImageDecoder.setObjectValue(ValueTypes.ImageHandler, customImageHandler);


                    currentImageDecoder.setObjectValue(ValueTypes.Name, filename);
                    currentImageDecoder.setObjectValue(ValueTypes.ObjectStore,localStore);
                    currentImageDecoder.setFloatValue(BaseDecoder.Multiplier, multiplyer);
                    currentImageDecoder.setObjectValue(ValueTypes.PDFData,pageData);
                    currentImageDecoder.setIntValue(ValueTypes.PageNum, pageIndex);

                    currentImageDecoder.setObjectValue(ValueTypes.DynamicVectorRenderer,imageDisplay);

                    if (globalResources != null){
                        currentPdfFile.checkResolved(globalResources);
                        currentImageDecoder.readResources(globalResources,true);

                        PdfObject propObj=globalResources.getDictionary(PdfDictionary.Properties);
                        if(propObj!=null)
                            PropertiesObj=propObj;
                    }

                    /**read the resources for the page*/
                    if (Resources != null) {
                        currentPdfFile.checkResolved(Resources);
                        currentImageDecoder.readResources(Resources,true);

                        PdfObject propObj=Resources.getDictionary(PdfDictionary.Properties);
                        if(propObj!=null)
                            PropertiesObj=propObj;
                    }

                    setupPage(currentImageDecoder, true);

                    //can for max
                    if(multiplyer==-2){

                        multiplyer=-1;
                        currentImageDecoder.setFloatValue(BaseDecoder.Multiplier, multiplyer);

                        PdfStreamDecoderForSampling currentImageDecoder2 = new PdfStreamDecoderForSampling(currentPdfFile, Resources);
                        currentImageDecoder2.setParameters(true, renderPage, renderMode,0);

                        currentImageDecoder2.setObjectValue(ValueTypes.Name, filename);
                        currentImageDecoder2.setObjectValue(ValueTypes.ObjectStore,localStore);
                        currentImageDecoder2.setFloatValue(BaseDecoder.Multiplier, multiplyer);
                        currentImageDecoder2.setObjectValue(ValueTypes.PDFData,pageData);
                        currentImageDecoder2.setIntValue(ValueTypes.PageNum, pageIndex);
                        currentImageDecoder2.setObjectValue(ValueTypes.DynamicVectorRenderer,imageDisplay);

                        if (globalResources != null)
                            currentImageDecoder2.readResources(globalResources,true);

                        //read the resources for the page
                        if (Resources != null)
                            currentImageDecoder2.readResources(Resources,true);

                        setupPage(currentImageDecoder2, true);

                        /** bare minimum to get value*/
                        multiplyer=currentImageDecoder2.decodePageContentForImageSampling(pdfObject);

                        int bestQualityMaxScalingToUse = 0;
                        if(instance_bestQualityMaxScaling != null)
                            bestQualityMaxScalingToUse = instance_bestQualityMaxScaling.intValue();
                        else if (bestQualityMaxScaling != null)
                            bestQualityMaxScalingToUse = bestQualityMaxScaling.intValue();

                        if (bestQualityMaxScalingToUse > 0 && multiplyer > bestQualityMaxScalingToUse) {
                            multiplyer = bestQualityMaxScalingToUse;

                            if (debugHiRes)
                                System.out.println("Use max");
                        }

                        currentImageDecoder2.setFloatValue(BaseDecoder.Multiplier, multiplyer);
                        currentImageDecoder.setFloatValue(BaseDecoder.Multiplier, multiplyer);
                    }

                    if(!allowPagesSmallerThanPageSize.booleanValue() &&
                            !instance_allowPagesSmallerThanPageSize.booleanValue() &&
                            multiplyer<1 && multiplyer>0)
                        multiplyer=1;

                    //allow for value not set
                    if(multiplyer==-1)
                        multiplyer=1;

                    /**
                     * setup transformations and image
                     */


                    AffineTransform imageScaling = setPageParametersForImage(scaling*multiplyer, pageIndex);

                    //include scaling in size
                    int mediaW = (int) (scaling*pageData.getMediaBoxWidth(pageIndex));
                    int mediaH = (int) (scaling*pageData.getMediaBoxHeight(pageIndex));
                    int rotation = pageData.getRotation(pageIndex);

                    int crw = (int) (scaling*pageData.getCropBoxWidth(pageIndex));
                    int crh = (int) (scaling*pageData.getCropBoxHeight(pageIndex));
                    int crx = (int) (scaling*pageData.getCropBoxX(pageIndex));
                    int cry = (int) (scaling*pageData.getCropBoxY(pageIndex));

                    boolean rotated = false;
                    int w, h;
                    if ((rotation == 90) || (rotation == 270)) {
                        h = (int) (crw*multiplyer); // * scaling);
                        w = (int) (crh*multiplyer); // * scaling);
                        rotated = true;
                    } else {
                        w = (int) (crw*multiplyer); // * scaling);
                        h = (int) (crh*multiplyer); // * scaling);
                    }

                    image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

                    Graphics graphics = image.getGraphics();

                    g2 = (Graphics2D) graphics;

                    if (!imageIsTransparent) {
                        g2.setColor(Color.white);
                        g2.fillRect(0, 0, w, h);
                    }

                    /**
                     * adjustment for upside down images
                     */
                    if(rotation==180){
                    	g2.translate(crx*2*multiplyer, -(cry*2*multiplyer));
                    }

                    /**
                     * pass in values as needed for patterns
                     * @Mariusz - marks change below to fix scaling
                     */
                    ((DynamicVectorRenderer)currentImageDecoder.getObjectValue(ValueTypes.DynamicVectorRenderer)).setScalingValues(crx*multiplyer, (crh*multiplyer) + cry, multiplyer*scaling);



                    g2.setRenderingHints(ColorSpaces.hints);
                    g2.transform(imageScaling);

                    if (rotated){

//					System.out.println("rotation="+rotation+" crx="+crx+" cry="+cry+" crw="+crw+" crh="+crh+" w="+w+" h="+h);
//					System.out.println("MediaW="+w+" MediaH="+h);

                        if(rotation==90){//90

                            if(multiplyer<1){
                                cry = (int)(imageScaling.getTranslateX() + cry);
                                crx = (int)(imageScaling.getTranslateY() + crx);

                            }else{
                                cry = (int)((imageScaling.getTranslateX()/multiplyer) + cry);
                                crx = (int)((imageScaling.getTranslateY()/multiplyer) + crx);
                            }
                            g2.translate(-crx, -cry);

                        }else{ //270
                            if(cry<0)
                                g2.translate(-crx, mediaH-crh+cry);
                            else
                                g2.translate(-crx,mediaH-crh-cry);
                        }
                    }

                    /** decode and print in 1 go */
                    currentImageDecoder.setObjectValue(ValueTypes.DirectRendering, g2);//(Graphics2D) graphics);
                    imageDisplay.setG2(g2);
                    if (pdfObject != null)
                        currentImageDecoder.decodePageContent(pdfObject);

                    g2.setClip(null);

                    colorSpacesUsed= (Iterator) currentImageDecoder.getObjectValue(PageInfo.COLORSPACES);

                    hasYCCKimages = currentImageDecoder.getBooleanValue(DecodeStatus.YCCKImages);
                    imagesProcessedFully = currentImageDecoder.getBooleanValue(DecodeStatus.ImagesProcessed);
                    hasNonEmbeddedCIDFonts= currentImageDecoder.getBooleanValue(DecodeStatus.NonEmbeddedCIDFonts);
                    nonEmbeddedCIDFonts= (String) currentImageDecoder.getObjectValue(DecodeStatus.NonEmbeddedCIDFonts);
                    ttHintingRequired= currentImageDecoder.getBooleanValue(DecodeStatus.TTHintingRequired);
                    timeout= currentImageDecoder.getBooleanValue(DecodeStatus.Timeout);

                    /**
                     * draw acroform data onto Panel
                     */
                    if (formRenderer != null && formRenderer.hasFormsOnPage(pageIndex) && !formRenderer.ignoreForms()) {

                        //disable color list if forms
                        colorSpacesUsed=null;

                        /** make sure they exist */
                        //formRenderer.getCompData().setPageValues((float) scaling, 0,0);

                        formRenderer.createDisplayComponentsForPage(pageIndex);

                        formRenderer.getCompData().renderFormsOntoG2(g2,pageIndex, scaling, 0, this.displayRotation,
                                null, null,currentPdfFile, pageData.getMediaBoxHeight(pageIndex));

                        formRenderer.getCompData().resetScaledLocation(oldScaling, displayRotation, 0);

                    }




                    localStore.flush();
                }
//			}


                //workaround for bug in AIX
                if (!isRunningOnAIX && !converToRGB && !imageIsTransparent && image != null)
                    image = ColorSpaceConvertor.convertToRGB(image);

            }finally{
                isGeneratingPage = false;
            }
        }

        return image;

    }

    //<start-wrap>
    /**
     * return scaleup factor applied to last Hires image of page generated
     *
     * negative values mean no upscaling applied and should be ignored
     */
    public float getHiResUpscaleFactor(){

        return multiplyer;
    }

    //<end-wrap>

    /**
     * provide method for outside class to clear store of objects once written
     * out to reclaim memory
     *
     * @param reinit lag to show if image data flushed as well
     */
    final public void flushObjectValues(boolean reinit) {

        if (pdfData != null)
            pdfData.flushTextList(reinit);

        if ((pdfImages != null) && (reinit))
            pdfImages.clearImageData();

    }


    //<start-adobe>

    /**
     * provide method for outside class to get data object
     * containing images
     *
     * @return PdfImageData containing image metadata
     */
    final public PdfImageData getPdfImageData() {
        return pdfImages;
    }

    /**
     * provide method for outside class to get data object
     * containing images.
     *
     * @return PdfImageData containing image metadata
     */
    final public PdfImageData getPdfBackgroundImageData() {
        return pdfBackgroundImages;
    }

    //<end-adobe>

    /**
     * set render mode to state what is displayed onscreen (ie
     * RENDERTEXT,RENDERIMAGES) - only generally required if you do not wish to
     * show all objects on screen (default is all). Add values together to
     * combine settings.
     */
    final public void setRenderMode(int mode) {

        renderMode = mode;

        extractionMode = mode;

    }

    /**
     * set extraction mode telling JPedal what to extract -
     * (TEXT,RAWIMAGES,FINALIMAGES - add together to combine) - See
     * org.jpedal.examples for specific extraction examples
     */
    final public void setExtractionMode(int mode) {

        extractionMode = mode;

    }



    /**
     * read object and setup Annotations for multipage view
     */
    protected void readObjectForPage(PdfObject pdfObject, String currentPageOffset, int page, boolean redraw) {

        /** read page or next pages */
        currentPdfFile.readObject(pdfObject);

        /**
         * draw acroform data onto Panel
         */
        if (renderPage) {

            if (formRenderer != null && acroFormObj != null) {
                formRenderer.getCompData().setPageValues(scaling, displayRotation,0,0,0,Display.SINGLE_PAGE,currentOffset.widestPageNR,currentOffset.widestPageR);
                formRenderer.createDisplayComponentsForPage(page);
            }

            //force redraw
            if (redraw) {
                lastFormPage = -1;
                lastEnd = -1;
                lastStart = -1;
            }

            //	this.validate();
        }

    }


    /**
     * method to return null or object giving access info fields and metadata.
     */
    final public PdfFileInformation getFileInformationData() {

        if (currentPdfFile != null){
            /**Information object holds information from file*/
            PdfFileInformation currentFileInformation = new PdfFileInformation();

            return currentFileInformation.readPdfFileMetadata(metadataObj,currentPdfFile);
        }else
            return null;

    }



    /**
     *
     * Please do not use for general usage. Use setPageParameters(scalingValue, pageNumber) instead;
     */

    final public void setExtractionMode(int mode, int imageDpi, float scaling) {

        if (dpi % 72 != 0 && LogWriter.isOutput())
            LogWriter.writeLog("Dpi is not a factor of 72- this may cause problems");

        dpi = imageDpi;

        //if (scaling < .5)
        //	scaling = .5f;

        this.scaling = scaling;

        pageData.setScalingValue(scaling); //ensure aligned

        extractionMode = mode;

        if(layers!=null){
            boolean layersChanged=layers.setZoom(scaling);

            if(layersChanged){
                try {
                    decodePage(-1);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * return handle on PDFFactory which adjusts display size so matches size in Acrobat
     * @return
     */
    public DPIFactory getDPIFactory(){
        return scalingdpi;
    }


    /**
     * initialise panel and set size to fit PDF page<br>
     * intializes display with rotation set to the default, specified in the PDF document
     * scaling value of -1 means keep existing setting
     */
    final public void setPageParameters(float scaling, int pageNumber) {

        this.pageNumber = pageNumber;

        //<start-me>
        //pick up flag to prevent loop
        if (displayView==Display.PAGEFLOW3D && scaling==-100f)
            return;
        //<end-me>


        //ignore negative value or set
        if(scaling>0)
            this.scaling=scaling;
        else
            scaling=this.scaling;


        if(pages!=null)
            pages.setScaling(scaling);


        if(layers!=null){
            boolean layersChanged=layers.setZoom(scalingdpi.removeScaling(scaling));

            if(layersChanged){
                try {
                    decodePage(-1);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        pageData.setScalingValue(scaling); //ensure aligned

        int mediaW = pageData.getMediaBoxWidth(pageNumber);
        max_y = pageData.getMediaBoxHeight(pageNumber);
        max_x = pageData.getMediaBoxWidth(pageNumber);
        //int mediaX = pageData.getMediaBoxX(pageNumber);
        //int mediaY = pageData.getMediaBoxY(pageNumber);

        int cropW = pageData.getCropBoxWidth(pageNumber);
        int cropH = pageData.getCropBoxHeight(pageNumber);
        //int cropX = pageData.getCropBoxX(pageNumber);
        //int cropY = pageData.getCropBoxY(pageNumber);

        x_size_cropped = (int)(cropW*scaling);
        y_size_cropped = (int)(cropH*scaling);

        this.x_size =(int) ((cropW)*scaling);
        this.y_size =(int) ((cropH)*scaling);

        //@kieran - rotation is broken in viewer without this - you can't alter it
        //can anyone remember why we added this code???
        //it breaks PDFs if the rotation changes between pages
        if(!isNewRotationSet
                //<start-me>
                && displayView!=Display.PAGEFLOW3D
            //<end-me>
                ){
            displayRotation = pageData.getRotation(pageNumber);
        }else{
            isNewRotationSet=false;
        }

        currentDisplay.init(mediaW,max_y,displayRotation,pageColor);


        /**update the AffineTransform using the current rotation*/
        setPageRotation(displayRotation);

    }

    /**
     * initialise panel to display during updates<br>
     * pageScaling of 1 is 100%
     * @param pageNumber
     */
    final protected AffineTransform setPageParametersForImage(float scaling, int pageNumber) {

        if(layers!=null){
            boolean layersChanged=layers.setZoom(scaling);

            if(layersChanged){
                try {
                    decodePage(-1);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        //create scaling factor to use
        AffineTransform imageScaling = new AffineTransform();

        //int mediaW = pageData.getMediaBoxWidth(pageNumber);
        //int mediaH = pageData.getMediaBoxHeight(pageNumber);
        //int mediaX = pageData.getMediaBoxX(pageNumber);
        //int mediaY = pageData.getMediaBoxY(pageNumber);

        int crw = pageData.getCropBoxWidth(pageNumber);
        int crh = pageData.getCropBoxHeight(pageNumber);
        int crx = pageData.getCropBoxX(pageNumber);
        int cry = pageData.getCropBoxY(pageNumber);
        /**allow for rotation*/
        //breaks double.pdf extraction and probably legacy code
//		if((displayRotation==90)||(displayRotation==270)){
//		int tmp=crw;
//		crw=crh;
//		crh=tmp;
//		}

        int image_x_size =(int) ((crw)*scaling);
        int image_y_size =(int) ((crh)*scaling);

        int raw_rotation = pageData.getRotation(pageNumber);

        imageScaling.translate(-crx*scaling,+cry*scaling);

        if (raw_rotation == 270) {

            imageScaling.rotate(-Math.PI / 2.0, image_x_size/ 2, image_y_size / 2);

            double x_change = (imageScaling.getTranslateX());
            double y_change = (imageScaling.getTranslateY());
            imageScaling.translate((image_y_size - y_change), -x_change);

            // imageScaling.translate((image_y_size - y_change)-crx, 0);//(image_x_size - x_change));
            //System.out.println("x_change="+x_change+" y_change="+y_change);
            //System.out.println("image_x_size="+image_x_size+" image_y_size="+image_y_size);
            //System.out.println("current>>"+" "+(image_y_size - y_change)+" "+(image_x_size-x_change));
            // System.out.println("dimensions="+crx+" "+cry+" "+crw+" "+crh+" scaling="+scaling);
            // imageScaling.translate(crx-x_change,(y_change/2)-cry);
            //imageScaling.translate(crx+crx-x_change,((image_y_size - y_change)+crh)-image_x_size);
            //imageScaling.translate(0,230);

//			System.out.println("mediaBox="+pageData.getMediaBoxX(pageNumber)+" "+pageData.getMediaBoxY(pageNumber)+" "+pageData.getMediaBoxWidth(pageNumber)+" "+pageData.getMediaBoxHeight(pageNumber));
//			System.out.println(pageData.getCropBoxWidth(pageNumber)-pageData.getMediaBoxWidth(pageNumber));

//			System.out.println(pageData.getCropBoxHeight(pageNumber)-pageData.getMediaBoxHeight(pageNumber));
            imageScaling.translate(2*cry*scaling,0);
            imageScaling.translate(0,-scaling*(pageData.getCropBoxHeight(pageNumber)-pageData.getMediaBoxHeight(pageNumber)));
            //imageScaling.translate(2*cry,crx+pageData.getMediaBoxHeight(pageNumber)-pageData.getCropBoxHeight(pageNumber));
        } else if (raw_rotation == 180) {

            imageScaling.rotate(Math.PI, image_x_size / 2, image_y_size / 2);

        } else if (raw_rotation == 90) {

            imageScaling.rotate(Math.PI / 2.0,  image_x_size / 2,  image_y_size / 2);

            double x_change =(imageScaling.getTranslateX());
            double y_change = (imageScaling.getTranslateY());
            imageScaling.translate(-y_change, image_x_size - x_change);

        }

        if(scaling<1){
            imageScaling.translate(image_x_size, image_y_size);
            imageScaling.scale(1, -1);
            imageScaling.translate(-image_x_size, 0);

            imageScaling.scale(scaling,scaling);
        }else{
            imageScaling.translate(image_x_size, image_y_size);
            imageScaling.scale(1, -1);
            imageScaling.translate(-image_x_size, 0);

            imageScaling.scale(scaling,scaling);
        }

        //System.out.println("scale="+pageScale+" "+print_x_size+" "+print_y_size);

        return imageScaling;
    }

    /** calls setPageParameters(scaling,pageNumber) after setting rotation to draw page */
    final public void setPageParameters(float scaling, int pageNumber,int newRotation) {

        isNewRotationSet=true;
        displayRotation=newRotation;
        //<start-me>
        if (displayView == Display.PAGEFLOW3D)
            pages.init(0,0,displayRotation,0,null,false,null,0,0);
        else
            //<end-me>
            setPageParameters(scaling, pageNumber);
    }

    //<start-adobe>




    /**
     * just extract annotations for a page - if you want to decode the page and
     * extract the annotations use decodePage(int pageNumber) which does both.
     * <p/>
     * Now returns PdfAnnots object
     *
     *
     *
     *
     final synchronized public PdfAnnots decodePageForAnnotations(int i) {

     PdfAnnots annotsData=null;

     if (isDecoding) {
     LogWriter.writeLog("[PDF]WARNING - this file is being decoded already - use  waitForDecodingToFinish() to check");
     //isDecoding = false;

     return null;

     } else {

     try{
     isDecoding=true;

     //
     annotsData = null;

     //
     if (i > pageCount) {

     LogWriter.writeLog("Page out of bounds");

     } else {

     //
     String currentPageOffset = (String) pagesReferences.get(new Integer(i));

     //
     if (currentPageOffset != null) {

     //
     PdfObject pdfObject=new PageObject(currentPageOffset);
     currentPdfFile.readObject(pdfObject);
     byte[][] annotList = pdfObject.getKeyArray(PdfDictionary.Annots);

     if (annotList != null) {
     annotsData = new PdfAnnots(currentPdfFile,-1);
     annotsData.readAnnots(annotList);
     }
     }
     }

     }catch(Exception e){
     e.printStackTrace();
     }finally {
     isDecoding=false;
     }
     return annotsData;
     }
     }/**/
    //<end-adobe>

    /**
     * get pdf as Image of any page scaling is size (100 = full size)
     * Use getPageAsImage to create images
     *
     *
     *
     final public BufferedImage getPageAsThumbnail(int pageNumber, int h) {

     BufferedImage image;
     int mediaX, mediaY, mediaW, mediaH;

     // the actual display object
     DynamicVectorRenderer imageDisplay = new DynamicVectorRenderer(pageNumber, true,
     1000, this.objectStoreRef); //
     imageDisplay.setHiResImageForDisplayMode(useHiResImageForDisplay);
     // simageDisplay.setDirectRendering((Graphics2D) graphics);

     try {

     // check in range
     if (pageNumber > pageCount) {

     LogWriter.writeLog("Page " + pageNumber + " out of bounds");

     } else {

     // resolve page size
     mediaX = pageData.getMediaBoxX(pageNumber);
     mediaY = pageData.getMediaBoxY(pageNumber);
     mediaW = pageData.getMediaBoxWidth(pageNumber);
     mediaH = pageData.getMediaBoxHeight(pageNumber);

     // get pdf object id for page to decode
     String currentPageOffset = (String) pagesReferences.get(new Integer(pageNumber));

     // decode the file if not already decoded, there is a valid
     //object id and it is unencrypted

     if ((currentPageOffset != null)) {

     //@speed
     // read page or next pages
     PdfObject pdfObject=new PageObject(currentPageOffset);
     currentPdfFile.readObject(pdfObject);
     PdfObject Resources=pdfObject.getDictionary(PdfDictionary.Resources);

     // get information for the page
     if (pdfObject != null) {

     if(layers!=null)
     layers.setScaling(scaling);
     PdfStreamDecoder imageDecoder = new PdfStreamDecoder(useHiResImageForDisplay,layers, Resources);
     imageDecoder.setExternalImageRender(customImageHandler);


     imageDecoder.setName(filename);
     imageDecoder.setStore(objectStoreRef);

     imageDecoder.init(true, true, renderMode, 0, pageData,
     pageNumber, imageDisplay, currentPdfFile, this);

     if (globalResources != null){
     currentPdfFile.checkResolved(globalResources);
     imageDecoder.readResources(globalResources,true);

     PdfObject propObj=globalResources.getDictionary(PdfDictionary.Properties);
     if(propObj!=null)
     PropertiesObj=propObj;
     }

     //read the resources for the page
     if (Resources != null){
     currentPdfFile.checkResolved(Resources);
     imageDecoder.readResources(Resources,true);

     PdfObject propObj=Resources.getDictionary(PdfDictionary.Properties);
     if(propObj!=null)
     PropertiesObj=propObj;
     }

     setupPage(imageDecoder, false);

     int rotation = pageData.getRotation(pageNumber);
     imageDisplay.init(mediaW, mediaH, rotation);
     imageDecoder.decodePageContent(pdfObject, mediaX,mediaY, null, null);

     }
     }
     }
     } catch (Exception e) {
     e.printStackTrace();

     }

     // workout scaling and get image

     image = getImageFromRenderer(h, imageDisplay, pageNumber);

     return image;

     }   /**/

    //<start-adobe>
    /**
     * set status bar to use when decoding a page - StatusBar provides a GUI
     * object to display progress and messages.
     */
    public void setStatusBarObject(StatusBar statusBar) {
        this.statusBar = statusBar;
    }

    //<end-adobe>

    /**
     * wait for decoding to finish
     */
    public void waitForDecodingToFinish() {

        //wait to die
        while (isDecoding) {
            // System.out.println("Waiting to die");
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                // should never be called
                e.printStackTrace();

                //ensure will exit loop
                isDecoding=false;
            }
        }
    }

    /**
     * wait for decoding forms to finish
     *
     private void waitForDecodingFormsToFinish() {

     //wait to die
     while (formsDecoding) {
     // System.out.println("Waiting to die");
     try {
     Thread.sleep(100);
     } catch (InterruptedException e) {
     // should never be called
     e.printStackTrace();

     //ensure will exit loop
     formsDecoding=false;
     } finally{
     formsDecoding=false;
     }
     }
     }  /**/


    /**
     * ask JPedal to stop printing a page
     */
    final public void stopPrinting() {
        stopPrinting = true;
    }

    //<start-adobe>

    /**
     * gets DynamicVector Object
     */
    public DynamicVectorRenderer getDynamicRenderer() {
        return currentDisplay;
    }

    /**
     * gets DynamicVector Object - NOT PART OF API and subject to change (DO NOT USE)
     */
    public DynamicVectorRenderer getDynamicRenderer(boolean reset) {

        DynamicVectorRenderer latestVersion=currentDisplay;

        if(reset)
            currentDisplay=new ScreenDisplay(0,objectStoreRef,false);

        return latestVersion;
    }

    /**
     * extract marked content - not yet live
     */
    final synchronized public void decodePageForMarkedContent(int pageNumber, PdfObject pdfObject, Object pageStream) throws Exception {


        //boolean hasTree=true;

        if (isDecoding) {
        	
        	if(LogWriter.isOutput())
        		LogWriter.writeLog("[PDF]WARNING - this file is being decoded already - use  waitForDecodingToFinish() to check");
            //isDecoding = false;

        } else {

            //if no tree use page
            if(pdfObject==null){
                String currentPageOffset = (String) pagesReferences.get(new Integer(pageNumber));


                pdfObject=new PageObject(currentPageOffset);
                currentPdfFile.readObject(pdfObject);

                //hasTree=false;
            }else{

                pageNumber=pageLookup.convertObjectToPageNumber(new String(pdfObject.getUnresolvedData()));

                //get Page data in Map
                currentPdfFile.checkResolved(pdfObject);

            }

            try{
                isDecoding=true;

                PdfObject Resources=pdfObject.getDictionary(PdfDictionary.Resources);

                /** read page or next pages */
                if (pdfObject != null) {

                    PdfStreamDecoder current = new PdfStreamDecoder(currentPdfFile, useHiResImageForDisplay,layers, Resources);
                    current.setParameters(true, false, renderMode,PdfDecoder.TEXT + PdfDecoder.RAWIMAGES + PdfDecoder.FINALIMAGES);

                    current.setObjectValue(ValueTypes.Name, filename);
                    current.setObjectValue(ValueTypes.ObjectStore,objectStoreRef);
                    current.setObjectValue(ValueTypes.StatusBar, statusBar);
                    current.setObjectValue(ValueTypes.PDFData,pageData);
                    current.setIntValue(ValueTypes.PageNum, pageNumber);

                    if (globalResources != null){
                        currentPdfFile.checkResolved(globalResources);
                        current.readResources(globalResources,true);

                        PdfObject propObj=globalResources.getDictionary(PdfDictionary.Properties);
                        if(propObj!=null)
                            PropertiesObj=propObj;
                    }

                    /**read the resources for the page*/
                    if (Resources != null){
                        currentPdfFile.checkResolved(Resources);
                        current.readResources(Resources,true);

                        PdfObject propObj=Resources.getDictionary(PdfDictionary.Properties);
                        if(propObj!=null)
                            PropertiesObj=propObj;
                    }

                    setupPage(current, false);

                    // <start-me>
                    current.setObjectValue(ValueTypes.MarkedContent,pageStream);
                    // <end-me>

                    /*
                  * If highlights are required for page, reset highlights
                  */
                    this.setLineAreas(null);

                    current.decodePageContent(pdfObject);

                    //All data loaded so now get all line areas for page
                    Vector_Rectangle vr = (Vector_Rectangle) current.getObjectValue(ValueTypes.TextAreas);
                    vr.trim();
                    Rectangle[] pageTextAreas = vr.get();

                    Vector_Int vi = (Vector_Int) current.getObjectValue(ValueTypes.TextDirections);
                    vi.trim();
                    int[] pageTextDirections = vi.get();

                    for(int k=0; k!=pageTextAreas.length; k++){
                        this.addToLineAreas(pageTextAreas[k], pageTextDirections[k], pageNumber);
                    }


                    //pdfImages = current.getImages();

//                    Vector_Rectangle vr = current.getTextAreas();
//                    vr.trim();
//                    Rectangle[] pageTextAreas = vr.get();
//
//                    Vector_Int vi = current.getTextDirections();
//                    vi.trim();
//                    int[] pageTextDirections = vi.get();
//
//                    for(int k=0; k!=pageTextAreas.length; k++){
//                    	this.addToLineAreas(pageTextAreas[k], pageTextDirections[k], pageNumber);
//                    }
                }
            }catch(Exception e){
                e.printStackTrace();
            }finally {
                isDecoding=false;
            }
        }

    }

    /**
     * used to decode multiple pages on views
     */
    public final void decodeOtherPages(int pageCount) {
        pages.decodeOtherPages(pageNumber, pageCount);
    }
    //<end-adobe>

    /**
     * Creates the form components for a specified page
     * @param page The page to create form objects for
     */
    public void setupForms(int page) {
        String ref = (String) pagesReferences.get(new Integer(page));
        PdfObject pdfObject = new PageObject(ref);
        createFormComponents(page, false, pdfObject);
    }

    /**
     * decode a page, - <b>page</b> must be between 1 and
     * <b>PdfDecoder.getPageCount()</b> - Will kill off if already running
     *
     * returns minus page if trying to open linearized page not yet available
     */
    final public void decodePage(int rawPage) throws Exception {

	if(customDVR!=null)
	    currentDisplay= customDVR;

        //wait to die
        /**while (isGeneratingPage) {
         // System.out.println("Waiting to die");
         try {
         Thread.sleep(100);
         } catch (InterruptedException e) {
         // should never be called
         e.printStackTrace();

         //ensure will exit loop
         isGeneratingPage=false;
         }
         }  /**/

        //show if we close file at end or after JavaScript
        boolean flagClosedAtEnd=true;

        boolean isForm=false;

        //flag to allow us to not do some things when we re decode the page with layers on for example
        boolean isDuplicate = false;
        if(rawPage==-1){
            rawPage=lastPageDecoded;
            isDuplicate = true;
        }

        boolean isSamePage = false;
        if(rawPage==lastPageDecoded)
            isSamePage = true;

        final int page=rawPage;

        final boolean debugRace=false;

        if (isDecoding) {
        	if(LogWriter.isOutput())
        		LogWriter.writeLog("[PDF]WARNING - this file is being decoded already - use  waitForDecodingToFinish() to check");
            //isDecoding = false;

        } else {

            boolean isPageAvailable=isPageAvailable(rawPage);
            PdfObject pdfObject=linObject;

//        if(pdfObject==null)
//            System.out.println("decode page="+rawPage+" "+pdfObject+" "+isPageAvailable);
//        else
//            System.out.println("decode page="+rawPage+" "+pdfObject+" "+pdfObject.getObjectRefAsString()+" "+isPageAvailable);

            /**
             * if linearized and PdfObect then setup
             */
            if(isPageAvailable && pdfObject!=null){
                isDecoding=true;
                tempPageCount=rawPage;
                readAllPageReferences(true,  pdfObject,new HashMap(), new HashMap());
            }


            if(!isPageAvailable){
                // System.out.println("cannot yet fully resolve page "+rawPage);
                isDecoding=false;
                return;
            }

            //reset timeout flags
            timeout=false;

            try{
                isDecoding = true;

                //wait to die
                //checkImagesFinished();

                if(layers!=null && layers.getChangesMade()){


                    lastPageDecoded=-1;
                    layers.setChangesMade(false);//set flag to say we have decoded the changes

                    //refresh forms in case any effected by layer change
                    formRenderer.getCompData().setForceRedraw(true);
                    formRenderer.getCompData().setLayerData(layers);
                    formRenderer.getCompData().resetScaledLocation(scaling,displayRotation,(int)indent);//indent here does nothing.

                }

                if (this.displayView != Display.SINGLE_PAGE){
                    isDecoding=false;
                    return;
                }



                lastPageDecoded = page;

                decodeStatus = "";



                cursorBoxOnScreen = null;

                if (!isSamePage && renderPage && formRenderer != null){

                    formRenderer.removeDisplayComponentsFromScreen();
                    lastFormPage=-1; //reset so will appear on reparse
                }

                //invalidate();
                /** flush renderer */
                currentDisplay.flush();
                pages.refreshDisplay();

                if(debugRace)
                    System.out.println("8");

                /** check in range */
                if (page > pageCount || page < 1) {

                	if(LogWriter.isOutput())
                		LogWriter.writeLog("Page out of bounds");

                    isDecoding=false;

                } else{

                    if(debugRace)
                        System.out.println("9");


                    //<start-adobe>
                    /**
                     * title changes to give user something to see under timer
                     * control
                     */
//                long time=0;
                    Timer t = null;
                    if (statusBar != null) {
//                        System.out.println("statusBar != null");
                        ActionListener listener = new ProgressListener();
                        t = new Timer(150, listener);
                        t.start(); // start it
//                        time = System.currentTimeMillis();
                    }

                    //<end-adobe>

//                    System.out.println("T running: " + t.isRunning());
//                    Thread.sleep(200);

                    this.pageNumber = page;
                    String currentPageOffset = (String) pagesReferences.get(new Integer(page));

                    /**
                     * decode the file if not already decoded, there is a valid
                     * object id and it is unencrypted
                     */
                    if (currentPageOffset != null && currentPdfFile == null)
                        throw new PdfException("File not open - did you call closePdfFile() inside a loop and not reopen");

                    //get Page data in Map

                    /** get pdf object id for page to decode */

                    if(debugRace)
                        System.out.println("10");

                    if(pdfObject==null){
                        pdfObject=new PageObject(currentPageOffset);
                        currentPdfFile.readObject(pdfObject);
                    }

                    PdfObject Resources=pdfObject.getDictionary(PdfDictionary.Resources);

                    /** read page or next pages */
                    if (pdfObject != null) {



                        byte[][] pageContents= pdfObject.getKeyArray(PdfDictionary.Contents);

                        if(debugRace)
                            System.out.println("11");

                        //<start-adobe>
                        /** flush annotations */
//                        if (displayHotspots != null && (specialMode==SpecialOptions.NONE || specialMode==SpecialOptions.SINGLE_PAGE))
//                            displayHotspots.flushAnnotationsDisplayed();
                        //<end-adobe>

                        //if it has no content but it still has a name obj then we need to still do all this.
                        if (pageContents != null || nameObj!=null) {

                            /** set hires mode or not for display */
                            currentDisplay.setHiResImageForDisplayMode(useHiResImageForDisplay);
                            currentDisplay.setPrintPage(page);
                            currentDisplay.setCustomColorHandler(customColorHandler);

                            current = new PdfStreamDecoder(currentPdfFile, useHiResImageForDisplay,layers, Resources);
                            current.setParameters(true, renderPage, renderMode, extractionMode);

                            current.setObjectValue(ValueTypes.ImageHandler, customImageHandler);



                            current.setObjectValue(ValueTypes.Name, filename);
                            current.setIntValue(ValueTypes.PageNum, page);
                            current.setObjectValue(ValueTypes.ObjectStore,objectStoreRef);
                            current.setObjectValue(ValueTypes.StatusBar, statusBar);
                            current.setObjectValue(ValueTypes.PDFData,pageData);
                            current.setObjectValue(ValueTypes.DynamicVectorRenderer,currentDisplay);

                            if(debugRace)
                                System.out.println("12 current="+current);

                            if (globalResources != null){
                                currentPdfFile.checkResolved(globalResources);
                                current.readResources(globalResources,true);

                                PdfObject propObj=globalResources.getDictionary(PdfDictionary.Properties);
                                if(propObj!=null)
                                    PropertiesObj=propObj;
                            }

                            if(debugRace)
                                System.out.println("12a");

                            /**read the resources for the page*/
                            if (Resources != null){
                                currentPdfFile.checkResolved(Resources);
                                current.readResources(Resources,true);

                                PdfObject propObj=Resources.getDictionary(PdfDictionary.Properties);
                                if(propObj!=null)
                                    PropertiesObj=propObj;
                            }

                            if(debugRace)
                                System.out.println("12b");

                            setupPage(current, false);



                            if(debugRace)
                                System.out.println("13");

                            /** pass in visual multithreaded status bar */
                            current.setObjectValue(ValueTypes.StatusBar, statusBar);

                            int mediaW = pageData.getMediaBoxWidth(pageNumber);
                            int mediaH = pageData.getMediaBoxHeight(pageNumber);
                            //int mediaX = pageData.getMediaBoxX(pageNumber);
                            //int mediaY = pageData.getMediaBoxY(pageNumber);
                            int rotation = pageData.getRotation(pageNumber);
                            currentDisplay.init(mediaW, mediaH, rotation,pageColor);
                            /** toke out -min's%% */

                            if(debugRace)
                                System.out.println("13.1");

                            if (g2 != null)
                                current.setObjectValue(ValueTypes.DirectRendering, g2);

                            if(debugRace)
                                System.out.println("13.2");

                            try {
                                /*
                                * If highlights are required for page, reset highlights
                                */
                                this.setLineAreas(null);

                                if(debugRace)
                                    System.out.println("13.3");

                                current.decodePageContent(pdfObject);

                                //All data loaded so now get all line areas for page
                                Vector_Rectangle vr = (Vector_Rectangle) current.getObjectValue(ValueTypes.TextAreas);
                                vr.trim();
                                Rectangle[] pageTextAreas = vr.get();

                                Vector_Int vi =  (Vector_Int) current.getObjectValue(ValueTypes.TextDirections);
                                vi.trim();
                                int[] pageTextDirections = vi.get();

                                for(int k=0; k!=pageTextAreas.length; k++){
                                    this.addToLineAreas(pageTextAreas[k], pageTextDirections[k], page);
                                }
                                if(debugRace)
                                    System.out.println("13.4");

                                // min_x,min_y%%*/
                            } catch (Error err) {

                                decodeStatus = decodeStatus
                                        + "Error in decoding page "
                                        + err.toString();
                            }

                            if(debugRace)
                                System.out.println("13.5");

                            hasEmbeddedFonts = current.getBooleanValue(ValueTypes.EmbeddedFonts);

                            fontsInFile = (String) current.getObjectValue(PdfDictionary.Font);
                            imagesInFile = (String) current.getObjectValue(PdfDictionary.Image);

                            pdfData = (PdfData) current.getObjectValue(ValueTypes.PDFData);
                            if (embedWidthData)
                                pdfData.widthIsEmbedded();

                            // store page width/height so we can translate 270
                            // rotation co-ords
                            pdfData.maxX = mediaW;
                            pdfData.maxY = mediaH;

                            pdfImages = (PdfImageData) current.getObjectValue(ValueTypes.PDFImages);

                            if(debugRace)
                                System.out.println("13.6");

                            //<start-adobe>
                            /** get shape info */
                            //pageLines = current.getPageLines();
                            //<end-adobe>

                            //read flags
                            hasYCCKimages = current.getBooleanValue(DecodeStatus.YCCKImages);
                            imagesProcessedFully = current.getBooleanValue(DecodeStatus.ImagesProcessed);
                            hasNonEmbeddedCIDFonts= current.getBooleanValue(DecodeStatus.NonEmbeddedCIDFonts);
                            nonEmbeddedCIDFonts= (String)current.getObjectValue(DecodeStatus.NonEmbeddedCIDFonts);
                            ttHintingRequired= current.getBooleanValue(DecodeStatus.TTHintingRequired);
                            timeout= current.getBooleanValue(DecodeStatus.Timeout);


                            colorSpacesUsed=(Iterator) current.getObjectValue(PageInfo.COLORSPACES);  //To change body of created methods use File | Settings | File Templates.
                            //current = null;

                        }else if(currentDisplay.getType()==DynamicVectorRenderer.CREATE_HTML){ //needed if no page content

                            int mediaW = pageData.getMediaBoxWidth(pageNumber);
                            int mediaH = pageData.getMediaBoxHeight(pageNumber);
                            //int mediaX = pageData.getMediaBoxX(pageNumber);
                            //int mediaY = pageData.getMediaBoxY(pageNumber);
                            int rotation = pageData.getRotation(pageNumber);
                            currentDisplay.init(mediaW, mediaH, rotation,pageColor);

                        }
                    }

                    if(debugRace)
                        System.out.println("14");


                    /** turn off status bar update */
                    // <start-adobe>
                    if (t != null) {
//                        System.out.println("Timer stops: "+(System.currentTimeMillis()-time));
                        t.stop();
                        statusBar.setProgress(100);

                    }
                    // <end-adobe>

                    //isDecoding = false;
                    //pages.refreshDisplay();

                    if(debugRace)
                        System.out.println("15");


                    /**
                     * handle acroform data to display
                     */
                    if (renderPage) {

                        //do it below so we pick up Javascript
                        flagClosedAtEnd=false;


                        if (!isDuplicate && formRenderer != null && !formRenderer.ignoreForms() && formRenderer.hasFormsOnPage(page)) {

                            //disable color list if forms
                            //colorSpacesUsed=null;

                            if(debugRace)
                                System.out.println("16");

                            //swing needs it to be done with invokeLater
                            if(!SwingUtilities.isEventDispatchThread() && formRenderer.getFormFactory().getType()==FormFactory.SWING){ //

                                if(debugRace)
                                    System.out.println("16a "+formRenderer.hasFormsOnPage(page));

                                final PdfObject pdfObject2=pdfObject;
                                final Runnable doPaintComponent2 = new Runnable() {
                                    public void run() {

                                        if(debugRace)
                                            System.out.println("a");

                                        createFormComponents(page, debugRace,pdfObject2);

                                        if(debugRace)
                                            System.out.println("b");

                                        //System.err.println("d");
                                        //current = null;


                                        if(debugRace)
                                            System.out.println("c");


                                        //validate();
                                        isDecoding=false;

                                        //tell software page all done
                                        currentDisplay.flagDecodingFinished();

                                        if(debugRace)
                                            System.out.println("d "+isDecoding);

                                    }
                                };

                                if(debugRace)
                                    System.out.println("17a");

                                //don't set isDecoding false at end but in our routine
                                isForm=true;

                                SwingUtilities.invokeLater(doPaintComponent2);

                                if(debugRace)
                                    System.out.println("17b");

                                this.waitForDecodingToFinish();

                                if(debugRace)
                                    System.out.println("18a");
                            }else{

                                if(debugRace)
                                    System.out.println("16b");

                                createFormComponents(page, debugRace,pdfObject);
                                //this.waitForDecodingFormsToFinish();

                                isDecoding=false;
                            }
                        }
                    }
                }

                if(debugRace)
                    System.out.println("20");

                if(flagClosedAtEnd){
                    //current = null;

                    //tell software page all done
                    //currentDisplay.flagDecodingFinished();

                }

            } finally {

                if(!isForm){


                    if(debugRace)
                        System.out.println("21 flagClosedAtEnd="+flagClosedAtEnd);


                    if(debugRace)
                        System.out.println("22");


                    isDecoding = false;

                    if(statusBar!=null)
                        statusBar.percentageDone=100;   
                }
            }
        }

        //Check for exceptions in TrueType hinting and re decode if neccessary
        if (TTGlyph.redecodePage) {
            TTGlyph.redecodePage = false;
            decodePage(rawPage);
        }
        
        //tell software page all done
        currentDisplay.flagDecodingFinished();
    }


    /**
     * see if page available if in Linearized mode or return true
     * @param rawPage
     * @return
     */
    public synchronized boolean isPageAvailable(int rawPage) {

        boolean isPageAvailable=true;
        //PageObject linObject=null;

        try{
        if(linearizedBackgroundReaderer !=null && linearizedBackgroundReaderer.isAlive() && rawPage>1 && linHintTable!=null){

            //System.out.println("test page "+rawPage);


            Integer key=new Integer(rawPage);

            //cached data
            if(linObjects.containsKey(key)){
                linObject=(PageObject)linObjects.get(key);

               // System.out.println("cached "+rawPage);
                return true;
            }


            int objID=linHintTable.getPageObjectRef(rawPage);

            //return if Page data not available
            byte[] pageData=linHintTable.getObjData(objID);
            if(pageData!=null){

                /**
                 * turn page into obj
                 */
                //System.out.println("page obj="+objID+" "+new String(pageData));

                linObject=new PageObject(objID+" 0 R");
                linObject.setStatus(PdfObject.UNDECODED_DIRECT);
                linObject.setUnresolvedData(pageData, PdfDictionary.Page);
                linObject.isDataExternal(true);

                ObjectDecoder objDecoder=new ObjectDecoder(currentPdfFile.getObjectReader());

                //see if object and all refs loaded otherwise exit
                if(!objDecoder.resolveFully(linObject))
                    isPageAvailable=false;
                else{  //cache once available

                    /**
                     * check content as well
                     */
                    byte[][] pageContents= null;
                    if(linObject!=null){
                        pageContents= linObject.getKeyArray(PdfDictionary.Contents);

                        //System.out.println("pageContents="+pageContents+" "+linObject+" "+linObject.getObjectRefAsString());

                        //@speed - once converted, lose readPageIntoStream(contents); method
//                    if(linObject!=null && pageContents==null)
//                        b_data=currentPdfFile.readStream(linObject,true,true,false, false,false, null);
//                    else if(pageStream!=null)
//                        b_data=pageStream;
//                    else
                        byte[] b_data=null;
                        if(current!=null)
                        b_data=currentPdfFile.getObjectReader().readPageIntoStream(linObject);

                        //    System.out.println("data="+b_data+" "+b_data.length);

                        if(b_data==null){
                            isPageAvailable=false;
                        }else{
                            //check Resources
                            PdfObject Resources=linObject.getDictionary(PdfDictionary.Resources);

                            if(Resources==null){
                                 linObject=null;
                                isPageAvailable=false;
                            }else if(!objDecoder.resolveFully(Resources)){
                                linObject=null;
                                isPageAvailable=false;
                            }else{
                                Resources.isDataExternal(true);
                                current.readResources(Resources,true);
                                if(!Resources.isFullyResolved()){
                                    linObject=null;
                                    isPageAvailable=false;
                                }
                    }
                        }
                    }
                        if(isPageAvailable && linObject!=null){
                        linObjects.put(key,linObject);
                        //    this.linObject=linObject;
                }
                    }
            }else
                isPageAvailable=false;
        }else
            linObject=null;

        }catch(Exception ee){

            isPageAvailable=false;
        }

        //System.out.println("isPageAvailable="+isPageAvailable);

        return isPageAvailable;
    }

    private void createFormComponents(int page, boolean debugRace, PdfObject pdfObject) {

        formsDecoding=true;

        try{
            if(debugRace)
                System.out.println("14b");

            
            if(currentOffset!=null)
            	formRenderer.getCompData().setPageValues(scaling, displayRotation, (int)indent,0,0,Display.SINGLE_PAGE,currentOffset.widestPageNR,currentOffset.widestPageR);
            
            if(debugRace)
                System.out.println("14c");

            formRenderer.createDisplayComponentsForPage(page);

            if(debugRace)
                System.out.println("14d");

            if(debugRace)
                System.out.println("14e");

        }finally{
            formsDecoding=false;
        }

    }

    /** utility method to allow forms to be drawn inline with the PDF */
    public void drawFlattenedForm(FormObject form) throws PdfException{
    	
    	/**
    	 * we have 3 different PdfStreamDecoders for different operations -
    	 * so we need to make sure we pass it to the correct one!
    	 */
    	if(this.currentImageDecoder!=null)
    		currentImageDecoder.drawFlattenedForm(form);
    	else if(this.currentPrintDecoder!=null){
    		currentPrintDecoder.drawFlattenedForm(form);
    	}else if(current!=null)
    		current.drawFlattenedForm(form);
    	else{
    	}
    }



    /**
     * store objects to use on a print
     * @param page
     * @param type
     * @param colors
     * @param obj
     * @throws PdfException
     */
    public void printAdditionalObjectsOverPage(int page, int[] type, Color[] colors, Object[] obj) throws PdfException {


        Integer key = new Integer(page);

        if (obj == null) { //flush page

            overlayType.remove(key);
            overlayColors.remove(key);
            overlayObj.remove(key);

        } else { //store for printing and add if items already there



            int[] oldType = (int[]) overlayType.get(key);
            if (oldType == null){
                overlayType.put(key, type);

            }else { //merge items

                int oldLength = oldType.length;
                int newLength = type.length;
                int[] combined = new int[oldLength + newLength];

                System.arraycopy(oldType, 0, combined, 0, oldLength);

                System.arraycopy(type, 0, combined, oldLength, newLength);

                overlayType.put(key, combined);
            }


            Color[] oldCol = (Color[]) overlayColors.get(key);
            if (oldCol == null)
                overlayColors.put(key, colors);
            else { //merge items

                int oldLength = oldCol.length;
                int newLength = colors.length;
                Color[] combined = new Color[oldLength + newLength];

                System.arraycopy(oldCol, 0, combined, 0, oldLength);

                System.arraycopy(colors, 0, combined, oldLength, newLength);

                overlayColors.put(key, combined);
            }



            Object[] oldObj = (Object[]) overlayObj.get(key);



            if (oldType == null)
                overlayObj.put(key, obj);
            else { //merge items

                int oldLength = oldObj.length;
                int newLength = obj.length;
                Object[] combined = new Object[oldLength + newLength];

                System.arraycopy(oldObj, 0, combined, 0, oldLength);

                System.arraycopy(obj, 0, combined, oldLength, newLength);

                overlayObj.put(key, combined);
            }
        }

    }

    /**
     * store objects to use on a print
     * @param type
     * @param colors
     * @param obj
     * @throws PdfException
     */
    public void printAdditionalObjectsOverAllPages(int[] type, Color[] colors, Object[] obj) throws PdfException {


        Integer key = new Integer(-1);

        if (obj == null) { //flush page

            overlayTypeG.remove(key);
            overlayColorsG.remove(key);
            overlayObjG.remove(key);

        } else { //store for printing and add if items already there

            int[] oldType = (int[]) overlayTypeG.get(key);
            if (oldType == null){
                overlayTypeG.put(key, type);

            }else { //merge items

                int oldLength = oldType.length;
                int newLength = type.length;
                int[] combined = new int[oldLength + newLength];

                System.arraycopy(oldType, 0, combined, 0, oldLength);

                System.arraycopy(type, 0, combined, oldLength, newLength);

                overlayTypeG.put(key, combined);
            }


            Color[] oldCol = (Color[]) overlayColorsG.get(key);
            if (oldCol == null)
                overlayColorsG.put(key, colors);
            else { //merge items

                int oldLength = oldCol.length;
                int newLength = colors.length;
                Color[] combined = new Color[oldLength + newLength];

                System.arraycopy(oldCol, 0, combined, 0, oldLength);

                System.arraycopy(colors, 0, combined, oldLength, newLength);

                overlayColorsG.put(key, combined);
            }



            Object[] oldObj = (Object[]) overlayObjG.get(key);



            if (oldType == null)
                overlayObjG.put(key, obj);
            else { //merge items

                int oldLength = oldObj.length;
                int newLength = obj.length;
                Object[] combined = new Object[oldLength + newLength];

                System.arraycopy(oldObj, 0, combined, 0, oldLength);

                System.arraycopy(obj, 0, combined, oldLength, newLength);

                overlayObjG.put(key, combined);
            }
        }

    }



    /**
     * uses hires images to create a higher quality display - downside is it is
     * slower and uses more memory (default is false).- Does nothing in OS
     * version
     *
     * @param value
     */
    public void useHiResScreenDisplay(boolean value) {
    }

    //<start-adobe>
    /**
     * decode a page as a background thread (use
     * other background methods to access data)
     *
     *  we now recommend you use decodePage as this has been heavily optimised for speed
     */
    final synchronized public void decodePageInBackground(int i) throws Exception {

        if (isDecoding) {
        	if(LogWriter.isOutput()){
        		LogWriter.writeLog("[PDF]WARNING - this file is being decoded already in foreground");
        		LogWriter.writeLog("[PDF]Multiple access not recommended - use  waitForDecodingToFinish() to check");
            }

        } else if (isBackgroundDecoding) {
        	if(LogWriter.isOutput())
        		LogWriter.writeLog("[PDF]WARNING - this file is being decoded already in background");
        } else {

            try{
                isBackgroundDecoding = true;

                /** check in range */
                if (i > pageCount) {

                	if(LogWriter.isOutput())
                		LogWriter.writeLog("Page out of bounds");

                } else {

                    /** get pdf object id for page to decode */
                    String currentPageOffset = (String) pagesReferences.get(new Integer(i));

                    /**
                     * decode the file if not already decoded, there is a valid
                     * object id and it is unencrypted
                     */
                    if ((currentPageOffset != null)) {

                        if (currentPdfFile == null)
                            throw new PdfException(
                                    "File not open - did you call closePdfFile() inside a loop and not reopen");

                        /** read page or next pages */
                        PdfObject pdfObject=new PageObject(currentPageOffset);
                        currentPdfFile.readObject(pdfObject);
                        PdfObject Resources=pdfObject.getDictionary(PdfDictionary.Resources);

                        // if (!value.equals("null"))
                        if (pdfObject != null) {

                            PdfStreamDecoder backgroundDecoder = new PdfStreamDecoder(currentPdfFile, Resources);
                            backgroundDecoder.setParameters(true, false, 0, extractionMode);

                            backgroundDecoder.setObjectValue(ValueTypes.ImageHandler, customImageHandler);


                            backgroundDecoder.setObjectValue(ValueTypes.Name, filename);
                            backgroundDecoder.setObjectValue(ValueTypes.ObjectStore,backgroundObjectStoreRef);
                            backgroundDecoder.setObjectValue(ValueTypes.PDFData,pageData);
                            backgroundDecoder.setIntValue(ValueTypes.PageNum, i);

                            if (globalResources != null){
                                currentPdfFile.checkResolved(globalResources);
                                backgroundDecoder.readResources(globalResources,true);

                                PdfObject propObj=globalResources.getDictionary(PdfDictionary.Properties);
                                if(propObj!=null)
                                    PropertiesObj=propObj;
                            }

                            /**read the resources for the page*/
                            if (Resources != null){
                                currentPdfFile.checkResolved(Resources);
                                backgroundDecoder.readResources(Resources,true);

                                PdfObject propObj=Resources.getDictionary(PdfDictionary.Properties);
                                if(propObj!=null)
                                    PropertiesObj=propObj;
                            }

                            setupPage(backgroundDecoder, false);

                            /*
                             * If highlights are required for page, reset highlights
                             */
                            this.setLineAreas(null);

                            backgroundDecoder.decodePageContent(pdfObject);
                            /** removed min_x,min_y%% */

                            //All data loaded so now get all line areas for page

                            if(current!=null){
                                Vector_Rectangle vr = (Vector_Rectangle) current.getObjectValue(ValueTypes.TextAreas);
                                vr.trim();
                                Rectangle[] pageTextAreas = vr.get();

                                Vector_Int vi =  (Vector_Int) current.getObjectValue(ValueTypes.TextDirections);
                                vi.trim();

                                int[] pageTextDirections = vi.get();

                                for(int k=0; k!=pageTextAreas.length; k++){
                                    this.addToLineAreas(pageTextAreas[k], pageTextDirections[k], pageNumber);
                                }
                            }
                            pdfBackgroundData = (PdfData)backgroundDecoder.getObjectValue(ValueTypes.PDFData);
                            if (embedWidthData)
                                pdfBackgroundData.widthIsEmbedded();

                            // store page width/height so we can translate 270
                            // rotation co-ords
                            int mediaW = pageData.getMediaBoxWidth(i);
                            int mediaH = pageData.getMediaBoxHeight(i);
                            //int mediaX = pageData.getMediaBoxX(i);
                            //int mediaY = pageData.getMediaBoxY(i);

                            pdfBackgroundData.maxX = mediaW;
                            pdfBackgroundData.maxY = mediaH;

                            pdfBackgroundImages = (PdfImageData) backgroundDecoder.getObjectValue(ValueTypes.PDFImages);

//                            Vector_Rectangle vr = current.getTextAreas();
//                            vr.trim();
//                            Rectangle[] pageTextAreas = vr.get();
//
//                            Vector_Int vi = current.getTextDirections();
//                            vi.trim();
//                            int[] pageTextDirections = vi.get();
//
//                            for(int k=0; k!=pageTextAreas.length; k++){
//                            	this.addToLineAreas(pageTextAreas[k], pageTextDirections[k], i);
//                            }
                        }
                    }

                }

            }catch(Exception e){
                e.printStackTrace();
            }finally {
                isBackgroundDecoding = false;
            }
        }
    }
    //<end-adobe>

    /**
     * get page count of current PDF file
     */
    final public int getPageCount() {
        return pageCount;
    }

    /**
     * return true if the current pdf file is encrypted <br>
     * check <b>isFileViewable()</b>,<br>
     * <br>
     * if file is encrypted and not viewable - a user specified password is
     * needed.
     */
    final public boolean isEncrypted() {

        if (currentPdfFile != null) {
            PdfFileReader objectReader=currentPdfFile.getObjectReader();
            DecryptionFactory decryption=objectReader.getDecryptionObject();
            return decryption!=null && decryption.getBooleanValue(PDFflags.IS_FILE_ENCRYPTED);
        }else
            return false;
    }

    /**
     * show if encryption password has been supplied or set a certificate
     */
    final public boolean isPasswordSupplied() {

        //allow through if user has verified password or set certificate
        if (currentPdfFile != null){
            PdfFileReader objectReader=currentPdfFile.getObjectReader();

            DecryptionFactory decryption=objectReader.getDecryptionObject();
            return decryption!=null && (decryption.getBooleanValue(PDFflags.IS_PASSWORD_SUPPLIED) || certificate!=null);
        }else
            return false;
    }

    /**
     * show if encrypted file can be viewed,<br>
     * if false a password needs entering
     */
    public boolean isFileViewable() {

        if (currentPdfFile != null){
            PdfFileReader objectReader=currentPdfFile.getObjectReader();

            DecryptionFactory decryption=objectReader.getDecryptionObject();
            return decryption==null || decryption.getBooleanValue(PDFflags.IS_FILE_VIEWABLE) || certificate!=null;
        }else
            return false;
    }

    /**
     * show if content can be extracted
     */
    public boolean isExtractionAllowed() {

        if (currentPdfFile != null){

            PdfFileReader objectReader=currentPdfFile.getObjectReader();

            DecryptionFactory decryption=objectReader.getDecryptionObject();
            return decryption==null || decryption.getBooleanValue(PDFflags.IS_EXTRACTION_ALLOWED);

        }else
            return false;
    }

    /**
     * give user access to PDF flag value
     * - if file not open or input not valid
     * returns -1
     * <p/>
     * Possible values in PdfFLAGS
     * <p/>
     * ie PDFflags.USER_ACCESS_PERMISSIONS - return P value
     * <p/>
     * PDFflags.VALID_PASSWORD_SUPPLIED - tell if password supplied and if owner or user
     * POssible values PDFflags.NO_VALID_PASSWORD, PDFflags.VALID_USER_PASSWORD, PDFflags.VALID_OWNER_PASSWORD or both USER and owner
     */
    public int getPDFflag(Integer i) {
        if (currentPdfFile != null)
            return currentPdfFile.getObjectReader().getPDFflag(i);
        else
            return -1;
    }

    /**
     * used to retest access and see if entered password is valid,<br>
     * If so file info read and isFileViewable will return true
     */
    private void verifyAccess() {
        if (currentPdfFile != null) {
            try {
                openPdfFile();
            } catch (Exception e) {
                if(LogWriter.isOutput())
                	LogWriter.writeLog("Exception " + e + " opening file");
            }
        }
    }

    /**
     * set the font used for default from Java fonts on system - Java fonts are
     * case sensitive, but JPedal resolves this internally, so you could use
     * Webdings, webdings or webDings for Java font Webdings - checks if it is a
     * valid Java font (otherwise it will default to Lucida anyway)
     */
    public static void setDefaultDisplayFont(String fontName)
            throws PdfFontException {

        boolean isFontInstalled = false;

        // get list of fonts and see if installed
        String[] fontList = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();

        int count = fontList.length;

        for (int i = 0; i < count; i++) {
            if (fontList[i].toLowerCase().equals(fontName.toLowerCase())) {
                isFontInstalled = true;
                defaultFont = fontList[i];
                i = count;
            }
        }

        if (!isFontInstalled)
            throw new PdfFontException("Font " + fontName + " is not available.");

    }

    /**
     * set a password for encryption - software will resolve if user or owner
     * password- calls verifyAccess() from 2.74 so no separate call needed
     */
    final public void setEncryptionPassword(String password) throws PdfException {

        if (currentPdfFile == null)
            throw new PdfException("Must open PdfDecoder file first");

        currentPdfFile.getObjectReader().setPassword(password);

        verifyAccess();
    }

    /**
     * routine to open a byte stream containing the PDF file and extract key info
     * from pdf file so we can decode any pages. Does not actually decode the
     * pages themselves - By default files over 16384 bytes are cached to disk
     * but this can be altered by setting PdfFileReader.alwaysCacheInMemory to a maximimum size or -1 (always keep in memory)
     *
     */
    final public void openPdfArray(byte[] data) throws PdfException {

        if(data==null)
            throw new RuntimeException("Attempting to open null byte stream");

        if(isOpen)
            //throw new RuntimeException("Previous file not closed");
            closePdfFile(); //also checks decoding done

        isOpen = false;

        globalResources=null;
        pagesReferences.clear();

        try {

            currentPdfFile = new PdfReader();

            /** get reader object to open the file */
            currentPdfFile.openPdfFile(data);

            openPdfFile();

            /** store file name for use elsewhere as part of ref key without .pdf */
            objectStoreRef.storeFileName("r" + System.currentTimeMillis());

        } catch (Exception e) {
            throw new PdfException("[PDF] OpenPdfArray generated exception "
                    + e.getMessage());
        }
    }

    /**
     * allow user to open file using Certificate and key
     * @param filename
     * @param certificate
     * @param key
     */
    public void openPdfFile(String filename, Certificate certificate, PrivateKey key) throws PdfException{

        /**
         * set values and then call generic open
         */
        this.certificate=certificate;
        this.key=key;

        openPdfFile(filename);
    }

    /**
     * routine to open PDF file and extract key info from pdf file so we can
     * decode any pages. Does not actually decode the pages themselves. Also
     * reads the form data. You must explicitly close any open files with
     * closePdfFile() to Java will not release all the memory
     */
    final public void openPdfFile(final String filename) throws PdfException {

        if(isOpen && linearizedBackgroundReaderer==null)
            //throw new RuntimeException("Previous file not closed");
            closePdfFile(); //also checks decoding done

        isOpen = false;

        displayScaling = null;


        //System.out.println(filename);

        this.filename = filename;
        globalResources=null;
        pagesReferences.clear();

        /** store file name for use elsewhere as part of ref key without .pdf */
        objectStoreRef.storeFileName(filename);

        /**
         * possible caching of code File testFile=new File(filename);
         *
         * int size=(int)testFile.length(); if(size<300*1024){ byte[]
         * fileData=new byte[size]; // read the object try {
         *
         * FileInputStream fis=new FileInputStream(testFile);
         *
         * //get binary data fis.read( fileData ); } catch( Exception e ) {
         * LogWriter.writeLog( "Exception " + e + " reading from file" ); }
         *
         * openPdfFile(fileData); }else
         */


        /**
         * create Reader, passing in certificate if set
         */
        if(certificate!=null){
            currentPdfFile = new PdfReader(certificate, key);
        }else
            currentPdfFile = new PdfReader();

        /** get reader object to open the file */
        currentPdfFile.openPdfFile(filename);

        /**test code in case we need to test byte[] version
         //get size
         try{
         File file=new File(filename);
         int length= (int) file.length();
         byte[] fileData=new byte[length];
         FileInputStream fis=new FileInputStream(filename);
         fis.read(fileData);
         fis.close();
         currentPdfFile.openPdfFile(fileData);
         }catch(Exception e){

         }/**/

        openPdfFile();

    }

    /**
     * routine to open PDF file and extract key info from pdf file so we can
     * decode any pages which also sets password.
     * Does not actually decode the pages themselves. Also
     * reads the form data. You must explicitly close any open files with
     * closePdfFile() or Java will not release all the memory
     */
    final public void openPdfFile(final String filename,String password) throws PdfException {

        if(isOpen)
            //throw new RuntimeException("Previous file not closed");
            closePdfFile(); //also checks decoding done

        isOpen = false;

        displayScaling = null;


        this.filename = filename;
        globalResources=null;
        pagesReferences.clear();

        /** store file name for use elsewhere as part of ref key without .pdf */
        objectStoreRef.storeFileName(filename);

        /**
         * possible caching of code File testFile=new File(filename);
         *
         * int size=(int)testFile.length(); if(size<300*1024){ byte[]
         * fileData=new byte[size]; // read the object try {
         *
         * FileInputStream fis=new FileInputStream(testFile);
         *
         * //get binary data fis.read( fileData ); } catch( Exception e ) {
         * LogWriter.writeLog( "Exception " + e + " reading from file" ); }
         *
         * openPdfFile(fileData); }else
         */

        currentPdfFile = new PdfReader(password);

        /** get reader object to open the file */
        currentPdfFile.openPdfFile(filename);

        openPdfFile();


    }

    /**
     * routine to open PDF file via URL and extract key info from pdf file so we
     * can decode any pages - Does not actually decode the pages themselves -
     * Also reads the form data - Based on an idea by Peter Jacobsen
     * <br />
     * You must explicitly close any open files with closePdfFile() so Java will
     * release all the memory
     * <br />
     *
     * If boolean supportLinearized is true, method will return with true value once Linearized part read
     */
    final public boolean openPdfFileFromURL(String pdfUrl, boolean supportLinearized) throws PdfException {

        InputStream is=null;

        String rawFileName = null;

        try{
            URL url;
            url = new URL(pdfUrl);
            rawFileName = url.getPath().substring(url.getPath().lastIndexOf('/')+1);

            is = url.openStream();
        }catch(Exception e){
            e.printStackTrace();
        }

        return readFile(supportLinearized, is, rawFileName);

    }

    /**
     * routine to open PDF file via InputStream and extract key info from pdf file so we
     * can decode any pages - Does not actually decode the pages themselves -
     * <p/>
     * You must explicitly close any open files with closePdfFile() to Java will
     * not release all the memory
     *
     * IMPORTANT NOTE: If the stream does not contain enough bytes, test for Linearization may fail
     * If boolean supportLinearized is true, method will return with true value once Linearized part read
     * (we recommend use you false unless you know exactly what you are doing)
     */
    final public boolean openPdfFileFromInputStream(InputStream is, boolean supportLinearized) throws PdfException {

        String rawFileName = "inputstream"+System.currentTimeMillis()+".pdf";

        //make sure it will be deleted
        objectStoreRef.setFileToDeleteOnFlush(rawFileName);

        return readFile(supportLinearized, is, rawFileName);

    }

    /**
     * common code for reading URL and InputStream
     * @param supportLinearized
     * @param is
     * @param rawFileName
     * @return
     * @throws PdfException
     */
    private boolean readFile(boolean supportLinearized, InputStream is, String rawFileName) throws PdfException {

        displayScaling = null;
        globalResources=null;
        pagesReferences.clear();
        currentPdfFile = new PdfReader();


        if(is!=null){
            try {

                final File tempURLFile = ObjectStore.createTempFile(rawFileName);
                //final File tempURLFile = new File("/Users/markee/Desktop/fos2.pdf");

                // <start-me>
                final FileChannel fos = new RandomAccessFile(tempURLFile,"rws").getChannel();
                fos.force(true);
                // <end-me>

                final ByteArrayOutputStream bos=new ByteArrayOutputStream(8192);

                // Download buffer
                byte[] buffer = new byte[4096];
                int read,bytesRead=0;

                //main loop to read all the file bytes (carries on in thread if linearized)
                while ((read = is.read(buffer)) != -1) {

                	// <start-me>
                    if(read>0){
                        synchronized (fos){

                            byte[] b=new byte[read];
                            System.arraycopy(buffer,0,b,0,read);
                            ByteBuffer f=ByteBuffer.wrap(b);
                            fos.write(f);
                        }
                    }
                    // <end-me>

                    bytesRead=bytesRead+read;

                }
                //fos.flush();
                // Close streams
                is.close();
                // <start-me>
                synchronized (fos){
                    fos.close();
                }
                // <end-me>

                /** get reader object to open the file */
                openPdfFile(tempURLFile.getAbsolutePath());

                /** store fi name for use elsewhere as part of ref key without .pdf */
                objectStoreRef.storeFileName(tempURLFile.getName().substring(0, tempURLFile.getName().lastIndexOf('.')));

            } catch (IOException e) {
            	if(LogWriter.isOutput())
            		LogWriter.writeLog("[PDF] Exception " + e + " opening URL ");
                
            	e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * examine first few bytes to see if linearized and return true linearized file
     * @param pdfUrl
     * @return
     * @throws PdfException
     */
    static final public boolean isPDFLinearized(String pdfUrl) throws PdfException {

        if (pdfUrl.startsWith("jar"))
            return false;

        boolean isLinear=false;
        //read first few bytes
        URL url;
        final InputStream is;

        try {
            url = new URL(pdfUrl);
            is = url.openStream();
            //final String filename = url.getPath().substring(url.getPath().lastIndexOf('/')+1);

            // Download buffer
            byte[] buffer = new byte[128];
            is.read(buffer);
            is.close();


        } catch (IOException e) {
        	if(LogWriter.isOutput())
            LogWriter.writeLog("[PDF] Exception " + e + " scanning URL "+ pdfUrl);
            e.printStackTrace();
        }

        return isLinear;

    }



    /**
     * common code to all open routines
     */
    private synchronized void openPdfFile() throws PdfException {

        pageNumber = 1; // reset page number for metadata

        lastFormPage = -1;
        lastEnd = -1;
        lastStart = -1;

        //handle fdf
        PdfObject fdfObj=null;
        if (filename != null && filename.toLowerCase().endsWith(".fdf")) {

            int i = filename.lastIndexOf('/');

            if (i == -1)
                i = filename.lastIndexOf('\\');

            String path = "";
            if (i != -1)
                path = filename.substring(0, i + 1);

            /**read in data from fdf*/

            fdfObj = currentPdfFile.readFDF();

            /** store file name for use elsewhere as part of ref key without .pdf */
            if(fdfObj!=null){

                String pdfFile = fdfObj.getTextStreamValue(PdfDictionary.F);

                if (pdfFile != null)
                    filename = path + pdfFile;
            }

            objectStoreRef.storeFileName(filename);

            //open actual PDF
            this.currentPdfFile.openPdfFile(filename);

        } else{
            fdfObj=null;
        }

        try {
            isDecoding = true;

            pages.resetCachedValues();

            // remove listener if not removed by close
            if (hasViewListener) {
                //hasViewListener = false;

                //flush any cached pages
                pages.flushPageCaches();

                //<start-adobe>
                removeComponentListener(viewListener);
                //<end-adobe>

            }

            // set cache size to use

            currentPdfFile.getObjectReader().setCacheSize(minimumCacheSize);

            // reset printing
            lastPrintedPage = -1;
            this.currentPrintDecoder = null;


            if (formRenderer != null) {
                formRenderer.getCompData().setRootDisplayComponent(this);

            }
            //invalidate();

            // reset page data - needed to flush crop settings
            pageData = new PdfPageData();
            // read and log the version number of pdf used
            pdfVersion = currentPdfFile.getObjectReader().getType();
            
            if(LogWriter.isOutput())
            	LogWriter.writeLog("Pdf version : " + pdfVersion);

            if (pdfVersion == null) {
                currentPdfFile = null;
                isDecoding = false;

                throw new PdfException( "No version on first line ");

            }

            // read reference table so we can find all objects and say if
            // encrypted
            PdfObject pdfObject ;

            int linearPageCount=-1;

            long Ooffset=-1;

            //<start-demo>
            /**
            //<end-demo>
            /**/

            //linear page object set differently
            if(linearObj!=null && E!=-1){

                int O=linearObj.getInt(PdfDictionary.O);

                //read in the pages from the catalog and set values
                if(O!=-1){
                    linearObj.setIntNumber(PdfDictionary.O, -1);
                    currentPdfFile.getObjectReader().readReferenceTable(linearObj);
                    pdfObject=new PageObject(O,0);
                    currentPdfFile.readObject(pdfObject);

                    //get page count from linear data
                    linearPageCount=linearObj.getInt(PdfDictionary.N);

                    Ooffset = currentPdfFile.getObjectReader().getOffset(O);


                }else{ //use O as flag and reset
                    pdfObject=currentPdfFile.getObjectReader().readReferenceTable(null);
                }

                /**
                 * read and decode the hints table
                 */
                int[] H=linearObj.getIntArray(PdfDictionary.H);

                byte[] hintStream=currentPdfFile.getObjectReader().getBytes(H[0], H[1]);

                //find <<
                int length=hintStream.length;
                int startHint=0;
                int i=0;
                boolean contentIsDodgy=false;

                //number
                int keyStart2=i;
                while(hintStream[i]!=10 && hintStream[i]!=13 && hintStream[i]!=32 && hintStream[i]!=47 && hintStream[i]!=60 && hintStream[i]!=62){

                    if(hintStream[i]<48 || hintStream[i]>57) //if its not a number value it looks suspicious
                        contentIsDodgy=true;

                	i++;
                }

                //trap for content not correct
                if(!contentIsDodgy){

                    int number= NumberUtils.parseInt(keyStart2, i, hintStream);

                    //generation
                    while(hintStream[i]==10 || hintStream[i]==13 || hintStream[i]==32 || hintStream[i]==47 || hintStream[i]==60)
                        i++;

                    keyStart2=i;
                    //move cursor to end of reference
                    while(i<10 && hintStream[i]!=10 && hintStream[i]!=13 && hintStream[i]!=32 && hintStream[i]!=47 && hintStream[i]!=60 && hintStream[i]!=62)
                        i++;
                    int generation= NumberUtils.parseInt(keyStart2, i, hintStream);

                    while(i<length-1){

                        if(hintStream[i]=='<' && hintStream[i+1]=='<'){
                            startHint=i;
                            i=length;
                        }

                        i++;
                    }

                    byte[] data=new byte[length-startHint];

                    //convert the raw data into a PDF object
                    System.arraycopy(hintStream,startHint,data,0,data.length);
                    LinearizedObject hintObj=new LinearizedObject(number,generation);
                    hintObj.setStatus(PdfObject.UNDECODED_DIRECT);
                    hintObj.setUnresolvedData(data, PdfDictionary.Linearized);
                    currentPdfFile.checkResolved(hintObj);

                    //get page content pointers
                    linHintTable.readTable(hintObj,linearObj,O,Ooffset);

                    if(debugLinearization && LogWriter.isOutput())
                        LogWriter.writeLog("[Linearized] Linearized table loaded and stored in PdfReader");

                }
                /**/

            }else//<end-gpl>
                pdfObject=currentPdfFile.getObjectReader().readReferenceTable(null);

            //new load code - be more judicious in how far down tree we scan
            final boolean ignoreRecursion=true;

            // open if not encrypted or has password
            if (!isEncrypted() || isPasswordSupplied()) {

                if (pdfObject != null){
                    pdfObject.ignoreRecursion(ignoreRecursion);

                    currentPdfFile.checkResolved(pdfObject);
                    metadataObj=pdfObject.getDictionary(PdfDictionary.Metadata);
                    structTreeRootObj=pdfObject.getDictionary(PdfDictionary.StructTreeRoot);
                    markInfoObj=pdfObject.getDictionary(PdfDictionary.MarkInfo);
                    acroFormObj=pdfObject.getDictionary(PdfDictionary.AcroForm);
                    OutlinesObj=pdfObject.getDictionary(PdfDictionary.Outlines);
                    nameObj=pdfObject.getDictionary(PdfDictionary.Names);
                    OCProperties=pdfObject.getDictionary(PdfDictionary.OCProperties);

                }


                int type=pdfObject.getParameterConstant(PdfDictionary.Type);
                if(type!=PdfDictionary.Page){

                    PdfObject pageObj= pdfObject.getDictionary(PdfDictionary.Pages);
                    if(pageObj!=null){ //do this way incase in separate compressed stream

                        pdfObject=new PageObject(pageObj.getObjectRefAsString());
                        currentPdfFile.readObject(pdfObject);

                        // System.out.println("page="+pageObj+" "+pageObj.getObjectRefAsString());
                        //catch for odd files
                        if(pdfObject.getParameterConstant(PdfDictionary.Type)==-1)
                        pdfObject=pageObj;

                        //System.out.println("test code called");
                    }
                }

                if (pdfObject != null) {
                	
                	if(LogWriter.isOutput())
                		LogWriter.writeLog("Pages being read from "+pdfObject+ ' '+pdfObject.getObjectRefAsString());
                    
                	pageNumber = 1; // reset page number for metadata
                    tempPageCount=1;
                    // reset lookup table
                    pageLookup = new PageLookup();

                    //flush annots before we reread

                    if(formRenderer!=null)
                        formRenderer.resetAnnotData(pageLookup, insetW, insetH, pageData, 1, currentPdfFile,null);

                    //recursively read all pages
                    readAllPageReferences(ignoreRecursion, pdfObject, new HashMap(), new HashMap());

                    //set PageCount if in Linearized data
                    if(linearPageCount>0){
                        pageCount=linearPageCount;
                    }else{
                        pageCount = tempPageCount - 1; // save page count
                    }

                    pageNumber =0; // reset page number for metadata;
                    if (this.pageCount == 0 && LogWriter.isOutput()) 
                        LogWriter.writeLog("No pages found");
                }


                //<start-adobe><start-me>
                //set up outlines
                outlineData = null;
                hasOutline = OutlinesObj != null;

                //<end-adobe><end-me>

                // pass handle into renderer
                if (formRenderer != null) {
                    formRenderer.openFile(pageCount);

                    currentPdfFile.checkResolved(acroFormObj);


                    formRenderer.resetFormData(pageLookup, insetW, insetH, pageData, currentPdfFile, acroFormObj);
                }
            }



            currentOffset = null;

            // reset so if continuous view mode set it will be recalculated for
            // page
            pages.disableScreen();

            pages.stopGeneratingPage();

            //force back if only 1 page
            if (pageCount < 2)
                displayView = Display.SINGLE_PAGE;
            else
                displayView = pageMode;


            //<start-adobe>
            setDisplayView(this.displayView, alignment); //force reset and add back listener
            /**
            //<end-adobe>
            if (currentOffset == null)
            	currentOffset = new PageOffsets(pageCount, pageData);
            /**/

            isOpen = true;
        } catch (PdfException e) {

            //ensure all data structures/handles flushed
            isDecoding = false;
            isOpen=true; //temporarily set to true as otherwise will not work
            closePdfFile();

            isOpen=false;

            isDecoding = false;
            throw new PdfException(e.getMessage() + " opening file");
        }

        isDecoding = false;

    }

    /**
     * will return Layout dictionary when code enabled
     * (resolved at this point)
     * @deprecated - please use (PdfLayerList)getJPedalObject(PdfDictionary.Layer);
     */
    public PdfLayerList getLayers(){
        return layers;

    }

    /**
     * will return some dictionary values - if not a set value,
     * will return null
     * @return
     */
    public Object getJPedalObject(int id){
        switch(id){
            case PdfDictionary.Layer:
                return layers;

            case PdfDictionary.Linearized:

                //lazy initialisation if not URLstream
                if(!isLinearizationTested){

                }
                return linearObj;

            case PdfDictionary.LinearizedReader:

                return linearizedBackgroundReaderer;

            default:
                return null;
        }


    }

    /**Set default page Layout*/
    private int pageMode = Display.SINGLE_PAGE;

    private Map jpedalActionHandlers;


    public void setPageMode(int mode){
        pageMode = mode;
    }

    /**
     * allow access to Javascript object. Not part of API and not recommended for general usage
     *
     public Javascript getJavascript() {
     return javascript;
     } /**/


    /**
     * read the data from pages lists and pages so we can open each page.
     *
     * object reference to first trailer
     */
    private void readAllPageReferences(boolean ignoreRecursion, PdfObject pdfObject , Map rotations, Map parents) {

        String currentPageOffset=pdfObject.getObjectRefAsString();

        final boolean debug=false;

        int rotation=0;

        int type=pdfObject.getParameterConstant(PdfDictionary.Type);

        if(debug)
            System.out.println("currentPageOffset="+currentPageOffset+" type="+type+ ' '+PdfDictionary.showAsConstant(type));

        if(type== PdfDictionary.Unknown)
            type= PdfDictionary.Pages;


        /**
         * handle common values which can occur at page level or higher
         */

        /** page rotation for this or up tree*/
        int rawRotation=pdfObject.getInt(PdfDictionary.Rotate);
        String parent=pdfObject.getStringKey(PdfDictionary.Parent);

        if(rawRotation==-1 ){

            while(parent!=null && rawRotation==-1){

                if(parent!=null){
                    Object savedRotation=rotations.get(parent);
                    if(savedRotation!=null)
                        rawRotation=((Integer)savedRotation).intValue();
                }

                if(rawRotation==-1)
                    parent=(String) parents.get(parent);

            }

            //save
            if(rawRotation!=-1){
                rotations.put(currentPageOffset,new Integer(rawRotation));
                parents.put(currentPageOffset,parent);
            }
        }else{ //save so we can lookup
            rotations.put(currentPageOffset,new Integer(rawRotation));
            parents.put(currentPageOffset,parent);
        }

        if(rawRotation!=-1)
            rotation=rawRotation;

        pageData.setPageRotation(rotation, tempPageCount);

        /**
         * handle media and crop box, defaulting to higher value if needed (ie
         * Page uses Pages and setting crop box
         */
        float[] mediaBox=pdfObject.getFloatArray(PdfDictionary.MediaBox);
        float[] cropBox=pdfObject.getFloatArray(PdfDictionary.CropBox);

        if (mediaBox != null)
            pageData.setMediaBox(mediaBox);

        if (cropBox != null)
            pageData.setCropBox(cropBox);

        /** process page to read next level down */
        if (type==PdfDictionary.Pages) {

            globalResources=pdfObject.getDictionary(PdfDictionary.Resources);

            byte[][] kidList = pdfObject.getKeyArray(PdfDictionary.Kids);

            int kidCount=0;
            if(kidList!=null)
                kidCount=kidList.length;

            if(debug)
                System.out.println("PAGES---------------------currentPageOffset="+currentPageOffset+" kidCount="+kidCount);

            /** allow for empty value and put next pages in the queue */
            if (kidCount> 0) {

                if(debug)
                    System.out.println("KIDS---------------------currentPageOffset="+currentPageOffset);

                for(int ii=0;ii<kidCount;ii++){

                    String nextValue=new String(kidList[ii]);

                    PdfObject nextObject=new PageObject(nextValue);
                    nextObject.ignoreRecursion(ignoreRecursion);
                    nextObject.ignoreStream(true);

                    currentPdfFile.readObject(nextObject);
                    readAllPageReferences(ignoreRecursion, nextObject, rotations, parents);
                }

            }

        } else if (type==PdfDictionary.Page) {

            if(debug)
                System.out.println("PAGE---------------------currentPageOffset="+currentPageOffset);

            // store ref for later
            pagesReferences.put(new Integer(tempPageCount), currentPageOffset);
            pageLookup.put(currentPageOffset, tempPageCount);

            pageData.checkSizeSet(tempPageCount); // make sure we have min values
            // for page size

            /**if(structTreeRoot!=null){
             int structParents=Integer.parseInt((String)values.get("StructParents"));
             lookupStructParents.put(new Integer(pageNumber),new Integer(structParents));
             }*/


            /**
             * add Annotations
             */
            if (formRenderer != null) {

                /**
                 * read the annotations reference for the page we have
                 * found lots of issues with annotations so trap errors
                 */

                byte[][] annotList = pdfObject.getKeyArray(PdfDictionary.Annots);

                //allow for empty
                if(annotList!=null && annotList.length==1 && annotList[0]==null)
                    annotList=null;

                if (annotList != null) {

                    // pass handle into renderer
                    formRenderer.resetAnnotData(pageLookup, insetW, insetH, pageData, tempPageCount, currentPdfFile, annotList);
                }
            }

            tempPageCount++;

        }

    }

    private static ArrayList getDirectoryMatches(String sDirectoryName)
            throws IOException {

        MEUtils.replaceAll(sDirectoryName,"\\.","/");

        URL u = Thread.currentThread().getContextClassLoader().getResource(
                sDirectoryName);
        ArrayList retValue = new ArrayList(0);
        String s = u.toString();

        System.out.println("scanning " + s);

        if (s.startsWith("jar:") && s.endsWith(sDirectoryName)) {
            int idx = s.lastIndexOf(sDirectoryName);
            s = s.substring(0, idx); // isolate entry name

            System.out.println("entry= " + s);

            URL url = new URL(s);
            // Get the jar file
            JarURLConnection conn = (JarURLConnection) url.openConnection();
            JarFile jar = conn.getJarFile();

            for (Enumeration e = jar.entries(); e.hasMoreElements();) {
                JarEntry entry = (JarEntry) e.nextElement();
                if ((!entry.isDirectory())
                        & (entry.getName().startsWith(sDirectoryName))) { // this
                    // is how you can match
                    // to find your fonts.
                    // System.out.println("Found a match!");
                    String fontName = entry.getName();
                    int i = fontName.lastIndexOf('/');
                    fontName = fontName.substring(i + 1);
                    retValue.add(fontName);
                }
            }
        } else {
            // Does not start with "jar:"
            // Dont know - should not happen
        	if(LogWriter.isOutput())
        		LogWriter.writeLog("Path: " + s);
        }
        return retValue;
    }

    /**
     * read values from the classpath
     */
    private static ArrayList readIndirectValues(InputStream in)
            throws IOException {
        ArrayList fonts;
        BufferedReader inpStream = new BufferedReader(new InputStreamReader(in));
        fonts = new ArrayList(0);
        while (true) {
            String nextValue = inpStream.readLine();
            if (nextValue == null)
                break;

            fonts.add(nextValue);
        }

        inpStream.close();

        return fonts;
    }

    /**
     * This routine allows the user to add truetype,
     * type1 or type1C fonts which will be used to disalay the fonts in PDF
     * rendering and substitution as if the fonts were embedded in the PDF <br>
     * This is very useful for clients looking to keep down the size of PDFs
     * transmitted and control display quality -
     * <p/>
     * Thanks to Peter for the idea/code -
     * <p/>
     * How to set it up -
     * <p/>
     * JPedal will look for the existence of the directory fontPath (ie
     * com/myCompany/Fonts) -
     * <p/>
     * If this exists, Jpedal will look for 3 possible directories (tt,t1c,t1)
     * and make a note of any fonts if these directories exist -
     * <p/>
     * When fonts are resolved, this option will be tested first and if a font
     * if found, it will be used to display the font (the effect will be the
     * same as if the font was embedded) -
     * <p/>
     * If the enforceMapping is true, JPedal assumes there must be a match and
     * will throw a PdfFontException -
     * <p/>
     * Otherwise Jpedal will look in the java font path for a match or
     * approximate with Lucida -
     * <p/>
     * The Format is defined as follows: -
     * <p/>
     * fontname = filename
     * <p/>
     * Type1/Type1C Font names exclude any prefix so /OEGPNB+FGHeavyItalic is
     * resolved to FGHeavyItalic -
     * <p/>
     * Each font have the same name as the font it replaces (so Arial will
     * require a font file such as Arial.ttf) and it must be unique (there
     * cannot be an Arial font in each sub-directory) -
     * <p/>
     * So to use this functionality, place the fonts in a jar or add to the
     * JPedal jar and call this method after instancing PdfDecoder - JPedal will
     * do the rest
     *
     * @param fontPath       -
     *                       root directory for fonts
     * @param enforceMapping -
     *                       tell JPedal if all fonts should be in this directory
     * @return flag (true if fonts added)
     */
    public boolean addSubstituteFonts(String fontPath, boolean enforceMapping) {

        boolean hasFonts = false;

        try {
            String[] dirs = {"tt", "t1c", "t1"};
            String[] types = {"/TrueType", "/Type1C", "/Type1"};

            // check fontpath ends with separator - we may need to check this.
            // if((!fontPath.endsWith("/"))&(!fontPath.endsWith("\\")))
            // fontPath=fontPath=fontPath+separator;

            enforceFontSubstitution = enforceMapping;

            ClassLoader loader = this.getClass().getClassLoader();

            // see if root dir exists
            InputStream in = loader.getResourceAsStream(fontPath);

            if(LogWriter.isOutput())
            	LogWriter.writeLog("Looking for root " + fontPath);

            // if it does, look for sub-directories
            if (in != null) {

            	if(LogWriter.isOutput())
            		LogWriter.writeLog("Adding fonts fonts found in  tt,t1c,t1 sub-directories of "+ fontPath);

                hasFonts = true;

                for (int i = 0; i < dirs.length; i++) {

                    if (!fontPath.endsWith("/"))
                        fontPath = fontPath + '/';

                    String path = fontPath + dirs[i] + '/';

                    // see if it exists
                    in = loader.getResourceAsStream(path);

                    // if it does read its contents and store
                    if (in != null) {
                        System.out.println("Found  " + path + ' ' + in);

                        ArrayList fonts;

                        try {

                            // works with IDE or jar
                            if (in instanceof ByteArrayInputStream)
                                fonts = readIndirectValues(in);
                            else
                                fonts = getDirectoryMatches(path);

                            String value, fontName;

                            // now assign the fonts
                            int count = fonts.size();
                            for (int ii = 0; ii < count; ii++) {

                                value = (String) fonts.get(ii);

                                if (value == null)
                                    break;

                                int pointer = value.indexOf('.');
                                if (pointer == -1)
                                    fontName = value;
                                else
                                    fontName = value.substring(0, pointer);

                                FontMappings.fontSubstitutionTable.put(fontName
                                        .toLowerCase(), types[i]);
                                FontMappings.fontSubstitutionLocation.put(fontName
                                        .toLowerCase(), path + value);
                                //LogWriter.writeLog("Added from jar ="
                                //		+ fontName + " path=" + path + value);

                            }

                        } catch (Exception e) {
                        	if(LogWriter.isOutput())
                        			LogWriter.writeLog("Exception " + e+ " reading substitute fonts");
                            
                        	System.out.println("Exception " + e+ " reading substitute fonts");
                            // <start-demo>
                            // <end-demo>
                        }
                    }

                }
            } else if(LogWriter.isOutput())
                LogWriter.writeLog("No fonts found at " + fontPath);

        } catch (Exception e) {
        	if(LogWriter.isOutput())
        			LogWriter.writeLog("Exception adding substitute fonts "+ e.getMessage());
        }

        return hasFonts;

    }

    //<start-adobe>

    /**
     * allow user to set own icons for annotation hotspots to display in
     * renderer - pass user selection of hotspots as an array of format
     * Image[number][page] where number is Annot number on page and page is
     * current page -1 (ie 0 is page 1).
     *
     public void addUserIconsForAnnotations(int page, String type, Image[] icons) {

     if (userAnnotIcons == null)
     userAnnotIcons = new Hashtable();

     if(icons==null)
     userAnnotIcons.remove((page) + "-" + type);
     else
     userAnnotIcons.put((page) + "-" + type, icons);

     if (displayHotspots == null) {
     displayHotspots = new Hotspots();
     printHotspots = new Hotspots();
     }

     // ensure type logged
     displayHotspots.checkType(type);
     printHotspots.checkType(type);
     }/**/

    /**
     * initialise display hotspots and save global values
     *
     public void createPageHostspots(String[] annotationTypes, String string) {

     if(showAnnotations)
     return;

     if(displayHotspots==null || specialMode==SpecialOptions.NONE || specialMode==SpecialOptions.SINGLE_PAGE)
     displayHotspots = new Hotspots(annotationTypes, string);
     printHotspots = new Hotspots(annotationTypes, string);

     }/**/

    //<end-adobe>

    //<end-canoo><end-os>


    public void setThumbnailsDrawing(boolean b) {
        thumbnailsBeingDrawn=b;
        pages.setThumbnailsDrawing(b);

    }

    /**
     * show the imageable area in printout for debugging purposes
     */
    public void showImageableArea() {

        showImageable = true;

    }

    // <start-me>
    /**
     * part of pageable interface - used only in printing
     * Use getPageCount() for number of pages
     *
     * @see java.awt.print.Pageable#getNumberOfPages()
     */
    public int getNumberOfPages() {

        //handle 1,2,5-7,12
        if (range != null) {
            int rangeCount = 0;
            for (int ii = 1; ii < this.pageCount + 1; ii++) {
                if (range.contains(ii) && (!oddPagesOnly || (ii & 1) == 1) && (!evenPagesOnly || (ii & 1) == 0))
                    rangeCount++;
            }
            return rangeCount;
        }

        int count = 1;


        if (end != -1) {
            count = end - start + 1;
            if (count < 0) //allow for reverse order
                count = 2 - count;
        }

        if (oddPagesOnly || evenPagesOnly) {
            return (count + 1) / 2;
        } else {
            return count;
        }
    }

    /**
     * part of pageable interface
     *
     * @see java.awt.print.Pageable#getPageFormat(int)
     */
    public PageFormat getPageFormat(int p) throws IndexOutOfBoundsException {

        //System.out.println("=========getPageFormat "+p+" =========");
        Object returnValue;

        int actualPage;

        //remap if in list
        if(listOfPages!=null && p<listOfPages.length){
            p=listOfPages[p];
        }

        if (end == -1)
            actualPage = p + 1;
        else if (end > start)
            actualPage = start + p;
        else
            actualPage = start - p;

        returnValue = pageFormats.get(new Integer(actualPage));

        if (debugPrint)
            System.out.println("======================================================\nspecific for page="+returnValue + " Get page format for page p=" + p
                    + " start=" + start + " pf=" + pageFormats + ' '
                    + pageFormats.keySet());

        if (returnValue == null)
            returnValue = pageFormats.get("standard");

        PageFormat pf = new PageFormat();


        //usePDFPaperSize=true;
       // System.out.println("usePDFPaperSize="+usePDFPaperSize+" actualPage="+actualPage);

        pageFormats.put("Align-"+actualPage, "normal");

        if (usePDFPaperSize) {

            int crw = pageData.getCropBoxWidth(actualPage);
            int crh = pageData.getCropBoxHeight(actualPage);
            int rotation = pageData.getRotation(actualPage);

            if(allowDifferentPrintPageSizes){ //keep old mode for Rog
                //swap round if needed
                if(rotation==90 || rotation==270){
                    int tmp=crw;
                    crw=crh;
                    crh=tmp;
                }

                if(crw>crh){
                    int tmp=crw;
                    crw=crh;
                    crh=tmp;

                    if(rotation==90)
                        pageFormats.put("Align-"+actualPage, "inverted");

                }
            }

            createCustomPaper(pf, crw,crh);

        }else if (returnValue != null)
            pf = (PageFormat) returnValue;


        if (!isPrintAutoRotateAndCenter) {

            pf.setOrientation(PageFormat.PORTRAIT);

        } else {
            //int crw = pageData.getCropBoxWidth(actualPage);
            //int crh = pageData.getCropBoxHeight(actualPage);

            //Set PageOrientation to best use page layout
            //int orientation = crw < crh ? PageFormat.PORTRAIT: PageFormat.LANDSCAPE;
            //pf.setOrientation(orientation);

        }

        if (debugPrint){
            System.out.println("\n------Page format used="+pf);
            System.out.println("Orientation="+pf.getOrientation());
            System.out.println("Width="+pf.getWidth()+" imageableW="+pf.getImageableWidth());
            System.out.println("Height="+pf.getHeight()+" imageableH="+pf.getImageableHeight());
        }

        //System.out.println(usePDFPaperSize+" "+pf);

        return pf;
    }


    /**
     * set pageformat for a specific page - if no pageFormat is set a default
     * will be used. Recommended to use setPageFormat(PageFormat pf)
     */
    public void setPageFormat(int p, PageFormat pf) {

        if (debugPrint)
            System.out.println("Set page format for page " + p);

        pageFormats.put(new Integer(p), pf);

    }

    /**
     * set pageformat for a specific page - if no pageFormat is set a default
     * will be used.
     */
    public void setPageFormat(PageFormat pf) {

        if (debugPrint){
            System.out.println("Set page format Standard for page");
            System.out.println("---------------------------------");
            System.out.println("Page format used="+pf);
            System.out.println("Orientation="+pf.getOrientation());
            System.out.println("Width="+pf.getWidth()+" imageableW="+pf.getImageableWidth());
            System.out.println("Height="+pf.getHeight()+" imageableH="+pf.getImageableHeight());
            System.out.println("---------------------------------");

        }

        pageFormats.put("standard", pf);

    }
    // <end-me>

    /**
     * shows if text extraction is XML or pure text
     */
    public static boolean isXMLExtraction() {

        return isXMLExtraction;
    }

    /**
     * XML extraction is the default - pure text extraction is much faster
     */
    public static void useTextExtraction() {

        isXMLExtraction = false;
    }

    /**
     * XML extraction is the default - pure text extraction is much faster
     */
    public static void useXMLExtraction() {

        isXMLExtraction = true;
    }

    /**
     * remove all displayed objects for JPanel display (wipes current page)
     */
    public void clearScreen() {
        currentDisplay.flush();
        pages.refreshDisplay();
    }

    /**
     * allows user to cache large objects to disk to avoid memory issues,
     * setting minimum size in bytes (of uncompressed stream) above which object
     * will be stored on disk if possible (default is -1 bytes which is all
     * objects stored in memory) - Must be set before file opened.
     *
     */
    public void setStreamCacheSize(int size) {
        this.minimumCacheSize = size;
    }

    /**
     * used to display non-PDF files
     */
    public void addImage(BufferedImage img) {
    	GraphicsState gs = new GraphicsState();
    	gs.CTM[0][0] = img.getWidth();
    	gs.CTM[1][1] = img.getHeight();
    	currentDisplay.drawImage(1, img, gs, false, "image", PDFImageProcessing.NOTHING, -1);
    }

    /**
     * shows if embedded fonts present on page just decoded
     */
    public boolean hasEmbeddedFonts() {
        return hasEmbeddedFonts;
    }

    /**
     * shows if whole document contains embedded fonts and uses them
     */
    final public boolean PDFContainsEmbeddedFonts() throws Exception {

        boolean hasEmbeddedFonts = false;

        /**
         * scan all pages
         */
        for (int page = 1; page < pageCount + 1; page++) {

            /** get pdf object id for page to decode */
            String currentPageOffset = (String) pagesReferences.get(new Integer(page));

            /**
             * decode the file if not already decoded, there is a valid object
             * id and it is unencrypted
             */
            if ((currentPageOffset != null)) {

                //@speed
                /** read page or next pages */
                PdfObject pdfObject=new PageObject(currentPageOffset);
                pdfObject.ignoreStream(true);
                currentPdfFile.readObject(pdfObject);
                PdfObject Resources=pdfObject.getDictionary(PdfDictionary.Resources);

                /** get information for the page */
                byte[][] pageContents= pdfObject.getKeyArray(PdfDictionary.Contents);

                if (pageContents != null) {

                    PdfStreamDecoder current = new PdfStreamDecoder(currentPdfFile, Resources);
                    current.setParameters(true, renderPage, renderMode, extractionMode);

                    current.setObjectValue(ValueTypes.ImageHandler, customImageHandler);
                    current.setObjectValue(ValueTypes.PDFData,pageData);
                    current.setIntValue(ValueTypes.PageNum, page);
                    current.setObjectValue(ValueTypes.DynamicVectorRenderer,currentDisplay);

                    if (globalResources != null){
                        currentPdfFile.checkResolved(globalResources);
                        current.readResources(globalResources,true);

                        PdfObject propObj=globalResources.getDictionary(PdfDictionary.Properties);
                        if(propObj!=null)
                            PropertiesObj=propObj;
                    }

                    /**read the resources for the page*/
                    if (Resources != null){
                        currentPdfFile.checkResolved(Resources);
                        current.readResources(Resources,true);

                        PdfObject propObj=Resources.getDictionary(PdfDictionary.Properties);
                        if(propObj!=null)
                            PropertiesObj=propObj;
                    }

                    setupPage(current, false);

                    hasEmbeddedFonts = current.getBooleanValue(ValueTypes.EmbeddedFonts);

                    // exit on first true
                    if (hasEmbeddedFonts)
                        page = this.pageCount;
                }
            }
        }

        return hasEmbeddedFonts;
    }

    /**
     * Returns list of the fonts used on the current page decoded or null
     * type can be PdfDictionary.Font or PdfDictionary.Image
     */
    public String getInfo(int type) {

        String returnValue = null;

        switch (type) {
            case PdfDictionary.Font:

                if (fontsInFile == null) {
                    returnValue = "No fonts defined";
                } else {
                    returnValue = fontsInFile;
                }

                break;

            case PdfDictionary.Image:

                if (imagesInFile == null) {
                    returnValue = "No images defined as XObjects";
                } else {
                    returnValue = imagesInFile;
                }

                break;

            default:
                returnValue = null;
        }

        return returnValue;

    }

    /**
     * Returns list of the fonts used on the current page decoded
     * @deprecated Please use getInfo(PdfDictionary.Font)
     */
    public String getFontsInFile() {
        if (fontsInFile == null)
            return "No fonts defined";
        else
            return fontsInFile;
    }

    /**
     * include image data in PdfData - <b>not part of API, please do not use</b>
     *
     public void includeImagesInStream() {
     includeImages = true;
     } /**/

    //<start-adobe>
    /**
     * return lines on page after decodePage run - <b>not part of API, please do
     * not use</b>
     *
     public PageLines getPageLines() {
     return this.pageLines;
     } /**/
    //<end-adobe>

    /**
     * if <b>true</b> uses the original jpeg routines provided by sun, else
     * uses the imageIO routine in java 14 which is default<br>
     * only required for PDFs where bug in some versions of ImageIO fails to
     * render JPEGs correctly
     *
     public void setEnableLegacyJPEGConversion(boolean newjPEGConversion) {

     use13jPEGConversion = newjPEGConversion;
     } /**/

    /**
     * used to update statusBar object if exists
     */
    private class ProgressListener implements ActionListener {

        public void actionPerformed(ActionEvent evt) {

//            System.out.println("statusBar.percentageDone = " + statusBar.percentageDone);

            statusBar.setProgress((int) (statusBar.percentageDone));
        }

    }

    /**
     * Allow user to access Forms renderer - returns null not available
     * (should not generally be needed)
     */
    public AcroRenderer getFormRenderer() {
        return formRenderer;
    }

    /**
     * shows if page reported any errors while printing. Log
     * can be found with getPageFailureMessage()
     *
     * @return Returns the printingSuccessful.
     */
    public boolean isPageSuccessful() {
        return operationSuccessful;
    }

    /**
     * return any errors or other messages while calling decodePage() - zero
     * length is no problems
     */
    public String getPageDecodeReport() {
        return decodeStatus;
    }

    /**
     * Return String with all error messages from last printed (useful for
     * debugging)
     */
    public String getPageFailureMessage() {
        return pageErrorMessages;
    }

    /**
     * If running in GUI mode, will extract a section of rendered page as
     * BufferedImage -coordinates are PDF co-ordinates. If you wish to use hires
     * image, you will need to enable hires image display with
     * decode_pdf.useHiResScreenDisplay(true);
     *
     * @param t_x1
     * @param t_y1
     * @param t_x2
     * @param t_y2
     * @param scaling
     * @return pageErrorMessages - Any printer errors
     */
    public BufferedImage getSelectedRectangleOnscreen(float t_x1, float t_y1,
                                                      float t_x2, float t_y2, float scaling) {

        /** get page sizes */
        //int mediaBoxW = pageData.getMediaBoxWidth(pageNumber);
        int mediaBoxH = pageData.getMediaBoxHeight(pageNumber);
        //int mediaBoxX = pageData.getMediaBoxX(pageNumber);
        //int mediaBoxY = pageData.getMediaBoxY(pageNumber);
        int crw = pageData.getCropBoxWidth(pageNumber);
        int crh = pageData.getCropBoxHeight(pageNumber);
        int crx = pageData.getCropBoxX(pageNumber);
        int cry = pageData.getCropBoxY(pageNumber);

        // check values for rotated pages
        if (t_y2 < cry)
            t_y2 = cry;
        if (t_x1 < crx)
            t_x1 = crx;
        if (t_y1 > (crh + cry))
            t_y1 = crh + cry;
        if (t_x2 > (crx + crw))
            t_x2 = crx + crw;

        if ((t_x2 - t_x1) < 1 || (t_y1 - t_y2) < 1)
            return null;

        float scalingFactor = scaling / 100;
        float imgWidth = t_x2 - t_x1;
        float imgHeight = t_y1 - t_y2;

        /**
         * create the image
         */
        BufferedImage img = new BufferedImage((int) (imgWidth * scalingFactor),
                (int) (imgHeight * scalingFactor), BufferedImage.TYPE_INT_RGB);

        Graphics2D g2 = img.createGraphics();

        /**
         * workout the scaling
         */

        if (cry > 0)// fix for negative pages
            cry = mediaBoxH - crh - cry;

        // use 0 for rotated extraction
        AffineTransform scaleAf = getScalingForImage(pageNumber, 0, scalingFactor);// (int)(mediaBoxW*scale),
        // (int)(mediaBoxH*scale),
        int cx = -crx, cy = -cry;

        scaleAf.translate(cx, -cy);
        scaleAf.translate(-(t_x1 - crx), mediaBoxH - t_y1 - cry);

        AffineTransform af = g2.getTransform();

        g2.transform(scaleAf);

        currentDisplay.setG2(g2);
        currentDisplay.paintBackground(new Rectangle(crx, cry, crw, crh));

        currentDisplay.setOptimsePainting(true); //ensure drawn
        currentDisplay.paint(null, null, null);


        /**
         * draw acroform data onto Panel
         */
        if (formRenderer != null && formRenderer.hasFormsOnPage(pageNumber)) {

            formRenderer.getCompData().renderFormsOntoG2(g2, pageNumber, scaling, 0,
                    this.displayRotation, null,null, currentPdfFile, pageData.getMediaBoxHeight(pageNumber));
            formRenderer.getCompData().resetScaledLocation(oldScaling, displayRotation, 0);
        }

        // set up page hotspots
//        if (!showAnnotations && displayHotspots != null){
//            PdfAnnots aa=getPdfAnnotsData(formRenderer);
//            if(aa!=null)
//            displayHotspots.setHotspots(aa);
//        }
        g2.setTransform(af);


        g2.dispose();

        return img;
    }

    /**
     * return object which provides access to file images and name
     */
    public ObjectStore getObjectStore() {
        return objectStoreRef;
    }

    /**
     * return object which provides access to file images and name (use not
     * recommended)
     */
    public void setObjectStore(ObjectStore newStore) {
        objectStoreRef = newStore;
    }

    //<start-adobe>
    /**
     * returns object containing grouped text of last decoded page
     * - if no page decoded, a Runtime exception is thrown to warn user
     * Please see org.jpedal.examples.text for example code.
     *
     */
    public PdfGroupingAlgorithms getGroupingObject() throws PdfException {

        if(lastPageDecoded==-1){

            throw new RuntimeException("No pages decoded - call decodePage(pageNumber) first");

        }else{
            PdfData textData = getPdfData();
            if (textData == null)
                return null;
            else
                return new PdfGroupingAlgorithms(textData, pageData);
        }
    }

    /**
     * returns object containing grouped text from background grouping - Please
     * see org.jpedal.examples.text for example code
     */
    public PdfGroupingAlgorithms getBackgroundGroupingObject() {

        PdfData textData = this.pdfBackgroundData;
        if (textData == null)
            return null;
        else
            return new PdfGroupingAlgorithms(textData, pageData);
    }


    //<end-adobe>




    /**
     * get PDF version in file
     */
    final public String getPDFVersion() {
        return pdfVersion;
    }


    //<start-adobe>

    /**
     * used for non-PDF files to reset page
     */
    public void resetForNonPDFPage() {

        displayScaling = null;

        /** set hires mode or not for display */
        currentDisplay.setHiResImageForDisplayMode(false);

        fontsInFile = "";
        pageCount = 1;
        hasOutline = false;

        if (formRenderer != null)
            formRenderer.removeDisplayComponentsFromScreen();
        //invalidate();

        // reset page data
        this.pageData = new PdfPageData();
    }
    //<end-adobe>

    /**
     * provides details on printing to enable debugging info for IDRsolutions
     */
    public static void setDebugPrint(boolean newDebugPrint) {
        debugPrint = newDebugPrint;
    }


    //<start-adobe>
    /**
     * set view mode used in panel and redraw in new mode
     * SINGLE_PAGE,CONTINUOUS,FACING,CONTINUOUS_FACING delay is the time in
     * milli-seconds which scrolling can stop before background page drawing
     * starts
     * Multipage views not in OS releases
     */
	public void setDisplayView(final int displayView, final int orientation) {

		//ensure method is correctly accessed
		if (SwingUtilities.isEventDispatchThread()) {

			setDisplayViewx(displayView, orientation);

		} else {
			final Runnable doPaintComponent = new Runnable() {

				public void run() {
					setDisplayViewx(displayView,orientation);
				}
			};
			SwingUtilities.invokeLater(doPaintComponent);
		}
	}

    private void setDisplayViewx(int displayView, int orientation) {


        this.alignment = orientation;

        if (pages != null)
            pages.stopGeneratingPage();
      
        boolean needsReset = (displayView != Display.SINGLE_PAGE || this.displayView != Display.SINGLE_PAGE);
        if (needsReset && (this.displayView == Display.FACING || displayView == Display.FACING))
            needsReset = false;

        if (displayView != Display.SINGLE_PAGE)
            needsReset = true;

        boolean hasChanged = displayView != this.displayView;

        //log what we are changing from
        int lastDisplayView=this.displayView;

        this.displayView = displayView;


        //<start-thin>
        switch (displayView) {
            case Display.SINGLE_PAGE:
                if(pages==null || hasChanged){
                    pages = new SingleDisplay(pageNumber, pageCount, currentDisplay);

                    //reset form data offsets
                    //this.formRenderer.getCompData().setPageDisplacements(null,null);
                }
                break;


        }

        //<end-thin>
        /***/

        // remove listener if setup
        if (hasViewListener) {
            hasViewListener = false;

            removeComponentListener(viewListener);

        }



        /**
         * setup once per page getting all page sizes and working out settings
         * for views
         */
        if (currentOffset == null)
            currentOffset = new PageOffsets(pageCount, pageData);

        pages.setup(useAcceleration, currentOffset, this);
        pages.init(scaling, pageCount, displayRotation, pageNumber, currentDisplay, true, pageData, insetW, insetH);

        // force redraw
        lastFormPage = -1;
        lastEnd = -1;
        lastStart = -1;

        pages.refreshDisplay();
		
        // <start-me>
        //brreaks image tests (ie hangs and does not appear needed so removed by Mark 20110217)
        //updateUI();
        // <end-me>
        
        // add listener if one not already there
        if (!hasViewListener) {
            hasViewListener = true;
            addComponentListener(viewListener);
        }

        //move to correct page
        if (pageNumber > 0) {
            if (hasChanged && displayView == Display.SINGLE_PAGE) {
                try {
                    unsetScaling();
                    setPageParameters(scaling, pageNumber, displayRotation);
                    invalidate();
                    // <start-me>
                    updateUI();
                    // <end-me>
                    decodePage(pageNumber);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (displayView != Display.SINGLE_PAGE
                    // <start-me>
                    && displayView != Display.PAGEFLOW3D
                // <end-me>
                    ) {

                 throw new RuntimeException("Only SINGLE_PAGE is available in LGPL release");
                 /**/
            }
        }
    }
    //<end-adobe>

    /**
     * Class to handle pageflow scrollbar
     */
    private class ScrollListener implements AdjustmentListener {

        java.util.Timer t = null;
        int pNum=0;

        private void startTimer() {
            //turn if off if running
            if (t != null)
                t.cancel();

            //restart - if its not stopped it will trigger page update
            TimerTask listener = new PageListener();
            t = new java.util.Timer();
            t.schedule(listener, 100);
        }

        /**
         * Check for scrollbar activity and pick up the new page number
         */
        public void adjustmentValueChanged(AdjustmentEvent e) {
            pages.stopGeneratingPage();
            pages.drawBorder();
            pNum=e.getAdjustable().getValue()+1;
            startTimer();
        }


        /**
         * used to update statusBar object if exists
         */
        class PageListener extends TimerTask {
            public void run() {
                if (Display.debugLayout)
                    System.out.println("ActionPerformed " + pageCount);

                pages.stopGeneratingPage();
//                pages.drawBorder();
                
                //Ensure page range does not drop below one
                if(pNum<1)
                	pNum = 1;
                
                pages.decodeOtherPages(pNum, pageCount);
            }
        }
    }




    /**
     * return page currently being printed or -1 if finished
     */
    public int getCurrentPrintPage() {
        return currentPrintPage;
    }

    public void resetCurrentPrintPage() {
        currentPrintPage = 0;

        this.formRenderer.getCompData().resetAfterPrinting();

    }

    /**
     * flag to show if we suspect problem with some images
     */
    public boolean hasAllImages() {
        return imagesProcessedFully;
    }

    /**
     * allow user to set certain paramters - only supports DecodeStatus.Timeout at present
     * @param status
     * @param value
     */
    public void setPageDecodeStatus(int status, Object value) {

        if(status==(DecodeStatus.Timeout)){
            if(value instanceof Boolean){

                boolean timeout=((Boolean)value).booleanValue();
                if(timeout && current!=null)
                    current.reqestTimeout(null);
                if(timeout && currentImageDecoder!=null)
                    currentImageDecoder.reqestTimeout(null);

            }else if(value instanceof Integer){

                if(current!=null)
                    current.reqestTimeout(value);
                if(currentImageDecoder!=null)
                    currentImageDecoder.reqestTimeout(value);

            }
        }else
            new RuntimeException("Unknown parameter");
    }

    public boolean getPageDecodeStatus(int status) {


        /**if(status.equals(DecodeStatus.PageDecodingSuccessful))
         return pageSuccessful;
         else*/ if(status==(DecodeStatus.NonEmbeddedCIDFonts)){
            return hasNonEmbeddedCIDFonts;
        }else if(status==(DecodeStatus.ImagesProcessed))
            return imagesProcessedFully;
        else if(status==(DecodeStatus.Timeout))
            return timeout;
        else if(status==(DecodeStatus.YCCKImages))
            return hasYCCKimages;
        else if(status==(DecodeStatus.TTHintingRequired))
            return ttHintingRequired;
        else
            new RuntimeException("Unknown parameter");

        return false;
    }

    /**
     * get page statuses
     */
    public String getPageDecodeStatusReport(int status) {

        if(status==(DecodeStatus.NonEmbeddedCIDFonts)){
            return nonEmbeddedCIDFonts;
        }else
            new RuntimeException("Unknown parameter");

        return "";
    }

    /**
     * set print mode (Matches Abodes Auto Print and rotate output
     */
    public void setPrintAutoRotateAndCenter(boolean value) {
        isPrintAutoRotateAndCenter = value;

    }

    public void setPrintCurrentView(boolean value) {
        this.printOnlyVisible = value;
    }

    /**
     * allows external helper classes to be added to JPedal to alter default functionality -
     * not part of the API and should be used in conjunction with IDRsolutions only
     * <br>if Options.FormsActionHandler is the type then the <b>newHandler</b> should be
     * of the form <b>org.jpedal.objects.acroforms.ActionHandler</b>
     *
     * @param newHandler
     * @param type
     */
    public void addExternalHandler(Object newHandler, int type) {
//		System.out.println("PdfDecoder.addExternalHandler()");
        switch (type) {

            case Options.SwingContainer:
                swingGUI = newHandler;
                break;

            case Options.ImageHandler:
                customImageHandler = (ImageHandler) newHandler;
                break;

            case Options.ColorHandler:
                customColorHandler = (ColorHandler) newHandler;
                break;


            case Options.Renderer:
                //cast and assign here
                break;

            case Options.FormFactory:
                formRenderer.setFormFactory((FormFactory) newHandler);
                break;


            case Options.MultiPageUpdate:
                customSwingHandle = newHandler;
                break;


            case Options.LinkHandler:

                if (formRenderer != null)
                    formRenderer.resetHandler(newHandler, this,Options.LinkHandler);

                break;

            case Options.FormsActionHandler:

                if (formRenderer != null)
                    formRenderer.resetHandler(newHandler, this,Options.FormsActionHandler);

                break;

            //<start-thin><start-adobe>// <start-me>
            case Options.SwingMouseHandler:
                if(formRenderer != null){
                    //if(formRenderer.getFormFactory().getType()==FormFactory.SWING)
                    formRenderer.getActionHandler().setMouseHandler((SwingMouseListener) newHandler);
                }
                break;


            case Options.ThumbnailHandler:
                pages.setThumbnailPanel((org.jpedal.examples.simpleviewer.gui.generic.GUIThumbnailPanel) newHandler);
                break;
            //<end-adobe><end-thin>// <end-me>

            case Options.JPedalActionHandler:
                jpedalActionHandlers = (Map) newHandler;
                break;

            case Options.CustomMessageOutput:
                customMessageHandler = (CustomMessageHandler) newHandler;
                break;

            case Options.RenderChangeListener:
                customRenderChangeListener = (RenderChangeListener) newHandler;
                break;

            case Options.CustomPrintHintingHandler:
                customPrintHintingHandler = (CustomPrintHintingHandler) newHandler;
                break;

            case Options.CustomOutput:
                customDVR = (DynamicVectorRenderer) newHandler;

                //<start-std>
                //<start-pro>
                /**
                //<end-pro>
                if(customDVR.getType()==DynamicVectorRenderer.CREATE_HTML){
                    FormFactory HTMLFormFactory=new org.jpedal.examples.html.HTMLFormFactory(customDVR);
                    HTMLFormFactory.setDecoder(this);
                    addExternalHandler(HTMLFormFactory, Options.FormFactory); //custom object to draw Forms
                }
                /**/
                //<end-std>
                break;

            default:
                throw new IllegalArgumentException("Unknown type "+type);

        }
    }

    /**
     * allows external helper classes to be accessed if needed - also allows user to access SwingGUI if running
     * full Viewer package - not all Options available to get - please contact IDRsolutions if you are looking to
     * use
     *
     * @param type
     */
    public Object getExternalHandler(int type) {

        switch (type) {
            case Options.ImageHandler:
                return customImageHandler;

            case Options.ColorHandler:
                return customColorHandler;


            case Options.SwingContainer:
                return swingGUI;

            case Options.Renderer:
                return null;

            case Options.FormFactory:
                return formRenderer.getFormFactory();

            case Options.MultiPageUpdate:
                return customSwingHandle;


            // case Options.LinkHandler:
            //   break;

//            case Options.FormsActionHandler:
//
//                if (formRenderer != null)
//                    formRenderer.resetHandler(newHandler, this,Options.FormsActionHandler);
//
//                break;

            //<start-thin><start-adobe>
//            case Options.ThumbnailHandler:
//                pages.setThumbnailPanel((org.jpedal.examples.simpleviewer.gui.generic.GUIThumbnailPanel) newHandler);
//                break;
            //<end-adobe><end-thin>

            case Options.JPedalActionHandler:
                return jpedalActionHandlers;

            case Options.CustomMessageOutput:
                return customMessageHandler;

            case Options.Display:
                return pages;

            case Options.CurrentOffset:
                return currentOffset;

            case Options.CustomOutput:
                return customDVR;

            default:
                throw new IllegalArgumentException("Unknown type");

        }
    }

    /**
     * used internally by multiple pages
     * scaling -1 to ignore, -2 to force reset
     */
    public int getYCordForPage(int page, float scaling) {

        if (scaling == -2 || (scaling != -1f && scaling != oldScaling)) {
            oldScaling = scaling;
            pages.setPageOffsets(this.pageCount, page);

            //System.out.println("xxxxxxx  RESET xxxxxxxxxxx "+scaling);
        }
        return pages.getYCordForPage(page);
    }

    public void unsetScaling() {

        displayScaling = null;


    }

    /**
     * used internally by multiple pages
     * scaling -1 to ignore, -2 to force reset
     */
    public int getXCordForPage(int page, float scaling) {
        // <start-me>
        //Update scrollbar for pageFlow
        if (displayView == Display.PAGEFLOW) {
            scroll.setValue(page-1);
            return 0;
        } else // <end-me>
        {
            if (scaling == -2 || (scaling != -1f && scaling != oldScaling)) {
                oldScaling = scaling;
                pages.setPageOffsets(this.pageCount, page);

                //System.out.println("xxxxxxx  RESET xxxxxxxxxxx "+scaling);
            }
            return pages.getXCordForPage(page);
        }
    }



    /**
     * return PDF data object or Objects
     * for field containing values from PDF file
     *
     * This will take either the Name or the PDFref
     *
     * (ie Box or 12 0 R)
     *
     * This can return an object[] if Box is a radio button with multiple
     * vales so you need to check instanceof Object[] on data

     * In the case of a PDF with radio buttons Box (12 0 R), Box (13 0 R), Box (14 0 R)
     * getFormDataAsObject(Box) would return an Object which is actually Object[3]
     * getFormDataAsObject(12 0 R) would return an Object which is a single value
     *

     */
    public Object[] getFormDataForField(String formName) {

        Object[] formData=null;

        //test first then form if no value
        if (formRenderer != null)
            formData= formRenderer.getFormDataAsObject(formName);

        return formData;
    }

    /**
     * return full list of Fields for Annots and Forms
     */
    public Set getNamesForAllFields() throws PdfException {

        if(formRenderer==null){

            System.out.println("================No DATA=====================");
            return new HashSet();
        }

        Set set = new HashSet();

        List forms = formRenderer.getComponentNameList();

        if (forms != null)
            set.addAll(forms);

        return set;

    }

    /**
     * return swing widget regardless of whether it came from Annot or form
     * -1 if not found values in FormFactory (ie UNKNOWN)
     *
     *
     *  use decode_pdf.getFormRenderer().getCompData().getTypeValueByName(name);
     *
     public Integer getFormComponentType(String name) {

     Integer type=FormFactory.UNKNOWN;

     if(formRenderer!=null)
     type= formRenderer.getCompData().getTypeValueByName(name);

     return type;

     } /**/

    /**
     * return swing widget regardless of whether it came from Annot or form
     */
//	public Object[] getFormComponent(String name) {

//	Component[] comps = null;

//	if(formRenderer!=null)
//	comps= (Component[]) formRenderer.getComponentsByName(name);

//	return comps;

//	}


    public PdfObjectReader getIO() {
        return currentPdfFile;
    }



    public boolean isThumbnailsDrawing() {
        return thumbnailsBeingDrawn;
    }

    public void setPageCount(int numPages) {
        pageCount = numPages;
    }

    public boolean isPDF() {
        return isPDf;
    }

    public void setPDF(boolean isPDf) {
        this.isPDf = isPDf;
    }

    public boolean isMultiPageTiff() {
        return isMultiPageTiff;
    }

    public void setMultiPageTiff(boolean isMultiPageTiff) {
        this.isMultiPageTiff = isMultiPageTiff;
    }

    public String getFileName() {
        return filename;
    }

    public int getObjectUnderneath(int x, int y) {
        if(!extractingAsImage && displayView==Display.SINGLE_PAGE){
            int type = currentDisplay.getObjectUnderneath(x, y);
            switch(type){
                case -1 :
                    this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                    break;
                case DynamicVectorRenderer.TEXT :
                    this.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
                    break;
                case DynamicVectorRenderer.IMAGE :
                    this.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
                    break;
                case DynamicVectorRenderer.TRUETYPE :
                    this.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
                    break;
                case DynamicVectorRenderer.TYPE1C :
                    this.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
                    break;
                case DynamicVectorRenderer.TYPE3 :
                    this.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
                    break;
            }
            return type;
        }
        return -1;
    }

    /**
     * set up our font replacement values
     * @param decode_pdf
     */
    public static void setFontReplacements(PdfDecoder decode_pdf) {

        //@sam-fonts
        //this is where we setup specific font mapping to use fonts on local machines
        //note different settigns for Win, linux, MAC

        //general
        String[] aliases6={/**"AcArial"};//,/**/"acarialunicodems__cn"};//,"acarial,bold"};
        decode_pdf.setSubstitutedFontAliases("adobeheitistd-regular",aliases6);

        //platform settings
        if(PdfDecoder.isRunningOnMac){

            //Courier (CourierNew) both on Mac and different
            decode_pdf.setSubstitutedFontAliases("Courier italic",new String[]{"Courier-Oblique"});
            decode_pdf.setSubstitutedFontAliases("Courier bold",new String[]{"Courier-Bold"});
            decode_pdf.setSubstitutedFontAliases("Courier bold italic",new String[]{"Courier-BoldOblique"});

            decode_pdf.setSubstitutedFontAliases("Courier new italic",new String[]{"CourierNew,italic"});
            decode_pdf.setSubstitutedFontAliases("Courier new bold",new String[]{"CourierNew,Bold","Courier-Bold"});
            decode_pdf.setSubstitutedFontAliases("Courier new bold italic",new String[]{"CourierNew-BoldOblique"});
            decode_pdf.setSubstitutedFontAliases("Courier new",new String[]{"CourierNew","Courier"});

            //Helvetica (Arial)
            decode_pdf.setSubstitutedFontAliases("arial",new String[]{"Helvetica","arialmt"});
            decode_pdf.setSubstitutedFontAliases("arial italic",new String[]{"arial-italic", "arial-italicmt","Helvetica-Oblique","Arial,Italic"});
            decode_pdf.setSubstitutedFontAliases("arial bold",new String[]{"arial-boldmt","Helvetica-Bold","Arial,bold"});
            decode_pdf.setSubstitutedFontAliases("arial bold italic",new String[]{"Arial-BoldItalicMT"});

            //Arial Narrow - not actually one of fonts but  very common so added
            decode_pdf.setSubstitutedFontAliases("arial Narrow",new String[]{"ArialNarrow",});  //called ArialNarrow in PDF, needs to be arialn for Windows
            decode_pdf.setSubstitutedFontAliases("arial Narrow italic",new String[]{"ArialNarrow-italic"});
            decode_pdf.setSubstitutedFontAliases("arial Narrow bold",new String[]{"ArialNarrow-bold"});
            decode_pdf.setSubstitutedFontAliases("arial Narrow bold italic",new String[]{"ArialNarrow-bolditalic"});

            //Times/TimesNewRoman
            decode_pdf.setSubstitutedFontAliases("times new roman bold",new String[] {"Times-Bold","TimesNewRoman,Bold","TimesNewRomanPS-BoldMT"});
            decode_pdf.setSubstitutedFontAliases("times new roman bold italic",new String[] {"Times-BoldItalic","TimesNewRoman,BoldItalic","TimesNewRomanPS-BoldItalicMT"});
            decode_pdf.setSubstitutedFontAliases("times new roman italic",new String[] {"Times-Italic","TimesNewRoman,Italic","TimesNewRomanPS-ItalicMT"});
            decode_pdf.setSubstitutedFontAliases("times new roman",new String[] {"Times-Roman","TimesNewRoman","Times","TimesNewRomanPSMT"});


            decode_pdf.setSubstitutedFontAliases("wingdings",new String[] {"ZapfDingbats","ZaDb"});

            //default at present for others as well
        }else {//if(PdfDecoder.isRunningOnWindows){

            //Courier (CourierNew)
            decode_pdf.setSubstitutedFontAliases("Couri",new String[]{"Courier-Oblique", "CourierNew,italic"});
            decode_pdf.setSubstitutedFontAliases("Courbd",new String[]{"Courier-Bold","CourierNew,Bold"});
            decode_pdf.setSubstitutedFontAliases("Courbi",new String[]{"Courier-BoldOblique","CourierNew-BoldOblique"});
            decode_pdf.setSubstitutedFontAliases("Cour",new String[]{"CourierNew","Courier"});

            //Helvetica (Arial)
            decode_pdf.setSubstitutedFontAliases("arial",new String[]{"Helvetica","arialmt"});
            decode_pdf.setSubstitutedFontAliases("ariali",new String[]{"arial-italic", "arial-italicmt","Helvetica-Oblique","Arial,Italic"});
            decode_pdf.setSubstitutedFontAliases("arialbd",new String[]{"arial-boldmt","Helvetica-Bold","Arial,bold","arial bold"});

            //Arial Narrow - not actually one of fonts but  very common so added
            decode_pdf.setSubstitutedFontAliases("arialn",new String[]{"ArialNarrow",}); //called ArialNarrow in PDF, needs to be arialn for Windows
            decode_pdf.setSubstitutedFontAliases("arialni",new String[]{"ArialNarrow-italic"});
            decode_pdf.setSubstitutedFontAliases("arialnb",new String[]{"ArialNarrow-bold"});
            decode_pdf.setSubstitutedFontAliases("arialnbi",new String[]{"ArialNarrow-bolditalic"});

            //Times/TimesNewRoman
            decode_pdf.setSubstitutedFontAliases("timesbd",new String[] {"Times-Bold","TimesNewRoman,Bold","TimesNewRomanPS-BoldMT"});
            decode_pdf.setSubstitutedFontAliases("timesi",new String[] {"Times-BoldItalic","TimesNewRoman,BoldItalic"});
            decode_pdf.setSubstitutedFontAliases("timesbi",new String[] {"Times-Italic","TimesNewRoman,Italic"});
            decode_pdf.setSubstitutedFontAliases("times",new String[] {"Times-Roman","TimesNewRoman","Times","TimesNewRomanPSMT"});

            decode_pdf.setSubstitutedFontAliases("wingdings",new String[] {"ZapfDingbats","ZaDb"});

        }

        //@sam-fonts
        //locations where fonts may be found

        //set general mappings for non-embedded fonts (assumes names the same)
        PdfDecoder.setFontDirs(new String[]{"C:/windows/fonts/","C:/winNT/fonts/",
                "/System/Library/Fonts/","/Library/Fonts/",
                "/usr/share/fonts/truetype/msttcorefonts/",
                //"/usr/share/fonts/truetype/",
                //"/windows/D/Windows/Fonts/"
        });

    }


    public Map getJPedalActionHandlers() {
        return jpedalActionHandlers;
    }



    public boolean isForm() {
        return acroFormObj!=null;
    }

    /**
     * handles download of rest of file in Linearized mode
     */
    public class LinearThread extends Thread {

        public int percentageDone=0;

        // <start-me>
        FileChannel fos;
        // <end-me>
        PdfObject linearObj;
        InputStream is;
        File tempURLFile;
        LinearizedHintTable linHintTable;

        final byte[] startObj="obj".getBytes(),endObj="endobj".getBytes();
        int startCharReached=0, endCharReached=0;
        int startObjPtr=0,endObjPtr=0;

        //use top line to slow down load speed
        //int bufSize=300, lastBytes=300;
        int bufSize=8192, lastBytes=8192;

        int generation=0,ref=0,firstObjLength=0;

        // <start-me>
        public LinearThread(InputStream is, FileChannel fos, File tempURLFile, PdfObject linearObj, byte[] linearBytes, LinearizedHintTable linHintTable) {

            this.fos=fos;
            this.linearObj=linearObj;
            this.is=is;
            this.tempURLFile=tempURLFile;
            this.linHintTable=linHintTable;

            //scan start of file for objects
            firstObjLength=linearBytes.length;

            scanStreamForObjects(0, null, linearBytes);

        }
        // <end-me>

        public int getPercentageLoaded(){
            return percentageDone;
        }

        public void run() {

            final int linearfileLength=linearObj.getInt(PdfDictionary.L);

            try{

                int read,bytesRead=0;

                //we cache last few bytes incase ref rolls across boundary
                byte[] lastBuffer=new byte[lastBytes];

                byte[] buffer = new byte[bufSize];

                while ((read = is.read(buffer)) != -1 && !this.isInterrupted() && isAlive()) {

                	// <start-me>
                    if(read>0){
                        synchronized (fos){

                            byte[] b=new byte[read];
                            System.arraycopy(buffer,0,b,0,read);
                            buffer=b;
                            ByteBuffer f=ByteBuffer.wrap(b);
                            fos.write(f);
							}
                        }
                    // <end-me>

                    //track start endobj and flag so we know if object read
                    if(read>0){
                        scanStreamForObjects(firstObjLength+bytesRead, lastBuffer, buffer);

                        bytesRead=bytesRead+read;

                        //save last few bytes incase of overlap
                        int aa=30;

                        int size1=buffer.length;

                        if(aa>size1-1)
                            aa=size1-1;

                        lastBuffer=new byte[aa];
                        System.arraycopy(buffer, size1-aa, lastBuffer,0,aa);

                        //System.arraycopy(buffer,0,lastBuffer,bufSize-lastBytes,lastBytes);
                    }

                    percentageDone= (int) (100*((float)bytesRead/(float)linearfileLength));

                    //System.out.println(percentageDone);
                }

                linHintTable.setFinishedReading();


            } catch (IOException e) {
                e.printStackTrace();
            	
            } finally{

                try{
                    is.close();
                    //fos.close();

                    //possible that page still being decoded on slower machine so wait
                    waitForDecodingToFinish();
                    
                    pagesReferences.clear();
                    currentPdfFile = new PdfReader();

                    
                    
                    /** get reader object to open the file if all downloaded*/
	                if(isAlive() && !isInterrupted()){
	                   	openPdfFile(tempURLFile.getAbsolutePath());
	                        
	                   	/** store fi name for use elsewhere as part of ref key without .pdf */
	                   	objectStoreRef.storeFileName(tempURLFile.getName().substring(0, tempURLFile.getName().lastIndexOf('.')));
	                }
                	
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        }

        private void scanStreamForObjects(int bytesRead, byte[] lastBuffer, byte[] buffer) {

            int bufSize=buffer.length;

            for(int i=0;i<bufSize;i++){

                if(startCharReached==0){ //look for gap at start of obj
                    if(buffer[i]==' ' || buffer[i]==0 || buffer[i]==10 || buffer[i]==32 )
                        startCharReached++;
                }else if(startCharReached<4){ //look for rest of obj

                        if(buffer[i]==startObj[startCharReached-1]){

                            if(startCharReached==3){ //start found so read object ref and log start

                                startObjPtr=bytesRead+i-4;

                                //get the values
                                int ii=i-4;

                                byte[] data =null;

                                //add in last buffer to allow for crossing boundary
                                if(lastBuffer!=null && ii<30){

                                    int size1=lastBuffer.length;
                                    int size2=buffer.length;
                                    data =new byte[size1+size2];
                                    System.arraycopy(lastBuffer,0, data,0,size1);
                                    System.arraycopy(buffer,0, data,size1,size2);

                                    //System.out.println(lastBuffer.length+" old="+new String(lastBuffer));
                                    //System.out.println(buffer.length+" new="+new String(buffer));
                                    ii=ii+size1;

                                }else{
                                    data =buffer;
                                }

                                int keyEnd =ii;

                                //generation value
                                while(data[ii]!=10 && data[ii]!=13 && data[ii]!=32 && data[ii]!=9){
                                    ii--;
                                    startObjPtr--;
                                }

                                generation= NumberUtils.parseInt(ii + 1, keyEnd, data);

                                //roll back to start of number
                                while(data[ii]==10 || data[ii]==13 || data[ii]==32 || data[ii]==47 || data[ii]==60){
                                    ii--;
                                    startObjPtr--;
                                }

                                keyEnd =ii+1;

                                while(data[ii]!=10 && data[ii]!=13 && data[ii]!=32 && data[ii]!=47 && data[ii]!=60 && data[ii]!=62){
                                    ii--;
                                    startObjPtr--;
                                }

                                ref= NumberUtils.parseInt(ii + 1, keyEnd, data);

                            }

                            startCharReached++;
                        }else
                            startCharReached=0;

                }else{
                    if(buffer[i]==endObj[endCharReached]){
                        endCharReached++;

                        if(endCharReached==6){
                            endObjPtr=bytesRead+i;

                            //currentPdfFile.storeOffset()
                            linHintTable.storeOffset(ref,startObjPtr,endObjPtr);

                            startCharReached=0;
                            endCharReached=0;
                        }
                    }else
                        endCharReached=0;
                }
            }
        }
    }

	public void goBackAView() {
	}

	public void goForwardAView() {
	}

    /**
     * reference for Page object
     * @param page
     * @return String ref (ie 1 0 R)
     * pdfObject=new PageObject(currentPageOffset);
     * currentPdfFile.readObject(pdfObject);
     */
    public String getReferenceforPage(int page){
        return (String) pagesReferences.get(new Integer(page));
    }

}
