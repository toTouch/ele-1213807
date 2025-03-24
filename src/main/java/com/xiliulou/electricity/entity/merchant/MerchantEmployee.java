package com.xiliulou.electricity.entity.merchant;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author BaoYu
 * @description:
 * @date 2024/2/18 21:20
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_merchant_employee")
public class MerchantEmployee {
    
    /**
     * 商户员工ID
     */
    private Long id;
    
    /**
     * 商户员工uid
     */
    private Long uid;
    
    /**
     * 商户员工状态
     */
    /*private Integer status;*/
    
    /**
     * 商户UID
     */
    private Long merchantUid;
    
    /**
     * 场地ID
     */
    private Long placeId;
    
    /**
     * 租户ID
     */
    private Long tenantId;
    
    /**
     * 删除标记
     */
    private Integer delFlag;
    
    /**
     * 备注
     */
    private String remark;
    
    /**
     * 创建时间
     */
    private Long createTime;
    
    /**
     * 更新时间
     */
    private Long updateTime;


    /**
     * 邀请权限：0-开启，1-关闭
     */
    private Integer inviteAuth;

    /**
     * 站点代付权限：0-开启，1-关闭
     */
    private Integer enterprisePackageAuth;
}
