package com.xiliulou.electricity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;


/**
 * @author Mr. wang
 * @date: 2023年5月16日
 * desc: 微信支付证书内容实体类
 */
@Data
@TableName("t_wechat_payment_certificate")
@Accessors(chain = true)
public class WechatPaymentCertificate {
    
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    /**
     * 租户id
     */
    private Integer tenantId;
    
    /**
     * 证书内容
     */
    private String certificateContent;
    
    /**
     * 上传时间
     */
    private Long uploadTime;
}
