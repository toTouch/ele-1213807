package com.xiliulou.electricity.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author : eclair
 * @date : 2022/4/12 09:32
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MaintenanceUserNotifyConfigVo {
    /**
     * 权限
     */
    private Integer permissions;
    /**
     * 手机号
     */
    private List<String> phones;
    /**
     * 二维码链接
     */
    private String qrUrl;
}
