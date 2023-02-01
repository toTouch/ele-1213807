package com.xiliulou.electricity.query;

import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.electricity.validator.UpdateGroup;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-01-31-15:40
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FaceRecognizeDataQuery {

    private Long size;

    private Long offset;

    private String tenantName;

    @NotNull(message = "id不能为空!", groups = {UpdateGroup.class})
    private Long id;
    /**
     * 人脸核身次数
     */
    @Min(0)
    @NotNull(message = "人脸核身次数不能为空!", groups = {CreateGroup.class, UpdateGroup.class})
    private Integer faceRecognizeCapacity;
    /**
     * 充值时间
     */
    private Long rechargeTime;

    @NotNull(message = "租户不能为空!", groups = {CreateGroup.class, UpdateGroup.class})
    private Integer tenantId;

}
