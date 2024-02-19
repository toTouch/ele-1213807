package com.xiliulou.electricity.entity.merchant;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author maxiaodong
 * @date 2024/2/2 11:43
 * @desc 场地表
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_merchant_place")
public class MerchantPlace {
    /**
     * id
     */
    private Long id;
    /**
     * id
     */
    private String name;
    /**
     * 加盟商id
     */
    private Long franchiseeId;
    /**
     * 联系方式
     */
    private String phone;
    /**
     * 区域id
     */
    private Long merchantAreaId;
    /**
     * 地址经度
     */
    private Double longitude;
    /**
     * 地址纬度
     */
    private Double latitude;
    /**
     * 场地地址
     */
    private String address;
    /**
     * 状态：1-启用，0-禁用
     */
    private Integer status;
    /**
     * 租户Id
     */
    private Integer tenantId;
    
    /**
     * 删除标记(0-未删除，1-已删除)
     */
    private Integer delFlag;
    
    /**
     * 创建时间
     */
    private Long createTime;
    
    /**
     * 修改时间
     */
    private Long updateTime;
}
