package bgu.spl.net.impl.stomp;

import bgu.spl.net.srv.ConnectionHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class User {
    private String username;
    private String password;
    private Map<Integer, String> idToTopic;
    private ConnectionHandler<String> handler;
    private boolean isConnected;

    public User(String username, String password){
        this.username = username;
        this.password = password;
        idToTopic = new HashMap<>();
        this.isConnected = false;
    }
    public boolean connect(String password, ConnectionHandler handler){
        if(this.password.equals(password) && !isConnected){
            this.handler = handler;
            setConnected(true);
            return true;
        }
        return false;
    }

    public void sub(Integer id, String topic){
        idToTopic.put(id, topic);
    }

    public boolean unsub(Integer id){
        return idToTopic.remove(id) != null;
    }

    public boolean isConnected() {
        return isConnected;
    }

    public void setConnected(boolean isConnected){
        this.isConnected = isConnected;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getTopic(int id) {
        return idToTopic.get(id);
    }

    public int getTopicId(String topic){
        for(Map.Entry entry: idToTopic.entrySet()){
            if(entry.getValue().equals(topic))
                return (int)entry.getKey();
        }
        return -1;
    }

    public ConnectionHandler getHandler(){
        return handler;
    }

}
