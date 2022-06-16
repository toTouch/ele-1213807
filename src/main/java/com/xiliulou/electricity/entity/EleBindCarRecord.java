package com.xiliulou.electricity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 绑定车辆记录表(TEleBindCarRecord)实体类
 *
 * @author makejava
 * @since 2022-06-16 10:17:06
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_ele_bind_car_record")
public class EleBindCarRecord {
    /**
     * 记录Id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 车辆sn码
     */
    private String sn;

    /**
     * 操作人
     */
    private String operateUser;

    /**
     * 绑定状态 0--绑定 1--解绑
     */
    private Integer status;

    /**
     * 绑定的用户
     */
    private String userName;

    /**
     * 绑定的用户手机号
     */
    private String phone;

    /**
     * 绑定的车辆型号
     */
    private String model;

    /**
     * 创建时间
     */
    private Long createTime;
    /**
     * 更新时间
     */
    private Long updateTime;

    /**
     * 租户id
     */
    private Integer tenantId;

    /**
     * 绑定单号
     */
    private String binCarNo;

    public static final Integer BIND_CAR = 0;
    public static final Integer NOT_BIND_CAR = 0;


}
