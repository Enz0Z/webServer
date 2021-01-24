package me.enz0z;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class Core extends Thread {

	public static final Map<String, String> MIME_TYPES = new HashMap<String, String>();
	private boolean _running = true;
	private File _rootDir;
	private ServerSocket _serverSocket;

	static {
		MIME_TYPES.put(".gif", "image/" + "gif");
		MIME_TYPES.put(".jpg", "image/" + "jpeg");
		MIME_TYPES.put(".jpeg", "image/" + "jpeg");
		MIME_TYPES.put(".png", "image/" + "png");
		MIME_TYPES.put(".html", "text/" + "html");
		MIME_TYPES.put(".htm", "text/" + "html");
		MIME_TYPES.put(".txt", "text/" + "plain");
	}

	public static void main(String[] args) {
		try {
			if (args.length > 1) {
				new Core(new File("./www/"), Integer.parseInt(args[1]));
			} else {
				new Core(new File("./www/"), 80);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public Core(File dir, Integer port) throws IOException {
		_rootDir = dir.getCanonicalFile();

		if (!dir.isDirectory()) {
			dir.mkdir();
		}
		_serverSocket = new ServerSocket(port);

		start();
		System.out.print("webServer >> " + "Running on port " + port + ".\n");
	}

	@Override
	public void run() {
		while (_running) {
			try {
				Socket socket = _serverSocket.accept();
				Threader requestThread = new Threader(socket, _rootDir);

				requestThread.start();
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
	}
}