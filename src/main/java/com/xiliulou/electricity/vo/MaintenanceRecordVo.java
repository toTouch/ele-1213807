package com.xiliulou.electricity.vo;

import lombok.Data;

/**
 * @author : eclair
 * @date : 2021/9/26 3:58 下午
 */
@Data
public class MaintenanceRecordVo {
    private Long id;
    /**
     * 状态： CREATED,PROCESSING,COMPLETED
     */
    private String status;
    /**
     * 上报人的备注
     */
    private String remark;
    /**
     * 维修类型
     */
    private String type;

    private Long createTime;

    private Long updateTime;
    /**
     * 图片地址
     */
    private String url;
    /**
     * 上报人
     */
    private Long uid;
    /**
     * 上报人手机号
     */
    private String phone;
    /**
     * 处理人
     */
    private Integer operateUid;
    /**
     * 处理人的备注
     */
    private String operateRemark;

    private Integer electricityCabinetId;

    private String electricityCabinetName;

}
