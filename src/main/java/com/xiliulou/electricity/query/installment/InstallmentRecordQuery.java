package com.xiliulou.electricity.query.installment;

import com.xiliulou.electricity.entity.installment.InstallmentRecord;
import com.xiliulou.electricity.validator.CreateGroup;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.context.annotation.DeferredImportSelector;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

/**
 * @Description ...
 * @Author: SongJP
 * @Date: 2024/8/27 11:03
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InstallmentRecordQuery {
    
    private Integer offset;
    
    private Integer size;
    
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
    
    private List<Integer> statuses;
    
    private List<Long> franchiseeIds;
    
    private List<Long> storeIds;
}
