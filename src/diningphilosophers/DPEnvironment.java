package diningphilosophers;

import platform.Environment;

public class DPEnvironment extends Environment{
    private final int nb_philosophers;
    private final boolean[] forks;
    private int thoughts = 0;

    public DPEnvironment(int nb_philosophers){
        this.nb_philosophers = nb_philosophers;
        forks = new boolean[nb_philosophers];

        for (int i = 0; i < nb_philosophers; i++){
            forks[i] = true;
        }
    }

    public int getNbPhilosophers(){
        return nb_philosophers;
    }

    public boolean forkAvailable(int position){
        return forks[position];
    }

    public synchronized boolean take(int f){
        if(forks[f]){
            forks[f] = false;
            return true;
        }
        else{
            return false;
        }
    }

    public synchronized void  drop(int f){
        System.out.println("Fork dropped");
        forks[f] = true;
    }

    public synchronized void think(String name){
        thoughts++;
        System.out.println(name + " produced idea # "+thoughts);
    }
}