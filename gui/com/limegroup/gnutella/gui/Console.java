package com.limegroup.gnutella.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.WriterAppender;
import org.apache.log4j.spi.LoggerRepository;

import com.limegroup.gnutella.ErrorService;
import com.limegroup.gnutella.bugs.LocalClientInfo;
import com.limegroup.gnutella.gui.themes.ThemeFileHandler;
import com.limegroup.gnutella.gui.themes.ThemeMediator;
import com.limegroup.gnutella.gui.themes.ThemeObserver;
import com.limegroup.gnutella.settings.ConsoleSettings;
import com.limegroup.gnutella.util.CommonUtils;

/**
 * A Console for log/any output
 */
public class Console extends JPanel implements ThemeObserver {

    private final int idealSize;
    private final int maxExcess;
    
    private JScrollPane scrollPane;

    private JTextArea output;

    private JButton apply;
    private JButton clear;
    private JButton save;
    
    private JComboBox loggerComboBox;

    private JComboBox levelComboBox;

    private boolean scroll = true;
    
    private boolean altCtrlDown = false;
    
    public Console() {
        
        idealSize = ConsoleSettings.CONSOLE_IDEAL_SIZE.getValue();
        maxExcess = ConsoleSettings.CONSOLE_MAX_EXCESS.getValue();
        
        output = new JTextArea();
        output.setEditable(false);

        scrollPane = new JScrollPane(output);
        scrollPane.getVerticalScrollBar().addAdjustmentListener(
                new AdjustmentListener() {
                    public void adjustmentValueChanged(AdjustmentEvent e) {
                        if (e.getValueIsAdjusting()) {
                            scroll = false;
                        } else {
                            scroll = true;
                        }
                    }
                });

        loggerComboBox = new JComboBox(new LoggerComboBoxModel());
        levelComboBox = new JComboBox(new LevelComboBoxModel());
        
        loggerComboBox.setAutoscrolls(true);
        loggerComboBox.setMaximumRowCount(20);
        loggerComboBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent evt) {
                selectLoggerLevel();
            }
        });
        
        loggerComboBox.addPopupMenuListener(new PopupMenuListener() {
            public void popupMenuWillBecomeVisible(PopupMenuEvent evt) {
                refreshLoggers();
            }
            
            public void popupMenuCanceled(PopupMenuEvent evt) {}
            public void popupMenuWillBecomeInvisible(PopupMenuEvent evt) {}
        });
        
        levelComboBox.setAutoscrolls(true);
        
        apply = new JButton(GUIMediator.getStringResource("GENERAL_APPLY_BUTTON_LABEL"));
        apply.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                applyLevel();
            }
        });

        clear = new JButton(GUIMediator.getStringResource("GENERAL_CLEAR_BUTTON_LABEL"));
        clear.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                clear();
            }
        });
        
        save = new JButton(GUIMediator.getStringResource("CONSOLE_SAVE_BUTTON_LABEL"));
        save.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                save();
            }
        });
        
        // Developers can press and hold Alt+Ctrl while clicking
        // on Save to get the current stack traces.
        if (CommonUtils.isJava15OrLater()) {
            KeyListener keyListener = new KeyAdapter() {
                public void keyPressed(KeyEvent e) {
                    altCtrlDown = e.isAltDown() && e.isControlDown();
                }

                // Note: if the user is holding Alt+Ctrl while
                // switching to a different Tab this will never
                // get called! We need a second Listener!
                public void keyReleased(KeyEvent e) {
                    altCtrlDown = false;
                }
            };
            
            // Install the listener on all components
            addKeyListener(keyListener);
            scrollPane.addKeyListener(keyListener);
            output.addKeyListener(keyListener);
            loggerComboBox.addKeyListener(keyListener);
            levelComboBox.addKeyListener(keyListener);
            apply.addKeyListener(keyListener);
            clear.addKeyListener(keyListener);
            save.addKeyListener(keyListener);
            
            // Reset the flag if this Tab gets invisible
            addComponentListener(new ComponentAdapter() {
                public void componentHidden(ComponentEvent e) {
                    altCtrlDown = false;
                }
            });
        }
        
        setLayout(new BorderLayout());
        add(BorderLayout.CENTER, scrollPane);

        JPanel controlsPanel = new JPanel();
        controlsPanel.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 2;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        controlsPanel.add(loggerComboBox, gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 0;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        controlsPanel.add(levelComboBox, gbc);

        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 0;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        controlsPanel.add(apply, gbc);
        
        gbc.gridx = 3;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 0;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        controlsPanel.add(clear, gbc);
        
        gbc.gridx = 4;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 0;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        controlsPanel.add(save, gbc);
        
        add(BorderLayout.SOUTH, controlsPanel);

        updateTheme();
        ThemeMediator.addThemeObserver(this);

        refreshLoggers();
        attachLogs();
    }
    
    /**
     * 
     */
    private void attachLogs() {
        WriterAppender append = new WriterAppender(new PatternLayout(
                ConsoleSettings.CONSOLE_PATTERN_LAYOUT.getValue()), new ConsoleWriter());
        LogManager.getRootLogger().addAppender(append);
    }
    
    /**
     * Rebuilds the Logger ComboBox
     */
    private void refreshLoggers() {
        LoggerRepository repository = LogManager.getLoggerRepository();
        Enumeration currentLoggers = repository.getCurrentLoggers();
        
        LoggerComboBoxModel loggerModel = (LoggerComboBoxModel) loggerComboBox.getModel();
        int loggerIndex = loggerComboBox.getSelectedIndex();
        LoggerNode currentLogger = (loggerIndex >= 0) ? loggerModel.getLogger(loggerIndex) : null;
        
        /*
         * Step 1: Create a Tree of Packages and Classes
         */
        ArrayList pkgList = new ArrayList();
        HashMap pkgMap = new HashMap();
        while (currentLoggers.hasMoreElements()) {
            Logger lggr = (Logger)currentLoggers.nextElement();
            
            String pkg = PackageNode.getPackage(lggr);
            PackageNode node = (PackageNode)pkgMap.get(pkg);
            if (node == null) {
                node = new PackageNode(pkg);
                pkgMap.put(pkg, node);
                pkgList.add(node);
            }
            node.add(lggr);
        }
        
        /*
         * Step 2: Sort the Packages by name
         */
        Collections.sort(pkgList, new Comparator() {
            public int compare(Object o1, Object o2) {
                return ((PackageNode) o1).getName().compareTo(((PackageNode) o2).getName());
            }
        });
        
        /*
         * Step 3: Turn the Tree into a flat List of
         * 
         * Package
         *     Class
         *     Class
         * Package
         *     Class
         *     ...
         */
        loggerIndex = -1;
        ArrayList nodes = new ArrayList();
        for(Iterator it = pkgList.iterator(); it.hasNext(); ) {
            PackageNode pkgNode = (PackageNode)it.next();
            pkgNode.sort();
            nodes.add(pkgNode);
            
            if (loggerIndex == -1
                    && pkgNode.equals(currentLogger)) {
                loggerIndex = nodes.size()-1;
            }
            
            for(Iterator it2 = pkgNode.getNodes().iterator(); it2.hasNext(); ) {
                ClassNode classNode = (ClassNode)it2.next();
                nodes.add(classNode);
                
                if (loggerIndex == -1 
                        && classNode.equals(currentLogger)) {
                    loggerIndex = nodes.size()-1;
                }
            }
        }
        
        loggerModel.refreshLoggers(nodes);
        
        boolean empty = nodes.isEmpty();
        loggerComboBox.setEnabled(!empty);
        levelComboBox.setEnabled(!empty);
        apply.setEnabled(!empty);
        
        if (!empty) {
            loggerComboBox.setSelectedIndex(loggerIndex >= 0 ? loggerIndex : 0);
            selectLoggerLevel();
        }
    }

    /**
     * Selects the Level of the currently selected Logger
     */
    private void selectLoggerLevel() {
        LoggerComboBoxModel loggerModel = (LoggerComboBoxModel) loggerComboBox.getModel();
        LevelComboBoxModel levelModel = (LevelComboBoxModel) levelComboBox.getModel();
        
        int loggerIndex = loggerComboBox.getSelectedIndex();
        if (loggerIndex < 0)
            return;
        
        Level level = getLevel(loggerModel.getLogger(loggerIndex));

        levelModel.setSelectedItem(level);
    }
    
    /**
     * Applies the currently selected logging level
     */
    private void applyLevel() {
        LoggerComboBoxModel loggerModel = (LoggerComboBoxModel) loggerComboBox.getModel();
        LevelComboBoxModel levelModel = (LevelComboBoxModel) levelComboBox.getModel();

        int loggerIndex = loggerComboBox.getSelectedIndex();
        if (loggerIndex < 0)
            return;
        
        LoggerNode logger = loggerModel.getLogger(loggerIndex);
        Level currentLevel = getLevel(logger);

        int levelIndex = levelComboBox.getSelectedIndex();
        Level newLevel = (levelIndex > 0) ? levelModel.getLevel(levelIndex) : null;

        if (!currentLevel.equals(newLevel)) {
            logger.setLevel(newLevel);
            loggerComboBox.setSelectedIndex(loggerIndex); // update the ComboxBox  (the text)
            loggerModel.updateIndex(loggerIndex);
        }
    }

    /**
     * Appends text to the console.
     * 
     * @param text
     *            The text to be appended
     */
    public void appendText(final String text) {
        if (!output.isEnabled()) {
            return;
        }
        
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                output.append(text);

                int excess = output.getDocument().getLength() - idealSize;
                if (excess >= maxExcess) {
                    output.replaceRange("", 0, excess);
                }
                if (scroll)
                    output.setCaretPosition(output.getText().length());
            }
        });
    }

    /**
     * Clears the console.
     */
    public void clear() {
        output.setText(null);
    }

    /**
     * Saves the current Console output and the stack traces of
     * all active Threads if available
     */
    public void save() {
        try {
            output.setEnabled(altCtrlDown);
            
            String log = output.getText().trim();
            String traces = CommonUtils.getAllStackTraces();
            
            if (log.length() == 0 
                    && traces.length() == 0) {
                return;
            }
            
            if (altCtrlDown) {
                StringBuffer buffer = new StringBuffer();
                buffer.append("-- BEGIN STACK TRACES --\n");
                buffer.append(traces.length() > 0 ? traces : "NONE");
                buffer.append("\n-- END STACK TRACES --\n");
                appendText(buffer.toString());
            } else {
                StringBuffer buffer = new StringBuffer();
                buffer.append(new Date()).append("\n\n");
                
                Exception e = new Exception() {
                    public void printStackTrace(PrintWriter out) {
                        /* PRINT NOTHING */
                    }
                };
                
                LocalClientInfo info =
                    new LocalClientInfo(e, Thread.currentThread().getName(), "Console Log", false);
                buffer.append(info.toBugReport());
                
                buffer.append("-- BEGIN STACK TRACES --\n");
                buffer.append(traces.length() > 0 ? traces : "NONE");
                buffer.append("\n-- END STACK TRACES --\n");
            
                buffer.append("\n-- BEGIN LOG --\n");
                buffer.append(log.length() > 0 ? log : "NONE");
                buffer.append("\n-- END LOG --\n");
                
                File file = FileChooserHandler.getSaveAsFile(GUIMediator.getAppFrame(), 
                        "CONSOLE_DIALOG_SAVE_TITLE", new File("limewire-log.txt"));
                if (file == null) {
                    return;
                }
                
                BufferedWriter out = new BufferedWriter(new FileWriter(file));
                out.write(buffer.toString());
                out.close();
            }
        } catch (IOException err) {
            ErrorService.error(err);
        } finally {
            output.setEnabled(true);
        }
    }
    
    /**
     * Updates the appearance of this panel based on the current theme.
     */
    public void updateTheme() {
        Color tableColor = ThemeFileHandler.TABLE_BACKGROUND_COLOR.getValue();
        scrollPane.getViewport().setBackground(tableColor);
    }

    /**
     * Returns Level.OFF instead of null if logging is turned off
     */
    private static final Level getLevel(LoggerNode logger) {
        Level level = logger.getLevel();
        if (level == null)
            level = Level.OFF;
        return level;
    }
    
    private final class ConsoleWriter extends Writer {

        private StringBuffer buffer = new StringBuffer();

        public void write(char[] cbuf, int off, int len) {
            buffer.append(cbuf, off, len);
        }

        public void close() {
            buffer = null;
        }

        public void flush() {
            Console.this.appendText(buffer.toString());
            buffer.setLength(0);
        }
    }
    
    /**
     * Logger ComboBox model
     */
    private static class LoggerComboBoxModel extends DefaultComboBoxModel {
        
        private static final String SPACER = "    ";
        
        private List nodes = Collections.EMPTY_LIST;
        
        private void updateIndex(int index) {
            fireContentsChanged(this, index, index);
        }
        
        private void refreshLoggers(List nodes) {
            this.nodes = nodes;
            fireContentsChanged(this, 0, nodes.size());
        }

        public int getSize() {
            return nodes.size();
        }

        private LoggerNode getLogger(int index) {
            return (LoggerNode)nodes.get(index);
        }

        public Object getElementAt(int index) {
            LoggerNode logger = getLogger(index);
            Level level = getLevel(logger);
            
            if (level.equals(Level.OFF)) {
                if (logger.isLeaf()) {
                    return SPACER + logger.getName();
                } else {
                    return logger.getName();
                }
            } else {
                if (logger.isLeaf()) {
                    return SPACER + logger.getName() + " [" + level + "]";
                } else {
                    return logger.getName();
                }
            }
        }
    }

    /**
     * Logging level ComboBox model
     */
    private class LevelComboBoxModel extends DefaultComboBoxModel {

        private final Level[] levels = new Level[] { 
                Level.OFF, 
                Level.ALL,
                Level.DEBUG, 
                Level.ERROR, 
                Level.FATAL, 
                Level.INFO, 
                Level.WARN 
        };

        public int getSize() {
            return levels.length;
        }

        private Level getLevel(int index) {
            return levels[index];
        }

        public Object getElementAt(int index) {
            return getLevel(index).toString();
        }
    }
    
    /**
     * A interface to build a very simple Tree of 
     * Packages and Classes
     */
    private interface LoggerNode {
        boolean isLeaf();
        Level getLevel();
        void setLevel(Level level);
        String getName();
    }
    
    private static class PackageNode implements LoggerNode {
        
        private String pkg;
        private ArrayList classNodes = new ArrayList();
        
        private PackageNode(String pkg) {
            this.pkg = pkg;
        }
        
        public void add(Logger logger) {
            classNodes.add(new ClassNode(this, logger));
        }
        
        public Level getLevel() {
            return Level.OFF;
        }
        
        public void setLevel(Level level) {
            for(int i = classNodes.size()-1; i >= 0; i--) {
                ((ClassNode)classNodes.get(i)).setLevel(level);
            }
        }
        
        public boolean isLeaf() {
            return false;
        }
        
        public void sort() {
            Collections.sort(classNodes, new Comparator() {
                public int compare(Object o1, Object o2) {
                    return ((ClassNode) o1).getName().compareTo(((ClassNode) o2).getName());
                }
            });
        }
        
        public List getNodes() {
            return classNodes;
        }
        
        public int hashCode() {
            return pkg.hashCode();
        }
        
        public boolean equals(Object o) {
            if (!(o instanceof PackageNode)) {
                return false;
            }
            return pkg.equals(((PackageNode)o).pkg);
        }
        
        public String getName() {
            return pkg;
        }
        
        public String toString() {
            return getName();
        }
        
        private static String getPackage(Logger logger) {
            String name = logger.getName();
            return name.substring(0, name.lastIndexOf('.')) + ".*";
        }
    }
    
    private static class ClassNode implements LoggerNode {
        
        private PackageNode parent;
        private Logger logger;
        
        private ClassNode(PackageNode parent, Logger logger) {
            this.parent = parent;
            this.logger = logger;
        }
        
        public PackageNode getParent() {
            return parent;
        }
        
        public Logger getLogger() {
            return logger;
        }
        
        public Level getLevel() {
            return logger.getLevel();
        }
        
        public void setLevel(Level level) {
            logger.setLevel(level);
        }
        
        public boolean isLeaf() {
            return true;
        }
        
        public String getName() {
            return logger.getName();
        }
        
        public int hashCode() {
            return getName().hashCode();
        }
        
        public boolean equals(Object o) {
            if (!(o instanceof ClassNode)) {
                return false;
            }
            return getName().equals(((ClassNode)o).getName());
        }
        
        public String toString() {
            return getName();
        }
    }
}
