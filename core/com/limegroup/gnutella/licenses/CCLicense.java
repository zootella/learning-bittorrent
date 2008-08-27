package com.limegroup.gnutella.licenses;

import java.io.IOException;
import java.io.Serializable;

import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;

import java.net.URL;
import java.net.MalformedURLException;

import org.apache.commons.httpclient.URI;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.NamedNodeMap;

import com.limegroup.gnutella.ErrorService;
import com.limegroup.gnutella.URN;

/**
 * A concrete implementation of a License, for Creative Commons licenses.
 */
class CCLicense extends AbstractLicense {
    
    private static final Log LOG = LogFactory.getLog(CCLicense.class);
    
    private static final long serialVersionUID = 8213994964631107858L;
    
    /** The license string. */
    private transient String license;
    
    /** The license information for each Work. */
    private Map /* URN -> Details */ allWorks;
    
    /**
     * Constructs a new CCLicense.
     */
    CCLicense(String license, URI uri) {
        super(uri);
        this.license = license;
    }
    
    public String getLicense() {
        return license;
    }
    
    /**
     * Retrieves the license deed for the given URN.
     */
    public URL getLicenseDeed(URN urn) {
        Details details = getDetails(urn);
        if(details == null || details.licenseURL == null)
            return guessLicenseDeed();
        else
            return details.licenseURL;
    }

    /**
     * Attempts to guess what the license URI is from the license text.
     */    
    private URL guessLicenseDeed() {
        return CCConstants.guessLicenseDeed(license);
    }
        
    /**
     * Determines if the CC License is valid with this URN.
     */
    public boolean isValid(URN urn) {
        return getDetails(urn) != null;
    }
    
    /**
     * Returns a CCLicense exactly like this, except
     * with a different license string.
     */
    public License copy(String license, URI licenseURI) {
        CCLicense newL = null;
        try {
            newL = (CCLicense)clone();
            newL.license = license;
            newL.licenseLocation = licenseURI;
        } catch(CloneNotSupportedException error) {
            ErrorService.error(error);
        }
        return newL;
    }
    
    /**
     * Builds a description of this license based on what is permitted,
     * probibited, and required.
     */
    public String getLicenseDescription(URN urn) {
        List permitted = Collections.EMPTY_LIST;
        List prohibited = Collections.EMPTY_LIST;
        List required = Collections.EMPTY_LIST;
        Details details = getDetails(urn);
        if(details != null) {
            permitted = details.permitted;
            prohibited = details.prohibited;
            required = details.required;
        }
        
        StringBuffer sb = new StringBuffer();
        if(permitted != null && !permitted.isEmpty()) {
            sb.append("Permitted: ");
            for(Iterator i = permitted.iterator(); i.hasNext(); ) {
                sb.append(i.next().toString());
                if(i.hasNext())
                    sb.append(", ");
            }
        }
        if(prohibited != null && !prohibited.isEmpty()) {
            if(sb.length() != 0)
                sb.append("\n");
            sb.append("Prohibited: ");
            for(Iterator i = prohibited.iterator(); i.hasNext(); ) {
                sb.append(i.next().toString());
                if(i.hasNext())
                    sb.append(", ");
            }
        }
        if(required != null && !required.isEmpty()) {
            if(sb.length() != 0)
                sb.append("\n");
            sb.append("Required: ");
            for(Iterator i = required.iterator(); i.hasNext(); ) {
                sb.append(i.next().toString());
                if(i.hasNext())
                    sb.append(", ");
            }
        }
        
        if(sb.length() == 0)
            sb.append("Permissions unknown.");
        
        return sb.toString();
    }
    
    /**
     * Erases all data associated with a verification.
     */
    protected void clear() {
        if(allWorks != null)
            allWorks.clear();
    }

    /**
     * Locates the RDF from the body of the URL.
     */
    protected String getBody(String url) {
        return locateRDF(super.getBody(url));
    }
    
