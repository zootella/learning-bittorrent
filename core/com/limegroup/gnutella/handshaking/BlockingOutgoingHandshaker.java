package com.limegroup.gnutella.handshaking;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Properties;

import com.limegroup.gnutella.Constants;
import com.limegroup.gnutella.statistics.HandshakingStat;

/**
 * An outgoing handshaker that blocks while handshaking.
 */
public class BlockingOutgoingHandshaker implements Handshaker {
    
    private BlockingHandshakeSupport support;
    private Properties ourRequestHeaders;
    private HandshakeResponder ourResponder;

    /**
     * Constructs a new BlockingOutgoingHandshaker using the given
     * requestHeaders for the initial request, responsder to handle our response,
     * and Socket/InputStream/OutputStream for I/O.
     */ 
    public BlockingOutgoingHandshaker(Properties requestHeaders, HandshakeResponder responder,
                                      Socket socket, InputStream in, OutputStream out) {
        this.support = new BlockingHandshakeSupport(socket, in, out);
        this.ourRequestHeaders = requestHeaders;
        this.ourResponder = responder;
    }
    
    /** Returns a HandshakeResponse containing all the headers we read while handshaking. */
    public HandshakeResponse getReadHeaders() {
        return support.getReadHandshakeResponse();
    }

    /** Returns a HandshakeResponse containing all the headers we wrote while handshaking. */
    public HandshakeResponse getWrittenHeaders() {
        return support.getWrittenHandshakeResponse();
    }

    /** Performs the handshake.*/
    public void shake() throws IOException, BadHandshakeException, NoGnutellaOkException {
        initializeOutgoing();
        concludeOutgoingHandshake();
    }

    /** 
     * Sends and receives handshake strings for outgoing connections,
     * throwing exception if any problems. 
     * 
     * @exception NoGnutellaOkException one of the participants responded
     *  with an error code other than 200 OK (possibly after several rounds
     *  of 401's)
     * @exception IOException any other error.  
     */
    private void initializeOutgoing() throws IOException {
        support.writeConnectLine();
        support.sendHeaders(ourRequestHeaders);
    }
    
    /**
     * Responds to the responses/challenges from the host on the other
     * end of the connection, till a conclusion reaches. Handshaking may
     * involve multiple steps. 
     *
     * @exception NoGnutellaOkException one of the participants responded
     *              with an error code other than 200 OK
     * @exception IOException any other error.  
     */
    private void concludeOutgoingHandshake() throws IOException {
        String connectLine = support.readLine();
        if (!support.isConnectLineValid(connectLine)) {
            HandshakingStat.OUTGOING_BAD_CONNECT.incrementStat();
            throw new IOException("Bad connect string");
        }
        
        support.readHeaders(Constants.TIMEOUT);

        //Terminate abnormally if we read something other than 200 or 401.
        HandshakeResponse theirResponse = support.createRemoteResponse(connectLine);

        switch(theirResponse.getStatusCode()) {
        case HandshakeResponse.OK:
            break;
        case HandshakeResponse.SLOTS_FULL:
            handleSlotsFullStats(theirResponse);
            throw NoGnutellaOkException.SERVER_REJECT;
        default: 
            HandshakingStat.OUTGOING_SERVER_UNKNOWN.incrementStat();
            throw NoGnutellaOkException.createServerUnknown(theirResponse.getStatusCode());
        }

        HandshakeResponse ourResponse = ourResponder.respond(theirResponse, true);
        support.writeResponse(ourResponse);

        switch(ourResponse.getStatusCode()) {
        case HandshakeResponse.OK:
            HandshakingStat.SUCCESSFUL_OUTGOING.incrementStat();
            break;
        case HandshakeResponse.SLOTS_FULL:
            HandshakingStat.OUTGOING_CLIENT_REJECT.incrementStat();
            throw NoGnutellaOkException.CLIENT_REJECT;
        case HandshakeResponse.LOCALE_NO_MATCH:
            //if responder's locale preferencing was set 
            //and didn't match the locale this code is used.
            //(currently in use by the dedicated connectionfetcher)
            throw NoGnutellaOkException.CLIENT_REJECT_LOCALE;
        default: 
            HandshakingStat.OUTGOING_CLIENT_UNKNOWN.incrementStat();
            throw NoGnutellaOkException.createClientUnknown(ourResponse.getStatusCode());
        }
    }
    
    /** Increments various HandshakingStats because of a slots full response. */
    private void handleSlotsFullStats(HandshakeResponse theirResponse) {
        if (theirResponse.isLimeWire()) {
            if (theirResponse.isUltrapeer())
                HandshakingStat.OUTGOING_LIMEWIRE_ULTRAPEER_REJECT.incrementStat();
            else
                HandshakingStat.OUTGOING_LIMEWIRE_LEAF_REJECT.incrementStat();
        } else {
            if (theirResponse.isUltrapeer())
                HandshakingStat.OUTGOING_OTHER_ULTRAPEER_REJECT.incrementStat();
            else
                HandshakingStat.OUTGOING_OTHER_LEAF_REJECT.incrementStat();
        }
    }
}
