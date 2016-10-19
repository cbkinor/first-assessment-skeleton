package com.cooksys.assessment.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cooksys.assessment.model.Message;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ClientHandler implements Runnable {
	private Logger log = LoggerFactory.getLogger(ClientHandler.class);

	private Socket socket;
	private Server server;
	private String userName;
	String response = "";

	public ClientHandler(Socket socket, Server server) {
		super();
		this.socket = socket;
		this.server = server;
	}
	
	public void setUserName(String userName) {
		this.userName = userName;
	}


	
	public void run() {
		try {

			ObjectMapper mapper = new ObjectMapper();
			BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			PrintWriter writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
			DateTimeFormatter formattedTime = DateTimeFormatter.ofPattern("hh:mm:ss a MMM d yyyy"); // format message timestamp, using ofPattern makes it reusable - immutable

			while (!socket.isClosed()) {
				ConcurrentHashMap<String, ClientHandler> users = server.getAllUsers();
				String raw = reader.readLine();
				Message message = mapper.readValue(raw, Message.class);
				LocalDateTime currentTime = LocalDateTime.now();
				
				switch (message.getCommand()) {
					case "connect":
						log.info("user <{}> connected", currentTime.format(formattedTime), message.getUsername());
						message.setContents(currentTime.format(formattedTime) + " " + message.getUsername() + " has connected");
						response = mapper.writeValueAsString(message);
						writer.write(response);
						writer.flush();
						break;
					case "disconnect":
						log.info("user <{}> disconnected", currentTime.format(formattedTime), message.getUsername());
						message.setContents(currentTime.format(formattedTime) + " " + message.getUsername() + " has disconnected");
						response = mapper.writeValueAsString(message);
						writer.write(response);
						writer.flush();
						this.socket.close();
						break;
					case "echo":
						log.info("user <{}> echoed message <{}>", currentTime.format(formattedTime), message.getUsername(), message.getContents());
						message.setContents(currentTime.format(formattedTime) + " " + message.getUsername() + " (echo): " + message.getContents());
						response = mapper.writeValueAsString(message);
						writer.write(response);
						writer.flush();
						break;
					case "broadcast":
						log.info("user <{}> broadcasted message <{}>", currentTime.format(formattedTime), message.getUsername(), message.getContents());
						message.setContents(currentTime.format(formattedTime) + " " + message.getUsername() + " (all): " + message.getContents());
						response = mapper.writeValueAsString(message);
						writer.write(response);
						writer.flush();
						break;
					case "users":
						log.info("user <{}> users message <{}>", currentTime.format(formattedTime), message.getUsername());
						message.setContents(currentTime.format(formattedTime) + " " + message.getUsername() + " currently connected users: ");
						response = mapper.writeValueAsString(message);
						writer.write(response);
						writer.flush();
						break;
					case "directMessage":
						log.info("user <{}> direct message <{}>", currentTime.format(formattedTime), message.getUsername(), message.getContents());
						message.setContents(currentTime.format(formattedTime) + " " + message.getUsername() + " (whisper): " + message.getContents());
						response = mapper.writeValueAsString(message);
						writer.write(response);
						writer.flush();
						break;
				}
			}

		} catch (IOException e) {
			log.error("Something went wrong :/", e);
		}
	}

}
