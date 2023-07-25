package com.xiliulou.electricity.query;

import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.electricity.validator.UpdateGroup;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-07-24-19:44
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class InsuranceUserInfoQuery {

    @NotNull(message = "uid不能为空", groups = {CreateGroup.class, UpdateGroup.class})
    private Long uid;

    /**
     * 加盟商id
     */
    @NotNull(message = "加盟商id不能为空", groups = {CreateGroup.class})
    private Long franchiseeId;

    /**
     * 保险Id
     */
    @NotNull(message = "保险id不能为空", groups = {CreateGroup.class})
    private Integer insuranceId;

    /**
     * 保险过期时间
     */
    @NotNull(message = "保险过期时间不能为空", groups = {CreateGroup.class, UpdateGroup.class})
    private Long insuranceExpireTime;

    /**
     * 是否出险 0--未出险 1--已出险
     */
    private Integer isUse;

    /**
     * 保险类型
     */
    @NotNull(message = "保险类型不能为空", groups = {CreateGroup.class, UpdateGroup.class})
    private Integer type;

}
