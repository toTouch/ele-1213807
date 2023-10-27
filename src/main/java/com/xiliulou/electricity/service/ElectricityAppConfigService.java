package com.xiliulou.electricity.service;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.ElectricityAppConfig;
import com.xiliulou.electricity.query.ElectricityAppConfigQuery;

/**
 * app配置(ElectricityAppConfig)表服务接口
 *
 * @author zhangyongbo
 * @since 2023-10-11
 */
public interface ElectricityAppConfigService extends IService<ElectricityAppConfig> {

    R edit(ElectricityAppConfigQuery electricityAppConfigQuery);
    
    R queryUserAppConfigInfo();
    
    Integer updateByUid(ElectricityAppConfig electricityAppConfig);
}
