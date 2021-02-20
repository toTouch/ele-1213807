package com.xiliulou.electricity.service.impl;

import com.xiliulou.electricity.entity.EleUserAuth;
import com.xiliulou.electricity.mapper.EleUserAuthMapper;
import com.xiliulou.electricity.service.EleUserAuthService;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;
import javax.annotation.Resource;

/**
 * 实名认证信息(TEleUserAuth)表服务实现类
 *
 * @author makejava
 * @since 2021-02-20 13:37:38
 */
@Service("tEleUserAuthService")
public class EleUserAuthServiceImpl implements EleUserAuthService {
    @Resource
    private EleUserAuthMapper eleUserAuthMapper;

    /**
     * 通过ID查询单条数据从DB
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public EleUserAuth queryByIdFromDB(Long id) {
        return this.eleUserAuthMapper.queryById(id);
    }
    
        /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public EleUserAuth queryByIdFromCache(Long id) {
        return null;
    }
    
    /**
     * 新增数据
     *
     * @param eleUserAuth 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public EleUserAuth insert(EleUserAuth eleUserAuth) {
        this.eleUserAuthMapper.insert(eleUserAuth);
        return eleUserAuth;
    }

    /**
     * 修改数据
     *
     * @param eleUserAuth 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(EleUserAuth eleUserAuth) {
       return this.eleUserAuthMapper.update(eleUserAuth);
         
    }
}