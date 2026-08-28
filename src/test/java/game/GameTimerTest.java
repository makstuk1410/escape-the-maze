package game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class GameTimerTest {

    @Test
    void updateTicksOncePerElapsedSecond() {
        GameTimer timer = new GameTimer(3);

        assertFalse(timer.update(0.9));
        assertTrue(timer.update(0.2));
        assertEquals(2, timer.getTimeLeft());
    }

    @Test
    void updateProcessesLongFrameWithoutLosingSeconds() {
        GameTimer timer = new GameTimer(5);

        assertTrue(timer.update(2.5));
        assertEquals(3, timer.getTimeLeft());
    }

    @Test
    void timeEndCallbackRunsOnlyWhenTimerReachesZero() {
        GameTimer timer = new GameTimer(1);
        AtomicInteger callbackCount = new AtomicInteger();
        timer.setOnTimeEnd(callbackCount::incrementAndGet);

        timer.update(1.0);
        timer.update(1.0);

        assertEquals(1, callbackCount.get());
    }

    @Test
    void resetClearsPartialSecond() {
        GameTimer timer = new GameTimer(5);
        timer.update(0.8);

        timer.reset(5);

        assertFalse(timer.update(0.3));
        assertEquals(5, timer.getTimeLeft());
    }

    @Test
    void negativeDurationIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new GameTimer(-1));
        assertThrows(IllegalArgumentException.class, () -> new GameTimer(1).reset(-1));
    }
}
