package com.xiliulou.electricity.vo;

import lombok.Data;
@Data
public class XllThreadPoolExecutorServiceVO {

    private Long queueSize;

    private  Boolean  terminated;

    private  Boolean  shutdown;

    private Integer getActiveCount;

    private Long getCompletedTaskCount;

    private Integer getCorePoolSize;

    private Integer getPoolSize;

    private Integer getMaximumPoolSize;

    private Integer getLargestPoolSize;

    private Long getTaskCount;

    private Long getQueenSize;

    private callerInfoVO callerInfo;
}
