package me.enz0z;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.URLDecoder;
import java.util.Date;

public class Threader extends Thread {

	private File _rootDir;
	private Socket _socket;
	
	public Threader(Socket socket, File rootDir) {
		_rootDir = rootDir;
		_socket = socket;
	}

	@Override
	public void run() {
		InputStream reader = null;

		try {
			_socket.setSoTimeout(30000);
			BufferedReader in = new BufferedReader(new InputStreamReader(_socket.getInputStream()));
			BufferedOutputStream out = new BufferedOutputStream(_socket.getOutputStream());
			String request = in.readLine();

			if (request == null || !request.startsWith("GET ") || !(request.endsWith(" HTTP/1.0") || request.endsWith("HTTP/1.1"))) {
				sendError(out, 500, "Invalid Method.");
				return;
			}
			String path = request.substring(4, request.length() - 9);
			File file = new File(_rootDir, URLDecoder.decode(path, "UTF-8")).getCanonicalFile();

			if (!file.getName().equals("") && !file.getName().equals("favicon.ico")) {
				System.out.print("webServer >> " + _socket.getInetAddress().getHostAddress() + " requested " +  path + ".\n");
			}
			if (file.isDirectory()) {
				File indexFile = new File(file, "index.html");

				if (indexFile.exists() && !indexFile.isDirectory()) {
					file = indexFile;
				}
			}
			if (!file.toString().startsWith(_rootDir.toString())) {
				sendError(out, 403, "Permission Denied.");
			} else if (!file.exists()) {
				sendError(out, 404, "File Not Found.");
			} else if (file.isDirectory()) {
				if (!path.endsWith("/")) {
					path = path + "/";
				}
				File[] files = file.listFiles();

				sendHeader(out, 200, "text/html", -1, System.currentTimeMillis());
				for (Integer i = 0; i < files.length; i++) {
					file = files[i];
					String filename = file.getName();

					out.write(("<a href=\"" + path + filename + "\">" + path + filename + "</a><br>\n").getBytes());
				}
				out.write(("</p><hr><p>webServer</p></body><html>").getBytes());
			} else {
				reader = new BufferedInputStream(new FileInputStream(file));
				String contentType = Core.MIME_TYPES.get(getExtension(file));

				if (contentType == null) {
					contentType = "application/octet-stream";
				}
				sendHeader(out, 200, contentType, file.length(), file.lastModified());
				byte[] buffer = new byte[4096];
				Integer bytesRead;

				while ((bytesRead = reader.read(buffer)) != -1) {
					out.write(buffer, 0, bytesRead);
				}
				reader.close();
			}
			out.flush();
			out.close();
		} catch (IOException e1) {
			if (reader != null) {
				e1.printStackTrace();
				try {
					reader.close();
				} catch (IOException e2) {
					e2.printStackTrace();
				}
			}
		}
	}

	private static void sendHeader(BufferedOutputStream out, Integer code, String contentType, long contentLength, long lastModified) throws IOException {
		out.write((
			"HTTP/1.0 " + code + " OK\r\n" + 
			"Date: " + new Date().toString() + "\r\n" + 
			"Content-Type: " + contentType + "\r\n" + 
			((contentLength != -1) ? "Content-Length: " + contentLength + "\r\n" : "") + 
			"Last-modified: " + new Date(lastModified).toString() + "\r\n\r\n"
		).getBytes());
	}

	private static void sendError(BufferedOutputStream out, Integer code, String message) throws IOException {
		sendHeader(out, code, "text/html", message.length(), System.currentTimeMillis());
		out.write(message.getBytes());
		out.flush();
		out.close();
	}

	public static String getExtension(File file) {
		String extension = "";
		String filename = file.getName();
		Integer dotPos = filename.lastIndexOf(".");

		if (dotPos >= 0) {
			extension = filename.substring(dotPos);
		}
		return extension.toLowerCase();
	}
}