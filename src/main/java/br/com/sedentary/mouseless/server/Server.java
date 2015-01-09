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
    
    private Boolean connected = false;
    
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
        server.start();
        
        callback.log("Servidor inicializado com sucesso");
        callback.log("Endereço: " + server.getConfiguration().getHostname());
        callback.log("Porta: " + String.valueOf(server.getConfiguration().getPort()));
    }
    
    /**
     * 
     */
    public void stop() {
       server.stop();
    }
    
    /**
     * 
     * @return 
     */
    public Boolean isConnected() {
        return connected;
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
                
                connected = true;
            }
        });

        LOGGER.info("Configurando evento de desconexão");
        server.addDisconnectListener(new DisconnectListener() {

            @Override
            public void onDisconnect(SocketIOClient client) {
                callback.disconnected();
                
                connected = false;
            }
        });
        
        LOGGER.info("Configurando evento de coordenadas");
        server.addEventListener("coordinate", Coordinates.class, new DataListener<Coordinates>() {
            
            @Override
            public void onData(SocketIOClient client, Coordinates coords, AckRequest request) throws Exception {
                callback.log("Coords: " + coords.toString());
                
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
                callback.log("Mouseclick: " + type.toString());
                
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
        void log(String text);
    }
}