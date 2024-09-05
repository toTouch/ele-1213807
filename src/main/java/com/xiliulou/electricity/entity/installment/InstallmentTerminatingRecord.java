package com.xiliulou.electricity.entity.installment;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @Description ...
 * @Author: SongJP
 * @Date: 2024/8/28 10:55
 */
@Data
@TableName("t_installment_terminating_record")
public class InstallmentTerminatingRecord {
    
    @TableId(type = IdType.AUTO, value = "id")
    private Long id;
    
    /**
     * 请求签约用户uid
     */
    private Long uid;
    
    /**
     * 请求签约号，唯一
     */
    private String externalAgreementNo;
    
    /**
     * 实际签约人姓名
     */
    private String userName;
    
    /**
     * 实际签约人手机号
     */
    private String mobile;
    
    /**
     * 分期套餐id
     */
    private Long packageId;
    
    /**
     * 分期套餐类型，0-换电，1-租车，2-车电一体
     */
    private Integer packageType;
    
    /**
     * 已支付金额
     */
    private BigDecimal paidAmount;
    
    /**
     * 审核状态
     */
    private Integer status;
    
    /**
     * 申请解约原因
     */
    private String reason;
    
    /**
     * 审核意见
     */
    private String opinion;
    
    /**
     * 审核人uid
     */
    private Long auditorId;
    
    private Integer tenantId;
    
    private Long franchiseeId;
    
    private Long createTime;
    
    private Long updateTime;
}
