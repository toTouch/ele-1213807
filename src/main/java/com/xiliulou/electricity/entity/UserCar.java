package com.xiliulou.electricity.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.TableName;


/**
 * (UserCar)表实体类
 *
 * @author zzlong
 * @since 2022-12-07 17:35:15
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_user_car")
public class UserCar {
    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    /**
     * uid
     */
    private Long uid;
    /**
     * 车辆id
     */
    private Long cid;
    /**
     * 车辆sn
     */
    private String sn;
    /**
     * 车辆型号
     */
    private Long carModel;
    /**
     * 0--正常 1--删除
     */
    private Integer delFlag;

    private Integer tenantId;

    private Long createTime;

    private Long updateTime;

    public static final Integer DEL_NORMAL = 0;
    public static final Integer DEL_DEL = 1;

}
