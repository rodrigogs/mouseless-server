package com.sedentary.mouseless.main;

import com.sedentary.mouseless.mouse.MouseClickType;
import com.sedentary.mouseless.server.Coordinates;
import com.sedentary.mouseless.server.Server;
import com.corundumstudio.socketio.SocketIOClient;
import java.awt.AWTException;
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
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JFrame;

/**
 *
 * @author Rodrigo Gomes da Silva
 */
public class Main extends javax.swing.JFrame {
    
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
                    cmbInterface.setSelectedItem(ni);
                }
            } catch (SocketException ex) {
                logAppInfo("Não foi possível carregar a configuração");
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        // Load server port configuration
        Integer port = (Integer) Preferences.get(Preferences.SERVER_PORT, Integer.class, null);
        if (port == null) {
            port = Server.DEFAULT_SERVER_PORT;
        }
        spnPort.setValue(port);
    }
    
    /**
     * 
     */
    private void createSystemTrayIcon() {
        //Check the SystemTray is supported
        if (!SystemTray.isSupported()) {
            System.out.println("SystemTray is not supported");
            return;
        }
        final PopupMenu popup = new PopupMenu();
        final TrayIcon trayIcon;
        trayIcon = new TrayIcon(new ImageIcon(getClass().getResource("/icon.png")).getImage(), "Mouseless-Server");
        trayIcon.setImageAutoSize(true);
        trayIcon.addMouseListener(systemTrayOnClickListener);
        final SystemTray tray = SystemTray.getSystemTray();
        
        // Create a pop-up menu components
        startStopItem = new MenuItem((server != null && server.isRunning()) == true ? "Parar" : "Iniciar");
        MenuItem exitItem = new MenuItem("Exit");
        
        startStopItem.addActionListener(startStopItemOnClickListener);
        exitItem.addActionListener(exitItemOnClickListener);
       
        //Add components to pop-up menu
        popup.add(startStopItem);
        popup.add(exitItem);
       
        trayIcon.setPopupMenu(popup);
        
        try {
            tray.add(trayIcon);
        } catch (AWTException e) {
            System.out.println("TrayIcon could not be added.");
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
                NetworkInterface ntInterface = (NetworkInterface) ie.getItem();
                String ip = getNetworkInterfaceIpAddress(ntInterface);
                if (ip != null) {
                    if (server == null || (server != null && !server.isRunning())) {
                        btnStartStop.setEnabled(true);
                    }
                } else {
                    if (server == null || (server != null && !server.isRunning())) {
                        btnStartStop.setEnabled(false);
                    }
                    ip = "Interface não possui uma coniguração de IP válida";
                }
            
            txtLog.setText(txtLog.getText() + ip + System.getProperty("line.separator"));
            
            Preferences.set(Preferences.NETWORK_INTERFACE, ip);
            }
        }
    };

    /**
     * 
     * @param ni
     * @return 
     */
    private String getNetworkInterfaceIpAddress(NetworkInterface ni) {
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

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Mouseless Server");
        setIconImage(new ImageIcon(getClass().getResource("/icon.png")).getImage());
        setMinimumSize(new java.awt.Dimension(360, 210));
        setName("frmMain"); // NOI18N

        lblInterface.setText("Interface:");

        cmbInterface.setModel(new NetworkInterfaceComboBoxModel());
        cmbInterface.setToolTipText("Selecione uma interface de rede");

        btnStartStop.setText("Iniciar");
        btnStartStop.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnStartStopActionPerformed(evt);
            }
        });

        txtLog.setEditable(false);
        txtLog.setColumns(20);
        txtLog.setRows(5);
        scrlLog.setViewportView(txtLog);

        lblPort.setText("Porta:");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(cmbInterface, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblInterface)
                            .addComponent(lblPort)
                            .addComponent(spnPort, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(btnStartStop, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(scrlLog, javax.swing.GroupLayout.DEFAULT_SIZE, 336, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblInterface)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cmbInterface, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblPort)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(spnPort, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnStartStop)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(scrlLog, javax.swing.GroupLayout.DEFAULT_SIZE, 110, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * 
     * @param evt 
     */
    private void btnStartStopActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnStartStopActionPerformed
        startStopServer();
    }//GEN-LAST:event_btnStartStopActionPerformed

    /**
     * 
     */
    public void startStopServer() {
        
        if (server == null || (server != null && !server.isRunning())) {
            
            NetworkInterface ntInterface = (NetworkInterface) cmbInterface.getSelectedItem();
            String ip = getNetworkInterfaceIpAddress(ntInterface);
            
            if (ip != null) {
                server = new Server(
                        getNetworkInterfaceIpAddress(ntInterface),
                        Integer.parseInt(spnPort.getValue().toString()),
                        serverCallback);
                server.start();
            } else {
                logAppInfo("Selecione uma interface com configuração de IP válida");
            }
            
        } else {
            
            server.stop();
            
        }
    }
    
    /**
     * 
     */
    private final Server.Callback serverCallback = new Server.Callback() {

        @Override
        public void connected(SocketIOClient client) {
            logAppInfo("Conectado à: " + client.getHandshakeData().getAddress());
        }

        @Override
        public void disconnected() {
            logAppInfo("Desconectado");
        }

        @Override
        public void serverStarted(Server.ServerInfo serverInfo) {
            btnStartStop.setText("Parar");
            startStopItem.setLabel("Parar");
            
            logAppInfo("Servidor iniciado");
            logAppInfo(serverInfo.toString());
        }

        @Override
        public void serverStoped() {
            btnStartStop.setText("Iniciar");
            startStopItem.setLabel("Iniciar");
            
            logAppInfo("Servidor encerrado");
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
            logAppInfo("Erro: " + error);
        }
    };
    
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
    private javax.swing.JButton btnStartStop;
    private javax.swing.JComboBox cmbInterface;
    private javax.swing.JLabel lblInterface;
    private javax.swing.JLabel lblPort;
    private javax.swing.JScrollPane scrlLog;
    private javax.swing.JSpinner spnPort;
    private javax.swing.JTextArea txtLog;
    // End of variables declaration//GEN-END:variables
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
                addElement(ni);
            }
        } catch(SocketException e) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Can't get network interfaces", e);
        }
    }
}