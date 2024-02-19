package com.xiliulou.electricity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @author maxiaodong
 * @date 2023/12/27 20:25
 * @desc
 */
@TableName("t_tenant_note")
@Data
public class TenantNote {
    /**
     * 主键Id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    /**
     * 租户Id
     */
    private Integer tenantId;
    /**
     * 短信次数
     */
    private Long noteNum;
    /**
     * 充值时间
     */
    private Long rechargeTime;
    /**
     * 创建时间
     */
    private Long createTime;
    
    /**
     * 创建时间
     */
    private Long updateTime;
    
}
