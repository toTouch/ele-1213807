package com.xiliulou.electricity.vo;

import lombok.Data;

/**
 * @author : eclair
 * @date : 2022/8/2 09:32
 * 换电过程中给前端显示图片和内容的vo
 */
@Data
public class ExchangeOrderMsgShowVO {
    /**
     * 文字显示
     */
    private String status;
    /**
     * 图片显示
     */
    private Integer picture;
    /**
     * 是否需要自助开仓
     */
    private Integer selfOpenCell;

    public static final Integer PLACE_BATTERY_IMG = 1;
    public static final Integer TAKE_BATTERY_IMG = 2;
    public static final Integer EXCEPTION_IMG = 2;
}
