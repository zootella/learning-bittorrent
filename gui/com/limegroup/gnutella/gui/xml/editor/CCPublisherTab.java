package com.limegroup.gnutella.gui.xml.editor;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import com.limegroup.gnutella.ErrorService;
import com.limegroup.gnutella.FileDesc;
import com.limegroup.gnutella.FileEventListener;
import com.limegroup.gnutella.archive.Archives;
import com.limegroup.gnutella.archive.Contribution;
import com.limegroup.gnutella.archive.DescriptionTooShortException;
import com.limegroup.gnutella.gui.GUIMediator;
import com.limegroup.gnutella.gui.SizedPasswordField;
import com.limegroup.gnutella.gui.SizedTextField;
import com.limegroup.gnutella.gui.URLLabel;
import com.limegroup.gnutella.licenses.CCConstants;
import com.limegroup.gnutella.licenses.License;
import com.limegroup.gnutella.licenses.PublishedCCLicense;
import com.limegroup.gnutella.settings.InternetArchiveSetting;
import com.limegroup.gnutella.settings.SharingSettings;
import com.limegroup.gnutella.util.NameValue;
import com.limegroup.gnutella.xml.LimeXMLDocument;
import com.limegroup.gnutella.xml.LimeXMLNames;

/**
 * This class provides the ability to publish an audio file with a the 
 * Creative Commons license. 
 */
public class CCPublisherTab extends AbstractMetaEditorPanel {
	
	private static final long serialVersionUID = 4529973794308191398L;

	private final int DIALOG_WIDTH = 600;
	
	private final JTextField COPYRIGHT_HOLDER = new SizedTextField(24);
	
	private final JLabel COPYRIGHT_HOLDER_LABEL = 
		new JLabel(GUIMediator.getStringResource("CC_PUBLISHER_COPYRIGHT_HOLDER_LABEL"));
	
	private final JTextField WORK_TITLE = new SizedTextField(24);
	
	private final JLabel WORK_TITLE_LABEL= 
		new JLabel(GUIMediator.getStringResource("CC_PUBLISHER_WORK_TITLE_LABEL"));
	
	private final JTextField COPYRIGHT_YEAR = new SizedTextField(6);
	
	private final JLabel COPYRIGHT_YEAR_LABEL= 
		new JLabel(GUIMediator.getStringResource("CC_PUBLISHER_COPYRIGHT_YEAR_LABEL"));
	
	private final JTextField DESCRIPTION = new SizedTextField(24);
	
	private final JLabel DESCRIPTION_LABEL= 
		new JLabel(GUIMediator.getStringResource("CC_PUBLISHER_DESCRIPTION_LABEL"));
	
	private final JLabel REMOVE_LICENSE_LABEL=
		new JLabel(GUIMediator.getStringResource("CC_PUBLISHER_REMOVE_LICENSE_LABEL"));
	
	/**
	 * The Verification URL field
	 */
	private final JTextField VERIFICATION_URL_FIELD = new SizedTextField(20);
	
	private final JLabel VERIFICATION_URL_LABEL = 
		new JLabel(GUIMediator.getStringResource("CC_PUBLISHER_LICENSE_VERIFICATION_URL"));
	
	private final String	VERIFICATION_ARCHIVE = 
		GUIMediator.getStringResource("CC_PUBLISHER_LICENSE_VERIFICATION_ARCHIVE");
	
	private final String	VERIFICATION_SELF = 
		GUIMediator.getStringResource("CC_PUBLISHER_LICENSE_VERIFICATION_SELF");
	
	private final String WARNING_MESSAGE_CREATE = GUIMediator.getStringResource("CC_PUBLISHER_WARNING_CREATE");
	
	private final String WARNING_MESSAGE_MODIFY = GUIMediator.getStringResource("CC_PUBLISHER_WARNING_MODIFY");
		
	private final JCheckBox WARNING_CHECKBOX = new JCheckBox("<html>"+WARNING_MESSAGE_CREATE+"</html>");
	
	private final JLabel INTRO_LABEL = new JLabel(GUIMediator.getStringResource("CC_PUBLISHER_INTRO"));
	
	private final JCheckBox LICENSE_ALLOWCOM = new JCheckBox(GUIMediator.getStringResource("CC_PUBLISHER_LICENSE_ALLOWCOM_LABEL"));
	
