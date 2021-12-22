package com.xiliulou.electricity.service;

import com.xiliulou.electricity.entity.ThirdCallBackUrl;
import com.xiliulou.electricity.web.query.ThirdCallBackUrlRequest;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * (ThirdCallBackUrl)表服务接口
 *
 * @author makejava
 * @since 2021-11-10 15:25:19
 */
public interface ThirdCallBackUrlService {

    /**
     * 通过ID查询单条数据从数据库
     *
     * @param id 主键
     * @return 实例对象
     */
    ThirdCallBackUrl queryByIdFromDB(Integer id);

    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    ThirdCallBackUrl queryByTenantIdFromCache(Integer id);

    /**
     * 查询多条数据
     *
     * @param offset 查询起始位置
     * @param limit  查询条数
     * @return 对象列表
     */
    List<ThirdCallBackUrl> queryAllByLimit(int offset, int limit);

    /**
     * 新增数据
     *
     * @param thirdCallBackUrl 实例对象
     * @return 实例对象
     */
    ThirdCallBackUrl insert(ThirdCallBackUrl thirdCallBackUrl);

    /**
     * 修改数据
     *
     * @param thirdCallBackUrl 实例对象
     * @return 实例对象
     */
    Pair<Boolean, Object> update(ThirdCallBackUrlRequest thirdCallBackUrl);

    @Transactional(rollbackFor = Exception.class)
    Integer update(ThirdCallBackUrl thirdCallBackUrl);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    Boolean deleteById(Integer id);

    Pair<Boolean, Object> queryThirdCallBackByTenantId();

    Pair<Boolean, Object> save(ThirdCallBackUrlRequest thirdCallBackUrlRequest);

    ThirdCallBackUrl queryByTenantIdFromDB(Integer id);

}