    ///// WORK & DETAILS CODE ///
    
    
    /**
     * Adds the given a work with the appropriate details to allWorks.
     */
    private void addWork(URN urn, String licenseURL) {
        URL url = null;
        try {
            url = new URL(licenseURL);
        } catch(MalformedURLException murl) {
            LOG.warn("Unable to make licenseURL out of: " + licenseURL, murl);
        }
        
        //See if we can refocus an existing licenseURL.
        Details details = getDetails(urn);
        if(details != null) {
            if(LOG.isDebugEnabled())
                LOG.debug("Found existing details item for URN: " + urn);
            if(url != null) {
                URL guessed = guessLicenseDeed();
                if(guessed != null && guessed.equals(url)) {
                    if(LOG.isDebugEnabled())
                        LOG.debug("Updating license URL to be: " + url);
                    details.licenseURL = url;
                }
            }
                
            // Otherwise, not much else we can do.
            // We already have a Details for this URN and it has
            // a licenseURL already.
            return;
        }
        
        // There's no existing details for this item, so lets add one.
        details = new Details(url);
        if(LOG.isDebugEnabled())
            LOG.debug("Adding new " + details + " for urn: " + urn);

        if(allWorks == null)
            allWorks = new HashMap(1); // assume it's small.
        allWorks.put(urn, details); // it is fine if urn is null.
    }   
    
    /**
     * Locates a details for a given URN.
     */
    private Details getDetails(URN urn) {
        if(allWorks == null)
            return null;
        
        // First see if there's a details that matches exactly.
        Details details = (Details)allWorks.get(urn);
        if(details != null)
            return details;
            
        // Okay, nothing matched.
        
        // If we want a specific URN, we can only give back the 'null' one.
        if(urn != null)
            return (Details)allWorks.get(null);
        
        // We must have wanted the null one.  Give back the first one we find.
        return (Details)allWorks.values().iterator().next();
    }
    
    /**
     * Locates all details that use the given License URL.
     */
    private List getDetailsForLicenseURL(URL url) {
        if(allWorks == null || url == null)
            return Collections.EMPTY_LIST;
        
        List details = new LinkedList();
        for(Iterator i = allWorks.values().iterator(); i.hasNext(); ) {
            Details detail = (Details)i.next();
            if(detail.licenseURL != null && url.equals(detail.licenseURL))
                details.add(detail);
        }
        return details;
    }
    
    /**
     * A single details.
     */
    private static class Details implements Serializable {
        private static final long serialVersionUID =  -1719502030054241350L;
                
        URL licenseURL;
        List required;
        List permitted;
        List prohibited;
        
        // for de-serializing.
        Details() { }
        
        Details(URL url) {
            licenseURL = url;
        }
        
        boolean isDescriptionAvailable() {
            return required != null || permitted != null || prohibited != null;
        }
        
        public String toString() {
            return "details:: license: " + licenseURL;
        }
    }   
    
    ///// VERIFICATION CODE ///
    
    /**
     * Locates RDF from a big string of HTML.
     */
    private String locateRDF(String body) {
        if(body == null || body.trim().equals(""))
            return null;
        
        // look for two rdf:RDF's.
        int startRDF = body.indexOf("<rdf:RDF");
        if(startRDF >= body.length() - 1)
            return null;
            
        int endRDF = body.indexOf("rdf:RDF", startRDF+6);
        if(startRDF == -1 || endRDF == -1)
            return null;
        
        // find the closing tag.
        endRDF = body.indexOf('>', endRDF);
        if(endRDF == -1)
            return null;
        
        // Alright, we got where the rdf is at!
        return body.substring(startRDF, endRDF + 1);
    }   

    /**
     * Parses through the XML.  If this is live data, we look for works.
     * Otherwise (it isn't from the verifier), we only look for licenses.
     */
    protected void parseDocumentNode(Node doc, boolean liveData) {
        NodeList children = doc.getChildNodes();
        
        // Do a first pass for Work elements.
        if(liveData) {
            for(int i = 0; i < children.getLength(); i++) {
                Node child = children.item(i);
                if(child.getNodeName().equals("Work"))
                    parseWorkItem(child);
            }
        }
        
        // And a second pass for License elements.
        for(int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if(child.getNodeName().equals("License"))
                parseLicenseItem(child);
        }
        
        // If this was from the verifier, see if we need to get any more
        // license details.
        if(liveData)
            updateLicenseDetails();
            
        return;
    }
    
    /**
     * Parses the 'Work' item.
     */
    protected void parseWorkItem(Node work) {
        if(LOG.isTraceEnabled())
            LOG.trace("Parsing work item.");
         
        // Get the URN of this Work item.   
        NamedNodeMap attributes = work.getAttributes();
        Node about = attributes.getNamedItem("rdf:about");
        URN expectedURN = null;
        if(about != null) {
            // attempt to create a SHA1 urn out of it.
            try {
                expectedURN = URN.createSHA1Urn(about.getNodeValue());
            } catch(IOException ioe) {}
        }
        
        // Get the license child element.
        NodeList children = work.getChildNodes();
        for(int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if(child.getNodeName().equals("license")) {
                attributes = child.getAttributes();
                Node resource = attributes.getNamedItem("rdf:resource");
                // if we found a resource, attempt to add the Work.
                if(resource != null)
                    addWork(expectedURN, resource.getNodeValue());
            }
        }
        
        // other than it existing, nothing else needs to happen.
        return;
    }
    