	private final JLabel LICENSE_ALLOWMOD_LABEL = 
		new JLabel(GUIMediator.getStringResource("CC_PUBLISHER_LICENSE_ALLOWMOD_LABEL"));
	
	private final String ALLOWMOD_SHAREALIKE = GUIMediator.getStringResource("CC_PUBLISHER_LICENSE_ALLOWMOD_SHAREALIKE");
	
	private final String ALLOWMOD_YES = GUIMediator.getStringResource("YES");
	
	private final String ALLOWMOD_NO = GUIMediator.getStringResource("NO");
	
	private final JComboBox LICENSE_ALLOWMOD_BOX = new JComboBox(new String[] {
			ALLOWMOD_YES,
			ALLOWMOD_SHAREALIKE,
			ALLOWMOD_NO
	});
	
	private final JLabel CC_INTRO_URL_LABEL = 
		new URLLabel(SharingSettings.CREATIVE_COMMONS_INTRO_URL.getValue(),
				GUIMediator.getStringResource("CC_PUBLISHER_CC_INTRO_URL_LABEL"));
	
	private final JLabel CC_VERIFICATION_WHATIS_LABEL =
		new URLLabel(SharingSettings.CREATIVE_COMMONS_VERIFICATION_URL.getValue(),
				GUIMediator.getStringResource("CC_PUBLISHER_CC_VERIFICATION_URL_LABEL"));
	
	private final JRadioButton ARCHIVE_VERIFICATION = new JRadioButton(VERIFICATION_ARCHIVE);

	private final JRadioButton SELF_VERIFICATION = new JRadioButton(VERIFICATION_SELF);
	
	private final JLabel ARCHIVE_USERNAME_LABEL = 
		new JLabel(GUIMediator.getStringResource("CC_PUBLISHER_ARCHIVE_USERNAME_LABEL"));
	
	private final JLabel ARCHIVE_PASSWORD_LABEL =
		new JLabel(GUIMediator.getStringResource("CC_PUBLISHER_ARCHIVE_PASSWORD_LABEL"));
	
	private final JTextField ARCHIVE_USERNAME_FIELD = new SizedTextField(6);
	
	private final JTextField ARCHIVE_PASSWORD_FIELD = new SizedPasswordField(6);
	
	private final MouseListener _removeLicenseListener = new RemoveLabelMouseListener();
	
	private LimeXMLDocument _xmlDoc;
	
	private FileDesc _fd;
	
	private boolean _licenseRemoved = false;
	
	private JPanel _warningPanel = new JPanel(new GridBagLayout());
	
	private ButtonGroup _verificationURLgroup;
	
	private Contribution _contribution;
	
	/**
	 * Creates a new instance of CCPublisherTab.
	 * 
	 * @param fd The file descriptor
	 * @param doc The meta data of the file to publish
	 */
	public CCPublisherTab(FileDesc fd, LimeXMLDocument doc) {
		_xmlDoc = doc;
		_fd = fd;
		init();
		initInfo();
		updateDisplay();
	}
	
