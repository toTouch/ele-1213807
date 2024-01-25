package com.xiliulou.electricity.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author HeYafeng
 * @description 批量发放优惠券
 * @date 2024/1/25 14:25:41
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CouponBatchReleaseRequest {
    
    /**
     * 优惠券ID
     */
    @NotNull(message = "优惠券ID不能为空!")
    private Integer id;
    
    /**
     * 要发放优惠券的uid
     */
    @NotEmpty(message = "uid不能为空!")
    private List<Long> uids;
    
}
