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
 * SwingData.java
 * ---------------
 */
package org.jpedal.objects.acroforms.formData;

import org.jpedal.exception.PdfException;
import org.jpedal.external.CustomFormPrint;
import org.jpedal.io.PdfObjectReader;
import org.jpedal.constants.ErrorCodes;
import org.jpedal.objects.acroforms.actions.ActionHandler;
import org.jpedal.objects.acroforms.actions.DefaultActionHandler;
import org.jpedal.objects.acroforms.overridingImplementations.FixImageIcon;
import org.jpedal.objects.acroforms.overridingImplementations.PdfSwingPopup;
import org.jpedal.objects.acroforms.overridingImplementations.ReadOnlyTextIcon;
import org.jpedal.objects.acroforms.rendering.AcroRenderer;
// <start-me>
import org.jpedal.objects.acroforms.utils.ConvertToString;
// <end-me>
import org.jpedal.objects.acroforms.utils.FormUtils;
import org.jpedal.objects.acroforms.creation.FormFactory;
import org.jpedal.objects.acroforms.creation.JPedalBorderFactory;
import org.jpedal.objects.raw.PageObject;
import org.jpedal.objects.raw.PdfDictionary;
import org.jpedal.objects.raw.FormObject;
import org.jpedal.objects.raw.PdfObject;
import org.jpedal.utils.LogWriter;
import org.jpedal.utils.Strip;
import org.jpedal.Display;
import org.jpedal.PdfDecoder;


import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.*;
import java.util.List;

/**
 * Swing specific implementation of Widget data
 * (all non-Swing variables defined in ComponentData)
 * 
 */
public class SwingData extends ComponentData {

    //used to enable work around for bug in JDK1.6.0_10+
    public static boolean JVMBugRightAlignFix =false;

	//private static final boolean tryNewSortRoutine = true;

    CustomFormPrint customFormPrint=null;

    PdfObjectReader currentPdfFile=null;

	/**
	 * array to hold components
	 */
	private Component[] allFields;
	
	/**
	 * flag to show if checked
	 */
	private boolean[] testedForDuplicates;

	/**useNews
	 * possible wrapper for some components such as JList which do not have proper scrolling
	 */
	private JScrollPane[] scroll;

	/**
	 * panel components attached to
	 */
	private JPanel panel;

	/** we store Buttongroups in this */
	private Map annotBgs = new HashMap();
	
	/**
	 * generic call used for triggering repaint and actions
	 */
	public void loseFocus() {
		/*if (panel != null) {
			if (SwingUtilities.isEventDispatchThread())
				panel.grabFocus();
			else {
				final Runnable doPaintComponent = new Runnable() {
					public void run() {
						panel.grabFocus();
					}
				};
				SwingUtilities.invokeLater(doPaintComponent);
			}
		}*/
	}

//	/**
//	 * set the type of object for each Form created
//	 * @param fieldName
//	 * @param type
//	 */
//	public void setFormType(String fieldName, Integer type) {
//
//		typeValues.put(fieldName, type);
//
//	}

	/**
	 * return components which match object name
	 * @return
	 */
	private Object[] getComponentsByName(String objectName, Object checkObj,boolean collateIndexs) {

        //avoid double counting duplicates
        Map valuesCounted= new HashMap();

		// allow for duplicates
		String duplicateComponents = (String) duplicates.get(objectName);

		int index = ((Integer) checkObj).intValue();
        valuesCounted.put(String.valueOf(index),"x");

		boolean moreToProcess = true;
		int firstIndex = index;
		while (moreToProcess) {
			if (index + 1 < allFields.length && allFields[index + 1] != null) {

				String name = allFields[index + 1].getName();
				if(name==null){	//we now pass Annots through so need to allow for no name
					moreToProcess = false;
				} else if (FormUtils.removeStateToCheck(name, false).equals(objectName)) {
                    valuesCounted.put(String.valueOf((index + 1)),"x");

                    index += 1;

				} else {
					moreToProcess = false;
				}
			} else
				moreToProcess = false;
		}

		int size = (index + 1) - firstIndex;

        Component[] compsToRet = new Component[size];
        
        if(NEW_HIDE_COMPS_BEHIND && collateIndexs){
			//add all current indexs from valuescounted map only as there are no duplicates
			indexs = new int[size];
        }
        
		for (int i = 0; i < size; i++, firstIndex++) {
			compsToRet[i] = allFields[firstIndex];
			
			if(NEW_HIDE_COMPS_BEHIND && collateIndexs){
				indexs[i] = firstIndex;
			}
			
			if (firstIndex == index)
				break;
		}
		
		// recreate list and add in any duplicates
		if (duplicateComponents != null && duplicateComponents.indexOf(',') != -1) {

			StringTokenizer additionalComponents = new StringTokenizer(duplicateComponents, ",");

			int count = additionalComponents.countTokens();

            //avoid double-counting
            int alreadyCounted=0;
            String[] keys=new String[count];
            for(int ii=0;ii<count;ii++){
                keys[ii]=additionalComponents.nextToken();
                if(valuesCounted.containsKey(keys[ii])){
                    alreadyCounted++;
                    keys[ii]=null;
                }
            }
            count=count-alreadyCounted;

			Component[] origComponentList = compsToRet;
			compsToRet = new Component[size + count];

			// add in original components
			System.arraycopy(origComponentList, 0, compsToRet, 0, size);

			if(NEW_HIDE_COMPS_BEHIND && collateIndexs){
				//collate original sized array with new size needed
				int[] tmpind = indexs;
				
				indexs = new int[compsToRet.length];
				// add in original components
				System.arraycopy(tmpind, 0, indexs, 0, size);
			}
			
			// and duplicates
            int ii;
			for (int i = 0; i < count; i++) {

                if(keys[i]==null) //ignore if removed above
                continue;

				ii = Integer.parseInt(keys[i]);
				
				if(NEW_HIDE_COMPS_BEHIND && collateIndexs){
					//add index ii for all other fields aswell
					indexs[i+size] = ii;
				}
				
				// System.out.println(ii+" "+count);
				compsToRet[i + size] = allFields[ii];
			}
		}
		
		return compsToRet;
	}

	/**
	 * get value using objectName or field pdf ref.
	 * Pdf ref is more acurate
	 */
	public Object getValue(Object objectName) {

		if (objectName == null)
			return "";

		Object checkObj;
		if (((String) objectName).indexOf("R") != -1) {
			checkObj = refToCompIndex.get(objectName);
		} else {
			checkObj = nameToCompIndex.get(objectName);
		}

		Object retValue = "";
		retValue = getFormValue(checkObj);

		return retValue;

	}
	
	/**valid flag used by Javascript to allow rollback
	 * &nbsp
	 * @param ref - name of the field to change the value of
	 * @param value - the value to change it to
	 * @param isValid - is a valid value or not
	 * @param isFormatted - is formatted properly
	 */
	public void setValue(String ref, Object value, boolean isValid, boolean isFormatted) {

        Object checkObj = super.setValue(ref, value, isValid, isFormatted, getValue(ref));
        
        //System.out.println("SwingData.setValue()"+ref+" set display value="+value);
        //set the display fields value
        if(checkObj!=null)
            setFormValue(value, checkObj);
        
        //idea to move flagging into here, thus allowing kids to be synced easily, but needs a lot of tweaking to work.
//        flagLastUsedValue(allFields[((Integer) checkObj).intValue()], (FormObject)rawFormData.get(ref), true);
	}

	public String getComponentName(int currentComp, ArrayList nameList, String lastName) {

		String currentName;

		Component currentField = allFields[currentComp];

		if (currentField != null) {
			// ensure following fields don't get added if (e.g they are a group)

			currentName = FormUtils.removeStateToCheck(currentField.getName(), false);
			//TODO we can remove lastName as not used.
			if (currentName != null){// && !lastName.equals(currentName)) {

				if (!testedForDuplicates[currentComp]) {
					// stop multiple matches
					testedForDuplicates[currentComp] = true;

					// track duplicates
					String previous = (String) duplicates.get(currentName);
					if (previous != null)
						duplicates.put(currentName, previous + ',' + currentComp);
					else
						duplicates.put(currentName, String.valueOf(currentComp));
				}

				// add to list
				nameList.add(currentName);
				lastName = currentName;
			}
		}
		return lastName;
	}

	public Object[] getComponentsByName(String objectName) {

		if (objectName == null)
			return allFields;
		
		Object checkObj = nameToCompIndex.get(objectName);
		if (checkObj == null)
				return null;

		if (checkObj instanceof Integer) {
			// return allFields[index];
			return getComponentsByName(objectName, checkObj,false);
		} else {
			LogWriter.writeLog("{stream} ERROR DefaultAcroRenderer.getComponentByName() Object NOT Integer and NOT null");
			return null;
		}
	}

	public Object getFormValue(Object checkObj) {

		Object retValue = "";

		if (checkObj != null) {
			int index = ((Integer) checkObj).intValue();

			//converted to allfieldstype on 3/6/10
			if(allFieldsType[index]==FormFactory.checkboxbutton){
				retValue = Boolean.valueOf(((JCheckBox) allFields[index]).isSelected());
			}else if(allFieldsType[index]==FormFactory.combobox){
				retValue = ((JComboBox) allFields[index]).getSelectedItem();
			}else if(allFieldsType[index]==FormFactory.list){
				retValue = ((JList) allFields[index]).getSelectedValues();
			}else if(allFieldsType[index]==FormFactory.radiobutton){
				retValue = Boolean.valueOf(((JRadioButton) allFields[index]).isSelected());
			}else if(allFieldsType[index]==FormFactory.singlelinepassword
					 || allFieldsType[index]==FormFactory.multilinepassword || allFieldsType[index]==FormFactory.multilinetext){
				retValue = ((JTextComponent) allFields[index]).getText();
			}else if(allFieldsType[index]==FormFactory.singlelinetext){
				if(allFields[index] instanceof JButton){
					retValue = ((ReadOnlyTextIcon)((JButton) allFields[index]).getIcon()).getText();
				}else {
					retValue = ((JTextComponent) allFields[index]).getText();
				}
			}else if(allFieldsType[index]==FormFactory.pushbutton || allFieldsType[index]==FormFactory.annotation
					 || allFieldsType[index]==FormFactory.signature){
				//these are most likely readonly textfields, so ignore
			}else {

				retValue = "";
			}
		}
		return retValue;
	}

	/** sets the value for the displayed form */
	public void setFormValue(Object value, Object checkObj) {

		if (checkObj != null) {
			int index = ((Integer) checkObj).intValue();

			switch(allFieldsType[index]){
			case FormFactory.checkboxbutton:
				((JCheckBox) allFields[index]).setSelected(Boolean.valueOf((String) value).booleanValue());
				break;
			case FormFactory.combobox:
				((JComboBox) allFields[index]).setSelectedItem(value);
				break;
			case FormFactory.list:
				((JList) allFields[index]).setSelectedValue(value, false);
				break;
			case FormFactory.radiobutton:
				((JRadioButton) allFields[index]).setText((String) value);
				break;
			case FormFactory.singlelinepassword:
			case FormFactory.multilinepassword:
			case FormFactory.multilinetext:
				((JTextComponent) allFields[index]).setText((String) value);
				break;
			case FormFactory.singlelinetext:
				if(allFields[index] instanceof JButton){
					((ReadOnlyTextIcon)((JButton) allFields[index]).getIcon()).setText((String) value);
				}else {
					((JTextComponent) allFields[index]).setText((String) value);
				}
				break;
			case FormFactory.pushbutton:
			case FormFactory.annotation:
			case FormFactory.signature:
				//do not alter as is read only
				break;
			default:
				break;
			}
		}
	}
	
