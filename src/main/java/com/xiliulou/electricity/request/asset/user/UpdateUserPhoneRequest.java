package com.xiliulou.electricity.request.asset.user;

import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.electricity.validator.UpdateGroup;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * Description: UpdateUserPhoneRequest
 *
 * @author zhangyongbo
 * @version 1.0
 * @since 2024/1/5 10:46
 */
@Data
public class UpdateUserPhoneRequest {
    
    @NotNull(message = "uid不能为空!", groups = {UpdateGroup.class})
    private Long uid;
    
    /**
     * 手机号
     */
    @NotEmpty(message = "手机号不能为空!", groups = {UpdateGroup.class})
    private String phone;
}