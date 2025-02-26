package com.xiliulou.electricity.entity.merchant;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 商户提现使用旧配置信息表(TMerchantWithdrawOldConfigInfo)实体类
 *
 * @author maxiaodong
 * @since 2025-02-13 17:43:57
 */
@Data
@TableName("t_merchant_withdraw_old_config_info")
public class MerchantWithdrawOldConfigInfo implements Serializable {
    private static final long serialVersionUID = -70940135637647011L;

    private Long id;
    /**
     * 租户id
     */
    private Integer tenantId;

    /**
     * 加盟商id
     */
    private Long franchiseeId;

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

