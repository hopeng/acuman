package com.macquarie.ofr.dawn.centera;

import com.filepool.fplibrary.FPLibraryException;

/**
 * Centera Store Service
 * @author srathor
 *
 */
public interface CenteraStoreService {

	/**
	 * This method will retrieve the canon message from the Centera datastore based on the clipId passed to it.
	 * @param clipId - the unique key to retrieve the canon message from Centera datastore.
	 * @return Canon message in String format.
	 * @throws Exception 
	 */
	public String getCanon(String clipId) throws Exception;


	/**
	 * This method will store a canon message in Centera data store.
	 * @param uri - uri for the canon message in NDS, it will we saved as uri attribute in canon store.
	 * @param content - canon message to be stored.
	 * @return ClipId string will be returned, it is the unique key to retrieve the canon message; attached to actual canon message in the NDS after a message is stored in Centera for future reference retrieval.
	 * @throws FPLibraryException 
	 * @throws Exception 
	 */
	public String storeCanon(String uri, String content) throws Exception;


}
