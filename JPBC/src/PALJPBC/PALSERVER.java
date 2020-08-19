package PALJPBC;

import java.net.*;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.util.*;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.border.TitledBorder;

import it.unisa.dia.gas.jpbc.*;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;

public class PALSERVER {
/*---------------------initial setup------------------*/  
	static Pairing pairing;  
	/* Return Zr, G1 and GT */
	static Field Zp;
	static Field G1;
	static Field GT;  
	//Public parameters;
	static Element g, h, gt;
	byte[] g_bytes ,h_bytes, gt_bytes;
	static String g_str, h_str, gt_str;
	/* Server info */
	static Element IDs;
	static Element b,β0, IDS_PrivateKey_US, IDS_PrivateKey_US1 , IDS_PrivateKey_US2, IDS_PrivateKey_XS, IDS_PrivateKey_XS1, IDS_PrivateKey_XS2, IDS_PublicKey_PS, IDS_PublicKey_TS; 
	Element y , Y1, Y2, ns, SKSJ1, SKSJ2, SKSJ3, SKSJ4;
	String IDs_str, XORSJ_str, ns_str,Y1_str, Y2_str,IDS_PublicKey_PS_str,IDS_PublicKey_TS_str;
	byte[] IDs_bytes, y_bytes, Y1_bytes, Y2_bytes,IDS_PublicKey_PS_bytes, IDS_PublicKey_TS_bytes, XORSJBytes, ns_bytes;
	/* Client info */
	static Element a, α0,IDC_PrivateKey_UC, IDC_PrivateKey_UC1, IDC_PrivateKey_UC2,IDC_PublicKey_PC;
	static Element x, X,nc, SKCI1, SKCI2, SKCI3, SKCI4;
    static String IDc, X_str, XORCI_str, nc_str, IDC_PublicKey_PC_str;
	byte[] IDc_bytes, X_bytes, IDC_PublicKey_PC_bytes, XORCIBytes, nc_bytes;
	
	
/*---------------------initial setup end--------------*/  	
	/* ServerSocket */
	private static ServerSocket server;
	private Thread Serverthread;
	private boolean isStart = false;
	
	
	/*---------- Main Frame ---------*/
	private JFrame frame;
	private JTextArea SystemParameterArea, contentArea;
	private JTextField txt_ServerPort;
	private JButton btn_StartServer;
	private JButton btn_EndServer;

	private GridLayout northPanelGrid;
	private JPanel northPanel;
	private JScrollPane upPanel;
	private JScrollPane downPanel;
	private JSplitPane centerSplit;
	/*---------- End Line -----------*/
	
