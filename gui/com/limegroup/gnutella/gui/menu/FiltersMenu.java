package com.limegroup.gnutella.gui.menu;

import com.limegroup.gnutella.gui.options.ConfigureOptionsAction;
import com.limegroup.gnutella.settings.ContentSettings;

/**
 * Filters menu.
 */
public class FiltersMenu extends AbstractMenu {

    public FiltersMenu(String key) {
        super(key);
        
        addMenuItem("FILTERS_CONFIGURE", new ConfigureOptionsAction("OPTIONS_CONTENT_FILTER_MAIN_TITLE"));
        addMenuItem("FILTERS_LEARN_MORE", new LinkListener(ContentSettings.LEARN_MORE_URL));
    }

}
