package com.macquarie.ofr.dawn.centera;

import org.springframework.stereotype.Component;

import com.filepool.fplibrary.FPLibraryConstants;
import com.filepool.fplibrary.FPLibraryException;
import com.filepool.fplibrary.FPPool;

/**
 *  This component provides connection to the Centera datastore.
 * @author srathor
 *
 */

@Component
public class CenteraConnectionPool {
	
	private static FPPool thePool = null;
	private static String appVersion="3.1";
	private static String appName = "StructuredWORM_Test";
	private static int embeddedBlobsize = 50000;
	private static String[] poolAddress = {"10.137.40.31?centera/centrh03_StructuredWORM_Test.pea", "10.137.40.32?centera/centrh03_StructuredWORM_Test.pea", "10.137.40.33?centera/centrh03_StructuredWORM_Test.pea", "10.137.40.34?centera//centrh03_StructuredWORM_Test.pea"};

	/**
	 * Create a pool to the Centera cluster.
	 * @return the  pool instance
	 * @throws Exception
	 */
	public FPPool getPool() throws Exception {
		if(thePool == null) {
			try {    			
				FPPool.RegisterApplication(appName,appVersion);    			    
				// New feature for 2.3 lazy pool open
				FPPool.setGlobalOption(FPLibraryConstants.FP_OPTION_OPENSTRATEGY,FPLibraryConstants.FP_LAZY_OPEN);
				thePool = new FPPool(poolAddress);
				// Prompt user for embedded blob size
				System.out.println("Maximum blob size to embed data in CDF[" + embeddedBlobsize + "]: ");
				// New feature for 2.3 embedded blobs
				FPPool.setGlobalOption(	FPLibraryConstants.FP_OPTION_EMBEDDED_DATA_THRESHOLD,embeddedBlobsize);
			} catch (FPLibraryException e) {    			
				System.out.println("Centera SDK Error: " + e.getMessage());
				throw e;
			} catch (IllegalArgumentException e) {
				System.out.println(e.getMessage() + "" + e);
				throw e;
			}
		}
		return thePool;
	}
}
