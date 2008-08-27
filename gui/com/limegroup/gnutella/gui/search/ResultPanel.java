package com.limegroup.gnutella.gui.search;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.OverlayLayout;

import com.limegroup.gnutella.BrowseHostHandler;
import com.limegroup.gnutella.FileDetails;
import com.limegroup.gnutella.GUID;
import com.limegroup.gnutella.MediaType;
import com.limegroup.gnutella.RemoteFileDesc;
import com.limegroup.gnutella.RouterService;
import com.limegroup.gnutella.URN;
import com.limegroup.gnutella.gui.BoxPanel;
import com.limegroup.gnutella.gui.FileDetailsProvider;
import com.limegroup.gnutella.gui.GUIConstants;
import com.limegroup.gnutella.gui.GUIMediator;
import com.limegroup.gnutella.gui.IconButton;
import com.limegroup.gnutella.gui.LicenseWindow;
import com.limegroup.gnutella.gui.PaddedPanel;
import com.limegroup.gnutella.gui.ProgTabUIFactory;
import com.limegroup.gnutella.gui.tables.AbstractTableMediator;
import com.limegroup.gnutella.gui.tables.ColumnPreferenceHandler;
import com.limegroup.gnutella.gui.tables.DataLine;
import com.limegroup.gnutella.gui.tables.LimeJTable;
import com.limegroup.gnutella.gui.tables.LimeTableColumn;
import com.limegroup.gnutella.gui.tables.TableSettings;
import com.limegroup.gnutella.licenses.License;
import com.limegroup.gnutella.licenses.VerificationListener;
import com.limegroup.gnutella.search.QueryHandler;
import com.limegroup.gnutella.settings.FilterSettings;
import com.limegroup.gnutella.settings.SearchSettings;
import com.limegroup.gnutella.xml.LimeXMLDocument;

