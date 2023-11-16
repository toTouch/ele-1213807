package com.xiliulou.electricity.query;

import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.electricity.validator.UpdateGroup;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author HeYafeng
 * @description 新增邀请人活动
 * @date 2023/11/13 12:50:56
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InvitationActivityUserSaveQuery {
    
    /**
     * 邀请人uid
     */
    @NotNull(message = "uid不能为空", groups = {CreateGroup.class, UpdateGroup.class})
    private Long uid;
    
    /**
     * 活动id列表
     */
    private List<Long> activityIds;
    
}
