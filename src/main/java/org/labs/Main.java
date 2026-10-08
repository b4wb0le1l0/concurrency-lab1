package org.labs;

import java.util.Scanner;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.ReentrantLock;

class DiningSimulation {
    private final int programmerCount;
    private int food;
    private final Semaphore waiters;

    DiningSimulation(int programmerCount, int food, int waiterCount) {
        if (programmerCount <= 0 || food <= 0 || waiterCount <= 0) {
            throw new IllegalArgumentException("All numbers must be greater than zero");
        }
        this.programmerCount = programmerCount;
        this.food = food;
        waiters = new Semaphore(waiterCount, true);
    }

    int[] run() throws InterruptedException {
        ReentrantLock[] spoons = new ReentrantLock[Math.max(2, programmerCount)];
        for (int i = 0; i < spoons.length; i++) {
            spoons[i] = new ReentrantLock(true);
        }

        Programmer[] programmers = new Programmer[programmerCount];
        for (int i = 0; i < programmerCount; i++) {
            int left = i;
            int right = (i + 1) % spoons.length;
            // Take the spoon with the smaller index first.
            programmers[i] = new Programmer(this,
                    spoons[Math.min(left, right)], spoons[Math.max(left, right)]);
        }

        for (Programmer programmer : programmers) {
            programmer.start();
        }
        try {
            for (Programmer programmer : programmers) {
                programmer.join();
            }
        } catch (InterruptedException e) {
            for (Programmer programmer : programmers) {
                programmer.interrupt();
            }
            for (Programmer programmer : programmers) {
                programmer.join();
            }
            throw e;
        }

        int[] result = new int[programmerCount];
        for (int i = 0; i < programmerCount; i++) {
            result[i] = programmers[i].eaten;
        }
        return result;
    }

    boolean bringFood() throws InterruptedException {
        waiters.acquire();
        try {
            return takeFood();
        } finally {
            waiters.release();
        }
    }

    private synchronized boolean takeFood() {
        if (food == 0) {
            return false;
        }
        food--;
        return true;
    }

    synchronized int getFood() {
        return food;
    }
}

class Programmer extends Thread {
    private final DiningSimulation dinner;
    private final ReentrantLock firstSpoon;
    private final ReentrantLock secondSpoon;
    int eaten;

    Programmer(DiningSimulation dinner,
               ReentrantLock firstSpoon, ReentrantLock secondSpoon) {
        this.dinner = dinner;
        this.firstSpoon = firstSpoon;
        this.secondSpoon = secondSpoon;
    }

    @Override
    public void run() {
        try {
            while (dinner.bringFood()) {
                firstSpoon.lockInterruptibly();
                try {
                    secondSpoon.lockInterruptibly();
                    try {
                        eaten++;
                    } finally {
                        secondSpoon.unlock();
                    }
                } finally {
                    firstSpoon.unlock();
                }
                // Discuss after eating, once both spoons are free.
                Thread.yield();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

public class Main {
    public static void main(String[] args) throws InterruptedException {
        Scanner scanner = new Scanner(System.in);
        int programmers = readNumber(scanner, "Number of programmers: ");
        int food = readNumber(scanner, "Number of portions: ");
        int waiters = readNumber(scanner, "Number of waiters: ");

        DiningSimulation dinner = new DiningSimulation(programmers, food, waiters);
        int[] eaten = dinner.run();
        int total = 0;
        int min = eaten[0];
        int max = eaten[0];
        for (int i = 0; i < eaten.length; i++) {
            System.out.println("Programmer " + (i + 1) + " ate: " + eaten[i]);
            total += eaten[i];
            min = Math.min(min, eaten[i]);
            max = Math.max(max, eaten[i]);
        }
        System.out.println("Total portions eaten: " + total);
        System.out.println("Portions left: " + dinner.getFood());
        double average = (double) total / eaten.length;
        System.out.println("Minimum portions: " + min);
        System.out.println("Maximum portions: " + max);
        System.out.printf("Average portions: %.2f%n", average);
        System.out.printf("Spread relative to average: %.2f%%%n",
                average == 0 ? 0 : (max - min) * 100.0 / average);
    }

    static int readNumber(Scanner scanner, String message) {
        while (true) {
            System.out.print(message);
            if (!scanner.hasNext()) {
                throw new IllegalArgumentException("Input ended");
            }
            if (scanner.hasNextInt()) {
                int number = scanner.nextInt();
                if (number > 0) {
                    return number;
                }
            } else {
                scanner.next();
            }
            System.out.println("Enter an integer greater than zero.");
        }
    }
}
