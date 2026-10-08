package org.labs;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import static org.junit.jupiter.api.Assertions.*;

@Timeout(10)
class DiningSimulationTest {
    @Test
    void allFoodIsEaten() throws InterruptedException {
        DiningSimulation dinner = new DiningSimulation(7, 10_000, 2);
        int[] eaten = dinner.run();
        int total = 0;
        for (int portions : eaten) {
            total += portions;
        }
        assertEquals(10_000, total);
        assertEquals(0, dinner.getFood());
    }

    @Test
    void everyProgrammerGetsFood() throws InterruptedException {
        int[] eaten = new DiningSimulation(7, 10_000, 2).run();
        assertEquals(7, eaten.length);
        for (int portions : eaten) {
            assertTrue(portions > 0);
        }
    }

    @Test
    void worksWithOneProgrammer() throws InterruptedException {
        int[] eaten = new DiningSimulation(1, 10, 1).run();
        assertArrayEquals(new int[]{10}, eaten);
    }

    @Test
    void worksWhenFoodIsNotEnoughForEveryone() throws InterruptedException {
        DiningSimulation dinner = new DiningSimulation(7, 3, 1);
        int[] eaten = dinner.run();
        int total = 0;
        for (int portions : eaten) {
            assertTrue(portions >= 0);
            total += portions;
        }
        assertEquals(3, total);
        assertEquals(0, dinner.getFood());
    }

    @Test
    void rejectsIncorrectParameters() {
        assertThrows(IllegalArgumentException.class, () -> new DiningSimulation(0, 10, 1));
        assertThrows(IllegalArgumentException.class, () -> new DiningSimulation(7, 0, 1));
        assertThrows(IllegalArgumentException.class, () -> new DiningSimulation(7, 10, 0));
    }
}
