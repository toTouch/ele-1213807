package com.xiliulou.electricity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author: eclair
 * @Date: 2020/7/1 09:32
 * @Description:
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EleOpenDTO {
	private String sessionId;
	private Boolean operResult;
}
