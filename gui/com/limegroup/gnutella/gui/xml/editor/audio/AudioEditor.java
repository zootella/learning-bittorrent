package com.limegroup.gnutella.gui.xml.editor.audio;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import java.util.Arrays;

import javax.swing.DefaultComboBoxModel;

import com.limegroup.gnutella.FileDesc;
import com.limegroup.gnutella.gui.xml.editor.AutoCompleteComboBoxEditor;
import com.limegroup.gnutella.gui.xml.editor.MetaEditorPanel;
import com.limegroup.gnutella.gui.xml.editor.MetaEditorUtil;
import com.limegroup.gnutella.gui.xml.ComboBoxValue;
import com.limegroup.gnutella.xml.LimeXMLDocument;
import com.limegroup.gnutella.xml.LimeXMLNames;
import com.limegroup.gnutella.xml.LimeXMLSchema;
import com.limegroup.gnutella.xml.SchemaFieldInfo;


class AudioEditor extends MetaEditorPanel {
    
    private String title = null;
    private boolean titleEdited = false;
    
    public AudioEditor(FileDesc fd, LimeXMLSchema schema, LimeXMLDocument doc) {
        super(fd, schema, doc);
        
        initComponents();
        initLabels();
        
        super.setName(MetaEditorUtil.getStringResource(LimeXMLNames.AUDIO));
        
        AutoCompleteComboBoxEditor editor = new AutoCompleteComboBoxEditor();
        genreComboBox.setEditor(editor);
        initTextFields(); // sets the combo box model
        editor.setModel(genreComboBox.getModel()); // use the model for auto complete
        
        setMultiEdit(false);
    }
    
    public boolean hasChanged() {
        return (titleEdited || super.hasChanged());
    }
    
    private void setMultiEdit(boolean multiEdit) {
        titleLabel.setVisible(!multiEdit);
        titleTextField.setVisible(!multiEdit);
        setCheckBoxesVisible(multiEdit);
    }
    
