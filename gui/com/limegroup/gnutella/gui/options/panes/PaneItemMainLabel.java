package com.limegroup.gnutella.gui.options.panes;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FontMetrics;

import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JPanel;

import com.limegroup.gnutella.gui.BoxPanel;
import com.limegroup.gnutella.gui.GUIMediator;
import com.limegroup.gnutella.gui.MultiLineLabel;
import com.limegroup.gnutella.gui.URLLabel;

/**
 * This class uses the decorator pattern around a <tt>MultiLineLabel</tt>
 * that is customized for use in options panes.
 */
//2345678|012345678|012345678|012345678|012345678|012345678|012345678|012345678|
final class PaneItemMainLabel {
	
	/**
	 * Label component.
	 */
	private final JComponent LABEL;

	/**
	 * Constant for the width of labels.
	 */
	private final int LABEL_WIDTH = 415;

	/**
	 * This constructor creates the label object with the standard width
	 * and maximum size.
	 *
	 * @param str the string for the label
	 */
	PaneItemMainLabel(final String str) {
        this(str, null);
    }
    
    PaneItemMainLabel(String str, String url) {
        MultiLineLabel label = new MultiLineLabel(str, LABEL_WIDTH);
		FontMetrics fm = label.getFontMetrics(label.getFont());
		int height = fm.getHeight();
		height *= label.getLineCount();
        
        if(url == null) {
            LABEL = label;         
        } else {
            JPanel panel = new BoxPanel(BoxPanel.Y_AXIS);
            JPanel urlPanel = new BoxPanel(BoxPanel.X_AXIS);
            URLLabel urlLabel = new URLLabel(url, GUIMediator.getStringResource("OPTIONS_LEARN_MORE_LABEL"));
            urlPanel.add(urlLabel);
            urlPanel.add(Box.createHorizontalGlue());
            height += urlLabel.getPreferredSize().height;
            panel.add(label);
            panel.add(Box.createVerticalStrut(5));
            panel.add(urlPanel);
            panel.add(Box.createVerticalStrut(5));
            LABEL = panel;
        }
        
        // add separator pixels to the height
        height += 10;
        Dimension dim = new Dimension(500, height);
        LABEL.setPreferredSize(dim);
        LABEL.setMaximumSize(dim);        
	}

	/**
	 * Returns the <tt>Component</tt> containing the underlying label.
	 *
	 * @return the <tt>Component</tt> containing the underlying label
	 */
	final Component getLabel() {
		return LABEL;
	}
}
