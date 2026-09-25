package platform;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

/**
 * 
 * Generic Agent running as its own thread
 */

public abstract class Agent extends Thread {

    public static final int DELAY = 300;
    
    private final String name;
    private final Environment env;
    private final Queue<Message> mailBox;

    private boolean running = true;
    private boolean actionResult = false;

    protected Action previousAction = null;

    protected Agent(String name, Environment env){
        this.name = name;
        this.env = env;
        this.mailBox = new LinkedList<>();
        env.registerAgent(this);
    }

    public String getAgentName(){
        return name;
    }

    public Environment getEnvironment(){
        return env;
    }

    public void stopAgent(){
        running = false;
    }

    public void sendMessage(Message message){
        env.sendMessage(message);
    };

    public void receiveMessage(Message message) {
        this.mailBox.add(message); 
    }
    public ArrayList<Message> getMessages() {
        ArrayList<Message> messages = new ArrayList<>();
        synchronized (mailBox) {
            messages.addAll(mailBox);
            mailBox.clear();
        }
        return messages;
    }

    protected abstract void perceive(boolean previousActionResult);

    protected abstract Action deliberate();

    @Override 
    public void run(){
        while (running) {
            try {
                perceive(actionResult);
                Action action = deliberate();
                actionResult = action.act(env);
                previousAction = action;
                Thread.sleep(DELAY);
            } catch(InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}