/**
 * 
 */
package com.limegroup.gnutella.gui.actions;

import java.awt.event.ActionEvent;
import java.text.MessageFormat;

import javax.swing.AbstractAction;
import javax.swing.Action;

import com.limegroup.gnutella.gui.GUIMediator;
import com.limegroup.gnutella.gui.search.SearchMediator;

/**
 * Provides a keyword search action.
 * <p>
 * After the search has been sent the search panel is focused.
 */
public class SearchForKeywordsAction extends AbstractAction {

	private String keywords;
	
	/**
	 * Constructs an action that searches a space separated list of keywords.
	 * 
	 * @param keywords the keywords should already be processed through
	 * {@link StringUtils#createQueryString(String)} and be longer than
	 * 2 characters.
	 */
	public SearchForKeywordsAction(String keywords) {
		this.keywords = keywords;
		putValue(Action.NAME, MessageFormat.format
				(GUIMediator.getStringResource("SEARCH_FOR_KEYWORDS_ACTION_NAME"), 
						new Object[] { keywords }));
	}
	
	public void actionPerformed(ActionEvent e) {
		SearchMediator.triggerSearch(keywords);
		GUIMediator.instance().setWindow(GUIMediator.SEARCH_INDEX);
	}
}
