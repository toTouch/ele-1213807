package com.xiliulou.electricity.entity.enterprise;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 用户使用记录表(EnterpriseRentRecord)实体类
 *
 * @author Eclair
 * @since 2023-10-10 20:03:40
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_enterprise_rent_record")
public class EnterpriseRentRecord {
    
    private Long id;
    
    private Long uid;
    
    /**
     * 租电套餐订单编码
     */
    private String rentMembercardOrderId;
    
    /**
     *退电套餐订单编码
     */
    private String returnMembercardOrderId;
    
    /**
     * 租电套餐id
     */
    private Long rentMid;
    
    /**
     * 退电套餐id
     */
    private Long returnMid;
    
    /**
     * 当前套餐订单到期时间
     */
    private Long orderExpireTime;
    
    /**
     * 租电时间
     */
    private Long rentTime;
    
    /**
     * 退电时间
     */
    private Long returnTime;
    
    /**
     * 创建时间
     */
    private Long createTime;
    
    /**
     * 修改时间
     */
    private Long updateTime;
    
    private Integer tenantId;
    
    public static final Integer DEL_NORMAL = 0;
    
    public static final Integer DEL_DEL = 1;
    
}
