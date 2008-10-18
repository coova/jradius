/**
 * JRadiusSimulator
 * Copyright (C) 2004-2005 PicoPoint, B.V.
 * Copyright (C) 2006-2007 David Bird <david@coova.com>
 * 
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation; either version 2 of the License, or (at your
 * option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License 
 * for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package net.jradius.client.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Event;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.URL;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;

import javax.swing.AbstractButton;
import javax.swing.AbstractCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;
import javax.swing.text.NumberFormatter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import net.jradius.client.RadiusClient;
import net.jradius.client.auth.EAPAKAAuthenticator;
import net.jradius.client.auth.RadiusAuthenticator;
import net.jradius.client.auth.TunnelAuthenticator;
import net.jradius.dictionary.Attr_AcctSessionId;
import net.jradius.dictionary.Attr_AcctStatusType;
import net.jradius.dictionary.Attr_Class;
import net.jradius.dictionary.Attr_EAPAkaCK;
import net.jradius.dictionary.Attr_EAPAkaIK;
import net.jradius.dictionary.Attr_ReplyMessage;
import net.jradius.exception.RadiusException;
import net.jradius.exception.StandardViolatedException;
import net.jradius.packet.AccessReject;
import net.jradius.packet.AccessRequest;
import net.jradius.packet.AccountingRequest;
import net.jradius.packet.CoARequest;
import net.jradius.packet.DisconnectRequest;
import net.jradius.packet.RadiusPacket;
import net.jradius.packet.RadiusRequest;
import net.jradius.packet.RadiusResponse;
import net.jradius.packet.attribute.AttributeFactory;
import net.jradius.packet.attribute.AttributeList;
import net.jradius.packet.attribute.RadiusAttribute;
import net.jradius.packet.attribute.VSADictionary;
import net.jradius.packet.attribute.AttributeFactory.VendorValue;
import net.jradius.packet.attribute.value.IntegerValue;
import net.jradius.packet.attribute.value.NamedValue;
import net.jradius.packet.attribute.value.NamedValue.NamedValueMap;
import net.jradius.standard.IRAPStandard;
import net.jradius.standard.RadiusStandard;
import net.jradius.standard.WISPrStandard;
import net.jradius.util.Base64;
import net.jradius.util.RadiusRandom;

/**
 * Java Swing Graphical User Interface for the JRadius RADIUS Client.
 * @author David Bird
 */
public class JRadiusSimulator extends JFrame implements Runnable
{
    private static final long serialVersionUID = (long)0;
    public  static final String logSepLine = "----------------------------------------------------------";
    private static String configFileUrl = "file:///" + System.getProperty("user.home") + "/.jRadiusSimulator";
    private String[] authTypeNames = { "PAP", "CHAP", "MSCHAPv1", "MSCHAPv2", "EAP-MD5", "EAP-MSCHAPv2", "EAP-TLS", "PEAP", "EAP-TTLS/PAP" };
    private String[] keystoreTypes = { "PKCS12", "JKS" };
    private Properties properties;
    private Thread[] simulationThreads = null;
    boolean interactiveSession = false;
    private final HashMap namedValueComponentCache = new HashMap();
    private NumberFormatter numberFormatter;
    private PrintStream logSent;
    private PrintStream logRecv;
    private PrintStream logErr;
    private AttributesTableModel attributesTableModel = new AttributesTableModel();
    private JMenuBar jJMenuBar = null;
    private JMenu fileMenu = null;
    private JMenu helpMenu = null;
    private JMenuItem exitMenuItem = null;
    private JMenuItem aboutMenuItem = null;
    private JMenuItem saveMenuItem = null;
    private JMenuItem saveAsMenuItem = null;
    private JMenuItem openMenuItem = null;
    private JMenuItem openUrlMenuItem = null;
    private JTabbedPane mainTabPane = null;
    private JPanel radiusPanel = null;
    private JPanel diameterPanel = null;
    private JPanel dhcpPanel = null;
    private JPanel logPanel = null;
    private JPanel runStatusPanel = null;
    private LogConsole logConsole = null;
    private JPanel logButtonPanel = null;
    private JPanel mainContentPane = null;
    private JButton clearLogButton = null;
    private JButton saveLogButton = null;
    private JToggleButton runButton = null;
    private JScrollPane attributesTableScrollPane = null;
    private JTable attributesTable = null;
    private JPanel sendOptionsPanel = null;
    private JButton addAttributeButton = null;
    private JDialog addAttributeDialog = null; 
    private JPanel addAttributeContentPane = null;
    private JTree attributeTree = null;
    private JPanel addAttributePanel = null;
    private JComboBox authTypeComboBox = null;
    private JComboBox checkStandardComboBox = null;
    private JPanel attributesPanel = null;
    private JPanel keysPanel = null;
    private JPanel keysOptionsPanel = null;
    private JTextField radiusServerTextField = null;
    private JTextField tlsKeyFileTextField = null;
    private JTextField tlsKeyPasswordTextField = null;
    private JTextField tlsCAFileTextField = null;
    private JTextField tlsCAPasswordTextField = null;
    private JTextField akaIKTextField = null;
    private JTextField akaCKTextField = null;
    private JFormattedTextField requestersTextField = null;
    private JFormattedTextField requestsTextField = null;
    private JComboBox tlsKeyFileTypeComboBox = null;
    private JComboBox tlsCAFileTypeComboBox = null;
    private JCheckBox tlsTrustAll = null;
    private JCheckBox tlsUseJavaRootCA = null;    
    private JLabel radiusServerLabel = null;
    private JTextField sharedSecretTextField = null;
    private JButton doneButton = null;
    private JScrollPane attributeTreeScrollPane = null;
    private JPanel attributeTreeScrollPanel = null;
    private JButton addButton = null;
    private JComboBox simulationTypeComboBox = null;
    private JPanel attributesButtonPanel = null;
    private JButton removeAttributeButton = null;
    private JButton moveUpButton = null;
    private JButton moveDownButton = null;
    private JFormattedTextField radiusAuthPortTextField = null;
    private JFormattedTextField radiusAcctPortTextField = null;
    private JFormattedTextField radiusRetriesTextField = null;
    private JFormattedTextField radiusTimeoutTextField = null;
    private JFormattedTextField radiusInterimIntervalTextField = null;
    private JFormattedTextField sessionTimeTextField = null;
    private JCheckBox generateAcctSessionIdCheckBox = null;
    private JCheckBox notStopOnRejectCheckBox = null;
    private JCheckBox notSendClassAttribute = null;
    private JDialog openUrlDialog = null;
    private JPanel openUrlContentPane = null;
    private JTextField openUrlTextField = null;
    private JButton openUrlButton = null;
    private JLabel openUrlStatusLabel = null;
    private JButton cancelUrlButton = null;
    private JLabel statusLabel = null;
    private boolean isJava14 = false;
    
    /**
     * This is the default constructor
     */
    public JRadiusSimulator() {
        super();
 
 //       Security.addProvider(new BouncyCastleProvider());
        String version = System.getProperty("java.version");
        if (version.startsWith("1.4")) 
        {
            isJava14 = true;
            for (int i = 0; i < authTypeNames.length; i++)
            {
                if (authTypeNames[i].startsWith("EAP-T"))
                {
                    authTypeNames[i] += " (requires Java 1.5)";
                }
            }
        }
        initialize();
    }

    private boolean windows;
    
    /**
     * This method initializes this
     * 
     * @return void
     */
    private void initialize() {
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setJMenuBar(getJJMenuBar());
        this.setSize(650, 500);
        this.setContentPane(getMainContentPane());
        this.setTitle("JRadiusSimulator");
        this.setVisible(true);
        
        String osName = System.getProperty("os.name");
        if  (osName.indexOf("Windows") != -1) windows = true;
        else windows = false;

        logSent = new PrintStream(logConsole.createFilteredStream(LogConsole.CATEGORY_PACKETS_SENT));
        logRecv = new PrintStream(logConsole.createFilteredStream(LogConsole.CATEGORY_PACKETS_RECV));
        logErr = new PrintStream(logConsole.createFilteredStream(LogConsole.CATEGORY_ERROR));

        AttributeFactory.loadAttributeDictionary("net.jradius.dictionary.AttributeDictionaryImpl");
        try { loadConfigFile(configFileUrl); } catch (Exception e) { e.printStackTrace(); }
        statusLabel.setText("Ready");
        fileMenu.setEnabled(true);
        mainTabPane.setEnabled(true);
        runButton.setEnabled(true);
    }
    
    private void loadConfigFile(String url) throws IOException
    {
        this.properties = new Properties();
        loadConfigFile(new URL(url).openStream());
    }
    
