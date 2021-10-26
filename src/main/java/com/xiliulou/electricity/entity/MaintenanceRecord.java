package com.xiliulou.electricity.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * (MaintenanceRecord)实体类
 *
 * @author Eclair
 * @since 2021-09-26 14:07:39
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_maintenance_record")
public class MaintenanceRecord {

    @TableId(value = "id", type = IdType.AUTO)
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
    private String pic;
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
    private Long operateUid;
    /**
     * 处理人的备注
     */
    private String operateRemark;

    private Integer electricityCabinetId;

    private Integer tenantId;


    public static final String STATUS_CREATED = "CREATED";
    public static final String STATUS_PROCESSING = "PROCESSING";
    public static final String STATUS_COMPLETED = "COMPLETED";

}
