package com.xiliulou.electricity.query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-02-06-17:01
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FranchiseeMoveQuery {

    @NotNull(message = "电池类型不能为空!")
    private Integer batteryModel;


}
