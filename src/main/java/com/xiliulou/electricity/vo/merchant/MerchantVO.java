package com.xiliulou.electricity.vo.merchant;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author maxiaodong
 * @date 2024/2/11 22:36
 * @desc
 */
@Data
public class MerchantVO {
    
    private Long id;
    
    private String name;
    
    private String phone;
    
    private String franchiseeName;
    
    private String gradeName;
    
    private String channelName;
    
    private Integer placeCount;
    
    private Integer cabinetCount;
    
    private Integer userCount;
    
    private BigDecimal balance;
    
    private BigDecimal withdrawAmount;
    
    private String status;
    
    private List<Long> placeIdList;
    
    private List<Long> cabinetIdList;
    
    /**
     * 站点代付权限：1-开启，0-关闭
     */
    private Integer enterprisePackageAuth;
    
    /**
     * 会员代付权限 0：关，1：开
     */
    private Integer purchaseAuthority;
    
    /**
     * 企业套餐id
     */
    private List<Long> packageIdList;
}
