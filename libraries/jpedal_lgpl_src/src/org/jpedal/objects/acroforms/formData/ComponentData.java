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
* ComponentData.java
* ---------------
*/
package org.jpedal.objects.acroforms.formData;


import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.jpedal.PdfDecoder;
import org.jpedal.objects.Javascript;

import org.jpedal.objects.PdfPageData;
import org.jpedal.objects.raw.FormObject;
import org.jpedal.objects.raw.PdfDictionary;
import org.jpedal.objects.layers.PdfLayerList;
import org.jpedal.objects.acroforms.creation.FormFactory;
import org.jpedal.objects.acroforms.utils.FormUtils;
import org.jpedal.utils.repositories.Vector_String;


/**holds all data not specific to Swing/SWT/ULC*/
public abstract class ComponentData implements GUIData {
	
	//########################### development flags ##########################
	
	/** used to turn off the hide components behind the component being hidden so we dont get any show through */
	static final protected boolean NEW_HIDE_COMPS_BEHIND = true;
	
    
    public static final boolean SYNCVALUESNEWWAY = true;
	
	
	//################### static values ################
    
    /**
     * flag to make forms draw as images, not swing components
     */
    protected boolean rasterizeForms = false;
    
    public void setRasterizeForms(boolean inlineForms){
    	rasterizeForms = inlineForms;
    }
    
	public static final int TEXT_TYPE = 0;
	public static final int BUTTON_TYPE = 1;
	public static final int LIST_TYPE = 2;
	public static final int UNKNOWN_TYPE = -1;
	
	//##################### variables #####################

    /** for if we add a popup to the panel, could be used for adding other objects*/
	protected boolean forceRedraw = false;
	
	protected Map hideObjectsMap=new HashMap();
	
	/** used in hideComp to get the indexs back from the getComponentsByName method */
	protected int[] indexs = null;

    //allow user to move relative draw position
    protected int userX, userY,widestPageNR,widestPageR;
    
    /** the current display view, Display.SINGLE, Display.CONTINOUS etc */
    protected int displayView;
	
	/**
	 * the type of form this field holds.
	 * holds a value from FormFactory.list, FormFactory.signature etc
	 */
	protected int[] allFieldsType;
	
	/**
	 * holds forms data array of formObjects data from PDF as Map for fast lookup
	 * <br>convertformIDtoRef = component index to PDF reference
	 * <br>nameToRef = field name to PDF Reference
	 * <br>rawFormData = PDF Reference to FormObject
	 * <br>duplicateNames = field name to PDF References seperated by commas
	 * <br>caseInsensitiveNameToFieldName = gives the case sensitive fieldname back 
	 * from case insensitive names use toLowerCase() before using get().
	 * <br>nameToCompIndex = stores the name and component index as Integer in allFields array.
	 * <br>refToCompIndex = stores the Pdf Reference and component index as Integer in allFields array.
	 * <br>
	 **/
	protected Map rawFormData=new HashMap(),convertFormIDtoRef=new HashMap(), nameToRef=new HashMap(),
		duplicateNames=new HashMap(),caseInsensitiveNameToFieldName = new HashMap(),
		nameToCompIndex,refToCompIndex;
	
	/** fully qualified field name to PDF reference */
	private Map fullyQualToRef = new HashMap();
	
	/** list of all fieldnames so we can find half referenced name such as "1.2." for fields 1.2.1 and 1.2.2 */
	private List namesMap=new ArrayList();
	
	/** the lastValue set for a specified name,
	 * <br>fieldname to LastValue selected*/
	protected Map LastValueByName = new HashMap();
	/** keeps a record of the last value being changed in the flag and sync methods*/
	protected Map LastValueChanged = new HashMap();
	
	/** used to map new XFA component references to there form object */
	private Map xfaRefToForm;

    protected Map componentsToIgnore=new HashMap();
    
	protected int insetW;

	protected int insetH;

	protected PdfPageData pageData;

	protected Javascript javascript;

	/**
	 * local copy needed in rendering
	 */
	protected int pageHeight, indent;

	protected int[] cropOtherY;
	/**
	 * track page scaling
	 */
	protected float displayScaling;
	protected int rotation;

	/**
	 * used to only redraw as needed
	 */
	protected float lastScaling = -1, oldRotation = 0, oldIndent = 0;

