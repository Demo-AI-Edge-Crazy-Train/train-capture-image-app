package org.redhat.demo.crazytrain.consumer;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.Produces;
import jakarta.json.JsonObject;


@Path("/mqtt")
public class MqttResource {

    @Inject
    MqttConsumer consumer;

    // @GET
    // @Produces(MediaType.APPLICATION_JSON)
    // public JsonObject getLastMessage() {
    //     return consumer.getLastMessage();
    // }
}