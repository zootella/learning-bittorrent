package com.limegroup.gnutella.gui.xml.editor;

import java.io.File;
import java.util.HashMap;

import com.limegroup.gnutella.Assert;
import com.limegroup.gnutella.gui.GUIMediator;
import com.limegroup.gnutella.xml.LimeXMLUtils;
import com.limegroup.gnutella.xml.LimeXMLNames;
/**
 * 
 */
public final class MetaEditorUtil {
   
    private static final HashMap XSD_MESSAGEBUNDLE_BRIDGE = new HashMap();
    
    static {
        XSD_MESSAGEBUNDLE_BRIDGE.put(LimeXMLNames.AUDIO, "META_EDITOR_AUDIO_LABEL");
        XSD_MESSAGEBUNDLE_BRIDGE.put(LimeXMLNames.AUDIO_TITLE, "META_EDITOR_TITLE_LABEL");
        XSD_MESSAGEBUNDLE_BRIDGE.put(LimeXMLNames.AUDIO_ARTIST, "META_EDITOR_ARTIST_LABEL");
        XSD_MESSAGEBUNDLE_BRIDGE.put(LimeXMLNames.AUDIO_ALBUM, "META_EDITOR_ALBUM_LABEL");
        XSD_MESSAGEBUNDLE_BRIDGE.put(LimeXMLNames.AUDIO_GENRE, "META_EDITOR_GENRE_LABEL");
        XSD_MESSAGEBUNDLE_BRIDGE.put(LimeXMLNames.AUDIO_YEAR, "META_EDITOR_YEAR_LABEL");
        XSD_MESSAGEBUNDLE_BRIDGE.put(LimeXMLNames.AUDIO_TYPE, "META_EDITOR_TYPE_LABEL");
        XSD_MESSAGEBUNDLE_BRIDGE.put(LimeXMLNames.AUDIO_TRACK, "META_EDITOR_TRACK_LABEL");
        XSD_MESSAGEBUNDLE_BRIDGE.put(LimeXMLNames.AUDIO_LANGUAGE, "META_EDITOR_LANGUAGE_LABEL");
        XSD_MESSAGEBUNDLE_BRIDGE.put(LimeXMLNames.AUDIO_SECONDS, "META_EDITOR_SECONDS_LABEL");
        XSD_MESSAGEBUNDLE_BRIDGE.put(LimeXMLNames.AUDIO_BITRATE, "META_EDITOR_BITRATE_LABEL");
        XSD_MESSAGEBUNDLE_BRIDGE.put(LimeXMLNames.AUDIO_COMMENTS, "META_EDITOR_COMMENTS_LABEL");
        XSD_MESSAGEBUNDLE_BRIDGE.put(LimeXMLNames.AUDIO_SHA1, "META_EDITOR_SHA1_LABEL");
        XSD_MESSAGEBUNDLE_BRIDGE.put(LimeXMLNames.AUDIO_PRICE, "META_EDITOR_PRICE_LABEL");
        XSD_MESSAGEBUNDLE_BRIDGE.put(LimeXMLNames.AUDIO_LINK, "META_EDITOR_LINK_LABEL");
        XSD_MESSAGEBUNDLE_BRIDGE.put(LimeXMLNames.AUDIO_ACTION, "META_EDITOR_ACTION_LABEL");
        XSD_MESSAGEBUNDLE_BRIDGE.put(LimeXMLNames.AUDIO_LICENSE, "META_EDITOR_LICENSE_LABEL");

        XSD_MESSAGEBUNDLE_BRIDGE.put(LimeXMLNames.VIDEO, "META_EDITOR_VIDEO_LABEL");
        XSD_MESSAGEBUNDLE_BRIDGE.put(LimeXMLNames.VIDEO_TITLE, "META_EDITOR_TITLE_LABEL");
        XSD_MESSAGEBUNDLE_BRIDGE.put(LimeXMLNames.VIDEO_TYPE, "META_EDITOR_TYPE_LABEL");
        XSD_MESSAGEBUNDLE_BRIDGE.put(LimeXMLNames.VIDEO_YEAR, "META_EDITOR_YEAR_LABEL");
        XSD_MESSAGEBUNDLE_BRIDGE.put(LimeXMLNames.VIDEO_RATING, "META_EDITOR_RATING_LABEL");
        XSD_MESSAGEBUNDLE_BRIDGE.put(LimeXMLNames.VIDEO_LENGTH, "META_EDITOR_LENGTH_LABEL");
        XSD_MESSAGEBUNDLE_BRIDGE.put(LimeXMLNames.VIDEO_COMMENTS, "META_EDITOR_COMMENTS_LABEL");
        XSD_MESSAGEBUNDLE_BRIDGE.put(LimeXMLNames.VIDEO_LICENSE, "META_EDITOR_LICENSE_LABEL");
        XSD_MESSAGEBUNDLE_BRIDGE.put(LimeXMLNames.VIDEO_LICENSETYPE, "META_EDITOR_LICENSETYPE_LABEL");
        XSD_MESSAGEBUNDLE_BRIDGE.put(LimeXMLNames.VIDEO_ACTION, "META_EDITOR_ACTION_LABEL");
        XSD_MESSAGEBUNDLE_BRIDGE.put(LimeXMLNames.VIDEO_DIRECTOR, "META_EDITOR_DIRECTOR_LABEL");
        XSD_MESSAGEBUNDLE_BRIDGE.put(LimeXMLNames.VIDEO_STUDIO, "META_EDITOR_STUDIO_LABEL");
        XSD_MESSAGEBUNDLE_BRIDGE.put(LimeXMLNames.VIDEO_LANGUAGE, "META_EDITOR_LANGUAGE_LABEL");
        XSD_MESSAGEBUNDLE_BRIDGE.put(LimeXMLNames.VIDEO_STARS, "META_EDITOR_STARS_LABEL");
        XSD_MESSAGEBUNDLE_BRIDGE.put(LimeXMLNames.VIDEO_PRODUCER, "META_EDITOR_PRODUCER_LABEL");
        XSD_MESSAGEBUNDLE_BRIDGE.put(LimeXMLNames.VIDEO_SUBTITLES, "META_EDITOR_SUBTITLES_LABEL");
        
        XSD_MESSAGEBUNDLE_BRIDGE.put(LimeXMLNames.DOCUMENT, "META_EDITOR_DOCUMENT_LABEL");
        XSD_MESSAGEBUNDLE_BRIDGE.put(LimeXMLNames.DOCUMENT_TITLE, "META_EDITOR_TITLE_LABEL");
        XSD_MESSAGEBUNDLE_BRIDGE.put(LimeXMLNames.DOCUMENT_TOPIC, "META_EDITOR_TOPIC_LABEL");
        XSD_MESSAGEBUNDLE_BRIDGE.put(LimeXMLNames.DOCUMENT_AUTHOR, "META_EDITOR_AUTHOR_LABEL");
        XSD_MESSAGEBUNDLE_BRIDGE.put(LimeXMLNames.DOCUMENT_LICENSE, "META_EDITOR_LICENSE_LABEL");
        XSD_MESSAGEBUNDLE_BRIDGE.put(LimeXMLNames.DOCUMENT_LICENSETYPE, "META_EDITOR_LICENSETYPE_LABEL");
        
        XSD_MESSAGEBUNDLE_BRIDGE.put(LimeXMLNames.APPLICATION, "META_EDITOR_APPLICATION_LABEL");
        XSD_MESSAGEBUNDLE_BRIDGE.put(LimeXMLNames.APPLICATION_NAME, "META_EDITOR_NAME_LABEL");
        XSD_MESSAGEBUNDLE_BRIDGE.put(LimeXMLNames.APPLICATION_PUBLISHER, "META_EDITOR_PUBLISHER_LABEL");
        XSD_MESSAGEBUNDLE_BRIDGE.put(LimeXMLNames.APPLICATION_PLATFORM, "META_EDITOR_PLATFORM_LABEL");
        XSD_MESSAGEBUNDLE_BRIDGE.put(LimeXMLNames.APPLICATION_LICENSETYPE, "META_EDITOR_LICENSETYPE_LABEL");
        XSD_MESSAGEBUNDLE_BRIDGE.put(LimeXMLNames.APPLICATION_LICENSE, "META_EDITOR_LICENSE_LABEL");
        
        XSD_MESSAGEBUNDLE_BRIDGE.put(LimeXMLNames.IMAGE, "META_EDITOR_IMAGE_LABEL");
        XSD_MESSAGEBUNDLE_BRIDGE.put(LimeXMLNames.IMAGE_TITLE, "META_EDITOR_TITLE_LABEL");
        XSD_MESSAGEBUNDLE_BRIDGE.put(LimeXMLNames.IMAGE_DESCRIPTION, "META_EDITOR_DESCRIPTION_LABEL");
        XSD_MESSAGEBUNDLE_BRIDGE.put(LimeXMLNames.IMAGE_ARTIST, "META_EDITOR_ARTIST_LABEL");
        XSD_MESSAGEBUNDLE_BRIDGE.put(LimeXMLNames.IMAGE_LICENSE, "META_EDITOR_LICENSE_LABEL");
        XSD_MESSAGEBUNDLE_BRIDGE.put(LimeXMLNames.IMAGE_LICENSETYPE, "META_EDITOR_LICENSETYPE_LABEL");
    }
    
    public static boolean contains(String resource) {
        return XSD_MESSAGEBUNDLE_BRIDGE.containsKey(resource);
    }
    
    /**
     * 
     */
    public static String getStringResource(String resourceKey) {
        String rscKey = (String)XSD_MESSAGEBUNDLE_BRIDGE.get(resourceKey);
        Assert.that(rscKey != null, "Unknown resourceKey: " + resourceKey);
        return GUIMediator.getStringResource(rscKey);
    }
    
    /**
     * 
     */
    public static String getKind(File file) {
        String name = file.getName();
        
        if (LimeXMLUtils.isMP3File(name)) {
            return GUIMediator.getStringResource("META_EDITOR_MP3_KIND_LABEL");
        } else if (LimeXMLUtils.isM4AFile(name)) {
            return GUIMediator.getStringResource("META_EDITOR_MP4_KIND_LABEL");
        } else if (LimeXMLUtils.isOGGFile(name)) {
            return GUIMediator.getStringResource("META_EDITOR_OGG_KIND_LABEL");
        } else {
            return null;
        }
    }
    
    private MetaEditorUtil() {
    }
}
