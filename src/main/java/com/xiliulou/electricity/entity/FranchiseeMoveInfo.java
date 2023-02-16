package com.xiliulou.electricity.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-02-06-14:19
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FranchiseeMoveInfo {
    /**
     * 迁移前加盟商ID
     */
    private Long fromFranchiseeId;

    /**
     * 迁移后加盟商ID
     */
    private Long toFranchiseeId;

    /**
     * 迁移前加盟名字
     */
    private String fromFranchiseeName;

    /**
     * 迁移后加盟商名字
     */
    private String toFranchiseeName;

    /**
     * 电池型号
     */
    private Integer batteryModel;

}
