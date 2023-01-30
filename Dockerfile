FROM adoptopenjdk/openjdk11

ADD target/scala-2.13/BowlingGame.jar app.jar

EXPOSE 8080

CMD ["java","-jar","app.jar"]