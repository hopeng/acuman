package com.macquarie.ofr.dawn.centera;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.filepool.fplibrary.FPClip;
import com.filepool.fplibrary.FPLibraryConstants;
import com.filepool.fplibrary.FPLibraryException;
import com.filepool.fplibrary.FPPool;
import com.filepool.fplibrary.FPTag;

@Component
public class CenteraStoreServiceImpl implements CenteraStoreService {

	private final Logger log = LoggerFactory.getLogger(this.getClass().getName());

	private static String appVersion="3.1";
	private static String appName = "StructuredWORM_Test";
	long retentionPeriod = 5;
	String CLIP_NAME = "Store-Content-String";
	String VENDOR_NAME = "Macquarie";
	String TAG_NAME = "StoreContentObject";
	String URI = "uri";

	@Autowired
	private CenteraConnectionPool centeraConnectionPool;

	private FPPool thePool = null;

	/**
	 * This method will retrieve the canon message from the Centera datastore based on the clipId passed to it.
	 * @param clipId - the unique key to retrieve the canon message from Centera datastore.
	 * @return Canon message in String format.
	 * @throws Exception 
	 */
	@Override
	public String getCanon(String clipId) throws Exception {
		log.info("searching for clipsId: "+clipId);		
		// create a new named C-Clip
		FPClip theClip = null;
		try {
			thePool = centeraConnectionPool.getPool();
			theClip = new FPClip(thePool, clipId, FPLibraryConstants.FP_OPEN_FLAT);
		} catch (Exception exception) {
			log.error("Error encounterd creating a new clip while searching for clip id: " + clipId );
			throw exception;
		}		
		FPTag topTag = theClip.getTopTag();				
		String attr = topTag.getStringAttribute(URI);
		System.out.println("URI of the Clip: " + attr);		
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();	
		try {
			topTag.BlobRead(byteArrayOutputStream);
		} catch (IOException exception) {
			log.error("Error encounterd reading blob while searching for clip id: " + clipId );
			throw exception;
		}			
		String canonMessage = new String( byteArrayOutputStream.toByteArray(), StandardCharsets.UTF_8 );	
		topTag.Close();
		theClip.Close();
		return canonMessage;		
	}	

	/**
	 * This method will store a canon message in Centera data store.
	 * @param uri - uri for the canon message in NDS, it will we saved as uri attribute in canon store.
	 * @param content - canon message to be stored.
	 * @return ClipId string will be returned, it is the unique key to retrieve the canon message; attached to actual canon message in the NDS after a message is stored in Centera for future reference retrieval.
	 * @throws FPLibraryException 
	 * @throws Exception 
	 */
	@Override
	public String storeCanon(String uri, String content) throws Exception {
		log.info("URI: " + uri + " is recieved  for storage Centera Data Store.");
		// populate a clip to write data on Centera.
		FPClip theClip = null;
		try {
			theClip = createClip();
		} catch (Exception exception) {	
			log.error("Error encounterd while creating a new clip. \nUri: " + uri + ",\nContent: " + content );
			throw exception;
		}		
		String clipId = this.write(theClip, uri, content);
		log.info("URI: " + uri + " is written on Clip Id: " + clipId + " in Centera Data Store.");
		//Closes the opened C-Clip.
		theClip.Close();	
		return clipId;
	}

	/**
	 * Write the data to Centera Store and returns clip id for reference.
	 * @param vClip
	 * @param uri
	 * @param content
	 * @return
	 * @throws Exception
	 */
	private String write( FPClip theClip, String uri, String content ) throws Exception{
		FPTag vTopTag = theClip.getTopTag();		
		FPTag vTag = new FPTag (vTopTag, TAG_NAME) ;
		vTag.setAttribute(URI, uri);
		InputStream value = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
		//Writing data
		try {
			vTag.BlobWrite (value );
		} catch (IOException iOException) {
			log.error("Error encounterd while writing data.\nUri: " + uri + ",\nContent: " + content );
			throw iOException;
		}		
		String id = theClip.Write();
		//Closes the connection to the opened tag.
		vTag.Close();
		//Closes the root tag.
		vTopTag.Close();			
		return id;
	}

	/**
	 * Creates a clip to write data on Centera
	 * @return FPClip isntace
	 * @throws Exception
	 */
	private FPClip createClip() throws Exception{		
		//creating a new named C-Clip
		FPClip theClip = null;
		try{
			thePool = centeraConnectionPool.getPool();
			theClip = new FPClip(thePool, CLIP_NAME);
		} catch (Exception exception) {	
			log.error("Error encounterd while creating a new clip.");
			throw exception;
		} 
		// It's a good practice to write out vendor, application and version info
		theClip.setDescriptionAttribute("app-vendor", VENDOR_NAME);
		theClip.setDescriptionAttribute("app-name", appName);
		theClip.setDescriptionAttribute("app-version", appVersion);
		// It's a good idea to explicitly set retention period.  For more info on retention periods and classes see ManageRetention example.
		theClip.setRetentionPeriod(retentionPeriod);		
		//Opens a top-level (root) tag as a starting point for C-Clip	navigation.
		return theClip;
	}
}
