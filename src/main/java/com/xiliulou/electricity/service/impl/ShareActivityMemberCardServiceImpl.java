package com.xiliulou.electricity.service.impl;

import com.xiliulou.electricity.entity.ShareActivity;
import com.xiliulou.electricity.entity.ShareActivityMemberCard;
import com.xiliulou.electricity.mapper.ShareActivityMemberCardMapper;
import com.xiliulou.electricity.service.ShareActivityMemberCardService;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

/**
 * (ShareActivityMemberCard)表服务实现类
 *
 * @author zzlong
 * @since 2023-05-24 10:19:26
 */
@Service("shareActivityMemberCardService")
@Slf4j
public class ShareActivityMemberCardServiceImpl implements ShareActivityMemberCardService {
    @Resource
    private ShareActivityMemberCardMapper shareActivityMemberCardMapper;

    /**
     * 通过ID查询单条数据从DB
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public ShareActivityMemberCard queryByIdFromDB(Long id) {
        return this.shareActivityMemberCardMapper.queryById(id);
    }

    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public ShareActivityMemberCard queryByIdFromCache(Long id) {
        return null;
    }


    /**
     * 查询多条数据
     *
     * @param offset 查询起始位置
     * @param limit  查询条数
     * @return 对象列表
     */
    @Override
    public List<ShareActivityMemberCard> queryAllByLimit(int offset, int limit) {
        return this.shareActivityMemberCardMapper.queryAllByLimit(offset, limit);
    }

    /**
     * 新增数据
     *
     * @param shareActivityMemberCard 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ShareActivityMemberCard insert(ShareActivityMemberCard shareActivityMemberCard) {
        this.shareActivityMemberCardMapper.insertOne(shareActivityMemberCard);
        return shareActivityMemberCard;
    }

    /**
     * 修改数据
     *
     * @param shareActivityMemberCard 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(ShareActivityMemberCard shareActivityMemberCard) {
        return this.shareActivityMemberCardMapper.update(shareActivityMemberCard);

    }

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteById(Long id) {
        return this.shareActivityMemberCardMapper.deleteById(id) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer batchInsert(List<ShareActivityMemberCard> shareActivityMemberCards) {
        return shareActivityMemberCardMapper.batchInsert(shareActivityMemberCards);
    }

    @Override
    public List<ShareActivityMemberCard> selectByActivityId(Integer id) {
        return shareActivityMemberCardMapper.selectByActivityId(id);
    }

    @Override
    public Integer deleteByActivityId(Integer id) {
        return shareActivityMemberCardMapper.deleteByActivityId(id);
    }
}
