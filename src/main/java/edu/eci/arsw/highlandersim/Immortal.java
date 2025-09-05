package edu.eci.arsw.highlandersim;

import java.util.List;
import java.util.Random;

public class Immortal extends Thread {

    private ImmortalUpdateReportCallback updateCallback = null;

    private int health;

    private int defaultDamageValue;

    private final List<Immortal> immortalsPopulation;

    private final String name;

    private final Random r = new Random(System.currentTimeMillis());

    private boolean shouldStop;

    private final Object syncObject;
    private boolean isDead;

    public Immortal(String name, List<Immortal> immortalsPopulation, int health, int defaultDamageValue, ImmortalUpdateReportCallback ucb, Object syncObject) {
        super(name);
        this.updateCallback = ucb;
        this.name = name;
        this.immortalsPopulation = immortalsPopulation;
        this.health = health;
        this.defaultDamageValue = defaultDamageValue;

        this.shouldStop = false;
        this.syncObject = syncObject;
        this.isDead = false;
    }

    public void killInmortal() {
        this.isDead = true;
    }

    public boolean isDead() {
        return isDead || health <= 0;
    }

    public synchronized void stopThread() {
        this.shouldStop = true;
    }

    public synchronized void resumeThread() {
        this.shouldStop = false;
        notifyAll();
    }

    public void run() {

        while (true) {

            if (isDead()) {
                break; // Salir del bucle si está muerto
            }

            synchronized (this) {
                while (shouldStop) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

            Immortal im;

            int myIndex = immortalsPopulation.indexOf(this);

            int nextFighterIndex = r.nextInt(immortalsPopulation.size());

            //avoid self-fight
            if (nextFighterIndex == myIndex) {
                nextFighterIndex = ((nextFighterIndex + 1) % immortalsPopulation.size());
            }

            im = immortalsPopulation.get(nextFighterIndex);
            synchronized (syncObject) {

                if (!this.isDead() && !im.isDead()) {
                    this.fight(im);
                }
            }

            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }

    }

    public void fight(Immortal i2) {

        if (i2.getHealth() > 0) {
            // i2.changeHealth(i2.getHealth() - defaultDamageValue);
            synchronized (syncObject) {
                i2.changeHealth(i2.getHealth() - defaultDamageValue);
                this.health += defaultDamageValue;
                updateCallback.processReport("Fight: " + this + " vs " + i2 + "\n");
            }

        } else {

            updateCallback.processReport(this + " says:" + i2 + " is already dead!\n");
        }

    }

    public void changeHealth(int v) {
        health = v;
        if (health <= 0) {
            isDead = true;
        }
    }

    public int getHealth() {
        return health;
    }

    @Override
    public String toString() {

        return name + "[" + health + "]";
    }

    public boolean getFlag() {
        return this.shouldStop;
    }

}
