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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.quickcache.client.config.Endpoint;
import org.quickcache.client.config.QuickCacheMethod;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quickcache.server.exception.QuickCacheOperationException;

public class MapOperation {

	private String mapOpsUrl;

	private ObjectMapper mapper = new ObjectMapper();

	public MapOperation(String baseUrl) {
		this.mapOpsUrl = baseUrl.concat("/" + Endpoint.MAP.getValue() + "/");
	}

	public String getMapValue(String key, String field) {
		if (key == null || key.isEmpty()) {
			throw new QuickCacheOperationException(6);
		}
		if (field == null || field.isEmpty()) {
			throw new QuickCacheOperationException(8);
		}
		try {
			HttpURLConnection connection = getUrlConnection(key, field);
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

	public Set<String> getMapFields(String key) {
		if (key == null || key.isEmpty()) {
			throw new QuickCacheOperationException(6);
		}
		try {
			HttpURLConnection connection = getUrlConnection(key, "fields");
			connection.setRequestMethod(QuickCacheMethod.GET.toString());
			connection.setRequestProperty("Accept", "application/json");

			if (connection.getResponseCode() >= 200) {
				BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

				String responsePart;
				List<String> responseBuffer = new ArrayList<>();
				while ((responsePart = bufferedReader.readLine()) != null) {
					responseBuffer.add(responsePart);
				}

				return mapper.readValue(String.join("\n", responseBuffer), new TypeReference<HashSet<String>>() {
				});
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

	public Map<String, String> getMapFieldValues(String key) {
		if (key == null || key.isEmpty()) {
			throw new QuickCacheOperationException(6);
		}
		try {
			HttpURLConnection connection = getUrlConnection(key, "all");
			connection.setRequestMethod(QuickCacheMethod.GET.toString());
			connection.setRequestProperty("Accept", "application/json");

			if (connection.getResponseCode() >= 200) {
				BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

				String responsePart;
				List<String> responseBuffer = new ArrayList<>();

				while ((responsePart = bufferedReader.readLine()) != null) {
					responseBuffer.add(responsePart);
				}

				return mapper.readValue(String.join("\n", responseBuffer),
						new TypeReference<HashMap<String, String>>() {
						});
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

	public String setMapValue(String key, String field, String value) {
		if (key == null || key.isEmpty()) {
			throw new QuickCacheOperationException(6);
		}
		if (value == null || value.isEmpty()) {
			throw new QuickCacheOperationException(7);
		}
		if (field == null || field.isEmpty()) {
			throw new QuickCacheOperationException(8);
		}
		try {
			HttpURLConnection connection = getUrlConnection(key, field);

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

	private HttpURLConnection getUrlConnection(String key, String field) throws IOException {
		URL url = new URL(this.mapOpsUrl + key + "/" + field);
		return (HttpURLConnection) url.openConnection();
	}

}
