package com.xiliulou.electricity.query;

import com.xiliulou.electricity.validator.CreateGroup;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Set;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-08-02-19:32
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatteryMemberCardAndInsuranceQuery {
    @NotNull(message = "月卡不能为空!", groups = {CreateGroup.class})
    private Long memberId;

    private Integer insuranceId;

    //三元组
    private String productKey;

    //三元组
    private String deviceName;


    //优惠券
    private Set<Integer> userCouponIds;
}
