/**
 * ===========================================
 * Java Pdf Extraction Decoding Access Library
 * ===========================================
 *
 * Project Info:  http://www.jpedal.org
 * (C) Copyright 1997-2011, IDRsolutions and Contributors.
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
  * DecryptionFactory.java
  * ---------------
 */
package org.jpedal.io;

import org.jpedal.constants.PDFflags;
import org.jpedal.exception.PdfSecurityException;
import org.jpedal.objects.raw.PdfArrayIterator;
import org.jpedal.objects.raw.PdfDictionary;
import org.jpedal.objects.raw.PdfKeyPairsIterator;
import org.jpedal.objects.raw.PdfObject;
import org.jpedal.utils.LogWriter;
import org.jpedal.utils.ObjectCloneFactory;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.security.Key;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Provide AES/RSA decryption support
 */
public class DecryptionFactory {

    private Map cachedObjects=new HashMap();

    /**flag to show if extraction allowed*/
    private boolean extractionIsAllowed = true;

    /**flag to show provider read*/
    private boolean isInitialised=false;

    private boolean isMetaDataEncypted=true;

    /**flag if password supplied*/
    private boolean isPasswordSupplied=false;

    private boolean stringsEncoded=false;

    /**flag to show data encrytped*/
    private boolean isEncrypted = false;

    /**key used for encryption*/
    private byte[] encryptionKey=null;

    /** revision used for encryption*/
    private int rev=0;

    /**P value in encryption*/
    private int P=0;

    /**O value in encryption*/
    private byte[] O=null;

    /**U value in encryption*/
    private byte[] U=null;

    // <start-me>
    //SecOP java ME - removed to remove additional package secop1_0.jar in java ME
    /**cipher used for decryption*/
    Cipher cipher=null;
    // <end-me>

    //show if AES encryption
    private boolean isAES=false;

    private PdfObject StmFObj,StrFObj;

    private static boolean alwaysReinitCipher=false;

    static{
        String flag=System.getProperty("org.jpedal.cipher.reinit");
        if(flag!=null && flag.toLowerCase().equals("true"))
            alwaysReinitCipher=true;

    }

    /**encryption padding*/
    private final String[] pad={"28","BF","4E","5E","4E","75","8A","41","64","00","4E","56","FF","FA","01","08",
            "2E","2E","00","B6","D0","68","3E","80","2F","0C","A9","FE","64","53","69","7A"};

    private boolean isAESIdentity=false;

    /**length of encryption key used*/
    private int keyLength=5;

    /**flag to show if user can view file*/
    private boolean isFileViewable=true;

    //tell user status on password
    private int passwordStatus=0;

    /**holds file ID*/
    private byte[] ID=null;


    /**encryption password*/
    private byte[] encryptionPassword = null;

    private Certificate certificate;

    private Key key;

    public DecryptionFactory(byte[] ID, byte[] encryptionPassword){
        this.ID=ID;
        this.encryptionPassword=encryptionPassword;
    }

    /**
     * version for using public certificates
     * @param id
     * @param certificate
     * @param key
     */
    public DecryptionFactory(byte[] id, Certificate certificate, PrivateKey key) {
        this.ID=id;
        this.certificate=certificate;
        this.key=key;
    }

