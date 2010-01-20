/**
 * JRadiusSimulator
 * Copyright (C) 2004-2005 PicoPoint, B.V.
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
import java.awt.Color;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JComponent;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

/**
 * Console Log JComponent.
 * @author David Bird
 */
public final class LogConsole extends JComponent implements AdjustmentListener {
    private static final long serialVersionUID = (long)0;
    private static LogConsole singleton = null;
    
    public static LogConsole getInstance() { return singleton; }

    public static final String CATEGORY_DEFAULT = "sent";
    public static final String CATEGORY_PACKETS_SENT = "sent";
    public static final String CATEGORY_PACKETS_RECV = "recv";
    public static final String CATEGORY_ERROR = "err";

    private static final PrintStream out = new PrintStream(
            new FileOutputStream(FileDescriptor.out));

    private static final PrintStream err = new PrintStream(
            new FileOutputStream(FileDescriptor.err));

    private JTextPane console = new JTextPane();

    private JScrollPane container = new JScrollPane(console);

    private String TSPattern = null;

    private SimpleDateFormat sdf = null;

    private Color clrSent = Color.BLUE;
    private Color clrRecv = Color.MAGENTA;
    private Color clrError = Color.RED;

    private String defaultText = null;

    private boolean autoScroll = true;

    public LogConsole() {
        this("");
    }

    public LogConsole(String defaultText) {
        this.defaultText = defaultText;
        setLayout(new BorderLayout());
        add(container, BorderLayout.CENTER);
        console.setEditable(false);
        console.getCaret().setBlinkRate(0);
        container.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        container.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        container.getVerticalScrollBar().addAdjustmentListener(this);
        singleton = this;
    }

    public Document getDocument() {
        return console.getDocument();
    }

    public String getText() {
        return console.getText();
    }

    public synchronized void append(String category, String s) {
        SimpleAttributeSet sas = new SimpleAttributeSet();

        if (s == null)
            return;
        if (s.trim().length() == 0)
            return;

        Document doc = getDocument();

        StyleConstants.setFontFamily(sas, getFont().getFamily());
        StyleConstants.setFontSize(sas, getFont().getSize());
        StyleConstants.setBold(sas, getFont().isBold());
        StyleConstants.setItalic(sas, getFont().isItalic());
        StyleConstants.setBackground(sas, getBackground());

        if (TSPattern != null && sdf != null
                && !s.equalsIgnoreCase(defaultText)) {
            String ts = sdf.format(new Date());
            StyleConstants.setForeground(sas, getForeground());
            ts = ts.concat(" ");
            try {
                doc.insertString(doc.getLength(), ts, sas);
            } catch (Exception e) {
            }
        }

        if (CATEGORY_PACKETS_SENT.equalsIgnoreCase(category)) {
            StyleConstants.setForeground(sas, (clrSent == null ? getForeground() : clrSent));
        } else if (CATEGORY_PACKETS_RECV.equalsIgnoreCase(category)) {
            StyleConstants.setForeground(sas, (clrRecv == null ? getForeground() : clrRecv));
        } else if (CATEGORY_ERROR.equalsIgnoreCase(category)) {
            StyleConstants.setForeground(sas, (clrError == null ? getForeground() : clrError));
        }

        try {
            doc.insertString(doc.getLength(), s, sas);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (autoScroll) 
        {
            try 
            {
                int length = doc.getLength();
                console.setCaretPosition(length);
                scrollRectToVisible(console.modelToView(length - 1));
            } 
            catch (Exception e) 
            {
                e.printStackTrace();
            }

            JScrollBar vs = container.getVerticalScrollBar();
            vs.setValue(vs.getMaximum());
        }

        console.invalidate();
        repaint();
    }

    public void append(String s) {
        append(CATEGORY_PACKETS_SENT, s);
    }

    public void setText(String text) {
        if (text == null)
            return;
        try {
            getDocument().remove(0, getDocument().getLength());
            if (text.trim().length() == 0) {
                append((defaultText == null ? "" : defaultText));
            } else {
                append(text);
            }
        } catch (Exception e) {
        }
    }

    public OutputStream createFilteredStream(String category) {
        return new ConsoleOutputStream(this, category);
    }

    /*
     * (non-Javadoc)
     * @see java.awt.event.AdjustmentListener#adjustmentValueChanged(java.awt.event.AdjustmentEvent)
     */
    public void adjustmentValueChanged(AdjustmentEvent e) {
        autoScroll = !e.getValueIsAdjusting();
    }

    private static class ConsoleOutputStream extends OutputStream {
        private StringBuffer buf = new StringBuffer("");
        private String category = null;
        private LogConsole reference;

        public ConsoleOutputStream(LogConsole owner, String category) {
            reference = owner;
            if (category != null) this.category = category;
            else this.category = CATEGORY_DEFAULT;
        }

        public synchronized void write(int b) {
            buf.append(Character.toString((char) b));
        }

        public synchronized void write(byte[] b, int offset, int length) {
            buf.append(new String(b, offset, length));
        }

        public synchronized void write(byte[] b) {
            buf.append(new String(b));
        }

        public synchronized void flush() {
            synchronized (buf) {
                if (buf.length() > 0) {
                    char last = buf.charAt(buf.length() - 1);
                    if (last == '\n' || last == '\r') {
                        String text = buf.toString();
                        SwingUtilities.invokeLater(new Appender(reference, category, text));
                        buf.setLength(0);
                    }
                }
            }
        }
    }

    private static class Appender implements Runnable {
        LogConsole textView = null;
        String category = null;
        String line = null;

        public Appender(LogConsole console, String category, String line) {
            this.textView = console;
            this.category = category;
            this.line = line;
        }

        public void run() {
            try {
                textView.append(category, line);
            } catch (Throwable t) {
            }
        }
    }
}