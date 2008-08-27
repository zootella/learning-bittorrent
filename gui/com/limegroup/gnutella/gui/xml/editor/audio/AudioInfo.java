package com.limegroup.gnutella.gui.xml.editor.audio;

import java.io.File;

import javax.swing.JLabel;

import com.limegroup.gnutella.FileDesc;
import com.limegroup.gnutella.gui.GUIMediator;
import com.limegroup.gnutella.gui.xml.editor.DetailsPanel;
import com.limegroup.gnutella.gui.xml.editor.IconPanel;
import com.limegroup.gnutella.gui.xml.editor.MetaEditorPanel;
import com.limegroup.gnutella.gui.xml.editor.MetaEditorUtil;
import com.limegroup.gnutella.util.CommonUtils;
import com.limegroup.gnutella.xml.LimeXMLDocument;
import com.limegroup.gnutella.xml.LimeXMLNames;
import com.limegroup.gnutella.xml.LimeXMLSchema;

class AudioInfo extends MetaEditorPanel {
    
    public AudioInfo(FileDesc fd, LimeXMLSchema schema, LimeXMLDocument doc) {
        super(fd, schema, doc);
        
        super.setName(GUIMediator.getStringResource("META_EDITOR_INFO_TAB_LABEL"));
        
        initComponents();
        
        File file = fd.getFile();
        
        ((DetailsPanel)detailsPanel).setMaxRows(13);
        ((DetailsPanel)detailsPanel).initWithFileDesc(fd, schema.getSchemaURI());
        
        ((IconPanel)iconPanel).initWithFileDesc(fd);
        whereTextArea.setFont(new JLabel().getFont());
        whereTextArea.setText(fd.getFile().toString());
        
        String title = getValue(LimeXMLNames.AUDIO_TITLE);
        String artist = getValue(LimeXMLNames.AUDIO_ARTIST);
        String album = getValue(LimeXMLNames.AUDIO_ALBUM);
        
        if (title != null) {
            String length = getValue(LimeXMLNames.AUDIO_SECONDS);
            if (length != null) {
                try {
                    title += " (" + CommonUtils.seconds2time(Integer.parseInt(length)) + ")";
                } catch (NumberFormatException err) {
                }
            }
            
            titleLabel.setText(title);
        } else {
            titleLabel.setText("");
        }
        
        if (artist != null) {
            artistLabel.setText(artist);
        } else {
            artistLabel.setText("");
        }
        
        
        if (album != null) {
            albumLabel.setText(album);
        } else {
            albumLabel.setText("");
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;
        javax.swing.JPanel panel1;
        javax.swing.JPanel panel2;
        javax.swing.JPanel panel3;
        javax.swing.JPanel panel4;
        javax.swing.JSeparator seperator1;
        javax.swing.JSeparator seperator2;

        panel1 = new javax.swing.JPanel();
        seperator1 = new javax.swing.JSeparator();
        panel2 = new javax.swing.JPanel();
        iconPanel = new IconPanel();
        titleLabel = new javax.swing.JLabel();
        artistLabel = new javax.swing.JLabel();
        albumLabel = new javax.swing.JLabel();
        panel3 = new javax.swing.JPanel();
        seperator2 = new javax.swing.JSeparator();
        whereTextArea = new javax.swing.JTextArea();
        panel4 = new DetailsPanel();
        detailsPanel = new DetailsPanel();

        setLayout(new java.awt.BorderLayout());

        setOpaque(false);
        panel1.setLayout(new java.awt.BorderLayout());

        panel1.setOpaque(false);
        panel1.add(seperator1, java.awt.BorderLayout.SOUTH);

        panel2.setLayout(new java.awt.GridBagLayout());

        panel2.setOpaque(false);
        iconPanel.setBackground(new java.awt.Color(255, 255, 255));
        iconPanel.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(153, 153, 153)));
        iconPanel.setPreferredSize(new java.awt.Dimension(48, 48));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridheight = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        panel2.add(iconPanel, gridBagConstraints);

        titleLabel.setText("TITLE (LENGTH)");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        panel2.add(titleLabel, gridBagConstraints);

        artistLabel.setText("ARTIST");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        panel2.add(artistLabel, gridBagConstraints);

        albumLabel.setText("ALBUM");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        panel2.add(albumLabel, gridBagConstraints);

        panel1.add(panel2, java.awt.BorderLayout.WEST);

        add(panel1, java.awt.BorderLayout.NORTH);

        panel3.setLayout(new java.awt.BorderLayout());

        panel3.setOpaque(false);
        panel3.add(seperator2, java.awt.BorderLayout.NORTH);

        whereTextArea.setEditable(false);
        whereTextArea.setLineWrap(true);
        whereTextArea.setRows(2);
        whereTextArea.setText("/Users/roger/Shared/HackThePlanet.mp3");
        whereTextArea.setWrapStyleWord(true);
        whereTextArea.setMinimumSize(new java.awt.Dimension(12, 13));
        whereTextArea.setPreferredSize(new java.awt.Dimension(12, 32));
        whereTextArea.setOpaque(false);
        panel3.add(whereTextArea, java.awt.BorderLayout.SOUTH);

        add(panel3, java.awt.BorderLayout.SOUTH);

        panel4.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        panel4.setOpaque(false);
        detailsPanel.setLayout(new java.awt.GridLayout(8, 2, 3, 0));

        detailsPanel.setOpaque(false);
        panel4.add(detailsPanel);

        add(panel4, java.awt.BorderLayout.CENTER);

    }//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel albumLabel;
    private javax.swing.JLabel artistLabel;
    private javax.swing.JPanel detailsPanel;
    private javax.swing.JPanel iconPanel;
    private javax.swing.JLabel titleLabel;
    private javax.swing.JTextArea whereTextArea;
    // End of variables declaration//GEN-END:variables
    
}