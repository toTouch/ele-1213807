package com.xiliulou.electricity.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * (ElectricityCabinetServerOperRecord)实体类
 *
 * @author Zgw
 * @since 2022-09-26 17:54:52
 */
@Data @AllArgsConstructor @NoArgsConstructor @Builder @TableName("t_electricity_cabinet_server_oper_record")
public class ElectricityCabinetServerOperRecord {

    private Long id;

    private Long eleServerId;

    private Long createUid;
    /**
     * 以前的服务开始时间
     */
    private Long oldServerBeginTime;
    /**
     * 以前的服务结束时间
     */
    private Long oldServerEndTime;
    /**
     * 修改后服务开始时间
     */
    private Long newServerBeginTime;
    /**
     * 修改后服务结束时间
     */
    private Long newServerEndTime;

    private Long createTime;

    public static final Integer DEL_NORMAL = 0;
    public static final Integer DEL_DEL = 1;

}
