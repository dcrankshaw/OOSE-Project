package org.jpedal.render;

import java.awt.BasicStroke;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.jpedal.color.PdfPaint;
import org.jpedal.objects.GraphicsState;
import org.jpedal.objects.PdfPageData;

public class HTMLShape
{
    private static final boolean ENABLE_CROPPING = true;

    private List	pathCommands;//genetics not supported in java ME
    private int    			currentColor;
    private Rectangle		cropBox;
    private Point2D			midPoint;

    private double[]		lastVisiblePoint;
    private double[]		lastInvisiblePoint;
    private double[] 		previousPoint;
    private boolean 		isPathSegmentVisible;
    private double[]		entryPoint, exitPoint;

    //For rectangle larger than cropBox
    private List	        largeBox;
    private boolean			isLargeBox;
    private boolean			largeBoxSideAlternation; //Flag to check that lines are alternating between horizontal and vertical.

    static int debug = 0;

    Rectangle clipBox=null;

    //dev flag for mark (left in just in case we need to disable)
    private boolean fixShape=false;

    /**number of decimal places on shapes*/
    private int dpCount=0;

    private int pageRotation=0;

    //applied to the whole page. Default= 1
    private float scaling;

    private boolean debugPath=false;

    //flag to show if we need to convert Fill to Stroke to appear in HTML
    private boolean isSinglePixelFill;
    private PdfPageData pageData;
    private int pageNumber;
    
    int pathCommand;
    
    final boolean changeCoordsAtEnd=true;

