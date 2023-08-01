package com.xiliulou.electricity.service.impl;

import com.xiliulou.electricity.entity.InvitationActivityMemberCard;
import com.xiliulou.electricity.mapper.InvitationActivityMemberCardMapper;
import com.xiliulou.electricity.service.InvitationActivityMemberCardService;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

/**
 * (InvitationActivityMemberCard)表服务实现类
 *
 * @author zzlong
 * @since 2023-06-05 15:31:55
 */
@Service("invitationActivityMemberCardService")
@Slf4j
public class InvitationActivityMemberCardServiceImpl implements InvitationActivityMemberCardService {
    @Resource
    private InvitationActivityMemberCardMapper invitationActivityMemberCardMapper;

    /**
     * 通过ID查询单条数据从DB
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public InvitationActivityMemberCard queryByIdFromDB(Long id) {
        return this.invitationActivityMemberCardMapper.queryById(id);
    }

    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public InvitationActivityMemberCard queryByIdFromCache(Long id) {
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
    public List<InvitationActivityMemberCard> queryAllByLimit(int offset, int limit) {
        return this.invitationActivityMemberCardMapper.queryAllByLimit(offset, limit);
    }

    /**
     * 新增数据
     *
     * @param invitationActivityMemberCard 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public InvitationActivityMemberCard insert(InvitationActivityMemberCard invitationActivityMemberCard) {
        this.invitationActivityMemberCardMapper.insertOne(invitationActivityMemberCard);
        return invitationActivityMemberCard;
    }

    /**
     * 修改数据
     *
     * @param invitationActivityMemberCard 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(InvitationActivityMemberCard invitationActivityMemberCard) {
        return this.invitationActivityMemberCardMapper.update(invitationActivityMemberCard);

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
        return this.invitationActivityMemberCardMapper.deleteById(id) > 0;
    }

    @Override
    public Integer deleteByActivityId(Long id) {
        return this.invitationActivityMemberCardMapper.deleteByActivityId(id);
    }

    @Override
    public List<InvitationActivityMemberCard> selectPackagesByActivityIdAndType(Long id, Integer packageType) {
        return invitationActivityMemberCardMapper.selectPackagesByActivityIdAndPackageType(id, packageType);
    }

    @Override
    public Integer batchInsert(List<InvitationActivityMemberCard> shareActivityMemberCards) {
        return invitationActivityMemberCardMapper.batchInsert(shareActivityMemberCards);
    }

    @Override
    public List<Long> selectMemberCardIdsByActivityId(Long id) {
        return invitationActivityMemberCardMapper.selectMemberCardIdsByActivityId(id);
    }
}
