package com.xiliulou.electricity.vo.userinfo;

import com.xiliulou.electricity.enums.enterprise.RenewalStatusEnum;
import com.xiliulou.electricity.vo.DetailsBatteryInfoProVO;
import com.xiliulou.electricity.vo.EleBatteryServiceFeeVO;
import com.xiliulou.electricity.vo.EleDepositRefundVO;
import com.xiliulou.electricity.vo.enterprise.EnterpriseChannelUserVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @date 2025/2/24 18:03:24
 * @author HeYafeng
 */
@Data
public class UserEleInfoProV2VO {
    
    /**
     * 用户uid
     */
    private Long uid;
    
    /**
     * 用户姓名
     */
    private String name;
    
    /**
     * 手机号
     */
    private String phone;
    
    /**
     * 用户可用状态 0--启用，1--禁用
     */
    private Integer usableStatus;
    
    /**
     * 电池押金状态
     */
    private Integer batteryDepositStatus;
    
    /**
     * 电池租赁状态 0--未租电池，1--已租电池
     */
    private Integer batteryRentStatus;
    
    /**
     * 电池SN信息
     */
    private String batterySn;
    
    /**
     * 押金金额(元)
     */
    private BigDecimal deposit;
    
    /**
     * 交易方式 0 线上，1线下，2免押，3美团支付
     */
    private Integer depositPayType;
    
    /**
     * 是否免押:0-非免押 1-免押
     */
    private Integer freeDeposit;
    
    /**
     * 用户所产生的电池服务费
     */
    private BigDecimal userBatteryServiceFee;
    
    /**
     * 套餐id
     */
    private Long memberCardId;
    
    /**
     * 换电套餐名称
     */
    private String memberCardName;
    
    
    /**
     * 当前套餐到期时间
     */
    private Long orderExpireTime;
    
    /**
     * 当前套餐剩余次数
     */
    private Long orderRemainingNumber;
    
    /**
     * 套餐总到期时间
     */
    private Long memberCardExpireTime;
    
    /**
     * 套餐总剩余次数
     */
    private Long remainingNumber;
    
    /**
     * 所属加盟商
     */
    private Long franchiseeId;
    
    /**
     * 所属加盟商名称
     */
    private String franchiseeName;
    
    /**
     * 所属门店
     */
    private Long storeId;
    
    /**
     * 所属门店名称
     */
    private String storeName;
    
    /**
     * 用户状态:0-正常,1-已删除, 2-已注销
     */
    private Integer userStatus;
    
    /**
     * 删除/注销时间
     */
    private Long delTime;
    
    /**
     * 自主续费状态 0:不自主续费, 1:自主续费
     * @see RenewalStatusEnum
     */
    private Integer renewalStatus;
    
}
