package com.xiliulou.electricity.request.merchant;

import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.electricity.validator.UpdateGroup;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * @author maxiaodong
 * @date 2024/2/6 11:17
 * @desc 场地保存请求
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MerchantPlaceSaveRequest {
    
    @NotNull(message = "id不能为空", groups = {UpdateGroup.class})
    private Long id;
    
    /**
     * 场地名称
     */
    @Size(max = 10, message = "场地名称字数超出最大限制10字")
    @NotEmpty(message = "场地名称不能为空", groups = {CreateGroup.class, UpdateGroup.class})
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
    private String phone;
    
    /**
     * 区域id
     */
    private Long merchantAreaId;
    
    /**
     * 场地地址
     */
    @Size(max = 255, message = "场地名称字数超出最大限制255字")
    @NotEmpty(message = "场地名称不能为空", groups = {CreateGroup.class, UpdateGroup.class})
    private String address;
    
    /**
     * 地址经度
     */
    private Double longitude;
    
    /**
     * 地址纬度
     */
    private Double latitude;
    
    /**
     * 加盟商用户绑定的加盟商
     */
    private List<Long> bindFranchiseeIdList;
}
