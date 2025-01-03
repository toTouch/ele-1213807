package com.xiliulou.electricity.vo.userinfo;

import com.xiliulou.electricity.vo.enterprise.EnterpriseChannelUserVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author HeYafeng
 * @description 运维小程序换电会员列表
 * @date 2025/1/3 09:55:43
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserBasicInfoEleProVO {
    
    private String name;
    
    private String phone;
    
    /**
     * 加盟商id
     */
    private Long franchiseeId;
    
    
    private String franchiseeName;
    
    /**
     * 门店id
     */
    private Long storeId;
    
    /**
     * 门店名称
     */
    private String storeName;
    
    /**
     * 电池押金状态
     */
    private Integer batteryDepositStatus;
    
    private Integer modelType;
    
    private EnterpriseChannelUserVO enterpriseChannelUserInfo;
    
}