	public void debugForms() {
	}
	
	public void showForms() {
		
		if (allFields != null) {
			for (int i = 0; i < allFields.length; i++) {
				if (allFields[i] != null) {
					if(allFieldsType[i]==FormFactory.pushbutton || allFieldsType[i]==FormFactory.annotation
							|| allFieldsType[i]==FormFactory.signature) {
						allFields[i].setBackground(Color.blue);
					} else  if(allFieldsType[i]==FormFactory.singlelinepassword || allFieldsType[i]==FormFactory.singlelinetext
							 || allFieldsType[i]==FormFactory.multilinetext || allFieldsType[i]==FormFactory.multilinepassword){
						allFields[i].setBackground(Color.red);
						if(allFields[i] instanceof JButton)
							allFields[i].setBackground(Color.cyan);
					} else {
						allFields[i].setBackground(Color.green);
					}

					allFields[i].setForeground(Color.lightGray);
					allFields[i].setVisible(true);
					allFields[i].setEnabled(true);
					((JComponent) allFields[i]).setOpaque(true);
					if(allFieldsType[i]==FormFactory.pushbutton || allFieldsType[i]==FormFactory.checkboxbutton
							|| allFieldsType[i]==FormFactory.annotation || allFieldsType[i]==FormFactory.signature){
						((AbstractButton) allFields[i]).setIcon(null);
					} else if(allFieldsType[i]==FormFactory.combobox) {
						((JComboBox) allFields[i]).setEditable(false);
					}
				}
			}
		}
	}

	/**
	 * get actual widget using objectName as ref or null if none
	 * @param objectName
	 * @return
	 */
	public Object getWidget(Object objectName) {

		if (objectName == null)
			return null;
		else {

			Object checkObj;
			if (((String) objectName).indexOf("R") != -1) {
				checkObj = refToCompIndex.get(objectName);
			} else {
				checkObj = nameToCompIndex.get(objectName);
			}

			if (checkObj == null)
				return null;
			else {
				int index = ((Integer) checkObj).intValue();

				return allFields[index];
			}
		}
	}

	/**
	 * render component onto G2 for print of image creation
	 * @param printcombo = tells us to print the raw combobox, and dont do aymore formatting of the combobox, should only be called from this method. 
	 */
	private void renderComponent(Graphics2D g2, int currentComp, Component comp, int rotation,boolean printcombo, int indent,boolean isPrinting) {

		// if (showMethods)
		// System.out.println("DefaultAcroRenderer.renderComponent()");

		if (comp != null) {

			boolean editable = false;
			int page = 
				//fix only works for page 4 it breaks page 5, and 6
				getPageForFormObject(convertIDtoRef(currentComp));
				//works for all except page 4.
//				currentPage;


			if (!printcombo && comp instanceof JComboBox) {
			
				//if we have the comobobox, adapt so we see what we want to
				//for the combobox we need to print the first item within it otherwise we doent see the contents.
				JComboBox combo = (JComboBox) comp;
				
				if (combo.isEditable()) {
					editable = true;
					combo.setEditable(false);
				}
				
				if (combo.getComponentCount() > 0) {
					Object selected = combo.getSelectedItem();
					if (selected != null) {

						JTextField text = new JTextField();

						text.setText(selected.toString());

						text.setBackground(combo.getBackground());
						text.setForeground(combo.getForeground());
						text.setFont(combo.getFont());

                        text.setBorder(combo.getBorder());

                        renderComponent(g2, currentComp, text, rotation, false,indent,isPrinting);
					}
				}
                
				//set flag to say this is the combobox. 
				//(we dont want to print this, as we have printed it as a textfield )
				printcombo = true;
			}
			
			if(!printcombo){
				
				AffineTransform ax = g2.getTransform();
				
				//when true works on printing,
				//whnen false works for testrenderer, on most except eva_subjob_quer.pdf
				if(isPrinting){
					//if we dont have the combobox print it
					scaleComponent(page, 1, rotation, currentComp, comp, false,false,indent);
	
					Rectangle rect = comp.getBounds();

					//work out new translate after rotate deduced from FixImageIcon
					AffineTransform at;
					switch(360-rotation){
					case 270:
						at = AffineTransform.getRotateInstance(
								(270 * java.lang.Math.PI) / 180,0,0);
						g2.translate(comp.getBounds().y + cropOtherY[page]-insetH, 
								pageData.getCropBoxHeight(page)- comp.getBounds().x+insetW);
						
						
						g2.transform (at);
						g2.translate(-insetW, 0);
						
						break;
					case 90:
						at = AffineTransform.getRotateInstance(
								(90 * java.lang.Math.PI) / 180,0,0);
						g2.translate(comp.getBounds().y + cropOtherY[page]-insetH, 
								comp.getBounds().x+insetW);
						
						
						g2.transform (at);
						g2.translate(0, -insetH);
						break;
					case 180://not tested
						at = AffineTransform.getRotateInstance(
								(180 * java.lang.Math.PI) / 180,0,0);
						//translate to x,y of comp before applying rotate.
						g2.translate(comp.getBounds().x - insetW, comp.getBounds().y + cropOtherY[page]);
						
						g2.transform (at);
	//					g2.translate(-rect.width, -rect.height );
						g2.translate(-insetW, -insetH );//will prob need this to work
						
						break;
					default:
						//translate to x,y of comp before applying rotate.
						g2.translate(comp.getBounds().x - insetW, comp.getBounds().y + cropOtherY[page]);
						break;
					}
				}else {//used for testrenderer, images
					
					//if we dont have the combobox print it
					scaleComponent(page, 1, rotation, currentComp, comp, false,false,indent);
	
					Rectangle rect = comp.getBounds();

					//translate to x,y of comp before applying rotate.
					g2.translate(rect.x - insetW, rect.y + cropOtherY[page]);

					//only look at rotate on text fields as other fields should be handled.
					if(getFieldType(comp, true)==ComponentData.TEXT_TYPE){
						if(pageData.getRotation(page)==90 || pageData.getRotation(page)==270){
							comp.setBounds(rect.x, rect.y, rect.height, rect.width);
							rect = comp.getBounds();
						}
					
						//fix for file eva_subjob_quer.pdf as it has page rotations 90 0 90 0, which makes 
						//page 1 and 3 print wrong when using each pages rotation value.
						int rotate = rotation-pageData.getRotation(0);
						if(rotate<0)
							rotate = 360+rotate;
						
						//work out new translate after rotate deduced from FixImageIcon
						AffineTransform at;
						switch(rotate){
						case 270:
							at = AffineTransform.getRotateInstance(
									(rotate * java.lang.Math.PI) / 180,0,0);
							g2.transform (at);
							g2.translate(-rect.width, 0 );
							break;
						case 90://not tested
							at = AffineTransform.getRotateInstance(
									(rotate * java.lang.Math.PI) / 180,0,0);
							g2.transform (at);
							g2.translate(0, -rect.height );
							
							break;
						case 180://not tested
							at = AffineTransform.getRotateInstance(
									(rotate * java.lang.Math.PI) / 180,0,0);
							g2.transform (at);
							g2.translate(-rect.width, -rect.height );
							
							break;
						}
					}
				}

                /**
                 *  fix for bug in Java 1.6.0_10 onwards with right aligned values
                 */
				boolean isPainted=false;
				//<start-me>
				//hack for a very sepcific issue so rather leave
				//Rog's code intack and take out for ME
				if (JVMBugRightAlignFix && comp instanceof JTextField) {
					
					JTextField field = new JTextField();
					JTextField source=(JTextField)comp;

					if (source.getHorizontalAlignment() == JTextField.RIGHT) {
					
                                                field.setFont(source.getFont());
						field.setLocation(source.getLocation());
						field.setSize(source.getSize());
						field.setBorder(source.getBorder());
						field.setHorizontalAlignment(JTextField.RIGHT);
						//field.setText(new String(createCharArray(' ', maxLengthForTextOnPage - source.getText().length())) + source.getText());

						//Rog's modified code
                        int additionalBlanks = 0;
                        int width =g2.getFontMetrics(comp.getFont()).stringWidth(new
                                String(createCharArray(' ', maxLengthForTextOnPage -
                                source.getText().length())) + source.getText());
                        int eightPointWidth =
                                g2.getFontMetrics(comp.getFont().deriveFont(7.0F)).stringWidth(new
                                        String(createCharArray(' ', maxLengthForTextOnPage -
                                        source.getText().length())) + source.getText());
                        int difference = width - eightPointWidth;
                        if (difference > 0) {
                            additionalBlanks = difference /
                                    g2.getFontMetrics(comp.getFont().deriveFont(7.0F)).stringWidth(" ");
                        }
                        String originalTest = source.getText();
                        int bunchOfSpaces = (maxLengthForTextOnPage +
                                additionalBlanks) - source.getText().length();
                        field.setText(new String(createCharArray(' ',
                                bunchOfSpaces)) + originalTest);
                        width =
                                g2.getFontMetrics(comp.getFont()).stringWidth(field.getText());

                        int insets = 0;
                        if (field.getBorder() != null)
                            insets = (field.getBorder().getBorderInsets(field).left + field.getBorder().getBorderInsets(field).right);
                        boolean needsChange = false;
                        while (bunchOfSpaces > 0 && width > field.getWidth() - insets) {
                            bunchOfSpaces = (maxLengthForTextOnPage + additionalBlanks) - source.getText().length();
                            String newText = new String(createCharArray(' ', bunchOfSpaces)) + originalTest;
                            field.setText(newText);
                            additionalBlanks--;
                            width = g2.getFontMetrics(comp.getFont().deriveFont(7.0F)).stringWidth(field.getText());
                            needsChange = true;
                        };
                        if (needsChange) {
                            additionalBlanks--;
                            bunchOfSpaces = (maxLengthForTextOnPage + additionalBlanks) - source.getText().length();
                            String newText = new String(createCharArray(' ', bunchOfSpaces)) + originalTest;
                            field.setText(newText);
                        }

                        ////
                        field.paint(g2);
						isPainted=true;
					}
				}
				//<end-me>

				if(!isPainted)
					comp.paint(g2);

				g2.setTransform(ax);
			}

			if (editable /* && comp instanceof JComboBox */) {
				((JComboBox) comp).setEditable(true);
			}
		}
	}

    /**
     * used by fix above
     * @param c
     * @param count
     * @return
     */
    private static char[] createCharArray(char c, int count) {
       if(count <= 0) return new char[0];
       char[] result = new char[count];
       Arrays.fill(result, 0, result.length, c);
       return result;
   }

	boolean renderFormsWithJPedalFontRenderer = false;

	private Component[] popups = new Component[0];
	
	int maxLengthForTextOnPage=0;
    
