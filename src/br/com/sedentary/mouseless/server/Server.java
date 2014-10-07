package br.com.sedentary.mouseless.server;

import java.net.Inet4Address;
import java.net.UnknownHostException;
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
	
	private static final String COORDINATES_TYPE	= "C";
	private static final String MOUSE_CLICK_TYPE	= "M";
	
	public static final Integer SERVER_PORT			= 9792;
	
	private static MouseControll mouse;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		LOGGER.info("Inicializando o sistema");
		
		// Iniciando controle do mouse
		mouse = new MouseControll();
		
		// Server configuration
		Configuration config = getServerConfiguration();
		
		// Server initialization
		SocketIOServer server = new SocketIOServer(config);
		
		// Add server listeners
		server = setServerListeners(server);
		
		// Star server
		LOGGER.info("Inicializando o servidor");
		server.start();
		System.out.println("Servidor inicializado com sucesso");
		System.out.println("Endereço: " + server.getConfiguration().getHostname());
		System.out.println("Porta: " + String.valueOf(server.getConfiguration().getPort()));
	}
	
	/**
	 * @return
	 */
	private static Configuration getServerConfiguration() {
		LOGGER.info("Gerando configuração do servidor");
		
		Configuration config = new Configuration();
		try {
			config.setHostname(Inet4Address.getLocalHost().getHostAddress());
		} catch (UnknownHostException e) {
			LOGGER.warning("Erro ao adquirir endereço IP");
			e.printStackTrace();
		}
		config.setPort(SERVER_PORT);
		
		return config;
	}
	
	/**
	 * @param server
	 */
	private static SocketIOServer setServerListeners(SocketIOServer server) {
		LOGGER.info("Configurando eventos do servidor");
		
		LOGGER.info("Configurando evento de dados");
		server.addMessageListener(new DataListener<String>() {
			
			@Override
			public void onData(SocketIOClient client, String message, AckRequest request) {
				
				String[] args = message.split(":");
				
				if ((args[0]).equals(COORDINATES_TYPE)) {
					Integer[] coordinates = new Integer[2];
					
					coordinates[0] = Integer.valueOf(args[1]);
					coordinates[1] = Integer.valueOf(args[2]);
					
					mouse.move(coordinates);
					
				} else if ((args[0]).equals(MOUSE_CLICK_TYPE)) {
					if (args[1].equals(MouseClickType.LEFT_DOWN.toString())) {
						mouse.click(MouseClickType.LEFT_DOWN);
						
					} else if (args[1].equals(MouseClickType.LEFT_UP.toString())) {
						mouse.click(MouseClickType.LEFT_UP);
						
					} else if (args[1].equals(MouseClickType.RIGHT_DOWN.toString())) {
						mouse.click(MouseClickType.RIGHT_DOWN);
						
					} else if (args[1].equals(MouseClickType.RIGHT_UP.toString())) {
						mouse.click(MouseClickType.RIGHT_UP);
						
					}
				}
			}
		});
		
		LOGGER.info("Configurando evento de conexão");
		server.addConnectListener(new ConnectListener() {
			
			@Override
			public void onConnect(SocketIOClient client) {
				
				System.out.println("Conectado à: " + client.getRemoteAddress().toString());
			}
		});
		
		LOGGER.info("Configurando evento de desconexão");
		server.addDisconnectListener(new DisconnectListener() {
			
			@Override
			public void onDisconnect(SocketIOClient arg0) {
				
				System.out.println("Desconectado");
			}
		});
		
		LOGGER.info("Configurando evento de recebimento de coordenadas");
		server.addJsonObjectListener(Coordinates.class, new DataListener<Coordinates>() {
			
			@Override
			public void onData(SocketIOClient client, Coordinates coords,
					AckRequest request) {
				System.out.println(coords.x);
				System.out.println(coords.y);
				System.out.println(coords.z);
			}

		});
		
		server.addEventListener("teste", Coordinates.class, new DataListener<Coordinates>() {

			@Override
			public void onData(SocketIOClient client, Coordinates coords,
					AckRequest ackSender) {
				System.out.println(coords.x);
				System.out.println(coords.y);
				System.out.println(coords.z);
			}
		});
		
		return server;
	}
}
