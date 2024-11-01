package com.xiliulou.electricity.mapper;


import com.xiliulou.electricity.entity.payparams.WechatPublicKeyEntity;
import com.xiliulou.electricity.queryModel.WechatPublicKeyQueryModel;

import java.util.List;

/**
 * <p>
 * Description: This interface is WechatPublicKeyMapper!
 * </p>
 * <p>Project: saas-electricity</p>
 * <p>Copyright: Copyright (c) 2024</p>
 * <p>Company: xiliulou</p>
 *
 * @author <a href="mailto:wxblifeng@163.com">PeakLee</a>
 * @since V1.0 2024/11/1
 **/
public interface WechatPublicKeyMapper {
    
    int insert(WechatPublicKeyEntity entity);
    
    int delete(Long id);
    
    int update(WechatPublicKeyEntity entity);
    
    WechatPublicKeyEntity selectByQueryModel(WechatPublicKeyQueryModel entity);
    
    List<WechatPublicKeyEntity> selectListByQueryModel(WechatPublicKeyQueryModel entity);
}
