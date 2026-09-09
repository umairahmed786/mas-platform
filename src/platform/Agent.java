package platform;

/**
 * 
 * Generic Agent running as its own thread
 */

public abstract class Agent extends Thread {

    public static final int DELAY = 300;
    
    private final String name;
    private final Environment env;

    private boolean running = true;
    private boolean actionResult = false;

    protected Action previousAction = null;

    protected Agent(String name, Environment env){
        this.name = name;
        this.env = env;
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