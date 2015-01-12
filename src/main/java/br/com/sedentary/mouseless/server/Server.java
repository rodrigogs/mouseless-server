package br.com.sedentary.mouseless.server;

import java.util.logging.Logger;

import br.com.sedentary.mouseless.mouse.MouseClickType;
import br.com.sedentary.mouseless.mouse.MouseControll;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.ConnectListener;
import com.corundumstudio.socketio.listener.DataListener;
import com.corundumstudio.socketio.listener.DisconnectListener;

/**
 * @author Rodrigo Gomes da Silva
 *
 */
public class Server {
    private final static Logger LOGGER = Logger.getLogger(Server.class.getName());

    public static final Integer DEFAULT_SERVER_PORT = 9792;

    private SocketIOServer server = null;
    private Callback callback = null;
    private static MouseControll mouse = null;

    private String host = null;
    private Integer port = null;
    
    private Boolean running = false;
    
    /**
     * 
     * @param host
     * @param port
     * @param callback 
     */
    public Server(String host, Integer port, Callback callback) {
        this.host = host;
        this.port = port;
        this.callback = callback;
    }
    
    /**
     * 
     */
    public void start() {
        // Iniciando controle do mouse
        mouse = new MouseControll();

        // Server configuration
        Configuration config = getServerConfiguration();

        // Server initialization
        server = new SocketIOServer(config);

        // Add server listeners
        server = setServerListeners(server);

        // Star server
        LOGGER.info("Inicializando o servidor");
        
        try {
            server.start();
            
            ServerInfo serverInfo = new ServerInfo(
                    server.getConfiguration().getHostname(),
                    server.getConfiguration().getPort());
            callback.serverStarted(serverInfo);
            
            running = true;
        } catch (Exception ex) {
            callback.error(ex.getMessage());
            running = false;
        }
    }
    
    /**
     * 
     */
    public void stop() {
       server.stop();
       callback.serverStoped();
       
       running = false;
    }
    
    /**
     * 
     * @return 
     */
    public Boolean isRunning() {
        return running;
    }
    
    /**
     * @return
     */
    private Configuration getServerConfiguration() {
        LOGGER.info("Gerando configuração do servidor");
	
        Configuration config = new Configuration();
        
        config.setHostname(host);
        config.setPort(port);
	
        return config;
    }
    
    /**
     * @param server
     */
    private SocketIOServer setServerListeners(SocketIOServer server) {
        LOGGER.info("Configurando eventos do servidor");
        
        LOGGER.info("Configurando evento de conexão");
        server.addConnectListener(new ConnectListener() {

            @Override
            public void onConnect(SocketIOClient client) {
                callback.connected(client);
            }
        });

        LOGGER.info("Configurando evento de desconexão");
        server.addDisconnectListener(new DisconnectListener() {

            @Override
            public void onDisconnect(SocketIOClient client) {
                callback.disconnected();
            }
        });
        
        LOGGER.info("Configurando evento de coordenadas");
        server.addEventListener("coordinate", Coordinates.class, new DataListener<Coordinates>() {
            
            @Override
            public void onData(SocketIOClient client, Coordinates coords, AckRequest request) throws Exception {
                callback.receivedCoordinates(coords);
                
                Integer[] coordinates = new Integer[2];
                coordinates[0] = coords.x.intValue();
                coordinates[1] = coords.y.intValue();
                mouse.move(coordinates);
            }
        });
        
        LOGGER.info("Configurando evento de mouseclick");
        server.addEventListener("mouseclick", MouseClickType.class, new DataListener<MouseClickType>() {
            
            @Override
            public void onData(SocketIOClient client, MouseClickType type, AckRequest request) throws Exception {
                callback.receivedMouseClick(type);
                
                mouse.click(type);
            }
        });

        return server;
    }
    
    /**
     * 
     */
    public interface Callback {
        void connected(SocketIOClient client);
        void disconnected();
        void serverStarted(ServerInfo serverInfo);
        void serverStoped();
        void receivedCoordinates(Coordinates coords);
        void receivedMouseClick(MouseClickType type);
        void error(String error);
    }
    
    /**
     * 
     */
    public class ServerInfo {
        private final String hostname;
        private final Integer port;
        
        /**
         * 
         * @param hostname
         * @param port 
         */
        public ServerInfo(String hostname, Integer port) {
            this.hostname = hostname;
            this.port = port;
        }
        
        public String getHostname() {
            return hostname;
        }

        public Integer getPort() {
            return port;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("Host name: ").append(this.hostname).append(System.getProperty("line.separator"));
            sb.append("Port: ").append(this.port);
            
            return sb.toString();
        }
    }
}