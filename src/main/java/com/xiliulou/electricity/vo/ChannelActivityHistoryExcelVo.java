package com.xiliulou.electricity.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * @author zgw
 * @date 2023/3/23 19:14
 * @mood
 */
@Data
public class ChannelActivityHistoryExcelVo {
    
    @ExcelProperty("用户名")
    private String name;
    
    @ExcelProperty("用户手机号")
    private String phone;
    
    @ExcelProperty("邀请人用户名")
    private String inviterName;
    
    @ExcelProperty("邀请人用户手机号")
    private String inviterPhone;
    
    @ExcelProperty("渠道用户名")
    private String channelName;
    
    @ExcelProperty("渠道用户手机号")
    private String channelPhone;
    
    @ExcelProperty("状态")
    private String status;
    
    @ExcelProperty("创建时间")
    private String createTime;
}
