package com.laoxin.mq.broker.stats;


public interface ProducerStatsRecorder extends ProducerStats {

    void incrementMsgInCounter(long num);

    void setRegisterTimestamp( long registerTimestamp);

    //发送速率 msg/s
    void setMsgRateIn( double msgRateIn);
    //上一次统计到现在发送到主题的消息总数
    void setMsgInCounter(long msgInCounter);

    //最近一次生产消息时间
    void setLastMsgInTimestamp( long lastMsgInTimestamp);

    //消费异常信息
    void setException(Throwable exception);
    //发生异常的时间
    void setExceptionTimestamp( long exceptionTimestamp);

    void intervalCalculate();

    static ProducerStatsRecorder disabledInstance(){
        return ProducerStatsDisabled.INSTANCE;
    }
}
