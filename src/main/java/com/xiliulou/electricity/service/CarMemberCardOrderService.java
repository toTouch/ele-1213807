package com.xiliulou.electricity.service;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.CarMemberCardOrder;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.query.CarMemberCardOrderQuery;
import com.xiliulou.electricity.query.RentCarMemberCardOrderQuery;
import com.xiliulou.electricity.query.RentCarHybridOrderQuery;
import com.xiliulou.electricity.vo.CarMemberCardOrderVO;
import com.xiliulou.electricity.vo.HomePageTurnOverGroupByWeekDayVo;
import org.apache.commons.lang3.tuple.Triple;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.List;

/**
 * 租车套餐订单表(CarMemberCardOrder)表服务接口
 *
 * @author zzlong
 * @since 2022-12-21 09:47:25
 */
public interface CarMemberCardOrderService {

    /**
     * 通过ID查询单条数据从数据库
     *
     * @param id 主键
     * @return 实例对象
     */
    CarMemberCardOrder selectByIdFromDB(Long id);

    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    CarMemberCardOrder selectByIdFromCache(Long id);

    /**
     * 查询多条数据
     * @return 对象列表
     */
    List<CarMemberCardOrderVO> selectByPage(RentCarMemberCardOrderQuery memberCardOrderQuery);

    Integer selectByPageCount(RentCarMemberCardOrderQuery memberCardOrderQuery);

    /**
     * 新增数据
     *
     * @param carMemberCardOrder 实例对象
     * @return 实例对象
     */
    CarMemberCardOrder insert(CarMemberCardOrder carMemberCardOrder);

    /**
     * 修改数据
     *
     * @param carMemberCardOrder 实例对象
     * @return 实例对象
     */
    Integer update(CarMemberCardOrder carMemberCardOrder);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    Boolean deleteById(Long id);

    Triple<Boolean,String,Object> payRentCarMemberCard(CarMemberCardOrderQuery carMemberCardOrderQuery, HttpServletRequest request);

    CarMemberCardOrder selectByOrderId(String orderNo);

    Triple<Boolean, String, Object> handleRentCarMemberCard(Long storeId, Long carModelId, Integer rentTime, String rentType, UserInfo userInfo);

    String getCardName(String rentType);

    Triple<Boolean, String, Object> userCarMemberCardInfo();

    BigDecimal queryCarMemberCardTurnOver(Integer tenantId, Long todayStartTime, List<Long> finalFranchiseeIds);

    List<HomePageTurnOverGroupByWeekDayVo> queryCarMemberCardTurnOverByCreateTime(Integer tenantId, List<Long> finalFranchiseeIds, Long beginTime, Long endTime);
}