	/**
	 * draw the forms onto display for print of image. Note different routine to
	 * handle forms also displayed at present
	 */
	public void renderFormsOntoG2(Object raw, int pageIndex, float currentScaling, int currentIndent,
                                  int currentRotation, Map componentsToIgnore,
                                  FormFactory formFactory, PdfObjectReader currentPdfFile, int pageHeight) {

		if(formsUnordered==null || rasterizeForms)
			return;

        this.componentsToIgnore=componentsToIgnore;

        //only passed in on print so also used as flag
        boolean isPrinting = formFactory!=null;
        
        //fix for issue with display of items in 1.6.0_10+
        if(JVMBugRightAlignFix && isPrinting){

            maxLengthForTextOnPage=0;

			//get unsorted components and iterate over forms
            Iterator formsUnsortedIterator = formsUnordered[pageIndex].iterator();
            while(formsUnsortedIterator.hasNext()){

                Component[] formComps = allFields;

            	//get ref from list and convert to index.
            	Object nextVal = formsUnsortedIterator.next();
            	int currentComp = ((Integer)refToCompIndex.get(nextVal)).intValue();


				if (formComps != null && currentComp != -1) {
                    Component comp = formComps[currentComp];

                    if(comp instanceof JTextField){
                        JTextField text=(JTextField)comp;
                        int newLength=text.getText().length();

                        if(newLength>maxLengthForTextOnPage && text.getHorizontalAlignment()==JTextField.RIGHT){
                            maxLengthForTextOnPage=newLength;
                           // System.out.println(maxLengthForTextOnPage+ " "+text.getText());
                        }
                    }

                }
            }
        }
        
		Graphics2D g2 = (Graphics2D) raw;

		AffineTransform defaultAf = g2.getTransform();

		// setup scaling
		AffineTransform aff = g2.getTransform();
		aff.scale(1, -1);
		aff.translate(0, -pageHeight - insetH);
		g2.setTransform(aff);

		// remove so don't appear rescaled on screen
		// if((currentPage==pageIndex)&&(panel!=null))
//		this.removeDisplayComponentsFromScreen(panel);//removed to stop forms disappearing on printing

		try {
				
			Component[] formComps = allFields;

			/** needs to go onto a panel to be drawn */
			JPanel dummyPanel = new JPanel();
			
			//get unsorted components and iterate over forms
            Iterator formsUnsortedIterator = formsUnordered[pageIndex].iterator();
            while(formsUnsortedIterator.hasNext()){
            	
            	//get ref from list and convert to index.
            	Object nextVal = formsUnsortedIterator.next();
            	int currentComp = ((Integer)refToCompIndex.get(nextVal)).intValue();

				if (formComps != null && currentComp != -1) {

					//is this form allowed to be printed
					boolean[] flags = ((FormObject) rawFormData.get(convertFormIDtoRef.get(new Integer(currentComp)))).getCharacteristics();
					if(flags[1] || (isPrinting && !flags[2])){//1 hidden, 2 print (hense !)
						continue;
					}
					
					Component comp = formComps[currentComp];
					
					if (comp != null && comp.isVisible()) {

                        /**
                         * sync kid values if needed
                         */
                        syncKidValues(currentComp);


						//wrap JList in ScrollPane to ensure displayed if size set to smaller than list
						// (ie like ComboBox)
						//@note - fixed by moving the selected item to the top of the list. 
						// this works for the file acro_func_baseline1.pdf
						//and now works on file fieldertest2.pdf and on formsmixed.pdf
						//but does not render correct in tests i THINK, UNCONFIRMED
						// leaves grayed out boxes in renderer.

						float boundHeight = boundingBoxs[currentComp][3]-boundingBoxs[currentComp][1];
						int swingHeight = comp.getPreferredSize().height;
						/**
						 * check if the component is a jlist, and if it is, then is their a selected item, 
						 * if their is then is the bounding box smaller than the jlist actual size
						 * then and only then do we need to change the way its printed
						 */
						if (renderFormsWithJPedalFontRenderer) {

							// get correct key to lookup form data
							String ref = this.convertIDtoRef(currentComp);


							//System.out.println(currentComp+" "+comp.getLocation()+" "+comp);
							Object[] rawForm = this.getRawForm(ref);
							for (int i = 0; i < rawForm.length; i++) {
								if (rawForm[i]!=null) {
									FormObject form = (FormObject) rawForm[i];
									System.out.println(ref+" "+form.getTextFont()+" "+form.getTextString());
									//@mark should this be there
								}
							}
                        }else if(isFormNotPrinted(currentComp)){
						}else if(comp instanceof JList && ((JList)comp).getSelectedIndex()!=-1 && boundHeight<swingHeight){

							JList comp2 = (JList) comp;

							dummyPanel.add(comp);

//								JList tmp = comp2;//((JList)((JScrollPane)comp).getViewport().getComponent(0));
							ListModel model = comp2.getModel();
							Object[] array = new Object[model.getSize()];

							int selectedIndex = comp2.getSelectedIndex();
							int c = 0;
							array[c++] = model.getElementAt(selectedIndex);

							for (int i = 0; i < array.length; i++) {
								if (i != selectedIndex)
									array[c++] = model.getElementAt(i);
							}

							comp2.setListData(array);
							comp2.setSelectedIndex(0);

							try {
								renderComponent(g2, currentComp,comp2,currentRotation,false, currentIndent,isPrinting);
								dummyPanel.remove(comp2);
							} catch (Exception cc) {

							}
                            
						} else { //if printing improve quality on AP images
							//FormObject form=null;

							boolean customPrintoverRide=false;
                            if(customFormPrint!=null){

                                //setup scalings
                                scaleComponent(currentPage, 1, rotation, currentComp, comp, false,false,indent);

                                //AffineTransform ax = g2.getTransform();
                                //g2.translate(comp.getBounds().x - insetW, comp.getBounds().y + cropOtherY[currentPage]);

                                //comp.paint(g2);
                                customPrintoverRide=customFormPrint.print(g2, currentComp, comp, this);
                                //g2.setTransform(ax);

                                                    }
                            //System.out.println(customFormPrint+" "+currentComp+" "+comp);

                            if(!customPrintoverRide){
                            	//this is where the cust1/display_error file line went, but it affects costena printing.
                                if(comp instanceof AbstractButton){
                                    Object obj=((AbstractButton)comp).getIcon();
                                    if(obj!=null && obj instanceof FixImageIcon)
                                        ((FixImageIcon)(obj)).setPrinting(true,1);
                                }
								dummyPanel.add(comp);

								try {
									renderComponent(g2, currentComp,comp,currentRotation,false, currentIndent,isPrinting);
									dummyPanel.remove(comp);
								} catch (Exception cc) {

								}

                                if(comp instanceof AbstractButton){
                                    Object obj=((AbstractButton)comp).getIcon();
                                    if(obj!=null && obj instanceof FixImageIcon)
                                        ((FixImageIcon)obj).setPrinting(false,1);
                                }
							}
						}
					}
                    
					currentComp++;

					if (currentComp == pageMap.length)
						break;
					
				}
            }
		} catch (Exception e) {
			e.printStackTrace();
		}

		g2.setTransform(defaultAf);

		// put componenents back
		if (currentPage == pageIndex && panel != null) {
			// createDisplayComponentsForPage(pageIndex,this.panel,this.displayScaling,this.rotation);
			// panel.invalidate();
			// panel.repaint();

            //forceRedraw=true;
            resetScaledLocation(displayScaling, rotation, indent);

        }
	}



    private void setField(Component nextComp,int formPage,float scaling, int rotation,int formNum) {

		// add fieldname to map for action events
		String curCompName = FormUtils.removeStateToCheck(nextComp.getName(), false);

		if (curCompName != null && !lastNameAdded.equals(curCompName)) {
			nameToCompIndex.put(curCompName, new Integer(formNum));
			lastNameAdded = curCompName;
		}

		// setup and add component to selection
		if (nextComp != null) {

			// set location and size
			Rectangle rect = nextComp.getBounds();
			if (rect != null) {

				boundingBoxs[formNum][0] = rect.x;
				boundingBoxs[formNum][1] = rect.y;
				boundingBoxs[formNum][2] = rect.width + rect.x;
				boundingBoxs[formNum][3] = rect.height + rect.y;

			}

			allFields[formNum] = nextComp;
			scroll[formNum] = null;

			//fontSizes was duplicate of fontSizes so removed, fontSize now gets set in completeField

			// flag as unused
			firstTimeDisplayed[formNum] = true;

			// make visible
			scaleComponent(formPage, scaling, rotation, formNum, nextComp, true,false, indent);

		}

		pageMap[formNum] = formPage;

		nextFreeField++;

	}

