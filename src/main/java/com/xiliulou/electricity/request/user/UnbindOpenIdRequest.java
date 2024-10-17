package com.xiliulou.electricity.request.user;

import com.xiliulou.electricity.validator.UpdateGroup;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * Description: UpdateUserPhoneRequest
 *
 * @author zhangyongbo
 * @version 1.0
 * @since 2024/1/5 10:46
 */
@Data
public class UnbindOpenIdRequest {
    
    @NotNull(message = "uid不能为空!", groups = {UpdateGroup.class})
    private Long uid;
    
    private Integer source;
}