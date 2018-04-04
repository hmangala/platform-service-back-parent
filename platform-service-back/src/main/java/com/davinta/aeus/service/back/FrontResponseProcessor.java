/**
 * Copyright (C) Davinta Technologies 2017. All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of Davinta Technologies. You shall not disclose such Confidential Information
 * and shall use it only in accordance with the terms and conditions
 * entered into with Davinta Technologies.
 */

package com.davinta.aeus.service.back;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.IntegrationMessageHeaderAccessor;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.stereotype.Component;

import com.davinta.aeus.integration.kafka.KafkaProducerConfiguration;
import com.davinta.aeus.messaging.base.Status;
import com.davinta.aeus.messaging.customer.FrontResponse;
import com.davinta.aeus.util.kafka.KafkaUtil;
import com.davinta.aeus.util.kafka.MessageContext;
import com.davinta.aeus.util.kafka.ResponseProcessor;
import com.davinta.aeus.util.logging.PlatformLogger;

/**
 * FrontResponseProcessor class.
 * @author Harish Mangala
 *
 */
@Component
public class FrontResponseProcessor implements ResponseProcessor<FrontResponse> {
	PlatformLogger logger = PlatformLogger.getLogger(getClass());

	@Autowired
	private KafkaProducerConfiguration kafkaProducerConfiguration;

	@Autowired
	MessageChannel toKafka;

	@Override
	public Class<FrontResponse> getResponseClass() {
		return FrontResponse.class;
	}

	@Override
	public void processResponse(MessageContext<FrontResponse> context) {
		context.getMessage().setStatus(new Status(Status.SUCCESS));
		String messageKey = context.getHeaders().get(KafkaHeaders.RECEIVED_MESSAGE_KEY) + "";
		String correlationId = context.getHeaders().get(IntegrationMessageHeaderAccessor.CORRELATION_ID) + "";
		Message<String> message = KafkaUtil.buildMessageWithId(context.getMessage(), kafkaProducerConfiguration.getTopicSend(), messageKey, 100, correlationId);
		logger.info("Sending request to Kafka.........." + message.getHeaders());
		toKafka.send(message);
	}

}
