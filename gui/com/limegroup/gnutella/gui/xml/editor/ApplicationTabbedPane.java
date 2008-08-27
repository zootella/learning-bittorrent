package com.limegroup.gnutella.gui.xml.editor;

import com.limegroup.gnutella.FileDesc;
import com.limegroup.gnutella.xml.LimeXMLNames;

public class ApplicationTabbedPane extends MetaEditorTabbedPane{
	 /** Creates a new instance of VideoTabbedPane */
    public ApplicationTabbedPane(FileDesc fd) {
        super(fd, LimeXMLNames.APPLICATION_SCHEMA);
        add(new ApplicationEditor(fd, getSchema(), getDocument()));
    }
}
