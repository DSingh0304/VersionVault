#!/bin/bash

echo "Building VersionVault..."

echo "Step 1: Building C++ core library..."
mkdir -p build
cd build
cmake ..
make
cd ..

echo ""
echo "Step 2: Compiling Java sources..."
mkdir -p bin

find src/java -name "*.java" > sources.txt
javac -d bin @sources.txt

if [ $? -eq 0 ]; then
    rm sources.txt
    echo "Build successful!"
    echo ""
    echo "To create an executable, run:"
    echo "  ./create_executable.sh"
else
    rm sources.txt
    echo "Build failed!"
    exit 1
fi