    /**see if valid for password*/
    private boolean testPassword() throws PdfSecurityException {

        boolean isMatch=true;

        int count=32;

        byte[] rawValue=new byte[32];
        byte[] keyValue ;

        for(int i=0;i<32;i++)
            rawValue[i]=(byte)Integer.parseInt(pad[i],16);

        byte[] encrypted= ObjectCloneFactory.cloneArray(rawValue);

        if (rev==2) {
            encryptionKey=calculateKey(O,P,ID);
            encrypted=decrypt(encrypted,"", true,null,false,false);

        } else if(rev>=3) {

            //use StmF values in preference
            int keyLength=this.keyLength;

            if(rev==4 && StmFObj!=null){
                int lenKey=StmFObj.getInt(PdfDictionary.Length);
                if(lenKey!=-1)
                    keyLength=lenKey;
            }

            count=16;
            encryptionKey=calculateKey(O,P,ID);
            byte[] originalKey= ObjectCloneFactory.cloneArray(encryptionKey);

            MessageDigest md = null;
            try {
                md = MessageDigest.getInstance("MD5");
            } catch (Exception e) {
                if(LogWriter.isOutput())
                    LogWriter.writeLog("Exception " + e + " with digest");
            }

            md.update(encrypted);

            //feed in ID
            keyValue = md.digest(ID);

            keyValue=decrypt(keyValue,"", true,null,true,false);

            byte[] nextKey = new byte[keyLength];

            for (int i=1; i<=19; i++) {

                for (int j=0; j<keyLength; j++)
                    nextKey[j] = (byte)(originalKey[j] ^ i);

                encryptionKey=nextKey;

                keyValue=decrypt(keyValue,"", true,null,true,false);

            }

            encryptionKey=originalKey;

            encrypted = new byte[32];
            System.arraycopy(keyValue,0, encrypted,0, 16);
            System.arraycopy(rawValue,0, encrypted,16, 16);

        }


        for(int i=0;i<count;i++){
            if(U[i]!=encrypted[i]){
                isMatch=false;
                i=U.length;
            }
        }

        return isMatch;
    }

    /**set the key value*/
    private void computeEncryptionKey() throws PdfSecurityException{

        MessageDigest md;

        /**calculate key to use*/
        byte[] key=getPaddedKey(encryptionPassword);

        /**feed into Md5 function*/
        try{

            // Obtain a message digest object.
            md = MessageDigest.getInstance("MD5");
            encryptionKey=md.digest(key);

            /**rev 3 extra security*/
            if(rev>=3){
                for (int ii=0; ii<50; ii++)
                    encryptionKey = md.digest(encryptionKey);
            }

        }catch(Exception e){
            throw new PdfSecurityException("Exception "+e+" generating encryption key");
        }
    }

    /**see if valid for password*/
    private boolean testOwnerPassword() throws PdfSecurityException{

        byte[] originalPassword=encryptionPassword;

        byte[] userPasswd=new byte[keyLength];
        byte[] inputValue= ObjectCloneFactory.cloneArray(O);

        computeEncryptionKey();

        byte[] originalKey= ObjectCloneFactory.cloneArray(encryptionKey);

        if(rev==2){
            userPasswd=decrypt(ObjectCloneFactory.cloneArray(O),"", false,null,false,false);
        }else if(rev>=3){

            //use StmF values in preference
            int keyLength=this.keyLength;
            if(rev==4 && StmFObj!=null){
                int lenKey=StmFObj.getInt(PdfDictionary.Length);
                if(lenKey!=-1)
                    keyLength=lenKey;

            }

            userPasswd=inputValue;
            byte[] nextKey = new byte[keyLength];


            for (int i=19; i>=0; i--) {

                for (int j=0; j<keyLength; j++)
                    nextKey[j] = (byte)(originalKey[j] ^ i);

                encryptionKey=nextKey;
                userPasswd=decrypt(userPasswd,"", false,null,true,false);

            }
        }

        //this value is the user password if correct
        //so test
        encryptionPassword = userPasswd;

        computeEncryptionKey();

        boolean isMatch=testPassword();

        //put back to original if not in fact correct
        if(!isMatch){
            encryptionPassword=originalPassword;
            computeEncryptionKey();
        }

        return isMatch;
    }

    /**test password and set access settings*/
    private void verifyAccess() throws PdfSecurityException{

        /**assume false*/
        isPasswordSupplied=false;
        extractionIsAllowed=false;

        passwordStatus= PDFflags.NO_VALID_PASSWORD;

        /**workout if user or owner password valid*/
        boolean isOwnerPassword =testOwnerPassword();
        boolean isUserPassword=testPassword();

        if(isOwnerPassword)
            passwordStatus=PDFflags.VALID_OWNER_PASSWORD;

        if(isUserPassword)
            passwordStatus=passwordStatus+PDFflags.VALID_USER_PASSWORD;


        if(!isOwnerPassword){

            /**test if user first*/
            if(isUserPassword){

                //tell if not default value
                if(encryptionPassword!=null && encryptionPassword.length>0 && LogWriter.isOutput())
                    LogWriter.writeLog("Correct user password supplied ");

                isFileViewable=true;
                isPasswordSupplied=true;

                if((P & 16)==16)
                    extractionIsAllowed=true;

            }else
                throw new PdfSecurityException("No valid password supplied");

        }else{
            if(LogWriter.isOutput())
                LogWriter.writeLog("Correct owner password supplied");

            isFileViewable=true;
            isPasswordSupplied=true;
            extractionIsAllowed=true;
        }
    }

