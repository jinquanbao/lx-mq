package com.laoxin.mq.broker.stats;


public interface ConsumerStatsRecorder extends ConsumerStats {

    void incrementMsgOutCounter(long num);

    void setAddress(String address);
    //消费者连接时间
    void setConnectedTimestamp(long connectedTimestamp);

    //消费者消费速率 msg/s
    void setMsgRateOut( double msgRateOut);

    void setMsgOutCounter(long msgOutCounter);

    //最近一次ack时间
    void setLastAckedTimestamp(long lastAckedTimestamp);
    //最近一次消费时间
    void setLastMsgOutTimestamp( long lastMsgOutTimestamp);
    //最近一次ack的位移
    void setLastAckedPosition(long lastAckedPosition);
    //最近一次发送给消费者消息位移
    void setLastMsgOutPosition(long lastMsgOutPosition);
    //消费异常信息
    void setException(Throwable exception);
    //发生异常的时间
    void setExceptionTimestamp( long exceptionTimestamp);

    void intervalCalculate();

    static ConsumerStatsRecorder disabledInstance(){
        return ConsumerStatsDisabled.INSTANCE;
    }
}