    public HTMLShape(boolean fixShape,float scaling, Shape currentShape, GraphicsState gs, AffineTransform scalingTransform,
                     Point2D midPoint, Rectangle cropBox, int currentColor, int dpCount, int pageRotation, PdfPageData pageData, int pageNumber)
    {

    	fixShape=false;
    	//this.fixShape=fixShape;
    	
        if(fixShape){
            //get any Clip (only rectangle outline)
            Area clip=gs.getClippingShape();
            if(clip!=null)
                clipBox=clip.getBounds();
            else
                clipBox=null;


        }

        this.scaling = scaling;
        this.currentColor = currentColor;
        this.cropBox = cropBox;
        this.dpCount = dpCount;
        this.midPoint = midPoint;
        this.pageData = pageData;
        this.pageNumber=pageNumber;

        //flag if w or h is 1 so we can sub fill for stroke
        this.isSinglePixelFill=currentShape.getBounds().width<2 || currentShape.getBounds().height<2;

        this.pageRotation=pageRotation;

        isPathSegmentVisible = true;
        lastVisiblePoint = new double[2];
        lastInvisiblePoint = new double[2];
        previousPoint = new double[2];
        exitPoint = new double[2];
        entryPoint = new double[2];

        PathIterator it = currentShape.getPathIterator(scalingTransform);

        pathCommands = new ArrayList();
        largeBox = new ArrayList();
        isLargeBox = true; //Prove wrong

        pathCommands.add("pdf_context.beginPath();");
        boolean firstDrawCommand = true && ENABLE_CROPPING;

        if(debugPath){
            System.out.println("About to generate commands for shape with bounds" +currentShape.getBounds());
            System.out.println("------------------------------------------------" );
            System.out.println("crop bounds=" +cropBox.getBounds());
            System.out.println("shape bounds=" +currentShape.getBounds());
            if(clipBox!=null)
                System.out.println("clip bounds=" +clipBox.getBounds());
        }

        while(!it.isDone()) {
            double[] coords = {0,0,0,0,0,0};

            pathCommand = it.currentSegment(coords);

            if(debugPath)
                System.out.println("\nGet pathCommand segment "+coords[0]+" "+coords[1]+" "+coords[2]+" "+coords[3]+" "+coords[4]+" "+coords[5]);

            if(!changeCoordsAtEnd)
            coords=correctCoords(new double[]{coords[0],coords[1],coords[2],coords[3],coords[4],coords[5]});

            checkLargeBox(coords, pathCommand);

            if(firstDrawCommand) {
                isPathSegmentVisible = cropBox.contains(coords[getCoordOffset(pathCommand)], coords[getCoordOffset(pathCommand) + 1]) && clipBox!=null && clipBox.contains(coords[getCoordOffset(pathCommand)], coords[getCoordOffset(pathCommand) + 1]);
                firstDrawCommand = false;

                if(debugPath)
                    System.out.println("isPathSegmentVisible="+isPathSegmentVisible);

            }

            if (pathCommand == PathIterator.SEG_CUBICTO) {

                boolean isPointVisible = testDrawLimits(coords, PathIterator.SEG_CUBICTO);
                if(debugPath)
                    System.out.println("PathIterator.SEG_CUBICTO isPointVisible="+isPointVisible);

                pathCommands.add("pdf_context.bezierCurveTo(" + coordsToStringParam(coords, 6,changeCoordsAtEnd) + ");");

                //flag incase next item is a line
                isPathSegmentVisible = isPointVisible;

            }else if (pathCommand == PathIterator.SEG_LINETO) {
                boolean isPointVisible = testDrawLimits(coords, PathIterator.SEG_LINETO);

                if(debugPath)
                    System.out.println("PathIterator.SEG_LINETO isPointVisible="+isPointVisible);

                if(isPointVisible && isPathSegmentVisible) {
                    pathCommands.add("pdf_context.lineTo(" + coordsToStringParam(coords, 2,changeCoordsAtEnd) + ");");
                    if(debugPath)
                        System.out.println("pdf_context.lineTo(" + coordsToStringParam(coords, 2,changeCoordsAtEnd) + ");");
                }else if(isPointVisible != isPathSegmentVisible) {
                    if(!isPathSegmentVisible) {

                        if(gs.getFillType() != GraphicsState.FILL) {
                            pathCommands.add("pdf_context.moveTo(" + coordsToStringParam(entryPoint, 2,changeCoordsAtEnd) + ");");

                            if(debugPath)
                                System.out.println("pdf_context.moveTo(" + coordsToStringParam(entryPoint, 2,changeCoordsAtEnd));
                        }else {
                            pathCommands.add("pdf_context.lineTo(" + coordsToStringParam(entryPoint, 2,changeCoordsAtEnd) + ");");

                            if(debugPath)
                                System.out.println("pdf_context.lineTo(" + coordsToStringParam(entryPoint, 2,changeCoordsAtEnd)+")");

                        }

                        pathCommands.add("pdf_context.lineTo(" + coordsToStringParam(coords, 2,changeCoordsAtEnd) + ");");

                        if(debugPath)
                            System.out.println("pdf_context.lineTo(" + coordsToStringParam(entryPoint, 2,changeCoordsAtEnd)+");");

                        isPathSegmentVisible = true;
                    }else {
                        pathCommands.add("pdf_context.lineTo(" + coordsToStringParam(exitPoint, 2,changeCoordsAtEnd) + ");");
                        isPathSegmentVisible = false;

                        if(debugPath)
                            System.out.println("pdf_context.lineTo(" + coordsToStringParam(entryPoint, 2,changeCoordsAtEnd)+");");
                    }
                }
            }else if (pathCommand == PathIterator.SEG_QUADTO) {

                if(debugPath)
                    System.out.println("PathIterator.SEG_QUADTO");

                if(testDrawLimits(coords, PathIterator.SEG_QUADTO)) {

                    if(debugPath)
                        System.out.println("pdf_context.quadraticCurveTo(" + coordsToStringParam(coords, 4,changeCoordsAtEnd));

                    pathCommands.add("pdf_context.quadraticCurveTo(" + coordsToStringParam(coords, 4,changeCoordsAtEnd) + ");");
                    isPathSegmentVisible = true;
                }else {
                    isPathSegmentVisible = false;
                }
            }else if (pathCommand == PathIterator.SEG_MOVETO) {

                if(debugPath)
                    System.out.println("PathIterator.SEG_MOVETO");

                if(testDrawLimits(coords, PathIterator.SEG_MOVETO)) {
                	
                    isPathSegmentVisible = true;
                    if(debugPath)
                        System.out.println("pdf_context.moveTo(" + coordsToStringParam(coords, 2,changeCoordsAtEnd) + ");");

                    pathCommands.add("pdf_context.moveTo(" + coordsToStringParam(coords, 2,changeCoordsAtEnd) + ");");
                    
                }else {
                    isPathSegmentVisible = false;
                }
            }

            it.next();
        }

        if(pathCommands.size()==1) {
            //No commands made it through
            pathCommands.clear();
        }else {
            if(!isPathSegmentVisible && gs.getFillType() != GraphicsState.FILL) {
                pathCommands.add("pdf_context.closePath();");

                if(debugPath)
                        System.out.println("pdf_context.closePath();");
            }
            applyGraphicsStateToPath(gs);
        }
    }

