package com.xiliulou.electricity.query;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * @Auther: benyun
 * @Date: 2019/11/8 10:30
 * @Description:
 */
@Data
public class BatteryExcelQuery {
    
    /**
     * 电池名称
     */
    @ExcelProperty(index = 0)
    private String sn;
    
    /**
     * 电池型号
     */
    @ExcelProperty(index = 1)
    private String model;
    
    /**
     * 电压
     */
    @ExcelProperty(index = 2)
    private Integer voltage;
    
    /**
     * 电池容量,单位(mah)
     */
    @ExcelProperty(index = 3)
    private Integer capacity;
    
	/***
	 * @description 最大导入数据不能超过2000
	 * @date 2023/10/18 10:38:44
	 * @author HeYafeng
	 */
    public static final Integer EXCEL_MAX_COUNT_TWO_THOUSAND = 2000;
    
}
