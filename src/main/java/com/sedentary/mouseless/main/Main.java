package com.sedentary.mouseless.main;

import com.sedentary.mouseless.mouse.MouseClickType;
import com.sedentary.mouseless.server.Coordinates;
import com.sedentary.mouseless.server.Server;
import com.corundumstudio.socketio.SocketIOClient;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import java.awt.AWTException;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowStateListener;
import java.awt.image.BufferedImage;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;

/**
 *
 * @author Rodrigo Gomes da Silva
 */
public class Main extends javax.swing.JFrame {
    
    private static final ResourceBundle i18nMessages = ResourceBundle.getBundle("i18n/messages");
    
    private static JFrame frame;
    private MenuItem startStopItem;
    
    private Server server = null;
    
    /**
     * Creates new form Main
     */
    public Main() {
        initComponents();
        
        loadPreferences();
        
        createSystemTrayIcon();
        
        this.addWindowStateListener(windowStateListener);
        
        cmbInterface.addItemListener(itemListener);
        
        btnShowQrCode.setVisible(false);
    }
    
    /**
     * 
     */
    private void loadPreferences() {
        // Load network interface configuration
        String storedIp = Preferences.get(Preferences.NETWORK_INTERFACE);
        if (storedIp != null) {
            try {
                NetworkInterface ni = findNetworkInterfaceByIp(storedIp);
                if (ni != null) {
                    ComboBoxModel model = cmbInterface.getModel();
                    
                    int size = model.getSize();
                    for (Integer i = 0; i < size; i++) {
                        ComboNetworkItem element = (ComboNetworkItem) model.getElementAt(i);
                        if (element.getIp() != null && element.getIp().equals(storedIp)) {
                            cmbInterface.setSelectedItem(element);
                        }
                    }
                }
            } catch (SocketException ex) {
                logAppInfo(i18nMessages.getString("main.configuration.load.failed"));
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        // Load server port configuration
        Integer port = (Integer) Preferences.get(Preferences.SERVER_PORT, Integer.class, Server.DEFAULT_SERVER_PORT);
        spnPort.setValue(port);
    }
    
    /**
     * 
     */
    private void createSystemTrayIcon() {
        //Check the SystemTray is supported
        if (!SystemTray.isSupported()) {
            System.out.println(i18nMessages.getString("main.systemtray.not.supported"));
            return;
        }
        final PopupMenu popup = new PopupMenu();
        final TrayIcon trayIcon;
        trayIcon = new TrayIcon(new ImageIcon(getClass().getResource("/images/icon.png")).getImage(), "Mouseless-Server");
        trayIcon.setImageAutoSize(true);
        trayIcon.addMouseListener(systemTrayOnClickListener);
        final SystemTray tray = SystemTray.getSystemTray();
        
        // Create a pop-up menu components
        startStopItem = new MenuItem((server != null && server.isRunning()) == true ? i18nMessages.getString("main.stop") : i18nMessages.getString("main.start"));
        MenuItem exitItem = new MenuItem(i18nMessages.getString("main.exit"));
        
        startStopItem.addActionListener(startStopItemOnClickListener);
        exitItem.addActionListener(exitItemOnClickListener);
       
        //Add components to pop-up menu
        popup.add(startStopItem);
        popup.add(exitItem);
       
        trayIcon.setPopupMenu(popup);
        
        try {
            tray.add(trayIcon);
        } catch (AWTException e) {
            System.out.println(i18nMessages.getString("main.trayicon.not.added"));
        }
    }
    
    /**
     * 
     */
    MouseAdapter systemTrayOnClickListener = new MouseAdapter() {

        @Override
        public void mouseClicked(MouseEvent e) {
            frame.setVisible(true);
        }
    };
    
    /**
     * 
     */
    ActionListener exitItemOnClickListener = new ActionListener() {

        @Override
        public void actionPerformed(ActionEvent e) {
            System.exit(0);
        }
    };
    
    /**
     * 
     */
    ActionListener startStopItemOnClickListener = new ActionListener() {

        @Override
        public void actionPerformed(ActionEvent e) {
            startStopServer();
        }
    };
    
    /**
     * 
     */
    ItemListener itemListener = new ItemListener() {
        
        @Override
        public void itemStateChanged(ItemEvent ie) {
            if (ie.getStateChange() == ItemEvent.SELECTED) {
                NetworkInterface ntInterface = ((ComboNetworkItem) ie.getItem()).getNetworkInterface();
                String ip = findNetworkInterfaceIpAddress(ntInterface);
                if (ip != null) {
                    if (server == null || (server != null && !server.isRunning())) {
                        btnStartStop.setEnabled(true);
                    }
                } else {
                    if (server == null || (server != null && !server.isRunning())) {
                        btnStartStop.setEnabled(false);
                    }
                    ip = i18nMessages.getString("main.interface.invalid.ip.configuration");
                }
            
                Preferences.set(Preferences.NETWORK_INTERFACE, ip);
            }
        }
    };

    /**
     * 
     * @param ni
     * @return 
     */
    private String findNetworkInterfaceIpAddress(NetworkInterface ni) {
        Enumeration e = ni.getInetAddresses();
        
        if (!e.hasMoreElements()) {
            return null;
        }
        
        for (Enumeration enumIpAddr = ni.getInetAddresses(); enumIpAddr.hasMoreElements();) {
            InetAddress inetAddress = (InetAddress) enumIpAddr.nextElement();
            if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                String ipAddress = inetAddress.getHostAddress();
                return ipAddress;
            }
        }
        
        return null;
    }
    
    /**
     * 
     * @param ip
     * @return 
     * @throws java.net.SocketException 
     */
    public NetworkInterface findNetworkInterfaceByIp(String ip) throws SocketException {
        Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces();
        while(e.hasMoreElements()) {
            NetworkInterface ni = e.nextElement();
            for (Enumeration enumIpAddr = ni.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                InetAddress inetAddress = (InetAddress) enumIpAddr.nextElement();
                if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                    if (inetAddress.getHostAddress().equals(ip)) {
                        return ni;
                    }
               }
            }
        }
        
        return null;
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblInterface = new javax.swing.JLabel();
        cmbInterface = new javax.swing.JComboBox();
        btnStartStop = new javax.swing.JButton();
        scrlLog = new javax.swing.JScrollPane();
        txtLog = new javax.swing.JTextArea();
        lblPort = new javax.swing.JLabel();
        spnPort = new javax.swing.JSpinner();
        btnShowQrCode = new javax.swing.JButton();
        btnRefreshNtInterfaces = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle(java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("i18n/messages").getString("app.nameWithVersion"), new Object[] {this.getClass().getPackage().getImplementationVersion()})); // NOI18N
        setIconImage(new ImageIcon(getClass().getResource("/images/icon.png")).getImage());
        setMaximumSize(null);
        setMinimumSize(new java.awt.Dimension(450, 300));
        setName("frmMain"); // NOI18N
        setPreferredSize(new java.awt.Dimension(450, 300));

