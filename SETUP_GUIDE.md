# 📘 MITM Attack Demo - Setup Guide

Complete installation and configuration guide for the MITM Attack Demonstration System.

## 📋 Table of Contents

- [System Requirements](#system-requirements)
- [Installation Steps](#installation-steps)
- [Configuration](#configuration)
- [Running the System](#running-the-system)


## 💻 System Requirements

### Minimum Requirements
- **Operating System:** Windows 10/11, macOS 10.14+, or Linux (Ubuntu 18.04+)
- **Java:** JDK 11 or higher
- **Maven:** 3.6 or higher
- **RAM:** 2 GB minimum
- **Disk Space:** 100 MB


### Recommended Requirements
- **Java:** JDK 17 or higher
- **RAM:** 4 GB or more


### Verify Prerequisites

**Check Java Version:**
```bash
java -version
```
Expected output: `java version "11.0.x"` or higher

**Check Maven Version:**
```bash
mvn -version
```
Expected output: `Apache Maven 3.6.x` or higher

## 🔧 Installation Steps

### Step 1: Extract/Clone the Project

If you received a ZIP file:
```bash
# Extract the archive
unzip mitm-attack-demo.zip
cd mitm-attack-demo
```

If using Git:
```bash
git clone https://github.com/g25ait2020/mitm-attack-demo
cd mitm-attack-demo
```

### Step 2: Verify Project Structure

Ensure the following structure exists:
```
mitm-attack-demo/
├── src/
├── pom.xml
├── README.md
└── SETUP_GUIDE.md
└── HOSPITAL_DEMO_GUIDE.md
└── run_hospital_demo.bat
```

### Step 3: Build the Project

```bash
# Clean and build
mvn clean package

# This will:
# 1. Download dependencies
# 2. Compile Java source files
# 3. Run tests (if any)
# 4. Create executable JAR file
```

**Expected Output:**
```
[INFO] BUILD SUCCESS
[INFO] Total time: XX.XXX s
```

**Generated Files:**
- `target/mitm-attack-demo-1.0.0.jar` - Executable JAR with dependencies

### Step 4: Verify Build

```bash
# Check if JAR was created
ls -l target/mitm-attack-demo-1.0.0.jar
```

## ⚙️ Configuration

### Default Ports

The system uses the following ports:

| Component | Port | Purpose |
|-----------|------|---------|
| Legitimate Server | 8080 | Real server |
| MITM Attacker Proxy | 8081 | Attacker's proxy |

### Changing Ports (Optional)

If ports are already in use, you can modify them:

**1. Edit Server Port:**
```java
// In src/main/java/com/mitm/server/Server.java
private static final int PORT = 8080; // Change this
```

**2. Edit Proxy Port:**
```java
// In src/main/java/com/mitm/attacker/MITMProxy.java
private static final int PROXY_PORT = 8081; // Change this
```


**4. Rebuild:**
```bash
mvn clean package
```

### Firewall Configuration

**Windows:**
```powershell
# Allow Java through firewall (run as Administrator)
netsh advfirewall firewall add rule name="MITM Demo" dir=in action=allow program="C:\Program Files\Java\jdk-11\bin\java.exe" enable=yes
```


## Quick Start

### One-Click Demo 

```bash
run_hospital_demo.bat
```

This automatically starts:
1. Hospital Server (port 8080)
2. MITM Attacker Proxy (port 8081)
3. Hospital Client GUI

