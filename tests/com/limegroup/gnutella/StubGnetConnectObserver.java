package com.limegroup.gnutella;

import com.limegroup.gnutella.connection.GnetConnectObserver;


public class StubGnetConnectObserver implements GnetConnectObserver {
    private boolean noGOK;
    private int code;
    private String msg;
    private boolean badHandshake;
    private boolean connect;
    private boolean shutdown;
    private Thread finishedThread;
    
    public synchronized void handleNoGnutellaOk(int code, String msg) {
        this.noGOK = true;
        this.code = code;
        this.msg = msg;
        this.finishedThread = Thread.currentThread();
        notify();
    }

    public synchronized void handleBadHandshake() {
        this.badHandshake = true;
        this.finishedThread = Thread.currentThread();
        notify();
    }

    public synchronized void handleConnect() {
        this.connect = true;
        this.finishedThread = Thread.currentThread();        
        notify();
    }

    public synchronized void shutdown() {
        this.shutdown = true;
        this.finishedThread = Thread.currentThread();
        notify();
    }
    
    public synchronized void waitForResponse(long time) throws Exception {
        wait(time);
    }

    public boolean isBadHandshake() {
        return badHandshake;
    }

    public int getCode() {
        return code;
    }

    public boolean isConnect() {
        return connect;
    }

    public String getMsg() {
        return msg;
    }

    public boolean isNoGOK() {
        return noGOK;
    }

    public boolean isShutdown() {
        return shutdown;
    }

    public Thread getFinishedThread() {
        return finishedThread;
    }

}
