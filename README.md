# Neural Code Insight Engine

A reactive Spring Boot application that analyzes Java code repositories with intelligent storage and processing capabilities.

## 🚀 Features

- **Reactive Architecture**: Built with Spring WebFlux for non-blocking, high-performance operations
- **GitHub Integration**: Automated repository cloning using JGit with support for different branches
- **AWS S3 Storage**: Secure cloud storage with proper region handling and multipart uploads
- **Robust Error Handling**: Comprehensive exception management with meaningful error responses
- **Input Validation**: Strong request validation with custom exception handling
- **Database Integration**: R2DBC with PostgreSQL for reactive database operations

## 🛠️ Tech Stack

- **Backend**: Java 17, Spring Boot 3.x, Spring WebFlux
- **Database**: R2DBC with PostgreSQL
- **Cloud**: AWS S3 SDK with CRT support
- **Version Control**: JGit for Git operations
- **Build Tool**: Maven
- **Architecture**: Reactive Programming with Project Reactor

## ⚙️ Prerequisites

- Java 17 or higher
- AWS Account with S3 access
- Git
