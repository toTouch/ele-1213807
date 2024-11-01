package com.xiliulou.electricity.vo.merchant;

import com.xiliulou.electricity.entity.ServicePhone;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author : eclair
 * @date : 2024/2/18 13:58
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MerchantLoginVO {
    
    /*
     * 手机号
     */
    private String phone;
    
    /*
     *用户姓名
     */
    private String username;
    
    // 授权的token
    private String token;
    
    /*
     * 租户ID
     */
    private Long tenantId;
    
    /*
     *绑定的商户/渠道ID
     */
    private Long bindBusinessId;
    
    /*
     * 用户UID
     */
    private Long uid;
    
    /*
     * 用户类型
     */
    private Integer userType;
    
    /**
     * 业务信息
     */
    private BusinessInfo businessInfo;
    
    /**
     * 0--正常 1--锁住
     */
    private Integer lockFlag;
    
    /**
     * 租户名称
     */
    private String tenantName;
    
    /**
     * 租户编号
     */
    private String tenantCode;
    
    /**
     * 客服电话
     */
    private String servicePhone;
    
    /**
     * 客服电话
     */
    private List<ServicePhone> servicePhones;
    
    public void setBusinessInfo(Integer enterprisePackageAuth, Integer purchaseAuthority) {
        this.businessInfo = new BusinessInfo(enterprisePackageAuth, purchaseAuthority);
    }
    
}

@Data
class BusinessInfo {
    
    /**
     * 站点代付权限：1-开启，0-关闭
     */
    private Integer enterprisePackageAuth;
    
    /**
     * 会员代付权限 0：关，1：开
     */
    private Integer purchaseAuthority;
    
    public BusinessInfo(Integer enterprisePackageAuth, Integer purchaseAuthority) {
        this.enterprisePackageAuth = enterprisePackageAuth;
        this.purchaseAuthority = purchaseAuthority;
    }
}
