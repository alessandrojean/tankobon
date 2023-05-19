FROM ubuntu:kinetic AS tankobon-base
RUN apt update
RUN apt install git gnupg ca-certificates curl -y
RUN curl -s https://repos.azul.com/azul-repo.key | gpg --dearmor -o /usr/share/keyrings/azul.gpg
RUN echo "deb [signed-by=/usr/share/keyrings/azul.gpg] https://repos.azul.com/zulu/deb stable main" | tee /etc/apt/sources.list.d/zulu.list
RUN apt update
RUN apt install zulu17-jdk -y
RUN curl -sL https://deb.nodesource.com/setup_19.x -o /tmp/nodesource_setup.sh
RUN bash /tmp/nodesource_setup.sh
RUN apt install nodejs -y
ENV PNPM_HOME="/root/.local/share/pnpm"
ENV PATH="${PATH}:${PNPM_HOME}"
RUN npm install --global pnpm

FROM tankobon-base AS tankobon-build
ENV DOCKER_PIPELINE=true
WORKDIR /build
COPY . ./
RUN ./gradlew copyWebDist bootJar

FROM azul/zulu-openjdk-alpine:17-jre-latest AS tankobon-deploy
WORKDIR /deploy
COPY --from=tankobon-build /build/server/build/libs/tankobon.jar .
CMD ["java", "-jar", "tankobon.jar"]