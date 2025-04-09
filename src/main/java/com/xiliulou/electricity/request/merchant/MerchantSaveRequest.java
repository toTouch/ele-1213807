package com.xiliulou.electricity.request.merchant;

import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.electricity.validator.UpdateGroup;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * @author maxiaodong
 * @date 2024/2/6 11:17
 * @desc 商户保存请求
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MerchantSaveRequest {
    @NotNull(message = "id不能为空", groups = {UpdateGroup.class})
    private Long id;
    
    /**
     * 商户名称
     */
    @Size(max = 10, message = "商户名称字数超出最大限制10字")
    @NotBlank(message = "商户名称不能为空", groups = {CreateGroup.class, UpdateGroup.class})
    private String name;
    
    /**
     * 加盟商id
     */
    @NotNull(message = "加盟商不能为空", groups = {CreateGroup.class, UpdateGroup.class})
    private Long franchiseeId;
    
    /**
     * 联系方式
     */
    @Size(max = 15, message = "联系方式称字数超出最大限制15字")
    @NotBlank(message = "联系方式不能为空", groups = {CreateGroup.class, UpdateGroup.class})
    private String phone;
    
    /**
     * 绑定场地集合
     */
    private List<Long> placeIdList;
    
    /**
     * 商户等级Id
     */
    @NotNull(message = "商户等级不能为空", groups = {CreateGroup.class, UpdateGroup.class})
    private Long merchantGradeId;
    
    /**
     * 渠道员Id
     */
    private Long channelEmployeeUid;
    
    /**
     * 等级自动升级(1-关闭， 0-开启)
     */
    @NotNull(message = "等级自动升级不能为空", groups = {CreateGroup.class, UpdateGroup.class})
    private Integer autoUpGrade;
    
    /**
     * 状态：0-启用，1-禁用
     */
    @Range(min = 0, max = 1, message = "状态不存在")
    @NotNull(message = "状态不能为空", groups = {CreateGroup.class, UpdateGroup.class})
    private Integer status;
    
    /**
     * 站点代付权限：0-开启，1-关闭
     */
    @Range(min = 0, max = 1, message = "站点代付权限不存在")
    @NotNull(message = "站点代付权限不能为空", groups = {CreateGroup.class, UpdateGroup.class})
    private Integer enterprisePackageAuth;
    
    /**
     * 邀请权限：0-开启，1-关闭
     */
    @Range(min = 0, max = 1, message = "邀请权限不存在")
    @NotNull(message = "邀请权限不能为空", groups = {CreateGroup.class, UpdateGroup.class})
    private Integer inviteAuth;
    
    /**
     * 会员代付权限 0：关，1：开
     */
    private Integer purchaseAuthority;
    
    /**
     * 企业套餐id集合
     */
    private List<Long> enterprisePackageIdList;
    
    /**
     * 企业id
     */
    private Long enterpriseId;
    
    /**
     * 加盟商绑定的数据权限的加盟商id
     */
    private List<Long> bindFranchiseeIdList;

    /**
     * 站点代付时间限制
     */
    @Range(min = 0, message = "站点代付限制时间不能小于零")
    @NotNull(message = "站点代付限制时间不能为空", groups = {CreateGroup.class, UpdateGroup.class})
    private Integer payTimeLimit;
}
