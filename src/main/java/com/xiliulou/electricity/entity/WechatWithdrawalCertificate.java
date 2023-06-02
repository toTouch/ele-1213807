package com.xiliulou.electricity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;


/**
 * @author Mr. wang
 * @date: 2023年5月22日
 * desc: 微信提现证书内容实体类
 */
@Data
@TableName("t_wechat_withdrawal_certificate")
@Accessors(chain = true)
public class WechatWithdrawalCertificate {
    
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    /**
     * 租户id
     */
    private Integer tenantId;
    
    /**
     * 证书二进制文件
     */
    private byte[] certificateValue;
    
    /**
     * 上传时间
     */
    private Long uploadTime;
}
