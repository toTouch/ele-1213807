package com.xiliulou.electricity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author zgw
 * @date 2023/3/23 11:10
 * @mood
 */
@Data
@AllArgsConstructor
public class ChannelActivityCodeVo {
    
    private String code;
    
    private String invitePhone;
    
    private Integer type;
    
    /**
     * 类型 1--渠道人  2--邀请人
     */
    public static final Integer TYPE_CHANNEL = 1;
    
    public static final Integer TYPE_INVITE = 2;
}
