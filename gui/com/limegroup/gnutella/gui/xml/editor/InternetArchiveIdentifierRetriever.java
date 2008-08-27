package com.limegroup.gnutella.gui.xml.editor;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import com.limegroup.gnutella.archive.Contribution;
import com.limegroup.gnutella.archive.IdentifierUnavailableException;
import com.limegroup.gnutella.gui.GUIMediator;
import com.limegroup.gnutella.gui.GUIUtils;
import com.limegroup.gnutella.gui.MessageService;
import com.limegroup.gnutella.util.ManagedThread;

public class InternetArchiveIdentifierRetriever extends ManagedThread {
	
	private static final int DIALOG_WIDTH = 200;

	private static final int DIALOG_HEIGHT = 100;

	private final JProgressBar _progressbar = new JProgressBar();

	private final JDialog _dialog;

	private volatile boolean _isCancelled;
	
	private boolean _isFinished;
	
	private String _id;
	
	private final Contribution _contribution;
	
	public InternetArchiveIdentifierRetriever(
			Contribution contrib,
			String id) {
		_id = id;
		_contribution = contrib;
		_dialog = new JDialog(GUIMediator.getAppFrame(), true);
	}

	public void reserveIdentifier() {
		init();
		_dialog.setLocationRelativeTo(MessageService.getParentComponent());
		_dialog.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent we) {
				cancel();
			}
		});
		start();
		_dialog.setVisible(true);
	}
	
	
	// TODO: DO NOT USE SWING METHODS FROM ANOTHER THREAD.
	protected void managedRun() {
        try {
            while(!_isCancelled) {
                try {
                    _contribution.requestIdentifier(_id);
                    break;
                } catch (IdentifierUnavailableException uex) {
                    if(_isCancelled)return;
                    // TODO: BAD.  DO NOT CALL SWING HERE.
                    _id = GUIMediator.showInputMessage(
                            "ERROR_CCPUBLISHER_IDENTIFIER_UNAVAILABLE",
                            uex.getIdentifier());
                    if(_id==null)break;
                } catch (IOException e) {
                    if(_isCancelled)return;
                    // TODO: BAD.  DO NOT CALL SWING HERE.
                    int i = GUIMediator.showYesNoMessage("ERROR_CCPUBLISHER_INTERNETARCHIVE_COMMUNICATION");
                    if(i == GUIMediator.NO_OPTION)break;
                } 
            }
        } finally {
            finish();
        }
	}

	private void init() {
		GUIUtils.addHideAction((JComponent) _dialog.getContentPane());
		_dialog.setResizable(false);
		_dialog.setTitle(
				GUIMediator.getStringResource("INTERNETARCHIVE_IDRETRIEVER_TITLE")+
				" - " +
				_contribution.getTitle());
		_dialog.setSize(new Dimension(DIALOG_WIDTH, DIALOG_HEIGHT));
		// content here
		JPanel mainPanel = new JPanel(new GridBagLayout());
		GridBagConstraints con = new GridBagConstraints();
		con.anchor = GridBagConstraints.CENTER;
		con.insets = new Insets(10,0,0,0);
		_progressbar.setIndeterminate(true);
		mainPanel.add(_progressbar,con);
		con.gridy=1;
		JButton cancelButton = new JButton(GUIMediator
				.getStringResource("GENERAL_CANCEL_BUTTON_LABEL"));
		cancelButton.addActionListener(new CancelButtonListener());
		mainPanel.add(cancelButton,con);
		_dialog.getContentPane().add(mainPanel);
	}
	
	private void cancel() {
		_isCancelled = true;
		interrupt();
		finish();
	}
	
	private void finish() {
		synchronized (_dialog) {
			if(!_isFinished) _isFinished=true;
			else return;
		}
		GUIMediator.safeInvokeLater(new Runnable() {
			public void run() {
				_dialog.setVisible(false);
				_dialog.dispose();
			}
		});
	}

	private class CancelButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			cancel();
		}
	}
}
