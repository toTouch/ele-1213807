package com.xiliulou.electricity.vo;

import lombok.Data;

/**
 * @author zgw
 * @date 2022/8/8 16:21
 * @mood
 */
@Data
public class ElectricityCabinetPhysicsOperRecordVo {
    private Long id;

    private Integer electricityCabinetId;

    private String command;

    private String cellNo;
    /**
     * 操作状态 0--初始化 1--成功,2--失败
     */
    private Integer status;

    private String msg;

    private Long uid;

    private String userName;
    /**
     * 操作类型 1--命令下发 2--柜机操作
     */
    private Integer operateType;

    private Long createTime;

    private String electricityCabinetName;
}
