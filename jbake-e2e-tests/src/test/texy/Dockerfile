FROM php:8.2-cli

# Install Composer
COPY --from=composer:latest /usr/bin/composer /usr/bin/composer

# Set working directory
WORKDIR /app

# Copy service file
COPY texy-service.php /app/

# Install Texy library
RUN apt-get update && apt-get install -y unzip git
RUN composer require texy/texy

# Expose port
EXPOSE 8080

# Run service
CMD ["php", "texy-service.php", "8080"]

