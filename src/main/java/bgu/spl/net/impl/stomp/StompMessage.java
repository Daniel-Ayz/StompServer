package bgu.spl.net.impl.stomp;

import java.util.HashMap;
import java.util.Map;

public class StompMessage {

    enum StompCommand{
        CONNECTED,
        MESSAGE,
        RECEIPT,
        ERROR,
        CONNECT,
        SEND,
        SUBSCRIBE,
        UNSUBSCRIBE,
        DISCONNECT
    }

    public StompCommand command;
    public Map<String, String> headers;
    public String body;

    public StompMessage(StompCommand cmd, Map<String, String> headers, String body){
        this.command = cmd;
        this.headers = headers;
        this.body = body;
    }

    @Override
    public String toString() {
        return command + "\n"
                + mapToString()
                + "\n"
                + body;
    }

    private String mapToString(){
        String str = "";
        for (Map.Entry<String, String> entry: headers.entrySet()){
            str += entry.getKey() + ":" + entry.getValue() + "\n";
        }
        return str;
    }

    public static StompMessage parseToStompMessage(String msg){
        Map<String, String> headers = new HashMap<>();
        String body = "";
        String[] lines = msg.split("\n");
        String cmd = lines[0];
        boolean headersEnded = false;
        for (int i=1;i< lines.length - 1; i++){
            if (lines[i].equals(""))
                headersEnded = true;
            else if(headersEnded){
                body += lines[i];
            }
            else{
                String[] pair = lines[i].split(":");
                headers.put(pair[0],pair[1]);
            }
        }
        return new StompMessage(StompCommand.valueOf(cmd), headers, body);
    }

}
