package com.xiliulou.electricity.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

/**
 * @author: eclair
 * @Date: 2020/4/29 14:09
 * @Description:
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DelayedMessage implements Delayed {
	private String message;
	private Long time;

	@Override
	public long getDelay(TimeUnit unit) {
		return time - System.currentTimeMillis();
	}

	@Override
	public int compareTo(Delayed o) {
		DelayedMessage item = (DelayedMessage) o;
		long diff = this.time - item.time;
		if (diff <= 0) {
			return -1;
		} else {
			return 1;
		}
	}
}