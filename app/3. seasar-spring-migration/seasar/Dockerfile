FROM ubuntu AS build
RUN apt update && apt install -y maven
WORKDIR /app
COPY . .
RUN mvn clean package

FROM tomcat:8.5
COPY --from=build /app/target/*.war /usr/local/tomcat/webapps/
CMD ["catalina.sh", "run"]