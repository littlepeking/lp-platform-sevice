enhantec-platform

- Initialize project:

        1. mvn install

- Deploy library artifact to cloudSmith:
  
        1. configure your $HOME/.m2/settings.xml file with the API key of the uploading user

        2. change version number in projectDir/.mvn/Maven.config file
  
        2. mvn clean package

        3, mvn clean deploy