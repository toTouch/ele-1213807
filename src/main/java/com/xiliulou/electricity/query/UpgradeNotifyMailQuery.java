package com.xiliulou.electricity.query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2022-08-08-15:37
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpgradeNotifyMailQuery {

    private Long id;
    /**
     * 通知邮箱json
     */
    private List<String> mail;

    private Long tenantId;

    private Long createTime;

    private Long updateTime;

}
