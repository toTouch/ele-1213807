package com.xiliulou.electricity.entity;
import com.baomidou.mybatisplus.annotation.IdType;
                                                                            import com.baomidou.mybatisplus.annotation.TableId;
                                                                            import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.TableName;
/**
 * 用户绑定列表(TUserInfo)实体类
 *
 * @author makejava
 * @since 2020-12-08 14:17:59
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_user_info")
public class UserInfo {

    @TableId(value = "id",type = IdType.AUTO)
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
    * 身份证号
    */
    private String idNumber;
    /**
    * 服务状态 未开通-0 已开通-1
    */
    private Integer serviceStatus;
    /**
    * 月卡剩余天数
    */
    private Integer memberCardDays;
    /**
    * 电池门店Id
    */
    private Integer batteryStoreId;
    /**
    * 电池门店地区Id
    */
    private Integer batteryAreaId;
    /**
    * 初始电池编号
    */
    private String initElectricityBatterySn;
    /**
    * 当前电池编号
    */
    private String nowElectricityBatterySn;
    /**
    * 租电池押金
    */
    private Double batteryDeposit;
    /**
    * 车辆门店Id
    */
    private Integer carStoreId;
    /**
    * 车辆编号
    */
    private String carSn;
    /**
    * 车牌号
    */
    private String numberPlate;
    /**
    * 租车辆押金
    */
    private Double carDeposit;
    /**
    * 0--正常 1--删除
    */
    private Integer delFlag;
    /**
    * 创建时间
    */
    private Long createTime;
    /**
    * 更新时间
    */
    private Long updateTime;
    /**
     * 可用状态(0--启用，1--禁用)
     */
    private Integer usableStatus;

    public static final Integer DEL_NORMAL = 0;
    public static final Integer DEL_DEL = 1;

    public static final Integer IS_SERVICE_STATUS = 0;
    public static final Integer NO_SERVICE_STATUS = 1;

    //可用
    public static final Integer USER_USABLE_STATUS = 0;
    //禁用
    public static final Integer USER_UN_USABLE_STATUS = 1;

}