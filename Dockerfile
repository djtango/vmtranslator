FROM maven:3.6.2-jdk-8

RUN groupadd -g 1000 app && useradd -u 1000 -g app -s /bin/bash --create-home app

ARG USER_HOME_DIR="/home/app"
ENV MAVEN_HOME /usr/share/maven
ENV MAVEN_CONFIG "$USER_HOME_DIR/.m2"

RUN mkdir /home/app/.m2
RUN chown -R app:app /home/app/.m2

USER app
WORKDIR /var/app
