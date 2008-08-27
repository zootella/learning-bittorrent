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
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import com.limegroup.gnutella.FileDesc;
import com.limegroup.gnutella.FileEventListener;
import com.limegroup.gnutella.FileManagerEvent;
import com.limegroup.gnutella.archive.Contribution;
import com.limegroup.gnutella.archive.DirectoryChangeFailedException;
import com.limegroup.gnutella.archive.LoginFailedException;
import com.limegroup.gnutella.archive.RefusedConnectionException;
import com.limegroup.gnutella.archive.UploadListener;
import com.limegroup.gnutella.gui.GUIMediator;
import com.limegroup.gnutella.gui.GUIUtils;
import com.limegroup.gnutella.gui.MessageService;
import com.limegroup.gnutella.util.ManagedThread;
import com.limegroup.gnutella.util.ThreadFactory;

public class InternetArchiveUploader implements FileEventListener, UploadListener {

	private static final int DIALOG_WIDTH = 400;

	private static final int DIALOG_HEIGHT = 200;

	private final JLabel _mainProgressLabel = new JLabel();
	
	private final JLabel _fileProgressLabel = new JLabel();
	
	private final JProgressBar _fileProgressBar = new JProgressBar(0, 100);
	
	private final JProgressBar _mainProgressBar = new JProgressBar(0, 100);
	
	private final JLabel _mainLabel = new JLabel(
			GUIMediator.getStringResource("INTERNETARCHIVE_UPLOADER_LABEL"));

	private JDialog _dialog;

	private FileDesc _fd;

	private final Contribution _contribution;

	private boolean _isEventHandled;
	
	private volatile boolean _isCancelled;
	
	private JButton finishButton;
    
    private final ProgressUpdater PROGRESS_UPDATER = new ProgressUpdater();
	
	public InternetArchiveUploader(FileDesc fd,
			Contribution contribution) {
		_fd = fd;
		_contribution = contribution;
	}

