package com.limegroup.gnutella.gui.xml.editor;

import com.limegroup.gnutella.FileDesc;
import com.limegroup.gnutella.xml.LimeXMLNames;

public class VideoTabbedPane extends MetaEditorTabbedPane {
	 /** Creates a new instance of VideoTabbedPane */
    public VideoTabbedPane(FileDesc fd) {
        super(fd, LimeXMLNames.VIDEO_SCHEMA);
        
        add(new VideoEditor(fd, getSchema(), getDocument()));
    }
}