    /**
     * trim if needed
     * @param i
     * @return
     *
     * (note duplicate in HTMLDisplay)
     */
    private String setPrecision(double i) {

        String value=String.valueOf(i);

        int ptr=value.indexOf(".");
        int len=value.length();
        int decimals=len-ptr-1;

        if(ptr>0 && decimals> this.dpCount){
            if(dpCount==0)
                value=value.substring(0,ptr+dpCount);
            else
                value=value.substring(0,ptr+dpCount+1);

        }

        return value;
    }


    /**
     * Extracts information out of graphics state to use in HTML
     */
    private void applyGraphicsStateToPath(GraphicsState gs)
    {
        int fillType = gs.getFillType();

        if(fillType==GraphicsState.FILL || fillType==GraphicsState.FILLSTROKE) {
            PdfPaint col = gs.getNonstrokeColor();

            if(isSinglePixelFill){ //special case to make sure appears in HTML if w or h of shape is 1
                pathCommands.add("pdf_context.strokeStyle = '" + rgbToCSSColor(col.getRGB()) + "';");
                pathCommands.add("pdf_context.stroke();");
            }else{ //add new fillStyle only if color changed
                if(col.getRGB() != currentColor) {
                    pathCommands.add("pdf_context.fillStyle = '" + rgbToCSSColor(col.getRGB()) + "';");
                    currentColor = col.getRGB();
                }
                pathCommands.add("pdf_context.fill();");
            }
        }

        if(fillType==GraphicsState.STROKE || fillType==GraphicsState.FILLSTROKE) {
            BasicStroke stroke = (BasicStroke) gs.getStroke();

            if(stroke.getLineWidth()!=1) { //attribute double lineWidth; (default 1)
                pathCommands.add("pdf_context.lineWidth = '" + ((double) stroke.getLineWidth()*scaling) + "';");
            }

            if(stroke.getMiterLimit()!=10) {  //attribute double miterLimit; // (default 10)
                pathCommands.add("pdf_context.miterLimit = '" + ((double) stroke.getMiterLimit()) + "';");
            }

            pathCommands.add("pdf_context.lineCap = '" + determineLineCap(stroke) + "';");
            pathCommands.add("pdf_context.lineJoin = '" + determineLineJoin(stroke) + "';");

            PdfPaint col = gs.getStrokeColor();
            pathCommands.add("pdf_context.strokeStyle = '" + rgbToCSSColor(col.getRGB()) + "';");
            pathCommands.add("pdf_context.stroke();");
        }
    }

    /**
     * Extract line cap attribute
     */
    private static String determineLineCap(BasicStroke stroke)
    {
        //attribute DOMString lineCap; // "butt", "round", "square" (default "butt")
        String attribute ="";

        switch(stroke.getEndCap()) {
            case(BasicStroke.CAP_ROUND):
                attribute = "round";
                break;
            case(BasicStroke.CAP_SQUARE):
                attribute = "square";
                break;
            default:
                attribute = "butt";
                break;
        }
        return attribute;
    }

