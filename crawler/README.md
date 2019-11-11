# BMAP Crawler

## Prerequisites

- `Java 9 or higher`
- Maven

## Usage

1. Go to the [firebase console](https://console.firebase.google.com/) to create a project and download the private key file for administration. See [here](https://firebase.google.com/docs/admin/setup#initialize_the_sdk) for more information.
2. `mvn install` to install dependencies.
3. `mvn package` to build a deployment.
4. `GOOGLE_APPLICATION_CREDENTIALS=/path/to/firebase-adminsdk-credentials.json java -jar ./target/crawler-jar-with-dependencies.jar` to run the project. Do remember to change the value of the path to your firebase administrator credentials.
