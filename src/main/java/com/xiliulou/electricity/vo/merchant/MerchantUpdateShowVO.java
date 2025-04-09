package com.xiliulou.electricity.vo.merchant;

import com.xiliulou.electricity.bo.merchant.MerchantEnterprisePackageBO;
import com.xiliulou.electricity.vo.enterprise.EnterprisePackageVO;
import lombok.Data;

import java.util.List;

/**
 * @author maxiaodong
 * @date 2024/2/11 22:36
 * @desc 商户编辑显示Vo
 */
@Data
public class MerchantUpdateShowVO {
    
    /**
     * id
     */
    private Long id;
    
    /**
     * 商户名称
     */
    private String name;
    
    /**
     * 加盟商id
     */
    private Long franchiseeId;
    
    /**
     * 加盟商名称
     */
    private String franchiseeName;
    
    /**
     * 手机号
     */
    private String phone;
    
    /**
     * 商户等级Id
     */
    private Long merchantGradeId;
    
    /**
     * 渠道员Id
     */
    private Long channelEmployeeUid;
    
    /**
     * 渠道员名称
     */
    private String channelUserName;
    
    /**
     * 绑定场地集合
     */
    private List<MerchantPlaceSelectVO> placeList;
    
    /**
     * 租户Id
     */
    private Long tenantId;
    
    /**
     * 等级自动升级(1-关闭， 0-开启)
     */
    private Integer autoUpGrade;
    
    /**
     * 状态：0-启用，1-禁用
     */
    private Integer status;
    
    /**
     * 站点代付权限：0-开启，1-关闭
     */
    private Integer enterprisePackageAuth;
    
    /**
     * 会员代付权限 0：关，1：开
     */
    private Integer purchaseAuthority;
    
    /**
     * 邀请权限：0-开启，1-关闭
     */
    private Integer inviteAuth;
    
    /**
     * 管理员id
     */
    private Long uid;
    
    /**
     * 企业id
     */
    private Long enterpriseId;
    
    /**
     * 删除标记(0-未删除，1-已删除)
     */
    private Integer delFlag;
    
    /**
     * 企业套餐id集合
     */
    private List<EnterprisePackageVO> enterprisePackageList;
    
    /**
     * 创建时间
     */
    private Long createTime;
    
    /**
     * 修改时间
     */
    private Long updateTime;
    
    /**
     * <p>
     *    Description: 第三方openid
     * </p>
    */
    private String openId;

    /**
     * 站点代付时间限制
     */
    private Integer payTimeLimit;
}
