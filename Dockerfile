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
ENV ANGLE_AVATARS_PATH=/app/resources/media/avatars
ENV ANGLE_DB_PASSWORD=root
ENV ANGLE_DB_URL=jdbc:mysql://mysql:3306/angleDB?useSSL=false&allowPublicKeyRetrieval=true&characterEncoding=UTF-8&serverTimezone=UTC
ENV ANGLE_DB_USER=root
ENV ANGLE_EMAIL_ADDRESS=mironskym@gmail.com
ENV ANGLE_EMAIL_PASSWORD="tzcx glkc wxed fcsy"
ENV ANGLE_FRONT_URL=http://142.93.104.248
ENV ANGLE_HLS_FILES_PATH=/app/resources/media/hls
ENV ANGLE_RAW_FILES_PATH=/app/resources/media/raw
ENV ANGLE_THUMBNAILS_PATH=/app/resources/media/thumbnails
ENV FFMPEG_PATH=/usr/bin/ffmpeg
ENV FFMPEG_TEMP_FOLDER=/app/resources/media/temp/
ENV FFMPEG_TEMP_THUMBNAILS_PATH=/app/resources/media/temp
ENV HLS_OUTPUT_PATH=/app/resources/media/hls
ENV JTOKEN_KEY=357638792F423F4428472B4B6250655368566D597133743677397A2443264629

# Running the app
CMD ["java", "-jar", "backend.jar"]