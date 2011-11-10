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
* DefaultActionHandler.java
* ---------------
*/
package org.jpedal.objects.acroforms.actions;

import org.jpedal.PdfDecoder;
//<start-adobe><start-thin>// <start-me>
import org.jpedal.examples.simpleviewer.gui.SwingGUI;
import org.jpedal.examples.simpleviewer.gui.swing.SwingMouseHandler;
import org.jpedal.examples.simpleviewer.gui.swing.SwingMouseListener;
import org.jpedal.examples.simpleviewer.Commands;
import org.jpedal.examples.simpleviewer.SimpleViewer;
import org.jpedal.examples.simpleviewer.Values;
// <end-me>
import org.jpedal.external.Options;
//<end-thin><end-adobe>
import org.jpedal.io.ArrayDecoder;
import org.jpedal.io.ObjectDecoder;
import org.jpedal.io.ObjectStore;
import org.jpedal.io.PdfObjectReader;
import org.jpedal.objects.Javascript;
import org.jpedal.objects.layers.PdfLayerList;
import org.jpedal.objects.acroforms.actions.privateclasses.FieldsHideObject;
import org.jpedal.objects.acroforms.formData.ComponentData;
import org.jpedal.objects.raw.FormStream;
import org.jpedal.objects.raw.FormObject;
import org.jpedal.objects.raw.PdfArrayIterator;
import org.jpedal.objects.raw.PdfDictionary;
import org.jpedal.objects.raw.PdfObject;
import org.jpedal.objects.acroforms.rendering.AcroRenderer;
import org.jpedal.objects.acroforms.rendering.DefaultAcroRenderer;
//<start-me>
import org.jpedal.objects.acroforms.utils.ConvertToString;
//<end-me>
import org.jpedal.objects.raw.*;
import org.jpedal.utils.BrowserLauncher;
import org.jpedal.utils.LogWriter;
import org.jpedal.utils.Messages;

import javax.swing.*;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;



public class DefaultActionHandler implements ActionHandler {
	
	final static private boolean showMethods = false;

	//shows the form you have hovered over with the mouse
	private static final boolean IdentifyForms = false;/**/

	private String AcrobatJSGuideURL = "http://www.adobe.com/devnet/acrobat/pdfs/Acro6JSGuide.pdf";

	private PdfObjectReader currentPdfFile;

    private Javascript javascript;

	private AcroRenderer acrorend;

    private ActionFactory actionFactory;

    //handle so we can access
	private PdfDecoder decode_pdf;

	//private int pageHeight,insetH;

	//flags to control reading of JS
	private boolean JSInitialised_A, JSInitialised_BI,JSInitialised_C, JSInitialised_D,
	JSInitialised_E,JSInitialised_F, JSInitialised_Fo, JSInitialised_K, JSInitialised_U,
	JSInitialised_V, JSInitialised_X;

	//<start-adobe><start-thin>// <start-me>
	private SwingMouseListener swingMouseHandler;
	//<end-thin><end-adobe>// <end-me>

	public void init(PdfDecoder decode_pdf, Javascript javascript, AcroRenderer acrorend) {
		if(showMethods)
			System.out.println("DefaultActionHandler.init()");

        if(decode_pdf!=null){
		    currentPdfFile = decode_pdf.getIO();
        }
        
		this.javascript = javascript;
		this.acrorend = acrorend;
		this.decode_pdf = decode_pdf;

    }
	
//	public void init(PdfObjectReader pdfFile, Javascript javascript, AcroRenderer acrorend) {
//		if(showMethods)
//			System.out.println("DefaultActionHandler.init()");
//
//		currentPdfFile = pdfFile;
//		this.javascript = javascript;
//		this.acrorend = acrorend;
//		
//    }

	public void setPageAccess(int pageHeight, int insetH) {
		if(showMethods)
			System.out.println("DefaultActionHandler.setPageAccess()");
		
		//this.pageHeight=pageHeight;
		//this.insetH=insetH;
	}

    public void setActionFactory(ActionFactory actionFactory) {
    	if(showMethods)
    		System.out.println("DefaultActionHandler.setActionFactory()");

        actionFactory.setPDF(decode_pdf,acrorend);
        this.actionFactory=actionFactory;

    }
    
    public ActionFactory getActionFactory(){
    	return actionFactory;
    }

    /**
	 * creates a returns an action listener that will change the down icon for each click
	 */
	public Object setupChangingDownIcon(Object downOff, Object downOn, int rotation) {
		if(showMethods)
			System.out.println("DefaultActionHandler.setupChangingDownIcon()");
		
		return actionFactory.getChangingDownIconListener(downOff, downOn, rotation);
	}

	/**
	 * sets up the captions to change as needed
	 */
	public Object setupChangingCaption(String normalCaption, String rolloverCaption, String downCaption) {
		if(showMethods)
			System.out.println("DefaultActionHandler.setupChangingCaption()");
		
		return new SwingFormButtonListener(normalCaption, rolloverCaption, downCaption);
	}
	
	public Object setHoverCursor(){
		if(showMethods)
			System.out.println("DefaultActionHandler.setHoverCursor()");
		
		return actionFactory.getHoverCursor();
		
	}