    /**
     * Parses the 'license' item.
     */
    protected void parseLicenseItem(Node license) {
        if(LOG.isTraceEnabled())
            LOG.trace("Parsing license item.");
           
        // Get the license URL. 
        NamedNodeMap attributes = license.getAttributes();
        Node about = attributes.getNamedItem("rdf:about");
        List details = Collections.EMPTY_LIST;
        if(about != null) {
            String value = about.getNodeValue();
            try {
                details = getDetailsForLicenseURL(new URL(value));
            } catch(MalformedURLException murl) {
                LOG.warn("Unable to create license URL for: " + value, murl);
            }
        }
        
        // Optimization:  If no details, exit early.
        if(!details.iterator().hasNext())
            return;
        
        List required = null;
        List prohibited = null;
        List permitted = null;
        
        // Get the 'permit', 'requires', and 'prohibits' values.
        NodeList children = license.getChildNodes();
        for(int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            String name = child.getNodeName();
            if(name.equalsIgnoreCase("requires")) {
                if(required == null)
                    required = new LinkedList();
                addPermission(required, child);
            } else if(name.equalsIgnoreCase("permits")) {
                if(permitted == null)
                    permitted = new LinkedList();
                addPermission(permitted, child);
            } else if(name.equalsIgnoreCase("prohibits")) {
                if(prohibited == null)
                    prohibited = new LinkedList();
                addPermission(prohibited, child);
            }
        }
        
        // Okay, now iterate through each details and set the lists.
        for(Iterator i = details.iterator(); i.hasNext(); ) {
            Details detail = (Details)i.next();
            if(LOG.isDebugEnabled())
                LOG.debug("Setting license details for " + details);
            detail.required = required;
            detail.prohibited = prohibited;
            detail.permitted = permitted;
        }
        
        return;
    }
    
    /**
     * Adds a single permission to the list.
     */
    private void addPermission(List permissions, Node node) {
        NamedNodeMap attributes = node.getAttributes();
        Node resource = attributes.getNamedItem("rdf:resource");
        if(resource != null) {
            String value = resource.getNodeValue();
            int slash = value.lastIndexOf('/');
            if(slash != -1 && slash != value.length()-1) {
                String permission = value.substring(slash+1);
                if(!permissions.contains(permission)) {
                    permissions.add(permission);
                    if(LOG.isDebugEnabled())
                        LOG.debug("Added permission: " + permission);
                } else {
                    if(LOG.isWarnEnabled())
                        LOG.warn("Duplicate permission: " + permission + "!");
                }
            } else if (LOG.isWarnEnabled()) {
                LOG.trace("Unable to find permission name: " + value);
            }
        } else if(LOG.isWarnEnabled()) {
            LOG.warn("No resource item for permission.");
        } 
    }
    
    /**
     * Updates the license details, potentially retrieving information
     * from the licenseURL in each Details.
     */
    private void updateLicenseDetails() {
        if(allWorks == null)
            return;
        
        for(Iterator i = allWorks.values().iterator(); i.hasNext(); ) {
            Details details = (Details)i.next();
            if(!details.isDescriptionAvailable() && details.licenseURL != null) {
                if(LOG.isDebugEnabled())
                    LOG.debug("Updating licenseURL for :" + details);
                
                String url = details.licenseURL.toExternalForm();
                // First see if we have cached details.
                Object data = LicenseCache.instance().getData(url);
                String body = null;
                if(data != null && data instanceof String) {
                    if(LOG.isDebugEnabled())
                        LOG.debug("Using cached data for url: " + url);
                    body = locateRDF((String)data);
                } else {
                    body = getBody(url);
                    if(body != null)
                        LicenseCache.instance().addData(url, body);
                    else
                        LOG.debug("Couldn't retrieve license details from url: " + url);
                }
                
                // parsing MUST NOT alter allWorks,
                // otherwise a ConcurrentMod will happen
                if(body != null)
                    parseXML(body, false);
             }
        }
    }
}
