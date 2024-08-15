package com.xiliulou.electricity.vo.merchant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author maxiaodong
 * @date 2024/2/11 22:36
 * @desc
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MerchantVO {
    
    private Long id;
    
    /**
     * 商户UID
     */
    private Long uid;
    
    /**
     * 商户名称
     */
    private String name;
    
    /**
     * 联系方式
     */
    private String phone;
    
    /**
     * 加盟商
     */
    private String franchiseeName;
    
    /**
     * 等级名称
     */
    private String gradeName;
    
    /**
     * 商户等级
     */
    private String merchantLevel;
    
    /**
     * 渠道员
     */
    private String channelUserName;
    /**
     * 场地数
     */
    private Integer placeCount;
    
    /**
     * 电柜数
     */
    private Integer cabinetCount;
    
    /**
     * 用户数
     */
    private Integer userCount;
    
    /**
     * 用户id集合
     */
    private List<Long> userIdList;
    
    /**
     * 可提现金额
     */
    private BigDecimal balance;
    
    /**
     * 已提现金额
     */
    private BigDecimal withdrawAmount;
    
    /**
     * 0-启用，1-禁用
     */
    private Integer status;
    
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
    
    /**
     * 邀请权限：邀请权限：0-开启，1-关闭
     */
    private Integer inviteAuth;
    
    /**
     * 企业id
     */
    private Long enterpriseId;
    
    /**
     * 商户等级Id
     */
    private Long merchantGradeId;
    
    /**
     * 企业云豆总数
     */
    private BigDecimal totalCloudBeanAmount;
    
    /**
     * 创建时间
     */
    private Long createTime;
    
    /**
     * 加盟商id
     */
    private Long franchiseeId;
    
    
}
