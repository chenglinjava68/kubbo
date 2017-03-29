/**
 * 
 */
package com.sogou.map.kubbo.metrics;

import com.sogou.map.kubbo.common.AbstractAttributable;

/**
 * @author liufuliang
 *
 */
public class FrequencyElapsedRecorder extends AbstractAttributable implements Metric {
    Counter frequency;
    Counter elapsed;
    
    public FrequencyElapsedRecorder(){
        frequency = new Counter();
        elapsed = new Counter();
    }
    
    public void mark(long time){
        frequency.inc();
        elapsed.inc(time);
    }
    
    public long frequency(){
        return frequency.count();
    }
    
    public long elapsed(){
        return elapsed.count();
    }
    
    public void clear(long frequencyToDec, long costToDec){
        frequency.dec(frequencyToDec);
        elapsed.dec(costToDec);
    }
}
