package platform;

import java.util.ArrayList;
import java.util.List;

public class Message {
    private final Agent sender;
    private final List<Agent> receivers = new ArrayList<>();
    private final String performative;
    private  String content;

    public Message(Agent sender, String performative){
        this.sender = sender;
        this.performative = performative;
    };

    public void addReceiver(Agent agent){
        this.receivers.add(agent);
    }

    public void addContent(String content){
        this.content = content;
    }

    public String getPerformative(){
        return this.performative;
    }

    public Agent getSender(){
        return  this.sender;
    }

    public List<Agent> getReceivers(){
        return  this.receivers;
    };

    public String getContent(){
        return  this.content;
    };
};

