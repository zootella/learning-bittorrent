package com.limegroup.gnutella.filters;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.limegroup.gnutella.Response;
import com.limegroup.gnutella.messages.BadPacketException;
import com.limegroup.gnutella.messages.Message;
import com.limegroup.gnutella.messages.QueryReply;
import com.limegroup.gnutella.messages.QueryRequest;

/** 
 * A spam filter that removes certain "bad" keywords. 
 * If <i>any</i> words in a query are in the banned set, the
 * query is disallowed.
 */
public class KeywordFilter extends SpamFilter {
    /** INVARIANT: strings in ban contain only lowercase */
    private List /* of String */ ban=new ArrayList();

    /** 
     * @modifies this
     * @effects bans the given phrase.  Capitalization does not matter.
     */
    public void disallow(String phrase) { 
        String canonical = phrase.toLowerCase();
        if (!ban.contains(canonical))
            ban.add(canonical);
    }

    /** 
     * @modifies this
     * @effects bans several well-known "adult" words.
     */
    public void disallowAdult() {
        disallow("anal");
        disallow("anul");
        disallow("asshole");
        disallow("blow");
        disallow("blowjob");
        disallow("bondage");
        disallow("centerfold");
        disallow("cock");
        disallow("cum");
        disallow("cunt");
        disallow("facial");
        disallow("fuck");
        disallow("gangbang");
        disallow("hentai");
        disallow("incest");
        disallow("jenna");
        disallow("masturbat");
        disallow("nipple");
        disallow("penis");
        disallow("playboy");
        disallow("porn");
        disallow("pussy");
        disallow("rape");
        disallow("sex");
        disallow("slut");
        disallow("suck");
        disallow("tittie");
        disallow("titty");
        disallow("twat");
        disallow("vagina");
        disallow("whore");
        disallow("xxx");
    }

    /**
     * @modifies this
     * @effects bans .vbs files
     */
    public void disallowVbs() {
        disallow(".vbs");
    }

    /**
     * @modifies this
     * @effects bans .htm and html files
     */
    public void disallowHtml() {
        disallow(".htm");
    }
    
    /**
     * bans .wmv and .asf files
     */
    public void disallowWMVASF() {
    	disallow(".asf");
	disallow(".asx");
    	disallow(".wmv");
    }

    public boolean allow(Message m) {
        boolean ok=true;
        if (m instanceof QueryRequest) 
            return allow((QueryRequest)m);
        else if (m instanceof QueryReply)
            return allow((QueryReply)m);
        else
            return ok;
    }

    protected boolean allow(QueryRequest qr) {
        //return false iff any of the words in query are in ban
        String query=qr.getQuery();
        return ! matches(query);
    }

    boolean allow(QueryReply qr) {
        //if any of the file names in qr contain bad words, the whole
        //thing is disallowed
        try {
            for (Iterator iter=qr.getResults(); iter.hasNext(); ) {
                Response response=(Response)iter.next();
                if (matches(response.getName()))
                    return false;
            }
        } catch (BadPacketException e) {
            return false;
        }
        return true;
    }

    /** 
     * Returns true if phrase matches any of the entries in ban.
     */
    protected boolean matches(String phrase) {
        String canonical=phrase.toLowerCase();
        for (int i=0; i<ban.size(); i++) {
            String badWord=(String)ban.get(i);
            //phrase contains badWord?
            //Hopefully indexOf uses some reasonably efficient 
            //algorithm, such as Knuth-Morris-Pratt.
            if (canonical.indexOf(badWord)!=-1)
                return true;
        }
        return false;
    }
}
