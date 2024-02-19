package com.xiliulou.electricity.entity.merchant;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author BaoYu
 * @description:
 * @date 2024/2/18 21:20
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_merchant_employee")
public class MerchantEmployee {

    private Integer id;
    
    private Long uid;
    
    private Integer status;
    
    private Long merchantUid;
    
    private Long placeId;
    
    private Long tenantId;
    
    private Integer delFlag;
    
    private String remark;
    
    private Long createTime;
    
    private Long updateTime;

}
