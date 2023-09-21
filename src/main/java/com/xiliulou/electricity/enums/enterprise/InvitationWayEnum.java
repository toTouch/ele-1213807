package com.xiliulou.electricity.enums.enterprise;

import com.xiliulou.electricity.enums.basic.BasicEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author BaoYu
 * @description:
 * @date 2023/9/18 17:17
 */

@Getter
@AllArgsConstructor
public enum InvitationWayEnum implements BasicEnum<Integer, String> {

    INVITATION_WAY_FACE_TO_FACE(0, "面对面添加"),

    INVITATION_WAY_BY_PHONE(1, "手机号添加"),

    ;

    private final Integer code;

    private final String desc;

}
