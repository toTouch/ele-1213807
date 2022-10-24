package com.xiliulou.electricity.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2022-08-08-16:01
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpgradeNotifyMailVO {

    private Long id;
    /**
     * 通知邮箱json
     */
    private List<String> mail;
}
