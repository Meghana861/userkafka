package com.amzur.producer

import com.amzur.userentities.User
import io.micronaut.configuration.kafka.annotation.KafkaClient
import io.micronaut.configuration.kafka.annotation.Topic

@KafkaClient
interface MessageProducer {
    @Topic("imp-topic")
    def sendMessage(def user)
}
