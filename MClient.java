package com.LRAKE_IoT_PAL.mobile_client;

import java.net.*;
import java.io.*;
import java.util.*;
import it.unisa.dia.gas.jpbc.*;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;

public class  MClient {

    /*Socket to perform  client connection to server and values*/
    Socket connectedServer;
    BufferedReader msgFromServer;
    DataOutputStream msgToServer;
    DataInputStream dataFromServer;
    String server_msg;
    int port=18980;
    /*---------------------initial setup------------------*/
    static Pairing pairing = PairingFactory.getPairing("assets/a.properties");

    /* Return Zr */
    static Field Zp = pairing.getZr();

    /* Return G1 */
    static Field G1 = pairing.getG1();

    /* Return GT */
    static Field GT = pairing.getGT();

    /* Elements */
    static String X_str,XORCI_str="", msg="",PublicParameters="";
    static Element g, h,gt,a ,α0 ,IDC_PrivateKey_UC, IDC_PrivateKey_UC1,IDC_PrivateKey_UC2,IDC_PublicKey_PC, x,X, nc;
    static String IDC_PublicKey_PC_str,nc_str,g_str,h_str,gt_str;

     /*---------------------initial setup end--------------*/

     public  String MClient(String IP_input, String ID_input,int phasenumber){
        /* connect to Server */
        //外部已測試IP_input="0.tcp.ngrok.io";
        try{
            connectedServer = new Socket(IP_input,port);
            // connectedServer = new Socket("0.tcp.ngrok.io",19443);
            String connectedServertoString=connectedServer.toString();
            System.out.println("\nconnectedServertoString"+connectedServertoString);
            dataFromServer=new DataInputStream(connectedServer.getInputStream());
            //msgFromServer=new BufferedReader(new InputStreamReader(new DataInputStream(connectedServer.getInputStream()),"utf8"));
            msgToServer=new DataOutputStream(connectedServer.getOutputStream());
            System.out.println("Connect to Server successful!");
            //ConstructSessionKey();
        } catch (IOException e){
            e.printStackTrace();
            System.out.println("Connect to Server failed.");
            System.exit(0);
        };
        if(phasenumber==0){//拿g, gt, h公共參數
            TogetPP();
            return PublicParameters;
        }else {
            ConstructSessionKey(ID_input);
            String txt="XORCI_str="+XORCI_str+"IDC_PrivateKey_UC1="+IDC_PrivateKey_UC1+"IDC_PrivateKey_UC2="+IDC_PrivateKey_UC2;
            return txt;
        }
     }

