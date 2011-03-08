 package org.mca.server;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ByteCodeServer extends Thread{

	private ServerSocket server;
	
	public ByteCodeServer(int port, String path) throws IOException {
		init(port, path);
	}

	private void init(int port, String path) throws IOException {
		server = new ServerSocket(port);
		
	}
	
	public static void main(String[] args) {
		try {
			new ByteCodeServer(7070, "/Users/cyril");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		while(true){
			try {
				new Task(server.accept()).start();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private class Task extends Thread{
		
		private Socket sock;
		
		public Task(Socket sock) {
			this.sock = sock;
		}

		@Override
		public void run() {
			try {
				BufferedInputStream in = new BufferedInputStream(sock.getInputStream());
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
