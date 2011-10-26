/**
 * ===========================================
 * Java Pdf Extraction Decoding Access Library
 * ===========================================
 *
 * Project Info:  http://www.jpedal.org
 *
 * (C) Copyright 2010, IDRsolutions and Contributors.
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

  * CustomFormPrint.java
  * ---------------
  * (C) Copyright 2010, by IDRsolutions and Contributors.
  *
  *
  * --------------------------
 */
package org.jpedal.external;

import org.jpedal.PdfDecoder;

import java.awt.*;

/**
 * allow user to set print hinting
 */
public interface CustomPrintHintingHandler {
    boolean preprint(Graphics2D g2, PdfDecoder pdf);
    boolean postprint(Graphics2D g2, PdfDecoder pdf);
}