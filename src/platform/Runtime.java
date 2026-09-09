package platform;

import java.util.ArrayList;
import java.util.List;


public class Runtime {

    private final List<Agent> agents = new ArrayList<Agent>();
    private boolean running = false;

    public void addAgent(Agent a){
        agents.add(a);

        if(running){
            a.start();
        }
    }

    public void startAll(){
        running = true;
        for(Agent a : agents){
            a.start();
        }
    };

    public void stopAll(){
        running = false;
        for(Agent a : agents){
            a.stopAgent();
        }
    }
}