package me.enz0z;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class Core extends Thread {
	
	private File _rootDir;
	private ServerSocket _serverSocket;
	private static String _version = "0.2";
	private boolean _running = true;
	public static final Map<String, String> MIME_TYPES = new HashMap<String, String>();

	static {
		String image = "image/";
		MIME_TYPES.put(".gif", image + "gif");
		MIME_TYPES.put(".jpg", image + "jpeg");
		MIME_TYPES.put(".jpeg", image + "jpeg");
		MIME_TYPES.put(".png", image + "png");

		String text = "text/";
		MIME_TYPES.put(".html", text + "html");
		MIME_TYPES.put(".htm", text + "html");
		MIME_TYPES.put(".txt", text + "plain");
	}

	public static void main(String[] args) {
		try {
			new Core(new File("./www/"), 80);
		} catch (Exception e) {
			System.out.println(e);
		}
	}
	
	public Core(File _rootDir, Integer port) throws IOException {
		this._rootDir = _rootDir.getCanonicalFile();

		if (!this._rootDir.isDirectory()) {
			_rootDir.mkdir();
		}
		_serverSocket = new ServerSocket(port);
		start();
		System.out.print("webServer >> " + "Started and running on port 80.\n");
	}

	@Override
	public void run() {
		while (_running) {
			try {
				Socket socket = _serverSocket.accept();
				Threader requestThread = new Threader(socket, _rootDir);
				
				requestThread.start();
			} catch (Exception e) {
				System.exit(1);
			}
		}
	}

	public static String getVersion() {
		return "v" + _version;
	}
}