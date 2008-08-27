package com.limegroup.gnutella.gui.xml.editor;

import com.limegroup.gnutella.FileDesc;
import com.limegroup.gnutella.xml.LimeXMLNames;

public class DocumentTabbedPane extends MetaEditorTabbedPane {

	public DocumentTabbedPane(FileDesc fd) {
		 super(fd, LimeXMLNames.DOCUMENT_SCHEMA);
		 add(new DocumentEditor(fd, getSchema(), getDocument()));
	}

}