public class ResultPanel extends AbstractTableMediator
    implements VerificationListener, FileDetailsProvider {
    
    /**
     * Flag that a search has been stopped with a random GUID
     */
    static final GUID STOPPED_GUID = new GUID(GUID.makeGuid());
    
    private static final DateRenderer DATE_RENDERER = new DateRenderer();
    private static final QualityRenderer QUALITY_RENDERER = new QualityRenderer();
    private static final EndpointRenderer ENDPOINT_RENDERER = new EndpointRenderer();
    private static final ResultSpeedRenderer RESULT_SPEED_RENDERER = new ResultSpeedRenderer();
    private static final PercentageRenderer PERCENTAGE_RENDERER = new PercentageRenderer();
    
    /**
     * The TableSettings that all ResultPanels will use.
     */
    static final TableSettings SEARCH_SETTINGS =
        new TableSettings("SEARCH_TABLE");
    
    /**
     * The search info of this class.
     */
    private final SearchInformation SEARCH_INFO;
    
    /**
     * This' spam filter
     */
    private final SpamFilter SPAM_FILTER;

    /**
     * The GUID of the last search. (Use this to match up results.)
     *  May be a DummyGUID for the empty result list hack.
     */
    private volatile GUID guid;

    /**
     * The time (in milliseconds) that we last received a Query Result
     */
    private long timeLastResultReceived;
    
    /**
     * The BrowseHostHandler if this is a Browse Host tab.
     */
    private BrowseHostHandler browseHandler = null;
    
    /**
     * Start time of the query that this specific ResultPane handles
     */
    private long startTime = System.currentTimeMillis();
    
    /**
     * The CompositeFilter for this ResultPanel.
     */
    private CompositeFilter FILTER;
    
    /**
     * The download listener.
     */
    ActionListener DOWNLOAD_LISTENER;
    
    /**
     * The "download as" listener.
     */
    ActionListener DOWNLOAD_AS_LISTENER;
    
    /**
     * The chat listener.
     */
    ActionListener CHAT_LISTENER;
    
    /**
     * The browse host listener.
     */
    ActionListener BROWSE_HOST_LISTENER;
    
    /**
     * The stop listener.
     */
    ActionListener STOP_LISTENER;
    
    /**
     * The Mark As Spam listener
     */
    ActionListener MARK_AS_SPAM_LISTENER;
    
    /**
     * The Mark As Not Spam listener
     */
    ActionListener MARK_AS_NOT_SPAM_LISTENER;
    
    /**
     * The button that marks search results as spam or undoes it
     */
    private JButton SPAM_BUTTON;
    
    /**
     * Specialized constructor for creating a "dummy" result panel.
     * This should only be called once at search window creation-time.
     */
    ResultPanel(JPanel overlay) {
        super("SEARCH_TABLE");
        setupFakeTable(overlay);
        SEARCH_INFO = SearchInformation.createKeywordSearch("", null,
                                      MediaType.getAnyTypeMediaType());
        SPAM_FILTER=null;
        FILTER = null;
        this.guid = STOPPED_GUID;
        setButtonEnabled(SearchButtons.STOP_BUTTON_INDEX, false);
    }

    /**
     * Constructs a new ResultPanel for search results.
     *
     * @param guid the guid of the query.  Used to match results.
     * @param info the info of the search
     */
    ResultPanel(GUID guid, SearchInformation info) {
        super("SEARCH_TABLE");
        SEARCH_INFO = info;
        if (SEARCH_INFO.isBrowseHostSearch() || SEARCH_INFO.isWhatsNewSearch())
            SPAM_FILTER = null;
        else
            SPAM_FILTER = new SpamFilter();
        this.guid = guid;
        setupRealTable();
        resetFilters();
    }
    
    /**
     * Sets the default renderers to be used in the table.
     */
    protected void setDefaultRenderers() {
        super.setDefaultRenderers();
        TABLE.setDefaultRenderer(QualityHolder.class, QUALITY_RENDERER);
        TABLE.setDefaultRenderer(EndpointHolder.class, ENDPOINT_RENDERER);
        TABLE.setDefaultRenderer(ResultSpeed.class, RESULT_SPEED_RENDERER);
        TABLE.setDefaultRenderer(Date.class, DATE_RENDERER);
        TABLE.setDefaultRenderer(Float.class, PERCENTAGE_RENDERER);
    }
    
    /**
     * Does nothing.
     */
    protected void updateSplashScreen() { }
    
    /**
     * Simple inner class to allow a PaddedPanel to implement Progressor.
     * This is necessary for the ProgTabUIFactory to get the percentage
     * of its tabs.
     */
    private class PPP extends PaddedPanel
                      implements ProgTabUIFactory.Progressor {
        public double calculatePercentage(long now) {
            return ResultPanel.this.calculatePercentage(now);
        }
    }

    /**
     * Sets up the constants:
     * FILTER, MAIN_PANEL, DATA_MODEL, TABLE, BUTTON_ROW.
     */
    protected void setupConstants() {
        FILTER = new CompositeFilter(4);
        MAIN_PANEL = new PPP();
        DATA_MODEL = new TableRowFilter(FILTER);
        TABLE = new LimeJTable(DATA_MODEL);
        ((ResultPanelModel)DATA_MODEL).setTable(TABLE);
        BUTTON_ROW = new SearchButtons(this).getComponent();
        
        // The initialization of the SPAM_BUTTON is a bit
        // hackish. Use the NOT_SPAM label as it is longer
        // and needs thus more space. As next init the button
        // with the true label but keep the button width. See 
        // transformButton() for more info...
        SPAM_BUTTON = new IconButton(
                GUIMediator.getStringResource("SEARCH_NOT_SPAM_BUTTON_LABEL"), 
                "SEARCH_SPAM");
        transformSpamButton(GUIMediator.getStringResource("SEARCH_SPAM_BUTTON_LABEL"), 
                GUIMediator.getStringResource("SEARCH_SPAM_BUTTON_TIP"));
        
        SPAM_BUTTON.setEnabled(false);
        SPAM_BUTTON.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                TableLine[] lines = getAllSelectedLines();
                if (lines.length > 0) {
                    if (SpamFilter.isAboveSpamThreshold(lines[0])) {
                        MARK_AS_NOT_SPAM_LISTENER.actionPerformed(e);
                    } else {
                        MARK_AS_SPAM_LISTENER.actionPerformed(e);
                    }
                }
            }
        });
    }
    
    /**
     * Sets SETTINGS to be the static SEARCH_SETTINGS, instead
     * of constructing a new one for each ResultPanel.
     */
    protected void buildSettings() {
        SETTINGS = SEARCH_SETTINGS;
    }
    
    /**
     * Creates the specialized SearchColumnSelectionMenu menu,
     * which groups XML columns together.
     */
    protected JPopupMenu createColumnSelectionMenu() {
        return (new SearchColumnSelectionMenu(TABLE)).getComponent();
    }
    
    /**
     * Creates the specialized column preference handler for search columns.
     */
    protected ColumnPreferenceHandler createDefaultColumnPreferencesHandler() {
        return new SearchColumnPreferenceHandler(TABLE);
    }    
    
    /**
     * Sets DOWNLOAD_LISTENER, CHAT_LISTENER, BROWSE_HOST_LISTENER,
     * and STOP_LISTENER.
     */
    protected void buildListeners() {
        super.buildListeners();
        
        DOWNLOAD_LISTENER = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                SearchMediator.doDownload(ResultPanel.this);
            }
        };
        
        DOWNLOAD_AS_LISTENER = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                SearchMediator.doDownloadAs(ResultPanel.this);
            }
        };
        
        CHAT_LISTENER = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                doChat();
            }
        };
        
        BROWSE_HOST_LISTENER = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                SearchMediator.doBrowseHost(ResultPanel.this);
            }
        };
        
        STOP_LISTENER = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                stopSearch();
            }
        };
            
        MARK_AS_SPAM_LISTENER = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                TableLine[] lines = getAllSelectedLines();
                for (int i = 0; i < lines.length; i++) {
                    SPAM_FILTER.markAsSpamUser(lines[i], true);             
                }
                
                // This is a bit fine tuning...
                if (SearchSettings.hideJunk()) {
                    filtersChanged();   // i.e. hide the search result(s) we've just
                                        // marked as spam
                } else {
                    DATA_MODEL.refresh(); // mark 'em red
                    transformSpamButton(GUIMediator.getStringResource("SEARCH_NOT_SPAM_BUTTON_LABEL"), 
                            GUIMediator.getStringResource("SEARCH_NOT_SPAM_BUTTON_TIP"));
                }
            }
        };

        MARK_AS_NOT_SPAM_LISTENER = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                TableLine[] lines = getAllSelectedLines();
                for (int i = 0; i < lines.length; i++) {
                    SPAM_FILTER.markAsSpamUser(lines[i], false);
                }
                DATA_MODEL.refresh();
                
                transformSpamButton(GUIMediator.getStringResource("SEARCH_SPAM_BUTTON_LABEL"), 
                        GUIMediator.getStringResource("SEARCH_SPAM_BUTTON_TIP"));
            }
        };
    }
    
    /**
     * Creates the specialized SearchResultMenu for right-click popups.
     *
     * Upgraded access from protected to public for SearchResultDisplayer.
     */
    public JPopupMenu createPopupMenu() {
        //  do not return a menu if right-clicking on the dummy panel
        if (!isKillable())
            return null;
        
        TableLine[] lines = getAllSelectedLines();
        boolean allSpam = true;
        boolean allNot = true;
        
        if (SPAM_FILTER != null) {
            for (int i = 0; i < lines.length; i++) {
                if (!SpamFilter.isAboveSpamThreshold(lines[i]))
                    allSpam = false;
                else
                    allNot = false;
            }
        }
        
        return (new SearchResultMenu(this)).createMenu(lines, !allSpam, !allNot);
    }
    
    /**
     * Adds a single result.
     *
     * Also marks the last time a result was received.
     */
    public void add(Object o) {
        super.add(o);
        timeLastResultReceived = System.currentTimeMillis();
    }
    
    /**
     * Do not allow removal of rows.
     */
    public void removeSelection() { }
    
    /**
     * Clears the table and converts the download button into a
     * wishlist button.
     */
    public void clearTable() {
        super.clearTable();
    }
    
    /**
     * Sets the appropriate buttons to be disabled.
     */
    public void handleNoSelection() { 
        setButtonEnabled(SearchButtons.DOWNLOAD_BUTTON_INDEX, false);
        setButtonEnabled(SearchButtons.BROWSE_BUTTON_INDEX, false);
        
        SPAM_BUTTON.setEnabled(false);
        if (SearchSettings.ENABLE_SPAM_FILTER.getValue() && SPAM_FILTER != null) {
            transformSpamButton(GUIMediator.getStringResource("SEARCH_SPAM_BUTTON_LABEL"), 
                    GUIMediator.getStringResource("SEARCH_SPAM_BUTTON_TIP"));
        }
    }
    
    /**
     * Sets the appropriate buttons to be enabled.
     */
    public void handleSelection(int i)  { 
        setButtonEnabled(SearchButtons.DOWNLOAD_BUTTON_INDEX, true);
        
        TableLine line = (TableLine)DATA_MODEL.get(i);
        setButtonEnabled(SearchButtons.BROWSE_BUTTON_INDEX,
                         line.isBrowseHostEnabled());
        
        if (SearchSettings.ENABLE_SPAM_FILTER.getValue() && SPAM_FILTER != null) {
            SPAM_BUTTON.setEnabled(true);
            
            if (SpamFilter.isAboveSpamThreshold(line)) {
                transformSpamButton(GUIMediator.getStringResource("SEARCH_NOT_SPAM_BUTTON_LABEL"), 
                        GUIMediator.getStringResource("SEARCH_NOT_SPAM_BUTTON_TIP"));
            } else {
                transformSpamButton(GUIMediator.getStringResource("SEARCH_SPAM_BUTTON_LABEL"), 
                        GUIMediator.getStringResource("SEARCH_SPAM_BUTTON_TIP"));
            }
        }
    }
    
    /**
     * Forwards the event to DOWNLOAD_LISTENER.
     */
    public void handleActionKey() {
        DOWNLOAD_LISTENER.actionPerformed(null);
    }
    
    /**
     * Gets the SearchInformation of this search.
     */
    SearchInformation getSearchInformation() {
        return SEARCH_INFO;
    }
    
    /**
     * Gets the query of the search.
     */
    String getQuery() {
        return SEARCH_INFO.getQuery();
    }
    
    /**
     * Returns the title of the search.
     * @return
     */
    String getTitle() {
        return SEARCH_INFO.getTitle();
    }
    
    /**
     * Gets the rich query of the search.
     */
    String getRichQuery() {
        return SEARCH_INFO.getXML();
    }    
    
    /**
     * Stops this result panel from receiving more results.
     */
    void stopSearch() {
        final GUID guidToStop = guid;
        GUIMediator.instance().schedule(new Runnable() {
            public void run() {
                RouterService.stopQuery(guidToStop);
            }
        });
        setGUID(STOPPED_GUID);
        SearchMediator.checkToStopLime();
        setButtonEnabled(SearchButtons.STOP_BUTTON_INDEX, false);
    }
    

    /**
     * Chats with the host chat-enabled host in the selected
     * TableLine.
     */
    void doChat() {
        TableLine line = getSelectedLine();
        if(line == null)
            return;
        if(!line.isChatEnabled())
            return;
        line.doChat();
    }

    /**
     * Blocks the host that sent the selected result.
     */
    void blockHost() {
        TableLine line = getSelectedLine();
        if(line == null)
            return;
        
        String host = line.getHostname();
        int answer = GUIMediator.showYesNoMessage("SEARCH_BLOCK_HOST", " " + host + "?");
        if (answer == GUIMediator.YES_OPTION && host != null) {
            String[] bannedIps = FilterSettings.BLACK_LISTED_IP_ADDRESSES.getValue();
            // Ignore if this host is already banned.
            for (int i = 0; i < bannedIps.length; i++)
                if (host.equalsIgnoreCase(bannedIps[i]))
                    return;
            String[] newBannedIps = new String[bannedIps.length + 1];
            System.arraycopy(bannedIps, 0, newBannedIps, 0,
                             bannedIps.length);
            newBannedIps[bannedIps.length] = host;
            FilterSettings.BLACK_LISTED_IP_ADDRESSES.setValue(newBannedIps);
            RouterService.adjustSpamFilters();
        }
    }
    
    /**
     * Shows a LicenseWindow for the selected line.
     */
    void showLicense() {
        TableLine line = getSelectedLine();
        if(line == null)
            return;
            
        URN urn = line.getSHA1Urn();
        LimeXMLDocument doc = line.getXMLDocument();
        LicenseWindow window = LicenseWindow.create(line.getLicense(), urn, doc, this);
        window.setVisible(true);
    }
    
    public void licenseVerified(License license) {
        // if it was valid at all, refresh.
        if(license.isValid(null))
            ((ResultPanelModel)DATA_MODEL).slowRefresh();
    }
    
    /**
     * Determines whether or not this panel is stopped.
     */
    boolean isStopped() {
        return guid.equals(STOPPED_GUID);
    }
    
    /**
     * Determines if this is empty.
     */
    boolean isEmpty() {
        return DATA_MODEL.getRowCount() == 0;
    }
    
    /**
     * Determines if this can be removed.
     */
    boolean isKillable() {
        // the dummy panel has a null filter, and is the only one not killable
        return FILTER != null;
    }
    
    /**
     * Notification that a filter on this panel has changed.
     *
     * Updates the data model with the new list, maintains the selection,
     * and moves the viewport to the first still visible selected row.
     *
     * Note that the viewport moving cannot be done by just storing the first
     * visible row, because after the filters change, the row might not exist
     * anymore.  Thus, it is necessary to store all visible rows and move to
     * the first still-visible one.
     */
    boolean filterChanged(TableLineFilter filter, int depth) {
        if(!FILTER.setFilter(depth, filter))
            return false;
        
        // store the selection & visible rows
        int[] rows = TABLE.getSelectedRows();
        DataLine[] lines = new DataLine[rows.length];
        List inView = new LinkedList();
        for(int i = 0; i < rows.length; i++) {
            int row = rows[i];
            DataLine line = DATA_MODEL.get(row);
            lines[i] = line;
            if(TABLE.isRowVisible(row))
                inView.add(line);
        }
        
        // change the table.
        ((TableRowFilter)DATA_MODEL).filtersChanged();
        
        // reselect & move the viewpoint to the first still visible row.
        for(int i = 0; i < rows.length; i++) {
            DataLine line = lines[i];
            int row = DATA_MODEL.getRow(line);
            if(row != -1) {
                TABLE.addRowSelectionInterval(row, row);
                if(inView != null && inView.contains(line)) {
                    TABLE.ensureRowVisible(row);
                    inView = null;
                }                    
            }
        }
        
        // update the tab count.
        SearchMediator.setTabDisplayCount(this);
        return true;
    }
    
    /**
     * Returns the total number of sources found for this search.
     */
    int totalSources() {
        return ((ResultPanelModel)DATA_MODEL).getTotalSources();
    }
    
    /**
     * Returns the total number of filtered source found for this search.
     */
    int filteredSources() {
        return ((TableRowFilter)DATA_MODEL).getFilteredSources();
    }

    /**
     * Determines whether or not repeat search is currently enabled.
     * Repeat search will be disabled if, for example, the original
     * search was performed too recently.
     *
     * @return <tt>true</tt> if the repeat search feature is currently
     *  enabled, otherwise <tt>false</tt>
     */
    boolean isRepeatSearchEnabled() {
        return FILTER != null;
    }

    void repeatSearch() {
        clearTable();
        startTime = System.currentTimeMillis();
        resetFilters();
        
        SearchMediator.setTabDisplayCount(this);
        SearchMediator.repeatSearch(this, SEARCH_INFO);
        setButtonEnabled(SearchButtons.STOP_BUTTON_INDEX, true);
    }
    
    void resetFilters() {
        FILTER.reset();
        
        if (!SEARCH_INFO.isBrowseHostSearch() && !SEARCH_INFO.isWhatsNewSearch()) {
            ((TableRowFilter)DATA_MODEL).setJunkFilter(SPAM_FILTER);
        } else {
            ((TableRowFilter)DATA_MODEL).setJunkFilter(null);
        }
    }
    
    private void filtersChanged() {
        ((TableRowFilter)DATA_MODEL).filtersChanged();
        SearchMediator.setTabDisplayCount(this);
    }
    
    /**
     * Gets the MetadataModel used for results.
     */
    MetadataModel getMetadataModel() {
        return  ((ResultPanelModel)DATA_MODEL).getMetadataModel();
    }

    /** Returns true if this is responsible for results with the given GUID */
    boolean matches(GUID otherGuid) {
        return this.guid.equals(otherGuid);
    }

    /**
     * @modifies this
     * @effects sets this' guid.  This is needed for browse host functionality.
     */
    void setGUID(GUID guid) {
        this.guid=guid;
    }

    /** Returns the guid this is responsible for. */
    byte[] getGUID() {
        return guid.bytes();
    }

    /** Returns the media type this is responsible for. */
    MediaType getMediaType() {
        return SEARCH_INFO.getMediaType();
    }
    
    /**
     * Sets the BrowseHostHandler.
     */
    void setBrowseHostHandler(BrowseHostHandler bhh) {
        browseHandler = bhh;
    }
    
    /**
     * Gets all currently selected TableLines.
     * 
     * @return empty array if no lines are selected.
     */
    TableLine[] getAllSelectedLines() {
        int[] rows = TABLE.getSelectedRows();
        if(rows == null)
            return new TableLine[0];
        
        TableLine[] lines = new TableLine[rows.length];
        for(int i = 0; i < rows.length; i++)
            lines[i] = (TableLine)DATA_MODEL.get(rows[i]);
        return lines;
    }
    
    /**
     * Gets the currently selected TableLine.
     * 
     * @return null if there is no selected line.
     */
    TableLine getSelectedLine() {
        int selected = TABLE.getSelectedRow();
        if(selected != -1) 
            return (TableLine)DATA_MODEL.get(selected);
        else
            return null;
    }
    
    /**
     * Calculates the percentange of results that have been received for this
     * ResultPanel.
     */
    double calculatePercentage(long currentTime) {
        if(guid.equals(STOPPED_GUID))
            return 1d;

        if(SEARCH_INFO.isBrowseHostSearch()) {
            if( browseHandler != null )
                return browseHandler.getPercentComplete(currentTime);
            else
                return 0d;
        } 
        
        // first calculate the percentage solely based on 
        // the number of results we've received.
        int ideal = QueryHandler.ULTRAPEER_RESULTS;
        double resultPerc = (double)totalSources() / ideal;
        
        // then calculate the percentage solely based on
        // the time we've spent querying.
        long spent = currentTime - startTime;
        double timePerc = (double)spent / QueryHandler.MAX_QUERY_TIME;
        
        // If the results are already enough to fill it up, just use that.
        if( resultPerc >= 1 )
            return 1d;
        
        // Otherwise, the time percentage should fill up what remains in
        // the progress.
        timePerc = timePerc * (1 - resultPerc);
        
        // Return the results received + time spent.
        return resultPerc + timePerc;
    }            
    
    /**
     * Sets extra values for non dummy ResultPanels.
     * (Used for all tables that will have results.)
     *
     * Currently:
     * - Sorts the count column, if it is visible & real-time sorting is on.
     * - Adds listeners, so the filters can be displayed when necessary.
     */
    private void setupRealTable() {
        SearchTableColumns columns =
            ((ResultPanelModel)DATA_MODEL).getColumns();
        LimeTableColumn countColumn =
            columns.getColumn(SearchTableColumns.COUNT_IDX);
        if(SETTINGS.REAL_TIME_SORT.getValue() &&
           TABLE.isColumnVisible(countColumn.getId())) {
            DATA_MODEL.sort(SearchTableColumns.COUNT_IDX); // ascending
            DATA_MODEL.sort(SearchTableColumns.COUNT_IDX); // descending
        }
        
        MouseListener filterDisplayer = new MouseListener() {
            public void mouseClicked(MouseEvent e) {
                if(e.isConsumed())
                    return;
                e.consume();
                SearchMediator.panelSelected(ResultPanel.this);
            }
            public void mousePressed(MouseEvent e) {}
            public void mouseReleased(MouseEvent e) {}
            public void mouseEntered(MouseEvent e) {}
            public void mouseExited(MouseEvent e) {}
        };
        // catches around the button area.
        MAIN_PANEL.addMouseListener(filterDisplayer);
        // catches the blank area before results fill in
        SCROLL_PANE.addMouseListener(filterDisplayer);
        // catches selections on the table
        TABLE.addMouseListener(filterDisplayer);
        // catches the table header
        TABLE.getTableHeader().addMouseListener(filterDisplayer);
    }
    
    /**
     * Overwritten
     */
    protected void setupMainPanel() {
        if (SearchSettings.ENABLE_SPAM_FILTER.getValue() 
                && MAIN_PANEL != null) {
            MAIN_PANEL.add(getScrolledTablePane());
            addButtonRow();
            MAIN_PANEL.setMinimumSize(ZERO_DIMENSION);
        } else {
            super.setupMainPanel();
        }
    }

    /**
     * Adds the overlay panel into the table & converts the button
     * to 'download'.
     */
    private void setupFakeTable(JPanel overlay) {
        MAIN_PANEL.removeAll();
        
        JPanel background = new JPanel();
        background.setLayout(new OverlayLayout(background));
        JPanel overlayPanel = new BoxPanel(BoxPanel.Y_AXIS);
        overlayPanel.setOpaque(false);
        overlayPanel.add(Box.createVerticalStrut(20));
        overlayPanel.add(overlay);
        overlayPanel.setMinimumSize(new Dimension(0, 0));
        JComponent table = getScrolledTablePane();
        table.setOpaque(false);
        background.add(overlayPanel);
        background.add(table);
        
        MAIN_PANEL.add(background);
        addButtonRow();
        
        MAIN_PANEL.setMinimumSize(ZERO_DIMENSION);
    }
    
    /**
     * Adds the button row and the Spam Button
     */
    private void addButtonRow() {
        if (BUTTON_ROW != null) {
            MAIN_PANEL.add(Box.createVerticalStrut(GUIConstants.SEPARATOR));
            
            if (SearchSettings.ENABLE_SPAM_FILTER.getValue() && SPAM_BUTTON != null) {
                JPanel buttonPanel = new JPanel();
                buttonPanel.setOpaque(false);
                
                buttonPanel.setLayout(new GridBagLayout());
                GridBagConstraints gbc = null;
                
                gbc = new GridBagConstraints();
                gbc.gridx = 0;
                gbc.gridy = 0;
                gbc.anchor = GridBagConstraints.CENTER;
                gbc.fill = GridBagConstraints.NONE;
                gbc.gridwidth = GridBagConstraints.RELATIVE;
                gbc.weightx = 1;
                buttonPanel.add(BUTTON_ROW, gbc);
                
                gbc = new GridBagConstraints();
                gbc.gridx = 1;
                gbc.gridy = 0;
                gbc.anchor = GridBagConstraints.EAST;
                gbc.fill = GridBagConstraints.NONE;
                gbc.gridwidth = GridBagConstraints.REMAINDER;
                buttonPanel.add(SPAM_BUTTON, gbc);
                
                buttonPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 64));
                MAIN_PANEL.add(buttonPanel);
            } else {
                MAIN_PANEL.add(BUTTON_ROW);
            }
        }
    }
    
    public FileDetails[] getFileDetails() {
        int[] sel = TABLE.getSelectedRows();
        ArrayList list = new ArrayList(sel.length);
        for (int i = 0; i < sel.length; i++) {
            TableLine line = (TableLine)DATA_MODEL.get(sel[i]);
            // prefer non-firewalled rfds for the magnet action
            RemoteFileDesc rfd = line.getNonFirewalledRFD();
            if (rfd != null) {
                list.add(rfd);
            }
            else {
                // fall back on first rfd
                rfd = line.getRemoteFileDesc();
                if (rfd != null) {
                    list.add(rfd);
                }
            }
        }
        if (list.isEmpty()) {
            return new FileDetails[0];
        }
        return (FileDetails[])list.toArray(new FileDetails[0]);
    }

    /**
     * Change the text and tooltip text of the SPAM_BUTTON
     */
    private void transformSpamButton(String text, String tip) {
        Dimension oldDim = SPAM_BUTTON.getPreferredSize();
        
        SPAM_BUTTON.setText(text);
        SPAM_BUTTON.setToolTipText(tip);
        
        // Preserve/use the max width...
        Dimension newDim = SPAM_BUTTON.getPreferredSize();
        newDim.width = Math.max(oldDim.width, newDim.width);
        SPAM_BUTTON.setPreferredSize(newDim);
    }
}