	/**
	 * used for page tracking
	 */
	protected int startPage, endPage, currentPage;

	/**
	 * the last name added to the nameToCompIndex map
	 */
	protected String lastNameAdded = "";

	/** stores map of names to indexs of components in allfields*/
	protected Map duplicates = new HashMap();

	protected Map lastValidValue=new HashMap();
	protected Map lastUnformattedValue=new HashMap();

	protected Map typeValues;

	/**
	 * next free slot
	 */
	protected int nextFreeField = 0;



	/**
	 * holds the location and size for each field,
	 * <br>
	 * [][0] = x1;
	 * [][1] = y1;
	 * [][2] = x2;
	 * [][3] = y2;
	 */
	protected float[][] boundingBoxs;
	
	protected float[][] popupBounds = new float[0][4];

	/**
	 * used to draw pages offset if not in SINGLE_PAGE mode
	 */
	protected int[] xReached, yReached;

	/**
	 * table to store if page components already built
	 */
	protected int[] trackPagesRendered;
	
    /** stores the forms in there original order, accessable by page */
    protected List[] formsUnordered;

	/**
	 * flag to show if component has been displayed for the first time
	 */
	protected boolean[] firstTimeDisplayed;

	/**
	 * array to store fontsizes as the field is set up for use on rendering
	 */
	protected int[] fontSize;
	
	/** stores true if the form as each index ix an XFA form object*/
	protected boolean[] isXfaObj;

	/**
	 * the reset to value of the indexed field
	 */
	protected String[] defaultValues;

	/**
	 * array to hold page for each component so we can scan quickly on page change
	 */
	public int[] pageMap;
    private int formCount;
    protected PdfLayerList layers;

	protected PdfDecoder pdfDecoder;
     
    /**last value set by user in GUI or null if none*/
	public Object getLastValidValue(String fieldRef) {
		if (fieldRef.indexOf(" 0 R") == -1 && fieldRef.indexOf(" 0 X") == -1) {
			fieldRef = (String) nameToRef.get(fieldRef);
		}
		return lastValidValue.get(fieldRef);
	}

    public void setLayerData(PdfLayerList layers){
        this.layers=layers;
    }



    /**last value set by user in GUI or null if none
     * if fieldRef is not a reference we find a reference from a names table (which can be doubled up and therefore wrong)*/
	public Object getLastUnformattedValue(String fieldRef) {
		if (fieldRef.indexOf(" 0 R") == -1 && fieldRef.indexOf(" 0 X") == -1) {
			fieldRef = (String) nameToRef.get(fieldRef);
		}
		return lastUnformattedValue.get(fieldRef);
	}

	/** reset params for the specified form names, or if null reset all forms params */
	public void reset(String[] aFields) {
//		System.out.println("ComponentData.reset() lastUnformattedMap cleared");
		/** You can include non-terminal fields in the array.*/
		if(aFields!=null){
			for (int i = 0; i < aFields.length; i++) {
				String ref = (String)nameToRef.get(aFields[0]);
				lastValidValue.remove(ref);
				lastUnformattedValue.remove(ref);
			}
		}else {
			lastValidValue.clear();
			lastUnformattedValue.clear();
		}

    }

    /**
     * allow user to lookup page with name of Form.
     * @param formName
     * @return page number or -1 if no page found
     */
    public int getPageForFormObject(String formName) {

        //Object checkObj;
        FormObject formObj=null;

		if (formName.indexOf("R") != -1) {
		//	checkObj = refToCompIndex.get(formName);
            formObj = ((FormObject)rawFormData.get(formName));

		}else {
		//	checkObj = nameToCompIndex.get(formName);
            String ref= (String) nameToRef.get(formName);
            if(ref!=null)
            formObj = ((FormObject)rawFormData.get(ref));

		}

        if(formObj == null)
			return -1;
        else
            return formObj.getPageNumber();

	}

    /**
     * returns the Type of pdf form, of the named field
     * look at FormFactory.LIST etc to find out which type
     */
    public Integer getTypeValueByName(String fieldName) {

    	/* this would return the type of the form, or we could use getfieldtype
		int index; 
    	if(fieldName.indexOf("0 R")!=-1){
    		index = ((Integer)refToCompIndex.get(fieldName)).intValue();
    	}else {
    		index = ((Integer)nameToCompIndex.get(fieldName)).intValue();
    	}
    	
    	//@mark can we return this as int instead of Integer, or is rogs code using Integer, 
    	//and we store as int so may be quicker to just pass back.
    	return allFieldsType[index];*/
    	
        Object key=typeValues.get(fieldName);
        if(key==null)
            return  FormFactory.UNKNOWN;
        else
            return (Integer) typeValues.get(fieldName);
    }

