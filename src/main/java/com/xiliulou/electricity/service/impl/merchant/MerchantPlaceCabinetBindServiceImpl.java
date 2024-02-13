package com.xiliulou.electricity.service.impl.merchant;

import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.entity.merchant.MerchantPlaceCabinetBind;
import com.xiliulou.electricity.mapper.merchant.MerchantPlaceCabinetBindMapper;
import com.xiliulou.electricity.query.merchant.MerchantPlaceCabinetBindQueryModel;
import com.xiliulou.electricity.service.merchant.MerchantPlaceCabinetBindService;
import com.xiliulou.electricity.vo.merchant.MerchantPlaceCabinetBindVo;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author maxiaodong
 * @date 2024/2/11 9:53
 * @desc
 */
@Service("merchantPlaceCabinetBindService")
public class MerchantPlaceCabinetBindServiceImpl implements MerchantPlaceCabinetBindService {
    @Resource
    private MerchantPlaceCabinetBindMapper merchantPlaceCabinetBindMapper;
    
    @Slave
    @Override
    public List<MerchantPlaceCabinetBind> queryList(MerchantPlaceCabinetBindQueryModel placeCabinetBindQueryModel) {
        return merchantPlaceCabinetBindMapper.list(placeCabinetBindQueryModel);
    }
    
    @Slave
    @Override
    public List<MerchantPlaceCabinetBindVo> queryListByMerchantId(MerchantPlaceCabinetBindQueryModel placeCabinetBindQueryModel) {
        return merchantPlaceCabinetBindMapper.listByMerchantId(placeCabinetBindQueryModel);
    }
}
