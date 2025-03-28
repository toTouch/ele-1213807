package com.xiliulou.electricity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.xiliulou.electricity.enums.ElectricityConfigExtraEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @author HeYafeng
 * @description 系统设置扩展表
 * @date 2025/2/12 17:42:06
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_electricity_config_extra")
public class ElectricityConfigExtra {
    
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    private Integer tenantId;
    
    /**
     * 创建时间
     */
    private Long createTime;
    
    /**
     * 更新时间
     */
    private Long updateTime;
    
    /**
     * 用户账号注销开关：0-开启 1-关闭（默认）
     *
     * @see ElectricityConfigExtraEnum
     */
    private Integer accountDelSwitch;
    
    /**
     * 商户提现额度
     */
    private BigDecimal withdrawAmountLimit;
    
    /**
     * 删除/注销用户是否打标记开关：0-开启（默认） 1-关闭
     *
     * @see ElectricityConfigExtraEnum
     */
    private Integer delUserMarkSwitch;
}
