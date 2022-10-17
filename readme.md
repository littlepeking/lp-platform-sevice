enhantec-platform

Initialize project:
mvn install

3.login to cloudsmith and create config files (config.ini and credentials.ini): cloudsmith login

4.start project from intellij.

Deploy library artifact to cloudsmith:
in the project scripts directory, run: ./deploy.sh [version-number]