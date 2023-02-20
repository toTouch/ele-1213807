package com.xiliulou.electricity.query;

import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.electricity.validator.UpdateGroup;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-02-20-16:45
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FreeDepositDataQuery {

    private Long size;

    private Long offset;

    private String tenantName;

    private Long id;

    /**
     * 人脸核身次数
     */
    @Min(0)
    @Max(Integer.MAX_VALUE)
    @NotNull(message = "免押次数不能为空!", groups = {CreateGroup.class, UpdateGroup.class})
    private Integer freeDepositCapacity;
    /**
     * 充值时间
     */
    private Long rechargeTime;

    @NotNull(message = "租户不能为空!", groups = {CreateGroup.class, UpdateGroup.class})
    private Integer tenantId;
}