	/**
	 * A action when pressed in active area ?some others should now be ignored?
	 */
	public void A(Object raw, FormObject formObj, int eventType) {

        if(showMethods)
            System.out.println("DefaultActionHandler.A() ");
            
        
		// new version
        PdfObject aData = null;
        if(eventType==MOUSERELEASED ){
			//get the A action if we have activated the form (released)
			aData = formObj.getDictionary(PdfDictionary.A);
		}

		if(aData==null){
			aData = formObj.getDictionary(PdfDictionary.AA);
			if(aData!=null){
				if(eventType == MOUSEENTERED){
					aData = aData.getDictionary(PdfDictionary.E);
				}else if(eventType == MOUSEEXITED){
					aData = aData.getDictionary(PdfDictionary.X);
				}else if(eventType == MOUSEPRESSED){
					aData = aData.getDictionary(PdfDictionary.D);
				}else if(eventType == MOUSERELEASED){
					aData = aData.getDictionary(PdfDictionary.U);
				}
			}
		}

		//change cursor for each event
		actionFactory.setCursor(eventType);

        gotoDest(formObj,eventType,PdfDictionary.Dest);
		
        int subtype=formObj.getParameterConstant(PdfDictionary.Subtype);

		int popupFlag = formObj.getActionFlag();

		if (subtype == PdfDictionary.Sig) {

			additionalAction_Signature(formObj, eventType);

		} else if (eventType==MOUSECLICKED && (popupFlag == FormObject.POPUP || subtype==PdfDictionary.Text)){
			actionFactory.popup(raw,formObj,currentPdfFile);
		} else {
			// can get empty values
			if (aData == null)
				return;
			
			int command = aData.getNameAsConstant(PdfDictionary.S);

			// S is Name of action
			if (command != PdfDictionary.Unknown) {

				if (command == PdfDictionary.Named) {

					additionalAction_Named(eventType, aData);

                }else if(command==PdfDictionary.Goto || command==PdfDictionary.GoToR){
            		if (aData != null) {
            			gotoDest(aData, eventType,command);
            		}
				} else if (command == PdfDictionary.ResetForm) {
					
					additionalAction_ResetForm();
					
				} else if (command == PdfDictionary.SubmitForm) {
					
					additionalAction_SubmitForm(aData);

				} else if (command == PdfDictionary.JavaScript) {

					//javascript called above.

				} else if (command == PdfDictionary.Hide) {
					
					additionalAction_Hide(eventType, aData);

				} else if (command == PdfDictionary.URI) {

                    additionalAction_URI(eventType, aData.getTextStreamValue(PdfDictionary.URI));

				} else if (command == PdfDictionary.Launch) {

					//<start-thin><start-adobe>
					try {
						//get the F dictionary
				        PdfObject dict=aData.getDictionary(PdfDictionary.F);
				        
				        //System.out.println("dict="+dict+" "+dict.getObjectRefAsString());
				        
				        //then get the submit URL to use
				        if(dict!=null){
                            String target = dict.getTextStreamValue(PdfDictionary.F);

                            InputStream sourceFile = getClass().getResourceAsStream("/org/jpedal/res/"+target);

                            if(sourceFile==null){
                                JOptionPane.showMessageDialog(decode_pdf,"Unable to locate "+target);
                            }else{
                                //System.out.printl("name="+getClass().getResource("/org/jpedal/res/"+target).get);

                                //get name without path
                                int ptr=target.lastIndexOf("/");
                                if(ptr!=-1)
                                    target=target.substring(ptr+1);

                                File output=new File(ObjectStore.temp_dir+target);
                                output.deleteOnExit();

                                ObjectStore.copy(new BufferedInputStream(sourceFile),
                                        new BufferedOutputStream(new FileOutputStream(output)));

                                if(target.endsWith(".pdf")){

                                	// <start-me>
                                    try{
                                        SimpleViewer viewer=new SimpleViewer(Values.RUNNING_NORMAL);
                                        SimpleViewer.exitOnClose=false;

                                        //<start-wrap>
                                        /**
                                        //<end-wrap>
                                        viewer.setupViewer(ObjectStore.temp_dir+target);
                                        /**/
                                        //<start-wrap>
                                        viewer.setupViewer();
                                        viewer.openDefaultFile(ObjectStore.temp_dir+target);
                                        //<end-wrap>


                                    }catch(Exception e){
                                        e.printStackTrace();
                                    }
                                    // <end-me>

                                }else if(PdfDecoder.isRunningOnMac){
                                    target="open "+ObjectStore.temp_dir+target;

                                    // System.out.println("target="+target);

                                    Runtime.getRuntime().exec(target);
                                    /**String line="";

                                     BufferedReader input =
                                     new BufferedReader
                                     (new InputStreamReader(p.getInputStream()));
                                     while ((line = input.readLine()) != null) {
                                     System.out.println(line);
                                     }
                                     input.close();

                                     System.out.println("target="+target);
                                     /**/
                                }
                            }

                        }
					} catch (Exception e1) {
						e1.printStackTrace();						
					} catch (Error err) {
						err.printStackTrace();
					}
					
					//<end-adobe><end-thin>
					
                    LogWriter.writeFormLog("{stream} launch activate action NOT IMPLEMENTED", FormStream.debugUnimplemented);

				} else if (command == PdfDictionary.SetOCGState) {

					additionalAction_OCState(eventType, aData);

				} else if (command == PdfDictionary.Sound) {


				} else {
                    LogWriter.writeFormLog("{stream} UNKNOWN Command "+aData.getName(PdfDictionary.S)+" Action", FormStream.debugUnimplemented);
				}
			} else if(command!=-1){
                LogWriter.writeFormLog("{stream} Activate Action UNKNOWN command "+aData.getName(PdfDictionary.S)+" "+formObj.getObjectRefAsString(), FormStream.debugUnimplemented);
			}
		}
	}

