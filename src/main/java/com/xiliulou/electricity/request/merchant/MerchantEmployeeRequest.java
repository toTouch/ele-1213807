package com.xiliulou.electricity.request.merchant;

import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.electricity.validator.UpdateGroup;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.annotation.RegEx;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * @author BaoYu
 * @description:
 * @date 2024/2/19 9:20
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MerchantEmployeeRequest {
    
    /**
     * 商户员工ID
     */
    @NotNull(message = "id不能为空", groups = {UpdateGroup.class})
    private Long id;
    
    /**
     * 商户员工名称
     */
    @Size(min = 1, max = 20, message = "员工名称不合法", groups = {CreateGroup.class, UpdateGroup.class})
    @NotBlank(message = "员工名称不能为空", groups = {CreateGroup.class, UpdateGroup.class})
    private String name;
    
    /**
     * 商户员工电话
     */
    @Size(min = 11, max = 15, message = "员工手机号不合法", groups = {CreateGroup.class, UpdateGroup.class})
    @NotBlank(message = "员工手机号不能为空", groups = {CreateGroup.class, UpdateGroup.class})
    private String phone;
    
    /**
     * 商户员工uid
     */
    private Long uid;
    
    /**
     * 商户员工状态
     */
    @NotNull(message = "员工状态不能为空", groups = {CreateGroup.class, UpdateGroup.class})
    private Integer status;
    
    /**
     * 商户UID
     */
    private Long merchantUid;

    /**
     * 邀请权限：0-开启，1-关闭
     */
    private Integer inviteAuth;

    /**
     * 站点代付权限：0-开启，1-关闭
     */
    private Integer enterprisePackageAuth;
    
    /**
     * 场地ID
     */
    private Long placeId;
    
    /**
     * 租户ID
     */
    private Integer tenantId;
    
    /**
     * 删除标记
     */
    private Integer delFlag;
    
    /**
     * 备注
     */
    private String remark;
    
    /**
     * 创建时间
     */
    private Long createTime;
    
    /**
     * 更新时间
     */
    private Long updateTime;
    
    /**
     * 偏移量
     */
    private Long offset;
    
    /**
     * 取值数量
     */
    private Long size;
    
    
}