	/**
	 * alter font and size to match scaling. Note we pass in compoent so we can
	 * have multiple copies (needed if printing page displayed).
	 */
	private void scaleComponent(int curPage, float scale, int rotate, int id,
			final Component curComp, boolean redraw,boolean popups, int indent) {

		// if (showMethods)
		// System.out.println("DefaultAcroRenderer.scaleComponent()");

		if (curComp == null)
			return;
		
		/**
		 * work out if visible in Layer 
		 */
		if (layers != null) {
			
			/**
			 * get matching component
			 */
			// get correct key to lookup form data
			String ref = this.convertIDtoRef(id);
	
	
			String layerName = null;
			Object[] rawForm = this.getRawForm(ref);
			if (rawForm[0] != null) {
				layerName = ((FormObject) (rawForm[0])).getLayerName();
			}
			
			// do not display
			if (layerName != null && layers.isLayerName(layerName)) {

				boolean isVisible = layers.isVisible(layerName);
				curComp.setVisible(isVisible);
			}
		}
		// ////////////////////////
		
		float[] box=new float[]{0,0,0,0};
		//set to false if popups are already on page, to stop recalculating bounds, so that users can move them
		boolean calcBounds = true;
		if(popups){
			Rectangle popRect = curComp.getBounds();
			if(popRect.x!=0 && popRect.y!=0){
				//the popup has prob been set so keep the values.
				calcBounds = false;
			}else {
				box = popupBounds[id];
			}
		}else{
			box = boundingBoxs[id];
		}
		int[] bounds = new int[]{0,0,0,0};
		if(calcBounds){
			int cropRotation = rotate;
			bounds = cropComponent(box,curPage,scale,cropRotation, id,redraw);
		}
		
		if(popups){
			//we dont change the width and height
			bounds[2] = (int)(popupBounds[id][2]-popupBounds[id][0]);
			bounds[3] = (int)(popupBounds[id][3]-popupBounds[id][1]);
		}
		
		/**
		 * rescale the font size
		 */
		// if (debug)
		// System.out.println("check font size=" + comp);
		Font resetFont = curComp.getFont();
		if (!popups && resetFont != null) {
			//send in scale, rotation, and curComp as they could be from the print routines, 
			//which define these parameters.
			recalcFontSize(scale, rotate, id, curComp);
		}
		
		//scale border if needed
		if(!popups && curComp instanceof JComponent && ((JComponent)curComp).getBorder()!=null){
			
			FormObject form = (FormObject)rawFormData.get(convertIDtoRef(id));
			if(form!=null)
				((JComponent)curComp).setBorder((Border)generateBorderfromForm(form,scale));
		}
		
		// factor in offset if multiple pages displayed
		if (calcBounds && (xReached != null)) {
			bounds[0] = bounds[0] + xReached[curPage];
			bounds[1] = bounds[1] + yReached[curPage];
		}
		
		int pageWidth;
		if((pageData.getRotation(curPage)+rotate)%180==90){
			pageWidth = pageData.getCropBoxHeight(curPage);
		}else {
			pageWidth = pageData.getCropBoxWidth(curPage);
		}
		
		if(displayView==Display.CONTINUOUS){
			double newIndent;
			if(rotate==0 || rotate==180)
		    	newIndent =(widestPageNR -(pageWidth))/2;
			else
				newIndent =(widestPageR -(pageWidth))/2;
			
			indent = (int)(indent + (newIndent*scale));
		}
		
		int totalOffsetX = userX+indent+insetW;
		int totalOffsetY = userY+insetH;
		
		Rectangle boundRect = new Rectangle(totalOffsetX+bounds[0],totalOffsetY+bounds[1],bounds[2],bounds[3]);
		if(popups){
			if(!calcBounds){
				//if popup already calculated use current values so users moving is kept
				Rectangle popRect = curComp.getBounds();
				boundRect.x = popRect.x;
				boundRect.y = popRect.y;
			}
			//boundRect is changed within method if needed.
			checkPopupBoundsOnPage(boundRect,panel.getVisibleRect());
		}
		curComp.setBounds(boundRect);
		
		/**
		 * rescale the icons if any
		 */
		if (curComp != null && curComp instanceof AbstractButton) {
			AbstractButton but = ((AbstractButton) curComp);

			Icon curIcon = but.getIcon();
			
			boolean displaySingle = false;
			if(displayView==Display.SINGLE_PAGE || displayView==Display.NODISPLAY){
				displaySingle = true;
			}
			
			int combinedRotation=rotate;
			if (curIcon instanceof FixImageIcon)
				((FixImageIcon) curIcon).setAttributes(curComp.getWidth(), curComp.getHeight(), combinedRotation,displaySingle);
			else if(curIcon instanceof ReadOnlyTextIcon)
				((ReadOnlyTextIcon) curIcon).setAttributes(curComp.getWidth(), curComp.getHeight(), combinedRotation,displaySingle);

			curIcon = but.getPressedIcon();
			if (curIcon instanceof FixImageIcon)
				((FixImageIcon) curIcon).setAttributes(curComp.getWidth(), curComp.getHeight(), combinedRotation,displaySingle);

			curIcon = but.getSelectedIcon();
			if (curIcon instanceof FixImageIcon) 
				((FixImageIcon) curIcon).setAttributes(curComp.getWidth(), curComp.getHeight(), combinedRotation,displaySingle);

			curIcon = but.getRolloverIcon();
			if (curIcon instanceof FixImageIcon)
				((FixImageIcon) curIcon).setAttributes(curComp.getWidth(), curComp.getHeight(), combinedRotation,displaySingle);

			curIcon = but.getRolloverSelectedIcon();
			if (curIcon instanceof FixImageIcon)
				((FixImageIcon) curIcon).setAttributes(curComp.getWidth(), curComp.getHeight(), combinedRotation,displaySingle);

		}
	}

	/** we take in curComp as it could be a JTextField showing the selected value from a JComboBox 
	 * also the scale and rotation could be from a print routine and not the same as the global variables
	 */
	private void recalcFontSize(float scale, int rotate, int id,final Component curComp) {
		
		int rawSize = fontSize[id];
		
		if (rawSize == -1)
			rawSize = 0;//change -1 to best fit so that text is more visible
		
		if (rawSize == 0) {// best fit
			// work out best size for bounding box of object
			int height = (int) (boundingBoxs[id][3] - boundingBoxs[id][1]);
			int width = (int) (boundingBoxs[id][2] - boundingBoxs[id][0]);
			if (rotate == 90 || rotate == 270) {
				int tmp = height;
				height = width;
				width = tmp;
			}
		
			rawSize = (int)(height * 0.85);
			
			if (curComp instanceof JTextArea) {
				String text = ((JTextArea) curComp).getText();
				rawSize = calculateFontSize(height, width, true,text);
				
			} else if(curComp instanceof JTextField){
				String text = ((JTextComponent) curComp).getText();
				rawSize = calculateFontSize(height, width, false,text);
				
			} else if (curComp instanceof JButton) {
				String text = ((JButton) curComp).getText();
				if (text != null) {
					rawSize = calculateFontSize(height, width, false,text);
				}
			}else if( curComp instanceof JList){
				int count = ((JList) curComp).getModel().getSize()+2;
				rawSize = rawSize / count;
			}
		}
		
		int size = (int) (rawSize * scale);
		if (size < 1) {
			size = 1;
		}
		
		// if (debug)
		// System.out.println(size + "<<<<<<resetfont=" + resetFont);
		Font resetFont = curComp.getFont();
		Font newFont = new Font(resetFont.getFontName(),resetFont.getStyle(),size);
		// resetFont.getAttributes().put(java.awt.font.TextAttribute.SIZE,size);
		// if (debug)
		// System.out.println("newfont=" + newFont);
		
		curComp.setFont(newFont);
	}
	
	
	/** returns Border as is swing specific class */
	public Object generateBorderfromForm(FormObject form,float scaling) {
		float[] BC = form.getDictionary(PdfDictionary.MK).getFloatArray(PdfDictionary.BC);
        if(BC==null && form.getParameterConstant(PdfDictionary.Subtype) == PdfDictionary.Screen)
        	BC = form.getFloatArray(PdfDictionary.C);
        
        Border newBorder = JPedalBorderFactory.createBorderStyle(form.getDictionary(PdfDictionary.BS),
                FormObject.generateColor(BC),
                Color.white,scaling);
		return newBorder;
	}

	private int[] cropComponent(float[] box,int curPage,float s,int r,int i,boolean redraw){
		/** // OLD routine
		int x = 0, y = 0, w = 0, h = 0;
		
		int cropOtherX = (pageData.getMediaBoxWidth(curPage) - pageData.getCropBoxWidth(curPage) - pageData.getCropBoxX(curPage));
		if (r == 0) {

			// old working routine
//			int x = (int)((box[i][0])*scaling)+insetW-pageData.getCropBoxX(currentPage);
//			int y = (int)((pageData.getMediaBoxHeight(currentPage)-box[i][3]-cropOtherY)*scaling)+insetH;
			// int w = (int)((box[i][2]-box[i][0])*scaling);
			// int h = (int)((box[i][3]-box[i][1])*scaling);

			int crx = pageData.getCropBoxX(curPage);
			// new hopefully more accurate routine
			float x100 = (box[0]) - (crx);

//			if we are drawing the forms to "extract image" or "print",
//			we don't translate g2 by insets we translate by crop x,y
//			so add on crop values
//			we should also only be using 0 rotation
			
			if (!redraw)
				x100 += crx;

			float y100 = (pageData.getMediaBoxHeight(curPage) - box[3] - cropOtherY[curPage]);
			float w100 = (box[2] - box[0]);
			float h100 = (box[3] - box[1]);

			x = (int) (((x100) * s) + insetW);
			y = (int) (((y100) * s) + insetH);
			w = (int) (w100 * s);
			h = (int) (h100 * s);

		} else if (r == 90) {

			// old working routine
//			int x = (int)((box[i][1]-pageData.getCropBoxY(currentPage))*scaling)+insetW;
			// int y = (int)((box[i][0])*scaling)+insetH;
			// int w = (int)((box[i][3]-box[i][1])*scaling);
			// int h = (int)((box[i][2]-box[i][0])*scaling);

			// new hopefully better routine
			float x100 = (box[1] - pageData.getCropBoxY(curPage));
			float y100 = (box[0] - pageData.getCropBoxX(curPage));
			float w100 = (box[3] - box[1]);
			float h100 = (box[2] - box[0]);

			x = (int) (((x100) * s) + insetH);
			y = (int) (((y100) * s) + insetW);
			w = (int) (w100 * s);
			h = (int) (h100 * s);

		} else if (r == 180) {
			// old working routine
//			int x = (int)((pageData.getMediaBoxWidth(currentPage)-box[i][2]-cropOtherX)*scaling)+insetW;
//			int y = (int)((box[i][1]-pageData.getCropBoxY(currentPage))*scaling)+insetH;
			// int w = (int)((box[i][2]-box[i][0])*scaling);
			// int h = (int)((box[i][3]-box[i][1])*scaling);

			// new hopefully better routine
			int x100 = (int) (pageData.getMediaBoxWidth(curPage) - box[2] - cropOtherX);
			int y100 = (int) (box[1] - pageData.getCropBoxY(curPage));
			int w100 = (int) (box[2] - box[0]);
			int h100 = (int) (box[3] - box[1]);

			x = (int) (((x100) * s) + insetW);
			y = (int) (((y100) * s) + insetH);
			w = (int) (w100 * s);
			h = (int) (h100 * s);

		} else if (r == 270) {

			// old working routine
//			int x = (int)((pageData.getMediaBoxHeight(currentPage)-box[i][3]-cropOtherY)*scaling)+insetW;
//			int y = (int)((pageData.getMediaBoxWidth(currentPage)-box[i][2]-cropOtherX)*scaling)+insetH;
			// int w = (int)((box[i][3]-box[i][1])*scaling);
			// int h = (int)((box[i][2]-box[i][0])*scaling);

			// new hopefully improved routine
			float x100 = (pageData.getMediaBoxHeight(curPage) - box[3] - cropOtherY[curPage]);
			float y100 = (pageData.getMediaBoxWidth(curPage) - box[2] - cropOtherX);
			float w100 = (box[3] - box[1]);
			float h100 = (box[2] - box[0]);

			x = (int) (((x100) * s) + insetH);
			y = (int) (((y100) * s) + insetW);
			w = (int) (w100 * s);
			h = (int) (h100 * s);

		}
		/*///NEW routine
		
		//NOTE if needs adding in ULC check SpecialOptions.SINGLE_PAGE
		if(displayView!=Display.SINGLE_PAGE && displayView!=Display.NODISPLAY)
			r = (r+pageData.getRotation(curPage))%360;
		
		int cropX = pageData.getCropBoxX(curPage);
		int cropY = pageData.getCropBoxY(curPage);
		int cropW = pageData.getCropBoxWidth(curPage);
		
		int mediaW = pageData.getMediaBoxWidth(curPage);
		int mediaH = pageData.getMediaBoxHeight(curPage);
		
		int cropOtherX = (mediaW - cropW - cropX);
		
		float x100,y100,w100,h100;
		int x = 0, y = 0, w = 0, h = 0;
		
			{
			switch(r){
			case 0:
				
				x100 = box[0];
				//if we are drawing on screen take off cropX if printing or extracting we dont need to do this.
				if (redraw)
					x100 -= cropX;
				
				y100 = mediaH - box[3]-cropOtherY[curPage];
				w100 = (box[2] - box[0]);
				h100 = (box[3] - box[1]);
	
				x = (int) ((x100) * s);
				y = (int) ((y100) * s);
				w = (int) (w100 * s);
				h = (int) (h100 * s);
				
				//chris to look at
				//correction to allign text with background boxes.
				//x+=((3*s)+0.5f);
				
				break;
			case 90:
	
				// new hopefully better routine
				x100 = box[1]-cropY;
				y100 = box[0]-cropX;
				w100 = (box[3] - box[1]);
				h100 = (box[2] - box[0]);
				
				x = (int) ((x100) * s);
				y = (int) ((y100) *s);
				w = (int) (w100 * s);
				h = (int) (h100 * s);
	
				break;
			case 180:
	
				// new hopefully better routine
				w100 = box[2] - box[0];
				h100 = box[3] - box[1];
				y100 = box[1]-cropY;
				x100 = mediaW-box[2]-cropOtherX;
	
				x = (int) ((x100) * s);
				y = (int) ((y100) * s);
				w = (int) (w100 * s);
				h = (int) (h100 * s);
	
				break;
			case 270:
	
				// new hopefully improved routine
				w100 = (box[3] - box[1]);
				h100 = (box[2] - box[0]);
				x100 = mediaH -box[3]-cropOtherY[curPage];
				y100 = mediaW-box[2]-cropOtherX;
				
				x = (int) ((x100) * s);
				y = (int) ((y100) * s);
				w = (int) (w100 * s);
				h = (int) (h100 * s);
				
				break;
			}/**/
		}
		return new int[]{x,y,w,h};
	}

