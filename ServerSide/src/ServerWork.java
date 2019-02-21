import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class ServerWork extends Thread{

    private final Socket clientSocket;
    private final Server server;
    private String login = null;
    private OutputStream out;
    private InputStream in;
    private HashSet<String> topicSet = new HashSet<>();

    public ServerWork( Server server, Socket clientSocket ) {
        this.server = server;
        this.clientSocket = clientSocket;
    }


    @Override
    public void run() {
        handleClientSocket(clientSocket);
    }
    private void handleClientSocket(Socket clientSocket) {
        try{
            in = clientSocket.getInputStream();
            out = clientSocket.getOutputStream();

            BufferedReader reader = new BufferedReader( new InputStreamReader( in ));
            String input, cmd;

            while ( (input = reader.readLine()) != null) {
                String[] tokens = input.split( " " );
                if ( tokens.length > 0 ) {
                    cmd = tokens[0];
                    if ( "quit".equalsIgnoreCase( cmd ) || "logoff".equalsIgnoreCase( cmd ) ) {
                        handleLogoff();
                        break;
                    }else if ("login".equalsIgnoreCase( cmd )){
                        handleLogin(out ,tokens);
                    }else if("join".equalsIgnoreCase( cmd )){
                        handleJoin(tokens);
                    }
                    else if ("msg".equalsIgnoreCase( cmd )) {
                        String[] tokensMsg = input.split( " ", 3 );
                        handleMessage( tokensMsg );
                    }else if ("leave".equalsIgnoreCase( cmd )){
                        handleLeave(tokens);
                    }
                }
            }
            clientSocket.close();
        } catch ( IOException e) {
            e.printStackTrace();
        }
    }

    // BOOL TO CHECK WHETHER IS MEMBER OF THE TOPIC
    private boolean isMemberOfTopic( String topic ){ return topicSet.contains( topic ); }

    //  TOPIC HANDLING
    private void handleJoin(String[] tokens) {
        if(tokens.length > 1){
            String topic = tokens[1];
            topicSet.add( topic );
        }
    }

    //LEAVE TOPIC
    private void handleLeave(String[] tokens) {
        if(tokens.length > 1){
            String topic = tokens[1];
            topicSet.remove( topic );
        }
    }

    // format: "msg" "login" message...
    // format: "msg" "#topic" massage...
    private void handleMessage( String[] tokens ) throws IOException {
        String sendTo = tokens[1], body_msg = tokens[2];
        boolean isTopic = sendTo.charAt( 0 ) == '#';
        List<ServerWork> workList = server.getWorkerList();
        for ( ServerWork worker : workList ){
            try {
                // TOPIC CHAT
                /* ------------------------------------------------------ */
                if (isTopic){
                    if(worker.isMemberOfTopic( sendTo )){
                        String out_msg = sendTo + ":" + " <" + login  + ">" + " " + body_msg + "\n";
                        worker.send( out_msg );
                    }
                    /* -------------------------------------------------------- */

                }else {
                    if ( sendTo.equalsIgnoreCase( worker.getLogin() ) ) {
                        String out_msg = "msg " + login + " " + body_msg + "\n";
                        worker.send( out_msg );
                    } else if ( sendTo.equalsIgnoreCase( "all" ) && !login.equals( worker.getLogin() ) ) {
                        String out_msg = getLogin() + ": " + body_msg + "\n";
                        worker.send( out_msg );
                    }
                }
            }catch ( SocketException err ){
                handleError();
            }
        }

    }

    private void handleLogoff() throws IOException {
        server.removeWorker(this);
        List<ServerWork> workList = server.getWorkerList();

        for(ServerWork worker : workList) {
            if ( !login.equals( worker.getLogin() ) ) {
                worker.send( "Offline: " + login + "\n" );
            }
        }
        clientSocket.close();
    }

    private String getLogin(){ return login; }

    private void handleLogin( OutputStream out, String[] tokens ) throws IOException {
        if(tokens.length == 2){
            String login = tokens[1];


            if(login.equals( "Alpha" ) || login.equals( "Beta" ) || login.equals( "Gamma" ) || login.equals( "Delta" )|| login.equals( "Epsilon" )|| login.equals( "Sigma" )   ){
                this.login = login;
                String loggedin = "\n" + login + " has logged in" + "\n";
                out.write( loggedin.getBytes() );
                System.out.println("USer logged in: " + login);

                List<ServerWork> workList = server.getWorkerList();


                // Send current user all others online status

                for(ServerWork worker : workList) {
                    if (worker.getLogin() != null && !login.equals( worker.getLogin()) ){
                        String message = "\nonline: " + worker.getLogin() + "\n";
                        send( message);

                    }
                }

                //send other online users current user's status
                String online_Status = "\nOnline: " + login + "\n";
                for(ServerWork worker : workList) {
                    if ( !login.equals( worker.getLogin() ) ) {
                        worker.send(online_Status);

                    }
                }
            }else{
                out.write( ("Login unknown: " + login + "\n").getBytes() );
                System.err.println("Login failed: <" + login + ">");

            }
        }
    }

    private void send( String msg ) throws IOException{
        if (login != null){
            out.write( msg.getBytes() );
        }
    }

    private void handleError( ) throws IOException {
        server.removeWorker( this );
        List<ServerWork> workList = server.getWorkerList();
        for (ServerWork worker : workList){
            String out_msg = getLogin() + " is offline \n";
            worker.send( out_msg);
        }
    }

}


//rafael.oliveira@souunit.com.br