/**
 * Singleton class to track for timeout of program.
 */
public class Timer {
    private static Timer timer; // Store timer instance
    private long startTime; // Start time of timer
    private long duration; // Duration before timeout

    private Timer() {
        startTime = System.currentTimeMillis();
        this.duration = 10 * 60 * 1000;
    }

    /**
     * Get the one and only timer instance.
     * @return Timer instance
     */
    public static Timer getInstance() {
        if (timer == null) {
            timer = new Timer();
        }
        return timer;
    }

    /**
     * Check if timer has exceed duration.
     * @return True timer has exceed the duration, otherwise false
     */
    public boolean hasTimeout() {
        return System.currentTimeMillis() - startTime > duration;
    }
}
