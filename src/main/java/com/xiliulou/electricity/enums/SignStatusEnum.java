package com.xiliulou.electricity.enums;

import com.xiliulou.electricity.enums.basic.BasicEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Author SongJinpan
 * @description: 电子签名签署状态枚举
 * @Date 2024/3/18 14:51
 */
@AllArgsConstructor
@Getter
public enum SignStatusEnum implements BasicEnum<Integer, String>  {
    
    SIGNED_INCOMPLETE(0, "签署了，未部署"),
    
    SIGNED_COMPLETED(1, "签署了，部署完成"),
    
    UNSIGNED(2, "未做电子签名");
    
    private final Integer code;
    
    private final String desc;
}
