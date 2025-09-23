# --- STAGE 1 : build Maven ---
FROM maven:3.9.11-eclipse-temurin-24 AS build
WORKDIR /app

# Copier uniquement les fichiers nécessaires pour installer les dépendances Maven
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copier le code source
COPY src ./src

# Build le projet (skip tests pour prod)
RUN mvn clean package -DskipTests

# --- STAGE 2 : exécution ---
FROM eclipse-temurin:24-jdk
WORKDIR /app

# Copier le jar buildé depuis le stage 1
COPY --from=build /app/target/BotDiscordCalendar-1.0-SNAPSHOT-jar-with-dependencies.jar ./BotDiscordCalendar.jar

# Exposer le port si nécessaire (Discord n’en utilise pas mais utile si tu ajoutes HTTP server)
# EXPOSE 8080

# Variables d'environnement (sera rempli via Docker Compose)
ENV DISCORD_TOKEN=""
ENV URL_DB=""
ENV USER_DB=""
ENV PASSWORD_DB=""

# Lancer le bot
CMD ["java", "-jar", "BotDiscordCalendar.jar"]