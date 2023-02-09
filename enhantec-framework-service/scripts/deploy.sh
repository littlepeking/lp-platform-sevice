CLOUDSMITH_REPOSITORY=eh-xyp/enhantec-framework-service

cloudsmith push maven $CLOUDSMITH_REPOSITORY --pom-file ../../pom.xml ../../enhantec-framework-parent-service.jar


cloudsmith push maven $CLOUDSMITH_REPOSITORY --pom-file ../pom.xml ../target/enhantec-framework-service-$1.jar