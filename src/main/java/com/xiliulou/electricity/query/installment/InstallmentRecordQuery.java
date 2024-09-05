package com.xiliulou.electricity.query.installment;

import com.xiliulou.electricity.validator.CreateGroup;
import lombok.Builder;
import lombok.Data;
import org.springframework.context.annotation.DeferredImportSelector;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @Description ...
 * @Author: SongJP
 * @Date: 2024/8/27 11:03
 */
@Data
@Builder
public class InstallmentRecordQuery {
    
    private Integer offset;
    
    private Integer size;
    
    /**
     * 请求签约号，签约订单编号
     */
    private String externalAgreementNo;
    
    /**
     * 请求签约用户uid
     */
    private Long uid;
    
    /**
     * 签约状态
     */
    private Integer status;
    
    private List<Integer> statuses;
    
    /**
     * 签约类型类型，0-换电，1-租车，2-车电一体
     */
    private Integer packageType;
    
    /**
     * 代扣时间范围
     */
    private Long endTime;
    
    private Integer tenantId;
    
    private Long franchiseeId;
    
    private List<Long> franchiseeIds;
    
    private List<Long> storeIds;
}
