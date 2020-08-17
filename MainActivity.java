package com.LRAKE_IoT_PAL.mobile_client;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;

import java.net.Socket;

public class MainActivity extends AppCompatActivity {
    private Button  TogetPPButt, ConnectToServerButt, StartChatButt,EndChatButt, SendMessageButt;
    private EditText ID_Input, IP_Input, Message_Input;
    private InputMethodManager inputManager;
    private TextView SSKresult, ServerChat, ClientChat;
    private String SessionKey = null,chatcontrol=null;
    private String DialogMsg, Server_IP, GetPublicKeyComplete, SessionKey_str, ChatMessage, Server_ChatMessage;
    private Socket ChatServer;
    private String CipherText, PlainText,PublicParameters,IDC_PrivateKey_UC1,IDC_PrivateKey_UC2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide(); //設定隱藏標題
        init();
    }

    public void init(){
        inputManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        SSKresult = (TextView) findViewById(R.id.SSK);
        ServerChat = (TextView) findViewById(R.id.ServerChat);
        ServerChat.setMovementMethod(ScrollingMovementMethod.getInstance());
        ClientChat= (TextView) findViewById(R.id.ClientChat);
        ClientChat.setMovementMethod(ScrollingMovementMethod.getInstance());

        ID_Input = (EditText) findViewById(R.id.ID_input);
        IP_Input = (EditText) findViewById(R.id.IP_input);
        ID_Input.setText("MobileClient");
        IP_Input.setText("2.tcp.ngrok.io");//ngrok tcp channel symbol
        Message_Input = (EditText) findViewById(R.id.Msg_input);
        ConnectToServerButt = findViewById(R.id.ConnectToServer);
        TogetPPButt= findViewById(R.id.TogetPPButtom);
        StartChatButt = findViewById(R.id.StartChat);
        EndChatButt=findViewById(R.id.EndChat);

        SendMessageButt = findViewById(R.id.SendMessage);

        TogetPPButt.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                Thread thread = new Thread(GETPP);  //賦予執行緒工作
                thread.start();
            }
        });
        ConnectToServerButt.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                Thread thread = new Thread(MAKEConnect);  //賦予執行緒工作
                thread.start();
            }
        });

        StartChatButt.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                ServerChat.setText("");
                ClientChat.setText("");
                Thread thread = new Thread(ConnectToServerForChat);  //賦予執行緒工作
                thread.start();
                StartChatButt.setEnabled(false);
                EndChatButt.setEnabled(true);
            }
        });
        /*YEE*/
        EndChatButt.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                ServerChat.setText("");
                ClientChat.setText("");
                chatcontrol="EndChat";
                Thread thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            new MClient().SendChatMessageToServer(ChatServer, chatcontrol);
                        }
                    });  //賦予執行緒工作
                    thread.start();
                    Message_Input.setText("");
                StartChatButt.setEnabled(false);
                EndChatButt.setEnabled(false);
                SendMessageButt.setEnabled(false);
                inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        });
        SendMessageButt.setOnClickListener(SendMessageClick);
}
    //------------------------------------------------------------------------------------------------------------------------------------------------
    private Runnable GETPP=new Runnable(){
        @Override
        public void run() {
            PublicParameters = new MClient().MClient(IP_Input.getText().toString(),ID_Input.getText().toString(),0);
            if(PublicParameters != null) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {//先出來被壓過
                        DialogMsg = "GETPP完成! \n已獲得 Public parameters和個人金鑰!分別如下 \n ";
                        new AlertDialog.Builder(MainActivity.this).setTitle(DialogMsg).setMessage(PublicParameters).setPositiveButton("ok",null).create().show();
                        TogetPPButt.setEnabled(false);
                        ConnectToServerButt.setEnabled(true);
                    }
                });
            } else{
                System.out.println("Connect to Server failed.");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        DialogMsg = "GETPP失敗";
                        new AlertDialog.Builder(MainActivity.this).setTitle(DialogMsg).setPositiveButton("ok",null).create().show();
                        StartChatButt.setEnabled(false);}
                });
            }
        }
    };
    //------------------------------------------------------------------------------------------------------------------------------------------------
    private Runnable MAKEConnect=new Runnable(){
        @Override
        public void run() {
            String txt = new MClient().MClient(IP_Input.getText().toString(),ID_Input.getText().toString(),1);
            SessionKey=txt.substring(txt.indexOf("XORCI_str=")+10,txt.indexOf("IDC_PrivateKey_UC1="));
            IDC_PrivateKey_UC1="新的IDC_PrivateKey_UC1=".concat(txt.substring(txt.indexOf("IDC_PrivateKey_UC1=")+19,txt.indexOf("IDC_PrivateKey_UC2=")));
            IDC_PrivateKey_UC2="新的IDC_PrivateKey_UC2=".concat(txt.substring(txt.indexOf("IDC_PrivateKey_UC2=")+19));
            if(SessionKey != null) {
               runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        DialogMsg = "更新Client金鑰完成! \n";
                        new AlertDialog.Builder(MainActivity.this).setTitle(DialogMsg).setMessage(IDC_PrivateKey_UC1+"\n"+IDC_PrivateKey_UC2).setPositiveButton("ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface DialogMsg, int id) {
                                try {
                                    Thread.sleep(1000); //1000 毫秒，也就是1秒.
                                    String dialog= "MAKEConnect完成! \n已獲得 Session Key!";
                                    new AlertDialog.Builder(MainActivity.this).setTitle(dialog).setMessage(SessionKey).setPositiveButton("ok",null).create().show();
                                } catch(InterruptedException ex) {
                                    Thread.currentThread().interrupt();
                                }
                            }
                        }).create().show();

                        SSKresult.setText(SessionKey);
                       // DialogMsg = "MAKEConnect完成! \n已獲得 Session Key!";
                       // new AlertDialog.Builder(MainActivity.this).setTitle(DialogMsg).setPositiveButton("ok",null).create().show();
                        StartChatButt.setEnabled(true);
                    }
                });
            } else{
                System.out.println("Connect to Server failed.");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        DialogMsg = "MAKEConnect 失敗";
                        new AlertDialog.Builder(MainActivity.this).setTitle(DialogMsg).setPositiveButton("ok",null).create().show();
                        StartChatButt.setEnabled(false);}
                });
            }
        }
    };
    //------------------------------------------------------------------------------------------------------------------------------------------------
    private Runnable ConnectToServerForChat=new Runnable() {
        @Override
        public void run() {
            //Get Chat Room connection
            Server_IP = IP_Input.getText().toString();
            ChatServer = new MClient().SetChatConnection(Server_IP);
            if (ChatServer.isConnected()) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        SendMessageButt.setEnabled(true);
                    }
                });
            } else {
                System.out.println("Connect to Server failed.");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        SendMessageButt.setEnabled(false);
                    }
                });
            }
            //Loop of Get Server's Chat message
            while (ChatServer.isConnected()) {
                Server_ChatMessage = new MClient().GetChatMessageFromServer(ChatServer);
                if (Server_ChatMessage == "ConnectionLost") {
                    break;
                } else if (Server_ChatMessage != null) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ServerChat.append("Server說 : \n" + Server_ChatMessage + "\n");
                        }
                    });
                    /* 解密訊息 */
                    try {
                        PlainText = PUtils.Decrypt1(Server_ChatMessage, SessionKey);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ServerChat.append("解密後為 : \n" + PlainText + "\n");
                            }
                        });
                    } catch (Exception e) {
                    }
                }}
                System.out.println("Connect to Server failed.");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        SendMessageButt.setEnabled(false);
                    }
                });
            }
        };

        //------------------------------------------------------------------------------------------------------------------------------------------------
        private Button.OnClickListener SendMessageClick = new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChatMessage = Message_Input.getText().toString();
                if (ChatMessage.length() > 0) {
                    try {
                        CipherText = PUtils.Encrypt1(ChatMessage, SessionKey);
                        Thread thread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                new MClient().SendChatMessageToServer(ChatServer, CipherText);
                            }
                        });  //賦予執行緒工作
                        thread.start();
                        ClientChat.append("加密後為 : \n" + CipherText + "\n");
                    } catch (Exception e1) {
                    }
                    Message_Input.setText("");
                    ClientChat.append("你說 : \n" + ChatMessage + "\n");
                    Message_Input.setText("");
                }
            }
        };
    }