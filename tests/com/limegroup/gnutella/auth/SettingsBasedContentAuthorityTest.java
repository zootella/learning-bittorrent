package com.limegroup.gnutella.auth;

import java.util.Random;

import com.limegroup.gnutella.messages.Message;
import com.limegroup.gnutella.messages.PingRequest;
import com.limegroup.gnutella.settings.ContentSettings;
import com.limegroup.gnutella.util.BaseTestCase;
import com.limegroup.gnutella.util.IpPort;

import junit.framework.Test;
 
public class SettingsBasedContentAuthorityTest extends BaseTestCase {
    
    public SettingsBasedContentAuthorityTest(String name) {
        super(name);
    }

    public static Test suite() {
        return buildTestSuite(SettingsBasedContentAuthorityTest.class);
    }
    
    public void testInitialize() {
        ContentSettings.AUTHORITIES.setValue(new String[] { "yahoo.com", "google.com:82", "askjeeves.com:6346" });
        SettingsBasedContentAuthority auth = new SettingsBasedContentAuthority();
        auth.initialize();
        ContentAuthority[] all = auth.getAuthorities();
        assertEquals(3, all.length);
        
        IpPort one   = ((IpPortContentAuthority)all[0]).getIpPort();
        assertEquals("yahoo.com", one.getAddress());
        assertEquals(80, one.getPort());
        
        IpPort two   = ((IpPortContentAuthority)all[1]).getIpPort();
        assertEquals("google.com", two.getAddress());
        assertEquals(82, two.getPort());
        
        IpPort three = ((IpPortContentAuthority)all[2]).getIpPort();
        assertEquals("askjeeves.com", three.getAddress());
        assertEquals(6346, three.getPort());      
    }
    
    public void testInitializeDNSFails() {
        ContentSettings.AUTHORITIES.setValue(new String[] { "notarealdnsnamesodonteventryit.com", "google.com" });
        SettingsBasedContentAuthority auth = new SettingsBasedContentAuthority();
        auth.initialize();
        ContentAuthority[] all = auth.getAuthorities();
        assertEquals(1, all.length);
        
        IpPort one   = ((IpPortContentAuthority)all[0]).getIpPort();
        assertEquals("google.com", one.getAddress());
        assertEquals(80, one.getPort());           
    }
    
    public void testRandomSending() throws Exception {
        final int[] randoms = new int[] { 0, 2, 1, 0, 1, 0, 0, 1, 2, 2 };
        
        SettingsBasedContentAuthority auth = new SettingsBasedContentAuthority() {
            public Random newRandom() {
                return new StubRandom(randoms);
            }
        };
        StubContentAuthority zero = new StubContentAuthority();
        StubContentAuthority one  = new StubContentAuthority();
        StubContentAuthority two  = new StubContentAuthority();
        auth.setAuthorities(new ContentAuthority[] { zero, one, two } );

        Message[] ms = new Message[randoms.length];
        for(int i = 0; i < randoms.length; i++) {
            ms[i] = new PingRequest((byte)1);
            auth.send(ms[i]);
        }
        
        assertSame(zero.getSent().get(0), ms[0]);
        assertSame( two.getSent().get(0), ms[1]);
        assertSame( one.getSent().get(0), ms[2]);
        assertSame(zero.getSent().get(1), ms[3]);
        assertSame( one.getSent().get(1), ms[4]);
        assertSame(zero.getSent().get(2), ms[5]);
        assertSame(zero.getSent().get(3), ms[6]);
        assertSame( one.getSent().get(2), ms[7]);
        assertSame( two.getSent().get(1), ms[8]);
        assertSame( two.getSent().get(2), ms[9]);
    }
    
    private static class StubRandom extends Random {
        private int next = 0;
        private int[] rnds;
        
        StubRandom(int[] rnds) {
            this.rnds = rnds;
        }
        
        public int nextInt(int i) {
            return rnds[next++];
        }
    }
    
}
