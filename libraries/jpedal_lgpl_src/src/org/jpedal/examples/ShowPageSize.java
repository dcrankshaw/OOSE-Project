package org.jpedal.examples;

import org.jpedal.PdfDecoder;
import org.jpedal.objects.PdfPageData;

/**
 * example written to show pagesize of all pages on system
 */

public class ShowPageSize {

    public ShowPageSize(String file_name){

        PdfDecoder decode_pdf = new PdfDecoder( false ); //false as no display

        try{
        decode_pdf.openPdfFile( file_name );

            /**get page count*/
			int pageCount= decode_pdf.getPageCount();
			System.out.println( "Page count=" + pageCount );


            //get PageData object
            PdfPageData pageData = decode_pdf.getPdfPageData();
            //show all page sizes
            for(int ii=0;ii<pageCount;ii++){

                //pixels
                System.out.print("page (size in pixels) "+ii+
                        " mediaBox="+pageData.getMediaBoxX(ii)+" "+pageData.getMediaBoxY(ii)+" "+pageData.getMediaBoxWidth(ii)+" "+pageData.getMediaBoxHeight(ii)+
                        " CropBox="+pageData.getCropBoxX(ii)+" "+pageData.getCropBoxY(ii)+" "+pageData.getCropBoxWidth(ii)+" "+pageData.getCropBoxHeight(ii));

                //inches
                float factor=72f; //72 is the usual screen dpi
                System.out.print(" (size in inches) "+ii+
                        " mediaBox="+pageData.getMediaBoxX(ii)/factor+" "+pageData.getMediaBoxY(ii)/factor+" "+pageData.getMediaBoxWidth(ii)/factor+" "+pageData.getMediaBoxHeight(ii)/factor+
                        " CropBox="+pageData.getCropBoxX(ii)/factor+" "+pageData.getCropBoxY(ii)/factor+pageData.getCropBoxWidth(ii)/factor+" "+pageData.getCropBoxHeight(ii)/factor);

                //cm
                factor=72f/2.54f;
                System.out.print(" (size in cm) "+ii+
                        " mediaBox="+pageData.getMediaBoxX(ii)/factor+" "+pageData.getMediaBoxY(ii)/factor+" "+pageData.getMediaBoxWidth(ii)/factor+" "+pageData.getMediaBoxHeight(ii)/factor+
                        " CropBox="+pageData.getCropBoxX(ii)/factor+" "+pageData.getCropBoxY(ii)/factor+pageData.getCropBoxWidth(ii)/factor+" "+pageData.getCropBoxHeight(ii)/factor+"\n");

            }

			/**close the pdf file*/
			decode_pdf.closePdfFile();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    /** main method to run the software as standalone application */
	public static void main(String[] args) {
        if(args.length!=1){
            System.out.println("Please pass in file name (including path");
        }else{
            new ShowPageSize(args[0]);
        }
    }
}
