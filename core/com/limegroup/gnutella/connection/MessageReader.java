package com.limegroup.gnutella.connection;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import com.limegroup.gnutella.io.ChannelReadObserver;
import com.limegroup.gnutella.io.InterestReadChannel;
import com.limegroup.gnutella.messages.BadPacketException;
import com.limegroup.gnutella.messages.Message;

/**
 * Reads messages from a channel.  This class is notified when more of a message
 * can potentially be read by its handleRead() method being called.  To change
 * the channel this reads from, use setReaderChannel(ReadableByteChannel).
 *
 * It is possible to construct this class without an initial source channel.
 * However, before handleRead is called, the channel must be set.
 *
 * The first time the channel returns -1 this will throw an IOException, as it
 * never expects the channel to run out of data.  Upon each read notification,
 * as much data as possible will be read from the source channel.
 */
public class MessageReader implements ChannelReadObserver {
    
    /** the maximum size of a message payload that we'll accept */
    private static final long MAX_MESSAGE_SIZE = 64 * 1024;
    /** the size of the header */
    private static final int HEADER_SIZE = 23;
    /** where in the header the payload is */
    private static final int PAYLOAD_LENGTH_OFFSET = 19;
    
    /** the constant buffer to use for emtpy payloads. */
    private static final ByteBuffer EMPTY_PAYLOAD = ByteBuffer.allocate(0);
    
    /** the sole buffer for parsing msg headers */
    private final ByteBuffer header;
    /** the buffer used for parsing the payload -- recreated for each message */
    private ByteBuffer payload;
    
    /** the sole receiver of messages */
    private final MessageReceiver receiver;
    /** the source channel */
    private InterestReadChannel channel;
    
    /** whether or not this reader has been shut down yet. */
    private boolean shutdown = false;
    
    /**
     * Constructs a new MessageReader without an underlying source.
     * Prior to handleRead() being called, setReadChannel(ReadableByteChannel)
     * MUST be called.
     */    
    public MessageReader(MessageReceiver receiver) {
        this(null, receiver);
    }
    
    /**
     * Constructs a new MessageReader with the given source channel & receiver.
     */
    public MessageReader(InterestReadChannel channel, MessageReceiver receiver) {
        if(receiver == null)
            throw new NullPointerException("null receiver");
            
        this.channel = channel;
        this.receiver = receiver;
        this.header = ByteBuffer.allocate(HEADER_SIZE);
        header.order(ByteOrder.LITTLE_ENDIAN);
        this.payload = null;
    }
    
    /**
     * Sets the new channel to be reading from.
     */
    public void setReadChannel(InterestReadChannel channel) {
        if(channel == null)
            throw new NullPointerException("cannot set null channel!");
        
        this.channel = channel;
    }
    
    /**
     * Gets the channel that is used for reading.
     */
    public InterestReadChannel getReadChannel() {
        return channel;
    }
    
    /**
     * Notification that a read can be performed from the given channel.
     * All messages that can be read without blocking are read & dispatched.
     */
    public void handleRead() throws IOException {
        // Continue reading until we can't fill up the header or payload.
        while(true) {
            int read = 0;
            
            // First try to fill up the header.
            while(header.hasRemaining() && (read = channel.read(header)) > 0);
            
            // If there header's not full, we can't bother reading the payload, so abort.
            if(header.hasRemaining()) {
                if(read == -1)
                    throw new IOException("EOF");
                break;
            }
                
            // if we haven't set up a payload yet, set one up (if necessary).
            if(payload == null) {
                int payloadLength = header.getInt(PAYLOAD_LENGTH_OFFSET);
                
                if(payloadLength < 0 || payloadLength > MAX_MESSAGE_SIZE)
                    throw new IOException("should i implement skipping?");
                
                if(payloadLength == 0) {
                    payload = EMPTY_PAYLOAD;
                } else {
                    try {
                        payload = ByteBuffer.allocate(payloadLength);
                    } catch(OutOfMemoryError oome) {
                        throw new IOException("message too large.");
                    }
                }
            }
            
            // Okay, a payload is set up, let's read into it.
            while(payload.hasRemaining() && (read = channel.read(payload)) > 0);
            
            // If the payload's not full, we can't create a message, so abort.
            if(payload.hasRemaining()) {
                if(read == -1)
                    throw new IOException("eof");
                break;
            }
                
            // Yay, we've got a full message.
            try {
                Message m = Message.createMessage(header.array(), payload.array(), 
                                                  receiver.getSoftMax(), receiver.getNetwork());
                receiver.processReadMessage(m);
            } catch(BadPacketException ignored) {}
            
            if(read == -1)
                throw new IOException("eof");
            
            payload = null;
            header.clear();
        }
    }
    
    /** 
     * Informs the receiver that the message is shutdown.
     */
    public void shutdown() {
        synchronized(this) {
            if(shutdown)
                return;
                
            shutdown = true;
        }
        receiver.messagingClosed();
    }
    
    /** Unused */
    public void handleIOException(IOException iox) {
        throw new RuntimeException("unsupported operation", iox);
    }
    
}
    
    