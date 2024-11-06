package com.xiliulou.electricity.request.batteryrecycle;

import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.electricity.validator.UpdateGroup;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * @author maxiaodong
 * @date 2024/10/30 11:07
 * @desc
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BatteryRecycleSaveOrUpdateRequest {
    /**
     * 区域名称
     */
    @NotEmpty(message = "电池sn不能为空", groups = {CreateGroup.class})
    private List<String> batterySnList;
    
    /**
     * 回收原因
     */
    @NotBlank(message = "回收原因", groups = {CreateGroup.class})
    private String recycleReason;
    
    /**
     * 登录用户加盟商权限
     */
    private List<Long> bindFranchiseeIdList;
}
