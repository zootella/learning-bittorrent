package com.limegroup.gnutella.gui.menu;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

import com.limegroup.gnutella.RouterService;
import com.limegroup.gnutella.gui.AutoCompleteTextField;
import com.limegroup.gnutella.gui.GUIMediator;
import com.limegroup.gnutella.gui.GUIUtils;
import com.limegroup.gnutella.util.CommonUtils;

/**
 * Handles all of the contents of the file menu in the menu bar.
 */
//2345678|012345678|012345678|012345678|012345678|012345678|012345678|012345678|
final class FileMenu extends AbstractMenu {
	
	/**
	 * Creates a new <tt>FileMenu</tt>, using the <tt>key</tt> 
	 * argument for setting the locale-specific title and 
	 * accessibility text.
	 *
	 * @param key the key for locale-specific string resources unique
	 *            to the menu
	 */
	FileMenu(final String key) {
		super(key);
		MENU.add(getMenuItem("MENU_FILE_CONNECT", 
				new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				GUIMediator.instance().connect();
			}
		}));				
		MENU.add(getMenuItem("MENU_FILE_DISCONNECT",	
				new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				GUIMediator.instance().disconnect();
			}
		}));
		MENU.add(getMenuItem("MENU_FILE_OPEN_TORRENT",
				new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				GUIMediator.instance().openTorrent();
			}
		}));
		MENU.add(getMenuItem("MENU_FILE_DOWNLOAD_TORRENT",      
				new TorrentURLListener()));
		if(!CommonUtils.isMacOSX()) {
			MENU.addSeparator(); 
			MENU.add(getMenuItem("MENU_FILE_CLOSE", 
					new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					GUIMediator.close(false);
				}
			}));
		}
		setConnected(RouterService.isConnected() || RouterService.isConnecting());
	}

	/**
	 * Returns a new <tt>JMenuItem</tt> instance with all of the characteristics 
	 * specified in the arguments.
	 *
	 * @param key the key for obtaining locale-specific strings for both the
	 *  label and the accessible description of the menu item -- the key for the
	 *  accessible description is obtained by appending "_ACCESSIBLE" to the
	 *  end of the key for the label
	 * @param listener the <tt>ActionListener</tt> to use to respond to clicks 
	 *  on the menu item
	 * @return the new <tt>JMenuItem</tt> with the customized characteristics
	 *  specified in the arguments
	 */
	private JMenuItem getMenuItem(final String key, ActionListener listener) {
		String label = GUIMediator.getStringResource(key);
		String accessibleLabel = 
            GUIMediator.getStringResource(key + "_ACCESSIBLE");
		int mnemonic = getCodeForCharKey(key+"_MNEMONIC");
		JMenuItem menuItem = new JMenuItem(label, mnemonic);
		menuItem.getAccessibleContext().setAccessibleDescription(accessibleLabel);
		menuItem.addActionListener(listener);
		menuItem.setFont(AbstractMenu.FONT);
		return menuItem;
	}
	
	/**
	 * Sets whether or not we are currently connected or disconnected 
	 * from the network, enabling or disabling the correct menu items.
	 *
	 * @param connected specifies our connection status
	 */
	void setConnected(boolean connected) {
        MENU.getItem(0).setEnabled(!connected);
		MENU.getItem(1).setEnabled(connected);
	}
	
	private final class TorrentURLListener implements ActionListener {
		
		private JDialog dialog = null;
		
		private AutoCompleteTextField URL_INPUT = new AutoCompleteTextField(20);
		
		private JButton OK_BUTTON = new JButton(GUIMediator
				.getStringResource("GENERAL_OK_BUTTON_LABEL"));
		
		private JButton CANCEL_BUTTON = new JButton(GUIMediator
				.getStringResource("GENERAL_CANCEL_BUTTON_LABEL"));
		
		private void createDialog() {
			if (dialog != null)
				return;
			// 1. create modal dialog
			// Host: [ ]
			// [ OK ] [ Cancel ]
			dialog = new JDialog(
					GUIMediator.getAppFrame(),
					GUIMediator
					.getStringResource("MENU_FILE_OPEN_TORRENT_URL_DIALOG_TITLE"),
					true);
			JPanel jp = (JPanel) dialog.getContentPane();
			GUIUtils.addHideAction(jp);
			jp.setLayout(new GridBagLayout());
			GridBagConstraints gbc = new GridBagConstraints();
			
			// space between title bar and Host line
			gbc.gridwidth = 5;
			gbc.gridheight = 1;
			gbc.weightx = 1;
			gbc.weighty = 0;
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.gridx = 0;
			gbc.gridy = 0;
			jp.add(getHorizontalSpacer(), gbc);
			
			// lefthand side space
			gbc.gridwidth = 1;
			gbc.gridheight = 5;
			gbc.weightx = 0;
			gbc.weighty = 1;
			gbc.fill = GridBagConstraints.VERTICAL;
			gbc.gridx = 0;
			gbc.gridy = 1;
			jp.add(getVerticalSpacer(), gbc);
			
			// righthand side space
			gbc.gridwidth = 1;
			gbc.gridheight = 5;
			gbc.weightx = 0;
			gbc.weighty = 1;
			gbc.fill = GridBagConstraints.VERTICAL;
			gbc.gridx = 4;
			gbc.gridy = 1;
			jp.add(getVerticalSpacer(), gbc);
			
			// bottom spacer between buttons and window border
			gbc.gridwidth = 5;
			gbc.gridheight = 1;
			gbc.weightx = 1;
			gbc.weighty = 0;
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.gridx = 0;
			gbc.gridy = 6;
			jp.add(getHorizontalSpacer(), gbc);
			// host label
			gbc.gridwidth = 1;
			gbc.gridheight = 1;
			gbc.weightx = 0;
			gbc.weighty = 0;
			gbc.fill = GridBagConstraints.NONE;
			gbc.gridx = 1;
			gbc.gridy = 1;
			jp.add(new JLabel(
							GUIMediator
							.getStringResource("MENU_FILE_OPEN_TORRENT_URL_DIALOG_LABEL")),
							gbc);
			
			// host label <-> host input field spacer
			gbc.gridwidth = 1;
			gbc.gridheight = 1;
			gbc.weightx = 0;
			gbc.weighty = 0;
			gbc.fill = GridBagConstraints.NONE;
			gbc.gridx = 2;
			gbc.gridy = 1;
			jp.add(getVerticalSpacer(), gbc);
			
			// host input field
			gbc.gridwidth = 1;
			gbc.gridheight = 1;
			gbc.weightx = 1;
			gbc.weighty = 0;
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.gridx = 3;
			gbc.gridy = 1;
			jp.add(URL_INPUT, gbc);
			
			// host <-> buttons spacer
			gbc.gridwidth = 3;
			gbc.gridheight = 1;
			gbc.weightx = 1;
			gbc.weighty = 0;
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.gridx = 1;
			gbc.gridy = 4;
			jp.add(getHorizontalSpacer(), gbc);
			
			// buttons
			JPanel buttons = new JPanel();
			OK_BUTTON.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					String urlStr = URL_INPUT.getText();
					
					try {
						URL url = new URL(urlStr);
						GUIMediator.instance().openTorrent(url);
					} catch (MalformedURLException e) {
						GUIMediator.showError("ERROR_BAD_TORRENT_URL");
						return;
					}
					dialog.setVisible(false);
					dialog.dispose();
				}
			});
			CANCEL_BUTTON.addActionListener(GUIUtils.getDisposeAction());
			buttons.add(OK_BUTTON);
			buttons.add(CANCEL_BUTTON);
			gbc.gridwidth = 3;
			gbc.gridheight = 1;
			gbc.weightx = 1;
			gbc.weighty = 0;
			gbc.fill = GridBagConstraints.NONE;
			gbc.anchor = GridBagConstraints.EAST;
			gbc.gridx = 1;
			gbc.gridy = 5;
			jp.add(buttons, gbc);
		}
		
		public void actionPerformed(ActionEvent e) {
			if (dialog == null)
				createDialog();
			
			// 2. display dialog centered (and modal)
			dialog.getRootPane().setDefaultButton(OK_BUTTON);
			dialog.pack();
			dialog.setLocation(GUIMediator.getScreenCenterPoint(dialog));
			dialog.setVisible(true);
		}
		
		/** Returns a vertical separator */
		private Component getVerticalSpacer() {
			return Box.createRigidArea(new Dimension(6, 0));
		}
		
		/** Returns a horizontal separator */
		private Component getHorizontalSpacer() {
			return Box.createRigidArea(new Dimension(0, 6));
		}
	}
}



















