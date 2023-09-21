package com.xiliulou.electricity.vo.car;

import com.xiliulou.electricity.enums.RentalTypeEnum;
import com.xiliulou.electricity.enums.car.CarRentalStateEnum;
import lombok.Data;

import java.io.Serializable;

/**
 * 车辆租赁信息展示层数据模型
 *
 * @author xiaohui.song
 **/
@Data
public class CarRentalOrderVo implements Serializable {

    private static final long serialVersionUID = -2707347738709336980L;

    /**
     * 订单编码
     */
    private String orderNo;

    /**
     * 类型
     * <pre>
     *     1-租借
     *     2-归还
     * </pre>
     * @see RentalTypeEnum
     */
    private Integer type;

    /**
     * 车辆SN码
     */
    private String carSn;

    /**
     * 订单状态
     * <pre>
     *     1-审核中
     *     2-成功
     *     3-审核拒绝
     * </pre>
     * @see CarRentalStateEnum
     */
    private Integer rentalState;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间，时间戳
     */
    private Long createTime;

    // ++++++++++ 辅助业务数据 ++++++++++

    /**
     * 用户UID
     */
    private Long uid;

    /**
     * 用户真实姓名
     */
    private String userRelName;

    /**
     * 用户手机号
     */
    private String userPhone;

    /**
     * 车辆型号名称
     */
    private String carModelName;

    /**
     * 门店名称
     */
    private String storeName;
}
