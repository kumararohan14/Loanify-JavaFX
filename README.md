<<<<<<< HEAD
# Loanify-JavaFX 💰

**Loanify-JavaFX** is a desktop-based Loan Management System built using **Java** and **JavaFX**.  
The application helps manage customers, loans, EMI schedules, and repayments through a clean and user-friendly interface.

---

## ✨ Features

- 👤 **Customer Management**
  - Add, view, update customer details
  - Maintain customer loan history

- 💳 **Loan Management**
  - Create and manage loans
  - Support for different loan amounts, interest rates, and durations

- 📊 **EMI Calculation**
  - Automatic EMI calculation
  - Interest-based repayment logic

- 📅 **Repayment Tracking**
  - Track paid and pending installments
  - View repayment status clearly

- 🖥️ **Desktop UI**
  - Clean and simple JavaFX interface
  - Easy navigation between screens

- 🧱 **Structured Codebase**
  - Follows OOP principles
  - Easy to extend and maintain

---

## 🛠️ Tech Stack

| Technology | Description |
|-----------|------------|
| **Java 17 (LTS)** | Core programming language |
| **JavaFX 17** | UI framework for desktop application |
| **FXML** | UI layout definition |
| **CSS** | UI styling |
| **Maven** | Dependency management (if applicable) |
| **IntelliJ IDEA** | Recommended IDEs |

---

## 📂 Project Structure (Example)
Loanify-JavaFX
├── src
│ ├── main
│ │ ├── java
│ │ │ └── com.loanify.app
│ │ │ ├── controller
│ │ │ ├── model
│ │ │ ├── service
│ │ │ └── MainApp.java
│ │ └── resources
│ │ ├── view
│ │ ├── css
│ │ └── images
├── pom.xml
└── README.md
=======
# Loan Management System

A simple JavaFX Dashboard application for managing loans.

## Prerequisites

- **Java Development Kit (JDK) 17** or later.

## Quick Start (Included Maven)

I have downloaded Maven for you in the `tools` folder so you don't need to install it globally.

1.  Open this folder in your terminal.
2.  Run the application using the included Maven:

    ```powershell
    .\tools\apache-maven-3.9.6\bin\mvn.cmd clean javafx:run
    ```

## Project Structure

- `src/main/java`: Java source files.
- `src/main/resources`: FXML and other resources.
- `pom.xml`: Maven configuration file.
- `tools/`: Contains the Apache Maven distribution.

## Troubleshooting

- **Java Version Errors**: Ensure `java -version` shows version 17 or higher.
- **Port invalid**: If you see errors about ports, ensure no other application is blocking it.
>>>>>>> 3b19968 (Initial project structure)