	public void handleFileEvent(FileManagerEvent evt) {

		if (_contribution == null)
			return;

		if (!evt.isChangeEvent() || evt.getFileDescs() == null
				|| evt.getFileDescs().length == 0)
			return;

		if (_fd.equals(evt.getFileDescs()[0])) {

			synchronized (this) {
				if (_isEventHandled)
					return;
				_isEventHandled = true;
			}
			init();
			FileDesc newFD = evt.getFileDescs()[1];
			_contribution.addFileDesc(newFD);
			_contribution.addListener(this);
			_dialog.setLocationRelativeTo(MessageService.getParentComponent());
			_dialog.addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent we) {
					cancel();
				}
			});
			ThreadFactory.startThread(new Runnable() {
                public void run() {
                    doUpload();
                }
            }, "InternetArchive_Uploader");
			_dialog.setVisible(true);
		}
	}

	protected void doUpload() {
		String message = null;
		boolean success = false;
		if(!_isCancelled) {
			try {
				_contribution.upload();
				message= GUIMediator
				.getStringResource("INTERNETARCHIVE_UPLOADER_COMPLETED");
				success = true;
			} catch(RefusedConnectionException rce) {
				message = "ERROR_INTERNETARCHIVE_CONNECTION";
			} catch(LoginFailedException lfe) {
				message = "ERROR_INTERNETARCHIVE_LOGIN";
			}catch(DirectoryChangeFailedException lfe) {
				message = "ERROR_INTERNETARCHIVE_DIRECTORY";
			}catch (IOException e) {
				message = "ERROR_INTERNETARCHIVE_COMMUNICATION";
			}
			finally {
				if(!_isCancelled) {
					if(success) {
						GUIMediator.safeInvokeLater(new Runnable() {
							public void run() {
								_mainProgressBar.setValue(100);
								_mainProgressBar.setString(100+"%");
								_mainProgressLabel.setText(GUIMediator
										.getStringResource("INTERNETARCHIVE_UPLOADER_COMPLETED"));
								_fileProgressLabel.setText("");
								_fileProgressBar.setIndeterminate(false);
								_fileProgressBar.setStringPainted(true);
								_fileProgressBar.setValue(100);
								_fileProgressBar.setString(100+"%");
								finishButton.setText(GUIMediator.getStringResource("GENERAL_OK_BUTTON_LABEL"));
							}
						});
					}
					else {
						final String mess = message;
						GUIMediator.safeInvokeLater(new Runnable() {
							public void run() {
								GUIMediator.showError(mess);
								cancel();
							}
						});
					}
				}
			}
		}
	}

	public void init() {
		_dialog = new JDialog(GUIMediator.getAppFrame(), true);
		GUIUtils.addHideAction((JComponent) _dialog.getContentPane());
		_dialog.setResizable(false);
		String title = GUIMediator.getStringResource("INTERNETARCHIVE_UPLOADER_TITLE");
		_dialog.setTitle(
				title+
				" - " +
				_contribution.getTitle());
		_dialog.setSize(new Dimension(DIALOG_WIDTH, DIALOG_HEIGHT));
		// content here
		JPanel mainPanel = new JPanel(new GridBagLayout());
		GridBagConstraints con = new GridBagConstraints();
		con.anchor = GridBagConstraints.WEST;
		con.insets = new Insets(2,2,2,2);
		mainPanel.add(_mainLabel,con);
		con.anchor = GridBagConstraints.CENTER;
		con.gridy=1;
		_mainProgressBar.setIndeterminate(true);
		_mainProgressBar.setStringPainted(true);
		_mainProgressBar.setValue(0);
		mainPanel.add(_mainProgressBar,con);
		con.gridy=2;
		_mainProgressLabel.setText(GUIMediator
				.getStringResource("INTERNETARCHIVE_UPLOADER_INIT"));
		mainPanel.add(_mainProgressLabel,con);
		con.gridy=3;
		con.insets= new Insets(10,0,0,0);
		_fileProgressBar.setEnabled(false);
		_fileProgressBar.setStringPainted(true);
		_fileProgressBar.setValue(0);
		mainPanel.add(_fileProgressBar,con);
		con.gridy=4;
		con.insets= new Insets(2,0,0,0);
		mainPanel.add(_fileProgressLabel,con);
		finishButton = new JButton(GUIMediator
				.getStringResource("GENERAL_CANCEL_BUTTON_LABEL"));
		finishButton.addActionListener(new OKCancelButtonListener());
		con.gridy=5;
		con.insets= new Insets(20,0,0,0);
		mainPanel.add(finishButton,con);
		_dialog.getContentPane().add(mainPanel);
	}

	public void connected() {
		GUIMediator.safeInvokeAndWait(new Runnable() {
			public void run() {
			_mainProgressLabel.setText(GUIMediator
					.getStringResource("INTERNETARCHIVE_UPLOADER_CONNECTED"));
			}
		});
	}

	public void fileCompleted() {
		GUIMediator.safeInvokeAndWait(new Runnable() {
			public void run() {
				setProgressBarStatus();
			}
		});
	}

	public void fileProgressed() {
        PROGRESS_UPDATER.updateEvent();
		GUIMediator.safeInvokeLater(PROGRESS_UPDATER);
	}

    
	public void fileStarted() {
		GUIMediator.safeInvokeAndWait(new Runnable() {
			public void run() {
				_mainProgressBar.setIndeterminate(false);
				_fileProgressBar.setEnabled(true);
				setProgressBarStatus();
				String fileName;
                int filesSent,totalFiles;
                
                synchronized(_contribution) {
                    if(_contribution.getFileName().length()>30)
                        fileName = _contribution.getFileName().substring(0,30)+"...";
                    else fileName = _contribution.getFileName();
                    filesSent = _contribution.getFilesSent();
                    totalFiles = _contribution.getTotalFiles();
                }
                
				String mainLabelString = 
					GUIMediator.getStringResource("INTERNETARCHIVE_UPLOADER_FILE_UPLOADING")+
					" "+ (filesSent + 1) +
					" " +
					GUIMediator.getStringResource("INTERNETARCHIVE_UPLOADER_FILE_UPLOADING_OF")+	
					" "+ totalFiles +
					": "+ fileName;
				_mainProgressLabel.setText(mainLabelString);
			}
		});
	}
	
	public void checkinCompleted() {}

	public void checkinStarted() {
		GUIMediator.safeInvokeAndWait(new Runnable() {
			public void run() {
				_fileProgressBar.setIndeterminate(true);
				_fileProgressBar.setStringPainted(false);
				_mainProgressLabel.setText(
						GUIMediator.getStringResource("INTERNETARCHIVE_UPLOADER_CHECKIN"));
				_fileProgressLabel.setText("");
			}
		});
	}

	private void setProgressBarStatus() {
        int totalPercent,filePercent;
        
        synchronized(_contribution) {
            totalPercent = (int) ((100.0 * _contribution.getTotalBytesSent()) / _contribution.getTotalSize());
            filePercent = (int) ((100.0 * _contribution.getFileBytesSent()) / _contribution.getFileSize());
        }
        
		_fileProgressBar.setValue(filePercent);
		_fileProgressBar.setString(filePercent+"%");
		_mainProgressBar.setValue(totalPercent);
		_mainProgressBar.setString(totalPercent+"%");
	}

	private void cancel() {
		if(!_isCancelled)_isCancelled = true;
		_contribution.cancel();
		dispose();
	}
	
	private void dispose() {
		_dialog.setVisible(false);
		_dialog.dispose();
	}
	
	private class OKCancelButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent arg0) {
			if(((JButton)arg0.getSource()).getText().equals(
					GUIMediator.getStringResource("GENERAL_OK_BUTTON_LABEL")))
				dispose();
			else 
				cancel();
		}
	}
    
    private class ProgressUpdater implements Runnable {
        
        private final String uploaded = GUIMediator
            .getStringResource("INTERNETARCHIVE_UPLOADER_FILE_UPLOADED");
        private final String of = GUIMediator
            .getStringResource("INTERNETARCHIVE_UPLOADER_FILE_BYTESOF");
        
        private boolean dirty;
        
        public synchronized void updateEvent() {
            dirty = true;
        }
        
        public void run() {
            
            synchronized(this) {
                if (!dirty) return;
                dirty = false;
            }
            
            String fileString;
            synchronized(_contribution) {
                fileString = uploaded + "  " + _contribution.getFileBytesSent()+ " "+ of +" " + _contribution.getFileSize();
                setProgressBarStatus();
            }

            _fileProgressLabel.setText(fileString);
        }
    }

}
