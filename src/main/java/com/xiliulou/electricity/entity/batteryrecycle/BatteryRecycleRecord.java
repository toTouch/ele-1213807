package com.xiliulou.electricity.entity.batteryrecycle;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 电池回收记录表(TBatteryRecycleRecord)实体类
 *
 * @author maxiaodong
 * @since 2024-10-30 10:47:48
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BatteryRecycleRecord implements Serializable {
    
    private static final long serialVersionUID = -50613078221261236L;
    
    private Long id;
    
    /**
     * 批次号
     */
    private String batchNo;
    
    /**
     * 柜机id
     */
    private Integer electricityCabinetId;
    
    /**
     * 加盟商id
     */
    private Long franchiseeId;
    
    /**
     * 电池id
     */
    private Long batteryId;
    
    /**
     * 电池sn
     */
    private String sn;
    
    /**
     * 仓门号
     */
    private String cellNo;
    
    /**
     * 状态：0-已录入   1-已入柜锁定
     */
    private Integer status;
    
    /**
     * 回收原因
     */
    private String recycleReason;
    
    /**
     * 0--正常 1--删除
     */
    private Integer delFlag;
    
    /**
     * 操作人id
     */
    private Long operatorId;
    
    /**
     * 租户id
     */
    private Integer tenantId;
    
    /**
     * 创建时间
     */
    private Long createTime;
    
    /**
     * 更新时间
     */
    private Long updateTime;
}

