package com.xiliulou.electricity.service;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.CarDepositOrder;
import com.xiliulou.electricity.entity.EleDepositOrder;
import com.xiliulou.electricity.query.RentCarDepositOrderQuery;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.query.RentCarHybridOrderQuery;
import com.xiliulou.electricity.vo.CarDepositOrderVO;
import com.xiliulou.electricity.vo.HomePageTurnOverGroupByWeekDayVo;
import org.apache.commons.lang3.tuple.Triple;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
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
    List<CarDepositOrderVO> selectByPage(RentCarDepositOrderQuery rentCarDepositOrderQuery);

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

    CarDepositOrder selectByOrderId(String orderNo,Integer tenantId);

    Triple<Boolean, String, Object> handleRentCarDeposit(Long franchiseeId,Long carModelId, Long storeId, Integer memberCardId, UserInfo userInfo);

    Triple<Boolean, String, Object> handleRefundCarDeposit(String orderId, Long uid, String remark, BigDecimal refundAmount, HttpServletRequest request);

    Triple<Boolean, String, Object> handleOffLineRefundCarDeposit(String orderId, Long uid, HttpServletRequest request);
    
    BigDecimal queryDepositTurnOverByDepositType(Integer tenantId, Long o, Integer rentCarDeposit,
            List<Long> finalFranchiseeIds, Integer payType);

    List<HomePageTurnOverGroupByWeekDayVo> queryDepositTurnOverAnalysisByDepositType(Integer tenantId, Integer rentCarDeposit, List<Long> finalFranchiseeIds, Long beginTime, Long endTime);
    
    CarDepositOrder queryLastPayDepositTimeByUid(Long uid, Long franchiseeId, Integer tenantId);
    
    R payDepositOrderList(Long offset, Long size);
}
