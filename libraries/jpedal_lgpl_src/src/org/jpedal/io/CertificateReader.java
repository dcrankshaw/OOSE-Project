package org.jpedal.io;

import java.security.Key;
import java.security.cert.Certificate;

public class CertificateReader {

	//<start-adobe><start-wrap>
	public static byte[] readCertificate(byte[][] recipients, Certificate certificate, Key key) {
		
		byte[] envelopedData=null;
		
		//<start-me>
		/**
         * values for BC
         */
        String provider="BC";

        int numberOfRecipients=recipients.length;
        
		/**
         * loop through all and get data if match found
         */
        for(int j=0;j<numberOfRecipients;j++){

            try {
                org.bouncycastle.cms.CMSEnvelopedData recipientEnvelope = new org.bouncycastle.cms.CMSEnvelopedData(recipients[j]);

                Object[] recipientList = recipientEnvelope.getRecipientInfos().getRecipients().toArray();
                int listCount=recipientList.length;

                for (int ii=0;ii<listCount;ii++) {
                    org.bouncycastle.cms.RecipientInformation recipientInfo = (org.bouncycastle.cms.RecipientInformation) recipientList[ii];

                    if (recipientInfo.getRID().match(certificate)){
                        envelopedData = recipientInfo.getContent(key, provider);
                        ii=listCount;
                    }
                }
            } catch (Exception ee) {
                ee.printStackTrace();
            }
        }
      //<end-me>
       
        return envelopedData;
	}
	
	//<end-wrap><end-adobe>

}
