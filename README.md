# Angle Backend Application

This project has been created by Maciej Miroński (Canderousx).
It's a backend application for YouTube-like website called *Angle*.

This version has been adapted for deployment on servers running Ubuntu. The use of Docker is recommended.

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
| ANGLE_AVATARS_PATH| *A path to a folder where user avatars are meant to be stored on the server* egz: */app/resources/media/avatars*  |
| ANGLE_EMAIL_ADDRESS| *Your gmail address that will be used to send auto emails through smtp*                                           |
| ANGLE_EMAIL_PASSWORD| *Password to your gmail account that will be used to send auto emails through smtp*                               |
| ANGLE_FRONT_URL| *Url to frontend. For example 'http://{IP ADDRESS}'*                                                              |
| ANGLE_RAW_FILES_PATH | *A path to a folder where an app will store raw video files uploaded by the user* egz: */app/resources/media/raw* |
| ANGLE_THUMBNAILS_PATH| *A path to a folder where video thumbnails will be stored* egz: */app/resources/media/raw*                        |
| FFMPEG_PATH| *A path to a 'ffmpeg* egz: */usr/bin/ffmpeg*                                                                      |
| FFMPEG_TEMP_FOLDER| *A path to a folder where FFMpeg will store its temp files* egz: */app/resources/media/temp/*                     |
|FFMPEG_TEMP_THUMBNAILS_PATH| *A path to a folder where FFMpeg will store its temp thumbnails files* egz: */app/resources/media/thumbnails*     |
| HLS_OUTPUT_PATH| *A path to a folder where FFMpeg will store hls files* egz: */app/resources/media/hls*                            |
|JTOKEN_KEY| *Your secret key for JSON WEB TOKENS*                                                                             |

Make sure that media files are stored within *resources* folder, or you'll have to change *spring.resources.static-locations* in application.properties file.


After that the app should start working properly.


## Running the app

To start the app, you need to run *AngleApplication.java* file in your IDE. Make sure your MySQL server is up and running.

## Deployment

*available soon*



## Built with

Spring Boot 3.2.3

Spring Security 6.2.2

Java 17

Maven 3.9.5

MySQL 8.0.36


## Author

The app has been written by Maciej Miroński (Canderousx)














