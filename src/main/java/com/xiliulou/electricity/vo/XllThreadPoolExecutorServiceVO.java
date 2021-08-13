package com.xiliulou.electricity.vo;

import lombok.Data;
@Data
public class XllThreadPoolExecutorServiceVO {

    private Long queueSize;

    private  Boolean  terminated;

    private  Boolean  shutdown;

    private Integer activeCount;

    private Long completedTaskCount;

    private Integer corePoolSize;

    private Integer poolSize;

    private Integer maximumPoolSize;

    private Integer largestPoolSize;

    private Long taskCount;

    private Long queenSize;

    private StackTraceElement callerInfo;
}
