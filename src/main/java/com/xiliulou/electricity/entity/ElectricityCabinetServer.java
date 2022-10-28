package com.xiliulou.electricity.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * (ElectricityCabinetServer)实体类
 *
 * @author zgw
 * @since 2022-09-26 11:40:32
 */
@Data @AllArgsConstructor @NoArgsConstructor @Builder @TableName("t_electricity_cabinet_server")
public class ElectricityCabinetServer {

    private Long id;
    /**
     * 绑定的柜机id
     */
    private Integer electricityCabinetId;

    private String productKey;

    private String deviceName;
    /**
     * 租户id
     */
    private Integer tenantId;
    /**
     * 服务开始时间
     */
    private Long serverBeginTime;
    /**
     * 服务结束时间
     */
    private Long serverEndTime;

    private Integer delFlag;

    private Long createTime;

    private Long updateTime;

    public static final Integer DEL_NORMAL = 0;
    public static final Integer DEL_DEL = 1;

}