	/**
	 * return next ID for this page and also set pointer
	 * @param page
	 * @return
	 */
	private int setStartForPage(int page) {

		//flag start
        trackPagesRendered[page] = nextFreeField;

		return nextFreeField;
	}
	
	public void setUnsortedListForPage(int page,List unsortedComps){
		formsUnordered[page] = unsortedComps;
	}
	
	/**
	 * get next free field slot
	 * @return
	 */
	public int getNextFreeField() {
		return nextFreeField;
	}

    /**
	 * max number of form slots
	 */
	public int getMaxFieldSize() {
		return formCount;
	}

    /**
     * return start component ID or -1 if not set or -999 if trackPagesRendered not initialised
     * @param page
     * @return
     */
	public int getStartComponentCountForPage(int page) {
		if(trackPagesRendered==null)
			return -999;
		else if(trackPagesRendered.length>page && page>-1)
			return trackPagesRendered[page];
		else
			return -1;
	}

	/**
	 * setup values needed for drawing page
	 * @param pageData
	 * @param page
	 */
	public void initParametersForPage(PdfPageData pageData, int page,PdfDecoder decoder){

        //ensure setup
		if(cropOtherY==null || cropOtherY.length<=page)
			this.resetComponents(0, page+1, false);
		
		int mediaHeight = pageData.getMediaBoxHeight(page);
		int cropTop = (pageData.getCropBoxHeight(page) + pageData.getCropBoxY(page));

		//take into account crop		
		if (mediaHeight != cropTop)
			cropOtherY[page] = (mediaHeight - cropTop);
		else
			cropOtherY[page] = 0;
		
		this.pageHeight = mediaHeight;
		this.currentPage = page; //track page displayed

		if(!formsRasterizedForDisplay()){
			//set for page
			setStartForPage(page);
		}
		
		pdfDecoder = decoder;
	}

	/**
	 * used to flush/resize data structures on new document/page
	 * @param formCount
	 * @param pageCount
	 * @param keepValues
	 * return true if successful, false if formCount is less than current count.
	 */
	public boolean resetComponents(int formCount,int pageCount,boolean keepValues) {
		if(keepValues && this.formCount>formCount)
			return false;
		
		nextFreeField = 0;
        this.formCount=formCount;

		if(!keepValues){

			refToCompIndex = new HashMap(formCount+1);
			nameToCompIndex = new HashMap(formCount + 1);
			typeValues = new HashMap(formCount+1);

			pageMap = new int[formCount + 1];
			fontSize = new int[formCount + 1];

			//start up boundingBoxs
			boundingBoxs = new float[formCount + 1][4];
			popupBounds = new float[0][4];

			isXfaObj = new boolean[formCount+1];

			defaultValues = new String[formCount + 1];
			allFieldsType = new int[formCount+1];

			firstTimeDisplayed = new boolean[formCount + 1];

			//flag all fields as unread
			trackPagesRendered = new int[pageCount + 1];
			for (int i = 0; i < pageCount + 1; i++)
				trackPagesRendered[i] = -1;
			
			formsUnordered = new List[pageCount+1];
			
			//reset offsets
			cropOtherY=new int[pageCount+1];
			
			//reset the multi page shifting values so we dont get forms half way across the page on a single page view.
			xReached = yReached = null;
			
		}else if(pageMap!=null){

			boolean[] tmpFirstTimeDisplayed = firstTimeDisplayed;
			int[] tmpMap = pageMap;
			int[] tmpSize = fontSize;
			String[] tmpValues = defaultValues;
			int[] tmpTypes = allFieldsType;
			float[][] tmpBoxs = boundingBoxs;
			boolean[] tmpisXfaObj = isXfaObj;

			firstTimeDisplayed = new boolean[formCount + 1];
			// allFields = new Component[formCount + 1];
			pageMap = new int[formCount + 1];
			fontSize = new int[formCount + 1];
			defaultValues = new String[formCount + 1];
			allFieldsType = new int[formCount+1];

			//start up boundingBoxs
			boundingBoxs = new float[formCount + 1][4];

			isXfaObj = new boolean[formCount+1];

			int origSize=tmpMap.length;

			//populate
			for (int i = 0; i < formCount+1; i++) {

				if(i==origSize)
					break;
				//if (tmpFields[i] == null)
				//     break;

				firstTimeDisplayed[i] = tmpFirstTimeDisplayed[i];
				pageMap[i] = tmpMap[i];

				fontSize[i] = tmpSize[i];
				defaultValues[i] = tmpValues[i];
				allFieldsType[i] = tmpTypes[i];


				System.arraycopy(tmpBoxs[i], 0, boundingBoxs[i], 0, 4);

				isXfaObj[i] = tmpisXfaObj[i];

				nextFreeField++;
			}
		}
		
		return true;
	}

