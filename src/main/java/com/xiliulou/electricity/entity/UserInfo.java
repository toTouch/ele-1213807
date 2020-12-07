package com.xiliulou.electricity.entity;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.TableName;
/**
 * 用户列表(TUserInfo)实体类
 *
 * @author makejava
 * @since 2020-12-07 15:00:00
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_user_info")
public class UserInfo {
    @TableId
    private Long id;
    
    private Long uid;
    /**
    * 用户姓名
    */
    private String name;
    /**
    * 手机号
    */
    private String phone;
    /**
    * 门店Id
    */
    private Long storeId;
    /**
    * 身份证号
    */
    private String idNumber;
    /**
    * 初始电池编号
    */
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