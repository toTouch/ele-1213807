package com.xiliulou.electricity.request.userinfo;

import com.xiliulou.electricity.request.userinfo.emergencyContact.EmergencyContactRequest;
import com.xiliulou.electricity.utils.ValidList;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;

/**
 * @author HeYafeng
 * @date 2024/11/11 11:26:04
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EleUserAuthRequest {
    
    public static final Integer DEL_NORMAL = 0;
    
    public static final Integer DEL_DEL = 1;
    
    //等待审核中
    public static Integer STATUS_PENDING_REVIEW = 0;
    
    //审核被拒绝
    public static Integer STATUS_REVIEW_REJECTED = 1;
    
    //审核通过
    public static Integer STATUS_REVIEW_PASSED = 2;
    
    private Long id;
    
    /**
     * 用户uid
     */
    private Long uid;
    
    /**
     * 资料项id
     */
    private Integer entryId;
    
    /**
     * 资料项内容
     */
    private String value;
    
    /**
     * 资料项状态 0--未审核，1--审核成功 2--审核失败
     */
    private Object status;
    
    /**
     * 删除标记
     */
    private Object delFlag;
    
    /**
     * 创建时间
     */
    private Long createTime;
    
    /**
     * 更新时间
     */
    private Long updateTime;
    
    /**
     * 租户id
     */
    private Integer tenantId;
    
    /**
     * 紧急联系人
     */
    @Valid
    private ValidList<EmergencyContactRequest> emergencyContactList;
    
}
