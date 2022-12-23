package com.xiliulou.electricity.service;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.CarDepositOrder;
import com.xiliulou.electricity.query.RentCarDepositOrderQuery;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.query.RentCarHybridOrderQuery;
import org.apache.commons.lang3.tuple.Triple;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * (CarDepositOrder)表服务接口
 *
 * @author zzlong
 * @since 2022-12-21 09:15:22
 */
public interface CarDepositOrderService {

    /**
     * 通过ID查询单条数据从数据库
     *
     * @param id 主键
     * @return 实例对象
     */
    CarDepositOrder selectByIdFromDB(Long id);

    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    CarDepositOrder selectByIdFromCache(Long id);

    /**
     * 查询多条数据
     * @return 对象列表
     */
    List<CarDepositOrder> selectByPage(RentCarDepositOrderQuery rentCarDepositOrderQuery);

    Integer selectPageCount(RentCarDepositOrderQuery rentCarDepositOrderQuery);
    /**
     * 新增数据
     *
     * @param carDepositOrder 实例对象
     * @return 实例对象
     */
    CarDepositOrder insert(CarDepositOrder carDepositOrder);

    /**
     * 修改数据
     *
     * @param carDepositOrder 实例对象
     * @return 实例对象
     */
    Integer update(CarDepositOrder carDepositOrder);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    Boolean deleteById(Long id);

    Triple<Boolean,String,Object> payRentCarDeposit(Long storeId, Integer carModelId, HttpServletRequest request);

    Triple<Boolean, String, Object> selectRentCarDeposit();

    Triple<Boolean, String, Object> refundRentCarDeposit(HttpServletRequest request);

    CarDepositOrder selectByOrderId(String orderNo);

    Triple<Boolean, String, Object> handleRentCarDeposit(RentCarHybridOrderQuery query, UserInfo userInfo);
}