	private void additionalAction_OCState(int eventType, PdfObject aData) {
		if (eventType == MOUSECLICKED) {

			PdfArrayIterator state = aData.getMixedArray(PdfDictionary.State);

			if (state != null && state.getTokenCount() > 0) {

				final PdfLayerList layers = (PdfLayerList)decode_pdf.getJPedalObject(PdfDictionary.Layer);

				int count = state.getTokenCount();

				final int action = state.getNextValueAsConstant(true);
				String ref;
				for (int jj = 1; jj < count; jj++) {
					ref = state.getNextValueAsString(true);

					final String layerName = layers.getNameFromRef(ref);

					// toggle layer status when clicked
					Runnable updateAComponent = new Runnable() {
						public void run() {
							// force refresh
							decode_pdf.invalidate();
							decode_pdf.validate();

							// update settings on display and in PdfDecoder
							boolean newState;
							if (action == PdfDictionary.Toggle)
								newState = !layers.isVisible(layerName);
							else if (action == PdfDictionary.OFF)
								newState = false;
                            else //must be ON
								newState = true;

							layers.setVisiblity(layerName, newState);

							// decode again with new settings
							try {
								decode_pdf.decodePage(-1);
							} catch (Exception e) {
                                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
							}
						}
					};

					SwingUtilities.invokeLater(updateAComponent);
				}
			}
		}
	}

	private void additionalAction_Named(int eventType, PdfObject aData) {
		int name = aData.getNameAsConstant(PdfDictionary.N);

		if (name == PdfDictionary.Print) {
                    //<start-me>
			additionalAction_Print(eventType);
                        //<end-me>
		} else if(name == PdfDictionary.SaveAs){
			additionalAction_SaveAs();
		}else if(name == PdfDictionary.NextPage){
			changeTo(null, decode_pdf.getlastPageDecoded()+1, null, null,true);
		}else if(name == PdfDictionary.PrevPage){
			changeTo(null, decode_pdf.getlastPageDecoded()-1, null, null,true);
		}else if(name == PdfDictionary.FirstPage){
			changeTo(null, 1, null, null,true);
		}else if(name == PdfDictionary.GoBack){
			//<start-adobe><start-thin>// <start-me>
            SwingGUI swingGUI=((SwingGUI)decode_pdf.getExternalHandler(Options.SwingContainer));

            if(swingGUI!=null)
            	swingGUI.currentCommands.executeCommand(Commands.BACK,null);
            //<end-thin><end-adobe>// <end-me>
		}else if(name == PdfDictionary.LastPage){
			changeTo(null, decode_pdf.getPageCount(), null, null,true);
		}else if(name == PdfDictionary.ZoomTo){
			//create scaling values list, taken from SimpleViewer.init(resourceBundle)
			JComboBox scaling = new JComboBox(new String[]{Messages.getMessage("PdfViewerScaleWindow.text"),Messages.getMessage("PdfViewerScaleHeight.text"),
					Messages.getMessage("PdfViewerScaleWidth.text"),
					"25","50","75","100","125","150","200","250","500","750","1000"});
			int option = JOptionPane.showConfirmDialog(null, scaling, Messages.getMessage("PdfViewerToolbarScaling.text")+":", JOptionPane.DEFAULT_OPTION);
			
             //<start-adobe><start-thin>// <start-me>
			if(option!=-1){
				int selection = scaling.getSelectedIndex();
				if(selection!=-1){
					SwingGUI swing = ((SwingGUI)decode_pdf.getExternalHandler(Options.SwingContainer));
                    if(swing!=null){
					    swing.setSelectedComboIndex(Commands.SCALING, selection);
					    swing.zoom(false);
                    }
				}
			}
			//<end-thin><end-adobe>// <end-me>
		}else if(name == PdfDictionary.FullScreen){
            //<start-adobe><start-thin>// <start-me>
            SwingGUI swingGUI=((SwingGUI)decode_pdf.getExternalHandler(Options.SwingContainer));

            if(swingGUI!=null)
			swingGUI.currentCommands.executeCommand(Commands.FULLSCREEN,null);
            //<end-thin><end-adobe>// <end-me>
		}else if(name == PdfDictionary.AcroForm_FormsJSGuide) {//AcroForm:FormsJSGuide
                    //<start-adobe><start-thin>// <start-me>
                    int option = JOptionPane.showConfirmDialog(null, Messages.getMessage("AcroForm_FormsJSGuide.urlQuestion")
                            + "\n" + AcrobatJSGuideURL + " ?\n\n"
                            + Messages.getMessage("AcroForm_FormsJSGuide.urlFail"), Messages.getMessage("AcroForm_FormsJSGuide.Title"), JOptionPane.YES_NO_OPTION);

                    if (option == 0) {
                        SimpleViewer viewer = new SimpleViewer(Values.RUNNING_NORMAL);
                        SimpleViewer.exitOnClose = false;

                        //<start-wrap>
                        /**
                        //<end-wrap>
                        viewer.setupViewer(AcrobatJSGuideURL);
                        /**/
                        //<start-wrap>
                        viewer.setupViewer();
                        viewer.openDefaultFile(AcrobatJSGuideURL);
                        //<end-wrap>

                    }
                    //<end-thin><end-adobe>// <end-me>
                } else {

		}
	}
	
	private void additionalAction_SaveAs() {
		//- we should call it directly - I have put code below from Commands
		
		//<start-adobe><start-thin>// <start-me>
		SwingGUI swingGUI=((SwingGUI)decode_pdf.getExternalHandler(Options.SwingContainer));

		if(swingGUI!=null)
           	swingGUI.currentCommands.executeCommand(Commands.SAVEFORM, null);
		//<end-thin><end-adobe>// <end-me>
	}

