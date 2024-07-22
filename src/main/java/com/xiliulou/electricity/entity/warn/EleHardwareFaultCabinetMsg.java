package com.xiliulou.electricity.entity.warn;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @author maxiaodong
 * @description 故障告警柜机日结
 * @date 2023/12/26 11:55:39
 */
@TableName("t_ele_hardware_fault_cabinet_msg")
@Data
public class EleHardwareFaultCabinetMsg {
    
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    /**
     * 换电柜Id
     */
    private Integer cabinetId;
    
    /**
     * 租户Id
     */
    private Integer tenantId;
    
    /**
     * 故障次数
     */
    private Integer failureCount;
    
    /**
     * 告警次数
     */
    private Integer warnCount;
    
    /**
     * 创建时间
     */
    private Long createTime;
}
