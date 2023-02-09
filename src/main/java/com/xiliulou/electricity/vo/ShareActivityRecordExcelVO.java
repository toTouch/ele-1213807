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
    
    @ExcelProperty("分享状态")
    private String status;
    
    @ExcelProperty("邀请人手机号")
    private String phone;
    
    @ExcelProperty("邀请人名称")
    private String name;
    
    @ExcelProperty("总邀请人数")
    private Integer count;
    
    @ExcelProperty("现邀请人数")
    private Integer availableCount;
    
    @ExcelProperty("创建时间")
    private String createTime;
}
