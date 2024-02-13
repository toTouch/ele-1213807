package com.xiliulou.electricity.service.impl.merchant;

import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.entity.merchant.MerchantPlace;
import com.xiliulou.electricity.mapper.merchant.MerchantMapper;
import com.xiliulou.electricity.mapper.merchant.MerchantPlaceMapper;
import com.xiliulou.electricity.query.merchant.MerchantPlaceQueryModel;
import com.xiliulou.electricity.service.merchant.MerchantPlaceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author maxiaodong
 * @date 2024/2/6 14:46
 * @desc
 */
@Service("merchantPlaceService")
@Slf4j
public class MerchantPlaceServiceImpl implements MerchantPlaceService {
    @Resource
    private MerchantMapper merchantMapper;
    
    @Slave
    @Override
    public Integer existsByAreaId(Long areaId) {
        return merchantMapper.existsByAreaId(areaId);
    }
    private MerchantPlaceMapper merchantPlaceMapper;
    
    @Slave
    @Override
    public List<MerchantPlace> queryList(MerchantPlaceQueryModel placeQueryModel) {
        return merchantPlaceMapper.list(placeQueryModel);
    }
}