    /**
     * Extract line join attribute
     */
    private static String determineLineJoin(BasicStroke stroke)
    {
        //attribute DOMString lineJoin; // "round", "bevel", "miter" (default "miter")
        String attribute ="";
        switch(stroke.getLineJoin()) {
            case(BasicStroke.JOIN_ROUND):
                attribute = "round";
                break;
            case(BasicStroke.JOIN_BEVEL):
                attribute = "bevel";
                break;
            default:
                attribute = "miter";
                break;
        }
        return attribute;
    }

    /**
     * @return true if coordinates lie with visible area
     */
    private boolean testDrawLimits(double[] coords, int pathCommand)
    {

        //not live yet (only in Marks test)
        if(!fixShape)
            return true;

        if(debugPath)
            System.out.println("testDrawLimits coords[0] + coords[1]" + coords[0] +" "+ coords[1]);

        if(!ENABLE_CROPPING) {
            return true;
        }

        int offset = getCoordOffset(pathCommand);

        //assume not visible by default
        boolean isCurrentPointVisible = false;

        //use CropBox or clip (whicehver is smaller)
        Rectangle cropBox=this.cropBox;

        if(clipBox!=null){
            cropBox=cropBox.intersection(clipBox);
        }

        if(debugPath)
            System.out.println("cropBox=" +cropBox.getBounds());


        /**
         * turn co-ords into a Point
         * (the 1s are to allow for rounding errors)
         */
        double x=coords[offset];
        if(x>cropBox.x+1)
            x=x-1;
        double y=coords[offset+1];
        if(y>cropBox.y+1)
            y=y-1;
        double[] point = {x,y};

        if(cropBox.contains(point[0], point[1])) { //this point is visible
            lastVisiblePoint = point;
            isCurrentPointVisible = true;

            if(debugPath)
                System.out.println("Point visible");

        }
        else { //this point outside visible area
            lastInvisiblePoint = point;
            isCurrentPointVisible = false;

            if(debugPath)
                System.out.println("Point invisible");
        }

        if(!isCurrentPointVisible && isPathSegmentVisible) {

            if(debugPath)
                System.out.println("Case1 this point "+x+","+y+" invisible and isPathSegmentVisible");

            findSwitchPoint(point, true);
        }
        else if(isCurrentPointVisible && !isPathSegmentVisible) {

            if(debugPath)
                System.out.println("Case2 this point "+x+","+y+" visible and isPathSegment invisible");

            findSwitchPoint(point, false);

        }else{
            if(debugPath)
                            System.out.println("Case3 NOT COVERED");

        }


        //Check whether this point and last point cross crop box.
        if(!isCurrentPointVisible && (!cropBox.contains(previousPoint[0], previousPoint[1])) && pathCommand == PathIterator.SEG_LINETO) {

            if(debugPath)
                System.out.println("checkTraversalPoints");

            checkTraversalPoints(point, previousPoint);
        }

        previousPoint = point;

        return isCurrentPointVisible;
    }

