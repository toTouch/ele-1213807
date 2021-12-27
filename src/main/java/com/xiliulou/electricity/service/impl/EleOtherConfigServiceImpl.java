package com.xiliulou.electricity.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.constant.ElectricityCabinetConstant;
import com.xiliulou.electricity.entity.EleOtherConfig;
import com.xiliulou.electricity.mapper.EleOtherConfigMapper;
import com.xiliulou.electricity.service.EleOtherConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * (CupboardOtherConfig)表服务实现类
 *
 * @author Hardy
 * @since 2021-07-21 15:22:56
 */
@Service("cupboardOtherConfigService")
@Slf4j
public class EleOtherConfigServiceImpl implements EleOtherConfigService {
    @Resource
    EleOtherConfigMapper eleOtherConfigMapper;
    @Autowired
    private RedisService redisService;



    /**
     * 新增数据
     *
     * @param eleOtherConfig 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer insert(EleOtherConfig eleOtherConfig) {
        int insert = this.eleOtherConfigMapper.insert(eleOtherConfig);
        return insert;
    }

    /**
     * 修改数据
     *
     * @param eleOtherConfig 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(EleOtherConfig  eleOtherConfig) {
        int update = this.eleOtherConfigMapper.updateById(eleOtherConfig);
        if (update > 0){
            redisService.delete(ElectricityCabinetConstant.CACHE_ELE_OTHER_CONFIG + eleOtherConfig.getEid());
        }
        return update;
    }


    @Override
    public EleOtherConfig queryByEidFromCache(Integer eid) {
        EleOtherConfig eleOtherConfigCache = redisService.getWithHash(ElectricityCabinetConstant.CACHE_ELE_OTHER_CONFIG + eid, EleOtherConfig.class);

        if (Objects.nonNull(eleOtherConfigCache)){
            return eleOtherConfigCache;
        }

        LambdaQueryWrapper<EleOtherConfig> eq = new LambdaQueryWrapper<EleOtherConfig>().eq(EleOtherConfig::getEid, eid).eq(EleOtherConfig::getDelFlag, EleOtherConfig.DEL_NORMAL);
        EleOtherConfig eleOtherConfig = eleOtherConfigMapper.selectOne(eq);

        if (Objects.isNull(eleOtherConfig)){
            return null;
        }

        redisService.saveWithHash(ElectricityCabinetConstant.CACHE_ELE_OTHER_CONFIG + eid,eleOtherConfig);
        return eleOtherConfig;
    }

    @Override
    public R updateEleOtherConfig(EleOtherConfig  eleOtherConfig) {
        EleOtherConfig config = eleOtherConfigMapper.selectById(eleOtherConfig.getId());
        if (Objects.isNull(config)){
            return R.fail("未查询到相关配置信息");
        }

        config.setUpdateTime(System.currentTimeMillis());
        Integer update = this.update(config);
        if (update > 0) {
            return R.ok();
        }
        return R.fail("SYSTEM.0005", "数据库错误");
    }
}
