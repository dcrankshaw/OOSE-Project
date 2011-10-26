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
* Vector_String.java
* ---------------
*/
package org.jpedal.utils.repositories;
import java.io.Serializable;

/**
 * Provides the functionality/convenience of a Vector for ints - 
 *
 * Much faster because not synchronized and no cast - 
 * Does not double in size each time
 */
public class Vector_String implements Serializable
{

    //how much we resize each time - will be doubled up to 160
    int  increment_size = 1000;
    protected int  current_item = 0;

    //current max size
    int  max_size = 250;

    //holds the data
	private String[] items = new String[max_size];
	////////////////////////////////////
	
//	public String toString(){
//		String ret="";
//		for (int i = 0; i < current_item; i++) {
//			ret += items[i]+",";
//		}
//		return ret;
//	}

	//set size
	public Vector_String( int number ) 
	{
		max_size = number;
		items = new String[max_size];
	}
	
	
	
	//default size
	public Vector_String() 
	{
		
	}

    protected static int incrementSize(int increment_size){

		if(increment_size<8000)
		       increment_size=increment_size*4;
		    else if(increment_size<16000)
		       increment_size=increment_size*2;
	        else
	            increment_size=increment_size+2000;
		return increment_size;
	}
    
    /**
	 * extract underlying data
	 */
	final public String[] get()
	{
		return items;
	}
	///////////////////////////////////
	/**
	 * remove element at
	 */
	final public String elementAt( int id )
	{
		String value=null;
		
		if( id < max_size )
		value = items[id];
		
		//catch for null value
		if( value == null )
			value = "";
		
		return value;
	}
	////////////////////////////////////
	/**
	 * see if value present
	 */
	final public boolean contains( String value )
	{
		boolean flag = false;
		for( int i = 0;i < current_item;i++ )
		{
			if( items[i].equals( value ) )
			{
				i = current_item + 1;
				flag = true;
			}
		}
		return flag;
	}
	////////////////////////////////////
	//merge
	final public void merge( int master, int child, String separator )
	{
		items[master] = items[master] + separator + items[child];
		items[child] = null;
	}
	///////////////////////////////////
	/**
	 * clear the array
	 */
	final public void clear()
	{
		//items = null;
		//holds the data
		//items = new String[max_size];
		if(current_item>0){
			for(int i=0;i<current_item;i++)
				items[i]=null;
		}else{
			for(int i=0;i<max_size;i++)
				items[i]=null;
		}
		current_item = 0;
	}
	///////////////////////////////////
	/**
	 * remove element at
	 */
	final public void removeElementAt( int id )
	{
		if( id >= 0 )
		{
			//copy all items back one to over-write
            System.arraycopy(items, id + 1, items, id, current_item - 1 - id);
			
			//flush last item
			items[current_item - 1] = "";
		}
		else
			items[0] = "";
		//reduce counter
		current_item--;
	}
	///////////////////////////////////
	/**
	 * replace underlying data
	 */
	final public void set( String[] new_items )
	{
		items = new_items;
	}
	///////////////////////////////////
	/**
	 * add an item
	 */
	final public void addElement( String value )
	{
		checkSize( current_item );
		items[current_item] = value;
		current_item++;
	}
	///////////////////////////////////
	/**
	 * return the size
	 */
	final public int size()
	{
		return current_item + 1;
	}
	///////////////////////////////////
	/**
	 * set an element
	 */
	final public void setElementAt( String new_name, int id )
	{
		if( id >= max_size )
		checkSize( id );
		
		items[id] = new_name;
	}
	////////////////////////////////////
	/**
	 * check the size of the array and increase if needed
	 */
	final private void checkSize( int i )
	{
		if( i >= max_size )
		{
			int old_size = max_size;
			max_size = max_size + increment_size;
			
			//allow for it not creating space
			if( max_size <= i )
			max_size = i +increment_size+ 2;
			
			String[] temp = items;
			items = new String[max_size];
			System.arraycopy( temp, 0, items, 0, old_size );
			
			//increase size increase for next time
			increment_size=incrementSize(increment_size);
		}
	}
	
	public void trim(){
		
		String[] newItems = new String[current_item];
		
		System.arraycopy(items,0,newItems,0,current_item);
		
		items=newItems;
		max_size=current_item;
	}

    /**reset pointer used in add to remove items above*/
    public void setSize(int currentItem) {
        current_item=currentItem;
    }
}