    /**
     * Figure out where to draw a line if the given points do not lie within the cropbox
     * but should draw a line because they pass over it.
     */
    private void checkTraversalPoints(double[] startPoint, double[] endPoint)
    {
        boolean xTraversal = (endPoint[0] < cropBox.x && startPoint[0] > (cropBox.x + cropBox.width)) || (startPoint[0] < cropBox.x && endPoint[0] > (cropBox.x + cropBox.width));
        boolean yTraversal = (endPoint[1] < cropBox.y && startPoint[1] > (cropBox.y + cropBox.height)) || (startPoint[1] < cropBox.y && endPoint[1] > (cropBox.y + cropBox.height));
        boolean completeCropBoxMiss = isCompleteCropBoxMiss(startPoint, endPoint);

        if(!xTraversal && !yTraversal) {
            return;
        }

        double xSide = startPoint[0] - endPoint[0];
        double ySide = startPoint[1] - endPoint[1];

        if(xTraversal) {
            double tan = xSide / ySide;

            if(ySide == 0) {
                entryPoint[1] = exitPoint[1] = !completeCropBoxMiss ? startPoint[1] : getClosestCropEdgeY(startPoint[1]);
                if(endPoint[0] < cropBox.x ) {
                    entryPoint[0] = cropBox.x;
                    exitPoint[0] = cropBox.x + cropBox.width;
                }
                else {
                    entryPoint[0] = cropBox.x + cropBox.width;
                    exitPoint[0] = cropBox.x;
                }
            }
            else if(endPoint[0] < cropBox.x) {
                double distanceToCrop = cropBox.x - endPoint[0];
                entryPoint[0] = cropBox.x;
                entryPoint[1] = endPoint[1] + (tan * distanceToCrop);
                distanceToCrop = cropBox.x + cropBox.width - endPoint[0];
                entryPoint[0] = cropBox.x + cropBox.width;
                exitPoint[1] = startPoint[1] + (tan * distanceToCrop);
            }
            else {
                double distanceToCrop = cropBox.x + cropBox.width - endPoint[0];
                entryPoint[0] = cropBox.x + cropBox.width;
                entryPoint[1] = endPoint[1] + (tan * distanceToCrop);
                distanceToCrop = cropBox.x - endPoint[0];
                exitPoint[0] = cropBox.x;
                exitPoint[1] = startPoint[1] + (tan * distanceToCrop);
            }
        }

        if(yTraversal) {
            double tan = ySide / xSide;

            if(xSide == 0) {
                entryPoint[0] = exitPoint[0] = !completeCropBoxMiss ? startPoint[0] : getClosestCropEdgeX(startPoint[0]);
                if(endPoint[1] < cropBox.y ) {
                    entryPoint[1] = cropBox.y;
                    exitPoint[1] = cropBox.y + cropBox.height;
                }
                else {
                    entryPoint[1] = cropBox.y + cropBox.height;
                    exitPoint[1] = cropBox.y;
                }
            }
            else if(endPoint[1] < cropBox.y) {
                double distanceToCrop = cropBox.y - endPoint[1];
                entryPoint[0] = endPoint[0] + (tan * distanceToCrop);
                entryPoint[1] = cropBox.y;
                distanceToCrop = cropBox.y + cropBox.height - endPoint[1];
                exitPoint[0] = startPoint[0] + (tan * distanceToCrop);
                exitPoint[1] = cropBox.y + cropBox.height;
            }
            else {
                double distanceToCrop = cropBox.y + cropBox.height - endPoint[0];
                entryPoint[0] = endPoint[0] + (tan * distanceToCrop);
                entryPoint[1] = cropBox.y + cropBox.height;
                distanceToCrop = cropBox.y - endPoint[0];
                exitPoint[0] = startPoint[0] + (tan * distanceToCrop);
                exitPoint[1] = cropBox.y;
            }
        }

        pathCommands.add("pdf_context.lineTo(" + coordsToStringParam(entryPoint, 2,changeCoordsAtEnd) + ");");
        pathCommands.add("pdf_context.lineTo(" + coordsToStringParam(exitPoint, 2,changeCoordsAtEnd) + ");");
    }

