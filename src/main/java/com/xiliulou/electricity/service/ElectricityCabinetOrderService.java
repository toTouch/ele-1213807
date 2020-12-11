package com.xiliulou.electricity.service;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.ElectricityCabinetOrder;
import com.xiliulou.electricity.query.ElectricityCabinetOrderQuery;
import com.xiliulou.electricity.query.OpenDoorQuery;
import com.xiliulou.electricity.query.OrderQuery;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;

/**
 * 订单表(TElectricityCabinetOrder)表服务接口
 *
 * @author makejava
 * @since 2020-11-26 10:56:56
 */
public interface ElectricityCabinetOrderService {

    /**
     * 通过ID查询单条数据从数据库
     *
     * @param id 主键
     * @return 实例对象
     */
    ElectricityCabinetOrder queryByIdFromDB(Long id);

    /**
     * 新增数据
     *
     * @param electricityCabinetOrder 实例对象
     * @return 实例对象
     */
    ElectricityCabinetOrder insert(ElectricityCabinetOrder electricityCabinetOrder);

    /**
     * 修改数据
     *
     * @param electricityCabinetOrder 实例对象
     * @return 实例对象
     */
    Integer update(ElectricityCabinetOrder electricityCabinetOrder);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    Boolean deleteById(Long id);

    R order(OrderQuery orderQuery);

    R queryList(ElectricityCabinetOrderQuery electricityCabinetOrderQuery);

    R openDoor(OpenDoorQuery openDoorQuery);

    Integer homeOneCount(Long first, Long now);

    BigDecimal homeOneSuccess(Long first, Long now);

    List<HashMap<String, String>> homeThree(long startTimeMilliDay, Long endTimeMilliDay);

    Integer homeMonth(Long uid, Long firstMonth, Long now);

    Integer homeTotal(Long uid);

    R queryCount(ElectricityCabinetOrderQuery electricityCabinetOrderQuery);

    void handlerExpiredCancelOrder(String orderId);

    R queryStatus(String orderId);
}
