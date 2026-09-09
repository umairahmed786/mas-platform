package diningphilosophers;

import platform.Agent;
import platform.Action;



public class DPAgent extends Agent {

    public static final int maxHunger = 5;

    public final int position;
    public final int rightPos;

    private int hunger = 0;
    private boolean left = false;
    private boolean right = false;
    private boolean eating = false;

    public DPAgent(int pos, String name, DPEnvironment env){
        super(name, env);
        this.position = pos;
        this.rightPos = (position + 1) % env.getNbPhilosophers();
    };

    private final boolean rightForkAvailable(){
        return ((DPEnvironment) getEnvironment()).forkAvailable(rightPos);
    }

    private final Action eat = new Action(){
        @Override
        public boolean act(platform.Environment env){
            System.out.println(getAgentName() + "is eating.");
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
            System.out.println(getAgentName() + "is waiting.");
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
        if (eating && hunger > 0) {
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
            if(rightForkAvailable()){
                return takeLeft;
            } else{
                return wait;
            }
            
        } else if (!right) {
            return takeRight;
        } else {
            eating = true;
            return eat;
        }
    }
}