package com.xiliulou.electricity.vo;

import lombok.Data;

/**
 * @program: XILIULOU
 * @description:
 * @author: Mr.YG
 * @create: 2020-12-11 14:57
 **/
@Data
public class OwnMemberCardInfoVo {
    /**
     * 月卡过期时间
     */
    private Long memberCardExpireTime;
    /**
     * 剩余使用次数
     */
    private Long remainingNumber;


    private Long days;
    private Integer type;

}
