package com.xiliulou.electricity.mapper;

import com.xiliulou.electricity.entity.CarDepositOrder;

import java.math.BigDecimal;
import java.util.List;

import com.xiliulou.electricity.entity.EleDepositOrder;
import com.xiliulou.electricity.query.RentCarDepositOrderQuery;
import com.xiliulou.electricity.vo.HomePageTurnOverGroupByWeekDayVo;
import com.xiliulou.electricity.vo.UserCarDepositOrderVo;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * (CarDepositOrder)表数据库访问层
 *
 * @author zzlong
 * @since 2022-12-21 09:15:22
 */
public interface CarDepositOrderMapper extends BaseMapper<CarDepositOrder> {

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    CarDepositOrder selectById(Long id);

    /**
     * 查询指定行数据
     *
     * @return 对象列表
     */
    List<CarDepositOrder> selectByPage(RentCarDepositOrderQuery rentCarDepositOrderQuery);

    Integer selectPageCount(RentCarDepositOrderQuery rentCarDepositOrderQuery);

    /**
     * 通过实体作为筛选条件查询
     *
     * @param carDepositOrder 实例对象
     * @return 对象列表
     */
    List<CarDepositOrder> selectByQuery(CarDepositOrder carDepositOrder);

    /**
     * 新增数据
     *
     * @param carDepositOrder 实例对象
     * @return 影响行数
     */
    int insertOne(CarDepositOrder carDepositOrder);

    /**
     * 修改数据
     *
     * @param carDepositOrder 实例对象
     * @return 影响行数
     */
    int update(CarDepositOrder carDepositOrder);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(Long id);
    
    BigDecimal queryDepositTurnOverByDepositType(@Param("tenantId") Integer tenantId,
            @Param("todayStartTime") Long todayStartTime, @Param("depositType") Integer depositType,
            @Param("franchiseeIds") List<Long> franchiseeIds, @Param("payType") Integer payType);

    List<HomePageTurnOverGroupByWeekDayVo> queryDepositTurnOverAnalysisByDepositType(@Param("tenantId") Integer tenantId, @Param("depositType") Integer depositType, @Param("franchiseeIds") List<Long> franchiseeIds, @Param("beginTime") Long beginTime, @Param("endTime") Long endTime);
    
    List<UserCarDepositOrderVo> payDepositOrderList(@Param("uid") Long uid, @Param("tenantId") Integer tenantId,
            @Param("offset") Long offset, @Param("size") Long size);
    
    CarDepositOrder queryLastPayDepositTimeByUid(@Param("uid") Long uid, @Param("franchiseeId") Long franchiseeId,
            @Param("tenantId") Integer tenantId);

    BigDecimal queryFreeDepositAlipayTurnOver(@Param("tenantId")Integer tenantId,
                                              @Param("todayStartTime")Long todayStartTime,
                                              @Param("rentCarDeposit")Integer rentCarDeposit,
                                              @Param("finalFranchiseeIds")List<Long> finalFranchiseeIds);
}
