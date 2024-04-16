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

    private MqttClient client;
    // Constructor
    public MqttPublisher(String broker, String topic) {
        this.broker = broker;
        this.topic = topic;
    }

    // Connect to the MQTT broker
    public void connect() {
        try {
            // Generate a client ID
            String clientId = MqttClient.generateClientId();
            // Create a new MQTT client
            client = new MqttClient(broker, clientId, new MemoryPersistence());
            // Connect to the MQTT broker
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            LOGGER.debugf("Connecting to broker: %s", broker);
            client.connect(connOpts);
            LOGGER.debugf("Connected to broker: %s", broker);
        } catch (MqttException e) {
            LOGGER.errorf("Error connecting to broker: %s", e.getMessage());
        }
    }
    // Publish a message to the MQTT broker
    public void publish(String content) throws MqttException {
        // Generate a client ID
        try {
            LOGGER.debugf("Publishing message to broker: %s", broker);
            // Publish the message
            MqttMessage message = new MqttMessage(content.getBytes());
            message.setQos(0);
            client.publish(topic, message);
            LOGGER.debugf("Message published to topic: %s", topic);
            //client.disconnect();
        } catch (Exception e) {
            LOGGER.errorf("Error publishing message: %s", e.getMessage());
        }
    }
    // Disconnect from the MQTT broker
    public void disconnect() {
        try {
            client.disconnect();
            LOGGER.debugf("Disconnected from broker: %s", broker);
        } catch (MqttException e) {
            LOGGER.errorf("Error disconnecting from broker: %s", e.getMessage());
        }
    }   
}
