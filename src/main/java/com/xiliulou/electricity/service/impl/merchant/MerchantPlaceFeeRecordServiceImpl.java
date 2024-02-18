package com.xiliulou.electricity.service.impl.merchant;

import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.entity.merchant.MerchantPlaceFeeRecord;
import com.xiliulou.electricity.mapper.merchant.MerchantPlaceFeeRecordMapper;
import com.xiliulou.electricity.query.merchant.MerchantPlaceFeeRecordQueryModel;
import com.xiliulou.electricity.request.merchant.MerchantPlaceFeeRecordPageRequest;
import com.xiliulou.electricity.service.UserService;
import com.xiliulou.electricity.service.merchant.MerchantPlaceFeeRecordService;
import com.xiliulou.electricity.vo.merchant.MerchantPlaceFeeRecordVO;
import com.xiliulou.electricity.vo.merchant.MerchantPlaceVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author maxiaodong
 * @date 2024/2/17 22:35
 * @desc
 */
@Service("merchantPlaceMapService")
@Slf4j
public class MerchantPlaceFeeRecordServiceImpl implements MerchantPlaceFeeRecordService {
    
    @Resource
    private MerchantPlaceFeeRecordMapper merchantPlaceFeeRecordMapper;
    
   @Resource
   private UserService userService;
    
    @Slave
    @Override
    public Integer countTotal(MerchantPlaceFeeRecordPageRequest merchantPlaceFeePageRequest) {
        MerchantPlaceFeeRecordQueryModel merchantPlaceFeeQueryModel = new MerchantPlaceFeeRecordQueryModel();
        BeanUtils.copyProperties(merchantPlaceFeePageRequest, merchantPlaceFeeQueryModel);
        
        return merchantPlaceFeeRecordMapper.countTotal(merchantPlaceFeeQueryModel);
    }
    
    @Slave
    @Override
    public List<MerchantPlaceFeeRecordVO> listByPage(MerchantPlaceFeeRecordPageRequest merchantPlaceFeePageRequest) {
        MerchantPlaceFeeRecordQueryModel merchantPlaceFeeQueryModel = new MerchantPlaceFeeRecordQueryModel();
        BeanUtils.copyProperties(merchantPlaceFeePageRequest, merchantPlaceFeeQueryModel);
    
        List<MerchantPlaceFeeRecord> merchantPlaceFeeRecordList = this.merchantPlaceFeeRecordMapper.selectListByPage(merchantPlaceFeeQueryModel);
        if (ObjectUtils.isEmpty(merchantPlaceFeeRecordList)) {
            return Collections.EMPTY_LIST;
        }
    
        List<MerchantPlaceFeeRecordVO> voList = new ArrayList<>();
        merchantPlaceFeeRecordList.forEach(item -> {
            MerchantPlaceFeeRecordVO vo = new MerchantPlaceFeeRecordVO();
            
            BeanUtils.copyProperties(item, vo);
            
            // 查询修改人名称
            User user = userService.queryByUidFromCache(item.getModifyUserId());
            if (Objects.nonNull(user)) {
                vo.setModifyUserName(user.getName());
            }
            voList.add(vo);
        });
        
        return voList;
    }
    
    @Override
    public Integer save(MerchantPlaceFeeRecord merchantPlaceFeeRecord) {
        return merchantPlaceFeeRecordMapper.insert(merchantPlaceFeeRecord);
    }
}