	private void additionalAction_URI(int eventType, String url) {
		
		if (showMethods)
			System.out.println("DefaultActionHandler.additionalAction_URI()");
		
		//as we only call this now when we need to action the url just call it.
		try {
			BrowserLauncher.openURL(url);
		} catch (IOException e1) {
			actionFactory.showMessageDialog(Messages.getMessage("PdfViewer.ErrorWebsite"));
		}
	}

	private void additionalAction_Hide(int eventType, PdfObject aData) {
		if (showMethods)
			System.out.println("DefaultActionHandler.additionalAction_Hide()");

			FieldsHideObject fieldsToHide = new FieldsHideObject();
	        
			getHideMap(aData, fieldsToHide);

			actionFactory.setFieldVisibility(fieldsToHide);
	}

	private void additionalAction_SubmitForm(PdfObject aData) {
		if(showMethods)
			System.out.println("DefaultActionHandler.additionalAction_SubmitForm()");
		
		boolean newExcludeList=false;
		String newSubmitURL=null;
		String[] newListOfFields=null;
		
		//get the F dictionary
        PdfObject dict=aData.getDictionary(PdfDictionary.F);
        //then get the submit URL to use
        if(dict!=null)
		newSubmitURL = dict.getTextStreamValue(PdfDictionary.F);
        
		//get the fields we need to change
		PdfArrayIterator fieldList = aData.getMixedArray(PdfDictionary.Fields);
		if (fieldList != null) {
			if (fieldList.getTokenCount() < 1)
				fieldList = null;

			if (fieldList != null) {
				// code goes here
				int fieldIndex = 0;
				newListOfFields = new String[fieldList.getTokenCount()];

				// go through list of fields and store so we can send
				String formObject;
				String tok, preName = null;
				StringBuffer names = new StringBuffer();
				while (fieldList.hasMoreTokens()) {
					formObject = fieldList.getNextValueAsString(true);

					if (formObject.indexOf(".x") != -1) {
						preName = formObject.substring(formObject.indexOf('.') + 1,
								formObject.indexOf(".x") + 1);
					}
					if (formObject.indexOf(" R") != -1) {

                       FormObject formObj=new FormObject(formObject);
                       currentPdfFile.readObject(formObj);


                        tok=formObj.getTextStreamValue(PdfDictionary.T);
                           if (preName != null) {
							names.append(preName);
						}
						names.append(tok);
						names.append(',');

					}
				}

				newListOfFields[fieldIndex++] = names.toString();
			}// end of code section
		}// END of Fields defining
		
		//if there was a list of fields read the corresponding Flags see pdf spec v1.6 p662
		if (newListOfFields != null) {
			// if list is null we ignore this flag anyway
			int flags = aData.getInt(PdfDictionary.Flags);

			if ((flags & 1) == 1) {
				// fields is an exclude list
				newExcludeList = true;
			}
		}// END of if exclude list ( Flags )
		
		// send our values to the actioning method
		actionFactory.submitURL(newListOfFields, newExcludeList, newSubmitURL);
	}

	private void additionalAction_ResetForm() {
		if (showMethods)
    		System.out.println("DefaultActionHandler.additionalAction_ResetForm()");

		actionFactory.reset(null);
	}