    /**
     * Figure out where the line disappears or reappears off the cropbox boundary.
     * @param point The current point
     * @param exit true if you wish to find an exit point (one that marks a disappearance)
     */
    private void findSwitchPoint(double[] point, boolean exit)
    {
        double[] lastPoint = new double[2];
        double[] switchPoint = new double[2];

        lastPoint = exit ? lastVisiblePoint : lastInvisiblePoint;

        if(debugPath)
            System.out.println("Find switch point exit="+exit+" lastPoint="+lastPoint[0]+" "+lastPoint[1]);

        if(!exit) {
            double[] tmp = point;
            point = lastPoint;
            lastPoint = tmp;
        }

        double xSide = point[0] - lastPoint[0];
        double ySide = point[1] - lastPoint[1];

        //To indicate whether a coordinate has been found
        boolean xFound = false;
        boolean yFound = false;

        if(point[0] >= cropBox.width + cropBox.x) {
            switchPoint[0] = cropBox.width + cropBox.x;
            xFound = true;
        }
        else if(point[0] < cropBox.x) {
            switchPoint[0] = cropBox.x;
            xFound = true;
        }

        if(point[1] > cropBox.height + cropBox.y) {
            switchPoint[1] = cropBox.height + cropBox.y;
            yFound = true;
        }
        else if(point[1] < cropBox.y) {
            switchPoint[1] = cropBox.y;
            yFound = true;
        }

        if(yFound) {
            if(xSide == 0) {
                switchPoint[0] = point[0];
            }
            else {
                double tan = xSide / ySide;
                double distanceToCropY = switchPoint[1] - point[1];
                switchPoint[0] = point[0] + (tan * distanceToCropY);
            }
        }
        if(xFound) {
            if(ySide == 0) {
                switchPoint[1] = point[1];
            }
            else {
                double tan = ySide / xSide;
                double distanceToCropX = switchPoint[0] - point[0];
                switchPoint[1] = point[1] + (tan * distanceToCropX);
            }
        }

        if(debugPath)
            System.out.println("returns switchPoint="+switchPoint[0]+" "+switchPoint[1]);


        if(exit) {
            exitPoint = switchPoint;
        }
        else {
            entryPoint = switchPoint;
        }
    }

    /**
     * Add the coords to the large box list if it might possibly be part of a rectangle.
     * @param coords
     * @param pathCommand
     */
    private void checkLargeBox(double[] coords, int pathCommand)
    {
        if(!isLargeBox && (pathCommand != PathIterator.SEG_LINETO || pathCommand != PathIterator.SEG_MOVETO)) {
            return;
        }

        double px=coords[getCoordOffset(pathCommand)],py= coords[getCoordOffset(pathCommand) + 1];
        if(changeCoordsAtEnd){ //adjust if needed
        	double[] adjustedCords=this.correctCoords(new double[]{px,py});
        	px=adjustedCords[0];
        	py=adjustedCords[1];
        }

        Point point = new Point((int)px, (int)py);

        if(largeBox.isEmpty()) {
            largeBox.add(point);
        }
        else {
            Point2D last = (Point)largeBox.get(largeBox.size() - 1);
            double xSide = last.getX() - point.getX();
            double ySide = last.getY() - point.getY();

            //First time we can compare so check here and ignore below.
            if(largeBox.size() == 1) {

                if(ySide!=0)//allow for div by zero
                    largeBoxSideAlternation = xSide / ySide != 0;
                else
                    largeBoxSideAlternation=true;
            }

            //If this point and the last point do not form a horizontal or vertical line its not part of a large rectangular shape.
            if(xSide / ySide == 0 || ySide / xSide == 0) {

                boolean currentSide = xSide / ySide == 0;

                //Ignore if its part of a continous line.  The continous line could be going back on it self but currently not accounted for.
                if(largeBox.size() >1 || (currentSide != largeBoxSideAlternation)) {
                    largeBox.add(point);
                    largeBoxSideAlternation = xSide / ySide == 0;
                }
            }
            else if(!point.equals(largeBox.get(largeBox.size() - 1))) {
                isLargeBox = false;
                return;
            }

            //Check if we have enough point to see if it is larger than the whole page.
            if(largeBox.size() >= 4 && isLargerThanCropBox()) {
                drawCropBox();
            }
        }
    }

