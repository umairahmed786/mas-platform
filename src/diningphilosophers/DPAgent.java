package diningphilosophers;

import java.util.ArrayList;

import platform.*;



public class DPAgent extends Agent {

    public static final int maxHunger = 5;

    public final int position;
    public final int rightPos;

    private int hunger = 0;
    private boolean left = false;
    private boolean right = false;
    private boolean eating = false;
    private boolean waitingThanks = false;
    private boolean waitingAgree = false;
    private Agent rightAgent = null;

    public DPAgent(int pos, String name, DPEnvironment env){
        super(name, env);
        this.position = pos;
        this.rightPos = (position + 1) % env.getNbPhilosophers();
        env.registerPhilosopher(this);
    };

    private final Action eat = new Action(){
        @Override
        public boolean act(platform.Environment env){
            System.out.println(getAgentName() + " is eating.");
            return true;
        } 

        @Override 
        public String getActionName(){
            return "eat";
        }
    };

    private final Action wait = new Action(){
        @Override 
        public boolean act(platform.Environment env){
            System.out.println(getAgentName() + " is waiting.");
            return true;
        } 

        @Override 
        public String getActionName(){
            return "wait";
        }
    };

    private final Action think = new Action() {
        @Override
        public boolean act(platform.Environment env) {
            ((DPEnvironment) env).think(getAgentName());
            return true;
        }
 
        @Override
        public String getActionName() {
            return "think";
        }
    };

    private final Action dropLeft = new Action() {
        @Override
        public boolean act(platform.Environment env) {
            ((DPEnvironment) env).drop(position);
            left = false;
            return true;
        }
 
        @Override
        public String getActionName() {
            return "drop left";
        }
    };

    private final Action dropRight = new Action() {
        @Override
        public boolean act(platform.Environment env) {
            ((DPEnvironment) env).drop(rightPos);
            right = false;
            return true;
        }
 
        @Override
        public String getActionName() {
            return "drop right";
        }
    };
 
    private final Action takeLeft = new Action() {
        @Override
        public boolean act(platform.Environment env) {
            System.out.println("Left Picked");
            return ((DPEnvironment) env).take(position);
        }
 
        @Override
        public String getActionName() {
            return "take left";
        }
    };
 
    private final Action takeRight = new Action() {
        @Override
        public boolean act(platform.Environment env) {
            return ((DPEnvironment) env).take(rightPos);
        }
 
        @Override
        public String getActionName() {
            return "take right";
        }
    };

    public final Action doNothing = new Action() {
        @Override
        public boolean act(platform.Environment env) {
            System.out.println("Doing Nothing");
            return true;
        }
        @Override
        public String getActionName() {
            return "do nothing";
        }
    };


    @Override
    protected void perceive(boolean previousActionResult) {
        if (previousAction == takeLeft) {
            left = previousActionResult;
        }
        if (previousAction == takeRight) {
            right = previousActionResult;
        }
    }



    @Override
    protected Action deliberate() {
        ArrayList<Message> messages = getMessages();

        if (!messages.isEmpty()) {
            Message message = messages.get(0);
            if (message.getPerformative().equals("yes of course!")) {
                waitingAgree = false;
                Message answer = new Message(this , "thank you!");
                answer.addReceiver(message.getSender());
                sendMessage(answer);
                if (left) {
                    return takeRight;
                }
            }
            else if (message.getPerformative().equals("can I please have the fork?")) {
                if (waitingAgree) {
                    Message answer = new Message(this, "thank you!");
                    answer.addReceiver(message.getSender());
                    sendMessage(answer);
                    waitingAgree = false;
                }
                waitingThanks = true;
                Message answer = new Message(this, "yes of course!");
                answer.addReceiver(message.getSender());
                sendMessage(answer);
                if (left) {
                    return dropLeft;
                }

            }
            else if (message.getPerformative().equals("thank you!")) {
                waitingThanks = false;
            }
        }
        if (waitingThanks || waitingAgree) {
            return doNothing;
        }
        else if (eating && hunger > 0) {
            hunger--;
            return eat;
        } else if (eating && left) {
            return dropLeft;
        } else if (eating && right) {
            eating = false;
            return dropRight;
        } else if (hunger < maxHunger) {
            hunger++;
            return think;
        } else if (!left) {
            return takeLeft;
        } else if (!right) {
            if (rightAgent == null) {
                rightAgent = ((DPEnvironment) getEnvironment()).getPhilosopher(rightPos);
            }
            Message request = new Message(this, "can I please have the fork?");
            request.addReceiver(rightAgent);
            sendMessage(request);
            waitingAgree = true;
            return doNothing;
        } else {
            eating = true;
            return eat;
        }
    }
}