package com.xiliulou.electricity.query;

import com.xiliulou.electricity.validator.CreateGroup;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author zgw
 * @date 2023/3/22 18:29
 * @mood
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserChannelQuery {
    
    @NotEmpty(message = "用户手机号不能为空", groups = {CreateGroup.class})
    private String phone;
    
    @NotEmpty(message = "用户名不能为空", groups = {CreateGroup.class})
    private String name;

    private Long uid;

    private Integer tenantId;

    private Long size;

    private Long offset;

    private List<Long> franchiseeIds;

    private List<Long> storeIds;
}