    public void   TogetPP() {
        /*--------------- The Initial sent msg from server -----------------------*/
        try {
            System.out.println("TogetPP phase!");
            server_msg = dataFromServer.readUTF();
        } catch (IOException e) {
            System.out.println(e);
        }
        if (server_msg.startsWith("StoC1")) {
            System.out.println("\nThe Initial msg from Server = \n" + server_msg + "\n-----");

            /* Get g string to element */
            g_str = server_msg.substring(server_msg.indexOf(" g_str=") + 7, server_msg.indexOf(" h_str="));
            byte[] g_bytes = Base64.getDecoder().decode(g_str);
            g = G1.newElementFromBytes(g_bytes).getImmutable();

            /* Get h string to element */
             h_str = server_msg.substring(server_msg.indexOf(" h_str=") + 7, server_msg.indexOf(" gt_str="));
            byte[] h_bytes = Base64.getDecoder().decode(h_str);
            h = Zp.newElementFromBytes(h_bytes).getImmutable();

            /* Get gt string to element */
            gt_str = server_msg.substring(server_msg.indexOf(" gt_str=") + 8);
            byte[] gt_bytes = Base64.getDecoder().decode(gt_str);
            gt = GT.newElementFromBytes(gt_bytes).getImmutable();
            System.out.println("g = " + g + "\nh = " + h + "\ngt = " + gt + "\n\n");
        }
        /*-----------------------------------End line--------------------------------------*/
         /* Compute Elements */
        /*Get Random Variable in Zp*/
         a = Zp.newRandomElement().getImmutable();
        /* Client KEYS with IDC*/
         α0 = Zp.newRandomElement().getImmutable();
         IDC_PrivateKey_UC = h.powZn(a).getImmutable();
         IDC_PrivateKey_UC1 = h.powZn(α0).getImmutable();
         IDC_PrivateKey_UC2 =IDC_PrivateKey_UC.div(IDC_PrivateKey_UC1).getImmutable();
         IDC_PublicKey_PC = gt.powZn(IDC_PrivateKey_UC).getImmutable();

        /*Test*/
        System.out.println("Client process : ");
        System.out.println("a is : " + a);
        System.out.println("α0 is : " + α0);
        System.out.println("IDC_PrivateKey_UC is : " + IDC_PrivateKey_UC);
        System.out.println("IDC_PrivateKey_UC1 is : " + IDC_PrivateKey_UC1);
        System.out.println("IDC_PrivateKey_UC2 is : " + IDC_PrivateKey_UC2);
        System.out.println("IDC_PublicKey_PC is : " + IDC_PublicKey_PC);

        /* ToSring */
        byte[] IDC_PublicKey_PC_bytes = IDC_PublicKey_PC.toBytes();
        IDC_PublicKey_PC_str = Base64.getEncoder().encodeToString(IDC_PublicKey_PC_bytes);
        PublicParameters="g_str = " + g_str + "\nh_str = " + h_str + "\ngt_str = " + gt_str + "\nIDC_PublicKey_PC_str = " + IDC_PublicKey_PC_str + "\nIDC_PrivateKey_UC1 = " + IDC_PrivateKey_UC1+ "\nIDC_PrivateKey_UC2 = " + IDC_PrivateKey_UC2;
    }
        /*-------------------------------------End line-----------------------------------------*/
        public void   ConstructSessionKey(String ID_input){
            try {
                System.out.println("ConstructSessionKey phase!先把PP讀掉");
                server_msg = dataFromServer.readUTF();
            } catch (IOException e) {
                System.out.println(e);}
            /*client setups  short-term secret key and refreshes long-term secret key */
            /*client setups  short-term secret key */
            x = Zp.newRandomElement().getImmutable();
            X = g.powZn(x).getImmutable();
            byte[] X_bytes = X.toBytes();
            X_str = Base64.getEncoder().encodeToString(X_bytes);
            nc = Zp.newRandomElement().getImmutable();
            byte[] nc_bytes = nc.toBytes();
            nc_str = Base64.getEncoder().encodeToString(nc_bytes);
            System.out.println("x is : " + x);
            System.out.println("X is : " + X);
            System.out.println("nc is : " + nc);
            /*client refreshes long-term secret key */
            α0 = Zp.newRandomElement().getImmutable();
            IDC_PrivateKey_UC1=IDC_PrivateKey_UC1.mul(h.powZn(α0)).getImmutable();
            IDC_PrivateKey_UC2=IDC_PrivateKey_UC2.div(h.powZn(α0)).getImmutable();
            /* write msg to server */
            msg = "CtoS1 ID_input=" + ID_input + " X_str=" + X_str + " IDC_PublicKey_PC_str=" + IDC_PublicKey_PC_str + " nc=" + nc_str;
            System.out.println("Send the first msg to Server = \n" + msg + "\n-----\n");
            sendMessage(msg);
        try{
            server_msg =dataFromServer.readUTF();
        }catch(IOException e){
            System.out.println(e);
        }
        if(server_msg.startsWith("StoC2")){
            System.out.println("The Returned msg from Server = \n"+server_msg+"\n-----");

            /* Get Y1 string to element */
            String Y1_str = server_msg.substring(server_msg.indexOf(" Y1=")+4, server_msg.indexOf(" Y2="));
            byte[] Y1_bytes = Base64.getDecoder().decode(Y1_str);
            Element Y1 = G1.newElementFromBytes(Y1_bytes).getImmutable();

            /* Get Y2 string to element */
            String Y2_str = server_msg.substring(server_msg.indexOf(" Y2=")+4, server_msg.indexOf(" hash_auths_str="));
            byte[] Y2_bytes = Base64.getDecoder().decode(Y2_str);
            Element Y2 = GT.newElementFromBytes(Y2_bytes).getImmutable();

            /* Get hash_auths_str string  改用hash 後不用轉byte */
            String hash_auths_str = server_msg.substring(server_msg.indexOf(" hash_auths_str=")+16, server_msg.indexOf(" ns="));

            /* Get random ns string to element */
            String ns_str = server_msg.substring(server_msg.indexOf(" ns=")+4,server_msg.indexOf(" IDS_PublicKey_PS_str="));
            byte[] ns_bytes = Base64.getDecoder().decode(ns_str);
            //覺得多餘
            Element ns = Zp.newElementFromBytes(ns_bytes).getImmutable();
            /* Get random ns string to element */
            String IDS_PublicKey_PS_str = server_msg.substring(server_msg.indexOf(" IDS_PublicKey_PS_str=")+22,server_msg.indexOf(" IDS_PublicKey_TS_str="));
            byte[] IDS_PublicKey_PS_bytes = Base64.getDecoder().decode(IDS_PublicKey_PS_str);
            Element IDS_PublicKey_PS = GT.newElementFromBytes(IDS_PublicKey_PS_bytes).getImmutable();
            /* Get random ns string to element */
            String IDS_PublicKey_TS_str = server_msg.substring(server_msg.indexOf(" IDS_PublicKey_TS_str=")+22);
            byte[] IDS_PublicKey_TS_bytes = Base64.getDecoder().decode(IDS_PublicKey_TS_str);
            Element IDS_PublicKey_TS = GT.newElementFromBytes(IDS_PublicKey_TS_bytes).getImmutable();
            System.out.println("Y1 = "+Y1+"\nY2 = "+Y2+"\nAuths = "+ hash_auths_str+"\nns = "+ns+"\nIDS_PublicKey_PS ="+IDS_PublicKey_PS+"\nIDS_PublicKey_TS"+IDS_PublicKey_TS+"\n-----\n");

            /* Compute session key from Elements */
            Element SKCI1 =IDS_PublicKey_TS.powZn(x).getImmutable();
            System.out.println("SKCI1 = "+SKCI1);
            Element SKCI2 =Y2.powZn(IDC_PrivateKey_UC1).powZn(IDC_PrivateKey_UC2).getImmutable();
            System.out.println("SKCI2 = "+SKCI2);
            Element SKCI3 =Y1.powZn(x).getImmutable();
            System.out.println("SKCI3 = "+SKCI3);
            Element SKCI4 =IDS_PublicKey_PS.powZn(IDC_PrivateKey_UC1).powZn(IDC_PrivateKey_UC2).getImmutable();
            System.out.println("SKCI4 = "+SKCI4);
            //bytes
            byte[] SKCI1Bytes = SKCI1.toBytes();
            byte[] SKCI2Bytes = SKCI2.toBytes();
            byte[] SKCI3Bytes = SKCI3.toBytes();
            byte[] SKCI4Bytes = SKCI4.toBytes();
            //String
            String SKCI1_str = Base64.getEncoder().encodeToString(SKCI1Bytes);
            String SKCI2_str = Base64.getEncoder().encodeToString(SKCI2Bytes);
            String SKCI3_str = Base64.getEncoder().encodeToString(SKCI3Bytes);
            String SKCI4_str = Base64.getEncoder().encodeToString(SKCI4Bytes);
            System.out.println("SKCI1_str= "+SKCI1_str);System.out.println("SKCI2_str= "+SKCI2_str);System.out.println("SKCI3_str= "+SKCI3_str);System.out.println("SKCI4_str= "+SKCI4_str);
            byte[] XORCIBytes = PUtils.xor4(SKCI1Bytes,SKCI2Bytes,SKCI3Bytes,SKCI4Bytes);
            System.out.println("XORCIBytes= "+XORCIBytes);
            XORCI_str = Base64.getEncoder().encodeToString(XORCIBytes);
            System.out.println("XORCI_str= "+XORCI_str);

            /* Checking authentication message  hash_auths_str?=check_hash_auths_str*/
            System.out.println("Checking authentication message Auths...");
            //hash
            String XORCI_concat_ns_str = XORCI_str.concat(ns_str);
            System.out.println("ns_str = \n"+ns_str+"\n");
            System.out.println("strconcat = \n"+XORCI_concat_ns_str+"\n");
            String check_hash_auths_str=PUtils.hash(XORCI_concat_ns_str, "SHA1");
            /*  Test Print CheckAuths */
            System.out.println("The Checking message check_hash_auths_str：" + check_hash_auths_str + "\n\n");
            //string 相等https://blog.csdn.net/barryhappy/article/details/6082823
            if(hash_auths_str.equals(check_hash_auths_str)) {
                System.out.println("Authentication of hash value 'Auths' is seccess!, the session key is valid.");
                String XORCI_concat_nc_str = XORCI_str.concat(nc_str);
                System.out.println("nc_str = \n"+nc_str+"\n");
                System.out.println("strconcat = \n"+XORCI_concat_nc_str+"\n");
                String hash_authc_str=PUtils.hash(XORCI_concat_nc_str, "SHA1");
                /*  Test Print CheckAuths */
                System.out.println("hash_authc_str：" +hash_authc_str + "\n\n");

                /* write msg to server */
                msg ="CtoS2 hash_authc="+hash_authc_str;
                System.out.println("Send repeat messeng to Server = \n"+msg+"\n-----\n");
                sendMessage(msg);
                System.out.println(" MAKE complete!");
            } else {
                System.out.println("Authentication of Auths is failed, SESSION STOP! Disconnected to Server!");
                System.exit(0);
            }
        }
    }
    /*--------------------------------------End line------------------------------------------*/
      public Socket SetChatConnection(String Server_IP) {
              try {
                  /*-------------------------------- Connect to server ----------------------------------------*/
                  connectedServer = new Socket(Server_IP, port);
                  msgToServer = new DataOutputStream(connectedServer.getOutputStream());
                  System.out.println("Connect to Server successful!");
                  /*------- Send the  message to Server to start chat room with Server ------------*/
                  String msg = "StratChatRoom X_str="+X_str;
                  System.out.println("Send the  message to Server to start chat room");
                  dataFromServer=new DataInputStream(connectedServer.getInputStream());
                  dataFromServer.readUTF();
                  try{
                      msgToServer.writeUTF(msg+"\r\n");
                  }catch(IOException e){
                      System.out.println("Sending msg to server is failed.");
                      connectedServer.close();
                  }
                  /*----------------------------------- End line ---------------------------------------------*/
              } catch(IOException e){
                  System.out.println("Connect to Server failed!");
              }
              return connectedServer;
          }
        public String GetChatMessageFromServer(Socket Server) {
            /*-------------------------------- Connect to server ----------------------------------------*/
            connectedServer = Server;
            /*------- Get the chat message from Server  ------------*/
            String server_chat_msg= null;
            if(connectedServer.isConnected()){
                try {
                    msgFromServer = new BufferedReader(new InputStreamReader(new DataInputStream(connectedServer.getInputStream())));
                    server_chat_msg = msgFromServer.readLine();
                    if(server_chat_msg != null) System.out.println("Get the  message from Server : "+server_chat_msg);
                }catch(IOException e) {
                    System.out.println("Get msg from server is failed.");
                    return "ConnectionLost";
                }
            }
            return server_chat_msg;
            /*----------------------------------- End line ---------------------------------------------*/
        }
    public void SendChatMessageToServer(Socket Server, String ChatMessage) {
        try {
            /*-------------------------------- Connect to server ----------------------------------------*/
            connectedServer = Server;
            msgToServer = new DataOutputStream(connectedServer.getOutputStream());
            System.out.println("Connect to Server successful!");
            /*------- Send the  message to Server to get Server's public keys ------------*/
            System.out.println("Send the  chat message to Server : "+ChatMessage);
            try{
                msgToServer.writeBytes(ChatMessage+"\r\n");
            }catch(IOException e){
                System.out.println("Sending msg to server is failed.");
                connectedServer.close();
            }
            /*----------------------------------- End line ---------------------------------------------*/
        } catch(IOException e){
            System.out.println("Connect to Server failed!");
        }
    }
    private void sendMessage(String msg){
        try{
            msgToServer.writeUTF(msg);

        }catch(IOException e){
            System.out.println("Sending msg to server is failed.");
            System.out.println(e);
        }
    }
}