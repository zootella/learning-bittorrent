package com.limegroup.gnutella.gui.options.panes;

import java.io.IOException;

import javax.swing.JCheckBox;

import com.limegroup.gnutella.gui.LabeledComponent;
import com.limegroup.gnutella.settings.ContentSettings;

/**
 * Constructs the content filter dialog options pane item.
 */
public class ContentFilterPaneItem extends AbstractPaneItem {

    private final JCheckBox CHECK_BOX = new JCheckBox();

    public ContentFilterPaneItem(final String key) {
        super(key, ContentSettings.LEARN_MORE_URL);
        LabeledComponent comp = new LabeledComponent("OPTIONS_CONTENT_FILTER_CHECKBOX_LABEL",
                                                     CHECK_BOX,
                                                     LabeledComponent.LEFT_GLUE);
        add(comp.getComponent());
    }

    public void initOptions() {
        CHECK_BOX.setSelected(ContentSettings.USER_WANTS_MANAGEMENTS.getValue());
    }

    public boolean applyOptions() throws IOException {
        ContentSettings.USER_WANTS_MANAGEMENTS.setValue(CHECK_BOX.isSelected());
        return false;
    }
    
    public boolean isDirty() {
        return ContentSettings.USER_WANTS_MANAGEMENTS.getValue() != CHECK_BOX.isSelected();
    }
}
