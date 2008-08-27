package com.limegroup.gnutella.gui.xml.editor;

import com.limegroup.gnutella.FileDesc;
import com.limegroup.gnutella.xml.LimeXMLNames;

public class ImageTabbedPane extends MetaEditorTabbedPane {

	public ImageTabbedPane(FileDesc fd) {
		 super(fd, LimeXMLNames.IMAGE_SCHEMA);
		 add(new ImageEditor(fd, getSchema(), getDocument()));
	}

}
