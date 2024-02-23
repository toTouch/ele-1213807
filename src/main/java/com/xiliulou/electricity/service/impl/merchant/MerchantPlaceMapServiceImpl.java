package com.xiliulou.electricity.service.impl.merchant;

import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.entity.merchant.MerchantPlaceMap;
import com.xiliulou.electricity.mapper.merchant.MerchantPlaceMapMapper;
import com.xiliulou.electricity.query.merchant.MerchantPlaceMapQueryModel;
import com.xiliulou.electricity.service.merchant.MerchantPlaceMapService;
import com.xiliulou.electricity.vo.merchant.MerchantPlaceMapVO;
import com.xiliulou.electricity.vo.merchant.MerchantPlaceUserVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Set;

/**
 * @author maxiaodong
 * @date 2024/2/6 16:56
 * @desc
 */
@Service("merchantPlaceMapService")
@Slf4j
public class MerchantPlaceMapServiceImpl implements MerchantPlaceMapService {
    @Resource
    private MerchantPlaceMapMapper merchantPlaceMapMapper;
    
    @Override
    public int batchInsert(List<MerchantPlaceMap> merchantPlaceMapList) {
        return merchantPlaceMapMapper.batchInsert(merchantPlaceMapList);
    }
    
    @Slave
    @Override
    public List<MerchantPlaceMap> queryList(MerchantPlaceMapQueryModel queryModel) {
        return merchantPlaceMapMapper.list(queryModel);
    }
    
    @Override
    public int batchDeleteByMerchantId(Long merchantId, Set<Long> placeIdList) {
        return merchantPlaceMapMapper.batchDeleteByMerchantId(merchantId, placeIdList);
    }
    
    /**	* 小程序：员工添加下拉框场地选择
     * @param merchantId
     * @return
     */
    @Slave
    @Override
    public List<MerchantPlaceUserVO> queryListByMerchantId(Long merchantId) {
        return merchantPlaceMapMapper.selectListByMerchant(merchantId);
    }
    
    /**
     * 商户编辑与新增页面查询场地下拉框数据
     * @param notMerchantId
     * @param franchiseeId
     * @return
     */
    @Slave
    @Override
    public List<MerchantPlaceMap> queryBindList(Long notMerchantId, Long franchiseeId) {
    
        return merchantPlaceMapMapper.selectBindList(notMerchantId, franchiseeId);
    }
    
    @Slave
    @Override
    public List<MerchantPlaceMapVO> queryBindMerchantName(MerchantPlaceMapQueryModel placeMapQueryModel) {
        return merchantPlaceMapMapper.selectBindMerchantName(placeMapQueryModel);
    }
}