    /**
     * public as also called from Viewer to reset
     *
     * new page or -1 returned
     * @param aData
     * @param eventType
     * @param command
     */
	public int gotoDest(PdfObject aData, int eventType, int command) {

        final boolean debugDest=false;
        
        //aData can either be in top level of Form (as in Annots)
        //or second level (as in A/ /D - this allows for both
        //whoch this routine handles
        PdfObject a2=aData.getDictionary(PdfDictionary.A);
        if(a2!=null)
                aData=a2;
        
        //new page or -1 returned
        int page=-1;

        if (showMethods)
			System.out.println("DefaultActionHandler.gotoDest()");

		PdfArrayIterator Dest = aData.getMixedArray(PdfDictionary.Dest);
		if (Dest!=null) {

			if (eventType == MOUSECLICKED) {

				//allow for it being an indirect named object and convert if so
				if(Dest.getTokenCount()==1){
//					System.out.println("val="+ Dest.getNextValueAsString(false));

					String ref=decode_pdf.getIO().convertNameToRef( Dest.getNextValueAsString(false));
                    if(ref!=null){

                        //can be indirect object stored between []
                        if(ref.charAt(0)=='['){
                            if(debugDest)
                                System.out.println("data for named obj "+ref);

                            byte[] raw=ref.getBytes();
                            //replace char so subroutine works -ignored but used as flag in routine
                            raw[0]= 0;

                            ArrayDecoder objDecoder=new ArrayDecoder(decode_pdf.getIO().getObjectReader(), 0, raw.length, PdfDictionary.VALUE_IS_MIXED_ARRAY,null, PdfDictionary.Names);
                            objDecoder.readArray(false, raw, aData, PdfDictionary.Dest);
                            
                            Dest=aData.getMixedArray(PdfDictionary.Dest);

                        }else{
                            if(debugDest)
                                System.out.println("convert named obj "+ref);

                            aData=new OutlineObject(ref);
                            decode_pdf.getIO().readObject(aData);
                            Dest=aData.getMixedArray(PdfDictionary.Dest);
                        }
					}
				}
				
				String filename = aData.getTextStreamValue(PdfDictionary.F);

                if(filename==null){
    				PdfObject fDic = aData.getDictionary(PdfDictionary.F);

                    if(fDic!=null)
		    			filename = fDic.getTextStreamValue(PdfDictionary.F);
				}

                //add path if none present
                if(filename!=null && filename.indexOf('/')==-1 && filename.indexOf('\\')==-1)
                    filename=decode_pdf.getObjectStore().getCurrentFilepath()+filename;

                //removed \\ checking from iff so slashIndex will work, and 
                //stop null pointer exceptions, \\ will also be quicker.
                if(filename!=null){
                	
                	//if we have any \\ then replace with /
                	int index = filename.indexOf("\\");
                	while(index!=-1){
                		//for some reason String.replaceAll didnt like "\\" so done custom
                		filename = filename.substring(0,index)+
                		"/"+filename.substring(index+("\\".length()),filename.length());
                		index = filename.indexOf("\\");
                	}
                	
                	//if we dont start with a /,./ or ../ or #:/ then add ./
                	int slashIndex = filename.indexOf(":/");
                	if(slashIndex==-1 || slashIndex>1){
                		File fileStart = new File(decode_pdf.getFileName());
                		filename = fileStart.getParent()+"/"+filename;
                	}
                }
            	

				// new version - read Page Object to jump to
				String pageRef = "";

				if (Dest.getTokenCount() > 0){


                    //get pageRef as number of ref
                    int possiblePage=Dest.getNextValueAsInteger(false)+1;
					pageRef = Dest.getNextValueAsString(true);

                    //convert to target page if ref or ignore

                    if(pageRef.endsWith(" R"))
					    page = decode_pdf.getPageFromObjectRef(pageRef);
                    else if(possiblePage>0){ //can also be a number (cant check range as not yet open)
                        page=possiblePage;
                    }

                    if(debugDest)
                        System.out.println("pageRef="+pageRef+" page="+page+" "+aData.getObjectRefAsString());

                    //allow for named Dest
                    if(page==-1){
                        String newRef=decode_pdf.getIO().convertNameToRef(pageRef);

                        //System.out.println(newRef+" "+decode_pdf.getIO().convertNameToRef(pageRef+"XX"));

                        if(newRef!=null && newRef.endsWith(" R"))
					    page = decode_pdf.getPageFromObjectRef(newRef);

                    }
                    //commented out by mark as named dest should now be handled and -1 shows no page
					//if(page==-1){
						//we probably have a named destination
					//	page = 1;
					//}
				}

                //added by Mark so we handle these types of links as well in code below with no Dest
                //<</Type/Annot/Subtype/Link/Border[0 0 0]/Rect[56 715.1 137.1 728.9]/A<</Type/Action/S/GoToR/F(test1.pdf)>>
                if(Dest.getTokenCount()==0 && aData.getNameAsConstant(PdfDictionary.S)==PdfDictionary.GoToR)
                    command=PdfDictionary.GoToR;

//				boolean openInNewWindow = aData.getBoolean(PdfDictionary.NewWindow);

                if(debugDest)
                       System.out.println("Command="+PdfDictionary.showAsConstant(command));

				switch(command){
				case PdfDictionary.Dest :
					//read all the values
					if (Dest.getTokenCount()>1) {

						//get type of Dest
						//System.out.println("Next value as String="+Dest.getNextValueAsString(false)); //debug code to show actual value (note false so does not roll on)
						int type=Dest.getNextValueAsConstant(true);

                        if(debugDest)
                            System.out.println("Type="+PdfDictionary.showAsConstant(type));

                        Integer scale = null;
						Rectangle position=null;

	                    // - I have added all the keys for you and
	                    //changed code below. If you run this on baseline,
	                    //with new debug flag testActions on in DefaultAcroRender
	                    // it will exit when it hits one
	                    //not coded
						
						//type of Dest (see page 552 in 1.6Spec (Table 8.2) for full list)
	                    switch(type){
	                        case PdfDictionary.XYZ: //get X,y values and convert to rectangle which we store for later

	                            //get x and y, (null will return 0)
	                            float x=Dest.getNextValueAsFloat();
	                            float y=Dest.getNextValueAsFloat();

	                            //third value is zoom which is not implemented yet

	                            //create Rectangle to scroll to
	                            position=new Rectangle((int)x,(int)y,10,10);

	                            break;
	                        case PdfDictionary.Fit: //type sent in so that we scale to Fit.
	                        	scale = new Integer(-3);//0 for width in scaling box and -3 to show its an index
	                        	break;
	                        	
	                        case PdfDictionary.FitB: 
	                        	/*[ page /FitB ] - (PDF 1.1) Display the page designated by page, with its contents 
	                        	 * magnified just enough to fit its bounding box entirely within the window both 
	                        	 * horizontally and vertically. If the required horizontal and vertical magnification 
	                        	 * factors are different, use the smaller of the two, centering the bounding box 
	                        	 * within the window in the other dimension.
	                        	 */
	                        	//scale to same as Fit so use Fit.
	                        	scale = new Integer(-3);//0 for width in scaling box and -3 to show its an index
	                        
	                        	break;
	                        	
	                        case PdfDictionary.FitH:
	                        	/* [ page /FitH top ] - Display the page designated by page, with the vertical coordinate 
	                        	 * top positioned at the top edge of the window and the contents of the page magnified 
	                        	 * just enough to fit the entire width of the page within the window. A null value for 
	                        	 * top specifies that the current value of that parameter is to be retained unchanged.
	                        	 */
	                        	//scale to width
	                        	scale = new Integer(-1);//2 for width in scaling box and -3 to show its an index 
	                        	
	                        	//and then scroll to location
	                        	float top=Dest.getNextValueAsFloat();
	                            
	                            //create Rectangle to scroll to
	                            position=new Rectangle(10,(int)top,10,10);

	                            break;
	                        	
	                        	/* [ page /FitV left ] - Display the page designated by page, with the horizontal 
	                        	 * coordinate left positioned at the left edge of the window and the contents of 
	                        	 * the page magnified just enough to fit the entire height of the page within the window. 
	                        	 * A null value for left specifies that the current value of that parameter is to be 
	                        	 * retained unchanged.
	                        	 */
	                        	
	                        	/* [ page /FitR left bottom right top ] - Display the page designated by page, with its 
	                        	 * contents magnified just enough to fit the rectangle specified by the coordinates left, 
	                        	 * bottom, right, and topentirely within the window both horizontally and vertically. 
	                        	 * If the required horizontal and vertical magnification factors are different, use 
	                        	 * the smaller of the two, centering the rectangle within the window in the other 
	                        	 * dimension. A null value for any of the parameters may result in unpredictable behavior.
	                        	 */
	                        	
	                        	/* [ page /FitB ] - (PDF 1.1) Display the page designated by page, with its contents 
	                        	 * magnified just enough to fit its bounding box entirely within the window both 
	                        	 * horizontally and vertically. If the required horizontal and vertical magnification 
	                        	 * factors are different, use the smaller of the two, centering the bounding box within 
	                        	 * the window in the other dimension.
	                        	 */
	                        	
	                        	/* [ page /FitBH top ] - (PDF 1.1) Display the page designated by page, with the vertical 
	                        	 * coordinate top positioned at the top edge of the window and the contents of the page 
	                        	 * magnified just enough to fit the entire width of its bounding box within the window. 
	                        	 * A null value for top specifies that the current value of that parameter is to be retained 
	                        	 * unchanged.
	                        	 */
	                        	/* [ page /FitBV left ] - (PDF 1.1) Display the page designated by page, with the horizontal 
	                        	 * coordinate left positioned at the left edge of the window and the contents of the page 
	                        	 * magnified just enough to fit the entire height of its bounding box within the window. 
	                        	 * A null value for left specifies
	                        	  */
	                        default:

	                    }

						changeTo(filename, page, position,scale,true);
					}
					break;
					
				case PdfDictionary.Goto:
					// S /Goto or /GoToR action is a goto remote file action,
					// F specifies the file (GoToR only)
					// D specifies the location or page


                    if(page!=-1)
					changeTo(null, page, null,null,true);

					break;

				case PdfDictionary.GoToR:
					//A /GoToR action is a goto remote file action,
					//F specifies the file
					//D specifies the location or page
					//NewWindow a flag specifying whether to open it in a new window. 

					int index = pageRef.indexOf("P.");
					if (index != -1) {
						pageRef = pageRef.substring(index + 2, pageRef.length());
						page = Integer.parseInt(pageRef);
					} else if (pageRef.equals("F")) {
						//use file only
						page = 1;
					} else {
						//if no pageRef defined default to one, confirmed by working example
						page = 1;
					}

					//NOTE: filename full authenticated above dont redo.
					if (new File(filename).exists()) {

						//Open this file, on page 'page'
						if(page!=-1)
							changeTo(filename, page, null,null,true);

						LogWriter.writeFormLog("{DefaultActionHamdler.A} Form has GoToR command, needs methods for opening new file on page specified", FormStream.debugUnimplemented);
					} else {
						actionFactory.showMessageDialog("The file specified " + filename + " Does Not Exist!");
					}
					break;
				default:
				}
			} else
				actionFactory.setCursor(eventType);
		}

        return page;
	}

