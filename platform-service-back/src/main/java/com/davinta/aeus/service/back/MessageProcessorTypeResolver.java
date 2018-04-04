/**
 * Copyright (C) Davinta Technologies 2017. All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of Davinta Technologies. You shall not disclose such Confidential Information
 * and shall use it only in accordance with the terms and conditions
 * entered into with Davinta Technologies.
 */

package com.davinta.aeus.service.back;

import org.springframework.stereotype.Component;

import com.davinta.aeus.util.kafka.ProcessorTypeResolver;

/**
 * MessageProcessorTypeResolver class.
 * @author Harish Mangala
 *
 */
@Component
public class MessageProcessorTypeResolver extends ProcessorTypeResolver {

	@Override
	public String getProcessor(Integer type, boolean isRequest) {
		return getReqResProcessor(type, isRequest ? REQUEST_PROCESSOR : RESPONSE_PROCESSOR);
	}

	private String getReqResProcessor(Integer type, String postFix) {
		return "front" + postFix;
	}

}
