# How to Build and Run VersionVault

## Prerequisites Check

First, make sure you have the required tools installed:

```bash
java -version
g++ --version
cmake --version
```

If any are missing, install them:
```bash
sudo apt update
sudo apt install openjdk-11-jdk g++ cmake libssl-dev
```

## Step 1: Build the Project

Navigate to the project directory and run the build script:

```bash
cd "/home/deep/Desktop/Code/Projects/Version Control"

chmod +x build.sh create_executable.sh

./build.sh
```

This will:
- Compile the C++ core library
- Compile all Java source files
- Create the `bin` directory with compiled classes

## Step 2: Create the Executable

```bash
./create_executable.sh
```

This creates a `vv` executable in the current directory.

## Step 3: Run VersionVault

### Initialize a Repository

```bash
mkdir my-test-repo
cd my-test-repo

../vv init
```

When prompted:
- Enter your name: `Your Name`
- Enter your email: `your.email@example.com`

### Basic Usage Commands

```bash
echo "Hello World" > hello.txt
../vv add hello.txt
../vv commit "Initial commit"
../vv status
../vv log
```

### Create and Switch Branches

```bash
../vv branch feature-test
../vv branch
../vv checkout feature-test
echo "More content" >> hello.txt
../vv add hello.txt
../vv commit "Added more content"
../vv log
```

### Merge Branches

```bash
../vv checkout main
../vv merge feature-test
```

### File Locking

```bash
echo "binary data" > image.png
../vv lock image.png
../vv unlock image.png
```

## Complete Demo Script

Here's a complete demonstration you can run:

```bash
cd "/home/deep/Desktop/Code/Projects/Version Control"

mkdir demo-repo
cd demo-repo

../vv init
# Enter your details when prompted

echo "# My Project" > README.md
../vv add README.md
../vv commit "Initial commit with README"

../vv branch develop
../vv checkout develop

echo "- Feature 1" >> README.md
../vv add README.md
../vv commit "Added feature 1"

echo "- Feature 2" >> README.md
../vv add README.md
../vv commit "Added feature 2"

../vv log

../vv checkout main
../vv merge develop

echo "Build completed!" > build.log
../vv add build.log
../vv commit "Added build log"

../vv status
../vv log

cd ..
```

## Troubleshooting

### If build fails with Java errors:

Make sure you're using Java 11 or higher:
```bash
java -version
```

If you have multiple Java versions:
```bash
sudo update-alternatives --config java
```

### If C++ compilation fails:

Install OpenSSL development libraries:
```bash
sudo apt install libssl-dev
```

### If "vv: command not found":

Run with `./vv` (prefix with current directory):
```bash
./vv init
```

Or add to PATH temporarily:
```bash
export PATH=$PATH:"/home/deep/Desktop/Code/Projects/Version Control"
vv init
```

## Quick Test

To verify everything works:

```bash
cd "/home/deep/Desktop/Code/Projects/Version Control"

./build.sh && ./create_executable.sh

mkdir quicktest
cd quicktest
../vv init <<EOF
Test User
test@example.com
EOF

echo "test" > test.txt
../vv add test.txt
../vv commit "test commit"
../vv log

cd ..
rm -rf quicktest

echo "✓ VersionVault is working correctly!"
```

## For Your Exam Demo

1. **Build beforehand** - Don't build during the exam
2. **Test the demo** - Run through it at least once
3. **Have a backup** - Keep compiled binaries ready
4. **Know the commands** - Memorize basic usage

## All Available Commands

```
vv init              - Initialize a new repository
vv add <file>        - Add file to staging area  
vv commit <message>  - Commit staged changes
vv branch [name]     - List branches or create new one
vv checkout <branch> - Switch to a branch
vv merge <branch>    - Merge a branch into current
vv log               - Show commit history
vv status            - Show working tree status
vv lock <file>       - Lock a file (for binary files)
vv unlock <file>     - Unlock a file
```

## Success!

If you see no errors and can run the demo commands, you're ready for your exam! 🎉