	/**
	 * used to flush/resize data structures on new document/page
	 * @param formCount
	 * @param pageCount
	 * @param keepValues
	 */
	public boolean resetComponents(int formCount, int pageCount, boolean keepValues) {
		// System.out.println("count="+formCount);

		if(!super.resetComponents(formCount, pageCount, keepValues))
			//if return false then we already have enough forms
			return false;

		if (!keepValues) {
			scroll = new JScrollPane[formCount + 1];
			allFields = new Component[formCount + 1];
			popups = new Component[0];
			testedForDuplicates = new boolean[formCount + 1];
		} else if (pageMap != null) {
			JScrollPane[] tmpScroll = scroll;
			Component[] tmpFields = allFields;
			boolean[] tmptestedForDuplicates = testedForDuplicates;

			allFields = new Component[formCount + 1];
			testedForDuplicates = new boolean[formCount + 1];

			scroll = new JScrollPane[formCount + 1];

			int origSize = tmpFields.length;

			// populate
			for (int i = 0; i < formCount + 1; i++) {

				if (i == origSize)
					break;

				allFields[i] = tmpFields[i];
				testedForDuplicates[i] = tmptestedForDuplicates[i];
				scroll[i] = tmpScroll[i];
			}
		}

		// clean out store of buttonGroups
		annotBgs.clear();
		
		return true;
	}

	/**
	 * used to remove all components from display
	 */
	public void removeAllComponentsFromScreen() {
		Iterator formIter = rawFormData.values().iterator();
		while(formIter.hasNext()){
			FormObject formObj = (FormObject)formIter.next();
			pdfDecoder.getFormRenderer().getActionHandler().PI(formObj,PdfDictionary.AA);
			pdfDecoder.getFormRenderer().getActionHandler().PI(formObj,PdfDictionary.A);
			pdfDecoder.getFormRenderer().getActionHandler().PC(formObj,PdfDictionary.AA);
			pdfDecoder.getFormRenderer().getActionHandler().PC(formObj,PdfDictionary.A);
		}
		
		if (panel != null) {
			if (SwingUtilities.isEventDispatchThread())
				panel.removeAll();
			else {
				final Runnable doPaintComponent = new Runnable() {
					public void run() {
						panel.removeAll();
					}
				};
				SwingUtilities.invokeLater(doPaintComponent);
			}
		}

	}

	/**
	 * pass in object components drawn onto
	 * @param rootComp
	 */
	public void setRootDisplayComponent(final Object rootComp) {
		if (SwingUtilities.isEventDispatchThread())
			panel = (JPanel) rootComp;
		else {
			final Runnable doPaintComponent = new Runnable() {
				public void run() {
					panel = (JPanel) rootComp;
				}
			};
			SwingUtilities.invokeLater(doPaintComponent);
		}

	}

	/**
	 * used to add any additional radio/checkboxes on decode
	 * @param page
	 */
	public void completeFields(int page,AcroRenderer acroRend) {
		//go through all fields and call required actions, 
		//ie on buttons select the required buttons and call the A action on the appropriate field
		
		
		for (int currentComp = getStartComponentCountForPage(page); 
				currentComp<pageMap.length && pageMap[currentComp] == page; currentComp++) {
			
			int type=getFieldType(allFields[currentComp]);
			
			FormObject curForm = (FormObject)rawFormData.get(convertIDtoRef(currentComp));
			ActionHandler curHandler = acroRend.getActionHandler();
			
			//get the page object
			String pageRef = pdfDecoder.getReferenceforPage(currentPage);
			PageObject pdfObject=new PageObject(pageRef);
		    currentPdfFile.readObject(pdfObject); 
		    
		    //call commands on page viewed before action commands below
			curHandler.PV(pdfObject, PdfDictionary.AA);
			
			switch(type){
			case BUTTON_TYPE:
//			case FormFactory.checkboxbutton:
//			case FormFactory.radiobutton:
				if(((AbstractButton)allFields[currentComp]).isSelected()){
					//call action to setup form as required ie hide forms if needed.
					acroRend.getActionHandler().A(null, ((FormObject)rawFormData.get(convertIDtoRef(currentComp))), ActionHandler.MOUSEPRESSED);
					acroRend.getActionHandler().D(null, ((FormObject)rawFormData.get(convertIDtoRef(currentComp))));
					acroRend.getActionHandler().A(null, ((FormObject)rawFormData.get(convertIDtoRef(currentComp))), ActionHandler.MOUSECLICKED);
					acroRend.getActionHandler().A(null, ((FormObject)rawFormData.get(convertIDtoRef(currentComp))), ActionHandler.MOUSERELEASED);
					acroRend.getActionHandler().U(null, ((FormObject)rawFormData.get(convertIDtoRef(currentComp))));
				}
				break;
			}
			
		}
		
	//commented out for now as it should be fixed when extra dictionarys are read into library.
	
//		Object[] bgRefs = annotBgs.keySet().toArray();
//		for (int i = 0; i < bgRefs.length; i++) {
//			ButtonGroup bg = (ButtonGroup) annotBgs.get(bgRefs[i]);
//			
//			Enumeration list = bg.getElements();
//			List emptyButs = new ArrayList(bg.getButtonCount());
//			FixImageIcon selIcon = null;
//	        while(list.hasMoreElements()){
//	        	JToggleButton but = (JToggleButton)list.nextElement();
//	            if(but.getSelectedIcon()==null){
//	            	//if we dont have a button icon, store the field to add it later
//	            	emptyButs.add(but);
//	            }else {
//	            	//we now have an icon so store it, so we can add it to the other buttons in the group.
//	            	selIcon = (FixImageIcon)but.getSelectedIcon();
//	            }
//	        }
//	        
//	        if(selIcon!=null){
//	        	//add the icon to the other buttons found
//	        	for (int b = 0; b < emptyButs.size(); b++) {
//					((JToggleButton)emptyButs.get(b)).setSelectedIcon(selIcon);
//				}
//	        }
//		}
	}

	/**
	 * store and complete setup of component
	 * @param formObject
     * @param formNum
     * @param formType
     * @param rawField
     * @param currentPdfFile
     */
	public void completeField(final FormObject formObject,
                              int formNum, Integer formType,
                                  Object rawField, PdfObjectReader currentPdfFile) {
		
        this.currentPdfFile=currentPdfFile;

        if (rawField == null)
            return;

        //only in ULC builds
        //<start-thin>
        /**
        
        //<end-thin>
        if(org.jpedal.examples.canoo.server.ULCViewer.formOption == SpecialOptions.SWING_WIDGETS_ON_CLIENT){
	        //special ULC case
	        if(formNum==-1)
	        	formNum = nextFreeField;
	        
	        if(fontSize.length<=nextFreeField)
	    		resetComponents(nextFreeField, this.currentPage, true);
	    }
        /**/

        
        final int formPage = formObject.getPageNumber();

        // cast back to ULC or Swing or SWT
        // and change this class to suit
        Component retComponent = (Component) rawField;

        String fieldName = formObject.getTextStreamValue(PdfDictionary.T);

        /**
         * set values for Component
         */

        // append state to name so we can retrieve later if needed
        String name = fieldName;
        if (name != null) {// we have some empty values as well as null
			String stateToCheck = formObject.getNormalOnState();
            if (stateToCheck != null && stateToCheck.length() > 0)
                name = name + "-(" + stateToCheck + ')';
            retComponent.setName(name);
        }

        Rectangle rect = formObject.getBoundingRectangle();
        if (rect != null)
            retComponent.setBounds(rect);

        String defaultValue = formObject.getTextStreamValue(PdfDictionary.DV);
        if (formObject.getValuesMap() != null)
            defaultValue = (String) formObject.getValuesMap().get(Strip.checkRemoveLeadingSlach(defaultValue));
        else
            defaultValue = Strip.checkRemoveLeadingSlach(defaultValue);

        fontSize[formNum] = formObject.getTextSize();
        defaultValues[formNum] = defaultValue;
        isXfaObj[formNum] = formObject.isXFAObject();
        
        refToCompIndex.put(formObject.getObjectRefAsString(), new Integer(formNum));
        convertFormIDtoRef.put(new Integer(formNum), formObject.getObjectRefAsString());

        // set the type
        if(formType.equals(org.jpedal.objects.acroforms.creation.FormFactory.UNKNOWN))
            typeValues.put(fieldName, org.jpedal.objects.acroforms.creation.FormFactory.ANNOTATION);
        else
            typeValues.put(fieldName, formType);

        if (retComponent != null) { // other form objects

            //if /Open set to false should not be visible at start
            if(formObject.getBoolean(PdfDictionary.Open)==false &&
                    formObject.getParameterConstant(PdfDictionary.Subtype)==PdfDictionary.Popup){
                retComponent.setVisible(false);
            }

            //store field type in array to match allfields array
            allFieldsType[formNum] = formType.intValue();
            
            // put into array
            setField(retComponent, formPage, displayScaling, rotation,formNum);
        }
    }

