package com.limegroup.gnutella;

import java.io.IOException;
import java.io.Writer;
import java.text.ParseException;
import java.util.Comparator;
import java.util.Iterator;

import com.limegroup.gnutella.settings.ConnectionSettings;
import com.limegroup.gnutella.settings.ApplicationSettings;
import com.limegroup.gnutella.util.CommonUtils;
import com.limegroup.gnutella.util.Buffer;
import com.limegroup.gnutella.util.StringUtils;
import com.limegroup.gnutella.util.NetworkUtils;

/**
 * An endpoint with additional history information used to prioritize
 * HostCatcher's permanent list:
 * <ul>
 * <li>The average daily uptime in seconds, as reported by the "DU" GGEP
 *  extension.
 * <li>The system time in milliseconds that this was added to the cache
 * <li>The system times in milliseconds when was successfully connected
 *     to this host.
 * <li>The system times in milliseconds when we tried but failed to connect
 *     to this host.
 * </ul>
 *
 * ExtendedEndpoint has methods to read and write information to a single line
 * of text, e.g.:
 * <pre>
 *    "18.239.0.144:6347,3043,1039939393,529333939;3343434;23433,3934223"
 * </pre>
 * This "poor man's serialization" is used to help HostCatcher implement the
 * reading and writing of gnutella.net files.<p>
 *
 * ExtendedEndpoint does not override the compareTo method because
 * it creates confusion between compareTo and equals.
 * </ul> 
 * For comparing by priority, users should use the return value of 
 * priorityComparator()
 */
public class ExtendedEndpoint extends Endpoint {
    /** The value to use for timeRecorded if unknown.  Doesn't really matter. */
    static final long DEFAULT_TIME_RECORDED=0;
    /** The system time that dailyUptime was encountered, typically when this
     *  was added to the system, or -1 if we don't know (in which case we'll use
     *  DEFAULT_UPTIME_RECORDED for calculations) */
    private long timeRecorded=-1;


    /** The value to use for dailyUptime if not reported by the user.
     * Rationale: by looking at version logs, we find that the average session
     * uptime for a host is about 8.1 minutes.  A study of connection uptimes
     * (http://www.limewire.com/developer/lifetimes/) confirms this.
     * Furthermore, we estimate that users connect to the network about 0.71
     * times per day, for a total of 8.1*60*0.71=345 seconds of uptime per day.
     * 
     * Why not use 0?  If you have to choose between a node with an unknown
     * uptime and one with a confirmed low uptime, you'll gamble on the
     * former; it's unlikely to be worse! */
    static final int DEFAULT_DAILY_UPTIME=345;
    /** The average daily uptime in seconds, as reported by the "DU" GGEP
     *  extension, or -1 if we don't know (in which case we'll use
     *  DEFAULT_DAILY_UPTIME for calculations) */
    private int dailyUptime=-1;

    /** The number of connection attempts (failures) to record. */
    static final int HISTORY_SIZE=3;
    /** Never record two connection attempts (failures) within this many
     *  milliseconds.  Package access for testing. */
    static final long WINDOW_TIME=24*60*60*1000;   //1 day
    /** The system times (each a Long) that when I successfully connected to the
     *  given address.  Sorted by time with the most recent at the head.
     *  Bounded in size, so only the most recent HISTORY_SIZE times are
     *  recorded.  Also, only one entry is recorded for any two connection
     *  successes within a WINDOW_TIME millisecond window. */
    private Buffer /* of Long */ connectSuccesses=new Buffer(HISTORY_SIZE);
    /** Same as connectSuccesses, but for failed connections. */
    private Buffer /* of Long */ connectFailures=new Buffer(HISTORY_SIZE);

    /** the locale of the client that this endpoint represents */
    private String _clientLocale = 
        ApplicationSettings.DEFAULT_LOCALE.getValue();
        
    /**
     * The number of times this has failed while attempting to connect
     * to a UDP host cache.
     * If -1, this is NOT a udp host cache.
     */
    private int udpHostCacheFailures = -1;
    

    /** locale of this client */
    private final static String ownLocale =
        ApplicationSettings.LANGUAGE.getValue();
    
    /**
     * Creates a new ExtendedEndpoint with uptime data read from a ping reply.
     * The creation time is set to the current system time.  It is assumed that
     * that we have not yet attempted a connection to this.
     */
    public ExtendedEndpoint(String host, int port, int dailyUptime) {
        super(host, port);
        this.dailyUptime=dailyUptime;
        this.timeRecorded=now();
    }
    
