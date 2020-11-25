package com.xiliulou.electricity.service.impl;

import com.xiliulou.electricity.entity.Provincial;
import com.xiliulou.electricity.mapper.ProvincialMapper;
import com.xiliulou.electricity.service.ProvincialService;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;
import javax.annotation.Resource;
import java.util.List;

/**
 * (Provincial)表服务实现类
 *
 * @author makejava
 * @since 2020-11-25 16:20:43
 */
@Service("provincialService")
public class ProvincialServiceImpl implements ProvincialService {
    @Resource
    private ProvincialMapper provincialMapper;

    /**
     * 通过ID查询单条数据从DB
     *
     * @param pid 主键
     * @return 实例对象
     */
    @Override
    public Provincial queryByIdFromDB(Integer pid) {
        return this.provincialMapper.queryById(pid);
    }
    
        /**
     * 通过ID查询单条数据从缓存
     *
     * @param pid 主键
     * @return 实例对象
     */
    @Override
    public  Provincial queryByIdFromCache(Integer pid) {
        return null;
    }


    /**
     * 查询多条数据
     *
     * @param offset 查询起始位置
     * @param limit 查询条数
     * @return 对象列表
     */
    @Override
    public List<Provincial> queryAllByLimit(int offset, int limit) {
        return this.provincialMapper.queryAllByLimit(offset, limit);
    }

}