	private void init() {
		setName(GUIMediator.getStringResource("CC_PUBLISHER_TITLE"));
		setLayout(new GridBagLayout());
		GridBagConstraints mainConstraints = new GridBagConstraints();
		//Warning panel
		_warningPanel.setOpaque(false);
		GridBagConstraints warnConstraints = new GridBagConstraints();
		warnConstraints.anchor = GridBagConstraints.WEST;
		_warningPanel.add(INTRO_LABEL,warnConstraints);
		warnConstraints.gridx = 1;
		_warningPanel.add(CC_INTRO_URL_LABEL,warnConstraints);
		warnConstraints.gridx=0;
		warnConstraints.gridy = 1;
		warnConstraints.gridwidth=2;
		WARNING_CHECKBOX.addItemListener(new WarningCheckBoxListener());
		WARNING_CHECKBOX.setOpaque(false);
		WARNING_CHECKBOX.setPreferredSize(new Dimension(DIALOG_WIDTH,50));
		_warningPanel.add(WARNING_CHECKBOX,warnConstraints);
		warnConstraints.anchor = GridBagConstraints.CENTER;
		warnConstraints.gridy = 2;
		REMOVE_LICENSE_LABEL.setForeground(Color.BLUE);
		REMOVE_LICENSE_LABEL.addMouseListener(_removeLicenseListener);
		REMOVE_LICENSE_LABEL.setVisible(false);
		_warningPanel.add(REMOVE_LICENSE_LABEL,warnConstraints);
		mainConstraints.anchor = GridBagConstraints.WEST;
		add(_warningPanel,mainConstraints);
		mainConstraints.gridy=1;
		JSeparator separator = new JSeparator();
		separator.setPreferredSize(new Dimension(DIALOG_WIDTH,2));
		add(separator,mainConstraints);
		
		//license details
		JPanel licenseDetailsPanel = new JPanel(new GridBagLayout());
		GridBagConstraints licenseConstraints = new GridBagConstraints();
		licenseDetailsPanel.setOpaque(false);
		licenseConstraints.anchor = GridBagConstraints.WEST;
		licenseConstraints.insets = new Insets(2,2,2,2);
		licenseDetailsPanel.add(COPYRIGHT_HOLDER_LABEL,licenseConstraints);
		licenseConstraints.gridy=1;
		licenseDetailsPanel.add(COPYRIGHT_HOLDER,licenseConstraints);
		licenseConstraints.gridy=2;
		licenseDetailsPanel.add(WORK_TITLE_LABEL,licenseConstraints);
		licenseConstraints.gridy=3;
		licenseDetailsPanel.add(WORK_TITLE,licenseConstraints);
		licenseConstraints.gridx=1;
		licenseConstraints.gridy=0;
		licenseConstraints.insets = new Insets(2,40,2,2);
		licenseDetailsPanel.add(COPYRIGHT_YEAR_LABEL,licenseConstraints);
		licenseConstraints.gridy=1;
		licenseDetailsPanel.add(COPYRIGHT_YEAR,licenseConstraints);
		licenseConstraints.gridy=2;
		licenseDetailsPanel.add(DESCRIPTION_LABEL,licenseConstraints);
		licenseConstraints.gridy=3;
		licenseDetailsPanel.add(DESCRIPTION,licenseConstraints);
		
		//licensing options
		licenseConstraints.insets = new Insets(10,2,10,2);
		licenseConstraints.gridx=0;
		licenseConstraints.gridy=4;
		LICENSE_ALLOWCOM.setOpaque(false);
		LICENSE_ALLOWCOM.setHorizontalTextPosition(SwingConstants.LEFT);
		licenseDetailsPanel.add(LICENSE_ALLOWCOM,licenseConstraints);
		JPanel licenseAllowModPanel = new JPanel(new GridBagLayout());
		GridBagConstraints constr = new GridBagConstraints();
		licenseAllowModPanel.setOpaque(false);
		constr.anchor = GridBagConstraints.EAST;
		licenseAllowModPanel.add(LICENSE_ALLOWMOD_LABEL,constr);
		constr.gridx=1;
		constr.insets = new Insets(0,5,0,0);
		LICENSE_ALLOWMOD_BOX.setOpaque(false);
		licenseAllowModPanel.add(LICENSE_ALLOWMOD_BOX,constr);
		licenseConstraints.gridx=1;
		licenseConstraints.insets = new Insets(10,40,10,2);
		licenseDetailsPanel.add(licenseAllowModPanel,licenseConstraints);
		mainConstraints.gridy=2;
		add(licenseDetailsPanel,mainConstraints);
		
		//Verification URL
		separator = new JSeparator();
		separator.setPreferredSize(new Dimension(DIALOG_WIDTH,2));
		mainConstraints.gridy=3;
		add(separator,mainConstraints);
		
		JPanel verificationPanel = new JPanel(new GridBagLayout());
		licenseConstraints = new GridBagConstraints();
		licenseConstraints.anchor = GridBagConstraints.WEST;
		licenseConstraints.insets = new Insets(5,0,2,2);
		licenseConstraints.gridwidth=2;
		JPanel panel = new JPanel(new GridBagLayout());
		constr = new GridBagConstraints();
		panel.add(VERIFICATION_URL_LABEL,constr);
		constr.gridx=1;
		constr.insets=new Insets(0,5,0,0);
		panel.add(CC_VERIFICATION_WHATIS_LABEL,constr);
		verificationPanel.add(panel,licenseConstraints);
		licenseConstraints.gridwidth=1;
		licenseConstraints.insets = new Insets(2,2,2,2);
		licenseConstraints.gridy=1;
		ARCHIVE_VERIFICATION.setOpaque(false);
		ARCHIVE_VERIFICATION.addActionListener(new VerificationURLButtonListener());
		SELF_VERIFICATION.setOpaque(false);
		SELF_VERIFICATION.addActionListener(new VerificationURLButtonListener());
		_verificationURLgroup = new ButtonGroup();
		_verificationURLgroup.add(ARCHIVE_VERIFICATION);
		_verificationURLgroup.add(SELF_VERIFICATION);
		_verificationURLgroup.setSelected(ARCHIVE_VERIFICATION.getModel(),true);
		licenseConstraints.insets = new Insets(10,2,2,2);
		verificationPanel.add(ARCHIVE_VERIFICATION,licenseConstraints);
		licenseConstraints.insets = new Insets(2,2,2,2);
		licenseConstraints.gridy=2;
		verificationPanel.add(SELF_VERIFICATION,licenseConstraints);
		licenseConstraints.gridy=1;
		licenseConstraints.gridx=1;
		licenseConstraints.insets = new Insets(10,2,2,2);
		licenseConstraints.gridwidth = GridBagConstraints.REMAINDER;
		panel = new JPanel(new GridBagLayout());
		constr = new GridBagConstraints();
		panel.add(ARCHIVE_USERNAME_LABEL,constr);
		constr.gridx=1;
		constr.insets= new Insets(0,2,0,0);
		panel.add(ARCHIVE_USERNAME_FIELD,constr);
		constr.gridx=2;
		constr.insets= new Insets(0,10,0,0);
		panel.add(ARCHIVE_PASSWORD_LABEL,constr);
		constr.insets= new Insets(0,2,0,0);
		constr.gridx=3;
		panel.add(ARCHIVE_PASSWORD_FIELD,constr);
		verificationPanel.add(panel,licenseConstraints);
		licenseConstraints.gridwidth=0;
		licenseConstraints.gridy=2;
		licenseConstraints.insets = new Insets(2,2,2,2);
		panel = new JPanel(new GridBagLayout());
		constr = new GridBagConstraints();
		VERIFICATION_URL_FIELD.setText("http://");
		constr.gridx=1;
		panel.add(VERIFICATION_URL_FIELD,constr);
		verificationPanel.add(panel,licenseConstraints);
		setOpaque(false);
		mainConstraints.gridy=4;
		add(verificationPanel,mainConstraints);
	}
	
