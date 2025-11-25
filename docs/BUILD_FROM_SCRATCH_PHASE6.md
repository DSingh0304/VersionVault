# PHASE 6: Build Configuration & Testing

Now we create the build scripts to compile and run our project.

---

## Step 28: CMakeLists.txt - C++ Build Configuration

**Location**: `CMakeLists.txt` (root directory)

**Why This?**
- CMake is the standard build system for C++ projects
- Handles compilation, linking, and dependencies

**Code**:

```cmake
cmake_minimum_required(VERSION 3.15)
project(VersionVault VERSION 1.0.0 LANGUAGES CXX)

# Set C++17 standard
set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# Find OpenSSL for hashing
find_package(OpenSSL REQUIRED)

# Include directories
include_directories(${CMAKE_SOURCE_DIR}/src/core)

# Source files
set(CORE_SOURCES
    src/core/FileObject.cpp
    src/core/ObjectStore.cpp
    src/core/DiffEngine.cpp
)

# Build shared library
add_library(vv_core SHARED ${CORE_SOURCES})

# Link OpenSSL
target_link_libraries(vv_core OpenSSL::SSL OpenSSL::Crypto)

# Public include directories
target_include_directories(vv_core PUBLIC ${CMAKE_SOURCE_DIR}/src/core)

# Installation rules
install(TARGETS vv_core
    LIBRARY DESTINATION lib
    ARCHIVE DESTINATION lib
)

install(DIRECTORY src/core/
    DESTINATION include/versionvault
    FILES_MATCHING PATTERN "*.h"
)
```

**What This Does**:
1. **Requires C++17**: Modern C++ features
2. **Finds OpenSSL**: For SHA-256 hashing
3. **Builds Shared Library**: Creates `libvv_core.so`
4. **Links Libraries**: Connects OpenSSL to our code
5. **Installation**: Defines where files go when installed

---

## Step 29: build.sh - Build Script

**Location**: `build.sh` (root directory)

**Why This?**
- Automates the entire build process
- Builds both C++ and Java in order

**Code**:

```bash
#!/bin/bash

echo "Building VersionVault..."

# Build C++ core library
echo "Step 1: Building C++ core library..."
mkdir -p build
cd build
cmake ..
make
cd ..

echo ""
echo "Step 2: Compiling Java sources..."
mkdir -p bin

# Find all Java files and compile them
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
```

**What This Does**:
1. **Creates build directory**: For C++ build files
2. **Runs CMake**: Generates build system
3. **Runs Make**: Compiles C++ code
4. **Finds Java files**: Recursively finds all `.java` files
5. **Compiles Java**: Builds all Java classes to `bin/`
6. **Error Handling**: Checks if compilation succeeded

**How to Use**:
```bash
chmod +x build.sh
./build.sh
```

---

## Step 30: create_executable.sh - Executable Creator

**Location**: `create_executable.sh` (root directory)

**Why This?**
- Creates convenient `vv` command
- Wraps Java execution in a shell script

**Code**:

```bash
#!/bin/bash

echo "Creating VersionVault executable..."

# Create CLI executable
cat > vv << 'EOF'
#!/bin/bash
java -cp bin com.versionvault.cli.VersionVaultCLI "$@"
EOF

chmod +x vv

echo "Executable created: ./vv"
echo ""
echo "Usage: ./vv <command> [options]"
echo "Try: ./vv init"
```

**What This Does**:
1. **Creates `vv` script**: Shell script that runs Java
2. **Passes arguments**: `"$@"` forwards all arguments
3. **Makes executable**: `chmod +x` makes it runnable

**The Created `vv` File**:
```bash
#!/bin/bash
java -cp bin com.versionvault.cli.VersionVaultCLI "$@"
```

**How to Use**:
```bash
chmod +x create_executable.sh
./create_executable.sh
./vv init
```

---

## Step 31: .gitignore - Ignore Build Artifacts

**Location**: `.gitignore` (root directory)

**Why This?**
- Prevents committing generated files
- Keeps repository clean

**Code**:

```
# Build artifacts
build/
bin/
target/

# IDE files
.vscode/
.idea/
*.iml

# Compiled files
*.class
*.o
*.so
*.dylib
*.dll

# VersionVault metadata
.vv/

# Executables
vv
vv-gui

# Editor temp files
*.swp
*.swo
*~
.DS_Store
```

