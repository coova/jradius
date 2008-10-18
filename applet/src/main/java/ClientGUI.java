/**
 * JRadius - A RADIUS Server Java Adapter
 * Copyright (C) 2004-2006 PicoPoint, B.V.
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or (at
 * your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

public class ClientGUI extends JFrame
{
    final JRadiusWiFiClient client;

    JButton loginButton = new JButton("Login");
    JButton logoutButton = new JButton("Logout");
    JLabel statusLabel = new JLabel("Not yet on-line");
    JLabel locationLabel = new JLabel("unknown");
    
    public ClientGUI(final JRadiusWiFiClient client)
    {
        super("JRadius WiFi WISPr Client");
        this.client = client;
        
        setSize(350, 275);
        getContentPane().setLayout(new BorderLayout());
        
        GridBagLayout gridBagLayout = new GridBagLayout();

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(gridBagLayout);
        
        GridBagConstraints gbc;
        
        gbc = new GridBagConstraints();
        gbc.insets = new Insets(25, 25, 10, 25);
        contentPanel.add(new JLabel("Status:"), gbc);

        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.insets = new Insets(25, 25, 25, 10);
        contentPanel.add(statusLabel, gbc);

        gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 25, 10, 25);
        contentPanel.add(new JLabel("Location:"), gbc);

        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.insets = new Insets(10, 25, 25, 10);
        contentPanel.add(locationLabel, gbc);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(loginButton);
        buttonPanel.add(logoutButton);
        
        getContentPane().add(contentPanel, BorderLayout.CENTER);
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);
        
        loginButton.addActionListener(new ActionListener() {  
            public void actionPerformed(ActionEvent e) 
            {
                login();
            }
        });
        
        logoutButton.addActionListener(new ActionListener() {  
            public void actionPerformed(ActionEvent e) 
            {
                logoff();
            }
        });
    }
    
    public void login()
    {
        String username = client.getUsername();
        String password = client.getPassword();
        
        JTextField usernameField = new JTextField(username);

        JPasswordField passwordField = new JPasswordField(password);
        passwordField.setEchoChar('*');

        Object[] msg = { "User Name", usernameField, "Password", passwordField };

        // Showing the Dialog Box

        int result = JOptionPane.showConfirmDialog(this, msg, "WISPr Login", 
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION)
        {
            username = usernameField.getText();
            password = new String(passwordField.getPassword());
        }

        client.login(username, password);
    }
    
    public void logoff()
    {
        client.logoff();
    }
    
    public void setLocation(String location)
    {
        locationLabel.setText(location);
    }

    public void setStatus(String status)
    {
        statusLabel.setText(status);
    }
}