	/**
	 * Initializes the fieds with the file's Meta Data 
	 * only if a license does not exist.If a license exists, it populates 
	 * the verification URL field and the license distribution details.
	 */
	private void initInfo() {
		License license = _fd.getLicense();
		ARCHIVE_USERNAME_FIELD.setText(InternetArchiveSetting.INTERNETARCHIVE_USERNAME.getValue());
		ARCHIVE_PASSWORD_FIELD.setText(InternetArchiveSetting.INTERNETARCHIVE_PASS.getValue());
		if(license != null) {
			WARNING_CHECKBOX.setText("<html>"+WARNING_MESSAGE_MODIFY+"</html>");
			REMOVE_LICENSE_LABEL.setVisible(true);
			_warningPanel.setPreferredSize(new Dimension(DIALOG_WIDTH,80));
			if(license.getLicenseURI()!=null) {
				VERIFICATION_URL_FIELD.setText(license.getLicenseURI().toString());
				_verificationURLgroup.setSelected(SELF_VERIFICATION.getModel(),true);
				updateVerification();
			}
			String licenseDeed = license.getLicenseDeed(_fd.getSHA1Urn()).toString();
			if(licenseDeed!=null) {
				if(licenseDeed.equals(CCConstants.ATTRIBUTION_NON_COMMERCIAL_NO_DERIVS_URI)) {
					LICENSE_ALLOWCOM.setSelected(false);
					LICENSE_ALLOWMOD_BOX.setSelectedItem(ALLOWMOD_NO);
				}
				else if(licenseDeed.equals(CCConstants.ATTRIBUTION_NO_DERIVS_URI)) {
					LICENSE_ALLOWCOM.setSelected(true);
					LICENSE_ALLOWMOD_BOX.setSelectedItem(ALLOWMOD_NO);
				}
				else if(licenseDeed.equals(CCConstants.ATTRIBUTION_NON_COMMERCIAL_URI)) {
					LICENSE_ALLOWCOM.setSelected(false);
					LICENSE_ALLOWMOD_BOX.setSelectedItem(ALLOWMOD_YES);
				}
				else if(licenseDeed.equals(CCConstants.ATTRIBUTION_SHARE_NON_COMMERCIAL_URI)) {
					LICENSE_ALLOWCOM.setSelected(false);
					LICENSE_ALLOWMOD_BOX.setSelectedItem(ALLOWMOD_SHAREALIKE);
				}
				else if(licenseDeed.equals(CCConstants.ATTRIBUTION_SHARE_URI)) {
					LICENSE_ALLOWCOM.setSelected(true);
					LICENSE_ALLOWMOD_BOX.setSelectedItem(ALLOWMOD_SHAREALIKE);
				}
				else {
					LICENSE_ALLOWCOM.setSelected(true);
					LICENSE_ALLOWMOD_BOX.setSelectedItem(ALLOWMOD_YES);
				}
			}
		}
		//license does not exist and file has XML doc
		else if(_xmlDoc != null) {
			COPYRIGHT_HOLDER.setText(_xmlDoc.getValue(LimeXMLNames.AUDIO_ARTIST));
			COPYRIGHT_YEAR.setText(_xmlDoc.getValue(LimeXMLNames.AUDIO_YEAR));
			WORK_TITLE.setText(_xmlDoc.getValue(LimeXMLNames.AUDIO_TITLE));
		}	
	}
	
