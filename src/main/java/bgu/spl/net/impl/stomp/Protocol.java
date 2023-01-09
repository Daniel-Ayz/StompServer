package bgu.spl.net.impl.stomp;

import bgu.spl.net.api.StompMessagingProtocol;
import bgu.spl.net.srv.ConnectionHandler;
import bgu.spl.net.srv.Connections;

import java.util.HashMap;
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
        String error = "";
        switch(command){
            case CONNECT:
                if(!headers.containsKey("accept-version") ||
                        !headers.containsKey("host") ||
                        !headers.containsKey("login") ||
                        !headers.containsKey("passcode")) {
                    error = "Doesn't contain required headers";
                }
                else{
                    boolean connected = connections.connect(headers.get("login"), headers.get("passcode"),handler, connectionId);
                    if(!connected){
                        error = "Failed to connect";
                    }
                    else{
                        connections.send(connectionId, createConnected(headers.get("accept-version")).toString());
                        if(headers.containsKey("receipt"))
                            connections.send(connectionId, createReceipt(headers.get("receipt")).toString());
                    }
                }
                break;
            case SEND:
                if(!headers.containsKey("destination")) {
                    error = "Doesn't contain required headers";
                }
                else{
                    if(connections.send(headers.get("destination"), msg.body, connectionId)){
                        if(headers.containsKey("receipt"))
                            connections.send(connectionId, createReceipt(headers.get("receipt")).toString());
                    }
                    else
                        error = "Failed to send message to "+headers.get("destination");
                }
                break;
            case SUBSCRIBE:
                if(!headers.containsKey("destination") ||
                        !headers.containsKey("id")) {
                    error = "Doesn't contain required headers";
                }
                else{
                    if(connections.subscribe(Integer.parseInt(headers.get("id")), headers.get("destination"))){
                        if(headers.containsKey("receipt"))
                            connections.send(connectionId, createReceipt(headers.get("receipt")).toString());
                    }
                    else{
                        error = "Failed to Subscribe";
                    }
                }
                break;
            case UNSUBSCRIBE:
                if(!headers.containsKey("id")) {
                    error = "Doesn't contain required headers";
                }
                else{
                    if(connections.unsub(connectionId, Integer.parseInt(headers.get("id")))){
                        if(headers.containsKey("receipt"))
                            connections.send(connectionId, createReceipt(headers.get("receipt")).toString());
                    }
                    else{
                        error = "Failed to Unsubscribe";
                    }
                }
                break;
            case DISCONNECT:
                if(!headers.containsKey("receipt")) {
                    error = "Doesn't contain required headers";
                }
                else{
                    connections.disconnect(connectionId);
                    if(headers.containsKey("receipt"))
                        connections.send(connectionId, createReceipt(headers.get("receipt")).toString());
                    //close connection
                }
                break;
            default:
                error = "Unknown stomp command";
                break;
        }
        if(!error.equals("")){
            connections.send(connectionId, createError(headers.getOrDefault("receipt",""), message, error).toString());
            //disconnect client
        }
    }

    @Override
    public boolean shouldTerminate() {
        return false;
        //?
    }

    private StompMessage createReceipt(String receiptId){
        Map<String, String> headers = new HashMap<String, String>(){{put("receipt-id", receiptId);}};
        return new StompMessage(StompMessage.StompCommand.RECEIPT, headers, "");
    }

    private StompMessage createError(String receiptId, String msg, String errorDesc){
        Map<String, String> headers = new HashMap<String, String>(){{put("message", errorDesc);}};
        if(!receiptId.equals(""))
            headers.put("receipt-id", receiptId);
        return new StompMessage(StompMessage.StompCommand.ERROR, headers, "The message:\n-----\n"+msg+"\n-----");
    }

    private StompMessage createConnected(String version){
        Map<String, String> headers = new HashMap<String, String>(){{put("version", version);}};
        return new StompMessage(StompMessage.StompCommand.CONNECTED, headers, "");
    }



}
