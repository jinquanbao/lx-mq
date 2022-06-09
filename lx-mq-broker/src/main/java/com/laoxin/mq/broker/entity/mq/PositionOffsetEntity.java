package com.laoxin.mq.broker.entity.mq;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class PositionOffsetEntity {

    String id;

    private long lastOffsetId;

    private long currentOffsetId;

    private LocalDateTime updateTime;
}
