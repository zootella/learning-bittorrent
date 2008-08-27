package com.limegroup.gnutella.gui.search;

import com.limegroup.gnutella.gui.GUIUtils;


/**
 * The ResultName displayed in a search result line. These values are rendered
 * by ResultNameRenderer.
 */
class ResultNameHolder implements Comparable {
    private TableLine line;   
    private String description;

    public ResultNameHolder(TableLine line) {
        this.description = line.getFilenameNoExtension();
		this.line = line; 
    }

    /** A textual description of this speed, e.g., 'Modem'. */
    public String stringValue() {
        return description;
    }

    /** Returns the spam rating */
    public float getSpamRating() {
        return line.getSpamRating();
    }
    
    /**
     * Returns true if the two ResultNameHolders are exactly the same
     */
    public boolean equals(Object other) {
        if(other instanceof ResultNameHolder) {
            ResultNameHolder o = (ResultNameHolder)other;
            return o.description.equals(description);
        } else {
            return false;
        }
    }    

    /**
     * compare by by description string
     */
    public int compareTo(Object other) {
        ResultNameHolder o = (ResultNameHolder)other;
        return this.description.compareTo(o.description);
    }
	
	public String toString() {
		return stringValue();
	}
}    