    /** 
     * Creates a new ExtendedEndpoint without extended uptime information.  (The
     * default will be used.)  The creation time is set to the current system
     * time.  It is assumed that we have not yet attempted a connection to this.  
     */
    public ExtendedEndpoint(String host, int port) { 
        super(host, port);
        this.timeRecorded=now();
    }
    
    /** 
     * Creates a new ExtendedEndpoint without extended uptime information.  (The
     * default will be used.)  The creation time is set to the current system
     * time.  It is assumed that we have not yet attempted a connection to this.  
     * Does not valid the host address.
     */
    public ExtendedEndpoint(String host, int port, boolean strict) { 
        super(host, port, strict);
        this.timeRecorded=now();
    }    
    
    /**
     * creates a new ExtendedEndpoint with the specified locale.
     */
    public ExtendedEndpoint(String host, int port, int dailyUptime,
                            String locale) {
        super(host, port);
        this.dailyUptime = dailyUptime;
        this.timeRecorded = now();
        _clientLocale = locale;
    }

    /**
     * creates a new ExtendedEndpoint with the specified locale
     */
    public ExtendedEndpoint(String host, int port, String locale) {
        this(host, port);
        _clientLocale = locale;
    }
    
    ////////////////////// Mutators and Accessors ///////////////////////

    /** Returns the system time (in milliseconds) when this' was created. */
    public long getTimeRecorded() {
        if (timeRecorded<0)
            return DEFAULT_TIME_RECORDED; //don't know
        else
            return timeRecorded;
    }
 
    /** Returns the average daily uptime (in seconds per day) reported in this'
     *  pong. */
    public int getDailyUptime() {
        if (dailyUptime<0)
            return DEFAULT_DAILY_UPTIME;   //don't know
        else
            return dailyUptime;
    }
    
    /**
     * a setter for the daily uptime.
     */
    public void setDailyUptime(int uptime) {
    	dailyUptime = uptime;
    }

    /** Records that we just successfully connected to this. */
    public void recordConnectionSuccess() {
        recordConnectionAttempt(connectSuccesses, now());
    }

    /** Records that we just failed to connect to this. */
    public void recordConnectionFailure() {
        recordConnectionAttempt(connectFailures, now());
    }
    
    /** Returns the last few times we successfully connected to this.
     *  @return an Iterator of system times in milliseconds, each as
     *   a Long, in descending order. */
    public Iterator /* Long */ getConnectionSuccesses() {
        return connectSuccesses.iterator();
    }

    /** Returns the last few times we successfully connected to this.
     *  @return an Iterator of system times in milliseconds, each as
     *   a Long, in descending order. */
    public Iterator /* Long */ getConnectionFailures() {
        return connectFailures.iterator();
    }

    /**
     * accessor for the locale of this endpoint
     */
    public String getClientLocale() {
        return _clientLocale;
    }

    /**
     * set the locale
     */
    public void setClientLocale(String l) {
        _clientLocale = l;
    }
    
    /**
     * Determines if this is an ExtendedEndpoint for a UDP Host Cache.
     */
    public boolean isUDPHostCache() {
        return udpHostCacheFailures != -1;
    }
    
    /**
     * Records a UDP Host Cache failure.
     */
    public void recordUDPHostCacheFailure() {
        Assert.that(isUDPHostCache());
        udpHostCacheFailures++;
    }
    
    /**
     * Decrements the failures for this UDP Host Cache.
     *
     * This is intended for use when the network has died and
     * we really don't want to consider the host a failure.
     */
    public void decrementUDPHostCacheFailure() {
        Assert.that(isUDPHostCache());
        // don't go below 0.
        udpHostCacheFailures = Math.max(0, udpHostCacheFailures-1);
    }
    
    /**
     * Records a UDP Host Cache success.
     */
    public void recordUDPHostCacheSuccess() {
        Assert.that(isUDPHostCache());
        udpHostCacheFailures = 0;
    }
    
    /**
     * Determines how many failures this UDP host cache had.
     */
    public int getUDPHostCacheFailures() {
        return udpHostCacheFailures;
    }
    
    /**
     * Sets if this a UDP host cache endpoint.
     */
    public ExtendedEndpoint setUDPHostCache(boolean cache) {
        if(cache == true)
            udpHostCacheFailures = 0;
        else
            udpHostCacheFailures = -1;
        return this;
    }