    private void initTextFields() {
        
        LimeXMLDocument doc = getDocument();
        
        this.title = null;
        String track = null;
        String artist = null;
        String album = null;
        String genre = null;
        String comments = null;
        String year = null;
        String type = null;
        String language = null;
        
        if (doc != null) {
            title = doc.getValue(LimeXMLNames.AUDIO_TITLE);
            track = doc.getValue(LimeXMLNames.AUDIO_TRACK);
            artist = doc.getValue(LimeXMLNames.AUDIO_ARTIST);
            album = doc.getValue(LimeXMLNames.AUDIO_ALBUM);
            genre = doc.getValue(LimeXMLNames.AUDIO_GENRE);
            comments = doc.getValue(LimeXMLNames.AUDIO_COMMENTS);
            year = doc.getValue(LimeXMLNames.AUDIO_YEAR);
            type = doc.getValue(LimeXMLNames.AUDIO_TYPE);
            language = doc.getValue(LimeXMLNames.AUDIO_LANGUAGE);
        }
            
        addComponent(LimeXMLNames.AUDIO_TITLE, titleTextField);
        
        addComponent(LimeXMLNames.AUDIO_ARTIST, artistCheckBox, artistTextField);
        addComponent(LimeXMLNames.AUDIO_ALBUM, albumCheckBox, albumTextField);
        addComponent(LimeXMLNames.AUDIO_YEAR, yearCheckBox, yearTextField);
        addComponent(LimeXMLNames.AUDIO_TRACK, trackCheckBox, trackTextField);
        addComponent(LimeXMLNames.AUDIO_LANGUAGE, languageCheckBox, languageTextField);
        addComponent(LimeXMLNames.AUDIO_COMMENTS, commentsCheckBox, commentsTextArea);
        addComponent(LimeXMLNames.AUDIO_TYPE, typeCheckBox, typeComboBox);
        addComponent(LimeXMLNames.AUDIO_GENRE, genreCheckBox, genreComboBox);
        
        if (title != null) {
            titleTextField.setText(title);
        }
        
        if (artist != null) {
            artistTextField.setText(artist);
        }
        
        if (album != null) {
            albumTextField.setText(album);
        }
        
        if (year != null) {
            yearTextField.setText(year);
        }
        
        if (track != null) {
            trackTextField.setText(track);
        }
        
        if (language != null) {
            languageTextField.setText(language);
        }
        
        if (comments != null) {
            commentsTextArea.setText(comments);
            commentsTextArea.setCaretPosition(0);
        }    
        
        LimeXMLSchema schema = getSchema();
        List enums = schema.getEnumerationFields();
        Iterator it = enums.iterator();
        while(it.hasNext()) {
            SchemaFieldInfo infoField = (SchemaFieldInfo)it.next();
            String currField = infoField.getCanonicalizedFieldName();

            if (currField.equals(LimeXMLNames.AUDIO_GENRE)) {

                ArrayList values = new ArrayList();
                values.add(0, new ComboBoxValue());
                addEnums(infoField.getEnumerationList(), values);

                int index = 0;

                if (genre != null && !genre.equals("")) {
                    ComboBoxValue value = new ComboBoxValue(genre);
                    if (!values.contains(value))
                        values.add(value);
                }

                Object[] arr = (Object[])values.toArray(new Object[0]);
                Arrays.sort(arr);
                if(genre != null && !genre.equals(""))
                    index = Arrays.asList(arr).indexOf(new ComboBoxValue(genre));
                
                genreComboBox.setModel(new DefaultComboBoxModel(arr));
                genreComboBox.setSelectedIndex(index);
                genreComboBox.setMaximumRowCount(15);
                
            } else if (currField.equals(LimeXMLNames.AUDIO_TYPE)) {

                ArrayList values = new ArrayList();
                values.add(0, new ComboBoxValue());
                addEnums(infoField.getEnumerationList(), values);

                int index = (type != null) ? values.indexOf(type) : 0;

                Object[] arr = (Object[])values.toArray(new Object[0]);
                Arrays.sort(arr);
                typeComboBox.setModel(new DefaultComboBoxModel(arr));
                typeComboBox.setSelectedIndex(index);
            }
        }
        
        titleTextField.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent evt) {
                titleEdited = true;
            }
        });
        setCheckBoxesSelected(false);
    }
    
    private void initLabels() {
        titleLabel.setText(MetaEditorUtil.getStringResource(LimeXMLNames.AUDIO_TITLE));
        artistLabel.setText(MetaEditorUtil.getStringResource(LimeXMLNames.AUDIO_ARTIST));
        albumLabel.setText(MetaEditorUtil.getStringResource(LimeXMLNames.AUDIO_ALBUM));
        yearLabel.setText(MetaEditorUtil.getStringResource(LimeXMLNames.AUDIO_YEAR));
        trackLabel.setText(MetaEditorUtil.getStringResource(LimeXMLNames.AUDIO_TRACK));
        languageLabel.setText(MetaEditorUtil.getStringResource(LimeXMLNames.AUDIO_LANGUAGE));
        commentsLabel.setText(MetaEditorUtil.getStringResource(LimeXMLNames.AUDIO_COMMENTS));
        genreLabel.setText(MetaEditorUtil.getStringResource(LimeXMLNames.AUDIO_GENRE));
        typeLabel.setText(MetaEditorUtil.getStringResource(LimeXMLNames.AUDIO_TYPE));
    }
    
    public void prepareSave() {
        if (title != null) {
            String text = titleTextField.getText().trim();
            if (text.equals("")) {
                titleTextField.setText(title);
            }
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;
        javax.swing.JPanel spacer1;
        javax.swing.JPanel spacer2;
        javax.swing.JPanel spacer3;
        javax.swing.JPanel spacer4;

        titleLabel = new javax.swing.JLabel();
        titleTextField = new com.limegroup.gnutella.gui.LimeTextField();
        artistLabel = new javax.swing.JLabel();
        artistTextField = new com.limegroup.gnutella.gui.LimeTextField();
        albumLabel = new javax.swing.JLabel();
        albumTextField = new com.limegroup.gnutella.gui.LimeTextField();
        commentsLabel = new javax.swing.JLabel();
        commentsScrollPane = new javax.swing.JScrollPane();
        commentsTextArea = new javax.swing.JTextArea();
        genreLabel = new javax.swing.JLabel();
        genreComboBox = new javax.swing.JComboBox();
        yearLabel = new javax.swing.JLabel();
        trackLabel = new javax.swing.JLabel();
        yearCheckBox = new javax.swing.JCheckBox();
        artistCheckBox = new javax.swing.JCheckBox();
        albumCheckBox = new javax.swing.JCheckBox();
        commentsCheckBox = new javax.swing.JCheckBox();
        genreCheckBox = new javax.swing.JCheckBox();
        trackCheckBox = new javax.swing.JCheckBox();
        languageLabel = new javax.swing.JLabel();
        languageTextField = new com.limegroup.gnutella.gui.LimeTextField();
        languageCheckBox = new javax.swing.JCheckBox();
        typeLabel = new javax.swing.JLabel();
        typeComboBox = new javax.swing.JComboBox();
        typeCheckBox = new javax.swing.JCheckBox();
        yearTextField = new com.limegroup.gnutella.gui.LimeTextField();
        trackTextField = new com.limegroup.gnutella.gui.LimeTextField();
        spacer1 = new javax.swing.JPanel();
        spacer2 = new javax.swing.JPanel();
        spacer3 = new javax.swing.JPanel();
        spacer4 = new javax.swing.JPanel();

        setLayout(new java.awt.GridBagLayout());

        setNextFocusableComponent(titleTextField);
        setOpaque(false);
        titleLabel.setText("TITLE");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 300;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 0, 0);
        add(titleLabel, gridBagConstraints);

        titleTextField.setNextFocusableComponent(artistTextField);
        titleTextField.setPreferredSize(new java.awt.Dimension(4, 22));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(titleTextField, gridBagConstraints);

        artistLabel.setText("ARTIST");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 300;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 0, 0);
        add(artistLabel, gridBagConstraints);

        artistTextField.setNextFocusableComponent(yearTextField);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(artistTextField, gridBagConstraints);

        albumLabel.setText("ALBUM");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 0, 0);
        add(albumLabel, gridBagConstraints);

        albumTextField.setNextFocusableComponent(trackTextField);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(albumTextField, gridBagConstraints);

        commentsLabel.setText("COMMENTS");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 0, 0);
        add(commentsLabel, gridBagConstraints);

        commentsScrollPane.setHorizontalScrollBarPolicy(javax.swing.JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        commentsScrollPane.setPreferredSize(new java.awt.Dimension(22, 22));
        commentsTextArea.setLineWrap(true);
        commentsTextArea.setWrapStyleWord(true);
        commentsTextArea.setMinimumSize(new java.awt.Dimension(8, 13));
        commentsTextArea.setNextFocusableComponent(genreComboBox);
        commentsTextArea.setPreferredSize(new java.awt.Dimension(8, 13));
        commentsScrollPane.setViewportView(commentsTextArea);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipady = 80;
        add(commentsScrollPane, gridBagConstraints);

        genreLabel.setText("GENRE");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 0, 0);
        add(genreLabel, gridBagConstraints);

        genreComboBox.setEditable(true);
        genreComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "", "GENRE1", "GENRE2", "GENRE3" }));
        genreComboBox.setMinimumSize(new java.awt.Dimension(160, 22));
        genreComboBox.setNextFocusableComponent(this);
        genreComboBox.setPreferredSize(new java.awt.Dimension(160, 22));
        genreComboBox.setAutoscrolls(true);
        genreComboBox.setOpaque(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(genreComboBox, gridBagConstraints);

        yearLabel.setText("YEAR");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 0, 0);
        add(yearLabel, gridBagConstraints);

        trackLabel.setText("TRACK");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 0, 0);
        add(trackLabel, gridBagConstraints);

        yearCheckBox.setFocusPainted(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        add(yearCheckBox, gridBagConstraints);

        artistCheckBox.setFocusPainted(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        add(artistCheckBox, gridBagConstraints);

        albumCheckBox.setFocusPainted(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        add(albumCheckBox, gridBagConstraints);

        commentsCheckBox.setFocusPainted(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        add(commentsCheckBox, gridBagConstraints);

        genreCheckBox.setFocusPainted(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 11;
        add(genreCheckBox, gridBagConstraints);

        trackCheckBox.setFocusPainted(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 5;
        add(trackCheckBox, gridBagConstraints);

        languageLabel.setText("LANGUAGE");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 0, 0);
        add(languageLabel, gridBagConstraints);

        languageTextField.setNextFocusableComponent(typeComboBox);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(languageTextField, gridBagConstraints);

        languageCheckBox.setFocusPainted(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        add(languageCheckBox, gridBagConstraints);

        typeLabel.setText("TYPE");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 0, 0);
        add(typeLabel, gridBagConstraints);

        typeComboBox.setMaximumRowCount(15);
        typeComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "", "TYPE1", "TYPE2", "TYPE3" }));
        typeComboBox.setNextFocusableComponent(commentsTextArea);
        typeComboBox.setOpaque(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(typeComboBox, gridBagConstraints);

        typeCheckBox.setFocusPainted(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 7;
        add(typeCheckBox, gridBagConstraints);

        yearTextField.setNextFocusableComponent(albumTextField);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(yearTextField, gridBagConstraints);

        trackTextField.setNextFocusableComponent(languageTextField);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(trackTextField, gridBagConstraints);

        spacer1.setMaximumSize(new java.awt.Dimension(22, 10));
        spacer1.setMinimumSize(new java.awt.Dimension(22, 10));
        spacer1.setPreferredSize(new java.awt.Dimension(22, 10));
        spacer1.setOpaque(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        add(spacer1, gridBagConstraints);

        spacer2.setMaximumSize(new java.awt.Dimension(22, 10));
        spacer2.setMinimumSize(new java.awt.Dimension(22, 10));
        spacer2.setPreferredSize(new java.awt.Dimension(22, 10));
        spacer2.setOpaque(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        add(spacer2, gridBagConstraints);

        spacer3.setMinimumSize(new java.awt.Dimension(22, 10));
        spacer3.setPreferredSize(new java.awt.Dimension(22, 10));
        spacer3.setOpaque(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 0;
        add(spacer3, gridBagConstraints);

        spacer4.setMinimumSize(new java.awt.Dimension(22, 10));
        spacer4.setPreferredSize(new java.awt.Dimension(22, 10));
        spacer4.setOpaque(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 12;
        add(spacer4, gridBagConstraints);

    }//GEN-END:initComponents
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox albumCheckBox;
    private javax.swing.JLabel albumLabel;
    private com.limegroup.gnutella.gui.LimeTextField albumTextField;
    private javax.swing.JCheckBox artistCheckBox;
    private javax.swing.JLabel artistLabel;
    private com.limegroup.gnutella.gui.LimeTextField artistTextField;
    private javax.swing.JCheckBox commentsCheckBox;
    private javax.swing.JLabel commentsLabel;
    private javax.swing.JScrollPane commentsScrollPane;
    private javax.swing.JTextArea commentsTextArea;
    private javax.swing.JCheckBox genreCheckBox;
    private javax.swing.JComboBox genreComboBox;
    private javax.swing.JLabel genreLabel;
    private javax.swing.JCheckBox languageCheckBox;
    private javax.swing.JLabel languageLabel;
    private com.limegroup.gnutella.gui.LimeTextField languageTextField;
    private javax.swing.JLabel titleLabel;
    private com.limegroup.gnutella.gui.LimeTextField titleTextField;
    private javax.swing.JCheckBox trackCheckBox;
    private javax.swing.JLabel trackLabel;
    private com.limegroup.gnutella.gui.LimeTextField trackTextField;
    private javax.swing.JCheckBox typeCheckBox;
    private javax.swing.JComboBox typeComboBox;
    private javax.swing.JLabel typeLabel;
    private javax.swing.JCheckBox yearCheckBox;
    private javax.swing.JLabel yearLabel;
    private com.limegroup.gnutella.gui.LimeTextField yearTextField;
    // End of variables declaration//GEN-END:variables

}
