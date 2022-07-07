package com.laoxin.mq.client.stats;

public interface ProducerStatsRecorder extends ProducerStats{

    void incrementIntervalMsgOut(long num);

    void incrementIntervalMsgOutFailed(long num);

    void intervalCalculate();

    static ProducerStatsRecorder disabledInstance(){
        return ProducerStatsDisabled.INSTANCE;
    }
}
