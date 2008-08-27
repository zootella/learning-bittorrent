

package com.limegroup.gnutella.gui.xml.editor.audio;

import com.limegroup.gnutella.FileDesc;
import com.limegroup.gnutella.gui.xml.editor.CCPublisherTab;
import com.limegroup.gnutella.gui.xml.editor.MetaEditorTabbedPane;
import com.limegroup.gnutella.gui.xml.editor.MetaEditorUtil;
import com.limegroup.gnutella.util.FileUtils;
import com.limegroup.gnutella.xml.LimeXMLNames;

public class AudioTabbedPane extends MetaEditorTabbedPane {
    
    /** Creates a new instance of AudioTabbedPane */
    public AudioTabbedPane(FileDesc fd) {
        super(fd, LimeXMLNames.AUDIO_SCHEMA);
        
        add(new AudioInfo(fd, getSchema(), getDocument()));
        add(new AudioEditor(fd, getSchema(), getDocument()));
    }
}