	private void updateDisplay() {
		WORK_TITLE.setEnabled(WARNING_CHECKBOX.isSelected());
		WORK_TITLE_LABEL.setEnabled(WARNING_CHECKBOX.isSelected());
		COPYRIGHT_HOLDER.setEnabled(WARNING_CHECKBOX.isSelected());
		COPYRIGHT_HOLDER_LABEL.setEnabled(WARNING_CHECKBOX.isSelected());
		COPYRIGHT_YEAR.setEnabled(WARNING_CHECKBOX.isSelected());
		COPYRIGHT_YEAR_LABEL.setEnabled(WARNING_CHECKBOX.isSelected());
		DESCRIPTION.setEnabled(WARNING_CHECKBOX.isSelected());
		DESCRIPTION_LABEL.setEnabled(WARNING_CHECKBOX.isSelected());
		LICENSE_ALLOWMOD_LABEL.setEnabled(WARNING_CHECKBOX.isSelected());
		VERIFICATION_URL_FIELD.setEnabled(WARNING_CHECKBOX.isSelected());
		VERIFICATION_URL_LABEL.setEnabled(WARNING_CHECKBOX.isSelected());
		CC_VERIFICATION_WHATIS_LABEL.setEnabled(WARNING_CHECKBOX.isSelected());
		LICENSE_ALLOWCOM.setEnabled(WARNING_CHECKBOX.isSelected());
		LICENSE_ALLOWMOD_BOX.setEnabled(WARNING_CHECKBOX.isSelected());
		SELF_VERIFICATION.setEnabled(WARNING_CHECKBOX.isSelected());
		ARCHIVE_VERIFICATION.setEnabled(WARNING_CHECKBOX.isSelected());
		updateVerification();
	}
	
	private void updateVerification() {
		boolean archiveSelected = ARCHIVE_VERIFICATION.getModel().isSelected();
		ARCHIVE_PASSWORD_FIELD.setEnabled(archiveSelected && WARNING_CHECKBOX.isSelected());
		ARCHIVE_PASSWORD_LABEL.setEnabled(archiveSelected && WARNING_CHECKBOX.isSelected());
		ARCHIVE_USERNAME_FIELD.setEnabled(archiveSelected && WARNING_CHECKBOX.isSelected());
		ARCHIVE_USERNAME_LABEL.setEnabled(archiveSelected && WARNING_CHECKBOX.isSelected());
		VERIFICATION_URL_FIELD.setEnabled(!archiveSelected && WARNING_CHECKBOX.isSelected());
	}
	
