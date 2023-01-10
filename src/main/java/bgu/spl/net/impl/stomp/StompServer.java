package bgu.spl.net.impl.stomp;

import bgu.spl.net.srv.Connections;
import bgu.spl.net.srv.Server;

import java.util.HashMap;
import java.util.Map;

public class StompServer {

    public static void main(String[] args) {
        // TODO: implement this
        if(args.length == 2 && args[0].matches("^[0-9]+$")){
            if(args[1].equals("reactor")){
                Server.reactor(
                        Runtime.getRuntime().availableProcessors(),
                        Integer.parseInt(args[0]), //port
                        () ->  new Protocol(), //protocol factory
                        EncoderDecoder::new, //message encoder decoder factory
                        new ConnectionsImpl()
                ).serve();
            }
            else if(args[1].equals("tpc")){
                Server.threadPerClient(
                        Integer.parseInt(args[0]), //port
                        () -> new Protocol(), //protocol factory
                        EncoderDecoder::new, //message encoder decoder factory
                        new ConnectionsImpl()
                ).serve();
            }
            else{
                System.out.println("Error in creation, undefined server type.");
            }
        }
    }
}
