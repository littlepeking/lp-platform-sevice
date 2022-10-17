CLOUDSMITH_REPOSITORY=eh-xyp/enhantec-platform-service


cloudsmith push maven $CLOUDSMITH_REPOSITORY --pom-file ../pom.xml --pom-file ../../pom.xml ../target/enhantec-framework-service-$1.jar