    private void recordConnectionAttempt(Buffer buf, long now) {
        if (buf.isEmpty()) {
            //a) No attempts; just add it.
            buf.addFirst(new Long(now));
        } else if (now - ((Long)buf.first()).longValue() >= WINDOW_TIME) {
            //b) Attempt more than WINDOW_TIME milliseconds ago.  Add.
            buf.addFirst(new Long(now));
        } else {
            //c) Attempt within WINDOW_TIME.  Coalesce.
            buf.removeFirst();
            buf.addFirst(new Long(now));
        }
    }

    /** Returns the current system time in milliseconds.  Exists solely
     *  as a hook for testing. */
    protected long now() {
        return System.currentTimeMillis();
    }


    ///////////////////////// Reading and Writing ///////////////////////
    
    /** The separator for list elements (connection successes) */
    private static final String LIST_SEPARATOR=";";
    /** The separator for fields in the gnutella.net file. */
    private static final String FIELD_SEPARATOR=",";
    /** We've always used "\n" for the record separator in our gnutella.net
     *  files, even on systems that normally use "\r\n" for end-of-line.  This
     *  has the nice advantage of making gnutella.net files portable across
     *  platforms. */
    public static final String EOL="\n";

    /**
     * Writes this' state to a single line of out.  Does not flush out.
     * @exception IOException some problem writing to out 
     * @see read
     */
    public void write(Writer out) throws IOException {
        out.write(getAddress());
        out.write(":");
        out.write(getPort() + "");
        out.write(FIELD_SEPARATOR);
        
        if (dailyUptime>=0)
            out.write(dailyUptime + "");
        out.write(FIELD_SEPARATOR);

        if (timeRecorded>=0)
            out.write(timeRecorded + "");
        out.write(FIELD_SEPARATOR);

        write(out, getConnectionSuccesses());
        out.write(FIELD_SEPARATOR);
        write(out, getConnectionFailures());
        out.write(FIELD_SEPARATOR);
        out.write(_clientLocale);
        out.write(FIELD_SEPARATOR);
        if(isUDPHostCache())
            out.write(udpHostCacheFailures + "");
        out.write(EOL);
    }

    /** Writes Objects to 'out'. */
    private void write(Writer out, Iterator objects) 
                       throws IOException {
        while (objects.hasNext()) {
            out.write(objects.next().toString());
            if (objects.hasNext())
                out.write(LIST_SEPARATOR);            
        }
    }

    /**
     * Parses a new ExtendedEndpoint.  Strictly validates all data.  For
     * example, addresses MUST be in dotted quad format.
     *
     * @param line a single line read from the stream
     * @return the endpoint constructed from the line
     * @exception IOException problem reading from in, e.g., EOF reached
     *  prematurely
     * @exception ParseException data not in proper format.  Does NOT 
     *  necessarily set the offset of the exception properly.
     * @see write
     */
    public static ExtendedEndpoint read(String line) throws ParseException {
        //Break the line into fields.  Skip if badly formatted.  Note that
        //subsequent delimiters are NOT coalesced.
        String[] linea=StringUtils.splitNoCoalesce(line, FIELD_SEPARATOR);
        if (linea.length==0)
            throw new ParseException("Empty line", 0);

        //1. Host and port.  As a dirty trick, we use existing code in Endpoint.
        //Note that we strictly validate the address to work around corrupted
        //gnutella.net files from an earlier version
        boolean pureNumeric;
        
        String host;
        int port;
        try {
            Endpoint tmp=new Endpoint(linea[0], true); // require numeric.
            host=tmp.getAddress();
            port=tmp.getPort();
            pureNumeric = true;
        } catch (IllegalArgumentException e) {
            // Alright, pure numeric failed -- let's try constructing without
            // numeric & without requiring a DNS lookup.
            try {
                Endpoint tmp = new Endpoint(linea[0], false, false);
                host = tmp.getAddress();
                port = tmp.getPort();
                pureNumeric = false;
            } catch(IllegalArgumentException e2) {
                ParseException e3 = new ParseException("Couldn't extract address and port from: " + linea[0], 0);
                e3.initCause(e2);
                throw e3;
            }
        }

        //Build endpoint without any optional data.  (We'll set it if possible
        //later.)
        ExtendedEndpoint ret=new ExtendedEndpoint(host, port, false);                

        //2. Average uptime (optional)
        if (linea.length>=2) {
            try {
                ret.dailyUptime=Integer.parseInt(linea[1].trim());
            } catch (NumberFormatException e) { }
        }
            
        //3. Time of pong (optional).  Do NOT use current system tome
        //   if not set.
        ret.timeRecorded=DEFAULT_TIME_RECORDED;
        if (linea.length>=3) {
            try {
                ret.timeRecorded=Long.parseLong(linea[2].trim());
            } catch (NumberFormatException e) { }
        }

        //4. Time of successful connects (optional)
        if (linea.length>=4) {
            try {
                String times[]=StringUtils.split(linea[3], LIST_SEPARATOR);
                for (int i=times.length-1; i>=0; i--)
                    ret.recordConnectionAttempt(ret.connectSuccesses,
                                                Long.parseLong(times[i].trim()));
            } catch (NumberFormatException e) { }
        }

        //5. Time of failed connects (optional)
        if (linea.length>=5) {
            try {
                String times[]=StringUtils.split(linea[4], LIST_SEPARATOR);
                for (int i=times.length-1; i>=0; i--)
                    ret.recordConnectionAttempt(ret.connectFailures,
                                                Long.parseLong(times[i].trim()));
            } catch (NumberFormatException e) { }
        }

        //6. locale of the connection (optional)
        if(linea.length>=6) {
            ret.setClientLocale(linea[5]);
        }
        
        //7. udp-host
        if(linea.length>=7) {
            try {
                int i = Integer.parseInt(linea[6]);
                if(i >= 0)
                    ret.udpHostCacheFailures = i;
            } catch(NumberFormatException nfe) {}
        }
        
        // validate address if numeric.
        if(pureNumeric && !NetworkUtils.isValidAddress(host))
            throw new ParseException("invalid dotted addr: " + ret, 0);        
            
        // validate that non UHC addresses were numeric.
        if(!ret.isUDPHostCache() && !pureNumeric)
            throw new ParseException("illegal non-UHC endpoint: " + ret, 0);

        return ret;
    }


