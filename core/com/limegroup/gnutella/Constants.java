package com.limegroup.gnutella;

/**
* A class to keep together the constants that may be used by multiple classes
* @author  Anurag Singla
*/
public final class Constants {
    
    private Constants() {}

    public static final String ENTRY_SEPARATOR = ",";
    
    /**
     * Mime Type to be used when returning QueryReplies on receiving a
     * HTTP request (or some other content request)
     */
    public static final String QUERYREPLY_MIME_TYPE = 
        "application/x-gnutella-packets";
    
    /**
     * Constant for the timeout to use on sockets.
     */
    public static final int TIMEOUT = 8000;  

    /**
     * how long a minute is.  Not final so that tests can change it.
     */
    public static long MINUTE = 60*1000;
    
    /**
	 * Identifier for ISO-Latin-1 encoding
	 */
	public static final String ASCII_ENCODING = "ISO-8859-1";

	/**
	 * Identifier for UTF-8 encoding
	 */
	public static final String UTF_8_ENCODING = "UTF-8";
}
