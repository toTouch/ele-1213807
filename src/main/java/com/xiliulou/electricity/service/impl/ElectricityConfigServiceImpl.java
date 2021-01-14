package com.xiliulou.electricity.service.impl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.constant.ElectricityCabinetConstant;
import com.xiliulou.electricity.entity.ElectricityConfig;
import com.xiliulou.electricity.mapper.ElectricityConfigMapper;
import com.xiliulou.electricity.service.ElectricityConfigService;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.Objects;

/**
 * 用户列表(LoginInfo)表服务实现类
 *
 * @author makejava
 * @since 2020-12-07 15:00:00
 */
@Service("loginInfoService")
@Slf4j
public class ElectricityConfigServiceImpl extends ServiceImpl<ElectricityConfigMapper, ElectricityConfig> implements ElectricityConfigService {

    @Resource
    ElectricityConfigMapper electricityConfigMapper;
    @Autowired
    RedisService redisService;


    @Override
    public R edit(String name) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        //操作频繁
        boolean result = redisService.setNx(ElectricityCabinetConstant.ELE_CONFIG_EDIT_UID + user.getUid(), "1", 3 * 1000L, false);
        if (!result) {
            return R.fail("ELECTRICITY.0034", "操作频繁");
        }
        ElectricityConfig electricityConfig=electricityConfigMapper.selectOne(new LambdaQueryWrapper<>());
        if(Objects.isNull(electricityConfig)){
            electricityConfig=new ElectricityConfig();
            electricityConfig.setName(name);
            electricityConfig.setCreateTime(System.currentTimeMillis());
            electricityConfig.setUpdateTime(System.currentTimeMillis());
            electricityConfigMapper.insert(electricityConfig);
            return R.ok();
        }
        electricityConfig.setName(name);
        electricityConfig.setUpdateTime(System.currentTimeMillis());
        electricityConfigMapper.updateById(electricityConfig);
        return R.ok();
    }

    @Override
    public ElectricityConfig queryOne() {
        return electricityConfigMapper.selectOne(new LambdaQueryWrapper<>());
    }
}