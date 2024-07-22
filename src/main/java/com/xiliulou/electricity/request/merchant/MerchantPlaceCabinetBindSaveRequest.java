package com.xiliulou.electricity.request.merchant;

import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.electricity.validator.UpdateGroup;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
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
public class MerchantPlaceCabinetBindSaveRequest {
    @NotNull(message = "id不能为空", groups = {UpdateGroup.class})
    private Long id;
    
    @NotNull(message = "场地Id不能为空", groups = {CreateGroup.class})
    private Long placeId;
    
    /**
     * 绑定时间
     */
    @NotNull(message = "开始时间不能为空", groups = {CreateGroup.class})
    private Long bindTime;
    
    /**
     * 绑定换电柜
     */
    @NotNull(message = "绑定换电柜不能为空", groups = {CreateGroup.class})
    private Integer cabinetId;
    
    /**
     * 绑定时间
     */
    @NotNull(message = "结束时间不能为空", groups = {UpdateGroup.class})
    private Long unBindTime;
    
    /**
     * 登录用户绑定加盟商
     */
    private List<Long> bindFranchiseeIdList;
    
}
