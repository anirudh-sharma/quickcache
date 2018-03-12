package org.quickcache.client.config.operations;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import org.quickcache.client.config.Endpoint;
import org.quickcache.client.config.QuickCacheMethod;

import com.quickcache.server.exception.QuickCacheOperationException;

public class StringOperation {

	private String stringOpsUrl;

	public StringOperation(String baseUrl) {
		this.stringOpsUrl = baseUrl.concat("/" + Endpoint.STRING.getValue() + "/");
	}

	public <T extends Serializable> T getObject(String key) {
		if (key == null || key.isEmpty()) {
			throw new QuickCacheOperationException(6);
		}
		try {
			HttpURLConnection connection = getUrlConnection(key);
			connection.setRequestMethod(QuickCacheMethod.GET.toString());
			connection.setRequestProperty("Accept", "text/plain");

			if (connection.getResponseCode() >= 200) {
				BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

				String response = bufferedReader.readLine();
				ObjectInputStream objectInputStream = new ObjectInputStream(
						new ByteArrayInputStream(Base64.getDecoder().decode(response)));
				@SuppressWarnings("unchecked")
				T object = (T) objectInputStream.readObject();
				return object;
			}

		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		return null;
	}

	public <T extends Serializable> void setObject(String key, T object) {

		if (key == null || key.isEmpty()) {
			throw new QuickCacheOperationException(6);
		}
		if (object == null) {
			throw new QuickCacheOperationException(7);
		}

		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		ObjectOutputStream objectOutputStream = null;
		try {
			objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
			objectOutputStream.writeObject(object);
			objectOutputStream.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		} finally {
			try {
				objectOutputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		try {
			HttpURLConnection connection = getUrlConnection(key);

			byte[] postData = Base64.getEncoder().encode(byteArrayOutputStream.toByteArray());

			connection.setDoOutput(true);
			connection.setRequestMethod(QuickCacheMethod.POST.toString());
			connection.setRequestProperty("charset", "utf-8");
			connection.setRequestProperty("Content-Length", Integer.toString(postData.length));

			OutputStream outputStream = connection.getOutputStream();
			outputStream.write(postData);
			outputStream.flush();

			if (connection.getResponseCode() >= 200) {
				BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
				String responsePart;
				List<String> responseBuffer = new ArrayList<>();
				while ((responsePart = bufferedReader.readLine()) != null) {
					responseBuffer.add(responsePart);
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String getString(String key) {
		if (key == null || key.isEmpty()) {
			throw new QuickCacheOperationException(6);
		}
		try {
			HttpURLConnection connection = getUrlConnection(key);
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
			HttpURLConnection connection = getUrlConnection(key);

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

	private HttpURLConnection getUrlConnection(String key) throws IOException {
		URL url = new URL(this.stringOpsUrl + key);
		return (HttpURLConnection) url.openConnection();
	}

}
