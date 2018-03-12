package org.quickcache.client.config.operations;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.quickcache.client.config.Endpoint;
import org.quickcache.client.config.QuickCacheMethod;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quickcache.server.exception.QuickCacheOperationException;

public class ListOperation {

	private String listgOpsUrl;

	private ObjectMapper mapper = new ObjectMapper();

	public ListOperation(String baseUrl) {
		this.listgOpsUrl = baseUrl.concat("/" + Endpoint.LIST.getValue() + "/");
	}

	public List<String> getListItems(String key) {
		if (key == null || key.isEmpty()) {
			throw new QuickCacheOperationException(6);
		}
		try {
			HttpURLConnection connection = getUrlConnection(key);
			connection.setRequestMethod(QuickCacheMethod.GET.toString());
			connection.setRequestProperty("Accept", "application/json");

			if (connection.getResponseCode() >= 200) {
				BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

				String responsePart;
				List<String> responseBuffer = new ArrayList<>();
				while ((responsePart = bufferedReader.readLine()) != null) {
					responseBuffer.add(responsePart);
				}

				return mapper.readValue(String.join("\n", responseBuffer), new TypeReference<ArrayList<String>>() {
				});
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

	public List<String> getListItems(String key, int start) {
		if (key == null || key.isEmpty()) {
			throw new QuickCacheOperationException(6);
		}
		try {
			HttpURLConnection connection = getUrlConnection(key, start);
			connection.setRequestMethod(QuickCacheMethod.GET.toString());
			connection.setRequestProperty("Accept", "application/json");

			if (connection.getResponseCode() >= 200) {
				BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

				String responsePart;
				List<String> responseBuffer = new ArrayList<>();
				while ((responsePart = bufferedReader.readLine()) != null) {
					responseBuffer.add(responsePart);
				}

				return mapper.readValue(String.join("\n", responseBuffer), new TypeReference<ArrayList<String>>() {
				});
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

	public List<String> getListItems(String key, int start, int length) {
		if (key == null || key.isEmpty()) {
			throw new QuickCacheOperationException(6);
		}
		try {
			HttpURLConnection connection = getUrlConnection(key, start, length);
			connection.setRequestMethod(QuickCacheMethod.GET.toString());
			connection.setRequestProperty("Accept", "application/json");

			if (connection.getResponseCode() >= 200) {
				BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

				String responsePart;
				List<String> responseBuffer = new ArrayList<>();
				while ((responsePart = bufferedReader.readLine()) != null) {
					responseBuffer.add(responsePart);
				}

				return mapper.readValue(String.join("\n", responseBuffer), new TypeReference<ArrayList<String>>() {
				});
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

	public String removeListItem(String key, int position) {
		if (key == null || key.isEmpty()) {
			throw new QuickCacheOperationException(6);
		}
		try {
			HttpURLConnection connection = getUrlConnection(key, position);
			connection.setRequestMethod(QuickCacheMethod.DELETE.toString());
			//connection.setRequestProperty("Accept", "text/plain");

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

	public String addListItem(String key, String value) {
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
		URL url = new URL(this.listgOpsUrl + key);
		return (HttpURLConnection) url.openConnection();
	}

	private HttpURLConnection getUrlConnection(String key, int offset) throws IOException {
		URL url = new URL(this.listgOpsUrl + key + "/" + offset);
		return (HttpURLConnection) url.openConnection();
	}

	private HttpURLConnection getUrlConnection(String key, int start, int length) throws IOException {
		URL url = new URL(this.listgOpsUrl + key + "/" + start + "/" + length);
		return (HttpURLConnection) url.openConnection();
	}

}
