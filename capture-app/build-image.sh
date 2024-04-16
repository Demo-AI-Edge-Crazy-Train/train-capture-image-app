mvn clean package
podman build -f src/main/docker/Dockerfile.opencv -t quarkus/train-capture-app-jvm --platform linux/arm64/v8 . 
podman tag quarkus/train-capture-app-jvm:latest quay.io/mouachan/train-capture-app-arm64:latest    
podman push quay.io/mouachan/train-capture-app-arm64:latest 