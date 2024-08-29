package com.xiliulou.electricity.entity.installment;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Description 签约记录表实体类
 * @Author: SongJP
 * @Date: 2024/8/26 11:13
 */
@Data
@Builder
@TableName("t_installment_record")
public class InstallmentRecord {
    
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
     * 签约状态
     */
    private Integer status;
    
    /**
     * 分期期数
     */
    private Integer installmentNo;
    
    /**
     * 已支付期数
     */
    private Integer paidInstallment;
    
    private Integer tenantId;
    
    private Long franchiseeId;
    
    private Long createTime;
    
    private Long updateTime;
}
