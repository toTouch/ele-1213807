package com.xiliulou.electricity.service.pay;


import com.xiliulou.core.web.R;
import com.xiliulou.electricity.bo.pay.WechatPublicKeyBO;
import com.xiliulou.electricity.request.payparams.WechatPublicKeyRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * <p>
 * Description: This interface is WechatPublicKeyService!
 * </p>
 * <p>Project: saas-electricity</p>
 * <p>Copyright: Copyright (c) 2024</p>
 * <p>Company: xiliulou</p>
 *
 * @author <a href="mailto:wxblifeng@163.com">PeakLee</a>
 * @since V1.0 2024/11/1
 **/
public interface WechatPublicKeyService {
    
    /**
     * <p>
     * Description: 根据Id查询微信公钥信息,优先查缓存
     * </p>
     * <p>Project: WechatPublicKeyService</p>
     * <p>Copyright: Copyright (c) 2024</p>
     * <p>Company: www.xiliulou.com</p>
     *
     * @param id id
     * @return com.xiliulou.electricity.bo.pay.WechatPublicKeyBO
     * @author <a href="mailto:wxblifeng@163.com">PeakLee</a>
     * @since V1.0 2024/11/1
     */
    WechatPublicKeyBO queryByIdFromCache(Long id);
    
    /**
     * <p>
     * Description: 根据Id查询微信公钥信息,不走缓存
     * </p>
     * <p>Project: WechatPublicKeyService</p>
     * <p>Copyright: Copyright (c) 2024</p>
     * <p>Company: www.xiliulou.com</p>
     *
     * @param id id
     * @return com.xiliulou.electricity.bo.pay.WechatPublicKeyBO
     * @author <a href="mailto:wxblifeng@163.com">PeakLee</a>
     * @since V1.0 2024/11/1
     */
    WechatPublicKeyBO queryByIdFromDB(Long id);
    
    /**
     * <p>
     * Description: 根据租户ID和加盟商ID查询微信公钥信息,优先查缓存 如果加盟商ID为空，则查询按照CreateTime倒序查询取第一条记录
     * </p>
     * <p>Project: WechatPublicKeyService</p>
     * <p>Copyright: Copyright (c) 2024</p>
     * <p>Company: www.xiliulou.com</p>
     *
     * @param tenantId     tenantId
     * @param franchiseeId franchiseeId
     * @return com.xiliulou.electricity.bo.pay.WechatPublicKeyBO
     * @author <a href="mailto:wxblifeng@163.com">PeakLee</a>
     * @since V1.0 2024/11/1
     */
    WechatPublicKeyBO queryByTenantIdFromCache(Long tenantId, Long franchiseeId);
    
    /**
     * <p>
     * Description: queryByTenantIdFromDB 根据租户ID和加盟商ID查询微信公钥信息,不走缓存 如果加盟商ID为空，则查询按照CreateTime倒序查询取第一条记录
     * <p>Project: WechatPublicKeyService</p>
     * <p>Copyright: Copyright (c) 2024</p>
     * <p>Company: www.xiliulou.com</p>
     * </p>
     *
     * @param tenantId     tenantId
     * @param franchiseeId franchiseeId
     * @return com.xiliulou.electricity.bo.pay.WechatPublicKeyBO
     * @author <a href="mailto:wxblifeng@163.com">PeakLee</a>
     * @since V1.0 2024/11/1
     */
    WechatPublicKeyBO queryByTenantIdFromDB(Long tenantId, Long franchiseeId);
    
    /**
     * <p>Title: queryListByTenantIdFromDB </p>
     * <p>Project: WechatPublicKeyService</p>
     * <p>Copyright: Copyright (c) 2024</p>
     * <p>Company: www.xiliulou.com</p>
     * <p>Description:
     * 根据租户ID和加盟商ID查询微信公钥信息,不走缓存 如果加盟商ID为空，则查询租户ID下的所有记录
     * </p>
     *
     * @param tenantId      tenantId
     * @param franchiseeIds franchiseeIds
     * @return java.util.List<com.xiliulou.electricity.bo.pay.WechatPublicKeyBO>
     * @author <a href="mailto:wxblifeng@163.com">PeakLee</a>
     * @since V1.0 2024/11/1
     */
    List<WechatPublicKeyBO> queryListByTenantIdFromDB(Long tenantId, List<Long> franchiseeIds);
    