	public PALSERVER()throws IOException {
		
		pairing = PairingFactory.getPairing("a.properties");  
		/* Return Zr, G1 and GT */
		Zp = pairing.getZr();
		G1 = pairing.getG1();
		GT = pairing.getGT();  
		//Public parameters;
		g = G1.newRandomElement().getImmutable();
		h = Zp.newRandomElement().getImmutable();
		gt =pairing.pairing(g, g).getImmutable();
		
		g_bytes = g.toBytes();
		g_str = Base64.getEncoder().encodeToString(g_bytes);
		h_bytes = h.toBytes();
		h_str = Base64.getEncoder().encodeToString(h_bytes);
		gt_bytes = gt.toBytes();
		gt_str = Base64.getEncoder().encodeToString(gt_bytes);
				
		/* northPanel */
		JLabel label_Server = new JLabel("Server");
		label_Server.setHorizontalAlignment(JLabel.CENTER);
		
		JLabel label_Serverport = new JLabel("Serve1Port :");
		label_Serverport.setHorizontalAlignment(JLabel.CENTER);
		txt_ServerPort = new JTextField("8082");
		txt_ServerPort.setEnabled(true);
		btn_StartServer = new JButton("啟動");
		btn_EndServer = new JButton("停止");
		btn_EndServer.setEnabled(false);
		northPanelGrid = new GridLayout(1,5);
		northPanelGrid.setVgap(5);
		northPanelGrid.setHgap(5);
		northPanel = new JPanel();
		northPanel.setLayout(northPanelGrid);
		northPanel.add(label_Server);
		northPanel.add(label_Serverport);
		northPanel.add(txt_ServerPort);
		northPanel.add(btn_StartServer);
		northPanel.add(btn_EndServer);
		northPanel.setBorder(new TitledBorder("配置資訊"));
	
		/* upPanel */
		SystemParameterArea = new JTextArea();
		SystemParameterArea.setEditable(false);
		Font font = new Font("Segoe Script", Font.BOLD, 16);
		SystemParameterArea.setFont(font);
		SystemParameterArea.setForeground(Color.blue);
		upPanel = new JScrollPane(SystemParameterArea);
		upPanel.setBorder(new TitledBorder("SERVER金鑰配置"));
		
		/* downPanel */
		contentArea = new JTextArea();
		contentArea.setEditable(false);
		font = new Font("Segoe Script", Font.BOLD, 20);
		contentArea.setFont(font);
		contentArea.setForeground(Color.blue);
		downPanel = new JScrollPane(contentArea);
		downPanel.setBorder(new TitledBorder("連線訊息顯示區"));
		
		/* Split up and down Panels in center */
		centerSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, upPanel, downPanel);
		centerSplit.setDividerLocation(315);
		
		/* JFrame */
		frame = new JFrame("SEVER CENTER");
		frame.setLayout(new BorderLayout());
		frame.add(northPanel, "North");
		frame.add(centerSplit, "Center");
		frame.setSize(500,600);
		int screen_width = Toolkit.getDefaultToolkit().getScreenSize().width;
		int screen_height = Toolkit.getDefaultToolkit().getScreenSize().height;
		frame.setLocation((screen_width - frame.getWidth()) / 2,(screen_height - frame.getHeight()) / 2);
		frame.setVisible(true);
		SetupServerPP();
		SetupServer();
		/* 關閉視窗時事件 */
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				if (isStart) {
					closeServer();// 關閉伺服器
				}
				System.exit(0);// 退出程式
			}
		});	
		/* 單擊"啟動startServer伺服器"按鈕時事件   */
		btn_StartServer.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
				SetupServer();
				}catch (Exception exc) {
					JOptionPane.showMessageDialog(frame, exc.getMessage(), "SERVER開啟失敗", JOptionPane.ERROR_MESSAGE);
				}
		}});
			// 單擊"btn_EndServer"按鈕時事件
		btn_EndServer.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!isStart) {
					JOptionPane.showMessageDialog(frame, "伺服器還未啟動,無需停止!", "錯誤", JOptionPane.ERROR_MESSAGE);
					return;
				}
				try {
					closeServer();
					btn_StartServer.setEnabled(true);
					txt_ServerPort.setEnabled(true);
					btn_EndServer.setEnabled(false);
					contentArea.setText("");
					JOptionPane.showMessageDialog(frame, "伺服器成功停止!");
				} catch (Exception exc) {
					JOptionPane.showMessageDialog(frame, "停止伺服器發生異常!", "錯誤", JOptionPane.ERROR_MESSAGE);
				}
			}
		});}	
