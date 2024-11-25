# Using an image openjdk17-slim
FROM openjdk:17-slim

# Setting up the WORKDIR
WORKDIR /app

# Installing tools necessary for ffmpeg installation
RUN apt-get update && apt-get install -y wget unzip

# Downloading ffmpeg for linux
RUN wget https://johnvansickle.com/ffmpeg/releases/ffmpeg-release-amd64-static.tar.xz

# Unzipping downloaded ffmpeg
RUN apt-get install -y xz-utils
RUN tar -xvf ffmpeg-release-amd64-static.tar.xz

# Moving ffmpeg to /usr/bin
RUN mv ffmpeg-*-amd64-static/ffmpeg /usr/bin/ffmpeg
RUN mv ffmpeg-*-amd64-static/ffprobe /usr/bin/ffprobe

# Creating folders for the app
RUN mkdir -p /app/resources/media/avatars /app/resources/media/hls /app/resources/media/raw /app/resources/media/thumbnails /app/resources/media/temp

# Copying JAR file into the container
COPY target/Angle-0.0.1-SNAPSHOT.jar backend.jar

# Exposing the backend port
EXPOSE 7700

# Environment Variables. Please fill them according to the README.md


# Running the app
CMD ["java", "-jar", "backend.jar"]