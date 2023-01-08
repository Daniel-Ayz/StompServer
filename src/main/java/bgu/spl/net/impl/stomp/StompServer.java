package bgu.spl.net.impl.stomp;

import java.util.HashMap;
import java.util.Map;

public class StompServer {

    public static void main(String[] args) {
        // TODO: implement this
//        Map<String, String> headers = new HashMap<String, String>(){{
//            put("destination", "/dest");
//            put("id", "1");
//        }};
//        StompMessage msg = new StompMessage(StompMessage.StompCommand.SUBSCRIBE, headers, "" );
//        System.out.println(msg);
        String str = "CONNECT\n" +
                "accept-version:1.2\n" +
                "host:stomp.cs.bgu.ac.il\n" +
                "login:meni\n" +
                "passcode:films\n" +
                "\n" +
                "^@";
        System.out.println(str);
        StompMessage msg = StompMessage.parseToStompMessage(str);
        System.out.println(msg);
    }
}
