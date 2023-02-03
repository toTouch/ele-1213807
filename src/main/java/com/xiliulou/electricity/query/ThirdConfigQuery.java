package com.xiliulou.electricity.query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-02-02-18:11
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ThirdConfigQuery {
//    @NotNull(message = "id不能为空")
    private Integer id;

    /**
     * 人脸核身商户号
     */
    @NotBlank(message = "人脸核身商户号不能为空")
    private String faceMerchantId;
}
