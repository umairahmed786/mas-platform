package diningphilosophers;
import platform.Runtime;

public class Main {
    public static void main(String[] args) {
        DPEnvironment table = new DPEnvironment(5);

        DPAgent kashir = new DPAgent(0, "Kashir", table);
        DPAgent umair = new DPAgent(1, "Umair", table);
        DPAgent farrukh = new DPAgent(2, "Farrukh", table);
        DPAgent asad = new DPAgent(3, "Asad", table);
        DPAgent ahad = new DPAgent(4, "Ahad", table);

        Runtime r = new Runtime();
        r.addAgent(kashir);
        r.addAgent(umair);
        r.addAgent(farrukh);
        r.addAgent(asad);
        r.addAgent(ahad);

        r.startAll();

        // Optional: let it run for a while then stop everyone cleanly.
        try { Thread.sleep(20000); } catch (InterruptedException e) {}
        r.stopAll();
    }
}