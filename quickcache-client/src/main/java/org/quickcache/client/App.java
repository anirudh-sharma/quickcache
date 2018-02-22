package org.quickcache.client;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.util.Base64;
import java.util.HashSet;
import java.util.Set;

import org.quickcache.client.config.ClientConfig;

/**
 * Hello world!
 *
 */
public class App {
	public static void main(String[] args) throws Exception {

		Set<String> set1 = new HashSet<>();
		set1.add("one");
		set1.add("two");
		System.out.println(set1);
		
		ClientConfig clientConfig = new ClientConfig();
		clientConfig.setHostName("127.0.0.1");
		clientConfig.setPort(9090);
		QuickCacheClient cacheClient = new QuickCacheClient();
		cacheClient.setClientConfig(clientConfig);
		
		Employee employee = new Employee();
		
		employee.setName("Anirudh Sharma");
		employee.setAge(28);
		
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream( baos );
        oos.writeObject( employee );
        oos.close();
        String value = Base64.getEncoder().encodeToString(baos.toByteArray());
        
//		long start = System.currentTimeMillis();
//		System.out.println(cacheClient.getString("foo504"));
//		
//		cacheClient.setString("foo504", "this is a really long value to set for a particular key");
//		
//		long end = System.currentTimeMillis();
//		System.out.println("Duration: "+(end - start));
//		System.out.println(cacheClient.getString("foo504"));
	
	}
}
