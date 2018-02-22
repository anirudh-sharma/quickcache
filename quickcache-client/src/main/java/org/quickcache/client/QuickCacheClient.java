package org.quickcache.client;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import org.quickcache.client.config.ClientConfig;
import org.quickcache.client.config.Endpoint;
import org.quickcache.client.config.QuickCacheMethod;

import com.quickcache.server.exception.QuickCacheOperationException;

public class QuickCacheClient {

	private ClientConfig clientConfig;

	private String baseUrl;

	public ClientConfig getClientConfig() {
		return clientConfig;
	}

	public void setClientConfig(ClientConfig clientConfig) {
		this.clientConfig = clientConfig;
		if (this.clientConfig.getHostName() == null || this.clientConfig.getPort() == null) {
			throw new IllegalArgumentException("Hostname or port is not set in client config");
		}
		this.baseUrl = new StringBuilder().append("http://").append(this.clientConfig.getHostName()).append(":")
				.append(this.clientConfig.getPort()).toString();
	}

	public <T> T getObject(String key) {
		return null;
	}

	public <T> void setObject(String key, T object) {

		if (key == null || key.isEmpty()) {
			throw new QuickCacheOperationException(6);
		}
		if (object == null) {
			throw new QuickCacheOperationException(7);
		}
		
//        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        ObjectOutputStream oos = new ObjectOutputStream( baos );
//        oos.writeObject( object );
//        oos.close();
        String value = null;//Base64.getEncoder().encodeToString(baos.toByteArray());
        
		try {
			URL url = new URL(this.baseUrl + "/" + Endpoint.STRING.getValue() + "/" + key);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();

			byte[] postData = value.getBytes(StandardCharsets.UTF_8);

			connection.setDoOutput(true);
			connection.setRequestMethod(QuickCacheMethod.POST.toString());
			connection.setRequestProperty("charset", "utf-8");
			connection.setRequestProperty("Content-Length", Integer.toString(postData.length));

			PrintWriter printWriter = new PrintWriter(connection.getOutputStream());
			printWriter.print(URLEncoder.encode(value, "utf-8"));
			printWriter.flush();

			if (connection.getResponseCode() >= 200) {
				BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
				String responsePart;
				List<String> responseBuffer = new ArrayList<>();
				while ((responsePart = bufferedReader.readLine()) != null) {
					responseBuffer.add(responsePart);
				}
				//return String.join("\n", responseBuffer);
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
		//return null;
	}

	public String getString(String key) {
		if (key == null || key.isEmpty()) {
			throw new QuickCacheOperationException(6);
		}
		try {
			URL url = new URL(this.baseUrl + "/" + Endpoint.STRING.getValue() + "/" + key);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod(QuickCacheMethod.GET.toString());
			connection.setRequestProperty("Accept", "text/plain");

			if (connection.getResponseCode() >= 200) {
				BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

				String responsePart;
				List<String> responseBuffer = new ArrayList<>();
				while ((responsePart = bufferedReader.readLine()) != null) {
					responseBuffer.add(responsePart);
				}
				return String.join("\n", responseBuffer);
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

	public String setString(String key, String value) {
		if (key == null || key.isEmpty()) {
			throw new QuickCacheOperationException(6);
		}
		if (value == null || value.isEmpty()) {
			throw new QuickCacheOperationException(7);
		}
		try {
			URL url = new URL(this.baseUrl + "/" + Endpoint.STRING.getValue() + "/" + key);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();

			byte[] postData = value.getBytes(StandardCharsets.UTF_8);

			connection.setDoOutput(true);
			connection.setRequestMethod(QuickCacheMethod.POST.toString());
			connection.setRequestProperty("charset", "utf-8");
			connection.setRequestProperty("Content-Length", Integer.toString(postData.length));

			PrintWriter printWriter = new PrintWriter(connection.getOutputStream());
			printWriter.print(URLEncoder.encode(value, "utf-8"));
			printWriter.flush();

			if (connection.getResponseCode() >= 200) {
				BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
				String responsePart;
				List<String> responseBuffer = new ArrayList<>();
				while ((responsePart = bufferedReader.readLine()) != null) {
					responseBuffer.add(responsePart);
				}
				return String.join("\n", responseBuffer);
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}
