package Server;

import java.awt.EventQueue;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.text.DateFormat;
import java.util.Date;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextArea;

public class ServerGUI {

	private JFrame frame;
	private Server server;
	private static JTextArea txtOutput;
	public static int serverPort = 3232;
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
		
		try{
			String serverIp = (InetAddress.getLocalHost()).toString();
			server = new Server(serverIp, serverPort);
			
			lblServerIP.setText(serverIp);
			lblServerPort.setText(String.valueOf(serverPort));
        } catch(UnknownHostException uhe){
        	log(uhe.toString());
        	return;
        } catch (RemoteException re) {
			log(re.toString());
			return;
		}
		
		log("Ready...");
	}
	
	public static void log(String text) {
		Date now = new Date(System.currentTimeMillis());
		txtOutput.setText(txtOutput.getText() + "\n" + now.toString() + ":" + text);
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