---

## Step 32: USAGE.md - User Documentation

**Location**: `USAGE.md` (root directory)

**Code**:

```markdown
# VersionVault Usage Guide

## Installation

1. **Build the project**:
   ```bash
   chmod +x build.sh create_executable.sh
   ./build.sh
   ./create_executable.sh
   ```

2. **Verify installation**:
   ```bash
   ./vv --help
   ```

## Quick Start

### Initialize a Repository

```bash
cd my-project
./vv init
```

This creates a `.vv/` directory to track your files.

### Add Files

```bash
./vv add file1.txt
./vv add file2.txt
```

Or add all files:
```bash
./vv add .
```

### Commit Changes

```bash
./vv commit -m "Initial commit"
```

### Create a Branch

```bash
./vv branch feature-login
```

### Switch Branches

```bash
./vv checkout feature-login
```

### Merge Branches

```bash
./vv checkout main
./vv merge feature-login
```

### View History

```bash
./vv log
```

View limited history:
```bash
./vv log -n 5
```

### Check Status

```bash
./vv status
```

### File Locking

Lock a file (prevents others from editing):
```bash
./vv lock database.db
```

Unlock a file:
```bash
./vv unlock database.db
```

## Commands Reference

| Command | Description | Example |
|---------|-------------|---------|
| `init` | Initialize repository | `vv init` |
| `add <file>` | Stage file | `vv add README.md` |
| `commit -m <msg>` | Create commit | `vv commit -m "Fix bug"` |
| `branch <name>` | Create branch | `vv branch dev` |
| `branch` | List branches | `vv branch` |
| `checkout <branch>` | Switch branch | `vv checkout dev` |
| `merge <branch>` | Merge branch | `vv merge dev` |
| `log` | View history | `vv log` |
| `log -n <num>` | View N commits | `vv log -n 10` |
| `status` | Check status | `vv status` |
| `lock <file>` | Lock file | `vv lock data.bin` |
| `unlock <file>` | Unlock file | `vv unlock data.bin` |

## Workflow Example

```bash
# Start a new project
mkdir my-app
cd my-app
vv init

# Create some files
echo "# My App" > README.md
echo "console.log('Hello');" > app.js

# Track and commit
vv add README.md app.js
vv commit -m "Initial commit"

# Create a feature branch
vv branch feature-auth
vv checkout feature-auth

# Make changes
echo "// Auth logic" >> app.js
vv add app.js
vv commit -m "Add authentication"

# Merge back to main
vv checkout main
vv merge feature-auth

# View history
vv log
```

## Advanced Features

### File Locking for Binary Files

VersionVault automatically detects binary files and encourages locking:

```bash
vv lock images/logo.png
# Edit the file
vv commit -m "Update logo"
vv unlock images/logo.png
```

### Merge Strategies

VersionVault uses a three-way merge algorithm by default, which:
- Compares base commit (common ancestor)
- Your changes
- Their changes
- Automatically merges non-conflicting changes

### User Roles

Set your role in `.vv/config`:
- **ADMIN**: Full permissions, can override locks
- **DEVELOPER**: Can read/write/lock files
- **VIEWER**: Read-only access

## Troubleshooting

### "Not a VersionVault repository"
Run `vv init` in your project directory.

### "Staging area is empty"
Add files with `vv add <file>` before committing.

### "Merge conflicts"
Manually resolve conflicts in the files, then commit.

### Lock Issues
Admins can force release locks:
```bash
vv unlock --force <file>
```

## Comparison with Git

| Feature | VersionVault | Git |
|---------|--------------|-----|
| File Locking | ✅ Built-in | ❌ Requires LFS |
| Binary Files | ✅ Optimized | ⚠️ Problematic |
| Merge | Simplified | Complex |
| Learning Curve | Easier | Steeper |
| Speed | Fast core (C++) | Fast |

## Tips

1. **Commit often**: Small, frequent commits are better
2. **Use branches**: Keep main stable, develop in branches
3. **Lock binaries**: Always lock binary files before editing
4. **Write good messages**: Descriptive commit messages help later
5. **Check status**: Run `vv status` before committing

```

---

## Step 33: Demo Script

**Location**: `demo/run_demo.sh`

**Code**:

