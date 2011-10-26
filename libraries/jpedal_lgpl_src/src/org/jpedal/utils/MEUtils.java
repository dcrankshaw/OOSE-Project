package org.jpedal.utils;

public class MEUtils {
	
	public static double hypot(double x,double y){
		// <start-me>
    	return Math.hypot(x,y);
    	/*<end-me>
    	return Math.sqrt((x*x)+(y*y));
    	/**/
	}

	public static String replaceAll(String string, String find,String replace) {
		// <start-me>
		return string.replaceAll(find,replace);
		/* <end-me>
		int index = string.indexOf(find);
    	while(index!=-1){
    		string = string.substring(0,index)+
    			replace+string.substring(index+(find.length()),string.length());
    		index = string.indexOf(find);
    	}
    	
    	return string;
    	/**/
	}

}
