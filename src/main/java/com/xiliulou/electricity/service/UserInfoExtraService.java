package com.xiliulou.electricity.service;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.entity.UserInfoExtra;
import com.xiliulou.electricity.exception.BizException;
import com.xiliulou.electricity.request.merchant.MerchantModifyInviterRequest;
import com.xiliulou.electricity.request.merchant.MerchantModifyInviterUpdateRequest;
import com.xiliulou.electricity.request.userinfo.UserInfoLimitRequest;
import com.xiliulou.electricity.vo.merchant.MerchantInviterVO;
import org.apache.commons.lang3.tuple.Triple;

import java.util.List;

/**
 * (UserInfoExtra)表服务接口
 *
 * @author zzlong
 * @since 2024-02-18 10:39:59
 */
public interface UserInfoExtraService {
    
    UserInfoExtra queryByUidFromDB(Long uid);
    
    UserInfoExtra queryByUidFromCache(Long uid);
    
    UserInfoExtra insert(UserInfoExtra userInfoExtra);
    
    Integer updateByUid(UserInfoExtra userInfoExtra);
    
    Integer deleteByUid(Long uid);
    
    void bindMerchant(UserInfo userInfo, String orderId, Long memberCardId);
    
    MerchantInviterVO querySuccessInviter(Long uid);
    
    R selectInviterList(MerchantModifyInviterRequest request);
    
    R modifyInviter(MerchantModifyInviterUpdateRequest merchantModifyInviterUpdateRequest, Long operator, List<Long> franchiseeIds);
    
    MerchantInviterVO judgeInviterTypeForMerchant(Long joinUid, Long inviterUid, Integer tenantId);
    
    Triple<Boolean, String, String> isLimitPurchase(Long uid, Integer tenantId);
    
    R updateEleLimit(UserInfoLimitRequest request, List<Long> franchiseeIds);
    
    /**
     * 校验用户端申请冻结时，申请冻结次数是否符合配置
     *
     * @param tenantId 租户id
     * @param uid      申请冻结用户uid
     * @return 校验结果
     */
    R<Object> checkFreezeCount(Integer tenantId, Long uid);
    
    /**
     * 增加或减少用户套餐冻结次数，各业务中须提前校验申请冻结用户的扩展信息 UserInfoExtra 是否存在，本方法内的校验只能保证不报空指针
     *
     * @param uid  申请冻结用户uid
     * @param type 操作类型，传递常量UserInfoExtraConstant.ADD_FREEZE_COUNT或UserInfoExtraConstant.SUBTRACT_FREEZE_COUNT
     * @return 执行结果
     * @throws BizException 若捕获到BizException，为操作类行传递错误，各业务自行斟酌如何处理
     */
    R<Object> changeFreezeCountForUser(Long uid, Integer type) throws BizException;
    
    Integer getUnusedFreezeCount(Integer tenantId, Long uid) throws BizException;
}
