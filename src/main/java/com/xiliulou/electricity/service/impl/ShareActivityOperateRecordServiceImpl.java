package com.xiliulou.electricity.service.impl;

import com.google.api.client.util.Lists;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.electricity.entity.ElectricityMemberCard;
import com.xiliulou.electricity.entity.ShareActivityOperateRecord;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.mapper.ShareActivityOperateRecordMapper;
import com.xiliulou.electricity.query.ShareActivityQuery;
import com.xiliulou.electricity.service.ElectricityMemberCardService;
import com.xiliulou.electricity.service.ShareActivityOperateRecordService;
import com.xiliulou.electricity.service.UserService;
import com.xiliulou.electricity.vo.ShareActivityOperateRecordVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shaded.org.apache.commons.lang3.StringUtils;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * (ShareActivityOperateRecord)表服务实现类
 *
 * @author zzlong
 * @since 2023-05-24 14:47:17
 */
@Service("shareActivityOperateRecordService")
@Slf4j
public class ShareActivityOperateRecordServiceImpl implements ShareActivityOperateRecordService {
    @Resource
    private ShareActivityOperateRecordMapper shareActivityOperateRecordMapper;
    @Autowired
    private UserService userService;
    @Autowired
    private ElectricityMemberCardService electricityMemberCardService;

    /**
     * 通过ID查询单条数据从DB
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public ShareActivityOperateRecord queryByIdFromDB(Long id) {
        return this.shareActivityOperateRecordMapper.queryById(id);
    }

    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public ShareActivityOperateRecord queryByIdFromCache(Long id) {
        return null;
    }

    @Override
    public List<ShareActivityOperateRecordVO> page(ShareActivityQuery query) {
        List<ShareActivityOperateRecord> list=shareActivityOperateRecordMapper.selectByPage(query);
        if(CollectionUtils.isEmpty(list)){
            return Collections.emptyList();
        }

        return list.parallelStream().map(item -> {
            ShareActivityOperateRecordVO shareActivityOperateRecordVO = new ShareActivityOperateRecordVO();
            BeanUtils.copyProperties(item, shareActivityOperateRecordVO);

            User user = userService.queryByUidFromCache(item.getUid());
            shareActivityOperateRecordVO.setUsername(Objects.nonNull(user) ? user.getName() : "");


            if (StringUtils.isNotBlank(item.getMemberCard())) {
                List<Long> membercardIds = JsonUtil.fromJsonArray(item.getMemberCard(), Long.class);
                if (CollectionUtils.isNotEmpty(membercardIds)) {
                    List<ElectricityMemberCard> membercardList = Lists.newArrayList();
                    for (Long membercardId : membercardIds) {
                        ElectricityMemberCard electricityMemberCard = electricityMemberCardService.queryByCache(membercardId.intValue());
                        if (Objects.nonNull(electricityMemberCard)) {
                            membercardList.add(electricityMemberCard);
                        }
                    }

                    if (CollectionUtils.isNotEmpty(membercardList)) {
                        List<String> names = membercardList.stream().map(ElectricityMemberCard::getName).collect(Collectors.toList());
                        shareActivityOperateRecordVO.setMembercardNames(names);
                    }
                }
            }

            return shareActivityOperateRecordVO;
        }).collect(Collectors.toList());
    }

    @Override
    public Integer count(ShareActivityQuery query) {
        return shareActivityOperateRecordMapper.selectByPageCount(query);
    }

    /**
     * 查询多条数据
     *
     * @param offset 查询起始位置
     * @param limit  查询条数
     * @return 对象列表
     */
    @Override
    public List<ShareActivityOperateRecord> queryAllByLimit(int offset, int limit) {
        return this.shareActivityOperateRecordMapper.queryAllByLimit(offset, limit);
    }

    /**
     * 新增数据
     *
     * @param shareActivityOperateRecord 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ShareActivityOperateRecord insert(ShareActivityOperateRecord shareActivityOperateRecord) {
        this.shareActivityOperateRecordMapper.insertOne(shareActivityOperateRecord);
        return shareActivityOperateRecord;
    }

    /**
     * 修改数据
     *
     * @param shareActivityOperateRecord 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(ShareActivityOperateRecord shareActivityOperateRecord) {
        return this.shareActivityOperateRecordMapper.update(shareActivityOperateRecord);

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
        return this.shareActivityOperateRecordMapper.deleteById(id) > 0;
    }
}