        lblInterface.setText(i18nMessages.getString("main.lblInterface.text")); // NOI18N

        cmbInterface.setModel(new NetworkInterfaceComboBoxModel());
        cmbInterface.setToolTipText(i18nMessages.getString("main.cmbInterface.tooltip")); // NOI18N

        btnStartStop.setText(i18nMessages.getString("main.start")); // NOI18N
        btnStartStop.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnStartStopActionPerformed(evt);
            }
        });

        txtLog.setEditable(false);
        txtLog.setColumns(20);
        txtLog.setRows(5);
        scrlLog.setViewportView(txtLog);

        lblPort.setText(i18nMessages.getString("main.lblPort.text")); // NOI18N

        spnPort.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(0), null, Integer.valueOf(47808), Integer.valueOf(1)));
        spnPort.setEditor(new javax.swing.JSpinner.NumberEditor(spnPort, "#####"));
        spnPort.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spnPortStateChanged(evt);
            }
        });

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("i18n/messages"); // NOI18N
        btnShowQrCode.setText(bundle.getString("main.btnShowQrCode.text")); // NOI18N
        btnShowQrCode.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnShowQrCodeActionPerformed(evt);
            }
        });

        btnRefreshNtInterfaces.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/refresh-icon.png"))); // NOI18N
        btnRefreshNtInterfaces.setToolTipText(bundle.getString("main.btnRefreshNtInterfaces.tooltip")); // NOI18N
        btnRefreshNtInterfaces.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRefreshNtInterfacesActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnStartStop, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(scrlLog, javax.swing.GroupLayout.DEFAULT_SIZE, 426, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(spnPort, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnShowQrCode))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblInterface)
                            .addComponent(lblPort))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(cmbInterface, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnRefreshNtInterfaces)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblInterface)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cmbInterface, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnRefreshNtInterfaces))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblPort)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(spnPort, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnShowQrCode))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnStartStop)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(scrlLog, javax.swing.GroupLayout.DEFAULT_SIZE, 122, Short.MAX_VALUE)
                .addContainerGap())
        );

        getAccessibleContext().setAccessibleDescription("");

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * 
     * @param evt 
     */
    private void btnStartStopActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnStartStopActionPerformed
        startStopServer();
    }//GEN-LAST:event_btnStartStopActionPerformed

    private void btnShowQrCodeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnShowQrCodeActionPerformed
        showQrCode(server.getServerInfo());
    }//GEN-LAST:event_btnShowQrCodeActionPerformed

    private void btnRefreshNtInterfacesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRefreshNtInterfacesActionPerformed
        cmbInterface.setModel(new NetworkInterfaceComboBoxModel());
        loadPreferences();
    }//GEN-LAST:event_btnRefreshNtInterfacesActionPerformed

    private void spnPortStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spnPortStateChanged
        Preferences.set(Preferences.SERVER_PORT, spnPort.getValue());
    }//GEN-LAST:event_spnPortStateChanged

    /**
     * 
     */
    public void startStopServer() {
        
        if (server == null || (server != null && !server.isRunning())) {
            
            NetworkInterface ntInterface = ((ComboNetworkItem) cmbInterface.getSelectedItem()).getNetworkInterface();
            String ip = findNetworkInterfaceIpAddress(ntInterface);
            
            if (ip != null) {
                server = new Server(
                        ip,
                        Integer.parseInt(spnPort.getValue().toString()),
                        serverCallback);
                server.start();
            } else {
                logAppInfo(i18nMessages.getString("main.select.valid.configuration.interface"));
            }
            
        } else {
            
            server.stop();
            
        }
    }
    
    /**
     * 
     */
    private void showQrCode(Server.ServerInfo serverInfo) {
        try {
            BufferedImage qrCode = createQrCode(serverInfo);
            JDialog floatOnParent = new JDialog(frame, false);
            JLabel qr = new JLabel(new ImageIcon(qrCode));
            floatOnParent.getContentPane().add(qr);

            floatOnParent.setBounds(frame.getX(), frame.getY(), 300, 300);
            floatOnParent.setVisible(true);
                
            logAppInfo(i18nMessages.getString("main.qrCode.message"));
        } catch (WriterException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            logAppInfo(i18nMessages.getString("main.qrCode.error.message"));
        }
    }
    
    /**
     * 
     */
    private final Server.Callback serverCallback = new Server.Callback() {

        @Override
        public void connected(SocketIOClient client) {
            logAppInfo(MessageFormat.format(ResourceBundle.getBundle("i18n/messages").getString("main.connected.to"), new Object[] {client.getHandshakeData().getAddress()}));
        }

        @Override
        public void disconnected() {
            logAppInfo(i18nMessages.getString("main.disconnected"));
        }

        @Override
        public void serverStarted(Server.ServerInfo serverInfo) {
            btnStartStop.setText(i18nMessages.getString("main.stop"));
            startStopItem.setLabel(i18nMessages.getString("main.stop"));
            
            btnShowQrCode.setVisible(true);
            
            logAppInfo(i18nMessages.getString("main.server.started"));
            logAppInfo(serverInfo.toString());
        }

        @Override
        public void serverStoped() {
            btnStartStop.setText(i18nMessages.getString("main.start"));
            startStopItem.setLabel(i18nMessages.getString("main.start"));
            
            btnShowQrCode.setVisible(false);
            
            logAppInfo(i18nMessages.getString("main.server.stoped"));
        }

        @Override
        public void receivedCoordinates(Coordinates coords) {
//            logAppInfo("Corrdenadas: " + coords);
        }

        @Override
        public void receivedMouseClick(MouseClickType type) {
//            logAppInfo("Click: " + type);
        }

        @Override
        public void error(String error) {
            logAppInfo(MessageFormat.format(ResourceBundle.getBundle("i18n/messages").getString("main.error"), new Object[] {error}));
        }
    };
    
    /**
     * 
     * @param info 
     */
    private BufferedImage createQrCode(Server.ServerInfo info) throws WriterException {
        String codeText = info.getHostname() + ":" + info.getPort();
        Integer size = 300;

        HashMap hintMap = new HashMap();
        hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix byteMatrix = qrCodeWriter.encode(codeText, BarcodeFormat.QR_CODE, size, size, hintMap);
        // Make the BufferedImage that are to hold the QRCode
        Integer matrixWidth = byteMatrix.getWidth();
        BufferedImage image = new BufferedImage(matrixWidth, matrixWidth, BufferedImage.TYPE_INT_RGB);
        image.createGraphics();

        Graphics2D graphics = (Graphics2D) image.getGraphics();
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, matrixWidth, matrixWidth);
        // Paint and save the image using the ByteMatrix
        graphics.setColor(Color.BLACK);

        for (int i = 0; i < matrixWidth; i++) {
            for (int j = 0; j < matrixWidth; j++) {
                if (byteMatrix.get(i, j)) {
                    graphics.fillRect(i, j, 1, 1);
                }
            }
        }
        
        return image;
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Main.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                frame = new Main();
                frame.setVisible(true);
            }
        });
    }
    
    /**
     * 
     */
    private final WindowStateListener windowStateListener = new WindowStateListener() {
        
        @Override
        public void windowStateChanged(WindowEvent e) {
            if (e.getNewState() == ICONIFIED) {
                frame.setVisible(false);
            }
        }
    };
    
    /**
     * 
     * @param log
     */
    private void logAppInfo(String log) {
        txtLog.setText(txtLog.getText() + log + System.getProperty("line.separator"));
        txtLog.setCaretPosition(txtLog.getDocument().getLength());
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnRefreshNtInterfaces;
    private javax.swing.JButton btnShowQrCode;
    private javax.swing.JButton btnStartStop;
    private javax.swing.JComboBox cmbInterface;
    private javax.swing.JLabel lblInterface;
    private javax.swing.JLabel lblPort;
    private javax.swing.JScrollPane scrlLog;
    private javax.swing.JSpinner spnPort;
    private javax.swing.JTextArea txtLog;
    // End of variables declaration//GEN-END:variables

    /**
     * 
     * @author Rodrigo Gomes da Silva
     */
    class ComboNetworkItem {
        private String ip;
        private NetworkInterface networkInterface;
        
        public ComboNetworkItem(NetworkInterface ni) {
            this.ip = findNetworkInterfaceIpAddress(ni);
            this.networkInterface = ni;
        }

        public String getIp() {
            return ip;
        }

        public void setIp(String ip) {
            this.ip = ip;
        }

        public NetworkInterface getNetworkInterface() {
            return networkInterface;
        }

        public void setNetworkInterface(NetworkInterface networkInterface) {
            this.networkInterface = networkInterface;
        }

        @Override
        public String toString() {
            return this.ip;
        }
    }
    
    /**
     * 
     * @author Rodrigo Gomes da Silva
     */
    class NetworkInterfaceComboBoxModel extends DefaultComboBoxModel {
        NetworkInterfaceComboBoxModel() {
            super();
            try {
                Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces();
                while(e.hasMoreElements()) {
                    NetworkInterface ni = e.nextElement();
                    if (findNetworkInterfaceIpAddress(ni) != null) {
                        ComboNetworkItem item = new ComboNetworkItem(ni);
                    
                    
                        addElement(item);
                    }
                }
            } catch(SocketException e) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, i18nMessages.getString("main.cant.get.network.interfaces"), e);
            }
        }
    }
}