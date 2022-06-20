package com.xiliulou.electricity.query;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * @author: hrp
 * @Date: 2022/6/16 10:02
 * @Description:
 */
@Data
@Builder
public class EleBindCarRecordQuery {

    private Integer id;

    private Long size;
    private Long offset;

    /**
     * 操作人
     */
    private String operateName;

    /**
     * 绑定时间
     */
    private Long bindTime;

    /**
     * 车辆sn码
     */
    private String sn;

    /**
     * 车辆Id
     */
    private Integer carId;

    /**
     * 用户手机号
     */
    private String phone;

    private Integer tenantId;

}