    /**
     * return true if the coordinates in this path specify a box larger than the cropbox.
     */
    private boolean isLargerThanCropBox()
    {
        if(!isLargeBox) {
            return false;
        }

        Point2D x = (Point)largeBox.get(largeBox.size() - 4);
        Point2D y = (Point)largeBox.get(largeBox.size() - 3);
        Point2D z = (Point)largeBox.get(largeBox.size() - 2);
        Point2D w = (Point)largeBox.get(largeBox.size() - 1);

        int shortestSide = cropBox.width < cropBox.height ? cropBox.width : cropBox.height;
        //Can not cover the page if this is true.
        if(x.distance(y) < shortestSide || y.distance(z) < shortestSide || z.distance(w) < shortestSide) {
            return false;
        }

        int outsideCount = 0;

        if (!cropBox.contains(x.getX(), x.getY())) outsideCount++;
        if (!cropBox.contains(y.getX(), y.getY())) outsideCount++;
        if (!cropBox.contains(z.getX(), z.getY())) outsideCount++;
        if (!cropBox.contains(w.getX(), w.getY())) outsideCount++;

        if(outsideCount <= 2) {
            return false;
        }

        Set points = new HashSet();
        points.add(x);
        points.add(y);
        points.add(z);
        points.add(w);

        outsideCount = 0;

        //Test that points lie in correct areas to justify a box covering the page.
        for(int hOffset = -1; hOffset <= 1; hOffset++) {
            for(int wOffset = -1; wOffset <= 1; wOffset++) {
                if(hOffset == 0 && wOffset==0) { //Would mean the test rectangle is same as cropbox so ignore
                    continue;
                }
                Rectangle outside = new Rectangle(cropBox);
                outside.translate(wOffset * cropBox.width, hOffset * cropBox.height);
                if(doesPointSetCollide(points, outside)) outsideCount++;
            }
        }

        return outsideCount >= 3;
    }

    /**
     * return true if any of the given points are contained in the given rectangle
     */
    private static boolean doesPointSetCollide(Set points, Rectangle rect)
    {
        //converted so java ME compilies
        Iterator iter = points.iterator();
        while(iter.hasNext()){
            Point2D pt = (Point2D)iter.next();
            if(rect.contains(pt)) {
                return true;
            }
        }
        return false;
    }

    private void drawCropBox()
    {
        pathCommands.clear();
        pathCommands.add("pdf_context.beginPath();");

        double[] coords = {cropBox.x, cropBox.y};
        pathCommands.add("pdf_context.moveTo(" + coordsToStringParam(coords, 2,false) + ");");
        coords[0] += cropBox.width;
        pathCommands.add("pdf_context.lineTo(" + coordsToStringParam(coords, 2,false) + ");");
        coords[1] += cropBox.height;
        pathCommands.add("pdf_context.lineTo(" + coordsToStringParam(coords, 2,false) + ");");
        coords[0] -= cropBox.width;
        pathCommands.add("pdf_context.lineTo(" + coordsToStringParam(coords, 2,false) + ");");
        
    }

    private double getClosestCropEdgeX(double x)
    {
        return x < cropBox.x + (cropBox.width / 2) ? cropBox.x : cropBox.x + cropBox.width;
    }

    private double getClosestCropEdgeY(double y)
    {
        return y < cropBox.y + (cropBox.height / 2) ? cropBox.y : cropBox.y + cropBox.height;
    }

    /**
     * Convert from PDF coords to java coords.
     */
    private double[] correctCoords(double[] coords)
    {

        int offset;

        switch(pathCommand) {
            case(PathIterator.SEG_CUBICTO):
                offset = 4;
                break;
            case(PathIterator.SEG_QUADTO):
                offset = 2;
                break;
            default:
                offset = 0;
                break;
        }
        
        //ensure fits
        if(offset>coords.length)
        	offset=coords.length-2;

        for(int i = 0; i < offset + 2; i+=2) {
            coords[i] = coords[i] - midPoint.getX();
            coords[i] += cropBox.width / 2;

            coords[i+1] = coords[i+1] - midPoint.getY();
            coords[i+1] = 0 - coords[i+1];
            coords[i+1] += cropBox.height / 2;
        }
        
        return coords;
    }

