package com.muc;

import java.io.*;
import java.net.Socket;

public class ChatClient {
    private final String serverName;
    private final int serverPort;
    private Socket socket;
    private OutputStream out;
    private InputStream in;
    private BufferedReader bufferedIn;

    public ChatClient (String serverName, int serverPort){
        this.serverName = serverName;
        this.serverPort = serverPort;
    }
    public static void main( String[] args ) throws IOException {
        ChatClient client = new ChatClient("localhost", 4444);
        if (!client.connect()){
            System.err.println( "Connect failed" );
        }else{
            System.out.println( "Success" );
            client.login("Alpha");
        }
    }

    private void login( String login ) throws IOException {
        String cmd = "login" + login + "\n";
        out.write( cmd.getBytes() );


        String response = bufferedIn.readLine();
        System.out.println("Line: " + response);
    }


    private boolean connect(){
        try {
            this.socket = new Socket(serverName, serverPort);
            System.out.println("Port: " + socket.getLocalPort());
            this.out = socket.getOutputStream();
            this.in = socket.getInputStream();
            this.bufferedIn = new BufferedReader( new InputStreamReader( in ) );
            return true;
        } catch ( IOException e ) {
            e.printStackTrace();
        }
        return false;
    }
}

