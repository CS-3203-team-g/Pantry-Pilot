# Stage 1: Build
FROM maven:3.8.6-openjdk-11 AS build
WORKDIR /app
# ...existing code: copy entire project...
COPY . /app
RUN mvn clean package -DskipTests

# Stage 2: Run
FROM openjdk:8-jre-slim
WORKDIR /app

# ---> Add these lines to install Python and dependencies <---
# Update package lists and install python3 and pip
# Add curl and gnupg needed for apt-key if using older debian base for openjdk
# Add build-essential if any python package requires compilation (less likely here)
# Switch to root to install packages
USER root
# Combine the RUN command for clarity
RUN apt-get update && \
    apt-get install -y --no-install-recommends python3 python3-pip && \
    rm -rf /var/lib/apt/lists/*

# Copy your Python script into the image
# Make sure ai_recipe_generator.py is in the root of your project context
COPY ai_recipe_generator.py /app/ai_recipe_generator.py

# Install Python dependencies
RUN pip3 install --no-cache-dir google-generativeai python-dotenv

# Switch back to a non-root user if your base image defines one (optional but good practice)
# USER nonrootuser

# ---> End of added lines <---

# Copy the application JAR from the build stage
COPY --from=build /app/target/PantryPilot.jar /app/PantryPilot.jar
EXPOSE 80
CMD ["java", "-jar", "PantryPilot.jar"]
