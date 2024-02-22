package com.xiliulou.electricity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @author maxiaodong
 * @description 新故障告警信息
 * @date 2023/12/26 11:55:39
 */
@TableName("t_ele_hardware_failure_cabinet_msg")
@Data
public class EleHardwareFailureCabinetMsg {
    
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
