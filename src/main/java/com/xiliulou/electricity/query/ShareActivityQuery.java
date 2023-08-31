package com.xiliulou.electricity.query;

import com.xiliulou.electricity.enums.ActivityEnum;
import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.electricity.validator.UpdateGroup;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author: lxc
 * @Date: 2021/4/15 16:02
 * @Description:
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ShareActivityQuery {
    private Long size;
    private Long offset;
    private String name;
    private List<Integer> typeList;
    private Long uid;
    /**
     * 加盟商Id
     */
    private List<Long> franchiseeIds;

    private Integer tenantId;

    /**
     * 活动状态
     */
    private Integer status;


    @NotNull(message = "活动id不能为空!", groups = {UpdateGroup.class})
    private Integer id;

    /**
     * 可发放优惠券的套餐
     */
    @NotEmpty(message = "领券套餐不能为空!", groups = {UpdateGroup.class})
    private List<Long> membercardIds;

    /**
     * 邀请标准 0-登录注册 1-实名认证 2-购买套餐
     * @see ActivityEnum
     */
    private Integer invitationCriteria;

}
