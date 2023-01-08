package bgu.spl.net.impl.stomp;

import bgu.spl.net.srv.ConnectionHandler;
import bgu.spl.net.srv.Connections;
import jdk.internal.net.http.common.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConnectionsImpl implements Connections<String> {

    private Map<Integer, String> connectionIdToUsername;
    private Map<String, User> usernameToUser;
    private Map<String, List<User>> topicsToUsers;

    public ConnectionsImpl(){
        usernameToUser = new HashMap<>();
        topicsToUsers = new HashMap<>();
    }

    public boolean connect(String username, String password, ConnectionHandler handler, Integer connectionId){
        if (!connectionIdToUsername.containsKey(connectionId))
            return false;
        if(!usernameToUser.containsKey(username))
            addUser(username, password);
        boolean connected = usernameToUser.get(username).connect(password, handler);
        if(connected)
            addConnectionIdToUser(connectionId, username);
        return connected;
    }

    private void addUser(String username, String password){
        User user = new User(username, password);
        usernameToUser.put(username, user);
    }

    private void addConnectionIdToUser(Integer connectionId, String username){
        connectionIdToUsername.put(connectionId, username);
    }

    public boolean subscribe(Integer connectionId, String dest){
        String username = connectionIdToUsername.get(connectionId);
        if(username != null){
            User user = usernameToUser.get(username);
            user.sub(connectionId, dest);
            List<User> users = topicsToUsers.getOrDefault(dest, new ArrayList<User>());
            users.add(user);
            topicsToUsers.put(dest, users);
            return true;
        }
        return false;
    }

    public boolean unsub(Integer connectionId, Integer id){
        String username = connectionIdToUsername.get(connectionId);
        if(username != null){
            User user = usernameToUser.get(username);
            String topic = user.getTopic(id);
            return user.unsub(id) && topicsToUsers.get(topic).remove(user);
        }
        return false;
    }

    @Override
    public boolean send(int connectionId, String msg) {
        String username = connectionIdToUsername.get(connectionId);
        if(username != null){
            User user = usernameToUser.get(username);
            user.getHandler().send(msg);
        }
        return false;
    }

    @Override
    public boolean send(String channel, String msg, Integer connectionId) {
        String username = connectionIdToUsername.get(connectionId);
        if(username != null){
            User user = usernameToUser.get(username);
            List<User> users = topicsToUsers.get(channel);
            if(users.contains(user)) {
                for (User u : users) {
                    u.getHandler().send(msg);
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public void disconnect(int connectionId) {
        String username = connectionIdToUsername.get(connectionId);
        usernameToUser.get(username).setConnected(false);
        connectionIdToUsername.remove(connectionId);
    }
}
