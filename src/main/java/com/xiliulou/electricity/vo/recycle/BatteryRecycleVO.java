package com.xiliulou.electricity.vo.recycle;

/**
 * @author maxiaodong
 * @date 2024/10/30 15:06
 * @desc
 */

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author maxiaodong
 * @date 2024/10/30 15:06
 * @desc
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BatteryRecycleVO {
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
     * 柜机名称
     */
    private String electricityCabinetName;
    
    /**
     * 加盟商id
     */
    private Long franchiseeId;
    
    /**
     * 加盟商名称
     */
    private String franchiseeName;
    
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
     * 操作者
     */
    private String operatorName;
    
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