//設定Public Parameters 
	public void SetupServerPP() throws java.net.BindException {
			/* Server Parameters*/
			b = Zp.newRandomElement().getImmutable();
			β0 =Zp.newRandomElement().getImmutable(); 
			IDS_PrivateKey_US = h.powZn(b).getImmutable();
			IDS_PrivateKey_US1 = h.powZn(β0).getImmutable();
			IDS_PrivateKey_US2 = IDS_PrivateKey_US.div(h.powZn(β0)).getImmutable();
			IDS_PrivateKey_XS = g.powZn(b).getImmutable();
			IDS_PrivateKey_XS1 = g.powZn(β0).getImmutable();
			IDS_PrivateKey_XS2 = IDS_PrivateKey_XS.div( g.powZn(β0)).getImmutable();
			IDS_PublicKey_PS =gt.powZn(IDS_PrivateKey_US).getImmutable();
			IDS_PublicKey_TS =gt.powZn(b).getImmutable();
			IDS_PublicKey_PS_bytes = IDS_PublicKey_PS.toBytes();
			IDS_PublicKey_PS_str = Base64.getEncoder().encodeToString(IDS_PublicKey_PS_bytes);
			IDS_PublicKey_TS_bytes = IDS_PublicKey_TS.toBytes();
			IDS_PublicKey_TS_str = Base64.getEncoder().encodeToString(IDS_PublicKey_TS_bytes);
			SystemParameterArea.append("SERVER\nFirst private key \nIDS_PrivateKey_US : "+IDS_PrivateKey_US+" \nSecond private key \nIDS_PrivateKey_XS : "+IDS_PrivateKey_XS+ "\nFirst public key \nIDS_PublicKey_PS : "+IDS_PublicKey_PS+" \nSecond Public key\n IDS_PublicKey_TS : "+IDS_PublicKey_TS+"\n");
			SystemParameterArea.append("\nSERVER\n First private key part1 \nIDS_PrivateKey_US1 : "+IDS_PrivateKey_US1+" \n first private key part2 \nIDS_PrivateKey_US2 : "+IDS_PrivateKey_US2+ "\n Second Private key part1 \nIDS_PrivateKey_XS1 : "+IDS_PrivateKey_XS1+" \n Second Private key part2 \n IDS_PrivateKey_XS2 : "+IDS_PrivateKey_XS2+"\n");
			
			JOptionPane.showMessageDialog(frame, "SERVER建立參數成功!");
	}
	//設定SERVER金鑰並啟動
	public void SetupServer() throws java.net.BindException {
		/*開啟連線*/
		int port;
		try {
			try {
				port = Integer.parseInt(txt_ServerPort.getText());
			} catch (Exception e1) {
				throw new Exception("埠號為正整數!");
			}
			if (port <= 0) {
				throw new Exception("埠號 為正整數!");
			} else {					
				/* Server setup */  
			    try{
				    server = new ServerSocket(port);
		   	    	Runnable runn = new Server_Connect(server);
			  	    Serverthread = new Thread(runn);
			  		Serverthread.start();
			  		isStart = true;
				    System.out.println("MultiSocketServer Port"+port+" Initialized");
				    contentArea.append("--- Server has initialized---\r\n");
				    btn_StartServer.setEnabled(false);
					btn_EndServer.setEnabled(true);		
					txt_ServerPort.setEnabled(false);
				    JOptionPane.showMessageDialog(frame, "伺服器成功啟動!");
			    } catch (BindException e){
			    	isStart = false;
			    	System.out.println("Startup failed");
			    	btn_StartServer.setEnabled(true);
					btn_EndServer.setEnabled(false);		
					txt_ServerPort.setEnabled(true);
					JOptionPane.showMessageDialog(frame, "伺服器未被正確啟動，請檢查PORT是否重複!", "錯誤", JOptionPane.ERROR_MESSAGE);
			    	//throw new BindException("--- Server failed to activate---\r\n");
			    }catch (Exception e1) {
					e1.printStackTrace();
					isStart = false;
					throw new BindException("啟動伺服器異常!");
				};
			}
		} catch (Exception exc) {
			JOptionPane.showMessageDialog(frame, exc.getMessage(), "錯誤", JOptionPane.ERROR_MESSAGE);
		  }}
	
	public class Server_Connect implements Runnable{
		/*-----------------Start loop of get connected User ---------*/
		ServerSocket server;
		Server_Connect(ServerSocket server){
			this.server = server;
		}
		public void run(){
			int ClientId = 0;
			try{
				while (true) {
					/* connected Client to thread -> run */
					Socket connectedClient = server.accept();
					Runnable r=new Server_Client_Conncetion(connectedClient, ++ClientId);
					Thread t=new Thread(r);
					t.start();        
				} 
			} catch (IOException e){
		    };
		}
		/*------------------------- End Line ------------------------*/
	}
	public class Server_Client_Conncetion implements Runnable{
		private Socket connectedClient;
		private int ID;
		Server_Client_Conncetion(Socket s, int i){
			this.connectedClient = s;
			this.ID = i;   
		}     
		public void run(){
			System.out.println("\nNew client thread start\nIP: "+connectedClient+" ID: "+ID+"\n");
			try { 
				/* input and output with Client */
				DataInputStream datainputstream  = new DataInputStream(connectedClient.getInputStream());//bytes
		    	InputStreamReader streamReader = new InputStreamReader(datainputstream,"utf8");//char array
		    	BufferedReader msgFromClient = new BufferedReader(streamReader);//才能使用ReadLine
		    	//BufferedReader msgFromClient = new BufferedReader(new InputStreamReader(new DataInputStream(connectedClient.getInputStream()),"utf8"));
		    	DataOutputStream msgtoClient = new DataOutputStream(connectedClient.getOutputStream());
		    	String msg ="StoC1 g_str="+g_str+" h_str="+h_str+" gt_str="+gt_str;
		    	System.out.println("prepare public parameters to Client = \n"+msg+"\n-----\n");
		    	String client_msg=new String();				
				int StartChatRoom = 0;
				
				/*--------------End line----------------*/ 
				while (connectedClient.isConnected()){
		    		//已經修改位置，連接後才傳送
		    		/* write msg to client */
		        	try{
		              msgtoClient.writeUTF(msg);
		          	System.out.println("the initial msg(parameter) to Client = \n"+msg+"\n-----\n");
		            }catch(IOException e){
		              break;
		            	//System.out.println(e);    
		            }
		        	break;}
		        	 
				while (connectedClient.isConnected()){
					try{
		        		 client_msg =new String();
		         		client_msg = datainputstream.readUTF(); 
		         		}catch(IOException e){
		       	      System.out.println(e);
		       	    }
		/*--------------- The First sent msg from client -------------------------*/    	     
		    		if(client_msg.startsWith("CtoS1")){
		    			System.out.println("The first msg from Client = \n"+client_msg+"\n-----");
		    			/* Get IDc string*/
		    			IDc = client_msg.substring(client_msg.indexOf(" ID_input=")+10, client_msg.indexOf(" X_str="));
		    			/* Get X string to element */
		    			X_str = client_msg.substring(client_msg.indexOf(" X_str=")+7, client_msg.indexOf(" IDC_PublicKey_PC_str="));
		    			X_bytes = Base64.getDecoder().decode(X_str);
		    			X = G1.newElementFromBytes(X_bytes).getImmutable(); 
				    	/* Get QTc string to element */
		    			IDC_PublicKey_PC_str = client_msg.substring(client_msg.indexOf(" IDC_PublicKey_PC_str=")+22, client_msg.indexOf(" nc="));
		    			IDC_PublicKey_PC_bytes = Base64.getDecoder().decode(IDC_PublicKey_PC_str);
		    			IDC_PublicKey_PC = GT.newElementFromBytes(IDC_PublicKey_PC_bytes).getImmutable(); 
		    			/* Get random nc string to element */
		    			nc_str = client_msg.substring(client_msg.indexOf(" nc=")+4);
		    			nc_bytes = Base64.getDecoder().decode(nc_str);
		    			nc = Zp.newElementFromBytes(nc_bytes).getImmutable(); 
		    			System.out.println("IDc = "+IDc+"\nX = "+X+"\nIDC_PublicKey_PC = "+IDC_PublicKey_PC+"\nnc = "+nc+"\n-----\n");
				    /*----------------------------Server process------------------------------*/ 
		    			 /*SERVER processing*/
		    			y = Zp.newRandomElement().getImmutable();
		     		    Y1 = g.powZn(y).getImmutable();
		     		    Y2 = pairing.pairing(g, Y1).getImmutable(); 
		     		    β0 =Zp.newRandomElement().getImmutable(); 
						IDS_PrivateKey_US1 = IDS_PrivateKey_US1.mul(h.powZn(β0)).getImmutable();
						IDS_PrivateKey_US2 = IDS_PrivateKey_US2.div(h.powZn(β0)).getImmutable();
						IDS_PrivateKey_XS1 = IDS_PrivateKey_XS1.mul(g.powZn(β0)).getImmutable();
						IDS_PrivateKey_XS2 = IDS_PrivateKey_XS2.div(g.powZn(β0)).getImmutable();
						JOptionPane.showMessageDialog(frame, "更新金鑰成功!");
						SystemParameterArea.append("SERVER\nNew First private key part1 \nIDS_PrivateKey_US1 : "+IDS_PrivateKey_US1+" \nNew first private key part2 \nIDS_PrivateKey_US2 : "+IDS_PrivateKey_US2+ "\nNew Second private key part1 \nIDS_PrivateKey_XS1 : "+IDS_PrivateKey_XS1+" \nNew Second Private key part2 \n IDS_PrivateKey_XS2 : "+IDS_PrivateKey_XS2+"\n");
						
		    		    SKSJ1 =IDC_PublicKey_PC.powZn(y).getImmutable();
		    		    System.out.println("SKSJ1 = "+SKSJ1);
		    		    SKSJ2 =pairing.pairing(X, IDS_PrivateKey_XS1).mul(pairing.pairing(X, IDS_PrivateKey_XS2)).getImmutable();
		    		    System.out.println("SKSJ2 = "+SKSJ2);
		    		    SKSJ3 =X.powZn(y).getImmutable();
		    		    System.out.println("SKSJ3 = "+SKSJ3);
		    		    SKSJ4 =IDC_PublicKey_PC.powZn(IDS_PrivateKey_US1).powZn(IDS_PrivateKey_US2).getImmutable();
		    		    System.out.println("SKSJ4 = "+SKSJ4);
		    		    //轉BYTE後轉STRING(base64)
		    		    byte[] SKSJ1Bytes = SKSJ1.toBytes();
		    		    byte[] SKSJ2Bytes = SKSJ2.toBytes();
		    		    byte[] SKSJ3Bytes = SKSJ3.toBytes();
		    		    byte[] SKSJ4Bytes = SKSJ4.toBytes();
		    		  	
		    		    XORSJBytes = PALTool.xor4(SKSJ1Bytes,SKSJ2Bytes,SKSJ3Bytes,SKSJ4Bytes);
		    		  	System.out.println("XORSJBytes= "+XORSJBytes);
		    		  	XORSJ_str = Base64.getEncoder().encodeToString(XORSJBytes);
		    		  	System.out.println("XORSJ_str= "+XORSJ_str);
		    		  	/* Choose a nonce ns */
		    		  	Element ns = Zp.newRandomElement().getImmutable();    			
		    			/* ToSring */
		    	    	Y1_bytes = Y1.toBytes();
		    			Y1_str = Base64.getEncoder().encodeToString(Y1_bytes);
		    			Y2_bytes = Y2.toBytes();
		    			Y2_str = Base64.getEncoder().encodeToString(Y2_bytes);
		    			ns_bytes = ns.toBytes();
		    			ns_str = Base64.getEncoder().encodeToString(ns_bytes);
		    			System.out.println("ns_str = \n"+ns_str+"\n");
		    				/*--hash--*/
		    			String XORSJ_concat_ns_str = XORSJ_str.concat(ns_str);
		    			System.out.println("XORSJ_concat_ns_str = \n"+XORSJ_concat_ns_str+"\n");
		    			String hash_auths_str=PALTool.hash(XORSJ_concat_ns_str, "SHA1");
		    			System.out.println("hash_auths_str：" + hash_auths_str + "\n\n");
		    			
		    			/*public key*/
		    			client_msg="";
		    			/* write msg to client */
		    			msg = "StoC2 Y1="+Y1_str+" Y2="+Y2_str+" hash_auths_str="+hash_auths_str+" ns="+ns_str+" IDS_PublicKey_PS_str="+IDS_PublicKey_PS_str+" IDS_PublicKey_TS_str="+IDS_PublicKey_TS_str;
		    			System.out.println("Return the msg to Client = \n"+msg+"\n-----\n");
		    			try{
		    	              msgtoClient.writeUTF(msg);
		    	        }catch(IOException e){
		    	          System.out.println(e);    
		    	        }
		/*----------------------- The Second sent msg from client ------------------------------*/    	        
		    		} else if(client_msg.startsWith("CtoS2")) {
		        			System.out.println("Client to Server msg doesn't equal CtoS1 ");

		        			System.out.println("The second msg from Client = \n"+client_msg+"\n-----");
		        	
		    			/* Get Authc string to Byte */
		    			String  hash_authc_str = client_msg.substring(client_msg.indexOf("CtoS2 hash_authc=")+17);
		    			System.out.println("hash_authc_str = "+hash_authc_str+"\n-----");

		/*----------------------------------Server process---------------------------------------*/ 
		    			/*/用 hash後會拿掉Utils.xor2***/
		    			/*Checking authentication message */
		    			System.out.println("Checking authentication message hash_authc...");
		    			//hash
		  	 		  	String XORSJ_concat_nc_str = XORSJ_str.concat(nc_str);
		  	 		  	System.out.println("nc_str = \n"+nc_str+"\n");
		  	 		  	System.out.println("strconcat = \n"+XORSJ_concat_nc_str+"\n");
		  	 		  	String check_hash_auths_str=PALTool.hash(XORSJ_concat_nc_str, "SHA1");
		    			/*  Test Print CheckAuths */
		    			System.out.println("The Checking message check_hash_auths_str  is : "+check_hash_auths_str);
		    			if(hash_authc_str.equals(check_hash_auths_str)) {
		    				System.out.println("Authentication is seccess, the session key is valid.");
		    				System.out.println(" MAKE complete!");
		    				contentArea.append("--- Server has MAKE SSK---\r\n");
		    			} else {
		    				System.out.println("Authentication is failed, session stop!");
		    				break;
		    			}
		/*--------------------------------------End line------------------------------------------*/ 
		    	} else if (client_msg.startsWith("StratChatRoom")){
		    			X_str = client_msg.substring(client_msg.indexOf(" X_str=")+7);
						StartChatRoom++;
		    			break;
		    		}	    	
		    	};/* while end */   
		    	/*--- the chat room, while loops of get message(thread), and send-message function ---*/
		    	if (StartChatRoom == 1) {
		    		contentArea.append("Start a ChatRoom for Client: ");
		    		contentArea.setCaretPosition(contentArea.getDocument().getLength());
    				System.out.println("Start a ChatRoom for Client \n"+IDc);
    				ChatFrame chatClient = new ChatFrame(connectedClient, IDc);
		    		/* Start thread of get Client's chat message */
		    		Thread thread = new Thread(new GetChatMessageFromClient(connectedClient, chatClient));  //賦予執行緒工作   		
		            thread.start();
		    	}
		    	/*--------------------------End line------------------------------*/
		    } catch (IOException e){
		    	System.out.println("Connect to Client failed");
		    	System.out.println(e);
		    };
		}
	}
    /* 聊天室子視窗 */
	class ChatFrame extends JFrame { 
		private Socket connectedClientChat;
		private String chat_message = "";
		private JTextArea ChatArea;
		private JTextField chat_message_txt;
	    public ChatFrame(Socket ConnClient ,String Client_ID) { 
	    	super("與 "+Client_ID+" 的聊天室"); 
			setSize(400,400); 
			/* 關閉視窗時事件 */
			addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent e) {
					try {
						connectedClientChat.close();
						ChatFrame.this.dispose();
					}catch(IOException e1){
					}
				}
			});	
			ChatArea = new JTextArea("");
			ChatArea.setEditable(false);
			ChatArea.setForeground(Color.blue);
			JScrollPane centerChatPanel = new JScrollPane(ChatArea);
			centerChatPanel.setBorder(BorderFactory.createEmptyBorder(1, 10, 5, 10));
			chat_message_txt = new JTextField();
			JButton btn_send = new JButton("傳送");
			JPanel southChatPanel = new JPanel(new BorderLayout());
			southChatPanel.setBorder(new TitledBorder("寫訊息"));
			southChatPanel.add(chat_message_txt, "Center");
			southChatPanel.add(btn_send, "East");
			add(centerChatPanel, "Center");
			add(southChatPanel, "South");
			int screen_width = Toolkit.getDefaultToolkit().getScreenSize().width;
			int screen_height = Toolkit.getDefaultToolkit().getScreenSize().height;
			setLocation((screen_width - frame.getWidth()) / 2,(screen_height - frame.getHeight()) / 2);
			setVisible(true);
			this.connectedClientChat = ConnClient;
			btn_send.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					chat_message = chat_message_txt.getText();
					if(chat_message != "") {
						try {    	
	                		String CipherText = "";
							try {
	            				CipherText = PALTool.Encrypt1(chat_message, XORSJ_str);
							} catch (Exception e1) {
							}
							DataOutputStream ChatMessagetoClient = new DataOutputStream(connectedClientChat.getOutputStream());
	                		ChatMessagetoClient.writeBytes(CipherText+"\r\n");
	                		/**/
	                		ChatArea.append("加密前為 : "+chat_message+"\n");
	                		ChatArea.append("加密後為 : "+CipherText+"\n");
							ChatArea.setCaretPosition(ChatArea.getDocument().getLength());
						}catch(IOException e1) {
							ChatArea.append("Warning!傳送訊息失敗! ");
							ChatArea.setCaretPosition(ChatArea.getDocument().getLength());
						}
	    				chat_message_txt.setText("");
	    				chat_message = "";
					}}});}
	}	
	public class GetChatMessageFromClient implements Runnable {
		Socket connectedClientChat;
		ChatFrame chatClient;
		GetChatMessageFromClient(Socket ConnClient, ChatFrame chatClient){
			this.connectedClientChat = ConnClient;
			this.chatClient = chatClient;
		}
		@Override
		public void run() {
			/* get Client's chat message and println */
			String client_chat_msg=null;
			try {  
				BufferedReader ChatMessageFromClient = new BufferedReader(new InputStreamReader(new DataInputStream(connectedClientChat.getInputStream())));  		  
				while(connectedClientChat.isConnected()){
					client_chat_msg = ChatMessageFromClient.readLine();
					/* 解密 */
					String PlainText = "";
					try {
						PlainText = PALTool.Decrypt1(client_chat_msg, XORSJ_str);
					} catch (Exception e) {
					}
					if(PlainText != null) {
						chatClient.ChatArea.append("解密前為 : "+client_chat_msg+"\n");
						chatClient.ChatArea.append("解密後為 : "+PlainText+"\n");
						chatClient.ChatArea.setCaretPosition(chatClient.ChatArea.getDocument().getLength());
					}
					if (PlainText.startsWith("EndChat")){
						connectedClientChat.close();
						chatClient.dispose();
						break;
					}
				}
			}catch(IOException e) {
			}	    
	    }
	};
  	//關閉伺服器
	@SuppressWarnings("deprecation")
	public void closeServer() {		
		try {
			if (Serverthread != null)
				Serverthread.stop();// 停止伺服器執行緒
			if (server != null) {
				server.close();// 關閉伺服器端連線
			}
			isStart = false;
		} catch (IOException e) {
			e.printStackTrace();
			isStart = true;
		}
	}
  public static void main(String[] args) throws IOException {	
	  new PALSERVER();
  }
}
  