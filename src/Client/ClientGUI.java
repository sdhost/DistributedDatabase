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
import javax.swing.JScrollPane;

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
		args = new String[]{"10.94.1.198", "3232"};
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
		
		JLabel lblLocalAccounts = new JLabel("Local Accounts");
		lblLocalAccounts.setBounds(12, 12, 134, 15);
		frame.getContentPane().add(lblLocalAccounts);
		
		JButton butCheckBalance = new JButton("Check balance");
		butCheckBalance.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (getList().getSelectedValue() == null)
					return;
				
				String selectedAccountId = getList().getSelectedValue().toString();
				
				try {
					
					String balance = client.txnCheckingBalance(selectedAccountId);
					if (balance == null) {
						ClientGUI.log("Couldn't check balance of account");
						return;
					}
					
					ClientGUI.log("AccountId: " + selectedAccountId + " has balance " + balance);	
				} catch (Exception ex) {
					 log(ex.toString());
					 return;
				}
			}
		});
		butCheckBalance.setBounds(158, 68, 155, 25);
		frame.getContentPane().add(butCheckBalance);
		
		JButton butDeposit = new JButton("Deposit");
		butDeposit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (getList().getSelectedValue() == null)
					return;
				
				String selectedAccountId = getList().getSelectedValue().toString();
				
				String val = JOptionPane.showInputDialog ("Deposit Money");
				if (val == null || val.length() == 0)
					return;
				Integer depositVal = Integer.valueOf(val);
				
				try {
					String balance = client.txnDeposit(selectedAccountId, depositVal);
					if (balance == null) {
						ClientGUI.log("Couldn't deposit into account");
						return;
					}
					
					ClientGUI.log("AccountId: " + selectedAccountId + " has balance " + balance);
				} catch (Exception ex) {
					 log(ex.toString());
					 return;
				 }
				
				
			}
		});
		butDeposit.setBounds(158, 105, 155, 25);
		frame.getContentPane().add(butDeposit);
		
		JButton butWithdraw = new JButton("Withdraw");
		butWithdraw.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				if (getList().getSelectedValue() == null)
					return;
				
				String selectedAccountId = getList().getSelectedValue().toString();
				
				String val = JOptionPane.showInputDialog ("Withdraw Money");
				if (val == null || val.length() == 0)
					return;
				Integer depositVal = Integer.valueOf(val);
				
				try {
					String balance = client.txnWithdraw(selectedAccountId, depositVal);
					if (balance == null) {
						ClientGUI.log("Couldn't withdraw from account");
						return;
					}
					
					ClientGUI.log("AccountId: " + selectedAccountId + " has balance " + balance);
				} catch (Exception ex) {
					 log(ex.toString());
					 return;
				 }
			}
		});
		butWithdraw.setBounds(158, 142, 155, 25);
		frame.getContentPane().add(butWithdraw);
		
		JButton butTransfer = new JButton("Transfer");
		butTransfer.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (getList().getSelectedValue() == null)
					return;
				
				String selectedAccountId = getList().getSelectedValue().toString();
				
				String val = JOptionPane.showInputDialog ("Transfer to ");
				if (val == null || val.length() == 0)
					return;
				String toAccountId = val;
				
				val = JOptionPane.showInputDialog ("Amount to transfer ");
				if (val == null || val.length() == 0)
					return;
				
				Integer transferVal = Integer.valueOf(val);
				
				try {
					String balance = client.txnTransfer(selectedAccountId, toAccountId, transferVal);
					if (balance == null) {
						ClientGUI.log("Couldn't transfer from account");
						return;
					}
					
					
					ClientGUI.log("AccountId: " + selectedAccountId + " has balance " + balance);
				} catch (Exception ex) {
					 log(ex.toString());
					 return;
				 }
				
			}
		});
		butTransfer.setBounds(158, 179, 155, 25);
		frame.getContentPane().add(butTransfer);
		
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
						 log("New account created");
					 } else {
						 log("New account couldn't be created");
					 }
					 
				 } catch (Exception ex) {
					 log(ex.toString());
					 return;
				 }
				 
			}
		});
		butCreateAccount.setBounds(158, 31, 155, 25);
		frame.getContentPane().add(butCreateAccount);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(12, 216, 560, 143);
		frame.getContentPane().add(scrollPane);
		
		txtOutput = new JTextArea();
		scrollPane.setViewportView(txtOutput);
		txtOutput.setEditable(false);
		
		list = new JList(new DefaultListModel<String>());
		
		list.setBounds(12, 31, 134, 173);
		frame.getContentPane().add(list);
	}
	public JList getList() {
		return list;
	}
}