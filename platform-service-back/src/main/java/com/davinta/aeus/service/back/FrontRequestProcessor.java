/**
 * Copyright (C) Davinta Technologies 2017. All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of Davinta Technologies. You shall not disclose such Confidential Information
 * and shall use it only in accordance with the terms and conditions
 * entered into with Davinta Technologies.
 */

package com.davinta.aeus.service.back;

import java.util.concurrent.atomic.AtomicLong;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.davinta.aeus.messaging.customer.FrontRequest;
import com.davinta.aeus.messaging.customer.FrontResponse;
import com.davinta.aeus.util.kafka.MessageContext;
import com.davinta.aeus.util.kafka.RequestProcessor;

/**
 * FrontRequestProcessor class.
 * @author Harish Mangala
 *
 */
@Component
public class FrontRequestProcessor implements RequestProcessor<FrontRequest> {

	@Autowired
	FrontResponseProcessor frontResponseProcessor;

	static AtomicLong atomicLong = new AtomicLong();

	@Override
	public Class<FrontRequest> getRequestClass() {
		return FrontRequest.class;
	}

	@Override
	public void processRequest(MessageContext<FrontRequest> context) {
		FrontResponse frontResponse = new FrontResponse();
		frontResponse.setMessage(context.getMessage());
		context.getMessage().setId(atomicLong.incrementAndGet());
		frontResponseProcessor.processResponse(new MessageContext<>(frontResponse, context.getHeaders()));
	}
}