    /**
     * routine to create a padded key
     */
    private byte[] getPaddedKey(byte[] password){

        /**get 32 bytes for  the key*/
        byte[] key=new byte[32];
        int passwordLength=0;

        if(password!=null){
            passwordLength=password.length;
            if(passwordLength>32)
                passwordLength=32;
        }

        if(encryptionPassword!=null)
            System.arraycopy(encryptionPassword, 0, key, 0, passwordLength);

        for(int ii=passwordLength;ii<32;ii++){

            key[ii]=(byte)Integer.parseInt(pad[ii-passwordLength],16);

        }


        return key;
    }
    /**
     * calculate the key
     */
    private byte[] calculateKey(byte[] O,int P,byte[] ID) throws PdfSecurityException{

        /**calculate key to use*/
        byte[] key=getPaddedKey(encryptionPassword),keyValue;

        /**feed into Md5 function*/
        try{

            // Obtain a message digest object.
            MessageDigest md = MessageDigest.getInstance("MD5");

            //add in padded key
            md.update(key);

            //write in O value
            md.update(O);

            //P value
            md.update(new byte[]{(byte)((P) & 0xff),(byte)((P>>8) & 0xff),(byte)((P>>16) & 0xff),(byte)((P>>24) & 0xff)});

            if(ID!=null)
                md.update(ID);

            if (rev==4 && !isMetaDataEncypted)
                md.update(new byte[]{(byte)255,(byte)255,(byte)255,(byte)255});

            byte digest[] = new byte[keyLength];
            System.arraycopy(md.digest(), 0, digest, 0, keyLength);

            //for rev 3
            if(rev>=3){
                for (int i = 0; i < 50; ++i)
                    System.arraycopy(md.digest(digest), 0, digest, 0, keyLength);
            }

            keyValue=new byte[keyLength];
            System.arraycopy(digest, 0, keyValue, 0, keyLength);

        }catch(Exception e){

            e.printStackTrace();
            throw new PdfSecurityException("Exception "+e+" generating encryption key");
        }

        /**put significant bytes into key*/
        byte[] returnKey = new byte[keyLength];
        System.arraycopy(keyValue,0, returnKey,0, keyLength);

        return returnKey;
    }

