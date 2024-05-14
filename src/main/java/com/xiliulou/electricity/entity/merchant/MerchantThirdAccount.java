package com.xiliulou.electricity.entity.merchant;

import com.baomidou.mybatisplus.annotation.TableName;
import com.xiliulou.electricity.enums.DelFlagEnum;
import lombok.Data;

import java.io.Serializable;

/**
 * @author: Ant
 * @className: MerchantThirdAccount
 * @description: 商户三方账户
 **/
@Data
@TableName("t_merchant_third_account")
public class MerchantThirdAccount implements Serializable {
    
    private static final long serialVersionUID = -7375182461062743318L;
    
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 创建时间，时间戳
     */
    private Long createTime;
    
    /**
     * 修改时间，时间戳
     */
    private Long updateTime;
    
    /**
     * 删除标识
     * <pre>
     *     0-正常
     *     1-删除
     * </pre>
     * @see DelFlagEnum
     */
    private Integer delFlag = DelFlagEnum.OK.getCode();
    
    /**
     * 商户ID
     */
    private Long merchantId;
}
