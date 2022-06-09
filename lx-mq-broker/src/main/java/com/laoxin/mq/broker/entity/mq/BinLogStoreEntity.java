package com.laoxin.mq.broker.entity.mq;


import lombok.Data;

import java.time.LocalDateTime;

//表名：destination + "_" + tenantId
@Data
public class BinLogStoreEntity {

    private long id;
    private long serverId;
    private String logfileName;
    private long logfileOffset;
    private String eventType;
    private String schemaName;
    private String tableName;
    private String beforeDatas;
    private String afterDatas;
    private String ddlSql;
    private long executeTime;
    private String pkNames;
    private long tenantId;
    private String txId;
    private LocalDateTime createTime;

}
