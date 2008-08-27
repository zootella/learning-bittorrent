package com.limegroup.gnutella.gui.options.panes;

import java.awt.Insets;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;

import javax.swing.JRadioButton;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;

import com.limegroup.gnutella.RouterService;
import com.limegroup.gnutella.gui.GUIUtils;
import com.limegroup.gnutella.gui.GUIMediator;
import com.limegroup.gnutella.gui.SizedWholeNumberField;
import com.limegroup.gnutella.gui.WholeNumberField;
import com.limegroup.gnutella.settings.ConnectionSettings;
import com.limegroup.gnutella.util.NetworkUtils;

/**
 * This class defines the panel in the options window that allows the user
 * to force their ip address to the specified value.
 */
//2345678|012345678|012345678|012345678|012345678|012345678|012345678|012345678|
public final class ForceIPPaneItem extends AbstractPaneItem {

	/**
	 * Constant <tt>WholeNumberField</tt> instance that holds the port 
	 * to force to.
	 */
	private final WholeNumberField PORT_FIELD = new SizedWholeNumberField();
	
    /**
     * Constant handle to the check box that enables or disables this feature.
     */
    private final ButtonGroup BUTTONS = new ButtonGroup();
    private final JRadioButton UPNP =
        new JRadioButton(GUIMediator.getStringResource("OPTIONS_ROUTER_UPNP"));
    private final JRadioButton PORT =
        new JRadioButton(GUIMediator.getStringResource("OPTIONS_ROUTER_PORT"));
    private final JRadioButton NONE =
        new JRadioButton(GUIMediator.getStringResource("OPTIONS_ROUTER_NOTHING"));

	/**
	 * The constructor constructs all of the elements of this 
	 * <tt>AbstractPaneItem</tt>.
	 *
	 * @param key the key for this <tt>AbstractPaneItem</tt> that the
	 *            superclass uses to generate locale-specific keys
	 */
	public ForceIPPaneItem(final String key) {
		super(key);
		
		BUTTONS.add(UPNP);
		BUTTONS.add(PORT);
		BUTTONS.add(NONE);
		PORT.addItemListener(new LocalPortListener());
		
		JPanel panel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(0, 0, 0, 6);
		panel.add(UPNP, c);
		c.gridwidth = GridBagConstraints.RELATIVE;
		panel.add(PORT, c);
		c.gridwidth = GridBagConstraints.REMAINDER;
		panel.add(PORT_FIELD, c);
		panel.add(NONE, c);
		
		add(GUIUtils.left(panel));
	}
	
	private void updateState() {
	    PORT_FIELD.setEnabled(PORT.isSelected());
        PORT_FIELD.setEditable(PORT.isSelected());
    }

    /** 
	 * Listener class that responds to the checking and the 
	 * unchecking of the check box specifying whether or not to 
	 * use a local ip configuration.  It makes the other fields 
	 * editable or not editable depending on the state of the
	 * check box.
	 */
    private class LocalPortListener implements ItemListener {
        public void itemStateChanged(ItemEvent e) {
            updateState();
        }
    }

	/**
	 * Defines the abstract method in <tt>AbstractPaneItem</tt>.<p>
	 *
	 * Sets the options for the fields in this <tt>PaneItem</tt> when the 
	 * window is shown.
	 */
	public void initOptions() {
	    if(ConnectionSettings.FORCE_IP_ADDRESS.getValue() && 
	      !ConnectionSettings.UPNP_IN_USE.getValue())
	        PORT.setSelected(true);
	    else if(ConnectionSettings.DISABLE_UPNP.getValue())
	        NONE.setSelected(true);
	    else
	        UPNP.setSelected(true);
	        
        PORT_FIELD.setValue(ConnectionSettings.FORCED_PORT.getValue());
        
		updateState();
	}

	/**
	 * Defines the abstract method in <tt>AbstractPaneItem</tt>.<p>
	 *
	 * Applies the options currently set in this window, displaying an
	 * error message to the user if a setting could not be applied.
	 *
	 * @throws IOException if the options could not be applied for some reason
	 */
	public boolean applyOptions() throws IOException {
	    boolean restart = false;
	    boolean oldUPNP = ConnectionSettings.UPNP_IN_USE.getValue();
        int oldPort = ConnectionSettings.FORCED_PORT.getValue();
        boolean oldForce = ConnectionSettings.FORCE_IP_ADDRESS.getValue();

	    
	    if(UPNP.isSelected()) {
	        if(!ConnectionSettings.UPNP_IN_USE.getValue())
	            ConnectionSettings.FORCE_IP_ADDRESS.setValue(false);
	        ConnectionSettings.DISABLE_UPNP.setValue(false);
	        if(!oldUPNP)
	            restart = true;
        } else if(NONE.isSelected()) {
            ConnectionSettings.FORCE_IP_ADDRESS.setValue(false);
            ConnectionSettings.DISABLE_UPNP.setValue(true);
        } else { // PORT.isSelected()
            int forcedPort = PORT_FIELD.getValue();
            if(!NetworkUtils.isValidPort(forcedPort)) {
                GUIMediator.showError("ERROR_FORCE_IP_PORT_RANGE");
                throw new IOException("bad port: "+forcedPort);
            }
            
            ConnectionSettings.DISABLE_UPNP.setValue(false);
            ConnectionSettings.FORCE_IP_ADDRESS.setValue(true);
            ConnectionSettings.UPNP_IN_USE.setValue(false);
            ConnectionSettings.FORCED_PORT.setValue(forcedPort);
        }
        
        // Notify that the address changed if:
        //    1) The 'forced address' status changed.
        // or 2) We're forcing and the ports are different.
        boolean newForce = ConnectionSettings.FORCE_IP_ADDRESS.getValue();
        int newPort = ConnectionSettings.FORCED_PORT.getValue();        
        if(oldForce != newForce || (newForce && (oldPort != newPort)))
            RouterService.addressChanged();
        
        return false;
    }
    
    public boolean isDirty() {
		
		if(ConnectionSettings.FORCE_IP_ADDRESS.getValue() && 
				!ConnectionSettings.UPNP_IN_USE.getValue()) {
			if (!PORT.isSelected()) {
				return true;
			}
		}
		else if(ConnectionSettings.DISABLE_UPNP.getValue()) {
			if (!NONE.isSelected()) {
				return true;
			}
		}
		else {
			if (!UPNP.isSelected()) {
				return true;
			}
		}
		return PORT.isSelected() 
			&& PORT_FIELD.getValue() != ConnectionSettings.FORCED_PORT.getValue();
    }
}
