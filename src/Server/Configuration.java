package Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Class used to parse configurationfile
 */
public class Configuration {
	private static final String _workDir = System.getProperty("user.dir").endsWith("/") ? System.getProperty("user.dir") : System.getProperty("user.dir") + "/";
	private HashMap<Integer, String> _serverIdToServer;
	
	public Configuration(List<String> servers) {
		_serverIdToServer = new HashMap<Integer, String>();
		for (int i = 0; i < servers.size(); i++)
			_serverIdToServer.put(i+1, servers.get(i));
	}
	
	/**
	 * @return Map from serverId -> server ip:port
	 */
	public HashMap<Integer, String> getAllServers() {
		return _serverIdToServer;
	}
	
	public static String getWorkDir() {
		return _workDir;
	}
	
	public static Configuration fromFile(String fullPath) throws IOException {
		ArrayList<String> servers = new ArrayList<String>();
		
		// Read file
		Path path = FileSystems.getDefault().getPath(".", fullPath);
		BufferedReader reader = Files.newBufferedReader(path, Charset.defaultCharset());
		String line = null;
		while ( (line = reader.readLine()) != null ) {
			line = line.toLowerCase().trim();
			if (line.startsWith("servers:")) {
				String rawServers = line.substring(line.indexOf("servers:") + 8);
				for (String server : rawServers.split(","))
					servers.add(server);
			}
		}
		
		return new Configuration(servers);
	}
}