    /**extract  metadata for  encryption object
     */
    public void readEncryptionObject(PdfObject encyptionObj) throws PdfSecurityException {

        //reset flags
        stringsEncoded=false;
        isMetaDataEncypted=true;
        StmFObj=null;
        StrFObj=null;
        isAES=false;

        if (!isInitialised) {
            isInitialised = true;
            SetSecurity.init();
        }

        //check type of filter and type and see if supported
        int v = encyptionObj.getInt(PdfDictionary.V);

        //get filter value
        PdfArrayIterator filters = encyptionObj.getMixedArray(PdfDictionary.Filter);
        int firstValue=PdfDictionary.Standard;
        if(filters!=null && filters.hasMoreTokens())
            firstValue=filters.getNextValueAsConstant(false);

        //throw exception if we have an unsupported encryption method
        if(v==3)
            throw new PdfSecurityException("Unsupported Custom Adobe Encryption method");
        else if (v > 4){
            if(firstValue!=PdfDictionary.Standard)
                throw new PdfSecurityException("Unsupported Encryption method");
        }

        int newLength=encyptionObj.getInt(PdfDictionary.Length)>>3;
        if(newLength!=-1)
            this.keyLength=newLength;

        //get rest of the values (which are not optional)
        rev = encyptionObj.getInt(PdfDictionary.R);
        P = encyptionObj.getInt(PdfDictionary.P);
        O = encyptionObj.getTextStreamValueAsByte(PdfDictionary.O);
        U = encyptionObj.getTextStreamValueAsByte(PdfDictionary.U);

        //get additional AES values
        if(v==4){

            isAES=true;

            String CFkey;

            PdfObject CF=encyptionObj.getDictionary(PdfDictionary.CF);

            //EFF=encyptionObj.getName(PdfDictionary.EFF);
            //CFM=encyptionObj.getName(PdfDictionary.CFM);

            isMetaDataEncypted=encyptionObj.getBoolean(PdfDictionary.EncryptMetadata);

            //now set any specific crypt values for StrF (strings) and StmF (streams)
            isAESIdentity=false;
            String key=encyptionObj.getName(PdfDictionary.StrF);

            if(key!=null){

                isAESIdentity=key.equals("Identity");

                stringsEncoded=true;

                PdfKeyPairsIterator keyPairs=CF.getKeyPairsIterator();

                while(keyPairs.hasMorePairs()){

                    CFkey=keyPairs.getNextKeyAsString();

                    if(CFkey.equals(key))
                        StrFObj=keyPairs.getNextValueAsDictionary();

                    //roll on
                    keyPairs.nextPair();
                }
            }

            key=encyptionObj.getName(PdfDictionary.StmF);

            if(key!=null){

                isAESIdentity=key.equals("Identity");

                PdfKeyPairsIterator keyPairs=CF.getKeyPairsIterator();

                while(keyPairs.hasMorePairs()){

                    CFkey=keyPairs.getNextKeyAsString();

                    if(CFkey.equals(key))
                        StmFObj=keyPairs.getNextValueAsDictionary();

                    //roll on
                    keyPairs.nextPair();
                }
            }
        }

        isEncrypted = true;
        isFileViewable = false;

        if(LogWriter.isOutput())
            LogWriter.writeLog("File has encryption settings");

        //test if encrypted with password (not certificate)
        if(firstValue==PdfDictionary.Standard){
            try{
                verifyAccess();
            }catch(PdfSecurityException e){
                if(LogWriter.isOutput())
                    LogWriter.writeLog("File requires password");
            }
        }else if(certificate!=null){

            /**
             * set flags and assume it will work correctly
             * (no validation at this point - error will be thrown in decrypt if not)
             */
            isFileViewable=true;
            isPasswordSupplied=true;
            extractionIsAllowed=true;

            passwordStatus=PDFflags.VALID_OWNER_PASSWORD;

        }
    }

    /**
     * setup password value isung certificate passed in by User
     */
    private void setPasswordFromCertificate(PdfObject AESObj){
        /**
         * if recipients set, use that for calculating key
         */
        byte[][] recipients = (AESObj.getStringArray(PdfDictionary.Recipients));

        if(recipients!=null){
            int numberOfRecipients=recipients.length;

            byte[] envelopedData=SetSecurity.extractCertificateData(recipients,certificate,key);

            /**
             * use match to create the key
             */
            if(envelopedData!=null){

                try {
                    MessageDigest md = MessageDigest.getInstance("SHA-1");
                    md.update(envelopedData, 0, 20);
                    for (int i = 0; i<numberOfRecipients; i++) {
                        md.update(recipients[i]);
                    }

                    if (!isMetaDataEncypted)
                        md.update(new byte[]{(byte)255, (byte)255, (byte)255, (byte)255});

                    encryptionKey = md.digest();
                }catch (Exception ee) {
                    ee.printStackTrace();
                }
            }
        }
    }

