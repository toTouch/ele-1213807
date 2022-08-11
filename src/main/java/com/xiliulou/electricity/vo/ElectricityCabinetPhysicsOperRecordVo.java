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
    /**
     * 餐柜id
     */
    private Integer cupboardId;
    /**
     * 创建时间
     */
    private Long createTime;

    private Integer type;
    /**
     * 格挡
     */
    private Integer cellNo;
    /**
     * 操作状态 0--初始化 1--成功,2--失败
     */
    private Integer status;
    /**
     * 消息
     */
    private String msg;

    private Long uid;

    private String username;


    private String name;
}
