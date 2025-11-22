# 이 이미지를 빌드하는 데 사용할 **기본 이미지(Base Image)**를 지정
FROM eclipse-temurin:21-jdk-jammy

#Dockerfile의 관리자
LABEL maintanier="bank.com"

#build/libs/accounts-0.0.1-SNAPSHOT.jar : 로컬 파일 경로
#accounts-0.0.1-SNAPSHOT.jar : 복사된 파일이 도커 이미지 내부에 저장될 경로 및 이름
COPY build/libs/accounts-0.0.1-SNAPSHOT.jar accounts-0.0.1-SNAPSHOT.jar

# 도커 이미지로부터 생성하려고 할 때 실행
ENTRYPOINT ["java", "-jar", "accounts-0.0.1-SNAPSHOT.jar"]