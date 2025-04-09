package com.xiliulou.electricity.entity.merchant;

import com.baomidou.mybatisplus.annotation.TableName;
import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.electricity.validator.UpdateGroup;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotNull;

/**
 * @author maxiaodong
 * @date 2024/2/2 11:20
 * @desc 商户
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_merchant")
public class Merchant {
    /**
     * id
     */
    private Long id;
    
    /**
     * 商户名称
     */
    private String name;
    
    /**
     * 加盟商id
     */
    private Long franchiseeId;
    
    /**
     * 手机号
     */
    private String phone;
    
    /**
     * 商户等级Id
     */
    private Long merchantGradeId;
    
    /**
     * 渠道员Id
     */
    private Long channelEmployeeUid;
    
    /**
     * 租户Id
     */
    private Integer tenantId;
    
    /**
     * 等级自动升级(1-关闭， 0-开启)
     */
    private Integer autoUpGrade;
    
    /**
     * 状态：0-启用，1-禁用
     */
    private Integer status;
    
    /**
     * 站点代付权限：0-开启，1-关闭
     */
    private Integer enterprisePackageAuth;
    
    /**
     * 会员代付权限 0：关，1：开
     */
    private Integer purchaseAuthority;
    
    /**
     * 邀请权限：0-开启，1-关闭
     */
    private Integer inviteAuth;
    
    /**
     * 是否存在场地费 0：是，1：否
     */
    private Integer existPlaceFee;
    
    /**
     * 管理员id
     */
    private Long uid;
    
    /**
     * 企业id
     */
    private Long enterpriseId;
    
    /**
     * 删除标记(0-未删除，1-已删除)
     */
    private Integer delFlag;
    
    /**
     * 创建时间
     */
    private Long createTime;
    
    /**
     * 修改时间
     */
    private Long updateTime;

    /**
     * 站点代付时间限制
     */
    private Integer payTimeLimit;
}
