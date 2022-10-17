enhantec-platform

- Initialize project:

        1. mvn install

- Deploy library artifact to cloudsmith:
        1. login to cloudsmith and create config files (config.ini and credentials.ini): cloudsmith login
        2. in the project scripts directory, run: ./deploy.sh [version-number]