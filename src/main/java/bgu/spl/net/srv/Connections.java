package bgu.spl.net.srv;

import java.io.IOException;

public interface Connections<T> {

    boolean send(int connectionId, T msg);

    boolean send(String channel, T msg, Integer connectionId);

    void disconnect(int connectionId);
}
