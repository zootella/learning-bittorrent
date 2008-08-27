package com.limegroup.gnutella.gui.options.panes;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;

import com.limegroup.gnutella.gui.BoxPanel;
import com.limegroup.gnutella.gui.GUIMediator;
import com.limegroup.gnutella.gui.LabeledComponent;
import com.limegroup.gnutella.settings.SearchSettings;
import com.limegroup.gnutella.spam.SpamManager;

/**
 * This class gives the user the option of whether or not to enable LimeWire's
 * internal spam filter
 */
// 2345678|012345678|012345678|012345678|012345678|012345678|012345678|012345678|
public final class EnableSpamFilterPaneItem extends AbstractPaneItem {
    
    /** Display search results in place */
    private JRadioButton DISPLAY_IN_PLACE = new JRadioButton();
    
    /** Move spam results to the bottom */
    private JRadioButton MOVE_TO_BOTTOM = new JRadioButton();
    
    /** Hide spam results */
    private JRadioButton HIDE_SPAM = new JRadioButton();

    /** The spam threshold slider */
    private JSlider THRESHOLD = new JSlider(0, 50);

    /** Reset the spam filter */
    private JButton RESET = new JButton();
    
    public EnableSpamFilterPaneItem(final String key) {
        super(key);

        DISPLAY_IN_PLACE.setText(GUIMediator.getStringResource("OPTIONS_SEARCH_JUNK_DISPLAY_IN_PLACE"));
        MOVE_TO_BOTTOM.setText(GUIMediator.getStringResource("OPTIONS_SEARCH_JUNK_MOVE_TO_BOTTOM"));
        HIDE_SPAM.setText(GUIMediator.getStringResource("OPTIONS_SEARCH_JUNK_HIDE_SPAM"));

        ButtonGroup group = new ButtonGroup();
        group.add(DISPLAY_IN_PLACE);
        group.add(MOVE_TO_BOTTOM);
        group.add(HIDE_SPAM);
                
        BoxPanel buttonPanel = new BoxPanel();
        buttonPanel.add(DISPLAY_IN_PLACE);
        buttonPanel.add(MOVE_TO_BOTTOM);
        buttonPanel.add(HIDE_SPAM);

        RESET.setText(GUIMediator.getStringResource("OPTIONS_SEARCH_JUNK_RESET_LABEL"));
        RESET.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                SpamManager.instance().clearFilterData();
            }
        });

        JPanel mainPanel = new JPanel();
        mainPanel.setOpaque(false);

        mainPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = null;

        // --- DISPLAY ---

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = 3;
        gbc.weightx = 1;
        gbc.insets = new Insets(10, 0, 3, 0);
        mainPanel.add(new JLabel(GUIMediator.getStringResource("OPTIONS_SEARCH_JUNK_DISPLAY")), gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        mainPanel.add(Box.createHorizontalStrut(10), gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = 2;
        gbc.weightx = 1;
        mainPanel.add(buttonPanel, gbc);

        // --- THRESHOLD ---

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = 3;
        gbc.weightx = 1;
        gbc.insets = new Insets(10, 0, 3, 0);
        mainPanel.add(new JLabel(GUIMediator.getStringResource("OPTIONS_SEARCH_JUNK_THRESHOLD")), gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        mainPanel.add(Box.createHorizontalStrut(10), gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        LabeledComponent comp = new LabeledComponent("OPTIONS_SEARCH_JUNK_THRESHOLD_LAX", THRESHOLD);
        THRESHOLD.setToolTipText(GUIMediator.getStringResource("OPTIONS_SEARCH_JUNK_THRESHOLD_TOOLTIP"));
        mainPanel.add(comp.getComponent(), gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        mainPanel.add(new JLabel(GUIMediator.getStringResource("OPTIONS_SEARCH_JUNK_THRESHOLD_STRICT")), gbc);

        // --- RESET ---

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.anchor = GridBagConstraints.SOUTH;
        gbc.fill = GridBagConstraints.NONE;
        gbc.gridwidth = 3;
        gbc.weighty = 1;
        mainPanel.add(RESET, gbc);

        add(mainPanel);
    }

    public void initOptions() {
        switch (SearchSettings.DISPLAY_JUNK_MODE.getValue()) {
        case SearchSettings.DISPLAY_JUNK_IN_PLACE:
            DISPLAY_IN_PLACE.setSelected(true);
            break;
        case SearchSettings.HIDE_JUNK:
            HIDE_SPAM.setSelected(true);
            break;
        default:
            MOVE_TO_BOTTOM.setSelected(true);
            break;
        }

        THRESHOLD.setValue( (int) (100 - 100 * SearchSettings.FILTER_SPAM_RESULTS.getValue()));
    }

    public boolean applyOptions() throws IOException {
        if (DISPLAY_IN_PLACE.isSelected()) {
            SearchSettings.DISPLAY_JUNK_MODE.setValue(SearchSettings.DISPLAY_JUNK_IN_PLACE);
        } else if (HIDE_SPAM.isSelected()) {
            SearchSettings.DISPLAY_JUNK_MODE.setValue(SearchSettings.HIDE_JUNK);
        } else {
            SearchSettings.DISPLAY_JUNK_MODE.setValue(SearchSettings.MOVE_JUNK_TO_BOTTOM);
        }
        
        SearchSettings.FILTER_SPAM_RESULTS.setValue((float) (100 - THRESHOLD.getValue()) / 100f);

        return false;
    }

    public boolean isDirty() {
        boolean modeChange = false;

        if (DISPLAY_IN_PLACE.isSelected()) {
            modeChange = SearchSettings.DISPLAY_JUNK_MODE.getValue() != SearchSettings.DISPLAY_JUNK_IN_PLACE;
        } else if (HIDE_SPAM.isSelected()) {
            modeChange = SearchSettings.DISPLAY_JUNK_MODE.getValue() != SearchSettings.HIDE_JUNK;
        } else {
            modeChange = SearchSettings.DISPLAY_JUNK_MODE.getValue() != SearchSettings.MOVE_JUNK_TO_BOTTOM;
        }
        
        return modeChange
                || (SearchSettings.FILTER_SPAM_RESULTS.getValue() != (float) (100 - THRESHOLD.getValue()) / 100f);
    }
}
