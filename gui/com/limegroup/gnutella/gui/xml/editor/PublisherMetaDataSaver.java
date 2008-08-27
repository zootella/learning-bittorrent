package com.limegroup.gnutella.gui.xml.editor;

public class PublisherMetaDataSaver extends MetaDataSaver {

	private CCPublisherTabbedPane _publisherPane;
	
	public PublisherMetaDataSaver(CCPublisherTabbedPane pane, String filename) {
		super(pane, filename);
		_publisherPane = pane;
	}

	public boolean saveMetaData() {
		if(!_publisherPane.checkInput() || !_publisherPane.reserveIdentifier())
			return false;
        else {
            doSave();
            return true;
        }
	}

	
}
