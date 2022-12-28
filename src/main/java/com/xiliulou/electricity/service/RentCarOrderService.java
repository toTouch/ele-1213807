package com.xiliulou.electricity.service;

import com.xiliulou.electricity.entity.RentCarOrder;
import com.xiliulou.electricity.query.RentCarHybridOrderQuery;
import com.xiliulou.electricity.query.RentCarOrderQuery;
import com.xiliulou.electricity.query.UserRentCarOrderQuery;
import com.xiliulou.electricity.vo.RentCarOrderVO;
import org.apache.commons.lang3.tuple.Triple;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 租车订单表(RentCarOrder)表服务接口
 *
 * @author zzlong
 * @since 2022-12-21 09:47:57
 */
public interface RentCarOrderService {

    /**
     * 通过ID查询单条数据从数据库
     *
     * @param id 主键
     * @return 实例对象
     */
    RentCarOrder selectByIdFromDB(Long id);

    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    RentCarOrder selectByIdFromCache(Long id);

    /**
     * 查询多条数据
     * @return 对象列表
     */
    List<RentCarOrderVO> selectByPage(RentCarOrderQuery rentCarOrderQuery);

    /**
     * 新增数据
     *
     * @param rentCarOrder 实例对象
     * @return 实例对象
     */
    RentCarOrder insert(RentCarOrder rentCarOrder);

    /**
     * 修改数据
     *
     * @param rentCarOrder 实例对象
     * @return 实例对象
     */
    Integer update(RentCarOrder rentCarOrder);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    Boolean deleteById(Long id);

    Triple<Boolean,String,Object> rentCarOrder(UserRentCarOrderQuery query);

    Triple<Boolean, String, Object> rentCarHybridOrder(RentCarHybridOrderQuery query, HttpServletRequest request);

    Triple<Boolean, String, Object> save(RentCarOrderQuery rentCarOrderQuery);

    Integer selectPageCount(RentCarOrderQuery rentCarOrderQuery);
}
