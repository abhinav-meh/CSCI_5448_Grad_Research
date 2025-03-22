# CSCI_5448_Grad_Research

This repository contains both Java and p5.js code examples. Below are instructions on how to set up and run both types of code.

## Team Members
- Abhinav Mehrotra
- Vishnu Vardhan Mahajan

## Table of Contents
- [Prerequisites](#prerequisites)
- [Java Code Setup](#java-code-setup)
- [p5.js Code Setup](#p5js-code-setup)
- [Troubleshooting](#troubleshooting)

## Prerequisites

Before getting started, ensure you have the following installed:

- Java Development Kit (JDK) 8 or higher
- Node.js and npm (Node Package Manager)
- http-server (can be installed via npm)

## Java Code Setup

To run the Java code examples:

1. Navigate to the Java directory:
   ```bash
   cd Java
   ```

2. Compile the Java file:
   ```bash
   javac FileName.java
   ```

3. Run the compiled Java program:
   ```bash
   java FileName
   ```

Note: Replace `FileName` with the actual name of the Java file you want to run (without the .java extension when running).

## p5.js Code Setup

To run the p5.js code examples:

1. Navigate to the p5.js directory:
   ```bash
   cd p5js
   ```

2. Initialize the Node.js project (if not already done):
   ```bash
   npm init -y
   ```

3. Start a local server:
   ```bash
   http-server
   ```

4. Open your browser and go to:
   ```
   http://localhost:8080
   ```

If you don't have http-server installed, you can install it globally using:
```bash
npm install -g http-server
```

## Troubleshooting

### Java Issues
- Make sure JDK is properly installed and added to your PATH
- Verify that you're using the correct file name when compiling and running
- Check for any compilation errors in your console output

### p5.js Issues
- Ensure Node.js and npm are properly installed
- Verify that http-server is running without errors
- Check your browser console for any JavaScript errors
