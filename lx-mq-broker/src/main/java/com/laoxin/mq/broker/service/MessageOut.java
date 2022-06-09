package com.laoxin.mq.broker.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.concurrent.atomic.AtomicInteger;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class MessageOut {
    private long outTime;
    private AtomicInteger outCounter;

}
