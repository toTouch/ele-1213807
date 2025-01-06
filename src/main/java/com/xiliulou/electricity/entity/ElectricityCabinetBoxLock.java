package com.xiliulou.electricity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * 换电柜锁仓仓门表(TElectricityCabinetBoxLock)实体类
 *
 * @author renhang
 * @since 2024-12-31
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Accessors(chain = true)
@TableName("t_electricity_cabinet_box_lock")
public class ElectricityCabinetBoxLock {
    /**
     * Id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 换电柜名称
     */
    private String name;
    /**
     * 换电柜sn
     */
    private String sn;

    /**
     * 所属换电柜柜Id
     */
    private Integer electricityCabinetId;

    /**
     * 仓门号
     */
    private Integer cellNo;

    /**
     * 锁仓类型 0--人为禁用 1--系统禁用 2--待检中
     * @see com.xiliulou.electricity.enums.LockTypeEnum
     */
    private Integer lockType;

    /**
     * 锁仓原因
     * @see com.xiliulou.electricity.enums.LockReasonEnum
     */
    private Integer lockReason;

    /**
     * 锁仓/解锁时间
     */
    private Long lockStatusChangeTime;


    /**
     * 换电柜地址
     */
    private String address;


    private Long areaId;

    private Integer tenantId;

    private Long franchiseeId;

    private Long storeId;

    /**
     * 创建时间
     */
    private Long createTime;

    /**
     * 更新时间
     */
    private Long updateTime;

    public static final Integer DEL_NORMAL = 0;

    public static final Integer DEL_DEL = 1;

}
