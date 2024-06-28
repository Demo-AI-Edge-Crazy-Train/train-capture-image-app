# Demo Stop The Crazy Train - Capture app

![lego](https://www.lego.com/cdn/cs/set/assets/blt95604d8cc65e26c4/CITYtrain_Hero-XL-Desktop.png?fit=crop&format=webply&quality=80&width=1600&height=1000&dpr=1)

# Capture-App

Capture-App is a RESTful web service built with Quarkus that allows you to start and stop video capture.
## Prerequisites

- **OpenCV**: Capture-App uses OpenCV for video processing. You need to have OpenCV installed on your machine to run Capture-App.
- **MQTT Broker**: Capture-App uses MQTT for messaging. You need to have an MQTT broker running and accessible to Capture-App. The MQTT broker's URL should be specified in the `mqtt.broker.url` property in the `application.properties` file.
- **Video File**: If you want to use the test mode where Capture-App reads video from a file, you need to have a video file available and its path should be specified in the `videoPath` property in the `application.properties` file.

## Related Modules

Capture-App is part of a larger system that includes the following microservices:
- **intelligent-train**: This microservice is responsible for making predictions based on the data received from the Capture-App.
- **train-ceq-app**: This microservice is responsible for post-processing the raw predictions made by the Intelligent-Train module, transforming them into actionable insights.
- **train-monitoring-app**: This microservice is responsible for receiving the CloudEvent from Train-CEQ-App via Kafka and using this information for monitoring and visualization purposes.
- **train-controller**: This microservice is responsible for receiving decisions from Train-CEQ-App and controlling the operation of the train accordingly.

## Endpoints

- `POST /capture/start`: Starts the video capture. If the capture is already running, it returns a 400 Bad Request response.
- `POST /capture/stop`: Stops the video capture.
- `POST /capture/test`: Starts reading video from a file. If the capture is already running, it returns a 400 Bad Request response.


## How to run
Clone the repository: git clone https://github.com/Demo-AI-Edge-Crazy-Train/train-capture-image-app
Navigate to the project directory: cd capture-app
Run the application dev mode : ./mvnw clean quarkus:dev

## How to use

You can use any HTTP client to send requests to these endpoints. For example, you can use `curl`:

```bash
# Start the video capture
curl -X 'POST' 'http://localhost:8082/capture/start' -H 'accept: */*'
```

```bash
# Stop the video capture
curl -X 'POST' 'http://localhost:8082/capture/stop' -H 'accept: */*'
```

In order to test, you may need to change the following properties in your `application.properties` file:

mock: This property should be set to true to enable the test mode where the application reads video from a file instead of capturing video from a camera.

videoPath: This property should be set to the path of the video file that the application will read when the test mode is enabled.

Here's an example of how you can set these properties:

mp4
Please replace /path/to/your/video/file.mp4 with the actual path of your video file.

After you have updated the application.properties file, you can start the application and use the POST /capture/test endpoint to start reading video from the specified file.
```bash
# Start reading video from a file
curl -X 'POST' 'http://localhost:8082/capture/test' -H 'accept: */*'
```

## Dependencies
Capture-App has the following dependencies:

- **OpenCV**: A library of programming functions mainly aimed at real-time computer vision. Used in this project for video capture and processing.
- **Eclipse Paho MQTT Client**: A Java client library for MQTT. Used for sending the captured video data to the Intelligent-Train module.
- **Quarkus**: A Kubernetes-native Java stack tailored for GraalVM and OpenJDK HotSpot, used for building lightweight and high-performance applications.

These dependencies are managed by Maven and are specified in the `pom.xml` file.

## License
This project is licensed under the MIT License - see the LICENSE file for details.