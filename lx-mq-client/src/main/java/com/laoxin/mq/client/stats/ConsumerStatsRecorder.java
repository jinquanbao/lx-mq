package com.laoxin.mq.client.stats;

public interface ConsumerStatsRecorder extends ConsumerStats{

    void incrementIntervalMsgReceived(long num);

    void incrementIntervalMsgReceivedFailed(long num);

    void intervalCalculate();

    static ConsumerStatsRecorder disabledInstance(){
        return ConsumerStatsDisabled.INSTANCE;
    }
}
