package com.xiliulou.electricity.entity.lostuser;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * 流失用户首次拉新套餐购买记录
 *
 * @author maxiaodong
 * @since 2024-10-09 11:39:06
 */

@Data
@TableName("t_lost_user_first_order_record")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LostUserFirstOrderRecord {
    
    private Long id;
    
    /**
     * 用户uid
     */
    private Long uid;
    
    /**
     * 商户id
     */
    private Long merchantId;
    
    /**
     * 订单号
     */
    private String orderId;
    
    /**
     * 车/电订单表id
     */
    private Long orderSourceId;
    
    /**
     * 套餐类型：0-电，1-车
     */
    private Integer packageType;
    
    /**
     * 0--正常 1--删除
     */
    private Integer delFlag;
    
    /**
     * 租户id
     */
    private Integer tenantId;
    
    /**
     * 创建时间
     */
    private Long createTime;
    
    /**
     * 更新时间
     */
    private Long updateTime;
}

