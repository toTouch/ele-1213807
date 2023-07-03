package com.xiliulou.electricity.enums;

import com.xiliulou.electricity.enums.basic.BasicEnum;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 租车套餐适用类型枚举<br>
 * 实际代表哪类用户能用
 * <pre>
 *     1 - 全部用户
 *     2 - 新用户
 *     3 - 老用户
 * </pre>
 *
 * @author xiaohui.song
 **/
@Getter
@AllArgsConstructor
public enum ApplicableTypeEnum implements BasicEnum<Integer, String> {

    ALL(1, "全部"),
    NEW(2, "新租套餐"),
    OLD(3, "续租套餐"),
    ;

    private final Integer code;

    private final String desc;
}
