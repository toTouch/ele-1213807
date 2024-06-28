package com.xiliulou.electricity.entity.merchant;

import com.baomidou.mybatisplus.annotation.TableName;
import com.xiliulou.electricity.enums.PackageTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author HeYafeng
 * @description 扫码参与记录
 * @date 2024/2/6 17:18:41
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_merchant_join_record")
public class MerchantJoinRecord {
    
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 商户ID
     */
    private Long merchantId;
    
    /**
     * 渠道员uid
     */
    private Long channelEmployeeUid;
    
    /**
     * 场地ID
     */
    private Long placeId;
    
    /**
     * 邀请人uid
     */
    private Long inviterUid;
    
    /**
     * 邀请人类型：1-商户本人 2-场地员工
     */
    private Integer inviterType;
    
    /**
     * 参与人uid
     */
    private Long joinUid;
    
    /**
     * 参与开始时间
     */
    private Long startTime;
    
    /**
     * 参与过期时间
     */
    private Long expiredTime;
    
    /**
     * 参与状态 1-已参与，2-邀请成功，3-已过期，4-已失效(场景1：退租后 场景2：过了保护期，重新扫码后，需要将旧的记录改为已失效)
     */
    private Integer status;
    
    /**
     * 保护期过期时间
     */
    private Long protectionTime;
    
    /**
     * 邀请保护期是否已过期(0-未过期，1-已过期)
     */
    @Deprecated
    private Integer protectionStatus;
    
    /**
     * 删除标记(0-未删除，1-已删除)
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
    
    /**
     * 租户ID
     */
    private Integer tenantId;
    
    /**
     * 备注
     */
    private String remark;
    
    /**
     * 邀请人是否被修改：0-未修改（旧的记录），1-已修改（新的记录）
     */
    private Integer modifyInviter;
    
    /**
     * 加盟商ID
     */
    private Long franchiseeId;
    
    /**
     * 套餐ID
     */
    private Long packageId;
    
    /**
     * 套餐类型
     * @see PackageTypeEnum
     */
    private Integer packageType;
    
}
