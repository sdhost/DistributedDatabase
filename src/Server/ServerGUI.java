package Server;

import java.awt.EventQueue;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.util.Date;
import java.util.Map.Entry;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextArea;

public class ServerGUI {
	private JFrame frame;
	private Server server;//TODO: Require a server list for all the available servers, call server.initialNeighbour(List<Server>)
	private static JTextArea txtOutput;
	private JLabel lblServerPort;
	private JLabel lblServerIP;
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ServerGUI window = new ServerGUI();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public ServerGUI() {
		initialize();
		try {
			
			// Parse conf.txt
			Configuration c = Configuration.fromFile("conf.txt");
			
			// Get local ip
			String serverIp = (InetAddress.getLocalHost().getHostAddress()).toString();
			
			// Port should gradually be increased until a free port is gotten
			int serverPort = 3232;
			for (int i = serverPort; i < 4000; i++) {
				try {
					ServerSocket s = new ServerSocket(i);
					s.close();
		        } catch (IOException ex) {
		            continue;
		        }
				
				serverPort = i;
				break;
			}

			// Determine the id of this server
			Integer serverId = null;
			for (Entry<Integer, String> e : c.getAllServers().entrySet()) {
				String ip = e.getValue().split(":")[0];
				int port = Integer.valueOf(e.getValue().split(":")[1]);
				
				if (ip.equals(serverIp) && port == serverPort) {
					serverId = e.getKey();
					break;
				}
			}
			
			if (serverId == null) {
				log("Server: " + serverIp + ":" + serverPort + " not found in configurationfile");
				return;
			}
			
			// Init server
			server = new Server(serverIp, serverPort, serverId);
			//server.initialNeighbour(c);
			lblServerIP.setText(serverIp);
			lblServerPort.setText(String.valueOf(serverPort));
        } catch(UnknownHostException uhe){
        	log(uhe.toString());
        	return;
        } catch (RemoteException re) {
			log(re.toString());
			return;
        } catch (IOException ie) {
        	log(ie.toString());
			return;
		}
		log("Ready...");
	}
	
	public static void log(String text) {
		Date now = new Date(System.currentTimeMillis());
		txtOutput.setText(txtOutput.getText() + "\n" + now.toString() + " : " + text);
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 601, 346);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		
		JLabel lbl2 = new JLabel("Server IP:");
		lbl2.setBounds(12, 12, 70, 15);
		frame.getContentPane().add(lbl2);
		
		lblServerIP = new JLabel("obtaining...");
		lblServerIP.setBounds(123, 12, 180, 15);
		frame.getContentPane().add(lblServerIP);
		
		JLabel lbl1 = new JLabel("Server PORT:");
		lbl1.setBounds(12, 39, 103, 15);
		frame.getContentPane().add(lbl1);
		
		lblServerPort = new JLabel("obtaining...");
		lblServerPort.setBounds(123, 39, 180, 15);
		frame.getContentPane().add(lblServerPort);
		
		txtOutput = new JTextArea();
		txtOutput.setBounds(12, 66, 575, 239);
		frame.getContentPane().add(txtOutput);
	}
}
