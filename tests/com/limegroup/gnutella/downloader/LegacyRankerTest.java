package com.limegroup.gnutella.downloader;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import junit.framework.Test;

import com.limegroup.gnutella.RemoteFileDesc;
import com.limegroup.gnutella.URN;
import com.limegroup.gnutella.util.BaseTestCase;
import com.limegroup.gnutella.util.IpPortImpl;

/**
 * Tests that the legacy ranker is properly selecting which 
 * RFDs it should return.
 */
public class LegacyRankerTest extends BaseTestCase {

    public LegacyRankerTest(String name) {
        super(name);
    }
    
    public static Test suite() {
        return buildTestSuite(LegacyRankerTest.class);
    }
    
    static SourceRanker ranker;
    
    public void setUp() throws Exception {
        ranker = new LegacyRanker();
    }
    
    /**
     * Tests that rfds that have urns are prefered to rfds without.
     */
    public void testPrefersRFDWithURN() throws Exception {
        ranker.addToPool(newRFD("1.2.3.4",3));
        ranker.addToPool(newRFDWithURN("1.2.3.4",3));
        
        RemoteFileDesc selected = (RemoteFileDesc) ranker.getBest();
        assertNotNull(selected.getSHA1Urn());
    }
    
    /**
     * tests that the ranker exhausts the list of rfds to try
     */
    public void testExhaustsRFDs() throws Exception {
        ranker.addToPool(newRFD("1.2.3.4",3));
        ranker.addToPool(newRFDWithURN("1.2.3.4",3));
        
        assertTrue(ranker.hasMore());
        ranker.getBest();
        ranker.getBest();
        assertFalse(ranker.hasMore());
    }
    
    /**
     * tests that the ranker does not allow duplicate rfds
     */
    public void testDisallowsDuplicates() throws Exception {
        RemoteFileDesc rfd1, rfd2;
        rfd1 = newRFDWithURN("1.2.3.4",3);
        rfd2 = newRFDWithURN("1.2.3.4",3);
        assertTrue(rfd1.equals(rfd2));
        assertTrue(rfd1.hashCode() == rfd2.hashCode());
        ranker.addToPool(rfd1);
        ranker.addToPool(rfd2);
        
        assertTrue(ranker.hasMore());
        ranker.getBest();
        assertFalse(ranker.hasMore());
    }
    
    public void testGetShareable() throws Exception {
        RemoteFileDesc rfd1, rfd2;
        rfd1 = newRFD("1.2.3.4",3);
        rfd2 = newRFDWithURN("1.2.3.4",3);
        ranker.addToPool(rfd1);
        ranker.addToPool(rfd2);
        
        Collection c = ranker.getShareableHosts();
        assertTrue(c.contains(rfd1));
        assertTrue(c.contains(rfd2));
        assertEquals(2,c.size());
    }
    
    // TODO: add more tests, although this ranker will be used rarely. 
    
    private static RemoteFileDesc newRFD(String host, int speed){
        return new RemoteFileDesc(host, 1,
                                  0, "asdf",
                                  TestFile.length(), new byte[16],
                                  speed, false, 4, false, null, null,
                                  false,false,"",0,null, -1);
    }

    private static RemoteFileDesc newRFDWithURN() {
        return newRFDWithURN();
    }

    private static RemoteFileDesc newRFDWithURN(String host, int speed) {
        Set set = new HashSet();
        try {
            // for convenience, don't require that they pass the urn.
            // assume a null one is the TestFile's hash.
            set.add(TestFile.hash());
        } catch(Exception e) {
            fail("SHA1 not created");
        }
        return new RemoteFileDesc(host, 1,
                                  0, "asdf",
                                  TestFile.length(), new byte[16],
                                  speed, false, 4, false, null, set,
                                  false, false,"",0,null, -1);
    }

}
