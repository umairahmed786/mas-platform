package platform;


public interface Action {

    boolean act(Environment e);

    String getActionName();
}