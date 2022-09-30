package com.xiliulou.electricity.query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2022-09-28-17:36
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PermissionTemplateQuery {


    private Integer type;

    private List<Long> permissionIds;

}
