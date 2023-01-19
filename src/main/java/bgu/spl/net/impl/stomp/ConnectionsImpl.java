package bgu.spl.net.impl.stomp;

import bgu.spl.net.srv.ConnectionHandler;
import bgu.spl.net.srv.Connections;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class ConnectionsImpl<T> implements Connections<T> {

    private Map<Integer, String> connectionIdToUsername;
    private Map<String, User> usernameToUser;
    private Map<String, List<User>> topicsToUsers;
    private AtomicInteger messageCounter;

    public ConnectionsImpl(){
        connectionIdToUsername = new ConcurrentHashMap<>();
        usernameToUser = new ConcurrentHashMap<>();
        topicsToUsers = new ConcurrentHashMap<>();
        messageCounter = new AtomicInteger(0);
    }

    public String connect(String username, String password, ConnectionHandler handler, Integer connectionId){
        if (connectionIdToUsername.containsKey(connectionId))
            return "The client is already logged in, log out before trying again";
        if(!usernameToUser.containsKey(username))
            addUser(username, password);
        String connected = usernameToUser.get(username).connect(password, handler);
        if(connected.equals(""))
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

    public boolean subscribe(Integer connectionId, Integer subId, String dest){
        String username = connectionIdToUsername.get(connectionId);
        if(username != null){
            User user = usernameToUser.get(username);
            user.sub(subId, dest);
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
    public boolean send(int connectionId, T msg) {
        System.out.println("ConnectionsImpl.send->sending message:\n"+msg.toString());
        String username = connectionIdToUsername.get(connectionId);
        if(username != null){
            User user = usernameToUser.get(username);
            user.getHandler().send(msg);
        }
        return false;
    }

    public void send(ConnectionHandler handler, T msg) {
        System.out.println("ConnectionsImpl.send->sending message:\n"+msg.toString());
        handler.send(msg);
    }

    @Override
    public boolean send(String channel, T msg, Integer connectionId) {
        String username = connectionIdToUsername.get(connectionId);
        if(username != null){
            User user = usernameToUser.get(username);
            List<User> users = topicsToUsers.get(channel);
            if(users != null){
                int messageId = messageCounter.getAndIncrement();
                if(users.contains(user)) {
                    for (User u : users) {
                        String message = createMessage(msg, user, channel, messageId).toString();
                        System.out.println("ConnectionsImpl.sendChannel->sending message:\n"+message.toString());
                        u.getHandler().send(message);
                    }
                    return true;
                }
                else{
                    // you arent subbed
                }
            }
            else{
                //no such topic
            }
            
        }
        return false;
    }

    // public boolean topicExists(String channel){
    //     return topicsToUsers.containsKey(channel);
    // }

    @Override
    public void disconnect(int connectionId) {
        if(connectionIdToUsername.containsKey(connectionId)){
            String username = connectionIdToUsername.get(connectionId);
            User u = usernameToUser.get(username);
            u.setConnected(false);
            for (Map.Entry<String, List<User>> entry : topicsToUsers.entrySet()) {
                List<User> userList = entry.getValue();
                userList.remove(u);
            }
            connectionIdToUsername.remove(connectionId);
        }
    }

    private StompMessage createMessage(T body, User user, String topic, int messageId){
        Map<String, String> headers = new HashMap<String, String>(){{put("subscription", String.valueOf(user.getTopicId(topic))); put("message-id", String.valueOf(messageId)); put("destination", topic);}};
        return new StompMessage(StompMessage.StompCommand.MESSAGE, headers, body.toString());
    }

}