	/**
	 * pass in current values used for all components
	 * @param scaling
	 * @param rotation
     * @param indent
	 */
	public void setPageValues(float scaling, int rotation,int indent, int userX, int userY,int displayView,int widestPageNR,int widestPageR) {

		this.rotation=rotation;
		this.displayScaling=scaling;
        this.indent=indent;
        this.userX=userX;
        this.userY=userY;
        this.displayView = displayView;
        
        this.widestPageNR = widestPageNR;
        this.widestPageR = widestPageR;
	}

/**
	 * used to pass in offsets and PdfPageData object so we can access in rendering
	 * @param pageData
	 * @param insetW
	 * @param insetH
	 */
	public void setPageData(PdfPageData pageData, int insetW, int insetH) {

		//track inset on page
		this.insetW = insetW;
		this.insetH = insetH;

		this.pageData = pageData;

	}

	/**
	 * returns the default values for all the forms in this document
	 */
	public String[] getDefaultValues() {
		return defaultValues;
	}

	/**
	 * offsets for forms in multi-page mode
	 */
	public void setPageDisplacements(int[] xReached, int[] yReached) {

		this.xReached = xReached;
		this.yReached = yReached;

        //force redraw
        forceRedraw=true;
       
	}

    /**
	 * force redraw (ie for refreshing layers)
	 */
	public void setForceRedraw(boolean forceRedraw) {

        this.forceRedraw=forceRedraw;

	}

	/**
	 * provide access to Javascript object
	 * @param javascript
	 */
	public void setJavascript(Javascript javascript) {
		this.javascript=javascript;

	}

	public void resetDuplicates() {
		duplicates.clear();

	}

     protected boolean isFormNotPrinted(int currentComp) {

        // get correct key to lookup form data
        String ref = this.convertIDtoRef(currentComp);


        //System.out.println(currentComp+" "+comp.getLocation()+" "+comp);
        FormObject form = (FormObject) ((Object[])getRawForm(ref))[0];

        if(form!=null){
            return componentsToIgnore!=null &&
                    (componentsToIgnore.containsKey(new Integer(form.getParameterConstant(PdfDictionary.Subtype))) ||
            componentsToIgnore.containsKey(new Integer(form.getParameterConstant(PdfDictionary.Type))));

        }else
            return false;
    }
     
     // <start-me>
     public void storeXFARefToForm(Map xfaRefToFormObject){
    	 xfaRefToForm = xfaRefToFormObject;
     }
     // <end-me>
	