	/**
	 * Reserves an Internet Archive identifier 
	 * 
	 * @return true if identifier was reserved correctly, false otherwise
	 */
	public boolean reserveIdentifier() {
		if(ARCHIVE_VERIFICATION.getModel().isSelected() && !_licenseRemoved) {
			String username = ARCHIVE_USERNAME_FIELD.getText();
			String password = ARCHIVE_PASSWORD_FIELD.getText();
			String title = WORK_TITLE.getText();
			String description = DESCRIPTION.getText();
			try {
				_contribution = Archives.createContribution(
						username,
						password,
						title,
						description,
						Archives.MEDIA_AUDIO,
						Archives.COLLECTION_OPENSOURCE_AUDIO,
						Archives.TYPE_SOUND);
			} catch (DescriptionTooShortException e) {
                GUIMediator.showFormattedError(
                        "ERROR_CCPUBLISHER_DESCRIPTION_START",
                        new Object[] {String.valueOf(e.getMinWords())});
                return false;
            }
			final String id = title;
			InternetArchiveIdentifierRetriever idRetriever = 
				new InternetArchiveIdentifierRetriever(_contribution,id);
			idRetriever.reserveIdentifier();
			if(_contribution.getVerificationUrl()==null)return false;
			else return true;
		}
		return true;
	}
	
