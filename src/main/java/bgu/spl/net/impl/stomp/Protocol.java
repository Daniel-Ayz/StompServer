package bgu.spl.net.impl.stomp;

import bgu.spl.net.api.StompMessagingProtocol;
import bgu.spl.net.srv.ConnectionHandler;
import bgu.spl.net.srv.Connections;

import java.util.Map;

public class Protocol implements StompMessagingProtocol<String> {

    private ConnectionsImpl connections;
    private Integer connectionId;
    private static Integer connectionIdCounter = 0;

    public Protocol(ConnectionsImpl connections){
        this.connections = connections;
        this.connectionId = connectionIdCounter;
        connectionIdCounter++;
    }

    @Override
    public void start(int connectionId, Connections<String> connections) {
        //what's this???? >:(((((
    }

    @Override
    public void process(String message, ConnectionHandler handler) {
        StompMessage msg = StompMessage.parseToStompMessage(message);
        StompMessage.StompCommand command = msg.command;
        Map<String, String> headers = msg.headers;
        boolean error = false;
        switch(command){
            case CONNECT:
                if(!headers.containsKey("accept-version") ||
                        !headers.containsKey("host") ||
                        !headers.containsKey("login") ||
                        !headers.containsKey("passcode")) {
                    error = true;
                }
                else{
                    boolean connected = connections.connect(headers.get("login"), headers.get("passcode"),handler, connectionId);
                    if(!connected){
                        error = true;
                    }
                    else{
                        //send CONNECTED
                    }
                }
                break;
            case SEND:
                if(!headers.containsKey("destination")) {
                    error = true;
                }
                else{
                    //check if sub to dest -> if not send >:(
                    //send SPAM to all users in DESTINATION
                }
                break;
            case SUBSCRIBE:
                if(!headers.containsKey("destination") ||
                        !headers.containsKey("id")) {
                    error = true;
                }
                else{
                    if(connections.subscribe(Integer.parseInt(headers.get("id")), headers.get("destination"))){
                        //send good message if has receipt id
                    }
                    else{
                        error = true;
                    }
                }
                break;
            case UNSUBSCRIBE:
                if(!headers.containsKey("id")) {
                    error = true;
                }
                else{
                    if(connections.unsub(connectionId, Integer.parseInt(headers.get("id")))){
                        //send good message if has receipt id
                    }
                    else{
                        error = true;
                    }
                }
                break;
            case DISCONNECT:
                if(!headers.containsKey("receipt")) {
                    error = true;
                }
                else{
                    connections.disconnect(connectionId);
                    //send receipt to client
                    //close connection
                }
                break;
            default:
                error = true;
                break;
        }
        if(error){
            //send error frame angry af >:(
            //disconnect client
        }
    }

    @Override
    public boolean shouldTerminate() {
        return false;
    }
}
