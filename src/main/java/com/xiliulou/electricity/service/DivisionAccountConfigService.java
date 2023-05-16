package com.xiliulou.electricity.service;

import com.xiliulou.electricity.entity.DivisionAccountConfig;
import com.xiliulou.electricity.query.DivisionAccountConfigQuery;
import com.xiliulou.electricity.query.DivisionAccountConfigStatusQuery;
import com.xiliulou.electricity.vo.DivisionAccountConfigRefVO;
import com.xiliulou.electricity.vo.DivisionAccountConfigVO;
import com.xiliulou.electricity.vo.SearchVo;
import org.apache.commons.lang3.tuple.Triple;

import java.util.List;

/**
 * (DivisionAccountConfig)表服务接口
 *
 * @author zzlong
 * @since 2023-04-23 18:00:37
 */
public interface DivisionAccountConfigService {

    /**
     * 通过ID查询单条数据从数据库
     *
     * @param id 主键
     * @return 实例对象
     */
    DivisionAccountConfig queryByIdFromDB(Long id);

    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    DivisionAccountConfig queryByIdFromCache(Long id);

    /**
     * 查询多条数据
     *
     * @return 对象列表
     */
    List<DivisionAccountConfigVO> selectByPage(DivisionAccountConfigQuery query);

    Integer selectByPageCount(DivisionAccountConfigQuery query);

    Integer selectDivisionAccountConfigExit(String name, Integer tenantId);

    /**
     * 新增数据
     *
     * @param divisionAccountConfig 实例对象
     * @return 实例对象
     */
    DivisionAccountConfig insert(DivisionAccountConfig divisionAccountConfig);

    /**
     * 修改数据
     *
     * @param divisionAccountConfig 实例对象
     * @return 实例对象
     */
    Integer update(DivisionAccountConfig divisionAccountConfig);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否   成功
     */
    Integer deleteById(Long id);

    Triple<Boolean, String, Object> delete(Long id);

    Triple<Boolean, String, Object> modify(DivisionAccountConfigQuery divisionAccountConfigQuery);

    Triple<Boolean, String, Object> save(DivisionAccountConfigQuery divisionAccountConfigQue);

    Triple<Boolean, String, Object> selectInfoById(Long id);

    List<DivisionAccountConfigRefVO> selectDivisionAccountConfigRefInfo(DivisionAccountConfigQuery query);

    DivisionAccountConfigRefVO selectDivisionConfigByRefId(Long membercardId, Long storeId, Long franchinseeId, Integer tenantId);

    Triple<Boolean, String, Object> updateStatus(DivisionAccountConfigStatusQuery divisionAccountConfigQuery);

    List<SearchVo> configSearch(Long size, Long offset, String name, Integer tenantId);
}
