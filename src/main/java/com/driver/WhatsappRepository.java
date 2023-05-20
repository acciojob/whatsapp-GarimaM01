package com.driver;

import java.util.*;

import org.springframework.stereotype.Repository;

@Repository
public class WhatsappRepository {

    //Assume that each user belongs to at most one group
    //You can use the below mentioned hashmaps or delete these and create your own.
    private HashMap<Group, List<User>> groupUserMap;
    private HashMap<Group, List<Message>> groupMessageMap;
    private HashMap<Message, User> senderMap;
    private HashMap<Group, User> adminMap;
    private HashSet<String> userData;
    private int customGroupCount;
    private int messageId;

    public WhatsappRepository(){
        this.groupMessageMap = new HashMap<Group, List<Message>>();
        this.groupUserMap = new HashMap<Group, List<User>>();
        this.senderMap = new HashMap<Message, User>();
        this.adminMap = new HashMap<Group, User>();
        this.userData = new HashSet<>();
        this.customGroupCount = 0;
        this.messageId = 0;
    }
    public String createUser(String name, String mobile) throws Exception{
        if (userData.contains(mobile)) {
            throw new Exception("User already exists");
        } else {
            User user = new User(name, mobile);
            userData.add(mobile);
            return "SUCCESS";
        }
    }

    public Group createGroup(List<User> users){
        if (users.size() == 2) {
            Group gp = new Group(users.get(1).getName(), 2);
            groupUserMap.put(gp, new ArrayList<>(users));
            groupMessageMap.put(gp, new ArrayList<>());
            return gp;
        } else {
            customGroupCount++;
            Group gp = new Group("Group " + customGroupCount, users.size());
            groupUserMap.put(gp, new ArrayList<>(users));
            groupMessageMap.put(gp, new ArrayList<>());
            adminMap.put(gp, users.get(0));
            return gp;
        }
    }


    public int createMessage(String content){
        messageId++;
        Message m = new Message(messageId,content);
        return m.getId();
    }

    public int sendMessage(Message message,User sender, Group group) throws Exception{
        if (!groupUserMap.containsKey(group)) {
            throw new Exception("Group does not exist");
        }

        if (!groupUserMap.get(group).contains(sender)) {
            throw new Exception("You are not allowed to send a message");
        }

        List<Message> messageList = groupMessageMap.get(group);
        messageList.add(message);
        groupMessageMap.put(group, messageList);

        senderMap.put(message, sender);

        return messageList.size();
    }

    public String changeAdmin(User approver, User user, Group group) throws Exception{
        if (!groupUserMap.containsKey(group)) {
            throw new Exception("Group does not exist");
        }

        if (adminMap.get(group) != approver) {
            throw new Exception("Approver does not have rights");
        }

        if (!groupUserMap.get(group).contains(user)) {
            throw new Exception("User is not a participant");
        }

        adminMap.replace(group, user);
        return "SUCCESS";

    }

    public int removeUser(User user) throws Exception{
        for (Group gp : groupUserMap.keySet()) {
            List<User> userList = groupUserMap.get(gp);
            if (userList.contains(user)) {
                for (User admin : adminMap.values()) {
                    if (admin == user) {
                        throw new Exception("Cannot remove admin");
                    }
                }
                groupUserMap.get(gp).remove(user);

                Iterator<Map.Entry<Message, User>> iterator = senderMap.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry<Message, User> entry = iterator.next();
                    if (entry.getValue() == user) {
                        Message m = entry.getKey();
                        iterator.remove();
                        groupMessageMap.get(gp).remove(m);
                        return groupUserMap.get(gp).size() + groupMessageMap.get(gp).size() + senderMap.size();
                    }
                }
            }
        }
        throw new Exception("User not found");
    }

    public String findMessage(Date start, Date end, int K) throws Exception{
        TreeMap<Integer, String> map = new TreeMap<>();
        ArrayList<Integer> list = new ArrayList<>();

        for (Message msg : senderMap.keySet()) {
            if (msg.getTimestamp().compareTo(start) > 0 && msg.getTimestamp().compareTo(end) < 0) {
                map.put(msg.getId(), msg.getContent());
                list.add(msg.getId());
            }
        }

        if (map.size() < K) {
            throw new Exception("K is greater than the number of messages");
        }

        Collections.sort(list);
        int k = list.get(list.size() - K);
        return map.get(k);

    }
}