    private void loadConfigFile(InputStream inputStream) throws IOException
    {
        this.properties.load(inputStream);
        
        String s = this.properties.getProperty("AttributesTableEntries");
        if (s != null)
        {
            attributesTableModel.setEntries((ArrayList)Base64.decodeToObject(s));
            attributesTableModel.fireTableDataChanged();
        }
        
        s = this.properties.getProperty("RadiusServer");
        if (s != null) radiusServerTextField.setText(s);

        s = this.properties.getProperty("SharedSecret");
        if (s != null) sharedSecretTextField.setText(s);

        s = this.properties.getProperty("AuthPort");
        if (s != null) radiusAuthPortTextField.setValue(new Integer(s));

        s = this.properties.getProperty("AcctPort");
        if (s != null) radiusAcctPortTextField.setValue(new Integer(s));

        s = this.properties.getProperty("Retries");
        if (s != null) radiusRetriesTextField.setValue(new Integer(s));

        s = this.properties.getProperty("Timeout");
        if (s != null) radiusTimeoutTextField.setValue(new Integer(s));

        s = this.properties.getProperty("Requesters");
        if (s != null) requestersTextField.setValue(new Integer(s));

        s = this.properties.getProperty("Requests");
        if (s != null) requestsTextField.setValue(new Integer(s));

        s = this.properties.getProperty("SimulationType");
        if (s != null) try { simulationTypeComboBox.setSelectedIndex(Integer.parseInt(s)); } catch (Exception e) { }

        s = this.properties.getProperty("AuthType");
        if (s != null) try { authTypeComboBox.setSelectedIndex(Integer.parseInt(s)); } catch (Exception e) { }

        s = this.properties.getProperty("CheckStandard");
        if (s != null) try { checkStandardComboBox.setSelectedIndex(Integer.parseInt(s)); } catch (Exception e) { }

        s = this.properties.getProperty("GenerateAcctSessionId");
        if (s != null) try { generateAcctSessionIdCheckBox.setSelected(new Boolean(s).booleanValue()); } catch (Exception e) { }

        s = this.properties.getProperty("StopOnReject");
        if (s != null) try { notStopOnRejectCheckBox.setSelected(new Boolean(s).booleanValue()); } catch (Exception e) { }

        s = this.properties.getProperty("SendClassAttr");
        if (s != null) try { notSendClassAttribute.setSelected(new Boolean(s).booleanValue()); } catch (Exception e) { }

        s = this.properties.getProperty("AKAIK");
        if (s != null) akaIKTextField.setText(s);

        s = this.properties.getProperty("AKACK");
        if (s != null) akaCKTextField.setText(s);

        s = this.properties.getProperty("TLSKeyFile");
        if (s != null) tlsKeyFileTextField.setText(s);

        s = this.properties.getProperty("TLSKeyPassword");
        if (s != null) tlsKeyPasswordTextField.setText(s);

        s = this.properties.getProperty("TLSCAFile");
        if (s != null) tlsCAFileTextField.setText(s);

        s = this.properties.getProperty("TLSCAPassword");
        if (s != null) tlsCAPasswordTextField.setText(s);

        s = this.properties.getProperty("TLSKeyFileType");
        if (s != null) try { tlsKeyFileTypeComboBox.setSelectedIndex(Integer.parseInt(s)); } catch (Exception e) { }

        s = this.properties.getProperty("TLSCAFileType");
        if (s != null) try { tlsCAFileTypeComboBox.setSelectedIndex(Integer.parseInt(s)); } catch (Exception e) { }

        s = this.properties.getProperty("TLSTrustAll");
        if (s != null) try { tlsTrustAll.setSelected(new Boolean(s).booleanValue()); } catch (Exception e) { }

        //s = this.properties.getProperty("TLSUseJavaCA");
        //if (s != null) try { tlsUseJavaRootCA.setSelected(new Boolean(s).booleanValue()); } catch (Exception e) { }
    }
    
    private void saveConfigFile(String fileName)
    {
        try
        {
            String encodedAttributes = Base64.encodeObject(attributesTableModel.getEntries(), Base64.GZIP | Base64.DONT_BREAK_LINES);
            this.properties.setProperty("AttributesTableEntries", encodedAttributes);
            this.properties.setProperty("RadiusServer", radiusServerTextField.getText());
            this.properties.setProperty("SharedSecret", sharedSecretTextField.getText());
            this.properties.setProperty("SimulationType", "" + simulationTypeComboBox.getSelectedIndex());
            this.properties.setProperty("AuthType", "" + authTypeComboBox.getSelectedIndex());
            this.properties.setProperty("CheckStandard", "" + checkStandardComboBox.getSelectedIndex());
            this.properties.setProperty("AuthPort", ((Integer)radiusAuthPortTextField.getValue()).toString());
            this.properties.setProperty("AcctPort", ((Integer)radiusAcctPortTextField.getValue()).toString());
            this.properties.setProperty("Retries", ((Integer)radiusRetriesTextField.getValue()).toString());
            this.properties.setProperty("Timeout", ((Integer)radiusTimeoutTextField.getValue()).toString());
            this.properties.setProperty("Requesters", ((Integer)requestersTextField.getValue()).toString());
            this.properties.setProperty("Requests", ((Integer)requestsTextField.getValue()).toString());
            this.properties.setProperty("GenerateAcctSessionId", Boolean.toString(generateAcctSessionIdCheckBox.isSelected()));
            this.properties.setProperty("StopOnReject", Boolean.toString(notStopOnRejectCheckBox.isSelected()));
            this.properties.setProperty("SendClassAttr", Boolean.toString(notSendClassAttribute.isSelected()));
            this.properties.setProperty("AKAIK", akaIKTextField.getText());
            this.properties.setProperty("AKACK", akaCKTextField.getText());
            this.properties.setProperty("TLSKeyFile", tlsKeyFileTextField.getText());
            this.properties.setProperty("TLSKeyPassword", tlsKeyPasswordTextField.getText());
            this.properties.setProperty("TLSCAFile", tlsCAFileTextField.getText());
            this.properties.setProperty("TLSCAPassword", tlsCAPasswordTextField.getText());
            this.properties.setProperty("TLSKeyFileType", "" + tlsKeyFileTypeComboBox.getSelectedIndex());
            this.properties.setProperty("TLSCAFileType", "" + tlsCAFileTypeComboBox.getSelectedIndex());
            this.properties.setProperty("TLSTrustAll", Boolean.toString(tlsTrustAll.isSelected()));
            //this.properties.setProperty("TLSUseJavaCA", Boolean.toString(tlsUseJavaRootCA.isSelected()));
            FileOutputStream fileOutputStream = new FileOutputStream(fileName);
            this.properties.store(fileOutputStream, "JRadiusSimulator");
        }
        catch(Exception e) { e.printStackTrace(); }
    }

    public static void setConfigFileUrl(String fileName)
    {
        configFileUrl = fileName;
    }
    
    /**
     * This method initializes jJMenuBar
     * 
     * @return javax.swing.JMenuBar
     */
    private JMenuBar getJJMenuBar() {
        if (jJMenuBar == null) {
            jJMenuBar = new JMenuBar();
            jJMenuBar.add(getFileMenu());
            jJMenuBar.add(getHelpMenu());
        }
        return jJMenuBar;
    }

    /**
     * This method initializes jMenu
     * 
     * @return javax.swing.JMenu
     */
    private JMenu getFileMenu() {
        if (fileMenu == null) {
            fileMenu = new JMenu();
            fileMenu.setText("File");
            fileMenu.add(getSaveMenuItem());
            fileMenu.add(getSaveAsMenuItem());
            fileMenu.add(getOpenMenuItem());
            fileMenu.add(getOpenUrlMenuItem());
            fileMenu.add(getExitMenuItem());
            fileMenu.setEnabled(false);
        }
        return fileMenu;
    }

    /**
     * This method initializes jMenu
     * 
     * @return javax.swing.JMenu
     */
    private JMenu getHelpMenu() {
        if (helpMenu == null) {
            helpMenu = new JMenu();
            helpMenu.setText("Help");
            helpMenu.add(getAboutMenuItem());
        }
        return helpMenu;
    }