    /**
     * <p>Title: queryListByTenantIdFromCache </p>
     * <p>Project: WechatPublicKeyService</p>
     * <p>Copyright: Copyright (c) 2024</p>
     * <p>Company: www.xiliulou.com</p>
     * <p>Description:
     * 根据租户ID和加盟商ID查询微信公钥信息,优先查缓存 如果加盟商ID为空，则查询租户ID下的所有记录
     * </p>
     *
     * @param tenantId      tenantId
     * @param franchiseeIds franchiseeIds
     * @return java.util.List<com.xiliulou.electricity.bo.pay.WechatPublicKeyBO>
     * @author <a href="mailto:wxblifeng@163.com">PeakLee</a>
     * @since V1.0 2024/11/1
     */
    List<WechatPublicKeyBO> queryListByTenantIdFromCache(Long tenantId, List<Long> franchiseeIds);
    
    /**
     * <p>Title: clearCache </p>
     * <p>Project: WechatPublicKeyService</p>
     * <p>Copyright: Copyright (c) 2024</p>
     * <p>Company: www.xiliulou.com</p>
     * <p>Description:
     * 清空所有缓存
     * </p>
     *
     * @param tenantId     tenantId
     * @param franchiseeId franchiseeId
     * @author <a href="mailto:wxblifeng@163.com">PeakLee</a>
     * @since V1.0 2024/11/1
     */
    void clearCache(Long tenantId, Long franchiseeId);
    
    /**
     * <p>Title: save </p>
     * <p>Project: WechatPublicKeyService</p>
     * <p>Copyright: Copyright (c) 2024</p>
     * <p>Company: www.xiliulou.com</p>
     * <p>Description: 保存微信公钥信息</p>
     *
     * @param wechatPublicKeyBO wechatPublicKeyBO
     * @return int
     * @author <a href="mailto:wxblifeng@163.com">PeakLee</a>
     * @since V1.0 2024/11/1
     */
    int save(WechatPublicKeyBO wechatPublicKeyBO);
    
    /**
     * <p>Title: update </p>
     * <p>Project: WechatPublicKeyService</p>
     * <p>Copyright: Copyright (c) 2024</p>
     * <p>Company: www.xiliulou.com</p>
     * <p>Description: 根据ID更新微信公钥信息</p>
     *
     * @param wechatPublicKeyBO wechatPublicKeyBO
     * @return int
     * @author <a href="mailto:wxblifeng@163.com">PeakLee</a>
     * @since V1.0 2024/11/1
     */
    int update(WechatPublicKeyBO wechatPublicKeyBO);
    
    /**
     * <p>Title: delete </p>
     * <p>Project: WechatPublicKeyService</p>
     * <p>Copyright: Copyright (c) 2024</p>
     * <p>Company: www.xiliulou.com</p>
     * <p>Description: 根据ID删除微信公钥ID,逻辑删除</p>
     *
     * @param id id
     * @return int
     * @author <a href="mailto:wxblifeng@163.com">PeakLee</a>
     * @since V1.0 2024/11/1
     */
    int delete(Long id);
    
    /**
     * <p>Title: uploadFile </p>
     * <p>Project: WechatPublicKeyService</p>
     * <p>Copyright: Copyright (c) 2024</p>
     * <p>Company: www.xiliulou.com</p>
     * <p>Description: 上传微信公钥信息</p>
     *
     * @param file         file
     * @param franchiseeId franchiseeId
     * @return com.xiliulou.core.web.R<?>
     * @author <a href="mailto:wxblifeng@163.com">PeakLee</a>
     * @since V1.0 2024/11/1
     */
    @Deprecated
    R<?> uploadFile(MultipartFile file, Long franchiseeId);
    
    /**
     * <p>Title: saveOrUpdate </p>
     * <p>Project: WechatPublicKeyService</p>
     * <p>Copyright: Copyright (c) 2024</p>
     * <p>Company: www.xiliulou.com</p>
     * <p>Description: 保存或更新微信公钥信息</p>
     * @param request request
     * @return com.xiliulou.core.web.R<?>
     * @author <a href="mailto:wxblifeng@163.com">PeakLee</a>
     * @since V1.0 2024/11/6
    */
    R<?> saveOrUpdate(WechatPublicKeyBO request);
}
