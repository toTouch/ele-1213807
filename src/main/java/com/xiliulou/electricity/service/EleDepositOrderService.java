package com.xiliulou.electricity.service;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.EleDepositOrder;

import javax.servlet.http.HttpServletRequest;

/**
 * 缴纳押金订单表(TEleDepositOrder)表服务接口
 *
 * @author makejava
 * @since 2021-02-22 10:16:44
 */
public interface EleDepositOrderService {

    /**
     * 通过ID查询单条数据从数据库
     *
     * @param id 主键
     * @return 实例对象
     */
    EleDepositOrder queryByIdFromDB(Long id);
    
      /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    EleDepositOrder queryByIdFromCache(Long id);

    /**
     * 新增数据
     *
     * @param eleDepositOrder 实例对象
     * @return 实例对象
     */
    EleDepositOrder insert(EleDepositOrder eleDepositOrder);

    /**
     * 修改数据
     *
     * @param eleDepositOrder 实例对象
     * @return 实例对象
     */
    Integer update(EleDepositOrder eleDepositOrder);
    

    EleDepositOrder queryByOrderId(String orderNo);
    
    R payDeposit(HttpServletRequest request);


    Long queryByUid(Long uid);
}