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
* -------------
* SignData.java
* -------------
*/

package org.jpedal.examples.simpleviewer.objects;

import java.awt.Rectangle;
import java.io.File;

import org.jpedal.examples.simpleviewer.utils.ItextFunctions;

//import com.itextpdf.text.pdf.PdfSignatureAppearance;

/**
 * Models all the data you need in order to sign a Pdf document.
 */
public class SignData {

	private boolean signMode, canEncrypt, flatten, isVisibleSignature;
	private String outputPath, keyFilePath, keyStorePath, alias, reason, location;
	private char[] keyFilePassword, keyStorePassword, aliasPassword, encryptUserPassword, encryptOwnerPassword;
	private int certifyMode, encryptPermissions;
	float x1, y1, x2, y2;
	private File outputFile, keyFile;

	//Fields for use with checking validity of data.
	private boolean valid = false;
	private String invalidMessage;
	private int signaturePage;
	
	private boolean appendMode;
	 
	/**
	 * @return True if using a keystore file to sign.
	 */
	public boolean isKeystoreSign()
	{
		return signMode;
	}
	
	/**
	 * @param b True if using a keystore file to sign document
	 */
	public void setSignMode(boolean b)
	{
		signMode = b;
	}
	
	/**
	 * @param path Absolute path of the destination of the signed document
	 */
	public void setOutputFilePath(String path)
	{
		outputPath = path;
	}
	
	public String getOutputFilePath()
	{
		return outputPath;
	}
	
	public File getOutput()
	{
		return outputFile;
	}
	
	/**
	 * @param path Absolute path of .pfx file.
	 */
	public void setKeyFilePath(String path)
	{
		keyFilePath = path;
	}
	
	public String getKeyFilePath()
	{
		return keyFilePath;
	}
	
	public File getKeyFile()
	{
		return keyFile;
	}

	public void setKeyStorePath(String path)
	{
		keyStorePath = path;
	}
	
	public String getKeyStorePath()
	{
		return keyStorePath;
	}

	public char[] getKeystorePassword()
	{
		return keyStorePassword;
	}
	
	public void setKeystorePassword(char[] password)
	{
		keyStorePassword = password;
	}

	public String getAlias()
	{
		return alias;
	}
	
	public void setAlias(String alias)
	{
		this.alias = alias;
	}

	public char[] getAliasPassword()
	{
		return aliasPassword;
	}
	
	public void setAliasPassword(char[] password)
	{
		aliasPassword = password;
	}

	public void setKeyFilePassword(char[] password)
	{
		keyFilePassword = password;
	}
	
	public char[] getKeyFilePassword()
	{
		return keyFilePassword;
	}

	public boolean canEncrypt()
	{
		return canEncrypt;
	}
	
	public void setEncrypt(boolean b)
	{
		canEncrypt = b;
	}
	
	public String getReason()
	{
		return reason;
	}
	
	public void setReason(String reason)
	{
		this.reason = reason;
	}

	/**
	 * @param certifyMode Certify mode in accordance with PdfSignatureAppearance constants.
	 */
	public void setCertifyMode(int certifyMode)
	{
		this.certifyMode = certifyMode;
	}
	
	public int getCertifyMode()
	{
		return certifyMode;
	}

	public void setFlatten(boolean selected)
	{
		flatten = selected;
	}
	
	public boolean canFlatten()
	{
		return flatten;
	}

	public void setEncryptUserPass(char[] password)
	{
		encryptUserPassword = password;
	}
	
	public char[] getEncryptUserPass()
	{
		return encryptUserPassword;
	}

	public void setEncryptOwnerPass(char[] password)
	{
		encryptOwnerPassword = password;
	}
	
	public char[] getEncryptOwnerPass()
	{
		return encryptOwnerPassword;
	}

	public void setLocation(String location)
	{
		this.location = location;
	}
	
	public String getLocation()
	{
		return location;
	}

	/**
	 * @param permissions In accordance with PdfWriter constants
	 */
	public void setEncryptPermissions(int permissions)
	{
		encryptPermissions = permissions;
	}
	
	public int getEncryptPermissions()
	{
		return encryptPermissions;
	}
	
	/**
	 * This method is overidden to display messages about its this objects state.
	 * Used after calling validate.
	 */
	public String toString()
	{
		String result;
		
		if(valid) {
			result =  "Output File: " + outputFile.getAbsolutePath() + "\n";
			if(signMode) {
				result += "Keystore: " + keyStorePath + "\n"
				        + "Alias: " + alias + "\n";

			}
			else {
				result += ".pfx File:" + keyFilePath + "\n";
			}
		}
		else {
			return invalidMessage;
		}
		
		result += "Reason: \"" + reason + "\"\n"
		        + "Location: " + location + "\n";
		
		if(canEncrypt()) {
			result += "Encrypt PDF" + "\n";
		}
		if(canFlatten()) {
			result += "Flatten PDF" + "\n";
		}
		if(certifyMode != ItextFunctions.NOT_CERTIFIED) {
			result += "Certify PDF" + "\n";
		}
		return result;
	}

	/**
	 * Initialises and checks validity of files.  This objects toString() method changes to
	 * reflect failures in validation in order for the user to be informed.
	 * 
	 * @return True if the files are valid.
	 */
	public boolean validate()
	{ //#TODO Validate whether an author or encryption signature is possible. 
		outputFile = new File(outputPath);
		
		if(outputFile.exists() || outputFile.isDirectory()) {   		
			invalidMessage = "Output file already exists."; //TODO Signer: Internalisation of messages
			return valid = false;
		}    	
		if(!signMode) {
			keyFile = new File(keyFilePath);
			if(!keyFile.exists() || keyFile.isDirectory()) {   		
				invalidMessage = "Key file not found."; //TODO Signer: Internalisation of messages
				return valid = false;
			}
		}
		return valid = true;
	}

	public boolean isVisibleSignature()
	{
		return isVisibleSignature;
	}
	
	public void setVisibleSignature(boolean b)
	{
		isVisibleSignature = b;
	}

    public void setRectangle(float x1, float y1, float x2, float y2)
    {
    	if(x1<x2) {
    		this.x1 = x1;
    		this.x2 = x2;
    	}
    	else {
    		this.x2 = x1;
    		this.x1 = x2;
    	}
    	
    	if(y1<y2) {
    		this.y1 = y1;
    		this.y2 = y2;
    	}
    	else {
    		this.y2 = y1;
    		this.y1 = y2;
    	}
    }

	/**
	 * @return Four coordinates representing the visible signature area.
	 */
	public float[] getRectangle()
	{
		float result[] = {x1, y1, x2, y2};
        return result;
	}
	
	/**
	 * @return The page to sign
	 */
	public int getSignPage()
	{
		return signaturePage;
	}
	
	public void setSignPage(int page)
	{
		signaturePage = page;
	}
	
	public void setAppend(boolean b)
	{
		appendMode = b;
	}

	public boolean isAppendMode() {
		return appendMode;
	}
}
