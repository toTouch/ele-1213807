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
     * 模块(1- 主板， 2- 子板，3- 电池，4 -电池异常消失，5 -车辆，6-充电器，7-BMS)
     */
    @Range(min = 1, max = 7, message = "等级不存在")
    @NotNull(message = "等级不能为空", groups = {CreateGroup.class, UpdateGroup.class})
    private Integer model;
    
    /**
     * 名称
     */
    @Size(max = 15, message = "名称字数超出最大限制15字")
    @NotEmpty(message = "名称不能为空", groups = {CreateGroup.class, UpdateGroup.class})
    private String name;
    
    
    /**
     * 错误码
     */
    @Range(min = 1000000000, max = 2147483647, message = "错误码不符合规定")
    @NotNull(message = "错误码不能为空", groups = {CreateGroup.class, UpdateGroup.class})
    private Integer errorCode;
    
    /**
     * 触发规则
     */
    @NotEmpty(message = "触发规则不能为空", groups = {CreateGroup.class, UpdateGroup.class})
    @Size(max = 50, message = "触发规则字数超出最大限制50字")
    private String triggerRules;
    
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
