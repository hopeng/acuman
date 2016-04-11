package com.macquarie.ofr.dawn;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.macquarie.ofr.dawn.centera.CenteraStoreService;
import com.macquarie.ofr.mds.ms.marklogic.MarklogicConnectionFactory;
import com.marklogic.client.DatabaseClient;
import com.marklogic.client.document.XMLDocumentManager;
import com.marklogic.client.io.DocumentMetadataHandle;
import com.marklogic.client.io.DocumentMetadataHandle.DocumentCollections;

@Service
public class WormClient {

	private final Logger log = LoggerFactory.getLogger(this.getClass().getName());

	@Autowired
	private CenteraStoreService centeraStoreService;

	@Autowired
	private MarklogicConnectionFactory marklogicConnectionFactory;

	private DatabaseClient client;

	private final static String CENTERA_COLLECTION = "/centera/";

	/**
	 * 	Fetches canonMessge for input uri from WORM-Centera Store
	 * @param uri
	 * @return If canon found canon message string else null;
	 */
	public String getDocumentContent(String uri) {  				
		String clipId = getDocumentClipId(uri);
		if(clipId != null){
			try {
				return centeraStoreService.getCanon(clipId);
			} catch (Exception e) {
				log.error("Error getting document from Centera for URI {}", uri);
				e.printStackTrace();
			}
		}
		return null;
	}

	/**
	 * Get marklogic client.
	 * @return
	 */
	private DatabaseClient getClient() {
		if (client == null)
			client = marklogicConnectionFactory.getInstance();
		return client;
	}

	/**
	 * Gets clip id for a canon message by looking into its meta data collection.
	 * @param uri
	 * @return
	 */
	private String getDocumentClipId(String uri){		
		XMLDocumentManager docMgr = getClient().newXMLDocumentManager();
		DocumentMetadataHandle metadataHandle = new DocumentMetadataHandle();
		docMgr.readMetadata(uri, metadataHandle);
		DocumentCollections collections = metadataHandle.getCollections();
		String clipId = null;
		for (String collection : collections){					
			if (collection.contains(CENTERA_COLLECTION)){
				String[] result = collection.split("/");
				clipId = result[2];
				break;				
			}					
		} 		
		if(clipId == null){
			log.info("Clip id not found for URI{}.", uri);
		}
		return clipId;
	}
}
