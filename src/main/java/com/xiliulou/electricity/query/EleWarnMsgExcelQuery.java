package com.xiliulou.electricity.query;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@Builder
public class EleWarnMsgExcelQuery {
//    @NotNull(message = "每页条数不能为空!")
//    private Long size;
//    @NotNull(message = "页码不能为空!")
//    private Long offset;
    @NotNull(message = "类型不能为空!")
    private Integer type;
    @NotNull(message = "开始时间不能为空!")
    private Long beginTime;
    @NotNull(message = "结束时间不能为空!")
    private Long endTime;
    /**
     * 格挡操作类型
     */
    private Integer operateType;
    /**
     * 格挡号
     */
    private Integer cellNo;
    /**
     * 电池名字
     */
    private String batteryName;

    private Long electricityCabinetId;

    private Integer tenantId;

    //导出类型 1 业务异常  2 柜机异常  3  格挡异常  4电池异常
    public static final int TYPE_BUSINESS_WARN=1;
    public static final int TYPE_CABINET_WARN=2;
    public static final int TYPE_CELL_WARN=3;
    public static final int TYPE_BATTERY_WARN=4;
}
