package com.xiliulou.electricity.query;

import com.xiliulou.electricity.validator.UpdateGroup;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-02-02-14:04
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FaceidResultQuery {

    @NotBlank(message = "token不能为空!")
    private String token;

    @NotNull(message = "核验结果不能为空!")
    private Boolean verifyDone;

}
