package com.limegroup.gnutella.gui.menu;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import com.limegroup.gnutella.gui.GUIMediator;

/**
 * Listeners that opens a link.
 */
class LinkListener implements ActionListener {
    private final String link;
    public LinkListener(String link) {
        this.link = link;
    }
    
	public void actionPerformed(ActionEvent e) {
		GUIMediator.openURL(link);
	}
}