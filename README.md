# P3
Time Registration System

Prerequisites: 

Before running the program, ensure the following tools are installed on your system:

- Java Development Kit (JDK):
  - Version: Java 22 or later.

- IntelliJ IDEA:
  - Community or Ultimate edition.

- Maven:
  - Version: 3.9.9 or later

Dependencies:

The project uses Maven for dependency management. Dependencies will be automatically resolved during the build process.
- Optionally you can manually download the dependencies with maven in IntelliJ IDEA


To run the Program

1. Clone the Repository

2. Open the Project in IntelliJ IDEA

3. Verify Project Structure

Ensure the project uses the correct JDK version:

Go to File > Project Structure > Project Settings > Project.

Set the Project SDK to Java 22 or later.

Confirm Maven is configured:

Go to File > Settings > Build, Execution, Deployment > Build Tools > Maven.

Ensure the Maven home directory is set correctly.

4. Run the Application

Locate the "Launcher.java" class in the com.p3 folder.
- You can run the project directly from the file, or set up a run configuration that runs the class when the project is run
  - This option can be enabled in the top right of IntelliJ IDEA

If any issues occur where javaFX cannot be found, manually install the latest JavaFX and in the run configuration add VM options and add the following line to fix any potential issues

--module-path "{PATH_TO_JAVAFX-SDK}\javafx-sdk-23.0.1\lib" --add-modules javafx.controls,javafx.fxml

Replace {PATH_TO_JAVAFX-SDK} with the path to your JavaFX-sdk\lib

5. Build an Executable (Optional)

To create an executable version of the program, follow these steps:

-  Clean and Package the Project

   - Run the following Maven command:

   - "mvn clean package"
     - If "mvn" is an unrecognized command in the CMD, it possibly needs to be added to your computers "path" enviorment variables

This will clean the project and create a JAR file in the target directory.

- Create an Executable Using jpackage
   - JPackage comes with Java 22 and should already be on your system
   - Run the following command:
   - (Replace "^" with "\\" if not on windows)

   - "jpackage ^
   --input target ^
   --name Timelogger ^
   --main-jar 1.0-SNAPSHOT.jar ^
   --main-class com.p3.Launcher" ^
   --type exe ^
   --icon src/main/resources/icons/favicon.ico

This will generate an executable file.

 * Notes

Ensure the necessary style sheets (CSS) and icons are available in the /resources folder for proper rendering of the JavaFX application.

If you encounter issues, check the logs in IntelliJ IDEA's console or Maven's output for details.