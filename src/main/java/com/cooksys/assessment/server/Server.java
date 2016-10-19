package com.cooksys.assessment.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Server implements Runnable {
	private Logger log = LoggerFactory.getLogger(Server.class);
	private ConcurrentHashMap<String, ClientHandler> allUsers = new ConcurrentHashMap<>();
	
	
	private int port;
	private ExecutorService executor;
	
	public Server(int port, ExecutorService executor) {
		super();
		this.port = port;
		this.executor = executor;
	}

	public ConcurrentHashMap<String, ClientHandler> getAllUsers() {
		return allUsers;
	}
	
	public void addUser(String user, ClientHandler handler) {
		allUsers.put(user, handler);
	}
	
	public void removeUser(String user) {
		allUsers.remove(user);
	}
	
	public ClientHandler getUser(String user) {
		return allUsers.get(user);
	}
	
	public void run() {
		log.info("server started");
		ServerSocket ss;
		try {
			ss = new ServerSocket(this.port);
			while (true) {
				Socket socket = ss.accept();
				ClientHandler handler = new ClientHandler(socket, this);
				executor.execute(handler);
			}
		} catch (IOException e) {
			log.error("Something went wrong :/", e);
		}
	}

}
