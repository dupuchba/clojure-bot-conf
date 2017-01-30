FROM java:8-alpine
MAINTAINER Your Name <you@example.com>

ADD target/uberjar/clojure-bot-conf.jar /clojure-bot-conf/app.jar

EXPOSE 3000

CMD ["java", "-jar", "/clojure-bot-conf/app.jar"]
