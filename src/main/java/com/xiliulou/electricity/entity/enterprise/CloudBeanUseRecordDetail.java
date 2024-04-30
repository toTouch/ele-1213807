package com.xiliulou.electricity.entity.enterprise;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author maxiaodong
 * @date 2024/4/17 17:07
 * @desc
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_cloud_bean_use_record_detail")
public class CloudBeanUseRecordDetail {
    
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 用户id
     */
    private Long uid;
    
    /**
     * 企业id
     */
    private Long enterpriseId;
    
    /**
     * 订单id
     */
    private String orderId;
    
    /**
     * 开始时间
     */
    private Long startTime;
    
    /**
     * 结束时间
     */
    private Long endTime;
    
    /**
     * 使用天数
     */
    private Integer totalUseDay;
    
    /**
     * 租电详情id
     */
    private String rentRecordDetail;
    
    /**
     * 云豆使用记录Id
     */
    private Long cloudBeanUseRecordId;
    
    /**
     * 租户Id
     */
    private Integer tenantId;
    
    /**
     * 删除标记(0-未删除，1-已删除)
     */
    private Integer delFlag;
    
    /**
     * 开始时间
     */
    private Long createTime;
    
    /**
     * 结束时间
     */
    private Long updateTime;
}