	/**
	 * store form data and allow lookup by PDF ref or name 
	 * (name may not be unique)
	 * @param formObject
	 */
	public void storeRawData(FormObject formObject) {
		
		String fieldName=formObject.getTextStreamValue(PdfDictionary.T);
		//add names to an array to track the kids
		if(fieldName!=null){
			namesMap.add(fieldName);
			
			//add case insensitive fieldname map to actual name, to fix some JS errors in files.
			caseInsensitiveNameToFieldName.put(fieldName.toLowerCase(),fieldName);
		}

		String ref=formObject.getObjectRefAsString();
		nameToRef.put(fieldName,ref);
		rawFormData.put(ref,formObject);

        String parent = formObject.getParentRef();

        // if no name, or parent has one recursively scan tree for one in Parent
        boolean isMultiple=false;
        String fullyQualName=null;

        while (parent != null && !parent.equals(ref)) {
            FormObject parentObj=null;
                {
                //parent.indexOf(" R")!=-1
                parentObj =new FormObject(parent,false);
                pdfDecoder.getIO().readObject(parentObj);
            }

//parentObj is null in mixedForms.pdf
            String newName = null;
            if(parentObj!=null)
                newName = parentObj.getTextStreamValue(PdfDictionary.T);
            if (newName == null)
                break;
            else if (fieldName!=null){
                //we pass in kids data so stop name.name
                if(fieldName.indexOf(newName)==-1) {
                    fullyQualName = newName + "." + fieldName.substring(fieldName.lastIndexOf('.')+1);
                    isMultiple=true;
                }
            }

            parent = parentObj.getParentRef();
        }

        //set the field name to be the Fully Qualified Name
        if(isMultiple)
            fullyQualToRef.put(fullyQualName, ref);

		/**
		 * track duplicates
		 */
		String duplicate=(String) duplicateNames.get(fieldName);
		if(duplicate==null){ //first case
			duplicateNames.put(fieldName,ref);
		}else{ //is a duplicate
			duplicate=duplicate+","+ref; // comma separated list
			duplicateNames.put(fieldName,duplicate);
		}
		
	}
	
	public void flushFormData() {
		
		nameToRef.clear();
		fullyQualToRef.clear();
		rawFormData.clear();
		LastValueByName.clear();
		duplicateNames.clear();
        convertFormIDtoRef.clear();
        namesMap.clear();

        lastNameAdded="";
        
        //clear the rectangle map for use in hiding objects behind
        hideObjectsMap.clear();

        this.oldIndent=-oldIndent;

    }

    /**
     * convert ID used for GUI components to PDF ref for underlying object used
     * so we can access form object knowing ID of component
     * @param objectID
     * @return
     */
    public String convertIDtoRef(int objectID){
        return (String)convertFormIDtoRef.get(new Integer(objectID));
    }

    /**
     * used to debug form names in test code
     * @param objectName
     * @return
     */
    public String getnameToRef(String objectName){
        return (String)nameToRef.get(objectName);
    }
    
    public int getIndexFromName(String name){
    	return ((Integer)nameToCompIndex.get(FormUtils.removeStateToCheck(name, false))).intValue();
    }
    
    /** calls getRawForm(objectName,true); 
     * <br>check getRawForm(String,boolean) for description
     */
    public Object[] getRawForm(String objectName){ return getRawForm(objectName,true); }
    /** returns an Object[] of FormObjects by the specified name.
     * <br>each item within the array can be NULL 
     * <br>if a reference is sent in an array with one formobject will be set back.
     * <br>caseSensitive is default search use false for specific cases.
     */
	public Object[] getRawForm(String objectName,boolean caseSensative) {
		if(!caseSensative){
			String name = (String)caseInsensitiveNameToFieldName.get(objectName.toLowerCase());
			if(name!=null)
				objectName = name;
		}
		
		//if name see if duplicates
		String matches = (String) duplicateNames.get(objectName);
		
		if(matches==null || (matches.indexOf(',')==-1)){//single form
			//convert to PDFRef if name first
			objectName = getPossRef(objectName);
			
			if(!rawFormData.containsKey(objectName)){
				//we may have a parent name and we want to return all the children
				String[] names = getChildNames(objectName);
				if(names!=null && names.length>0){
					Object[] values=new Object[names.length];
					for(int i=0;i<names.length;i++){
						objectName = getPossRef(names[i]);
						values[i] = rawFormData.get(objectName);
					}
					return values;
				}
			}
			
			//@mark - is it ok if i change to return Object[]
			//with 1 field I will just return new Object[]{our 1 object}   (as below commented out) 
			//currently we return either formobject or array[] of formobjects.
			
			return new Object[]{rawFormData.get(objectName)};
//			return rawFormData.get(objectName);
			
		}else{//duplicates
			
			StringTokenizer comps=new StringTokenizer(matches,",");
			int count=comps.countTokens();
			Object[] values=new Object[count];
			
			for(int ii=0;ii<count;ii++){
                values[ii] = rawFormData.get(comps.nextToken());
			}
			return values;
		}
	}

	private String getPossRef(String objectName) {
		String possRef=(String) nameToRef.get(objectName);
		if(possRef!=null)
			objectName=possRef;
		
		possRef = (String) fullyQualToRef.get(objectName);
		if(possRef!=null)
			objectName=possRef;
		return objectName;
	}
	