    /**
     * Return the index of the start coordinate
     */
    private static int getCoordOffset(int pathCommand)
    {
        int offset;

        switch(pathCommand) {
            case(PathIterator.SEG_CUBICTO):
                offset = 4;
                break;
            case(PathIterator.SEG_QUADTO):
                offset = 2;
                break;
            default:
                offset = 0;
                break;
        }
        return offset;
    }

    /**
     * Tests whether the line between the two given points crosses over the crop box.
     * @return true if it misses completely.
     */
    private boolean isCompleteCropBoxMiss(double[] start, double[] end)
    {
        int xLimMin = cropBox.x;
        int xLimMax = xLimMin + cropBox.width;
        int yLimMin = cropBox.y;
        int yLimMax = xLimMax + cropBox.height;

        return ((start[0] < xLimMin && end[0] < xLimMin) || (start[0] > xLimMax && end[0] > xLimMax)) &&
                ((start[1] < yLimMin && end[1] < yLimMin) || (start[1] > yLimMax && end[1] > yLimMax));
    }

    /**
     * Coverts an array of numbers to a String for JavaScript parameters.
     * Removes cropbox offset.
     *
     * @param coords Numbers to change
     * @param count Use up to count doubles from coords array
     * @return String Bracketed stringified version of coords
     * (note numbers rounded to nearest int to keep down filesize)
     */
    private String coordsToStringParam(double[] coords, int count, boolean changeCoordsAtEnd)
    {
    	
    	if(changeCoordsAtEnd){
    		//make copy factoring in size
    		int size=coords.length;
    		double[] copy=new double[size];
    		for(int ii=0;ii<size;ii++)
    			copy[ii]=coords[ii];
    		
    		coords=correctCoords(copy);
    	}
    	
        String result = "";

        if(pageRotation==90 || pageRotation==270){
            //for each set of coordinates, set value
            for(int i = 0; i<count/2; i=i+2) {
                if(i!=0) {
                    result += ",";
                }
                result += setPrecision((cropBox.height-20-coords[i+1])*scaling);
                //result += setPrecision((cropBox.height-coords[i+1]-pageData.getCropBoxY(pageNumber))*scaling);

                result += ",";
                result += setPrecision(coords[i]*scaling);//- (i%2 == 1 ? cropBox.x : cropBox.y) );
                //    result += setPrecision((coords[i]-(pageData.getCropBoxX(pageNumber)/2))*scaling);//- (i%2 == 1 ? cropBox.x : cropBox.y) );

            }
        }else{

            for(int i = 0; i<count; i++) {
                if(i!=0) {
                    result += ",";
                }
                result += setPrecision(coords[i]*scaling);//- (i%2 == 1 ? cropBox.x : cropBox.y) );
            }
        }

        return result;
    }

    /**
     * Formats an int to CSS rgb(r,g,b) string
     *
     */
    private static String rgbToCSSColor(int raw)
    {
        int r = (raw>>16) & 255;
        int g = (raw>>8) & 255;
        int b = raw & 255;

        return "rgb(" + r +  "," + g +  "," + b + ")";
    }

    public String getHTML()
    {
        String result = "";

        for(int i = 0; i < pathCommands.size(); i++) {
            //@TODO Hack to backspace out tab so as to not break test.
            if(i != 0) {
                result += "\t";
            }
            result += pathCommands.get(i);
            if(i != (pathCommands.size() - 1)) {
                result += "\n";
            }
        }
        return result;
    }

    public boolean isEmpty()
    {
        return pathCommands.isEmpty();
    }

    public int getShapeColor()
    {
        return currentColor;
    }

}
