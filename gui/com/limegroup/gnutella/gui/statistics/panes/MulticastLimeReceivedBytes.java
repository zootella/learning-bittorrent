package com.limegroup.gnutella.gui.statistics.panes;

import com.limegroup.gnutella.gui.GUIMediator;
import com.limegroup.gnutella.statistics.LimeReceivedMessageStatBytes;
import com.limegroup.gnutella.statistics.ReceivedMessageStatBytes;

/**
 * This class is a <tt>PaneItem</tt> for the total number of bytes 
 * passed for LimeWire vs. other Gnutella clients.
 */
public final class MulticastLimeReceivedBytes extends AbstractMessageGraphPaneItem {
	
	/**
	 * Constructs a new statistics window that displays the total
	 * number of bytes passed for all clients vs. the number for
	 * only LimeWire.
	 *
	 * @param key the key for obtaining display strings for this
	 *  <tt>PaneItem</tt>, including the strings for the x and y
	 *  axis labels, the statistic description, etc
	 */
	public MulticastLimeReceivedBytes(final String key) {
		super(key, GraphAxisData.createKilobyteGraphData());
		registerStatistic(ReceivedMessageStatBytes.MULTICAST_ALL_MESSAGES,
						  GUIMediator.getStringResource("STATS_ALL_CLIENTS"));  
		registerStatistic(LimeReceivedMessageStatBytes.MULTICAST_ALL_MESSAGES,
						  GUIMediator.getStringResource("STATS_LIMEWIRE"));  
	}
}