	/** returns the rawformdata map, the key is the pdfRef for whichever object you want */
	public Map getRawFormData() {
		return rawFormData;
	}
	
	/**
	 * this takes in a name with a . at the end and returns the kids of that object
	 */
	public String[] getChildNames(String name){
		if(name==null)
			return null;
		
		//find all fullyqualifiednames and check them.
		
		//check if the name is within the list we currently have
		if(!namesMap.isEmpty() && namesMap.toString().indexOf(name)!=-1){
			Vector_String childNames = new Vector_String();
			
			//scan over the list and find the child names
			Iterator iter = namesMap.iterator();
			while(iter.hasNext()){
				String val = (String)iter.next();
				if(val.indexOf(name)!=-1){
					// add them to our arrayList
					childNames.addElement(val);
				}
			}
			
			//return the Vector of childnames as a String[] 
			//NOTE: remember to trim first otherwise you get a massive array
			childNames.trim();
			return childNames.get();
		}else {
			// if there is no name within our list return null
			return null;
		}
	}
	
	/** moves the bounds rectangle to be within the inside rectangle */
	protected static Rectangle checkPopupBoundsOnPage(Rectangle bounds, Rectangle inside) {
		if(bounds.x<inside.x){
			//if too far left move min X
			bounds.x = inside.x;
		}
		if(bounds.y<inside.y){
			//if too far down move to min Y
			bounds.y = inside.y;
		}
		if(bounds.x+bounds.width>inside.x+inside.width){
			//if too far right move to rightmost point
			bounds.x = (inside.x+inside.width-bounds.width);
		}
		if(bounds.y+bounds.height>inside.y+inside.height){
			//if too far up move to uppermost point
			bounds.y = (inside.y+inside.height-bounds.height);
		}
		
		return bounds;
	}
	
	/** if ref is not a reference we find a reference from a names table (which can be doubled up and therefore wrong)
	 */
	public void setUnformattedValue(String ref,Object value){
		if (ref.indexOf(" 0 R") == -1 && ref.indexOf(" 0 X") == -1) {
			ref = (String) nameToRef.get(ref);
		}
		lastUnformattedValue.put(ref, value);
	}
	
	/** if ref is not a reference we find a reference from a names table (which can be doubled up and therefore wrong)
	 */
	public void setLastValidValue(String ref,Object value){
		if (ref.indexOf(" 0 R") == -1 && ref.indexOf(" 0 X") == -1) {
			ref = (String) nameToRef.get(ref);
		}
		lastValidValue.put(ref, value);
	}
	
	public Object setValue(String ref, Object value, boolean isValid, boolean isFormatted, Object oldValue){
		// track so we can reset if needed
		if (isValid) {
			setLastValidValue(ref, value);
		}
		// save raw version before we overwrite
		if (isFormatted) {
			setUnformattedValue(ref,oldValue);
		}
		
		Object checkObj;
		if (ref.indexOf("R") != -1) {
			checkObj = refToCompIndex.get(ref);
		} else {
			checkObj = nameToCompIndex.get(ref);
		}
		
		//Fix null exception in /PDFdata/baseline_screens/forms/406302.pdf
		if(checkObj==null)
			return null;
		
		// Now set the formObject value so we keep track of the current field value within our FormObject
		String pdfRef = convertIDtoRef(((Integer)checkObj).intValue());
		FormObject form = ((FormObject)rawFormData.get(pdfRef));
//		System.out.println("ComponentData.setValue("+ref+" formvalue="+value+")");
		form.setValue((String)value);
		
		return checkObj;
	}

	public String getFieldNameFromRef(String ref){
		return ((FormObject)rawFormData.get(ref)).getTextStreamValue(PdfDictionary.T);
	}
	
	public static int calculateFontSize(int height, int width, boolean area,String text) {
		int lineLen = text.length();
		
		double v1 = height*0.85;
		if(lineLen==0)
			return (int)v1;
		
		double v2 = width/lineLen;
		if(v1>v2*2){
			return (int)v2;
		}else{ 
			return (int)v1;
		}
	}
	
	public boolean formsRasterizedForDisplay() {
		return this.rasterizeForms;
	}

}
