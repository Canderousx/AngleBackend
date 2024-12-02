# Angle Backend Application

This project has been created by Maciej Miroński (Canderousx).
It's a backend application for YouTube-like website called *Angle*.

This version is the latest one and has been adapted for deployment on servers running Ubuntu. The use of Docker is recommended.

**UPDATE: Currently working on dividing the project into microservices with kafka as a messenger.

## Getting Started

These instructions will get you a copy of the project up and running on your local machine for development and testing purposes.

### Prerequisites

The project requires the following:

Spring Boot 3.2.3

Spring Security 6.2.2

Java 17

Maven 3.9.5

MySQL 8.0.36

FFMpeg (*https://www.ffmpeg.org/*)


## Installation

If you're using IntelliJ IDEA, simply copy HTTPS clone url and paste it in New/Project from Version Control/ tab

*https://github.com/Canderousx/AngleBackend.git*

Make sure you've MySQL 8.0.36.0 installed on your system.

### Setting up Environment Variables

In order to make app working you need to create following environment variables in your system,
where paths, secret keys and other vulnerable data are safe to store.

List of required environments variables: 


| *Variable name* | *Value*                                                                                                           |
| --------------- |-------------------------------------------------------------------------------------------------------------------|
 | ANGLE_DB_USER  | *username to your MySQL database*                                                                                 |
| ANGLE_DB_PASSWORD| *password to your MySQL database*                                                                                 |
| ANGLE_DB_URL | jdbc:mysql:{*url to your MySQL database*}                                                                         |
| ANGLE_AVATARS_PATH| *A path to a folder where user avatars are meant to be stored on the server* example: */app/resources/media/avatars*  |
| ANGLE_EMAIL_ADDRESS| *Your gmail address that will be used to send auto emails through smtp*                                           |
| ANGLE_EMAIL_PASSWORD| *Password to your gmail account that will be used to send auto emails through smtp*                               |
| ANGLE_FRONT_URL| *Url to frontend. For example 'http://{IP ADDRESS}'*                                                              |
| ANGLE_RAW_FILES_PATH | *A path to a folder where an app will store raw video files uploaded by the user* example: */app/resources/media/raw* |
| ANGLE_THUMBNAILS_PATH| *A path to a folder where video thumbnails will be stored* example: */app/resources/media/raw*                    |
| FFMPEG_PATH| *A path to a 'ffmpeg* example: */usr/bin/ffmpeg*                                                                      |
| FFMPEG_TEMP_FOLDER| *A path to a folder where FFMpeg will store its temp files* example: */app/resources/media/temp/*                     |
|FFMPEG_TEMP_THUMBNAILS_PATH| *A path to a folder where FFMpeg will store its temp thumbnails files* example: */app/resources/media/thumbnails*     |
| HLS_OUTPUT_PATH| *A path to a folder where FFMpeg will store hls files* example: */app/resources/media/hls*                            |
|JTOKEN_KEY| *Your secret key for JSON WEB TOKENS*                                                                             |

Make sure that media files are stored within *resources* folder, or you'll have to change *spring.resources.static-locations* in application.properties file.


After that the app should start working properly.


## Running the app

To start the app, you need to run *AngleApplication.java* file in your IDE. Make sure your MySQL server is up and running.

# Deployment

## Building the project with Maven

### Using Intellij IDEA

1. Open the project in Intellij IDEA
2. Open the Maven tab and run *clean* lifecycle
3. Run *install* lifecycle.

### Using PowerShell

1. Navigate to the project directory. Make sure you're where the pom.xml file is located.
2. Run clean and package command: *mvn clean package*



**Ensure that the path to the generated JAR file is correctly specified in your Dockerfile.**


## Building the Docker Image

*Ensure Docker is installed on your system.*

*Make sure you've defined the necessary environment variables in the Dockerfile.*

1. Open PowerShell and navigate to the project folder where your Dockerfile is located.
2. To build the Docker image, run the following command:

*docker build -t image_name*

3. To verify that the image has been created, use the command:

*docker images*

### Saving the Image as a .tar File

To save the Docker image as a .tar file, use the following command:

*docker save -o name_of_file.tar image_name:version*. 

This command will create a .tar file in the current directory.


## Preparing for Server Deployment

Once the image is saved, it's ready to be moved to the server. 
However, to fully deploy the application, you will also need the Docker image for the Angle Frontend and a docker-compose.yml file.



## Built with

Spring Boot 3.2.3

Spring Security 6.2.2

Java 17

Maven 3.9.5

MySQL 8.0.36


## Author

The app has been written by Maciej Miroński (Canderousx)














