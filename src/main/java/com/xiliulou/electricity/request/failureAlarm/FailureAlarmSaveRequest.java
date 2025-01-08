package com.xiliulou.electricity.request.failureAlarm;


import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.electricity.validator.UpdateGroup;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * @author maxiaodong
 * @description 故障告警设置 保存
 * @date 2023/12/15 11:34:19
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FailureAlarmSaveRequest {
    
    /**
     * 主键ID
     */
    @NotNull(message = "[主键ID]不能为空", groups = {UpdateGroup.class})
    private Long id;
    
    /**
     * 分类(1-故障， 2-告警)
     */
    @Range(min = 1, max = 2, message = "分类不存在")
    @NotNull(message = "分类不能为空", groups = {CreateGroup.class, UpdateGroup.class})
    private Integer type;
    
    /**
     * 等级(1- 一级， 2-二级，3- 三级，4 -四级)
     */
    @Range(min = 1, max = 4, message = "等级不存在")
    @NotNull(message = "等级不能为空", groups = {CreateGroup.class, UpdateGroup.class})
    private Integer grade;
    
    /**
     * 设备分类：1-电池  2-换电柜 3-平台通知
     */
    @Range(min = 1, max = 7, message = "等级不存在")
    @NotNull(message = "设备分类不能为空", groups = {CreateGroup.class, UpdateGroup.class})
    private Integer deviceType;
    
    /**
     * 信号量ID
     */
    @Size(max = 10, message = "信号量ID字数超出最大限制10字")
    @NotEmpty(message = "信号量ID", groups = {CreateGroup.class, UpdateGroup.class})
    private String signalId;
    
    /**
     * 信号量标准名
     */
    @Size(max = 50, message = "信号量标准名字数超出最大限制50字")
    @NotEmpty(message = "信号量标准名不能为空", groups = {CreateGroup.class, UpdateGroup.class})
    private String signalName;
    
    /**
     * 信号说明
     */
    @Size(max = 100, message = "信号说明字数超出最大限制100字")
    private String signalDesc;
    
    /**
     * 事件描述
     */
    @Size(max = 500, message = "事件描述字数超出最大限制100字")
    private String eventDesc;
   
    /**
     * 运营商可见(0-不可见， 1-可见)
     */
    @Range(min = 0, max = 1, message = "运营商可见不存在")
    @NotNull(message = "运营商可见不能为空", groups = {CreateGroup.class, UpdateGroup.class})
    private Integer tenantVisible;
    
    /**
     * 运作状态(0-启用， 1-禁用)
     */
    @Range(min = 0, max = 1, message = "运作状态不存在")
    @NotNull(message = "运作状态不能为空", groups = {CreateGroup.class, UpdateGroup.class})
    private Integer status;
    
    /**
     * 保护措施
     */
    @NotEmpty(message = "保护措施不能为空")
    private List<Integer> protectMeasureList;
}
