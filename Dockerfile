FROM adoptopenjdk/openjdk11

VOLUME /tmp

ADD target/scala-2.13/BowlingGame.jar app.jar

CMD ["java","-jar","app.jar"]