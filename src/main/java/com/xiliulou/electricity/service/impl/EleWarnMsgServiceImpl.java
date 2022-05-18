package com.xiliulou.electricity.service.impl;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.EleWarnMsg;
import com.xiliulou.electricity.mapper.EleWarnMsgMapper;
import com.xiliulou.electricity.query.EleWarnMsgQuery;
import com.xiliulou.electricity.service.EleWarnMsgService;
import com.xiliulou.electricity.vo.EleWarnMsgRankingVo;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;
import javax.annotation.Resource;
import java.util.List;

/**
 * 换电柜异常上报信息(TEleWarnMsg)表服务实现类
 *
 * @author makejava
 * @since 2021-03-29 14:12:45
 */
@Service("eleWarnMsgService")
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
        return this.eleWarnMsgMapper.selectById(id);
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
       return this.eleWarnMsgMapper.updateById(eleWarnMsg);

    }

    @Override
    public R queryList(EleWarnMsgQuery eleWarnMsgQuery) {
        return R.ok(eleWarnMsgMapper.queryList(eleWarnMsgQuery));
    }

    @Override
    public R queryAllTenant(EleWarnMsgQuery eleWarnMsgQuery) {
        return R.ok(eleWarnMsgMapper.queryAllTenantList(eleWarnMsgQuery));
    }

    @Override
    public R queryAllTenantCount() {
        return R.ok(eleWarnMsgMapper.queryAllTenantCount());
    }

    @Override
    public R queryCount(EleWarnMsgQuery eleWarnMsgQuery) {
        return R.ok(eleWarnMsgMapper.queryCount(eleWarnMsgQuery));
    }

    @Override
    public void delete(Long id) {
        eleWarnMsgMapper.deleteById(id);
    }

    @Override
    public R queryStatisticsEleWarmMsg(EleWarnMsgQuery eleWarnMsgQuery) {
        return R.ok(eleWarnMsgMapper.queryStatisticsEleWarmMsg(eleWarnMsgQuery));
    }

    @Override
    public R queryStatisticEleWarnMsgByElectricityCabinet(EleWarnMsgQuery eleWarnMsgQuery) {
        return R.ok(eleWarnMsgMapper.queryStatisticEleWarnMsgByElectricityCabinet(eleWarnMsgQuery));
    }

    @Override
    public R queryStatisticEleWarnMsgRanking(EleWarnMsgQuery eleWarnMsgQuery) {


        List<EleWarnMsgRankingVo> eleWarnMsgRankingVos=eleWarnMsgMapper.queryStatisticEleWarnMsgRanking(eleWarnMsgQuery);


        return R.ok();
    }
}
