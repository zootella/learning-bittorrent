package com.limegroup.gnutella.gui.init;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import com.limegroup.gnutella.gui.BoxPanel;
import com.limegroup.gnutella.gui.GUIMediator;
import com.limegroup.gnutella.settings.ContentSettings;

/**
 * Wizard step for showing the filtering option.
 */
public class FilterWindow extends SetupWindow {

    /**
     * The checkbox that determines whether or not to use content management.
     */
    private JCheckBox _filter;

    /**
     * Creates the window and its components.
     */
    FilterWindow(SetupManager manager) {
        super(manager, "SETUP_FILTER_TITLE", "SETUP_FILTER_LABEL", ContentSettings.LEARN_MORE_URL);
    }
    
    protected void createWindow() {
        super.createWindow();

        JPanel mainPanel = new BoxPanel(BoxLayout.X_AXIS);
        _filter = new JCheckBox(GUIMediator.getStringResource("SETUP_FILTER_CHECKBOX"));
        _filter.setSelected(ContentSettings.USER_WANTS_MANAGEMENTS.getValue());
        mainPanel.add(_filter);
        mainPanel.add(Box.createHorizontalGlue());
        addSetupComponent(mainPanel);
    }

    /**
     * Overrides applySettings in SetupWindow superclass.
     * Applies the settings handled in this window.
     */
    public void applySettings() throws ApplySettingsException {
        ContentSettings.USER_WANTS_MANAGEMENTS.setValue(_filter.isSelected());
    }
}
