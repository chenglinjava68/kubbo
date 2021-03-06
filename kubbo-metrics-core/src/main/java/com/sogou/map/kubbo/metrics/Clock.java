package com.sogou.map.kubbo.metrics;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

/**
 * An abstraction for how time passes.
 * @author liufuliang
 */
public abstract class Clock {
    public static final Clock DEFAULT = new UserTimeClock();

    /**
     * Returns the current time tick.
     *
     * @return time tick in nanoseconds
     */
    public abstract long getTick();

    /**
     * Returns the current time in milliseconds.
     *
     * @return time in milliseconds
     */
    public long getTime() {
        return System.currentTimeMillis();
    }


    /**
     * A clock implementation which returns the current time in epoch nanoseconds.
     */
    public static class UserTimeClock extends Clock {
        @Override
        public long getTick() {
            return System.nanoTime();
        }
    }

    /**
     * A clock implementation which returns the current thread's CPU time.
     */
    public static class CpuTimeClock extends Clock {
        private static final ThreadMXBean THREAD_MX_BEAN = ManagementFactory.getThreadMXBean();

        @Override
        public long getTick() {
            return THREAD_MX_BEAN.getCurrentThreadCpuTime();
        }
    }
}
