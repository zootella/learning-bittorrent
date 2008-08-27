package com.limegroup.gnutella.gui.options;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;

import com.limegroup.gnutella.gui.GUIMediator;

public class ConfigureOptionsAction extends AbstractAction {

	/**
	 * Resource key to go to in the options window
	 */
	private String paneTitle;
    
    public ConfigureOptionsAction(String pane) {
        paneTitle = pane;
    }
	
	public ConfigureOptionsAction(String pane, String menu, String tooltip) {
        this(pane);
        putValue(Action.NAME, GUIMediator.getStringResource(menu));
        putValue(Action.SHORT_DESCRIPTION, GUIMediator.getStringResource(tooltip));
    }

	/**
	 * Launches LimeWire's options with the given options pane selected.
	 */
	public void actionPerformed(ActionEvent e) {
		GUIMediator.instance().setOptionsVisible(true, paneTitle);
	}
}