	/**
	 * alter location and bounds so form objects show correctly scaled
	 */
	public void resetScaledLocation(final float currentScaling, final int currentRotation, final int currentIndent) {

		// we get a spurious call in linux resulting in an exception
		if (trackPagesRendered == null)
			return;

		// only if necessary
		if (forceRedraw || currentScaling != lastScaling || currentRotation != oldRotation || currentIndent != oldIndent) {
			
			oldRotation = currentRotation;
			lastScaling = currentScaling;
			oldIndent = currentIndent;
            forceRedraw=false;
            
			int currentComp;

			// fix rescale issue on Testfeld
			if (startPage <
					trackPagesRendered.length) {
				currentComp = trackPagesRendered[startPage];// startID;
			} else {
				currentComp = 0;
			}
			
			//draw popups on the screen
			if(panel!=null){
				if (SwingUtilities.isEventDispatchThread()) {
					for (int i = 0; i < popups.length; i++) {
						scaleComponent(currentPage, currentScaling, currentRotation, i, popups[i], true,true, indent);
						panel.add(popups[i]);
					}
				} else {
					final Runnable doPaintComponent = new Runnable() {
						public void run() {
							for (int i = 0; i < popups.length; i++) {
								scaleComponent(currentPage, currentScaling, currentRotation, i, popups[i], true,true, indent);
								panel.add(popups[i]);
							}

						}
					};
					SwingUtilities.invokeLater(doPaintComponent);
				}
			}

			// reset all locations
			if ((allFields != null) && (currentPage > 0) && (currentComp != -1) && (pageMap.length > currentComp)) {

				//just put on page, allowing for no values (last one alsways empty as array 1 too big
				// while(pageMap[currentComp]==currentPage){
				while (currentComp<pageMap.length && currentComp>-1 &&  
						((pageMap[currentComp] >= startPage) && (pageMap[currentComp] < endPage) 
								&& (allFields[currentComp] != null))) {

					// System.out.println("added"+currentComp);
					//while(currentComp<pageMap.length){//potential fix to help currentRotation
					if (panel != null){// && !(allFields[currentComp] instanceof JList))

						if (SwingUtilities.isEventDispatchThread()) {
							if (scroll[currentComp] == null)
								panel.remove(allFields[currentComp]);
							else
								panel.remove(scroll[currentComp]);

							scaleComponent(pageMap[currentComp], currentScaling, currentRotation, currentComp, allFields[currentComp], true,false, indent);

						} else {
							final int id = currentComp;
							final Runnable doPaintComponent = new Runnable() {
								public void run() {
									if (scroll[id] == null)
										panel.remove(allFields[id]);
									else
										panel.remove(scroll[id]);

									scaleComponent(pageMap[id], currentScaling, currentRotation, id, allFields[id], true,false, indent);

								}
							};
							SwingUtilities.invokeLater(doPaintComponent);
						}
					}

					if (panel != null) {

						/** possible problem with rotation files, 
						 * just test if rotated 90 or 270 and get appropriate height or width, 
						 * that would represent the height when viewed at correct orientation
						 */
						float boundHeight = boundingBoxs[currentComp][3]-boundingBoxs[currentComp][1];
						int swingHeight = allFields[currentComp].getPreferredSize().height;

						if(allFields[currentComp] instanceof JList && boundHeight<swingHeight){

							JList comp = (JList) allFields[currentComp];

							if (scroll[currentComp] != null)
								scroll[currentComp].remove(comp);

							scroll[currentComp] = new JScrollPane(comp);

							scroll[currentComp].setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
							scroll[currentComp].setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);


							scroll[currentComp].setLocation(comp.getLocation());

							scroll[currentComp].setPreferredSize(comp.getPreferredSize());
							scroll[currentComp].setSize(comp.getSize());

							// ensure visible (do it before we add)
							int index = comp.getSelectedIndex();
							if (index > -1)
								comp.ensureIndexIsVisible(index);

							if (SwingUtilities.isEventDispatchThread())
								panel.add(scroll[currentComp]);
							else {
								final int id = currentComp;
								final Runnable doPaintComponent = new Runnable() {
									public void run() {
										panel.add(scroll[id]);
									}
								};
								SwingUtilities.invokeLater(doPaintComponent);
							}

						} else {
							
							if (SwingUtilities.isEventDispatchThread())
								panel.add(allFields[currentComp]);
							else {
								final int id = currentComp;
								final Runnable doPaintComponent = new Runnable() {
									public void run() {
										panel.add(allFields[id]);
									}
								};
								SwingUtilities.invokeLater(doPaintComponent);
							}
						}
					}
					currentComp++;
				}
			}
		}
	}

	/**
	 * put components onto screen display
	 * @param startPage
	 * @param endPage
	 */
	public void displayComponents(int startPage, int endPage) {

		
		if (panel == null || rasterizeForms)
			return;

		this.startPage = startPage;
		this.endPage = endPage;

		/**    MIGHT be needed for multi display
         boolean multiPageDisplay=(startPage!=endPage);

         //remove all invisible forms
         if(multiPageDisplay){

         int start=1;
         int end=startPage;
         //from start to first page
         //removePageRangeFromDisplay(start, end, panel); //end not included in range

         //from end to last page
         int last=1+trackPagesRendered.length;
         //removePageRangeFromDisplay(end, last, panel);
         }
         /**/

		for (int page = startPage; page < endPage; page++) {

			int currentComp = getStartComponentCountForPage(page);
			//just put on page, allowing for no values (last one always empty as array 1 too big)

			// allow for empty form
			if (pageMap == null || pageMap.length <= currentComp)
				return;

			// display components
			if (currentComp!=-1 && currentComp != -999 && startPage>0 && endPage>0) {
				while (pageMap[currentComp] >= startPage && pageMap[currentComp] < endPage) {

					if (allFields[currentComp] != null) {

                        /**
                         * sync kid values if needed
                         */
                        syncKidValues(currentComp);

                        if (SwingUtilities.isEventDispatchThread()) {
							
							scaleComponent(pageMap[currentComp], displayScaling, rotation, currentComp, allFields[currentComp], true,false, indent);
							panel.add(allFields[currentComp]);

						} else {
							final int id = currentComp;
							final Runnable doPaintComponent = new Runnable() {
								public void run() {
									scaleComponent(pageMap[id], displayScaling, rotation, id, allFields[id], true,false, indent);
									panel.add(allFields[id]);
								}
							};
							SwingUtilities.invokeLater(doPaintComponent);
						}

						firstTimeDisplayed[currentComp] = false;
					}

					currentComp++;

					if (currentComp == pageMap.length)
						break;
				}
			}
		}
	}

    
    /**
	 * called by print and display methods to align all fields linked to the specified field.
	 */
	private void syncKidValues(int currentComp) {
			// get correct key to lookup form data and get FormObject
			String ref = this.convertIDtoRef(currentComp);
			Object[] rawForm = this.getRawForm(ref);
	
			for (int i = 0; i < rawForm.length; i++) {
				if (rawForm[i]!=null) {
					FormObject formObject = (FormObject) rawForm[i];
					String parent=formObject.getStringKey(PdfDictionary.Parent);
		
					//if it has a parent, store last value in parent so others can sync to it
					if(parent!=null){
						Object lastMapValue = LastValueByName.get(formObject.getTextStreamValue(PdfDictionary.T));
						
						Component comp = allFields[currentComp];
						
						if(allFieldsType[currentComp]==FormFactory.radiobutton || allFieldsType[currentComp]==FormFactory.checkboxbutton){ //NOTE Stores ref and not value
							
							JToggleButton checkComp = ((JToggleButton)comp);
							String currOnState = formObject.getNormalOnState();
							//we know parents are the same
							if((currOnState==null && lastMapValue==null) ||
									(currOnState!=null && currOnState.equals(lastMapValue))){
								//onstates and parents are the same this is treated as the same
								//we need to make sure we are selected.
								if(!checkComp.isSelected()){
									checkComp.setSelected(true);
									Icon icn = checkComp.getPressedIcon();
									if(icn!=null && icn instanceof FixImageIcon)
										((FixImageIcon)icn).swapImage(true);
								}
							}else {
								//we need to make sure we are deselected.
								if(checkComp.isSelected()){
									checkComp.setSelected(false);
									Icon icn = checkComp.getPressedIcon();
									if(icn!=null && icn instanceof FixImageIcon)
										((FixImageIcon)icn).swapImage(false);
								}        
							}
						}else if(lastMapValue!=null){
							if(allFieldsType[currentComp]==FormFactory.combobox){
								
								JComboBox combo = ((JComboBox)comp);
								if(combo.getSelectedItem()==null || !combo.getSelectedItem().equals(lastMapValue))
									combo.setSelectedItem(lastMapValue);
			
							}else if(lastMapValue!=null && allFieldsType[currentComp]==FormFactory.singlelinepassword || allFieldsType[currentComp]==FormFactory.singlelinetext
									|| allFieldsType[currentComp]==FormFactory.multilinepassword || allFieldsType[currentComp]==FormFactory.multilinetext){
								
								JTextComponent text = ((JTextComponent)comp);
								if(text.getText()==null || !text.getText().equals(lastMapValue))
									text.setText(lastMapValue.toString());
							}
						}
					}
				}
			}
	}

    /**
     * ensure all kid values sync across all pages before accessing data
     * - only call if you are using Viewer and want to read component values
     */
    public void syncAllValues() {
	        //get all forms
	        Map rawForms=this.getRawFormData();
	
	        Iterator it=rawForms.values().iterator();
	        
	        while(it.hasNext()){
	
	            Object rawForm = it.next();
	            int currentComp=-1;
	            String ref;
	
	            if (rawForm instanceof FormObject) {
	
	                FormObject formObject = (FormObject) rawForm;
	
	                String parent=formObject.getStringKey(PdfDictionary.Parent);
	                
	
	                //if it has a parent, store last value in parent so others can sync to it
	                if(parent!=null){// && formObject.isKid()){
						Object lastMapValue = LastValueByName.get(formObject.getTextStreamValue(PdfDictionary.T));
	                    
	                    ref=formObject.getObjectRefAsString();
	                    
	                    Object index = refToCompIndex.get(ref);
	                    if(index==null)
	                    	continue;
	                    
	                    currentComp=((Integer)index).intValue();
	
	                    Component comp = allFields[currentComp];
	                    
	                    if(allFieldsType[currentComp]==FormFactory.radiobutton || allFieldsType[currentComp]==FormFactory.checkboxbutton){ //NOTE Stores ref and not value
							
							JToggleButton checkComp = ((JToggleButton)comp);
							String currOnState = formObject.getNormalOnState();
							//we know parents are the same
							if((currOnState==null && lastMapValue==null) ||
									(currOnState!=null && currOnState.equals(lastMapValue))){
								//onstates and parents are the same this is treated as the same
									//we need to make sure we are selected.
									if(!checkComp.isSelected()){
										checkComp.setSelected(true);
										Icon icn = checkComp.getPressedIcon();
										if(icn!=null && icn instanceof FixImageIcon)
											((FixImageIcon)icn).swapImage(true);
									}
							}else {
									//we need to make sure we are deselected.
								if(checkComp.isSelected()){
									checkComp.setSelected(false);
									Icon icn = checkComp.getPressedIcon();
									if(icn!=null && icn instanceof FixImageIcon)
										((FixImageIcon)icn).swapImage(false);
	
								}
							}
	                    }else if(lastMapValue!=null){
		                    if(allFieldsType[currentComp]==FormFactory.combobox){
								
								JComboBox combo = ((JComboBox)comp);
								if(combo.getSelectedItem()==null || !combo.getSelectedItem().equals(lastMapValue))
									combo.setSelectedItem(lastMapValue);
		
							}else if(allFieldsType[currentComp]==FormFactory.singlelinepassword || allFieldsType[currentComp]==FormFactory.singlelinetext
									|| allFieldsType[currentComp]==FormFactory.multilinepassword || allFieldsType[currentComp]==FormFactory.multilinetext){
								
								JTextComponent text = ((JTextComponent)comp);
								if(text.getText()==null || !text.getText().equals(lastMapValue))
									text.setText(lastMapValue.toString());
							}
	                    }
	                }
	            }
	        }//END loop round all forms
    }

    /** goes through all forms with the given name, ie a single grouped set of forms
     * and sets the current values to all forms depending on if they are a button group
     * or if they are just linked text fields
     */
	private void syncFormsByName(String name) {
		//make sure we only work with values we have setup
		if(!LastValueByName.containsKey(name))
			return;
		
		Object lastMapValue = LastValueByName.get(name);
		
		//make sure we have all forms decoded so we can sync them all.
		try { pdfDecoder.getFormRenderer().getComponentNameList(); //decode all pages
        } catch (PdfException e) { e.printStackTrace(); }
        
		Object[] forms = getRawForm(name);
		for (int i = 0; i < forms.length; i++) {
			if(forms[i]==null)
				continue;
			
			FormObject form = (FormObject)forms[i];
		    Object index = refToCompIndex.get(form.getObjectRefAsString());
		    if(index==null)
		    	continue;
		    
		    int currentComp=((Integer)index).intValue();
		    if(allFieldsType[currentComp]==FormFactory.radiobutton || allFieldsType[currentComp]==FormFactory.checkboxbutton){ //NOTE Stores ref and not value
				
				JToggleButton checkComp = ((JToggleButton)allFields[currentComp]);
				String currOnState = form.getNormalOnState();
				//we know parents are the same
				if((currOnState==null && lastMapValue==null) ||
						(currOnState!=null && currOnState.equals(lastMapValue))){
					//onstates and parents are the same this is treated as the same
					//we need to make sure we are selected.
					if(!checkComp.isSelected()){
						checkComp.setSelected(true);
						Icon icn = checkComp.getPressedIcon();
						if(icn!=null && icn instanceof FixImageIcon)
							((FixImageIcon)icn).swapImage(true);
					}
				}else {
					//we need to make sure we are deselected.
					if(checkComp.isSelected()){
						checkComp.setSelected(false);
						Icon icn = checkComp.getPressedIcon();
						if(icn!=null && icn instanceof FixImageIcon)
							((FixImageIcon)icn).swapImage(false);
					}
				}
		    }else if(allFieldsType[currentComp]==FormFactory.combobox){
				
				JComboBox combo = ((JComboBox)allFields[currentComp]);
				if(combo.getSelectedItem()==null){
					if(lastMapValue!=null){
						combo.setSelectedItem(lastMapValue);
					}
				}else if(!combo.getSelectedItem().equals(lastMapValue)){
					combo.setSelectedItem(lastMapValue);
				}
			}else if(allFieldsType[currentComp]==FormFactory.singlelinepassword || allFieldsType[currentComp]==FormFactory.singlelinetext
					|| allFieldsType[currentComp]==FormFactory.multilinepassword || allFieldsType[currentComp]==FormFactory.multilinetext){
				
				String text=null;
				if(allFields[currentComp] instanceof JTextComponent)
					text = ((JTextComponent)allFields[currentComp]).getText();
				else if(allFields[currentComp] instanceof JButton)
					text = ((ReadOnlyTextIcon) ((JButton) allFields[currentComp]).getIcon()).getText();
				
				if(lastMapValue==null){
					if(text!=null){
						if(allFields[currentComp] instanceof JTextComponent)
							((JTextComponent)allFields[currentComp]).setText(null);
						else if(allFields[currentComp] instanceof JButton)
							((ReadOnlyTextIcon) ((JButton) allFields[currentComp]).getIcon()).setText("");
					}
				}else if(!lastMapValue.equals(text)){
					if(allFields[currentComp] instanceof JTextComponent)
						((JTextComponent)allFields[currentComp]).setText(lastMapValue.toString());
					else if(allFields[currentComp] instanceof JButton)
						((ReadOnlyTextIcon) ((JButton) allFields[currentComp]).getIcon()).setText(lastMapValue.toString());
				}
			}
		}//END loop round forms by name
	}

    /**
	 * tell user about Javascript validation error
	 * @param code
	 * @param args
	 */
	public void reportError(int code, Object[] args) {

        if(!PdfDecoder.showErrorMessages)
                return;

		// tell user
		if (code == ErrorCodes.JSInvalidFormat) {
			JOptionPane.showMessageDialog(panel,"The values entered does not match the format of the field ["+args[0]+" ]",
					"Warning: Javascript Window",JOptionPane.INFORMATION_MESSAGE);
		} else if (code == ErrorCodes.JSInvalidDateFormat)
			JOptionPane.showMessageDialog(panel,"Invalid date/time: please ensure that the date/time exists. Field ["+args[0]+" ] should match format "+args[1],
					"Warning: Javascript Window",JOptionPane.INFORMATION_MESSAGE);
		else if (code == ErrorCodes.JSInvalidRangeFormat) {
			
			JOptionPane.showMessageDialog(panel, args[1],
					"Warning: Javascript Window",JOptionPane.INFORMATION_MESSAGE);
		} else
			JOptionPane.showMessageDialog(panel,"The values entered does not match the format of the field",
					"Warning: Javascript Window",JOptionPane.INFORMATION_MESSAGE);

	}

	/**
	 * return list of form names for page
	 * @param pageNumber
	 * @return
	 */
	public List getComponentNameList(int pageNumber) {

		if (trackPagesRendered == null)
			return null;

		if ((pageNumber != -1) && (trackPagesRendered[pageNumber] == -1))
			return null; //now we can interrupt decode page this is more appropriate
		// throw new PdfException("[PDF] Page "+pageNumber+" not decoded");

		int currentComp;
		if (pageNumber == -1)
			currentComp = 0;
		else
			currentComp = trackPagesRendered[pageNumber];

		ArrayList nameList = new ArrayList();

		// go through all fields on page and add to list
		String lastName = "";
		//String currentName = "";
		while ((pageNumber == -1) || (pageMap[currentComp] == pageNumber)) {
			lastName = getComponentName(currentComp, nameList, lastName);
			currentComp++;
			if (currentComp == pageMap.length)
				break;
		}

		return nameList;
	}

	/**
	 * not used by Swing
	 * @param offset
	 */
	public void setOffset(int offset) {

	}

	/** repaints the specified form or all forms if null is sent in */
	public void invalidate(String name) {
		if (name == null) {
			for (int i = 0; i < allFields.length; i++) {
				//trap last element being null, in case we dont have extras.
				if(allFields[i]!=null)
					allFields[i].repaint();
			}
		} else {
			Object[] forms = getComponentsByName(name);
			
			if(forms==null)
				return;
			
			for (int i = 0; i < forms.length; i++) {
				allFields[i].repaint();
			}
		}
	}

	public void storeDisplayValue(String fieldRef) {
		Object compIndex = refToCompIndex.get(fieldRef);
		if(compIndex==null)
			return;
		
		int index = ((Integer)compIndex).intValue();
		
		if(allFieldsType[index]==FormFactory.combobox){
			
			FormObject form = (FormObject) rawFormData.get(fieldRef);
			Object value = ((JComboBox) allFields[index]).getSelectedItem();
			form.setSelectedItem((String)value);
			
		}else if(allFieldsType[index]==FormFactory.list){
			
			FormObject form = (FormObject) rawFormData.get(fieldRef);
			int[] values = ((JList) allFields[index]).getSelectedIndices();
			form.setTopIndex(values);
			
		}else if(allFieldsType[index]==FormFactory.radiobutton){
			
			FormObject form = (FormObject) rawFormData.get(fieldRef);
			JRadioButton but = ((JRadioButton) allFields[index]);
			if(but.isSelected()){
				form.setChildOnState(FormUtils.removeStateToCheck(but.getName(), true));
			}
			
		}else if(allFieldsType[index]==FormFactory.checkboxbutton){
			
			FormObject form = (FormObject) rawFormData.get(fieldRef);
			JCheckBox but = ((JCheckBox) allFields[index]);
			if(but.isSelected()){
				form.setCurrentState(FormUtils.removeStateToCheck(but.getName(), true));
			}
			
		}else if(allFieldsType[index]==FormFactory.singlelinepassword || allFieldsType[index]==FormFactory.multilinepassword){
			
			FormObject form = (FormObject) rawFormData.get(fieldRef);
			String value = ((JTextComponent) allFields[index]).getText();
			form.setTextValue(value);
			
		}else if(allFieldsType[index]==FormFactory.singlelinetext || allFieldsType[index]==FormFactory.multilinetext){
			//these fields have readonlytexticons(JButtons) associated sometimes so allow for.
			FormObject form = (FormObject) rawFormData.get(fieldRef);
			if(allFields[index] instanceof JTextComponent){
				String value = ((JTextComponent) allFields[index]).getText();
				form.setTextValue(value);
			}else {
				//JButton read only ignore
			}
		}else if(allFieldsType[index]==FormFactory.pushbutton || allFieldsType[index]==FormFactory.annotation
				|| allFieldsType[index]==FormFactory.signature){
//			FormObject form = (FormObject) rawFormData.get(fieldRef);
		}else {
            
		}
	}
	
	/** finds the display field of the defined form reference and changes its visibility as needed */
	public void setCompVisible(String ref, boolean visible) {
		Object checkObj;
		if (ref.indexOf("R") != -1) {
			checkObj = refToCompIndex.get(ref);
		}else {
			checkObj = nameToCompIndex.get(ref);
		}
		
		if(checkObj == null)
			return;
		
		int index = ((Integer) checkObj).intValue();
		allFields[index].setVisible(visible);
	}
    
    /** defined in page 102 of javascript for acrobat api */
    public int alert(String cMsg,int nIcon,int nType,String cTitle,Object oDoc,Object oCheckbox){
        
    	//setup what type of answer options the user has with nType
    	int optionType;
    	switch(nType){
    	case 1: optionType = JOptionPane.OK_CANCEL_OPTION; break;
    	case 2: optionType = JOptionPane.YES_NO_OPTION; break;
    	case 3: optionType = JOptionPane.YES_NO_CANCEL_OPTION; break;
    	default: optionType = JOptionPane.DEFAULT_OPTION; break;//0
    	}

    	//setup what type of message this is with nIcon
    	int messageType;
    	switch(nIcon){
    	case 1: messageType = JOptionPane.WARNING_MESSAGE; break;
    	case 2: messageType = JOptionPane.QUESTION_MESSAGE; break;
    	case 3: messageType = JOptionPane.INFORMATION_MESSAGE; break;
    	default: messageType = JOptionPane.ERROR_MESSAGE; break;//0
    	}

    	//add line breaks to message so it doesnt extend to wide
        //NOTE for loop added as ME does not support replaceAll
        int start = cMsg.indexOf("\\. ");
        while(start!=-1){
            StringBuffer buf = new StringBuffer();
            buf.append(cMsg.substring(0, start+1));
            buf.append("\\.\n");
            buf.append(cMsg.substring(start+3, cMsg.length()));
            cMsg = buf.toString();
            
            start = cMsg.indexOf("\\. ");
        }

    	//show the dialog
		int answer = JOptionPane.showConfirmDialog((Component)oDoc, cMsg, cTitle, optionType, messageType);
		/**/

    	switch(nType){
    	case 1: //ok/cancel
    		/*returns 1 - OK, 2 - Cancel, 3 - No, 4 - Yes*/
    		if(answer==0){
    			return 1; //OK
    		}else {
    			return 2; //Cancel
    		}
    	case 2: //yes/no
    	case 3://yes/no/cancel
    		switch(answer){
    		case 0: return 4; //Yes
    		case 1: return 3; //No
    		default: return 2; //Cancel
    		}
    	default: //ok
    		if(answer == 0){
    			return 1;//OK
    		}else {
    			return 2;//Cancel
    		}
    	}
    }
	public void popup(FormObject formObj, PdfObjectReader currentPdfFile) {
        if(ActionHandler.drawPopups){
	        //popup needs to be stored for each field, as method A() is static,
	        //and we need a seperate popup for each field.
	        JComponent popup;
	
	        if(formObj.isPopupBuilt()){
	            popup = (JComponent)formObj.getPopupObj();
	
	        }else {
	            PdfObject popupObj=formObj.getDictionary(PdfDictionary.Popup);
	            currentPdfFile.checkResolved(popupObj);
	            
	            if(popupObj==null){
	            	popupObj = new FormObject();
	            	((FormObject)popupObj).copyInheritedValuesFromParent(formObj);
	            	((FormObject)popupObj).setParent(formObj.getObjectRefAsString());
	            	//dont set the parent object as this is a copy of the same object
	            }
	            
	    		popup = new PdfSwingPopup(formObj,popupObj,pageData.getCropBoxWidth(currentPage));
	    		
	    		//copy current popup bounds array so we can add new bounds to new index
	    		float[][] tmpf = new float[popupBounds.length+1][4];
	            System.arraycopy(popupBounds, 0, tmpf, 0, popupBounds.length);
	            
	            //get rectangle for new popup
            	tmpf[popupBounds.length] = popupObj.getFloatArray(PdfDictionary.Rect);
	            popupBounds = tmpf;
	    		
	            //copy current popups array so we can add a new one to end
	            JComponent[] tmp = new JComponent[popups.length+1];
	            System.arraycopy(popups, 0, tmp, 0, popups.length);
	            
	            //add new popup to end of popup array
	            tmp[popups.length] = popup;
	            popups = tmp;
	            
	            formObj.setPopupBuilt(popup);
	            
	            //draw the popup on screen for the first time
	            popup.setVisible(popupObj.getBoolean(PdfDictionary.Open));
	            
	            //rescale the components to that the popup bounds are scaled to the current display
	            forceRedraw  = true;
	    		resetScaledLocation(displayScaling, rotation, indent);
	    		panel.repaint();
	        }
	        
	        if (popup.isVisible()) {
				popup.setVisible(false);
	        } else {
	        	popup.setVisible(true);
	        }
        }
	}
    
	/**
     * flag forms as needing redraw
     */
    public void invalidateForms() {
        lastScaling=-lastScaling;
    }

    /** sets the text color for the specified swing component */
	public void setTextColor(String ref, Color textColor) {
		Object checkObj;
		if (ref.indexOf("R") != -1) {
			checkObj = refToCompIndex.get(ref);
		} else {
			checkObj = nameToCompIndex.get(ref);
		}
		
		//Fix null exception in /PDFdata/baseline_screens/forms/406302.pdf
		if(checkObj==null)
			return ;
		
		//set the text color
		int index = ((Integer) checkObj).intValue();
		allFields[index].setForeground(textColor);
	}

    public void setCustomPrintInterface(CustomFormPrint customFormPrint) {
        this.customFormPrint=customFormPrint;
    }

    /** you can now send in the formobject and this will return the super form type
     * ie ComponentData.TEXT_TYPE, ComponentData.LIST_TYPE (list, combo) 
     * or ComponentData.BUTTON_TYPE (sign,annot,radio,check,push)
     */
	public int getFieldType(Object swingComp) {
		return getFieldType(swingComp,true);
	}
	
    /**
     * send in either FormObject for precise types, or swing components.
     * <br />
     * if returnSuper is true then we return the super form type
     * ie. ComponentData.TEXT_TYPE, ComponentData.LIST_TYPE (list, combo) 
     * or ComponentData.BUTTON_TYPE (sign,annot,radio,check,push)
     * <br />
     * if returnSuper is false then we return the type for the specified form
     * ie. formFactory.list,FormFactory.pushbutton etc
     */
	public int getFieldType(Object swingComp,boolean returnSuper) {
		if(returnSuper){//use OLD way
			if(swingComp instanceof FormObject){
				int subtype = ((FormObject)swingComp).getParameterConstant(PdfDictionary.Subtype);
				switch(subtype){
				case PdfDictionary.Tx:
					return TEXT_TYPE;
				case PdfDictionary.Ch:
					return LIST_TYPE;
				default: //button or sig or annot
					return BUTTON_TYPE;
				}
			}else if(swingComp instanceof JTextField || swingComp instanceof JTextArea || swingComp instanceof JPasswordField){
				return TEXT_TYPE;
	        }else if(swingComp instanceof JRadioButton || swingComp instanceof JCheckBox || swingComp instanceof JButton){
				return BUTTON_TYPE;
			}else if(swingComp instanceof JList || swingComp instanceof JComboBox){
				return LIST_TYPE;
			}else {
				return UNKNOWN_TYPE;
			}
		}else {
			if(swingComp instanceof FormObject){
				return ((FormObject)swingComp).getFormType();
			}else{
				int index = ((Integer)nameToCompIndex.get(FormUtils.removeStateToCheck(((JComponent)swingComp).getName(),false))).intValue();
				return allFieldsType[index];
			}
		}
	}

	//store last used value so we can align if kids
    public void flagLastUsedValue(Object component, FormObject formObject,boolean sync) {
		
		//get the component
		Component comp= (Component) component;
		String parent=formObject.getStringKey(PdfDictionary.Parent);
		String name = formObject.getTextStreamValue(PdfDictionary.T);
    
        //if it has a parent, stor elast value in parent so others can sync to it
        if(parent!=null){// && formObject.isKid()){
        	
            if(comp instanceof JComboBox){
				LastValueByName.put(name, ((JComboBox)comp).getSelectedItem());
				LastValueChanged.put(name,null);
            
            }else if(comp instanceof JTextComponent){
				LastValueByName.put(name, ((JTextComponent)comp).getText());
				LastValueChanged.put(name,null);

			}else if(comp instanceof JToggleButton){ //NOTE WE STORE REF and not value (which is implicit as its a radio button)
				boolean isSelected = ((JToggleButton) comp).isSelected();
				
				if(isSelected){
					LastValueByName.put(name, formObject.getNormalOnState());
					LastValueChanged.put(name,null);
				}else if(!isSelected){
					if(LastValueByName.get(formObject.getTextStreamValue(PdfDictionary.T))!=null){
            			 //if last value is null we dont need to set last value to null as is already.
						String currOnState = formObject.getNormalOnState();
						//we know parents are the same
						if(LastValueByName.get(formObject.getTextStreamValue(PdfDictionary.T)).equals(currOnState)){
							//onstates and parents are the same this is treated as the same
							if(formObject.getFieldFlags()[FormObject.NOTOGGLETOOFF_ID]){
	            				 //if this was the last value and we cannot turn off, turn back on
		                    	 //dont turn it off.
								((JToggleButton) comp).setSelected(true);
								Icon icn = ((JToggleButton) comp).getPressedIcon();
								if(icn!=null && icn instanceof FixImageIcon)
									((FixImageIcon)icn).swapImage(true);
	            			 }else {
	            				 //last value was this and we can toggle all off, store turned off
	            				 LastValueChanged.put(name,null);
	            					 LastValueByName.remove(name);
	            				 //parentObj.setLastUsedValue(null);
	            			 }
	            		 }
						//if last value is not this, we dont save anything as we dont know what is set here
            		 }
            	 }
            }
        }else {//If No Parent
			if(comp instanceof JToggleButton){ //NOTE WE STORE REF and not value (which is implicit as its a radio button)
            	 
				JToggleButton radioComp = ((JToggleButton)comp);
				if(!radioComp.isSelected() && formObject.getFieldFlags()[FormObject.NOTOGGLETOOFF_ID]){
                	 //dont turn it off.
                	 radioComp.setSelected(true);
					Icon icn = radioComp.getPressedIcon();
					if(icn!=null && icn instanceof FixImageIcon)
						((FixImageIcon)icn).swapImage(true);
				}
                 //NOTE if allowed to toggle to off, then it will do through java
            }
        }
        
        if(sync){
	        //NOTE sync by name as we have it already and using index just gets name by reading a lod of maps.
        	syncFormsByName(FormUtils.removeStateToCheck(comp.getName(),false));
        }
    }
    
	public void resetAfterPrinting(){
		//System.out.println("xx= ");
		
        forceRedraw=true;
		//resetScaledLocation(displayScaling, rotation, indent);
		
		//panel.invalidate();
		//panel.repaint();
	}



	public void hideComp(String compName,boolean visible){
		Component[] checkObj;
		indexs = null;
		if(NEW_HIDE_COMPS_BEHIND){
			if(compName==null){
				checkObj = allFields;
				indexs = new int[allFields.length];
				for (int i = 0; i < indexs.length; i++) {
					indexs[i] = i;
				}
			}
			
			Object compIndex = new Integer(getIndexFromName(compName));
			checkObj = (Component[]) getComponentsByName(compName,compIndex,true);
			
		}else {
			checkObj = (Component[]) getComponentsByName(compName);
		}
        if (checkObj != null) {
            for (int j = 0; j < checkObj.length; j++) {
        		if(NEW_HIDE_COMPS_BEHIND && indexs!=null){
        			//we need the index for the object so we can check the bounding boxes
        			float rx = boundingBoxs[indexs[j]][0];
        			float ry = boundingBoxs[indexs[j]][1];
        			float rwidth = boundingBoxs[indexs[j]][2]-boundingBoxs[indexs[j]][0];
        			float rheight = boundingBoxs[indexs[j]][3]-boundingBoxs[indexs[j]][1];
        			Rectangle rootRect = new Rectangle((int)rx,(int)ry,(int)rwidth,(int)rheight);
	        		
	        		//find components hidden within this components bounds
	        		List indexsToHide = (List)hideObjectsMap.get(rootRect);
	        		if(indexsToHide==null){
	        			//if no list figure out the list
	        			indexsToHide = new ArrayList();
		        		for (int i = 0; i < boundingBoxs.length; i++) {
		        			float x = boundingBoxs[i][0];
		        			float y = boundingBoxs[i][1];
		        			float width = boundingBoxs[i][2]-boundingBoxs[i][0];
		        			float height = boundingBoxs[i][3]-boundingBoxs[i][1];
		        			
							if(rootRect.contains(x, y, width, height)){
								indexsToHide.add(new Integer(i));
							}
						}
	
	                    hideObjectsMap.put(rootRect,indexsToHide);
	        		}
	        		
	        		//if we dont have a list we dont need to hide any other fields
	        		if(indexsToHide!=null){
	            		//we should have a list now
	            		ListIterator iter = indexsToHide.listIterator();
	                    while(iter.hasNext()){
	                    	int index = ((Integer)iter.next()).intValue();
	                    	
	                    	//hide or show the components depending on if we are showing or hiding the root component
	                    	allFields[index].setVisible(!visible);
	                    }
	        		}
        		}
            	
                checkObj[j].setVisible(visible);
            }
        }
        
        //reset to null so we cant mistake it for anything elsewhere.
        indexs = null;
	}

}