        //<start-me>
	private void additionalAction_Print(int eventType) {
		if (showMethods)
			System.out.println("DefaultActionHandler.additionalAction_Print()");

		if (eventType == MOUSERELEASED)
			actionFactory.print();

	}
        //<end-me>

	/**
	 * display signature details in popup frame
	 * @param formObj
	 * @param eventType
	 */
	private void additionalAction_Signature(FormObject formObj, int eventType) {
		if (showMethods)
    		System.out.println("DefaultActionHandler.additionalAction_Signature()");

		if (eventType == MOUSECLICKED) {

            PdfObject sigObject=formObj.getDictionary(PdfDictionary.V);//.getDictionary(PdfDictionary.Sig);

			if (sigObject == null)
				return;

			actionFactory.showSig(sigObject);

		} else
			actionFactory.setCursor(eventType);
	}

	/**
	 * this calls the PdfDecoder to open a new page and change to the correct page and location on page,
	 * is any value is null, it means leave as is.
	 * @param type - the type of action
	 */
	public void changeTo(String file, int page, Object location, Integer type,boolean storeView) {
		
		if (showMethods)
			System.out.println("DefaultActionHandler.changeTo()"+file);

		// open file 'file'
		if (file != null) {
			try {

                //we are working at '2 levels'. We have the Viewer and the
                //instance of PdfDecoder. If we only open in PDFDecoder, GUI thinks it is
                //still the original file, which causes issues. So we have to change file
                //at the viewer level.

                 //<start-adobe><start-thin>// <start-me>
                //added to check the forms save flag to tell the user how to save the now changed pdf file

                org.jpedal.examples.simpleviewer.gui.SwingGUI gui = ((org.jpedal.examples.simpleviewer.gui.SwingGUI) decode_pdf.getExternalHandler(Options.SwingContainer));
                if(gui!=null){
                    gui.stopThumbnails();
                   // gui.checkformSavedMessage();
                }

                if(file.startsWith("http://") || file.startsWith("ftp://") || file.startsWith("https:")){
                    if(gui!=null)
                        gui.currentCommands.executeCommand(Commands.OPENURL,new Object[]{file});
                    else
					    decode_pdf.openPdfFileFromURL(file, true);
				}else {
                    if(gui!=null)
                        gui.currentCommands.executeCommand(Commands.OPENFILE,new Object[]{file});
                    else
					decode_pdf.openPdfFile(file);
				}


                //<end-thin><end-adobe>// <end-me>

                

				if(page==-1)
					page=1;
			} catch (Exception e) {
			}
		}

		// change to 'page'
		if (page != -1) {
			//we should use +1 as we reference pages from 1.
			if(decode_pdf.getPageCount()!=1 && decode_pdf.getlastPageDecoded()!=page){
				if (page > 0 && page < decode_pdf.getPageCount()+1) {
					try {
						decode_pdf.decodePage(page);
	
						decode_pdf.updatePageNumberDisplayed(page);
					} catch (Exception e) {
						e.printStackTrace();
					}
	
					/** reset as rotation may change! */
					decode_pdf.setPageParameters(-1, page);

				}
			}
		}

		//<start-adobe><start-thin>// <start-me>
		if(type!=null){
			//now available via callback
			Object swingGUI = this.decode_pdf.getExternalHandler(org.jpedal.external.Options.SwingContainer);

        	/**
        	 * Display the page designated by page, with its contents magnified just enough to 
        	 * fit the entire page within the window both horizontally and vertically. 
        	 * If the required horizontal and vertical magnification factors are different, 
        	 * use the smaller of the two, centering the page within the window in the other 
        	 * dimension.
        	 */
            //set to fit - please use full paths (we do not want in imports as it will break Adobe version)
            if(swingGUI!=null){
            	if(type.intValue()<0){
            		//set scaling box to 0 index, which is scale to window
            		((org.jpedal.examples.simpleviewer.gui.SwingGUI)swingGUI).setSelectedComboIndex(org.jpedal.examples.simpleviewer.Commands.SCALING,type.intValue()+3);
            	}else {
            		//set scaling box to actual scaling value
            		((org.jpedal.examples.simpleviewer.gui.SwingGUI)swingGUI).setSelectedComboItem(org.jpedal.examples.simpleviewer.Commands.SCALING,type.toString());
            	}
            }
		}
		
		//<end-thin><end-adobe>// <end-me>
		
		//scroll to 'location'
        if (location != null) {
            Point p = decode_pdf.getPageOffsets(page);
            double scaling = decode_pdf.getScaling();
            double x = ((decode_pdf.getPdfPageData().getMediaBoxWidth(page) - ((Rectangle)location).getX())*scaling) + p.x;
            double y = ((decode_pdf.getPdfPageData().getCropBoxHeight(page) - ((Rectangle)location).getY())*scaling) + p.y;

            location = new Rectangle((int)x, (int)y, (int)decode_pdf.getVisibleRect().getWidth(), (int)decode_pdf.getVisibleRect().getHeight());

            decode_pdf.scrollRectToVisible((Rectangle) location);
        }

		//<start-adobe><start-thin>// <start-me>
		SwingGUI swingGUI=((SwingGUI)decode_pdf.getExternalHandler(Options.SwingContainer));
		if(swingGUI!=null){
			((org.jpedal.examples.simpleviewer.gui.SwingGUI)swingGUI).zoom(true);
	
			if(storeView)
				swingGUI.currentCommands.executeCommand(Commands.ADDVIEW,new Object[]{new Integer(page),location,type});
		}
		//<end-thin><end-adobe>// <end-me>

        decode_pdf.repaint();
    }

