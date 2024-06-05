package com.xiliulou.electricity.entity.enterprise;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author maxiaodong
 * @date 2024/4/15 16:08
 * @desc 用户租退电记录详情表
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_enterprise_rent_record_detail")
public class EnterpriseRentRecordDetail {
    /**
     * 主键
     */
    private Long id;
    
    /**
     * 用户id
     */
    private Long uid;
    
    /**
     * 订单id
     */
    private String orderId;
    
    /**
     * 租电订单表Id
     */
    private Long rentRecordId;
    
    /**
     * 企业id
     */
    private Long enterpriseId;
    
    /**
     * 租电时间
     */
    private Long rentTime;
    
    /**
     * 退电时间
     */
    private Long returnTime;
    
    /**
     * 退点日期
     */
    private String returnDate;
    
    /**
     * 创建时间
     */
    private Long createTime;
    
    /**
     * 修改时间
     */
    private Long updateTime;
    
    private Integer tenantId;
    
    private Integer delFlag;
}
