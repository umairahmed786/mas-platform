package platform;
import java.util.HashMap;
import java.util.Map;
/**
 * 
 * Generic Environment in which Agent acts
 */

public abstract class Environment {
     /**
      * Inetially Empty comes from subclassing
      */

     private final Map<String, Agent> agents = new HashMap<>();

      public synchronized void registerAgent(Agent agent) {
            agents.put(agent.getAgentName(), agent);
      };

      public synchronized void sendMessage(Message message) {
            for (Agent receiver: message.getReceivers()){
                  receiver.receiveMessage(message);
            }
      }
}