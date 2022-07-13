package com.xiliulou.electricity.query;

import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.electricity.validator.UpdateGroup;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class MemberCardOrderAddAndUpdate {
    /**
     * 套餐Id
     */
    @NotNull(message = "套餐id不能为空!")
    private Integer memberCardId;

    /**
     * 用户Id
     */
    @NotNull(message = "用户id不能为空!")
    private Long uid;

    /**
     * 月卡剩余天数
     */
    private Integer validDays;

    /**
     * 最大使用次数
     */
    private Long maxUseCount;

    public static final Integer ZERO_VALIdDAY_MEMBER_CARD = 0;

}