    /**
     * This method initializes jMenuItem
     * 
     * @return javax.swing.JMenuItem
     */
    private JMenuItem getExitMenuItem() {
        if (exitMenuItem == null) {
            exitMenuItem = new JMenuItem();
            exitMenuItem.setText("Exit");
            exitMenuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    System.exit(0);
                }
            });
        }
        return exitMenuItem;
    }

    /**
     * This method initializes jMenuItem
     * 
     * @return javax.swing.JMenuItem
     */
    private JMenuItem getAboutMenuItem() {
        if (aboutMenuItem == null) {
            aboutMenuItem = new JMenuItem();
            aboutMenuItem.setText("About");
            aboutMenuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    JOptionPane.showMessageDialog(JRadiusSimulator.this, 
                            "Version 1.1.0\n\n" +
                            "For help, go to http://jradius.net/\n" +
                            "Licensed under the GNU Public License (GPL).\n" + 
                            "Copyright (c) 2006 PicoPoint B.V.\n" +
                            "Copyright (c) 2007-2008 David Bird\n",
                            "About JRadiusSimulator", JOptionPane.INFORMATION_MESSAGE, null);
                }
            });
        }
        return aboutMenuItem;
    }

    /**
     * This method initializes jMenuItem
     * 
     * @return javax.swing.JMenuItem
     */
    private JMenuItem getSaveMenuItem() {
        if (saveMenuItem == null) {
            saveMenuItem = new JMenuItem();
            saveMenuItem.setText("Save");
            saveMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, Event.CTRL_MASK, true));
            saveMenuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (configFileUrl.startsWith("file:///"))
                        saveConfigFile(configFileUrl.substring(7));
                    else
                        doSaveAs();
                }
            });
        }
        return saveMenuItem;
    }

    /**
     * This method initializes jMenuItem
     * 
     * @return javax.swing.JMenuItem
     */
    private JMenuItem getSaveAsMenuItem() {
        if (saveAsMenuItem == null) {
            saveAsMenuItem = new JMenuItem();
            saveAsMenuItem.setText("Save As");
            saveAsMenuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    doSaveAs();
                }
            });
        }
        return saveAsMenuItem;
    }

    private void doSaveAs()
    {
        JFileChooser chooser = new JFileChooser();
        int returnVal = chooser.showSaveDialog(JRadiusSimulator.this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            saveConfigFile(file.getAbsolutePath());
        }
    }
    
    /**
     * This method initializes jMenuItem
     * 
     * @return javax.swing.JMenuItem
     */
    private JMenuItem getOpenMenuItem() {
        if (openMenuItem == null) {
            openMenuItem = new JMenuItem();
            openMenuItem.setText("Open");
            openMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, Event.CTRL_MASK, true));
            openMenuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    JFileChooser chooser = new JFileChooser();
                    int returnVal = chooser.showOpenDialog(JRadiusSimulator.this);
                    if (returnVal == JFileChooser.APPROVE_OPTION) {
                        File file = chooser.getSelectedFile();
                        try
                        {
                            loadConfigFile("file:///" + file.getAbsolutePath());
                        }
                        catch(Exception ex) 
                        {
                            JOptionPane.showMessageDialog(null, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                            ex.printStackTrace(); 
                        }
                    }
                }
            });
        }
        return openMenuItem;
    }

    /**
     * This method initializes jMenuItem
     * 
     * @return javax.swing.JMenuItem
     */
    private JMenuItem getOpenUrlMenuItem() {
        if (openUrlMenuItem == null) {
            openUrlMenuItem = new JMenuItem();
            openUrlMenuItem.setText("Open Url");
            openUrlMenuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    getOpenUrlDialog().setVisible(true);
                }
            });
        }
        return openUrlMenuItem;
    }

    /**
     * This method initializes mainTabPane
     * 
     * @return javax.swing.JTabbedPane
     */
    private JTabbedPane getMainTabPane() {
        if (mainTabPane == null) {
            mainTabPane = new JTabbedPane();
            mainTabPane.addTab("RADIUS", null, getRADIUSPanel(), null);
            //mainTabPane.addTab("Diameter", null, getDiameterPanel(), null);
            //mainTabPane.addTab("DHCP", null, getDHCPPanel(), null);
            mainTabPane.addTab("Attributes", null, getAttributesPanel(), null);
            mainTabPane.addTab("Keys", null, getKeysPanel(), null);
            mainTabPane.addTab("Log", null, getLogPanel(), null);
            mainTabPane.setEnabled(false);
        }
        return mainTabPane;
    }

    /**
     * This method initializes radiusPanel
     * 
     * @return javax.swing.JPanel
     */
    private JPanel getRADIUSPanel() {
        if (radiusPanel == null) {
            radiusPanel = new JPanel();
            radiusPanel.setLayout(new BorderLayout());
            radiusPanel.add(getRunStatusPanel(), java.awt.BorderLayout.SOUTH);
            radiusPanel.add(getSendOptionsPanel(), java.awt.BorderLayout.NORTH);
        }
        return radiusPanel;
    }

    /**
     * This method initializes diameterPanel
     * 
     * @return javax.swing.JPanel
     */
    private JPanel getDHCPPanel() {
        if (dhcpPanel == null) {
            dhcpPanel = new JPanel();
            dhcpPanel.setLayout(new BorderLayout());
        }
        return dhcpPanel;
    }

    /**
     * This method initializes diameterPanel
     * 
     * @return javax.swing.JPanel
     */
    private JPanel getDiameterPanel() {
        if (diameterPanel == null) {
            diameterPanel = new JPanel();
            diameterPanel.setLayout(new BorderLayout());
        }
        return diameterPanel;
    }

    /**
     * This method initializes logPanel
     * 
     * @return javax.swing.JPanel
     */
    private JPanel getLogPanel() {
        if (logPanel == null) {
            logPanel = new JPanel();
            logPanel.setLayout(new BorderLayout());
            logPanel.add(getLogConsole(), BorderLayout.CENTER);
            logPanel.add(getLogButtonPanel(), java.awt.BorderLayout.SOUTH);
        }
        return logPanel;
    }

    /**
     * This method initializes progressBarPanel
     * 
     * @return javax.swing.JPanel
     */
    private JPanel getRunStatusPanel() {
        if (runStatusPanel == null) {
            statusLabel = new JLabel();
            statusLabel.setText("Initializing...");
            runStatusPanel = new JPanel();
            runStatusPanel.setLayout(new BorderLayout());
            runStatusPanel.add(getRunButton(), java.awt.BorderLayout.EAST);
            runStatusPanel.add(statusLabel, java.awt.BorderLayout.CENTER);
        }
        return runStatusPanel;
    }

    /**
     * This method initializes logConsole
     * 
     * @return javax.swing.JTextPane
     */
    private LogConsole getLogConsole() {
        if (logConsole == null) {
            logConsole = new LogConsole();
        }
        return logConsole;
    }

    /**
     * This method initializes logButtonPanel
     * 
     * @return javax.swing.JPanel
     */
    private JPanel getLogButtonPanel() {
        if (logButtonPanel == null) {
            logButtonPanel = new JPanel();
            logButtonPanel.add(getClearLogButton(), null);
            logButtonPanel.add(getSaveLogButton(), null);
        }
        return logButtonPanel;
    }

    /**
     * This method initializes mainContentPane
     * 
     * @return javax.swing.JPanel
     */
    private JPanel getMainContentPane() {
        if (mainContentPane == null) {
            mainContentPane = new JPanel();
            mainContentPane.setLayout(new BorderLayout());
            mainContentPane.add(getMainTabPane(), java.awt.BorderLayout.CENTER);
        }
        return mainContentPane;
    }

    /**
     * This method initializes clearLogButton
     * 
     * @return javax.swing.JButton
     */
    private JButton getClearLogButton() {
        if (clearLogButton == null) {
            clearLogButton = new JButton();
            clearLogButton.setText("Clear");
            clearLogButton.addActionListener(new java.awt.event.ActionListener() { 
            	public void actionPerformed(java.awt.event.ActionEvent e) {    
                    logConsole.setText("");
            	}
            });
        }
        return clearLogButton;
    }

    /**
     * This method initializes saveLogButton
     * 
     * @return javax.swing.JButton
     */
    private JButton getSaveLogButton() {
        if (saveLogButton == null) {
            saveLogButton = new JButton();
            saveLogButton.setText("Save");
            saveLogButton.addActionListener(new java.awt.event.ActionListener() { 
            	public void actionPerformed(java.awt.event.ActionEvent e) {    
            		JFileChooser chooser = new JFileChooser();
            		int returnVal = chooser.showSaveDialog(JRadiusSimulator.this);
            		if (returnVal == JFileChooser.APPROVE_OPTION) {
            		    File file = chooser.getSelectedFile();
            		    try
            		    {
            		        FileOutputStream out = new FileOutputStream(file);
            		        out.write(logConsole.getText().getBytes());
            		        out.close();
            		    }
                     catch(Exception ex)
                     {
                         System.err.println(ex.getMessage());
                     }
            		}
            	}
            });
        }
        return saveLogButton;
    }

    /**
     * This method initializes sendButton
     * 
     * @return javax.swing.JButton
     */
    private JToggleButton getRunButton() {
        if (runButton == null) {
            runButton = new JToggleButton();
            runButton.setText("Start");
            
            runButton.addActionListener(new java.awt.event.ActionListener() { 
            	public void actionPerformed(java.awt.event.ActionEvent e) {    
                    AbstractButton ab = (AbstractButton)e.getSource();
                    if (ab.isSelected())
                    {
                        simulationThreads = new Thread[(Integer)requestersTextField.getValue()];
                        for (int i=0; i < simulationThreads.length; i++)
                        {
                            simulationThreads[i] = new Thread(JRadiusSimulator.this);
                            simulationThreads[i].start();
                        }
                        runButton.setText("Stop");
                    }
                    else
                    {
                        if (simulationThreads != null)
                        {
                            for (int i=0; i < simulationThreads.length; i++)
                            {
                                simulationThreads[i].interrupt();
                            }
                        }
                        simulationThreads = null;
                        statusLabel.setText("Ready");
                        runButton.setText("Start");
                    }
            	}
            });
            
            runButton.setEnabled(false);
        }
        return runButton;
    }
    
    /**
     * This method initializes attributesTableScrollPane
     * 
     * @return javax.swing.JScrollPane
     */
    private JScrollPane getAttributesTableScrollPane() 
    {
        if (attributesTableScrollPane == null) 
        {
            attributesTableScrollPane = new JScrollPane();
            attributesTableScrollPane.setViewportView(getAttributesTable());
        }
        return attributesTableScrollPane;
    }

    private class ValueTableCellEditor extends AbstractCellEditor implements TableCellEditor 
    {
        private static final long serialVersionUID = (long)0;
        private JComponent component;
    
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int rowIndex, int colIndex) {

            if (isSelected) {
                
            }

            AttributesTableEntry entry = (AttributesTableEntry)attributesTableModel.getEntries().get(rowIndex);
            if (entry.getValueClass().equals(NamedValue.class))
            {
                component = createNamedValueCellEditor(entry.getAttributeName());
                ((JComboBox)component).setSelectedItem(value);
            }
            else if (entry.getValueClass().equals(IntegerValue.class))
            {
                JFormattedTextField ftf = new JFormattedTextField(getNumberFormatter());
                Integer iValue = null;
                try { iValue = new Integer((String)value); } catch (Exception e) { iValue = new Integer(0); }
                ftf.setValue(iValue);
                component = ftf;
            }
            else
            {
                component = new JTextField();
                ((JTextField)component).setText((String)value);
            }

            component.setBorder(null);
            return component;
        }
    
        public Object getCellEditorValue() 
        {
            if (component instanceof JComboBox) return ((JComboBox)component).getSelectedItem();
            if (component instanceof JFormattedTextField) return ((Integer)((JFormattedTextField)component).getValue()).toString();
            return ((JTextField)component).getText();
        }

        public boolean stopCellEditing() 
        {
            if (component instanceof JFormattedTextField) 
            {
                JFormattedTextField ftf = (JFormattedTextField)component;
                if (ftf.isEditValid()) 
                {
                    try { ftf.commitEdit(); } catch (java.text.ParseException exc) { }
                }
                else
                {
                    return false;
                }
            }
            return super.stopCellEditing();
        }
    }

    /**
     * This method initializes attributesTable
     * 
     * @return javax.swing.JTable
     */
    private JTable getAttributesTable() {
        if (attributesTable == null) {
            attributesTable = new JTable(attributesTableModel);
            TableColumn col = attributesTable.getColumnModel().getColumn(6);
            col.setCellEditor(new ValueTableCellEditor());
            for (int i = 0; i < attributesTableModel.getColumnCount(); i++) {
                col = attributesTable.getColumnModel().getColumn(i);
                if (i == 0 || i == 6) {
                    col.setPreferredWidth(120); 
                } else {
                    col.setPreferredWidth(40);
                }
            }
        }
        return attributesTable;
    }
    
    private JComboBox createNamedValueCellEditor(String attributeName)
    {
        JComboBox comboBox = (JComboBox)namedValueComponentCache.get(attributeName);
        if (comboBox != null) return comboBox;
        try
        {
            RadiusAttribute attribute = AttributeFactory.newAttribute(attributeName);
            NamedValue namedValue = (NamedValue)attribute.getValue();
            NamedValueMap valueMap = namedValue.getMap();
            Long[] possibleValues = valueMap.getKnownValues();
            comboBox = new JComboBox();
            for (int i=0; i<possibleValues.length;i++)
            {
                comboBox.addItem(valueMap.getNamedValue(possibleValues[i]));
            }
            namedValueComponentCache.put(attributeName, comboBox);
        }
        catch (Exception e) { e.printStackTrace(); }
        return comboBox;
    }

    /**
     * This method initializes sendOptionsPanel
     * 
     * @return javax.swing.JPanel
     */
    private JPanel getSendOptionsPanel() {
        if (sendOptionsPanel == null) {
            GridBagConstraints gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
            gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 5);

            GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
            gridBagConstraints1.gridwidth = java.awt.GridBagConstraints.REMAINDER;
            gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints1.insets = new java.awt.Insets(0, 0, 0, 10);
            gridBagConstraints1.weightx = 1.0;

            GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
            gridBagConstraints2.anchor = java.awt.GridBagConstraints.EAST;
            gridBagConstraints2.insets = new java.awt.Insets(0, 10, 0, 5);
            
            GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
            gridBagConstraints3.gridwidth = java.awt.GridBagConstraints.REMAINDER;
            gridBagConstraints3.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints3.insets = new java.awt.Insets(0, 0, 0, 10);
            gridBagConstraints3.weightx = 1.0;

            GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
            gridBagConstraints4.anchor = java.awt.GridBagConstraints.EAST;
            gridBagConstraints4.insets = new java.awt.Insets(0, 10, 0, 5);

            GridBagConstraints gridBagConstraints5 = new GridBagConstraints();
            gridBagConstraints5.gridwidth = java.awt.GridBagConstraints.REMAINDER;
            gridBagConstraints5.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints5.insets = new java.awt.Insets(0, 0, 0, 10);
            gridBagConstraints5.weightx = 1.0;
            
            GridBagConstraints gridBagConstraints6 = new GridBagConstraints();
            gridBagConstraints6.anchor = java.awt.GridBagConstraints.EAST;
            gridBagConstraints6.insets = new java.awt.Insets(0, 10, 0, 5);
            
            GridBagConstraints gridBagConstraints7 = new GridBagConstraints();
            gridBagConstraints7.gridwidth = java.awt.GridBagConstraints.REMAINDER;
            gridBagConstraints7.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints7.insets = new java.awt.Insets(0, 0, 0, 10);
            gridBagConstraints7.weightx = 1.0;
            
            GridBagConstraints gridBagConstraints8 = new GridBagConstraints();
            gridBagConstraints8.anchor = java.awt.GridBagConstraints.EAST;
            gridBagConstraints8.insets = new java.awt.Insets(0, 10, 0, 5);
            
            GridBagConstraints gridBagConstraints9 = new GridBagConstraints();
            gridBagConstraints9.gridwidth = java.awt.GridBagConstraints.REMAINDER;
            gridBagConstraints9.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints9.insets = new java.awt.Insets(0, 0, 0, 10);
            gridBagConstraints9.weightx = 1.0;
            
            GridBagConstraints gridBagConstraints10 = new GridBagConstraints();
            gridBagConstraints10.anchor = java.awt.GridBagConstraints.EAST;
            gridBagConstraints10.insets = new java.awt.Insets(0, 10, 0, 5);
            
            GridBagConstraints gridBagConstraints11 = new GridBagConstraints();
            gridBagConstraints11.gridwidth = java.awt.GridBagConstraints.REMAINDER;
            gridBagConstraints11.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints11.insets = new java.awt.Insets(0, 0, 0, 10);
            gridBagConstraints11.weightx = 1.0;
            
            GridBagConstraints gridBagConstraints12 = new GridBagConstraints();
            gridBagConstraints12.anchor = java.awt.GridBagConstraints.EAST;
            gridBagConstraints12.insets = new java.awt.Insets(0, 10, 0, 5);
            
            GridBagConstraints gridBagConstraints13 = new GridBagConstraints();
            gridBagConstraints13.gridwidth = java.awt.GridBagConstraints.REMAINDER;
            gridBagConstraints13.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints13.insets = new java.awt.Insets(0, 0, 0, 10);
            gridBagConstraints13.weightx = 1.0;
            
            GridBagConstraints gridBagConstraints14 = new GridBagConstraints();
            gridBagConstraints14.anchor = java.awt.GridBagConstraints.EAST;
            gridBagConstraints14.insets = new java.awt.Insets(0, 10, 0, 5);
            
            GridBagConstraints gridBagConstraints15 = new GridBagConstraints();
            gridBagConstraints15.gridwidth = java.awt.GridBagConstraints.REMAINDER;
            gridBagConstraints15.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints15.insets = new java.awt.Insets(0, 0, 0, 10);
            gridBagConstraints15.weightx = 1.0;
            
            GridBagConstraints gridBagConstraints16 = new GridBagConstraints();
            gridBagConstraints16.gridwidth = java.awt.GridBagConstraints.REMAINDER;
            gridBagConstraints16.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints16.insets = new java.awt.Insets(0, 0, 0, 10);
            gridBagConstraints16.weightx = 1.0;
            gridBagConstraints16.gridx = 1;
            
            JLabel  authenticationProtocolLabel = new JLabel();
            authenticationProtocolLabel.setText("Authentication Protocol:");
            
            JLabel sharedSecretLabel = new JLabel();
            sharedSecretLabel.setText("Shared Secret:");
            
            radiusServerLabel = new JLabel();
            radiusServerLabel.setText("RADIUS Server:");

            JLabel simulationTypeLabel = new JLabel();
            simulationTypeLabel.setText("Simulation Type:");

            JLabel checkStandardLabel = new JLabel();
            checkStandardLabel.setText("Verify Standard:");

            JLabel radiusAuthPortLabel = new JLabel();
            radiusAuthPortLabel.setText("Auth Port:");

            JLabel radiusAcctPortLabel = new JLabel();
            radiusAcctPortLabel.setText("Acct Port:");

            JLabel radiusRetriesLabel = new JLabel();
            radiusRetriesLabel.setText("Send Retries:");

            JLabel radiusTimeoutLabel = new JLabel();
            radiusTimeoutLabel.setText("Send Timeout (sec):");

            JLabel radiusInterimIntervalLabel = new JLabel();
            radiusInterimIntervalLabel.setText("Interim Interval (sec):");

            JLabel sessionTimeLabel = new JLabel();
            sessionTimeLabel.setText("Session Duration (sec):");

            JLabel requestersLabel = new JLabel();
            requestersLabel.setText("Requester Threads:");

            JLabel requestsLabel = new JLabel();
            requestsLabel.setText("Requests per Thread:");

            GridBagLayout gridBagLayout = new GridBagLayout();

            sendOptionsPanel = new JPanel();
            sendOptionsPanel.setLayout(gridBagLayout);
            sendOptionsPanel.add(radiusServerLabel, gridBagConstraints);
            sendOptionsPanel.add(getRadiusServerTextField(), gridBagConstraints1);
            sendOptionsPanel.add(sharedSecretLabel, gridBagConstraints2);
            sendOptionsPanel.add(getSharedSecretTextField(), gridBagConstraints3);
            sendOptionsPanel.add(radiusAuthPortLabel, gridBagConstraints8);
            sendOptionsPanel.add(getRadiusAuthPortTextField(), gridBagConstraints9);
            sendOptionsPanel.add(radiusAcctPortLabel, gridBagConstraints10);
            sendOptionsPanel.add(getRadiusAcctPortTextField(), gridBagConstraints11);
            sendOptionsPanel.add(radiusTimeoutLabel, gridBagConstraints12);
            sendOptionsPanel.add(getRadiusTimeoutTextField(), gridBagConstraints13);
            sendOptionsPanel.add(radiusRetriesLabel, gridBagConstraints12);
            sendOptionsPanel.add(getRadiusRetriesTextField(), gridBagConstraints13);
            sendOptionsPanel.add(requestersLabel, gridBagConstraints12);
            sendOptionsPanel.add(getRequestersTextField(), gridBagConstraints13);
            sendOptionsPanel.add(requestsLabel, gridBagConstraints12);
            sendOptionsPanel.add(getRequestsTextField(), gridBagConstraints13);
            sendOptionsPanel.add(simulationTypeLabel, gridBagConstraints4);
            sendOptionsPanel.add(getSimulationTypeComboBox(), gridBagConstraints5);
            sendOptionsPanel.add(authenticationProtocolLabel, gridBagConstraints6);
            sendOptionsPanel.add(getAuthTypeComboBox(), gridBagConstraints7);
            sendOptionsPanel.add(checkStandardLabel, gridBagConstraints6);
            sendOptionsPanel.add(getCheckStandardComboBox(), gridBagConstraints7);
            //sendOptionsPanel.add(sessionTimeLabel, gridBagConstraints12);
            //sendOptionsPanel.add(getSessionTimeTextField(), gridBagConstraints13);
            //sendOptionsPanel.add(radiusInterimIntervalLabel, gridBagConstraints12);
            //sendOptionsPanel.add(getRadiusInterimIntervalTextField(), gridBagConstraints13);
            sendOptionsPanel.add(getOptionsLabel(), gridBagConstraints14);
            sendOptionsPanel.add(getGenerateAcctSessionIdCheckBox(), gridBagConstraints15);
            sendOptionsPanel.add(getNotStopOnRejectCheckBox(), gridBagConstraints16);
            sendOptionsPanel.add(getSendClassAttributeCheckBox(), gridBagConstraints16);
        }
        return sendOptionsPanel;
    }

    /**
     * This method initializes optionsLabel
     * 
     * @return javax.swing.JLabel
     */
    private JLabel getOptionsLabel() {
        JLabel optionsLabel = new JLabel();
        optionsLabel.setText("Options:");
        return optionsLabel;
    }
    
    /**
     * This method initializes addAttributeButton
     * 
     * @return javax.swing.JButton
     */
    private JButton getAddAttributeButton() {
        if (addAttributeButton == null) {
            addAttributeButton = new JButton();
            addAttributeButton.setText("Add Attribute");
            addAttributeButton.addActionListener(new java.awt.event.ActionListener() { 
            	public void actionPerformed(java.awt.event.ActionEvent e) {    
            		JDialog dialog = getAddAttributeDialog();
                    dialog.setModal(true);
                    dialog.setVisible(true);
            	}
            });
        }
        return addAttributeButton;
    }

    /**
     * This method initializes addAttributeDialog
     * 
     * @return javax.swing.JDialog
     */
    private JDialog getAddAttributeDialog() {
        if (addAttributeDialog == null) {
            addAttributeDialog = new JDialog();
            addAttributeDialog.setSize(350, 300);
            addAttributeDialog.setTitle("Add Attributes");
            addAttributeDialog.setContentPane(getAddAttributeContentPane());
        }
        return addAttributeDialog;
    }

    /**
     * This method initializes addAttributeContentPane
     * 
     * @return javax.swing.JPanel
     */
    private JPanel getAddAttributeContentPane() {
        if (addAttributeContentPane == null) {
            addAttributeContentPane = new JPanel();
            addAttributeContentPane.setLayout(new BorderLayout());
            addAttributeContentPane.add(getAddAttributePanel(), java.awt.BorderLayout.SOUTH);
            addAttributeContentPane.add(getAttributeTreeScrollPane(), java.awt.BorderLayout.CENTER);
        }
        return addAttributeContentPane;
    }

    /**
     * This method initializes attributeTree
     * 
     * @return javax.swing.JTree
     */
    private JTree getAttributeTree() {
        if (attributeTree == null) {
            DefaultMutableTreeNode top =
                new DefaultMutableTreeNode("Attribute Dictionary");
            createAttributeTreeNodes(top);
            attributeTree = new JTree(top);
        }
        return attributeTree;
    }
    
    private void createAttributeTreeNodes(DefaultMutableTreeNode top)
    {
        DefaultMutableTreeNode standardTree = new DefaultMutableTreeNode("Standard Attributes");
        DefaultMutableTreeNode vsaTree = new DefaultMutableTreeNode("Vendor Specific Attributes");
        addAttributesToTable(standardTree, AttributeFactory.getAttributeMap());
        top.add(standardTree);
      
        Map vendors = AttributeFactory.getVendorMap();
        LinkedHashMap dictList = new LinkedHashMap();
        for (Iterator i = vendors.values().iterator(); i.hasNext();)
        {
            VendorValue vendor = (VendorValue)i.next();
            try
            {
                VSADictionary dict = (VSADictionary)vendor.getDictClass().newInstance();
                String vendorName = dict.getVendorName();
                dictList.put(vendorName, vendor.getAttributeMap());
            }
            catch(Exception e) { e.printStackTrace(); }
        }
        LinkedList list = new LinkedList(dictList.keySet());
        Collections.sort(list);
        for (Iterator i = list.iterator(); i.hasNext();)
        {
            String vendorName = (String)i.next();
            DefaultMutableTreeNode vsaNode = new DefaultMutableTreeNode(vendorName);
            addAttributesToTable(vsaNode, (Map)dictList.get(vendorName));
            vsaTree.add(vsaNode);
        }
        top.add(vsaTree);
    }
    
    private void addAttributesToTable(DefaultMutableTreeNode node, Map attributes)
    {
        LinkedHashMap attributeList = new LinkedHashMap();
        for (Iterator i = attributes.entrySet().iterator(); i.hasNext();)
        {
            Map.Entry entry = (Map.Entry)i.next();
            Long type = (Long)entry.getKey();
            Class clazz = (Class)entry.getValue();
            if (type.intValue() <= 255)
            {
                try
                {
                    RadiusAttribute attribute = (RadiusAttribute)clazz.newInstance();
                    String attributeName = attribute.getAttributeName();
                    if (attributeName.equals("Vendor-Specific")) continue;
                    if (attributeName.startsWith("X-Ascend-")) continue;
                    attributeList.put(type, attributeName);
                }
                catch(Exception e) { e.printStackTrace(); }
            }
        }
        LinkedList list = new LinkedList(attributeList.keySet());
        Collections.sort(list);
        for (Iterator i = list.iterator(); i.hasNext();)
        {
            node.add(new DefaultMutableTreeNode(attributeList.get(i.next())));
        }
    }

    /**
     * This method initializes addAttributePanel
     * 
     * @return javax.swing.JPanel
     */
    private JPanel getAddAttributePanel() {
        if (addAttributePanel == null) {
            addAttributePanel = new JPanel();
            addAttributePanel.add(getAddButton(), null);
            addAttributePanel.add(getDoneButton(), null);
        }
        return addAttributePanel;
    }

    /**
     * This method initializes authTypeComboBox
     * 
     * @return javax.swing.JComboBox
     */
    private JComboBox getAuthTypeComboBox() {
        if (authTypeComboBox == null) {
            authTypeComboBox = new JComboBox(authTypeNames);
        }
        return authTypeComboBox;
    }
    
    private RadiusAuthenticator getAuthenticator() throws Exception
    {
        String authName = authTypeNames[authTypeComboBox.getSelectedIndex()];
        if (authName.startsWith("EAP-T"))
        {
            if (isJava14)
            {
                throw new Exception(authName + " not available with this Java version");
            }
            
            String s[] = authName.split("/");
            StringBuffer sb = new StringBuffer(s[0]);
            
            String v = tlsKeyFileTextField.getText();
            if (v != null && !"".equals(v))
            {
                sb.append(":keyFile=").append(v);
            }

            v = (String)tlsKeyFileTypeComboBox.getSelectedItem();
            if (v != null && !"".equals(v))
            {
                sb.append(":keyFileType=").append(v);
            }

            v = tlsKeyPasswordTextField.getText();
            if (v != null && !"".equals(v))
            {
                sb.append(":keyPassword=").append(v);
            }

            v = tlsCAFileTextField.getText();
            if (v != null && !"".equals(v))
            {
                sb.append(":caFile=").append(v);
            }
            
            v = (String)tlsCAFileTypeComboBox.getSelectedItem();
            if (v != null && !"".equals(v))
            {
                sb.append(":caFileType=").append(v);
            }

            v = tlsCAPasswordTextField.getText();
            if (v != null && !"".equals(v))
            {
                sb.append(":caPassword=").append(v);
            }
            
            if (tlsTrustAll.isSelected())
            {
                sb.append(":trustAll=true");
            }
            
            if (s.length == 2)
            {
                sb.append(":innerProtocol=").append(s[1]);
            }
            
            authName = sb.toString();
            System.out.println("Using Authenticator String: " + authName);
        }
        return RadiusClient.getAuthProtocol(authName);
    }

    /**
     * This method initializes checkStandardComboBox
     * 
     * @return javax.swing.JComboBox
     */
    private JComboBox getCheckStandardComboBox() {
        if (checkStandardComboBox == null) {
            checkStandardComboBox = new JComboBox(new String[] { "None", "IRAP", "WISPr" });
        }
        return checkStandardComboBox;
    }

    /**
     * This method initializes tlsCAFileTypeComboBox
     * 
     * @return javax.swing.JComboBox
     */
    private JComboBox getTLSCAFileTypeComboBox() {
        if (tlsCAFileTypeComboBox == null) {
            tlsCAFileTypeComboBox = new JComboBox(keystoreTypes);
        }
        return tlsCAFileTypeComboBox;
    }

    /**
     * This method initializes tlsKeyFileTypeComboBox
     * 
     * @return javax.swing.JComboBox
     */
    private JComboBox getTLSKeyFileTypeComboBox() {
        if (tlsKeyFileTypeComboBox == null) {
            tlsKeyFileTypeComboBox = new JComboBox(keystoreTypes);
        }
        return tlsKeyFileTypeComboBox;
    }

    /**
     * This method initializes attributesPanel
     * 
     * @return javax.swing.JPanel
     */
    private JPanel getAttributesPanel() {
        if (attributesPanel == null) {
            attributesPanel = new JPanel();
            attributesPanel.setLayout(new BorderLayout());
            attributesPanel.add(getAttributesTableScrollPane(), java.awt.BorderLayout.CENTER);
            attributesPanel.add(getAttributesButtonPanel(), java.awt.BorderLayout.SOUTH);
        }
        return attributesPanel;
    }

    /**
     * This method initializes tlsPanel
     * 
     * @return javax.swing.JPanel
     */
    private JPanel getKeysPanel() {
        if (keysPanel == null) {
            keysPanel = new JPanel();
            keysPanel.setLayout(new BorderLayout());
            keysPanel.add(getKeysOptionsPanel(), java.awt.BorderLayout.NORTH);
        }
        return keysPanel;
    }

    /**
     * This method initializes tlsOptionsPanel
     * 
     * @return javax.swing.JPanel
     */
    private JPanel getKeysOptionsPanel() {
        if (keysOptionsPanel == null) {
            GridBagConstraints gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
            gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 5);

            GridBagConstraints gridBagConstraintsH = new GridBagConstraints();
            gridBagConstraintsH.insets = new java.awt.Insets(10, 10, 5, 10);
            gridBagConstraintsH.gridwidth = java.awt.GridBagConstraints.REMAINDER;

            GridBagConstraints gridBagConstraints0 = new GridBagConstraints();
            gridBagConstraints0.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints0.insets = new java.awt.Insets(0, 0, 0, 5);
            gridBagConstraints0.gridwidth = java.awt.GridBagConstraints.REMAINDER;
            gridBagConstraints0.weightx = 1.0;

            GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
            gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints1.insets = new java.awt.Insets(0, 0, 0, 5);
            gridBagConstraints1.weightx = 1.0;

            GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
            gridBagConstraints2.anchor = java.awt.GridBagConstraints.WEST;
            gridBagConstraints2.gridwidth = java.awt.GridBagConstraints.REMAINDER;
            gridBagConstraints2.insets = new java.awt.Insets(0, 0, 0, 10);

            GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
            gridBagConstraints3.anchor = java.awt.GridBagConstraints.WEST;
            gridBagConstraints3.gridwidth = java.awt.GridBagConstraints.REMAINDER;
            gridBagConstraints3.insets = new java.awt.Insets(0, 0, 0, 10);
            gridBagConstraints3.gridx = 1;
            
            GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
            gridBagConstraints4.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints4.insets = new java.awt.Insets(0, 0, 0, 5);

            GridBagLayout gridBagLayout = new GridBagLayout();

            keysOptionsPanel = new JPanel();
            keysOptionsPanel.setLayout(gridBagLayout);
            keysOptionsPanel.add(new JLabel("Client Certificate Java Keystore"), gridBagConstraintsH);
            keysOptionsPanel.add(new JLabel("File:"), gridBagConstraints);
            keysOptionsPanel.add(getTLSKeyFileTextField(), gridBagConstraints0);
            keysOptionsPanel.add(new JLabel("Password:"), gridBagConstraints);
            keysOptionsPanel.add(getTLSKeyPasswordTextField(), gridBagConstraints1);
            keysOptionsPanel.add(new JLabel("Type:"), gridBagConstraints4);
            keysOptionsPanel.add(getTLSKeyFileTypeComboBox(), gridBagConstraints2);
            keysOptionsPanel.add(new JLabel("Root CA Chain Java Keystore"), gridBagConstraintsH);
            keysOptionsPanel.add(new JLabel("File:"), gridBagConstraints);
            keysOptionsPanel.add(getTLSCAFileTextField(), gridBagConstraints0);
            keysOptionsPanel.add(new JLabel("Password:"), gridBagConstraints);
            keysOptionsPanel.add(getTLSCAPasswordTextField(), gridBagConstraints1);
            keysOptionsPanel.add(new JLabel("Type:"), gridBagConstraints4);
            keysOptionsPanel.add(getTLSCAFileTypeComboBox(), gridBagConstraints2);
            keysOptionsPanel.add(getOptionsLabel(), gridBagConstraints);
            //tlsOptionsPanel.add(getUseJavaRootCAChainCheckBox(), gridBagConstraints2);
            keysOptionsPanel.add(getTLSTrustAllCheckBox(), gridBagConstraints2);
            keysOptionsPanel.add(new JLabel("AKA Authentication (to be implemented)"), gridBagConstraintsH);
            keysOptionsPanel.add(new JLabel("IK"), gridBagConstraints);
            keysOptionsPanel.add(getAKAIKTextField(), gridBagConstraints0);
            keysOptionsPanel.add(new JLabel("CK"), gridBagConstraints);
            keysOptionsPanel.add(getAKACKTextField(), gridBagConstraints0);
        }
        return keysOptionsPanel;
    }

    /**
     * This method initializes akaIKTextField
     * 
     * @return javax.swing.JTextField
     */
    private JTextField getAKAIKTextField() {
        if (akaIKTextField == null) {
        	akaIKTextField = new JTextField(40);
        }
        return akaIKTextField;
    }

    /**
     * This method initializes akaIKTextField
     * 
     * @return javax.swing.JTextField
     */
    private JTextField getAKACKTextField() {
        if (akaCKTextField == null) {
        	akaCKTextField = new JTextField(40);
        }
        return akaCKTextField;
    }

    /**
     * This method initializes tlsKeyFileTextField
     * 
     * @return javax.swing.JTextField
     */
    private JTextField getTLSKeyFileTextField() {
        if (tlsKeyFileTextField == null) {
            tlsKeyFileTextField = new JTextField(100);
        }
        return tlsKeyFileTextField;
    }

    /**
     * This method initializes tlsKeyPasswordTextField
     * 
     * @return javax.swing.JTextField
     */
    private JTextField getTLSKeyPasswordTextField() {
        if (tlsKeyPasswordTextField == null) {
            tlsKeyPasswordTextField = new JTextField(100);
        }
        return tlsKeyPasswordTextField;
    }

    /**
     * This method initializes tlsCAFileTextField
     * 
     * @return javax.swing.JTextField
     */
    private JTextField getTLSCAFileTextField() {
        if (tlsCAFileTextField == null) {
            tlsCAFileTextField = new JTextField(100);
        }
        return tlsCAFileTextField;
    }

    /**
     * This method initializes tlsCAPasswordTextField
     * 
     * @return javax.swing.JTextField
     */
    private JTextField getTLSCAPasswordTextField() {
        if (tlsCAPasswordTextField == null) {
            tlsCAPasswordTextField = new JTextField(100);
        }
        return tlsCAPasswordTextField;
    }

    /**
     * This method initializes radiusServerTextField
     * 
     * @return javax.swing.JTextField
     */
    private JTextField getRadiusServerTextField() {
        if (radiusServerTextField == null) {
            radiusServerTextField = new JTextField(100);
        }
        return radiusServerTextField;
    }

    /**
     * This method initializes sharedSecretTextField	
     * 	
     * @return javax.swing.JTextField	
     */
    private JTextField getSharedSecretTextField() {
        if (sharedSecretTextField == null) {
            sharedSecretTextField = new JTextField(40);
        }
        return sharedSecretTextField;
    }

    /**
     * This method initializes doneButton	
     * 	
     * @return javax.swing.JButton	
     */    
    private JButton getDoneButton() {
    	if (doneButton == null) {
    		doneButton = new JButton();
    		doneButton.setText("Done");
    		doneButton.addActionListener(new java.awt.event.ActionListener() { 
    			public void actionPerformed(java.awt.event.ActionEvent e) {    
    				addAttributeDialog.dispose();
    			}
    		});
    	}
    	return doneButton;
    }

    /**
     * This method initializes attributeTreeScrollPane	
     * 	
     * @return javax.swing.JScrollPane	
     */    
    private JScrollPane getAttributeTreeScrollPane() {
    	if (attributeTreeScrollPane == null) {
    		attributeTreeScrollPane = new JScrollPane();
    		attributeTreeScrollPane.setViewportView(getAttributeTreeScrollPanel());
    	}
    	return attributeTreeScrollPane;
    }

    /**
     * This method initializes attributeTreeScrollPanel	
     * 	
     * @return javax.swing.JPanel	
     */    
    private JPanel getAttributeTreeScrollPanel() {
    	if (attributeTreeScrollPanel == null) {
    		attributeTreeScrollPanel = new JPanel();
    		attributeTreeScrollPanel.add(getAttributeTree(), null);
    	}
    	return attributeTreeScrollPanel;
    }

    /**
     * This method initializes addButton	
     * 	
     * @return javax.swing.JButton	
     */    
    private JButton getAddButton() {
    	if (addButton == null) {
    		addButton = new JButton();
    		addButton.setText("Add");
    		addButton.addActionListener(new java.awt.event.ActionListener() { 
    			public void actionPerformed(java.awt.event.ActionEvent e) {    
    				int rows[] = attributeTree.getSelectionRows();
                 if (rows != null)
                 {
                     for (int i = 0; i < rows.length; i++)
                     {
                         TreePath path = attributeTree.getPathForRow(rows[i]);
                         DefaultMutableTreeNode node = (DefaultMutableTreeNode)path.getPathComponent(path.getPathCount() - 1);
                         String attr = (String)node.getUserObject();
                         try
                         {
                             attributesTableModel.addAttribute(AttributeFactory.newAttribute(attr));
                         } 
                         catch (Exception ex) 
                         {  
                             ex.printStackTrace();
                         }
                     }
                     attributesTableModel.fireTableDataChanged();
                 }
    			}
    		});
    	}
    	return addButton;
    }

    
    /**
     * This method initializes simulationTypeComboBox	
     * 	
     * @return javax.swing.JComboBox	
     */    
    private JComboBox getSimulationTypeComboBox() {
    	if (simulationTypeComboBox == null) {
    		simulationTypeComboBox = new JComboBox(new String[] { 
    		        "Auth Only", 
                    "Auth & Acct (Start, Interim, Stop)", 
                    "Auth & Acct (Start, Stop)",
                    "Acct Only (Start, Interim, Stop)", 
                    "Disconnect Request",
    		        "CoA Request"});
// "Auth & Acct (Start until you Stop)" });
    	}
    	return simulationTypeComboBox;
    }

    /**
     * This method initializes attributesButtonPanel	
     * 	
     * @return javax.swing.JPanel	
     */    
    private JPanel getAttributesButtonPanel() {
    	if (attributesButtonPanel == null) {
    		attributesButtonPanel = new JPanel();
    		attributesButtonPanel.add(getAddAttributeButton(), null);
    		attributesButtonPanel.add(getMoveUpButton(), null);
    		attributesButtonPanel.add(getMoveDownButton(), null);
    		attributesButtonPanel.add(getRemoveAttributeButton(), null);
    	}
    	return attributesButtonPanel;
    }

    /**
     * This method initializes removeAttributeButton	
     * 	
     * @return javax.swing.JButton	
     */    
    private JButton getRemoveAttributeButton() {
    	if (removeAttributeButton == null) {
    		removeAttributeButton = new JButton();
    		removeAttributeButton.setText("Remove Attribute");
    		removeAttributeButton.addActionListener(new java.awt.event.ActionListener() { 
    			public void actionPerformed(java.awt.event.ActionEvent e) {    
    				int selectedIndex[] = attributesTable.getSelectedRows();
                 if (selectedIndex == null) return;
                 for (int i = 0; i < selectedIndex.length; i++)
                     attributesTableModel.getEntries().remove(selectedIndex[i]);
                 attributesTableModel.fireTableDataChanged();
    			}
    		});
    	}
    	return removeAttributeButton;
    }

    /**
     * This method initializes moveUpButton	
     * 	
     * @return javax.swing.JButton	
     */    
    private JButton getMoveUpButton() {
    	if (moveUpButton == null) {
    		moveUpButton = new JButton();
    		moveUpButton.setText("Move Up");
    		moveUpButton.addActionListener(new java.awt.event.ActionListener() { 
    			public void actionPerformed(java.awt.event.ActionEvent e) {    
                    int selectedIndex = attributesTable.getSelectedRow();
                    if (selectedIndex <= 0) return;
                    ArrayList list = attributesTableModel.getEntries();
                    ArrayList newList = new ArrayList();
                    Object[] oList = list.toArray();
                    Object selectedObject = null;
                    for (int i = 0; i < oList.length; i++)
                    {
                        if (i == selectedIndex) { newList.add(oList[i]); newList.add(selectedObject); }
                        else if (i == (selectedIndex - 1)) { selectedObject = oList[i]; }
                        else { newList.add(oList[i]); }
                    }
                    attributesTableModel.setEntries(newList);
                    attributesTableModel.fireTableDataChanged();
                    attributesTable.setRowSelectionInterval(--selectedIndex, selectedIndex);
    			}
    		});
    	}
    	return moveUpButton;
    }

    /**
     * This method initializes moveDownButton	
     * 	
     * @return javax.swing.JButton	
     */    
    private JButton getMoveDownButton() {
    	if (moveDownButton == null) {
    		moveDownButton = new JButton();
    		moveDownButton.setText("Move Down");
    		moveDownButton.addActionListener(new java.awt.event.ActionListener() { 
    			public void actionPerformed(java.awt.event.ActionEvent e) {    
                 int selectedIndex = attributesTable.getSelectedRow();
                 if (selectedIndex == -1) return;
                 ArrayList list = attributesTableModel.getEntries();
                 if (selectedIndex >= (list.size() - 1)) return;
                 ArrayList newList = new ArrayList();
                 Object[] oList = list.toArray();
                 Object selectedObject = null;
                 for (int i = 0; i < oList.length; i++)
                 {
                     if (i == selectedIndex) { selectedObject = oList[i]; }
                     else if (i == (selectedIndex + 1)) { newList.add(oList[i]); newList.add(selectedObject); }
                     else { newList.add(oList[i]); }
                 }
                 attributesTableModel.setEntries(newList);
                 attributesTableModel.fireTableDataChanged();
                 attributesTable.setRowSelectionInterval(++selectedIndex, selectedIndex);
    			}
    		});
    	}
    	return moveDownButton;
    }
    
    /**
     * This method initializes generateAcctSessionIdCheckBox    
     *  
     * @return javax.swing.JCheckBox    
     */    
    private JCheckBox getGenerateAcctSessionIdCheckBox() {
        if (generateAcctSessionIdCheckBox == null) {
            generateAcctSessionIdCheckBox = new JCheckBox();
            generateAcctSessionIdCheckBox.setText("Generate Unique Acct-Session-Id");
        }
        return generateAcctSessionIdCheckBox;
    }

    /**
     * This method initializes tlsUseJavaRootCA 
     *  
     * @return javax.swing.JCheckBox    
     */    
    private JCheckBox getUseJavaRootCAChainCheckBox() {
        if (tlsUseJavaRootCA == null) {
            tlsUseJavaRootCA = new JCheckBox();
            tlsUseJavaRootCA.setText("Use Java's Root CA Chain");
        }
        return tlsUseJavaRootCA;
    }

    /**
     * This method initializes tlsTrustAll 
     *  
     * @return javax.swing.JCheckBox    
     */    
    private JCheckBox getTLSTrustAllCheckBox() {
        if (tlsTrustAll == null) {
            tlsTrustAll = new JCheckBox();
            tlsTrustAll.setText("Trust All Server Certificates");
        }
        return tlsTrustAll;
    }

    /**
     * This method initializes notStopOnRejectCheckBox 
     *  
     * @return javax.swing.JCheckBox    
     */    
    private JCheckBox getNotStopOnRejectCheckBox() {
        if (notStopOnRejectCheckBox == null) {
            notStopOnRejectCheckBox = new JCheckBox();
            notStopOnRejectCheckBox.setText("Don't Stop Simulation On AccessReject");
        }
        return notStopOnRejectCheckBox;
    }

    /**
     * This method initializes notSendClassAttribute 
     *  
     * @return javax.swing.JCheckBox    
     */    
    private JCheckBox getSendClassAttributeCheckBox() {
        if (notSendClassAttribute == null) {
            notSendClassAttribute = new JCheckBox();
            notSendClassAttribute.setText("Don't Send The Received Class Attribute");
        }
        return notSendClassAttribute;
    }

    /**
     * @return Returns the numberFormatter.
     */
    public NumberFormatter getNumberFormatter()
    {
        if (numberFormatter == null)
        {
            numberFormatter = new NumberFormatter(NumberFormat.getIntegerInstance());
            numberFormatter.setValueClass(Integer.class);
        }
        return numberFormatter;
    }

    /**
     * This method initializes radiusAuthPortTextField  
     *  
     * @return javax.swing.JTextField   
     */    
    private JFormattedTextField getRadiusAuthPortTextField() {
        if (radiusAuthPortTextField == null) {
            radiusAuthPortTextField = new JFormattedTextField(getNumberFormatter());
            radiusAuthPortTextField.setValue(new Integer(1812));
        }
        return radiusAuthPortTextField;
    }

    /**
     * This method initializes radiusAcctPortTextField  
     *  
     * @return javax.swing.JTextField   
     */    
    private JFormattedTextField getRadiusAcctPortTextField() {
        if (radiusAcctPortTextField == null) {
            radiusAcctPortTextField = new JFormattedTextField(getNumberFormatter());
            radiusAcctPortTextField.setValue(new Integer(1813));
        }
        return radiusAcctPortTextField;
    }

    /**
     * This method initializes radiusRetriesTextField  
     *  
     * @return javax.swing.JTextField   
     */    
    private JFormattedTextField getRadiusRetriesTextField() {
        if (radiusRetriesTextField == null) {
            radiusRetriesTextField = new JFormattedTextField(getNumberFormatter());
            radiusRetriesTextField.setValue(new Integer(0));
        }
        return radiusRetriesTextField;
    }

    /**
     * This method initializes requestersTextField  
     *  
     * @return javax.swing.JTextField   
     */    
    private JFormattedTextField getRequestersTextField() {
        if (requestersTextField == null) {
            requestersTextField = new JFormattedTextField(getNumberFormatter());
            requestersTextField.setValue(new Integer(1));
        }
        return requestersTextField;
    }

    /**
     * This method initializes radiusRetriesTextField  
     *  
     * @return javax.swing.JTextField   
     */    
    private JFormattedTextField getRequestsTextField() {
        if (requestsTextField == null) {
            requestsTextField = new JFormattedTextField(getNumberFormatter());
            requestsTextField.setValue(new Integer(1));
        }
        return requestsTextField;
    }

    /**
     * This method initializes radiusRetriesTextField  
     *  
     * @return javax.swing.JTextField   
     */    
    private JFormattedTextField getRadiusTimeoutTextField() {
        if (radiusTimeoutTextField == null) {
            radiusTimeoutTextField = new JFormattedTextField(getNumberFormatter());
            radiusTimeoutTextField.setValue(new Integer(10));
        }
        return radiusTimeoutTextField;
    }

    /**
     * This method initializes radiusInterimIntervalTextField  
     *  
     * @return javax.swing.JTextField   
     */    
    private JFormattedTextField getRadiusInterimIntervalTextField() {
        if (radiusInterimIntervalTextField == null) {
            radiusInterimIntervalTextField = new JFormattedTextField(getNumberFormatter());
            radiusInterimIntervalTextField.setValue(new Integer(60));
        }
        return radiusInterimIntervalTextField;
    }

    /**
     * This method initializes sessionTimeTextField  
     *  
     * @return javax.swing.JTextField   
     */    
    private JFormattedTextField getSessionTimeTextField() {
        if (sessionTimeTextField == null) {
            sessionTimeTextField = new JFormattedTextField(getNumberFormatter());
            sessionTimeTextField.setValue(new Integer(60));
        }
        return sessionTimeTextField;
    }

    private void checkStandard(RadiusStandard radiusStandard, RadiusPacket p)
    {
        if (radiusStandard != null)
        {
            try
            {
                radiusStandard.checkPacket(p);
            }
            catch (StandardViolatedException e)
            {
                statusLabel.setText(radiusStandard.getName() + " standard violated");
                logErr.println(radiusStandard.getName() + " Standard Violated: " + p.getClass().getName());
                logErr.println(logSepLine);
                logErr.println("Missing attributes:");
                logErr.println(e.listAttributes("\n") + "\n");
                logErr.flush();
            }
        }
    }
    
    private RadiusStandard getRadiusStandard()
    {
        switch (checkStandardComboBox.getSelectedIndex())
        {
        case 1: return new IRAPStandard(); 
        case 2: return new WISPrStandard(); 
        }
        return null;
    }
    
    public void run()
    {
        String radiusServer = radiusServerTextField.getText();
        String sharedSecret = sharedSecretTextField.getText();

        Integer authPort = (Integer)radiusAuthPortTextField.getValue();
        Integer acctPort = (Integer)radiusAcctPortTextField.getValue();
        Integer timeout  = (Integer)radiusTimeoutTextField.getValue();
        Integer retries  = (Integer)radiusRetriesTextField.getValue();
        Integer requests = (Integer)requestsTextField.getValue();
        
        byte[] bClass = null;

        if (radiusServer == null || sharedSecret == null || "".equals(radiusServer) || "".equals(sharedSecret))
        {
            statusLabel.setText("The RADIUS Server and Shared Secret are required");
            return;
        }

        if (authPort == null || acctPort == null)
        {
            statusLabel.setText("The Auth Port and Acct Port must be set");
            return;
        }

        for (int r=0; r<requests; r++)
        {
            // Default is Auth Only
            boolean sendPackets[] = { true, false, false, false };
            boolean sendDisconnectRequest = false;
            boolean sendCoARequest = false;
            boolean simulationSuccess = true;
            interactiveSession = false; 
            
            switch (simulationTypeComboBox.getSelectedIndex())
            {
                case 1: sendPackets[1] = sendPackets[2] = sendPackets[3] = true; break;
                case 2: sendPackets[1] = sendPackets[3] = true; break;
                case 3: sendPackets = new boolean[]{ false, true, true, true }; break;
                case 4: sendDisconnectRequest = true; break;
                case 5: sendCoARequest = true; break;
                //case 3: sendPackets[1] = true; interactiveSession = true; break;
            }
    
            Attr_AcctSessionId generatedAcctSessionId = null;
            if (generateAcctSessionIdCheckBox.isSelected())
            {
                generatedAcctSessionId = new Attr_AcctSessionId("JRadius-" + RadiusRandom.getRandomString(16));
            }
            
            try
            {
                // Run the Simulation
                AttributeList[] authAttributes = { new AttributeList(), new AttributeList() };
                AttributeList[] acctAttributes = { new AttributeList(), new AttributeList(), new AttributeList() };
    
                Object[] entries = attributesTableModel.getEntries().toArray();
                for (int i = 0; i < entries.length; i++)
                {
                    AttributesTableEntry entry = (AttributesTableEntry)entries[i];
                    RadiusAttribute attribute = AttributeFactory.newAttribute(entry.getAttributeName(), entry.getAttributeValue(), "=");
                    Boolean bool;
                    
                    if ((bool = entry.getAccessRequest()) != null     && bool.booleanValue()) authAttributes[0].add(attribute,false);
                    if ((bool = entry.getTunnelRequest()) != null     && bool.booleanValue()) authAttributes[1].add(attribute,false);
                    if ((bool = entry.getAccountingStart()) != null   && bool.booleanValue()) acctAttributes[0].add(attribute,false);
                    if ((bool = entry.getAccountingUpdate()) != null  && bool.booleanValue()) acctAttributes[1].add(attribute,false);
                    if ((bool = entry.getAccountingStop()) != null    && bool.booleanValue()) acctAttributes[2].add(attribute,false);
                }
                
                RadiusClient radiusClient = new RadiusClient(InetAddress.getByName(radiusServer), sharedSecret, 
                        authPort.intValue(), acctPort.intValue(), timeout.intValue())
                {
                    /* (non-Javadoc)
                     * @see net.jradius.client.RadiusClient#receive()
                     */
                    protected RadiusResponse receive() throws IOException, RadiusException
                    {
                        statusLabel.setText("Waiting for response...");
                        RadiusResponse res = super.receive();
                        statusLabel.setText("Received RADIUS Packet " + res.getClass().getName());
    
                        logRecv.println("Received RADIUS Packet:");
                        logRecv.println(logSepLine);
                        logRecv.println(res.toString());
                        logRecv.flush();
    
                        checkStandard(getRadiusStandard(), res);
    
                        return res;
                    }
    
                    /* (non-Javadoc)
                     * @see net.jradius.client.RadiusClient#send(net.jradius.packet.RadiusPacket, java.net.InetAddress, int, int)
                     */
                    protected void send(RadiusPacket p, InetAddress a, int port, int attempt) throws IOException
                    {
                        logSent.println("Sending RADIUS Packet:");
                        logSent.println(logSepLine);
                        logSent.println(p.toString());
                        logSent.flush();
    
                        checkStandard(getRadiusStandard(), p);
                        
                        statusLabel.setText("Sending RADIUS Packet " + p.getClass().getName());
                        super.send(p, a, port, attempt);
                    }
                };
         
                for (int i = 0; i < sendPackets.length; i++)
                {
                    if (!sendPackets[i]) continue;
                    RadiusRequest request;
                    if (i == 0) 
                    {
                        if (sendDisconnectRequest)
                        {
                            request = new DisconnectRequest(radiusClient, authAttributes[0]);
                        }
                        else if (sendCoARequest)
                        {
                            request = new CoARequest(radiusClient, authAttributes[0]);
                        }
                        else
                        {
                            request = new AccessRequest(radiusClient, authAttributes[0]);
                        }
                    }
                    else 
                    {
                        request = new AccountingRequest(radiusClient, acctAttributes[i - 1]);
                        if (request.findAttribute(Attr_AcctStatusType.TYPE) == null)
                        {
                            switch(i)
                            {
                            case 1: request.addAttribute(new Attr_AcctStatusType(Attr_AcctStatusType.Start)); break;
                            case 2: request.addAttribute(new Attr_AcctStatusType(Attr_AcctStatusType.InterimUpdate)); break;
                            case 3: request.addAttribute(new Attr_AcctStatusType(Attr_AcctStatusType.Stop)); break;
                            }
                        }
                    }
                    
                    if (bClass != null) request.addAttribute(new Attr_Class(bClass));
    
                    if (generatedAcctSessionId != null && request.findAttribute(Attr_AcctSessionId.TYPE) != null)
                    {
                        request.overwriteAttribute(generatedAcctSessionId);
                    }
    
                    RadiusPacket reply;
                    if (i == 0) 
                    {
                        if (request instanceof AccessRequest)
                        {
                            RadiusAuthenticator auth = getAuthenticator();
                            if (auth instanceof TunnelAuthenticator)
                            {
                                ((TunnelAuthenticator)auth).setTunneledAttributes(authAttributes[1]);
                            }
                            if (auth instanceof EAPAKAAuthenticator)
                            {
                            	byte[] ik=toBinArray(akaIKTextField.getText());
                            	byte[] ck=toBinArray(akaCKTextField.getText());
                            	request.addAttribute(new Attr_EAPAkaIK(ik));
                            	request.addAttribute(new Attr_EAPAkaCK(ck));
                            }
                            reply = radiusClient.authenticate((AccessRequest)request, auth, retries.intValue());
                            if (!notStopOnRejectCheckBox.isSelected())
                            {
                                if (reply instanceof AccessReject)
                                {
                                    String replyMessage = (String)reply.getAttributeValue(Attr_ReplyMessage.TYPE);
                                    if (replyMessage == null) replyMessage = "reason unknown";
                                    statusLabel.setText("Access Rejected: " + replyMessage);
                                    simulationSuccess = false;
                                    break;
                                }
                            }
                            if (!notSendClassAttribute.isSelected())
                            {
                                bClass = (byte[]) reply.getAttributeValue(Attr_Class.TYPE);
                            }
                        }
                        else if (sendDisconnectRequest)
                        {
                            reply = radiusClient.disconnect((DisconnectRequest)request, retries.intValue());
                        }
                        else if (sendCoARequest)
                        {
                            reply = radiusClient.changeOfAuth((CoARequest)request, retries.intValue());
                        }
                    }
                    else
                    {
                        reply = radiusClient.accounting((AccountingRequest)request, retries.intValue());
                    }
                }
                
                if (simulationSuccess) statusLabel.setText("Simulation complete");
            }
            catch (Exception e)
            {
                statusLabel.setText("Problem: " + e.getMessage());
                e.printStackTrace();
            }
        }
        runButton.setSelected(false);
        runButton.setText("Start");
    }

    /**
     * This method initializes openUrlDialog	
     * 	
     * @return javax.swing.JDialog	
     */    
    private JDialog getOpenUrlDialog() {
    	if (openUrlDialog == null) {
    		openUrlDialog = new JDialog();
    		openUrlDialog.setContentPane(getOpenUrlContentPane());
            openUrlDialog.setSize(425,125);
    	}
    	openUrlStatusLabel.setText("Enter the URL of the configuration file:");
    	openUrlTextField.setText("http://");
    	return openUrlDialog;
    }

    /**
     * This method initializes openUrlContentPane	
     * 	
     * @return javax.swing.JPanel	
     */    
    private JPanel getOpenUrlContentPane() {
    	if (openUrlContentPane == null) {
    	    GridBagConstraints gridBagConstraints = new GridBagConstraints();
    	    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    	    gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
    	    gridBagConstraints.insets = new java.awt.Insets(25, 25, 10, 25);

    	    GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
    	    gridBagConstraints1.insets = new java.awt.Insets(0, 25, 0, 5);
    	    gridBagConstraints1.anchor = java.awt.GridBagConstraints.EAST;

    	    GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
    	    gridBagConstraints2.gridwidth = java.awt.GridBagConstraints.REMAINDER;
    	    gridBagConstraints2.fill = java.awt.GridBagConstraints.HORIZONTAL;
    	    gridBagConstraints2.insets = new java.awt.Insets(0, 0, 0, 25);
    	    gridBagConstraints2.weightx = 1.0;

    	    GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
    	    gridBagConstraints3.fill = java.awt.GridBagConstraints.HORIZONTAL;
    	    gridBagConstraints3.gridwidth = java.awt.GridBagConstraints.REMAINDER;
    	    gridBagConstraints3.insets = new java.awt.Insets(0, 25, 25, 25);

            JLabel openUrlLabel = new JLabel();
    	    openUrlLabel.setText("URL:");

    	    openUrlStatusLabel = new JLabel();

    	    GridBagLayout gridBagLayout = new GridBagLayout();

    	    openUrlContentPane = new JPanel();
    	    openUrlContentPane.setLayout(gridBagLayout);
            
    	    JPanel buttonPanel = new JPanel();
    	    buttonPanel.add(getOpenUrlButton(), null);
    	    buttonPanel.add(getCancelUrlButton(), null);

    	    openUrlContentPane.add(openUrlStatusLabel, gridBagConstraints);
    	    openUrlContentPane.add(openUrlLabel, gridBagConstraints1);
    	    openUrlContentPane.add(getOpenUrlTextField(), gridBagConstraints2);
    	    openUrlContentPane.add(buttonPanel, gridBagConstraints3);
    	}
    	return openUrlContentPane;
    }

    /**
     * This method initializes openUrlTextField	
     * 	
     * @return javax.swing.JTextField	
     */    
    private JTextField getOpenUrlTextField() {
    	if (openUrlTextField == null) {
    		openUrlTextField = new JTextField();
    	}
    	return openUrlTextField;
    }

    /**
     * This method initializes openUrlButton	
     * 	
     * @return javax.swing.JButton	
     */    
    private JButton getOpenUrlButton() {
    	if (openUrlButton == null) {
    		openUrlButton = new JButton();
    		openUrlButton.setText("Open");
    		openUrlButton.addActionListener(new java.awt.event.ActionListener() { 
    			public void actionPerformed(java.awt.event.ActionEvent e) {    
                    try
                    {
                        loadConfigFile(openUrlTextField.getText());
                    }
                    catch(FileNotFoundException ex) 
                    {
                        openUrlStatusLabel.setText("Not found: " + ex.getMessage());
                        return;
                    }
                    catch(ConnectException ex) 
                    {
                        openUrlStatusLabel.setText("Open Failed: " + ex.getMessage());
                        return;
                    }
                    catch(Exception ex) 
                    {
                        openUrlStatusLabel.setText(ex.getMessage());
                        ex.printStackTrace();
                        return;
                    }
                    openUrlDialog.dispose();
    			}
    		});
    	}
    	return openUrlButton;
    }

    /**
     * This method initializes cancelUrlButton	
     * 	
     * @return javax.swing.JButton	
     */    
    private JButton getCancelUrlButton() {
    	if (cancelUrlButton == null) {
    		cancelUrlButton = new JButton();
    		cancelUrlButton.setText("Cancel");
    		cancelUrlButton.addActionListener(new java.awt.event.ActionListener() { 
    			public void actionPerformed(java.awt.event.ActionEvent e) {    
                    openUrlDialog.dispose();
    			}
    		});
    	}
    	return cancelUrlButton;
    }

    /**
     * Launches this application
     */
    public static void main(String[] args) {
        System.setProperty("org.apache.commons.logging.LogFactory", "net.jradius.client.gui.LogFactory");
        if (args.length > 0)
        {
            String url = args[0];
            File file = new File(url);
            if (file.exists()) url = "file:///" + file.getAbsolutePath();
            JRadiusSimulator.setConfigFileUrl(url);
        }
        JRadiusSimulator application = new JRadiusSimulator();
        application.setVisible(true);
    }

	public boolean isWindows() 
    {
		return windows;
	}

	public byte[] toBinArray(String hexStr)
	{
		hexStr = hexStr.replace("0x", "");
	    byte bArray[] = new byte[hexStr.length()/2];
	    for (int i=0; i<(hexStr.length()/2); i++)
	    {
	    	byte firstNibble  = Byte.parseByte(hexStr.substring(2*i,2*i+1),16);
	    	byte secondNibble = Byte.parseByte(hexStr.substring(2*i+1,2*i+2),16);
	    	int finalByte = (secondNibble) | (firstNibble << 4 );
	    	bArray[i] = (byte) finalByte;
	    }
	    return bArray;
	}

} //  @jve:decl-index=0:visual-constraint="10,10"
