package com.xiliulou.electricity.mapper;

import com.xiliulou.electricity.entity.EleUserAuth;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * 实名认证信息(TEleUserAuth)表数据库访问层
 *
 * @author makejava
 * @since 2021-02-20 13:37:38
 */
public interface EleUserAuthMapper extends BaseMapper<EleUserAuth>{

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    EleUserAuth queryById(Long id);

    /**
     * 查询指定行数据
     *
     */
    List<EleUserAuth> queryList(@Param("offset") int offset, @Param("limit") int limit);


    /**
     * 通过实体作为筛选条件查询
     *
     * @param eleUserAuth 实例对象
     * @return 对象列表
     */
    List<EleUserAuth> queryAll(EleUserAuth eleUserAuth);

    /**
     * 新增数据
     *
     * @param eleUserAuth 实例对象
     * @return 影响行数
     */
    int insertOne(EleUserAuth eleUserAuth);

    /**
     * 修改数据
     *
     * @param eleUserAuth 实例对象
     * @return 影响行数
     */
    int update(EleUserAuth eleUserAuth);

    void updateByUid(Long uid, Integer authStatus,long updateTime);
}