```bash
#!/bin/bash

echo "=== VersionVault Demo ==="
echo ""

# Clean up any existing demo
rm -rf /tmp/vv-demo
mkdir /tmp/vv-demo
cd /tmp/vv-demo

echo "1. Initialize repository"
../../vv init

echo ""
echo "2. Create some files"
echo "# Test Project" > README.md
echo "Hello, World!" > test.txt

echo ""
echo "3. Add files to staging"
../../vv add README.md test.txt

echo ""
echo "4. Commit"
../../vv commit -m "Initial commit"

echo ""
echo "5. Create a branch"
../../vv branch feature

echo ""
echo "6. Check status"
../../vv status

echo ""
echo "7. View log"
../../vv log

echo ""
echo "=== Demo Complete ==="
```

---

## Step 34: Complete Build & Test Workflow

**Create this script**: `test_build.sh`

```bash
#!/bin/bash

set -e  # Exit on error

echo "=== VersionVault Build & Test ==="
echo ""

echo "Step 1: Clean previous builds"
rm -rf build bin vv vv-gui
echo "✓ Cleaned"

echo ""
echo "Step 2: Build C++ and Java"
./build.sh

echo ""
echo "Step 3: Create executables"
./create_executable.sh

echo ""
echo "Step 4: Run demo"
cd demo
chmod +x run_demo.sh
./run_demo.sh

echo ""
echo "=== All Tests Passed ==="
```

---

## Summary - Complete Build Process

### Full Build Commands:

```bash
# 1. Make scripts executable
chmod +x build.sh create_executable.sh test_build.sh

# 2. Build everything
./build.sh

# 3. Create executable
./create_executable.sh

# 4. Test
./vv --help
./vv init
```

### Project Structure After Build:

```
Version Control/
├── build/              # CMake build files
│   ├── CMakeCache.txt
│   ├── Makefile
│   └── libvv_core.so   # Compiled C++ library
│
├── bin/                # Compiled Java classes
│   └── com/
│       └── versionvault/
│           ├── cli/
│           ├── core/
│           ├── operations/
│           ├── merge/
│           └── lock/
│
├── vv                  # Executable (generated)
└── .vv/                # Repository metadata (after init)
```

---

## Congratulations! 🎉

You've now built a complete version control system from scratch!

### What You've Learned:

#### C++ Concepts:
- ✅ Inheritance & Polymorphism
- ✅ Abstract Classes
- ✅ Operator Overloading
- ✅ Friend Classes
- ✅ Smart Pointers
- ✅ Templates
- ✅ Singleton Pattern
- ✅ Factory Pattern
- ✅ Strategy Pattern
- ✅ Move Semantics
- ✅ Const Correctness
- ✅ Enum Classes
- ✅ Thread Safety (Mutex)

#### Java Concepts:
- ✅ Interfaces
- ✅ Enums
- ✅ Collections (HashMap, ArrayList)
- ✅ Streams API
- ✅ Lambda Expressions
- ✅ Comparable & Cloneable
- ✅ Custom Exceptions
- ✅ Serialization
- ✅ Synchronized Methods
- ✅ Composition vs Aggregation
- ✅ Template Method Pattern
- ✅ Command Pattern
- ✅ Inner Classes

#### System Design:
- ✅ Layered Architecture
- ✅ Separation of Concerns
- ✅ Design Patterns
- ✅ Build Systems (CMake)
- ✅ Shell Scripting

### Next Steps:

1. **Test extensively**: Try all commands
2. **Read the code**: Understand each file
3. **Experiment**: Modify and extend
4. **Present**: Prepare to explain each concept
5. **Practice**: Walk through the build process

### Key Files to Review Before Exam:

1. **FileObject.h/cpp** - Inheritance, Polymorphism
2. **ObjectStore.h/cpp** - Singleton, Thread Safety
3. **Commit.java** - Static members, Cloneable
4. **CommitHistory.java** - Streams, Lambdas
5. **VaultOperation.java** - Template Method
6. **MergeStrategy.java** - Strategy Pattern
7. **VersionVaultCLI.java** - Command & Factory Patterns
8. **LockManager.java** - Synchronized methods

### Project Statistics:

- **Total Files**: 30+
- **Lines of Code**: 3000+
- **OOP Concepts**: 36+
- **Design Patterns**: 6
- **Languages**: 2 (C++, Java)

**You're ready for your exam! Good luck! 🚀**
