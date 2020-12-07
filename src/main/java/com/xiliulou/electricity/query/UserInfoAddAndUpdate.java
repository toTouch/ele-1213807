package com.xiliulou.electricity.query;

import com.baomidou.mybatisplus.annotation.TableId;
import com.xiliulou.electricity.validator.UpdateGroup;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * 用户列表(TUserInfo)实体类
 *
 * @author makejava
 * @since 2020-12-07 15:00:00
 */
@Data
public class UserInfoAddAndUpdate {
    @NotNull(message = "Id不能为空!", groups = {UpdateGroup.class})
    private Long id;

    private Long uid;
    /**
    * 用户姓名
    */
    @NotEmpty(message = "用户姓名不能为空!", groups = {UpdateGroup.class})
    private String name;
    /**
    * 手机号
    */
    private String phone;
    /**
    * 门店Id
    */
    @NotNull(message = "门店Id不能为空!", groups = {UpdateGroup.class})
    private Long storeId;
    /**
    * 身份证号
    */
    @NotEmpty(message = "身份证号不能为空!", groups = {UpdateGroup.class})
    private String idNumber;
    /**
    * 初始电池编号
    */
    @NotEmpty(message = "初始电池编号不能为空!", groups = {UpdateGroup.class})
    private String initElectricityBatterySn;
    /**
    * 当前电池编号
    */
    private String nowElectricityBatterySn;
    /**
    * 服务状态 未开通-0 已开通-1
    */
    private Object serviceStatus;
    /**
    * 月卡剩余天数
    */
    private Integer memberCardDays;
    /**
    * 租电池押金
    */
    @NotNull(message = "租电池押金不能为空!", groups = {UpdateGroup.class})
    private Double deposit;
    /**
    * 0--正常 1--删除
    */
    private Object delFlag;
    /**
    * 创建时间
    */
    private Long createTime;
    /**
    * 更新时间
    */
    private Long updateTime;

    public static final Integer DEL_NORMAL = 0;
    public static final Integer DEL_DEL = 1;

}