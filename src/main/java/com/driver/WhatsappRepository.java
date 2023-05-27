package com.driver;

import java.util.*;

import org.springframework.stereotype.Repository;

@Repository
public class WhatsappRepository {

    // Assume that each user belongs to at most one group
    // You can use the below mentioned hashmaps or delete these and create your own.

    // Group map
    private HashMap<Group, List<User>> groupUserMap;

    // Group having messages
    private HashMap<Group, List<Message>> groupMessageMap;

    // Message and sender name
    private HashMap<Message, User> senderMap;

    // Group info
    private HashMap<Group, User> adminMap;

    // Registered numbers
    private HashSet<String> userMobile;
    private HashMap<String, User> userHashMap;
    private HashMap<Integer, Message> messageHashMap;
    private int customGroupCount;
    private int messageId;

    public WhatsappRepository() {
        this.messageHashMap = new HashMap<>();
        this.groupMessageMap = new HashMap<Group, List<Message>>();
        this.groupUserMap = new HashMap<Group, List<User>>();
        this.senderMap = new HashMap<Message, User>();
        this.adminMap = new HashMap<Group, User>();
        this.userMobile = new HashSet<>();
        this.userHashMap = new HashMap<>();
        this.customGroupCount = 0;
        this.messageId = 0;
    }

    public String createUser(String name, String mobile) throws Exception {
        if (userMobile.contains(mobile))
            throw new Exception("User already exists");

        userHashMap.put(name, new User(name, mobile));
        userMobile.add(mobile);

        return "SUCCESS";
    }

    public Group createGroup(List<User> users) {
        if (users.size() == 2) {
            String grpName = users.get(1).getName();
            Group group = new Group(grpName, 2);
            groupUserMap.put(group, users);
            adminMap.put(group, users.get(0));
            return group;
        }

        if (users.size() > 2) {
            this.customGroupCount++;
            Group group = new Group("Group " + customGroupCount, users.size());
            groupUserMap.put(group, users);
            adminMap.put(group, users.get(0));
            return group;
        }
        return null;
    }

    public int createMessage(String content) {
        messageId++;
        messageHashMap.put(messageId, new Message(messageId, content));
        return messageId;
    }

    public String changeAdmin(User approver, User user, Group group) throws Exception {

        if (!adminMap.containsKey(group))
            throw new Exception("Group does not exist");

        User admin = adminMap.get(group);
        if (!admin.getName().equals(approver.getName()))
            throw new Exception("Approver does not have rights");

        for (User user1 : groupUserMap.get(group))
            if (user1 == user) {
                adminMap.remove(group);
                adminMap.put(group, user);
                return "SUCCESS";
            }
        throw new Exception("User is not a participant");
    }

    public int removeUser(User user) throws Exception {
        User userr = null;
        for (int i = 0; i < groupUserMap.size(); i++) {
            for (User users : groupUserMap.get(i)) {
                if (isSame(user, users)) {
                    userr = users;
                    break;
                }
            }
        }
        if (userr == null)
            throw new Exception("");

        for (User user1 : adminMap.values())
            if (isSame(user1, userr))
                throw new Exception("");

        // Delete the user here from 3 hashmaps

        userHashMap.remove(userr.getName());
        for (Map.Entry<Message, User> msg : senderMap.entrySet()) {
            if (msg.getValue().getName().equals(userr.getName()) && msg.getValue().getMobile()
                    .equals(userr.getMobile())) {
                senderMap.remove(msg.getKey());
            }
        }

        Group grpToBeRemoved = null;
        for (Map.Entry<Group, List<User>> mp : groupUserMap.entrySet()) {
            for (User user1 : mp.getValue()) {
                if (isSame(user1, userr)) {
                    grpToBeRemoved = mp.getKey();
                    break;
                }
            }
            if (grpToBeRemoved != null)
                break;
        }
        List<User> userList = groupUserMap.get(grpToBeRemoved);
        List<User> newList = null;

        for (User user1 : userList)
            if (!isSame(user1, userr))
                newList.add(user1);

        groupUserMap.put(grpToBeRemoved, newList);

        return 0;
    }

    public String findMessage(Date start, Date end, int k) {
        return "";
    }

    public int sendMessage(Message message, User sender, Group group) throws Exception {
        if (!groupUserMap.containsKey(group))
            throw new Exception("Group does not exist");
        for (User user : groupUserMap.get(group))
            if (isSame(user, sender)) {
                List<Message> messages = new ArrayList<>();
                if (groupMessageMap.containsKey(group))
                    messages = groupMessageMap.get(group);
                messages.add(message);
                groupMessageMap.put(group, messages);
                return messages.size();
            }
        throw new Exception("You are not allowed to send message");
    }

    public boolean isSame(User user1, User user2) {
        if (user1.getMobile().equals(user2.getMobile()) && user1.getName().equals(user2.getName()))
            return true;
        return false;
    }
}