    ////////////////////////////// Other /////////////////////////////

    /**
     * Returns a Comparator that compares ExtendedEndpoint's by priority, where
     * ExtendedEndpoint's with higher priority are more likely to be
     * available.  Currently this is implemented as follows, though the
     * heuristic may change in the future:
     * <ul>
     * <li>Whether the last connection attempt was a success (good), no
     *     connections have been attempted yet (ok), or the last connection 
     *     attempt was a failure (bad) 
     * <li>Average daily uptime (higher is better)
     * </ul>
     */
    public static Comparator priorityComparator() {
        return PRIORITY_COMPARATOR;
    }
    
    /**
     * The sole priority comparator.
     */
    private static final Comparator PRIORITY_COMPARATOR = new PriorityComparator();

    static class PriorityComparator implements Comparator {
        public int compare(Object extEndpoint1, Object extEndpoint2) {
            ExtendedEndpoint a=(ExtendedEndpoint)extEndpoint1;
            ExtendedEndpoint b=(ExtendedEndpoint)extEndpoint2;

            int ret=a.connectScore()-b.connectScore();
            if(ret != 0) 
                return ret;
     
            ret = a.localeScore() - b.localeScore();
            if(ret != 0)
                return ret;
                
            return a.getDailyUptime() - b.getDailyUptime();
        }
    }
    
    /**
     * Returns +1 if their locale matches our, -1 otherwise.
     * Returns 0 if locale preferencing isn't enabled.
     */
    private int localeScore() {
        if(!ConnectionSettings.USE_LOCALE_PREF.getValue())
            return 0;
        if(ownLocale.equals(_clientLocale))
            return 1;
        else
            return -1;
    }
    
    /** Returns +1 (last connection attempt was a success), 0 (no connection
     *  attempts), or -1 (last connection attempt was a failure). */
    private int connectScore() {
        if (connectSuccesses.isEmpty() && connectFailures.isEmpty())
            return 0;   //no attempts
        else if (connectSuccesses.isEmpty())
            return -1;  //only failures
        else if (connectFailures.isEmpty())
            return 1;   //only successes
        else {            
            long success=((Long)connectSuccesses.last()).longValue();
            long failure=((Long)connectFailures.last()).longValue();
            //Can't use success-failure because of overflow/underflow.
            if (success>failure)
                return 1;
            else if (success<failure)
                return -1;
            else 
                return 0;
        }
    }

    public boolean equals(Object other) {
        return super.equals(other);
        //TODO: implement
    }
}
