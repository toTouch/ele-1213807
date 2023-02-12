package com.xiliulou.electricity.query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-02-12-14:11
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSourceQuery {
    /**
     * 设备名称
     */
    private String deviceName;
    /**
     * 设备产品
     */
    private String productKey;
    /**
     * 用户来源 1：扫码，2：邀请，3：其它
     */
    @NotNull(message = "用户来源不能为空")
    private Integer source;

}
