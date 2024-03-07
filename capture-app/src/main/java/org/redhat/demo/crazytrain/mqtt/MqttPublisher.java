package org.redhat.demo.crazytrain.mqtt;

import java.net.URI;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.jboss.logging.Logger;

public class MqttPublisher {
    private static final Logger LOGGER = Logger.getLogger(MqttPublisher.class);

    private final String broker;
    private final String topic;

    public MqttPublisher(String broker, String topic) {
        this.broker = broker;
        this.topic = topic;
    }

    public void publish(String content) throws MqttException {
        String clientId = MqttClient.generateClientId();
        try {
        LOGGER.infof("Publishing message to broker: %s", broker);
        MqttClient client = new MqttClient(broker, clientId, new MemoryPersistence());

        MqttConnectOptions connOpts = new MqttConnectOptions();
        connOpts.setCleanSession(true);
        client.connect(connOpts);
        LOGGER.infof("Connected to broker: %s", broker);
        MqttMessage message = new MqttMessage(content.getBytes());
        client.publish(topic, message);
        LOGGER.infof("Message published to topic: %s", topic);
        client.disconnect();
    } catch (Exception e) {
        LOGGER.errorf("Error publishing message: %s", e.getMessage());
    }
    }
}
