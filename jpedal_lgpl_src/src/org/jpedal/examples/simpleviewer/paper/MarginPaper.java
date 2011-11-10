package org.jpedal.examples.simpleviewer.paper;

import java.awt.print.Paper;

/**
 * Created by IntelliJ IDEA.
 * User: Sam
 * Date: 02-Jul-2010
 * Time: 16:41:28
 * To change this template use File | Settings | File Templates.
 */
public class MarginPaper extends Paper {
    double minX=0, minY=0, maxRX =0, maxBY =0;

    public void setMinImageableArea(double x, double y, double w, double h) {
        this.minX = x;
        this.minY = y;
        this.maxRX = x+w;
        this.maxBY = y+h;
        super.setImageableArea(minX, minY, maxRX, maxBY);
    }

    public void setImageableArea(double x, double y, double w, double h) {

        if (x < minX)
            x = minX;
        if (y < minY)
            y = minY;
        if (x+w > maxRX)
            w = maxRX-x;
        if (y+h > maxBY)
            h = maxBY-y;

        super.setImageableArea(x, y, w, h);
    }

    public double getMinX() {
        return minX;
    }

    public double getMinY() {
        return minY;
    }

    public double getMaxRX() {
        return maxRX;
    }

    public double getMaxBY() {
        return maxBY;
    }
}
