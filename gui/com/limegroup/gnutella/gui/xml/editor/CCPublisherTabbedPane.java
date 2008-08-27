package com.limegroup.gnutella.gui.xml.editor;

import com.limegroup.gnutella.FileDesc;
import com.limegroup.gnutella.xml.LimeXMLNames;

public class CCPublisherTabbedPane extends MetaEditorTabbedPane {

	private CCPublisherTab _publisher;
	
	public CCPublisherTabbedPane(FileDesc fd) {
		super(fd, LimeXMLNames.AUDIO_SCHEMA);
		_publisher = new CCPublisherTab(fd,getDocument());
		add(_publisher);
	}
	
	public boolean reserveIdentifier() {
		return _publisher.reserveIdentifier();
	}
	
	
}
