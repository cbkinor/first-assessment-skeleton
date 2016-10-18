package com.cooksys.assessment.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cooksys.assessment.model.Message;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ClientHandler implements Runnable {
	private Logger log = LoggerFactory.getLogger(ClientHandler.class);

	private Socket socket;

	public ClientHandler(Socket socket) {
		super();
		this.socket = socket;
	}

	public void run() {
		try {

			ObjectMapper mapper = new ObjectMapper();
			BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			PrintWriter writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
			DateTimeFormatter formattedTime = DateTimeFormatter.ofPattern("hh:mm:ss a MMM d yyyy"); // format message timestamp, using ofPattern makes it reusable - immutable

			while (!socket.isClosed()) {
				String raw = reader.readLine();
				Message message = mapper.readValue(raw, Message.class);
				LocalDateTime currentTime = LocalDateTime.now();
				
				String response = "";
				
				switch (message.getCommand()) {
					case "connect":
						log.info("user <{}> connected", currentTime.format(formattedTime), message.getUsername());
						response = mapper.writeValueAsString(message);
						writer.write(response);
						writer.flush();
						break;
					case "disconnect":
						log.info("user <{}> disconnected", currentTime.format(formattedTime), message.getUsername());
						this.socket.close();
						response = mapper.writeValueAsString(message);
						writer.write(response);
						writer.flush();
						break;
					case "echo":
						log.info("user <{}> echoed message <{}>", currentTime.format(formattedTime), message.getUsername(), message.getContents());
						response = mapper.writeValueAsString(message);
						writer.write(response);
						writer.flush();
						break;
					case "broadcast":
						log.info("user <{}> broadcasted message <{}>", currentTime.format(formattedTime), message.getUsername(), message.getContents());
						response = mapper.writeValueAsString(message);
						writer.write(response);
						writer.flush();
						break;
					case "users":
						log.info("user <{}> users message <{}>", currentTime.format(formattedTime), message.getUsername(), message.getContents());
						response = mapper.writeValueAsString(message);
						writer.write(response);
						writer.flush();
						break;
					case "directMessage":
						log.info("user <{}> direct message <{}>", currentTime.format(formattedTime), message.getUsername(), message.getContents()); // add current time to "connect" case
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