	/**
	 * Checks the validity of the input fields. 
	 * 
	 * @return true if the input is valid
	 */
	private boolean inputValid() {
		String holder = COPYRIGHT_HOLDER.getText();
		String year = COPYRIGHT_YEAR.getText();
		String title = WORK_TITLE.getText();
		String url = null;
		if(holder.equals("")) {
			GUIMediator.showError("ERROR_CCPUBLISHER_MISSING_HOLDER");
			return false;
		}
		else if(year.equals("")) {
			GUIMediator.showError("ERROR_CCPUBLISHER_MISSING_YEAR");
			return false;
		}
		else if(title.equals("")) {
			GUIMediator.showError("ERROR_CCPUBLISHER_MISSING_TITLE");
			return false;
		}
		if(ARCHIVE_VERIFICATION.getModel().isSelected()) {
			String username = ARCHIVE_USERNAME_FIELD.getText();
			String password = ARCHIVE_PASSWORD_FIELD.getText();
			if(username==null || username.equals("") || password == null || password.equals("")) {
				GUIMediator.showError("ERROR_CCPUBLISHER_MISSING_ARCHIVE_LOGIN");
				return false;
			}
		}
		else{
			url = VERIFICATION_URL_FIELD.getText();
			if(url.equals("") || !url.startsWith("http://") || url.length()<8) {
				GUIMediator.showError("ERROR_CCPUBLISHER_MISSING_URL");
				VERIFICATION_URL_FIELD.setText("http://");
				return false;
			}
			try {
				new URL(url);
			}catch(MalformedURLException invalidURL) {
				GUIMediator.showError("ERROR_CCPUBLISHER_URL");
				return false;
			}
			try {
				Integer.parseInt(year);
			}catch(NumberFormatException badDate) {
				GUIMediator.showError("ERROR_CCPUBLISHER_DATE");
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Checks the validity of the input fields and if the license RDF has
	 * allready bean generated and is consistent.
	 * 
	 * @return true if input is valid
	 */
	public boolean checkInput() {
		if(WARNING_CHECKBOX.isSelected() && !_licenseRemoved && !inputValid())return false;
		else return true;
	}
	
	public void removeLicense() {
		int answer = GUIMediator.showYesNoMessage("CC_PUBLISHER_REMOVELICENSE_LABEL");
		if(answer == GUIMediator.YES_OPTION) {
			this.setVisible(false);
			_licenseRemoved=true;
			if(WARNING_CHECKBOX.isSelected())WARNING_CHECKBOX.doClick();
			INTRO_LABEL.setVisible(false);
			CC_INTRO_URL_LABEL.setVisible(false);
			WARNING_CHECKBOX.setVisible(false);
			REMOVE_LICENSE_LABEL.setText(GUIMediator.getStringResource("CC_PUBLISHER_LICENSEREMOVED_LABEL"));
			REMOVE_LICENSE_LABEL.setForeground(Color.BLACK);
			REMOVE_LICENSE_LABEL.removeMouseListener(_removeLicenseListener);
			REMOVE_LICENSE_LABEL.addMouseListener(new MouseAdapter() {
				public void mouseEntered(MouseEvent e) {
					e.getComponent().setCursor(Cursor.getDefaultCursor());
				}
			});
		}
	}
	
	public FileEventListener getFileEventListener() {
		if(_licenseRemoved) return null;
		if(ARCHIVE_VERIFICATION.getModel().isSelected()) 
			return new InternetArchiveUploader(_fd,_contribution);
		else 
			return new CCRDFOuptut(_fd,COPYRIGHT_HOLDER.getText(),
				WORK_TITLE.getText(),
				COPYRIGHT_YEAR.getText(),
				DESCRIPTION.getText(),
				VERIFICATION_URL_FIELD.getText(),
				getLicenseType());
	}
	
	
	private int getLicenseType(){
		int type = CCConstants.ATTRIBUTION;
		if(!LICENSE_ALLOWCOM.isSelected()) {
			type|=CCConstants.ATTRIBUTION_NON_COMMERCIAL;
		}
		String mod = (String)LICENSE_ALLOWMOD_BOX.getSelectedItem();
		if(mod.equals(ALLOWMOD_SHAREALIKE)) {
			type|=CCConstants.ATTRIBUTION_SHARE;
		}
		else if(mod.equals(ALLOWMOD_NO)) {
			type|=CCConstants.ATTRIBUTION_NO_DERIVS;
		}
		return type;
	}
	
	/**
	 * Validates the input and returns an ArrayList with the 
	 * <name,value> MetaData of the license.
	 * 
	 * @return an ArrayList with the <name,value> tuples for the license and licensetype.
	 */
	public List getInput() {
		ArrayList valList = new ArrayList();
		if(_licenseRemoved) {
			valList.addAll(getPreviousValList());
			NameValue nameVal = new NameValue(LimeXMLNames.AUDIO_LICENSE,"no license");
			valList.add(nameVal);
			nameVal = new NameValue(LimeXMLNames.AUDIO_LICENSETYPE,"");
			valList.add(nameVal);
		}
		else if(WARNING_CHECKBOX.isSelected() && inputValid()) {
			String holder = COPYRIGHT_HOLDER.getText();
			String year = COPYRIGHT_YEAR.getText();
			String title = WORK_TITLE.getText();
			String description = DESCRIPTION.getText();
			int type = getLicenseType();
			String url = null;
			if(ARCHIVE_VERIFICATION.getModel().isSelected()) {
				InternetArchiveSetting.INTERNETARCHIVE_USERNAME.setValue(
						ARCHIVE_USERNAME_FIELD.getText());
				InternetArchiveSetting.INTERNETARCHIVE_PASS.setValue(
						ARCHIVE_PASSWORD_FIELD.getText());
				url= _contribution.getVerificationUrl();
			}
			else url = VERIFICATION_URL_FIELD.getText();
			valList.addAll(getPreviousValList());
			String embeddedLicense = PublishedCCLicense.getEmbeddableString(holder,title,year,url,description,type);
			if(embeddedLicense!=null) {
				NameValue nameVal = new NameValue(LimeXMLNames.AUDIO_LICENSE,embeddedLicense);
				valList.add(nameVal);
				nameVal = new NameValue(LimeXMLNames.AUDIO_LICENSETYPE,CCConstants.CC_URI_PREFIX);
				valList.add(nameVal);
			}
		}
		return valList;
	}
	
	private List getPreviousValList() {
		ArrayList valList = new ArrayList();
		if(_xmlDoc!=null) {
			for (Iterator iter = _xmlDoc.getNameValueSet().iterator(); iter.hasNext();) {
				Map.Entry oldNameVal = (Map.Entry) iter.next();
				String key = (String)oldNameVal.getKey();
				if(!key.equals(LimeXMLNames.AUDIO_LICENSE)&&!key.equals(LimeXMLNames.AUDIO_LICENSETYPE))
				valList.add(new NameValue((String)oldNameVal.getKey(),(String)oldNameVal.getValue()));
			}
		}
		return valList;
	}
	
	private class WarningCheckBoxListener implements ItemListener{
		public void itemStateChanged(ItemEvent e) {
			updateDisplay();
		}
	}
	
	private class RemoveLabelMouseListener extends MouseAdapter{
		public void mouseClicked(MouseEvent e) {
			removeLicense();
		}

		public void mouseEntered(MouseEvent e) {
			e.getComponent().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		}

		public void mouseExited(MouseEvent e) {
			 e.getComponent().setCursor(Cursor.getDefaultCursor());
		}
		
	}
	
	private class VerificationURLButtonListener implements ActionListener{

		public void actionPerformed(ActionEvent arg0) {
			updateVerification();
		}
	}
}