    public PdfDecoder getPDFDecoder() {
        return decode_pdf;
    }

	/**
	 * E action when cursor enters active area
	 */
	public void E(Object e, FormObject formObj) {
		if (showMethods)
			System.out.println("DefaultActionHandler.E()");
		
	}

	/**
	 * X action when cursor exits active area
	 */
	public void X(Object e, FormObject formObj) {
		if (showMethods)
			System.out.println("DefaultActionHandler.X()");

	}

	/**
	 * D action when cursor button pressed inside active area
	 */
	public void D(Object e, FormObject formObj) {
		if (showMethods)
			System.out.println("DefaultActionHandler.D()");

	}

	/**
	 * U action when cursor button released inside active area
	 */
	public void U(Object e, FormObject formObj) {
		if (showMethods)
			System.out.println("DefaultActionHandler.U()");

	}

	/**
	 * Fo action on input focus
	 */
	public void Fo(Object e, FormObject formObj) {     //TODO called with focus gained
		if (showMethods)
			System.out.println("DefaultActionHandler.Fo()");


		// Scan through the fields and change any that have changed
		acrorend.updateChangedForms();
	}

	/**
	 * Bl action when input focus lost, blur
	 */
	public void Bl(Object e, FormObject formObj) { // TODO called by focus lost
		if (showMethods)
			System.out.println("DefaultActionHandler.Bl()");

	}

	/**
	 * O called when a page is opened
	 */
	public void O(PdfObject pdfObject,int type) {
		if (showMethods)
			System.out.println("DefaultActionHandler.O()");


        //@chris re - I don't think we need this
        //@mark - theoretically we will need it to update the forms, or to make sure they are updated.
        //the proxy code it may not need to be called but i thin we may have dropped that.
        
        //Scan through the fields and change any that have changed
		acrorend.updateChangedForms();
	}

