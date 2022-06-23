package com.laoxin.mq.broker.service;

public interface Task {

    //执行任务
    void doTask();

    //暂停任务
    boolean pause();

    //取消暂停
    boolean cancelPause();

    //是否正在运行
    boolean isRunning();

    //能否执行task
    boolean taskCanDo();


}