    /**
     * reads the line/s from file which make up an object
     * includes move
     */
    public byte[] decrypt(byte[] data, String ref,boolean isEncryption,
                          String cacheName,boolean alwaysUseRC4,
                          boolean isString) throws PdfSecurityException{

        //boolean debug=false;//ref.equals("100 0 R");

        if(getBooleanValue(PDFflags.IS_FILE_ENCRYPTED) || isEncryption){

            BufferedOutputStream streamCache= null;
            BufferedInputStream bis = null;
            //int streamLength=0;

            boolean isAES=false;

            byte[] AESData=null;

            if(cacheName!=null){ //this version is used if we cache large object to disk
                //rename file
                try {

                    //we may need bytes for key
                    if(data==null){
                        AESData=new byte[16];
                        FileInputStream fis=new FileInputStream(cacheName);
                        fis.read(AESData);
                        fis.close();
                    }

                    //streamLength = (int) new File(cacheName).length();

                    File tempFile2 = File.createTempFile("jpedal",".raw",new File(ObjectStore.temp_dir));

                    cachedObjects.put(tempFile2.getAbsolutePath(),"x");
                    //System.out.println(">>>"+tempFile2.getAbsolutePath());
                    ObjectStore.copy(cacheName,tempFile2.getAbsolutePath());

                    File rawFile=new File(cacheName);
                    rawFile.delete();

                    //decrypt
                    streamCache = new BufferedOutputStream(new FileOutputStream(cacheName));
                    bis=new BufferedInputStream(new FileInputStream(tempFile2));

                } catch (IOException e1) {
                    if(LogWriter.isOutput())
                        LogWriter.writeLog("Exception "+e1+" in decrypt");
                }
            }

            //default values for rsa
            int keyLength=this.keyLength;
            String algorithm="RC4",keyType="RC4";
            // <start-me>
            //SecOP java ME - removed to remove additional package secop1_0.jar in java ME
            IvParameterSpec ivSpec = null;
            // <end-me>

            //select for stream or string
            PdfObject AESObj ;
            if(!isString){
                AESObj=StmFObj;
            }else{
                AESObj=StrFObj;
            }

            /**
             * reset each time as can change
             * (we can add flag later if slow)
             */
            if(certificate!=null){
                setPasswordFromCertificate(AESObj);

                //ensure value set so code below works
                AESObj.setIntNumber(PdfDictionary.Length,16);
            }

            //AES identity
            if(!alwaysUseRC4 && AESObj==null && isAESIdentity)
                return data;

            //use RC4 as default but override if needed
            if(AESObj!=null){

                //use CF values in preference

                int AESLength=AESObj.getInt(PdfDictionary.Length);
                if(AESLength!=-1)
                    keyLength=AESLength;

                String cryptName=AESObj.getName(PdfDictionary.CFM);

                if(cryptName!=null && cryptName.equals("AESV2") && !alwaysUseRC4){

                    // <start-me>
                    //SecOP java ME - removed to remove additional package secop1_0.jar in java ME
                    cipher=null; //force reset as may be rsa
                    // <end-me>

                    algorithm="AES/CBC/PKCS5Padding";
                    keyType="AES";

                    isAES=true;

                    //setup CBC
                    byte[] iv=new byte[16];
                    if(AESData!=null)
                        System.arraycopy(AESData, 0, iv, 0, 16);
                    else
                        System.arraycopy(data, 0, iv, 0, 16);

                    // <start-me>
                    //SecOP java ME - removed to remove additional package secop1_0.jar in java ME
                    ivSpec = new IvParameterSpec(iv);
                    // <end-me>

                    //and knock off iv data in memory or cache
                    if(data==null){
                        try {
                            bis.skip(16);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }else{
                        int origLen=data.length;
                        int newLen=origLen-16;
                        byte[] newData=new byte[newLen];
                        System.arraycopy(data, 16, newData, 0, newLen);
                        data=newData;

                        //make sure data correct size
                        int diff= (data.length & 15);
                        int newLength=data.length;
                        if(diff>0){
                            newLength=newLength+16-diff;

                            newData=new byte[newLength];

                            System.arraycopy(data, 0, newData, 0, data.length);
                            data=newData;
                        }
                    }
                }
            }

            byte[] currentKey=new byte[keyLength];

            if(ref.length()>0)
                currentKey=new byte[keyLength+5];

            System.arraycopy(encryptionKey, 0, currentKey, 0, keyLength);

            try{
                //add in Object ref id if any
                if(ref.length()>0){
                    int pointer=ref.indexOf(' ');
                    int pointer2=ref.indexOf(' ',pointer+1);

                    int obj=Integer.parseInt(ref.substring(0,pointer));
                    int gen=Integer.parseInt(ref.substring(pointer+1,pointer2));

                    currentKey[keyLength]=((byte)(obj & 0xff));
                    currentKey[keyLength+1]=((byte)((obj>>8) & 0xff));
                    currentKey[keyLength+2]=((byte)((obj>>16) & 0xff));
                    currentKey[keyLength+3]=((byte)(gen & 0xff));
                    currentKey[keyLength+4]=((byte)((gen>>8) & 0xff));
                }

                byte[] finalKey = new byte[Math.min(currentKey.length,16)];

                if(ref.length()>0){
                    MessageDigest currentDigest =MessageDigest.getInstance("MD5");
                    currentDigest.update(currentKey);

                    //add in salt
                    if(isAES && keyLength>=16){
                        byte[] salt = {(byte)0x73, (byte)0x41, (byte)0x6c, (byte)0x54};

                        currentDigest.update(salt);
                    }
                    System.arraycopy(currentDigest.digest(),0, finalKey,0, finalKey.length);
                }else{
                    System.arraycopy(currentKey,0, finalKey,0, finalKey.length);
                }

                // <start-me>
                //SecOP java ME - removed to remove additional package secop1_0.jar in java ME
                /**only initialise once - seems to take a long time*/
                if(cipher==null)
                    cipher = Cipher.getInstance(algorithm);

                SecretKey testKey = new SecretKeySpec(finalKey, keyType);

                if(isEncryption)
                    cipher.init(Cipher.ENCRYPT_MODE, testKey);
                else{
                    if(ivSpec==null)
                        cipher.init(Cipher.DECRYPT_MODE, testKey);
                    else //aes
                        cipher.init(Cipher.DECRYPT_MODE, testKey,ivSpec);
                }

                //if data on disk read a byte at a time and write back

                if(streamCache!=null){
                    CipherInputStream cis=new CipherInputStream(bis,cipher);
                    int nextByte;
                    while(true){
                        nextByte=cis.read();
                        if(nextByte==-1)
                            break;
                        streamCache.write(nextByte);
                    }
                    cis.close();
                    streamCache.close();
                    bis.close();

                }

                if(data!=null)
                    data=cipher.doFinal(data);

                // <end-me>

            }catch(Exception e){

                throw new PdfSecurityException("Exception "+e+" decrypting content");

            }

        }

        // <start-me>
        //SecOP java ME - removed to remove additional package secop1_0.jar in java ME
        if(alwaysReinitCipher)
            cipher=null;
        // <end-me>

        return data;
    }

    /**show if file can be displayed*/
    public boolean getBooleanValue(int key) {

        switch(key){
            case PDFflags.IS_FILE_VIEWABLE:
                return isFileViewable;

            case PDFflags.IS_FILE_ENCRYPTED:
                return isEncrypted;

            case PDFflags.IS_METADATA_ENCRYPTED:
                return isMetaDataEncypted;

            case PDFflags.IS_EXTRACTION_ALLOWED:
                return extractionIsAllowed;

            case PDFflags.IS_PASSWORD_SUPPLIED:
                return isPasswordSupplied;
        }


        return false;
    }

    public byte[] decryptString(byte[] newString, String objectRef) throws PdfSecurityException {

        try{
            if((!isAES || stringsEncoded || isMetaDataEncypted))
                newString=decrypt(newString, objectRef, false, null, false, true);
        }catch(Exception e){
            if(LogWriter.isOutput())
                LogWriter.writeLog("Unable to decrypt string in Object "+objectRef+" "+new String(newString));
        }

        return newString;

    }

    public int getPDFflag(Integer flag) {

        if(flag.equals(PDFflags.USER_ACCESS_PERMISSIONS))
            return P;
        else if(flag.equals(PDFflags.VALID_PASSWORD_SUPPLIED))
            return passwordStatus;
        else
            return -1;
    }

    public void reset(byte[] encryptionPassword) {

        this.encryptionPassword=encryptionPassword;

        // <start-me>
        //SecOP java ME - removed to remove additional package secop1_0.jar in java ME
        //reset
        cipher=null;
        // <end-me>
    }

    public void flush() {

        if(cachedObjects!=null){
            Iterator files=cachedObjects.keySet().iterator();
            while(files.hasNext()){
                String fileName=(String)files.next();
                File file=new File(fileName);
                //System.out.println("PdfFileReader - deleting file "+fileName);
                file.delete();
                if(LogWriter.isOutput() && file.exists())
                    LogWriter.writeLog("Unable to delete temp file "+fileName);
            }
        }

    }

    public void dispose() {
        this.cachedObjects=null;

    }
}
