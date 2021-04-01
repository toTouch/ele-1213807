package com.xiliulou.electricity.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.EleWarnMsg;
import com.xiliulou.electricity.mapper.EleWarnMsgMapper;
import com.xiliulou.electricity.query.EleWarnMsgQuery;
import com.xiliulou.electricity.service.EleWarnMsgService;
import com.xiliulou.electricity.utils.PageUtil;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;
import javax.annotation.Resource;

/**
 * 换电柜异常上报信息(TEleWarnMsg)表服务实现类
 *
 * @author makejava
 * @since 2021-03-29 14:12:45
 */
@Service("tEleWarnMsgService")
public class EleWarnMsgServiceImpl implements EleWarnMsgService {
    @Resource
    private EleWarnMsgMapper eleWarnMsgMapper;

    /**
     * 通过ID查询单条数据从DB
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public EleWarnMsg queryByIdFromDB(Long id) {
        return this.eleWarnMsgMapper.queryById(id);
    }
    
        /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public EleWarnMsg queryByIdFromCache(Long id) {
        return null;
    }
    
    /**
     * 新增数据
     *
     * @param eleWarnMsg 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public EleWarnMsg insert(EleWarnMsg eleWarnMsg) {
        this.eleWarnMsgMapper.insert(eleWarnMsg);
        return eleWarnMsg;
    }

    /**
     * 修改数据
     *
     * @param eleWarnMsg 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(EleWarnMsg eleWarnMsg) {
       return this.eleWarnMsgMapper.update(eleWarnMsg);
         
    }

    @Override
    public R queryList(EleWarnMsgQuery eleWarnMsgQuery) {
        Page page = PageUtil.getPage(eleWarnMsgQuery.getOffset(), eleWarnMsgQuery.getSize());
        return R.ok(eleWarnMsgMapper.queryList(page,eleWarnMsgQuery));
    }
}