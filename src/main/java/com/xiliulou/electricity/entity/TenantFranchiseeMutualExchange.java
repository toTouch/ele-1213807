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
 * (Franchisee)实体类
 *
 * @author lxc
 * @since 2020-11-25 11:00:14
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_tenant_franchisee_mutual_exchange")
@Accessors(chain =true)
public class TenantFranchiseeMutualExchange {
    
    /**
     * Id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    /**
     * 组合名称
     */
    private String combinedName;
    
    /**
     * 组合加盟商
     */
    private String combinedFranchisee;
    
    /**
     * 状态 0:禁用,1:启用
     */
    private Integer status;
    
    
    private Integer tenantId;
    
    /**
     * 0--正常 1--删除
     */
    private Integer delFlag;
    
    /**
     * 创建时间
     */
    private Long createTime;
    
    /**
     * 更新时间
     */
    private Long updateTime;
    
    
}
