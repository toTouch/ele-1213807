package com.xiliulou.electricity.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * @author zgw
 * @date 2023/1/28 18:11
 * @mood
 */
@Data
public class ShareActivityRecordExcelVO {
    
    @ExcelProperty("活动名称")
    private String activityName;

    @ExcelProperty("邀请人")
    private String name;

    @ExcelProperty("邀请人手机号")
    private String phone;

    @ExcelProperty("领券数量")
    private String couponCount;
    
    @ExcelProperty("总邀请人数")
    private Integer count;

    @ExcelProperty("创建时间")
    private String createTime;
}
