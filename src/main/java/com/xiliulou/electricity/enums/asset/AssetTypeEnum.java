package com.xiliulou.electricity.enums.asset;

import com.xiliulou.electricity.enums.basic.BasicEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Author zhangzhe
 * @date 2023/11/16 13:42
 * @Description:
 **/
@Getter
@AllArgsConstructor
public enum AssetTypeEnum implements BasicEnum<Integer, String> {
    
    // 资产类型
    ASSET_TYPE_CABINET(1, "柜机"),
    
    ASSET_TYPE_BATTERY(2, "电池"),
    
    ASSET_TYPE_CAR(3, "车辆");
    
    private final Integer code;
    
    private final String desc;
}
