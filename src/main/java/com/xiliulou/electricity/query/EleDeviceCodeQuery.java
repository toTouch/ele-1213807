package com.xiliulou.electricity.query;

import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.electricity.validator.UpdateGroup;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EleDeviceCodeQuery {
    private Long size;
    private Long offset;
    
    @NotNull(message = "id不能为空", groups = UpdateGroup.class)
    private Long id;
    
    private String productKey;
    
    private String deviceName;
    
    /**
     * 密钥
     */
    private String secret;
    
    /**
     * 在线状态（0--在线，1--离线）
     */
    private Integer onlineStatus;
    
    @NotBlank(message = "备注不能为空", groups = UpdateGroup.class)
    @Length(min = 1, max = 50,message = "参数不合法")
    private String remark;
    
    private Integer delFlag;
    
    @Valid
    @NotEmpty(message = "参数不合法", groups = CreateGroup.class)
    @Size(min = 1, max = 500, message = "参数不合法")
    private Set<EleDeviceCodeInsertQuery> deviceNames;

    private Long startTime;

    private Long endTime;
}
