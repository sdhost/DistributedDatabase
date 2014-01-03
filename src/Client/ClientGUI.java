package Client;

import java.awt.EventQueue;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.AbstractListModel;

public class ClientGUI {

	private JFrame frame;
	private Client client;
	private static String serverIp;
	private static int serverPort;
	private static JTextArea txtOutput;
	private JList list;
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		args = new String[]{"127.0.1.1", "3232"};
		if (args.length == 2) {
			serverIp = args[0];
			serverPort = Integer.valueOf(args[1]);
		} else {
			System.out.println("Please provide serverip as first argument, and serverport as second argument");
			return;
		}
		
		// Show GUI
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ClientGUI window = new ClientGUI();
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
	public ClientGUI() {
		initialize();
		client = new Client();
		
		// Connect to remote server
		try {
			if (!client.registerRMIServer(serverIp, serverPort)) {
				log("Server is not in ONLINE state");
			}
		} catch (RemoteException re) {
			log("Failed to connect to server\n" + re.toString());
			return;
		} catch (NotBoundException nbe) {
			log("Failed to connect to server\n" + nbe.toString());
			return;
		}
		
		log("Connected to server");
	}
	
	public static void log(String text) {
		txtOutput.setText(txtOutput.getText() + "\n" + text);
	}
	

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 586, 400);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		
		list = new JList(new DefaultListModel<String>());
		
		list.setBounds(12, 31, 134, 173);
		frame.getContentPane().add(list);
		
		JLabel lblLocalAccounts = new JLabel("Local Accounts");
		lblLocalAccounts.setBounds(12, 12, 134, 15);
		frame.getContentPane().add(lblLocalAccounts);
		
		JButton butCheckBalance = new JButton("Check balance");
		butCheckBalance.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				
				
				
			}
		});
		butCheckBalance.setBounds(158, 68, 155, 25);
		frame.getContentPane().add(butCheckBalance);
		
		JButton butDeposit = new JButton("Deposit");
		butDeposit.setBounds(158, 105, 155, 25);
		frame.getContentPane().add(butDeposit);
		
		JButton butWithdraw = new JButton("Withdraw");
		butWithdraw.setBounds(158, 142, 155, 25);
		frame.getContentPane().add(butWithdraw);
		
		JButton butTransfer = new JButton("Transfer");
		butTransfer.setBounds(158, 179, 155, 25);
		frame.getContentPane().add(butTransfer);
		
		txtOutput = new JTextArea();
		txtOutput.setEditable(false);
		txtOutput.setBounds(12, 216, 560, 143);
		frame.getContentPane().add(txtOutput);
		
		JButton butCreateAccount = new JButton("Create account");
		butCreateAccount.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				 try {
					 String val = JOptionPane.showInputDialog ("Initial balance");
					 if (val == null || val.length() == 0)
						 return;
					 Integer initBalance = Integer.valueOf(val);
					 String accountId = client.txnCreatingAccounts(initBalance);
					 if (accountId != null) {
						 
						 
						 
						 DefaultListModel dlm = (DefaultListModel)getList().getModel();
						 dlm.addElement(accountId);
					 }
					 
				 } catch (Exception ex) {
					 log(ex.toString());
					 return;
				 }
				 log("New account created");
			}
		});
		butCreateAccount.setBounds(158, 31, 155, 25);
		frame.getContentPane().add(butCreateAccount);
	}
	public JList getList() {
		return list;
	}
}
