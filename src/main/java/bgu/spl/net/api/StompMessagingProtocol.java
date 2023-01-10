package bgu.spl.net.api;

import bgu.spl.net.impl.stomp.ConnectionsImpl;
import bgu.spl.net.srv.ConnectionHandler;
import bgu.spl.net.srv.Connections;

public interface StompMessagingProtocol<T>  {
	/**
	 * Used to initiate the current client protocol with it's personal connection ID and the connections implementation
	**/
    void start(ConnectionsImpl connections);
    
    void process(T message, ConnectionHandler<T> handler);
	
	/**
     * @return true if the connection should be terminated
     */
    boolean shouldTerminate();
}