	/**
	 * PO action when page containing is opened,
	 * actions O of pages AA dic, and OpenAction in document catalog should be done first
	 */
	public void PO(PdfObject pdfObject,int type) {
		if (showMethods)
			System.out.println("DefaultActionHandler.PO()");


		// Scan through the fields and change any that have changed
		acrorend.updateChangedForms();
	}

	/**
	 * PC action when page is closed, action C from pages AA dic follows this
	 */
	public void PC(PdfObject pdfObject,int type) {
		if (showMethods)
			System.out.println("DefaultActionHandler.PC()");
		

		// Scan through the fields and change any that have changed
		acrorend.updateChangedForms();
	}

	/**
	 * PV action on viewing containing page
	 */
	public void PV(PdfObject pdfObject,int type) {
		if (showMethods)
			System.out.println("DefaultActionHandler.PV()");


		// Scan through the fields and change any that have changed
		acrorend.updateChangedForms();
	}

	/**
	 * PI action when page no longer visible in viewer
	 */
	public void PI(PdfObject pdfObject,int type) {
		if (showMethods)
			System.out.println("DefaultActionHandler.PI()");


		// Scan through the fields and change any that have changed
		acrorend.updateChangedForms();
	}

	/**
	 * when user types a keystroke
	 * K action on - [javascript]
	 * keystroke in textfield or combobox
	 * modifys the list box selection
	 * (can access the keystroke for validity and reject or modify)
	 */
	public int K(Object ex, FormObject formObj, int actionID) {
		if (showMethods)
			System.out.println("DefaultActionHandler.K()");

		int result = 0;

		
		return result;
	}

	/**
	 * F the display formatting of the field (e.g 2 decimal places) [javascript]
	 */
	public void F(FormObject formObj) {
		if (showMethods)
			System.out.println("DefaultActionHandler.F()");

	}

	/**
	 * V action when fields value is changed [javascript], validate
	 */
	public void V(Object ex, FormObject formObj, int actionID) {
		if (showMethods)
			System.out.println("DefaultActionHandler.V()");
		
		//set this fields value within the FormObject so javascript actions are correct
		//String fieldRef = formObj.getPDFRef();
		String fieldRef = formObj.getObjectRefAsString();
		acrorend.getCompData().storeDisplayValue(fieldRef);

	}

	Map Ccalled = new HashMap();
	/**
	 * C action when another field changes (recalculate this field) [javascript]
	 * <p/>
	 * NOT actually called as called from other other objects but here for completeness
	 */
	public void C(FormObject formObj) {
		if (showMethods)
			System.out.println("DefaultActionHandler.C() called from="+formObj.getObjectRefAsString());
			
		if(Ccalled.get(formObj.getObjectRefAsString())!=null)
			return;
		Ccalled.put(formObj.getObjectRefAsString(), "1");
		
		Ccalled.remove(formObj.getObjectRefAsString());
	}

	/**
	 * goes through the map and adds the required data to the hideMap and returns it
	 */
	private static void getHideMap(PdfObject aData, final FieldsHideObject fieldToHide) {
		if (showMethods)
			System.out.println("DefaultActionHandler.getHideMap()");
		
		String[] fieldstoHide = fieldToHide.getFieldArray();
		boolean[] whethertoHide = fieldToHide.getHideArray();
		
		if (aData.getTextStreamValue(PdfDictionary.T) != null) {
			String fieldList = aData.getTextStreamValue(PdfDictionary.T);
			if(fieldList!=null){
				String[] fields;
				if (fieldstoHide.length>0){
					fields = new String[fieldstoHide.length + 1];
					System.arraycopy(fieldstoHide, 0, fields, 0, fieldstoHide.length);
					fields[fields.length - 1] = fieldList;
				} else {
					fields = new String[]{fieldList};
				}
				fieldstoHide = fields;
			}
		}
		
		boolean hideFlag = aData.getBoolean(PdfDictionary.H);
		
		boolean[] hideFlags;
		if (whethertoHide.length>0){
			hideFlags = new boolean[whethertoHide.length + 1];
			System.arraycopy(whethertoHide, 0, hideFlags, 0, whethertoHide.length);
			hideFlags[hideFlags.length - 1] = hideFlag;
		} else {
			hideFlags = new boolean[] { hideFlag };
		}
		whethertoHide = hideFlags;
		
		//put values back into fields to hide object
		fieldToHide.setFieldArray(fieldstoHide);
		fieldToHide.setHideArray(whethertoHide);
		
		if (aData.getDictionary(PdfDictionary.Next)!=null) {
			PdfObject nextDic = aData.getDictionary(PdfDictionary.Next);
			getHideMap(nextDic, fieldToHide);
		}
	}

	public PdfLayerList getLayerHandler() {
		return (PdfLayerList)decode_pdf.getJPedalObject(PdfDictionary.Layer);

	}

	//<start-adobe><start-thin>// <start-me>
	public void setMouseHandler(SwingMouseListener swingMouseHandler) {
		this.swingMouseHandler = swingMouseHandler;
	}
	
	public void updateCordsFromFormComponent(MouseEvent e, boolean mouseClicked) {
		
		if(swingMouseHandler!=null){
			swingMouseHandler.updateCordsFromFormComponent(e);
			swingMouseHandler.checkLinks(mouseClicked,decode_pdf.getIO());
		}
	}

    public SwingMouseListener getSwingMouseHandler() {
        return swingMouseHandler;
    }
    //<end-thin><end-adobe>// <end-me>
}
