package db;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import redis.clients.jedis.Jedis;

/**
 * API DOC: http://www.runoob.com/redis/redis-java.html
 * 
 * @author max
 *
 */
public class Redis {

	private static String ip = "172.16.81.173";
	private static int port = 6379;
	/**static {
		Properties prop = new Properties();
		InputStream in = null;
		try {
			in = new BufferedInputStream (new FileInputStream("c:/db.Properties"));
			 prop.load(in);
			 ip = prop.getProperty("ip");
			 port = new Integer(prop.getProperty("port"));
	         in.close();
		} catch (Exception  e) {
			e.printStackTrace();
		}
		
	}*/

	private Jedis jedis = new Jedis(ip, port);

	public void set(String key, String value) {
		jedis.set(key, value);
	}
	
	public String ping() {
		return jedis.ping();
	}
			
}