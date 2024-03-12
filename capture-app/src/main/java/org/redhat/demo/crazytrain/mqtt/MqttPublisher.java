package org.redhat.demo.crazytrain.mqtt;


import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.jboss.logging.Logger;

/**
 * MQTT publish class to publish message to MQTT broker
 */
public class MqttPublisher {
    private static final Logger LOGGER = Logger.getLogger(MqttPublisher.class);
    // broker is the MQTT broker
    private final String broker;
    // topic is the MQTT topic
    private final String topic;
    // Constructor
    public MqttPublisher(String broker, String topic) {
        this.broker = broker;
        this.topic = topic;
    }
    // Publish a message to the MQTT broker
    public void publish(String content) throws MqttException {
        // Generate a client ID
        String clientId = MqttClient.generateClientId();
        try {

            LOGGER.debugf("Publishing message to broker: %s", broker);
            MqttClient client = new MqttClient(broker, clientId, new MemoryPersistence());
            // Connect to the broker
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            client.connect(connOpts);
            LOGGER.debugf("Connected to broker: %s", broker);
            // Publish the message
            MqttMessage message = new MqttMessage(content.getBytes());
            client.publish(topic, message);
            LOGGER.debugf("Message published to topic: %s", topic);
            client.disconnect();
    } catch (Exception e) {
        LOGGER.errorf("Error publishing message: %s", e.getMessage());
    }
    }
}
