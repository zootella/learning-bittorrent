package com.limegroup.gnutella.gui.xml.editor;

import java.io.IOException;
import java.util.Iterator;

import org.xml.sax.SAXException;

import com.limegroup.gnutella.Assert;
import com.limegroup.gnutella.FileDesc;
import com.limegroup.gnutella.FileEventListener;
import com.limegroup.gnutella.RouterService;
import com.limegroup.gnutella.gui.GUIMediator;
import com.limegroup.gnutella.gui.library.LibraryMediator;
import com.limegroup.gnutella.xml.LimeXMLDocument;
import com.limegroup.gnutella.xml.LimeXMLNames;
import com.limegroup.gnutella.xml.LimeXMLReplyCollection;
import com.limegroup.gnutella.xml.LimeXMLSchema;
import com.limegroup.gnutella.xml.LimeXMLUtils;
import com.limegroup.gnutella.xml.SchemaNotFoundException;
import com.limegroup.gnutella.xml.SchemaReplyCollectionMapper;

public class MetaDataSaver {

	protected final MetaEditorTabbedPane _pane;
	
	private final String _fileName;
	
	public MetaDataSaver(MetaEditorTabbedPane pane,String filename) {
		_pane = pane;
		_fileName = filename;
	}

	public boolean saveMetaData() {
    	if(!_pane.checkInput())return false;
        else {
            doSave();
            return true;
        }
    }
	
	protected void doSave() {
		for (Iterator iter = _pane.getFileEventListeners().iterator(); iter.hasNext();) {
			FileEventListener listener = (FileEventListener) iter.next();
			RouterService.getFileManager().registerFileManagerEventListener(listener);
		}
		GUIMediator.instance().schedule( new Runnable(){
            public void run() {
                try {
                    GUIMediator.safeInvokeAndWait(new Runnable() {
                        public void run(){
                            LibraryMediator.instance().setAnnotateEnabled(false);
                        }
                    });                    
                    saveMeta();
                } finally{
                    GUIMediator.safeInvokeAndWait(new Runnable() {
                        public void run(){
                            LibraryMediator.instance().setAnnotateEnabled(true);
                        }
                    });
                    for (Iterator iter = _pane.getFileEventListeners().iterator(); iter.hasNext();) {
            			FileEventListener listener = (FileEventListener) iter.next();
            			RouterService.getFileManager().unregisterFileManagerEventListener(listener);
            		}
                }
            }
        });
	}

	private void saveMeta() {

		String XMLString = _pane.getInput();
		FileDesc fd = _pane.getFileDesc();
		if (XMLString == null) {
			return;
		} else if (XMLString.trim().length() == 0) {
			removeMeta(fd, _pane.getSchema());
			return;
		}
		LimeXMLDocument oldDoc = _pane.getDocument();
		LimeXMLDocument newDoc = null;

		try {
			newDoc = new LimeXMLDocument(XMLString);
		} catch (SAXException e) {
			GUIMediator.showError("ERROR_SAVE_META_DOC");
			return;
		} catch (SchemaNotFoundException e) {
			GUIMediator.showError("ERROR_SAVE_META_DOC");
			return;
		} catch (IOException e) {
			GUIMediator.showError("ERROR_SAVE_META_DOC");
			return;
		}

		//OK we have the new LimeXMLDocument
		SchemaReplyCollectionMapper map = SchemaReplyCollectionMapper
				.instance();
		String uri = newDoc.getSchemaURI();
		LimeXMLReplyCollection collection = map.getReplyCollection(uri);

		//This is a really bad case!
		Assert.that(collection != null,
				"Cant add doc to nonexistent collection");

		if (oldDoc == null) {
			collection.addReply(fd, newDoc);
		} else {
			oldDoc = collection.replaceDoc(fd, newDoc);
		}

		if (LimeXMLUtils.isSupportedFormat(_fileName)) {

			int committed = collection.mediaFileToDisk(fd, _fileName, newDoc,
					false);

			switch (committed) {
			case LimeXMLReplyCollection.FILE_DEFECTIVE:
				GUIMediator.showError("ERROR_SAVE_META_FILE");
				break;
			case LimeXMLReplyCollection.RW_ERROR:
				GUIMediator.showError("ERROR_SAVE_META_RW");
				break;
			case LimeXMLReplyCollection.BAD_ID3:
				GUIMediator.showError("ERROR_SAVE_META_ID3");
				break;
			case LimeXMLReplyCollection.FAILED_TITLE:
				GUIMediator.showError("ERROR_SAVE_META_ID3");
				break;
			case LimeXMLReplyCollection.FAILED_ARTIST:
				cleanUpChanges(fd, LimeXMLNames.AUDIO_ARTIST, collection,
						oldDoc);
				break;
			case LimeXMLReplyCollection.FAILED_ALBUM:
				cleanUpChanges(fd, LimeXMLNames.AUDIO_ALBUM, collection, oldDoc);
				break;
			case LimeXMLReplyCollection.FAILED_YEAR:
				cleanUpChanges(fd, LimeXMLNames.AUDIO_YEAR, collection, oldDoc);
				break;
			case LimeXMLReplyCollection.FAILED_COMMENT:
				cleanUpChanges(fd, LimeXMLNames.AUDIO_COMMENTS, collection,
						oldDoc);
				break;
			case LimeXMLReplyCollection.FAILED_TRACK:
				cleanUpChanges(fd, LimeXMLNames.AUDIO_TRACK, collection, oldDoc);
				break;
			case LimeXMLReplyCollection.FAILED_GENRE:
				cleanUpChanges(fd, LimeXMLNames.AUDIO_GENRE, collection, oldDoc);
				break;
			case LimeXMLReplyCollection.HASH_FAILED:
				GUIMediator.showError("ERROR_SAVE_META_DISK");
				break;
			}

		} else {

			if (!collection.writeMapToDisk()) {
				GUIMediator.showError("ERROR_SAVE_META_DISK");
			}
		}
	}

	private void cleanUpChanges(FileDesc fd, String canonicalFieldName,
			LimeXMLReplyCollection collection, LimeXMLDocument oldDoc) {
		GUIMediator.showError("ERROR_SAVE_META_BAD");

		if (oldDoc == null)// it was added....just remove
			collection.removeDoc(fd);
		else
			// older one was replaced....replace back
			collection.replaceDoc(fd, oldDoc);
	}

	private void removeMeta(FileDesc fd, LimeXMLSchema schema) {

		String uri = schema.getSchemaURI();
		SchemaReplyCollectionMapper map = SchemaReplyCollectionMapper
				.instance();
		LimeXMLReplyCollection collection = map.getReplyCollection(uri);

		Assert.that(collection != null,
				"Trying to remove data from a non-existent collection");

		if (!collection.removeDoc(fd)) {// unable to remove or write to disk
			GUIMediator.showError("ERROR_DEL_META_SYSTEM");
		}
	}
}
