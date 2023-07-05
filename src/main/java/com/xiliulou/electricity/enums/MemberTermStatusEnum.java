package com.xiliulou.electricity.enums;

import com.xiliulou.electricity.enums.basic.BasicEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 会员套餐期限状态枚举
 *
 * @author xiaohui.song
 **/
@Getter
@AllArgsConstructor
public enum MemberTermStatusEnum implements BasicEnum<Integer, String> {

    NORMAL(1, "正常"),
    ABNORMAL(2, "异常"),
    ;

    private final Integer code;

    private final String desc;

}
