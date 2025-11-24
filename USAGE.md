# VersionVault - Usage Guide

## Quick Start

### 1. Build the Project (First Time Only)

```bash
cd "/home/deep/Desktop/Code/Projects/Version Control"
./build.sh
```

## Basic Commands

### Initialize a Repository

```bash
./vv init
```

This will prompt you for:
- Your name
- Your email

Example:
```bash
./vv init
# Enter: John Doe
# Enter: john@example.com
```

### Add Files to Staging Area

```bash
./vv add <filename>
```

Examples:
```bash
./vv add README.md
./vv add src/main.java
./vv add *.txt
```

### Commit Changes

```bash
./vv commit "Your commit message"
```

Examples:
```bash
./vv commit "Initial commit"
./vv commit "Added new feature"
./vv commit "Fixed bug in login"
```

### View Repository Status

```bash
./vv status
```

This shows:
- Current branch
- Files staged for commit
- Working tree status

### View Commit History

```bash
./vv log
```

Shows all commits with:
- Commit hash
- Author
- Date
- Commit message

## Branch Management

### List All Branches

```bash
./vv branch
```

The current branch is marked with `*`

### Create a New Branch

```bash
./vv branch <branch-name>
```

Examples:
```bash
./vv branch feature-login
./vv branch bugfix-ui
./vv branch develop
```

### Switch to a Branch

```bash
./vv checkout <branch-name>
```

Examples:
```bash
./vv checkout main
./vv checkout feature-login
./vv checkout develop
```

### Merge a Branch

```bash
./vv merge <branch-name>
```

Example:
```bash
# Switch to main branch first
./vv checkout main

# Then merge feature branch
./vv merge feature-login
```

## File Locking (For Binary Files)

### Lock a File

```bash
./vv lock <filename>
```

Examples:
```bash
./vv lock image.png
./vv lock database.db
./vv lock video.mp4
```

### Unlock a File

```bash
./vv unlock <filename>
```

Examples:
```bash
./vv unlock image.png
./vv unlock database.db
```

## Complete Workflow Example

### Starting a New Project

```bash
# Navigate to your project directory
cd "/home/deep/Desktop/Code/Projects/Version Control"

# Create a test project
mkdir my-project
cd my-project

# Initialize repository
../vv init
# Enter: Your Name
# Enter: your@email.com

# Create some files
echo "# My Project" > README.md
echo "print('Hello World')" > main.py

# Add files to staging
../vv add README.md
../vv add main.py

# Check status
../vv status

# Commit changes
../vv commit "Initial project setup"

# View history
../vv log
```

### Working with Branches

```bash
# Create a new feature branch
../vv branch feature-authentication

# Switch to the feature branch
../vv checkout feature-authentication

# Make changes
echo "def login(): pass" >> main.py

# Add and commit
../vv add main.py
../vv commit "Added login function"

# Switch back to main
../vv checkout main

# Merge the feature
../vv merge feature-authentication

# Check status
../vv status

# View updated history
../vv log
```

### Working with Binary Files

```bash
# Lock a binary file before editing
../vv lock photo.jpg

# Make your changes to the file
# (edit photo.jpg in your image editor)

# Add and commit
../vv add photo.jpg
../vv commit "Updated profile photo"

# Unlock when done
../vv unlock photo.jpg
```

## All Commands Summary

| Command | Description | Example |
|---------|-------------|---------|
| `./vv init` | Initialize new repository | `./vv init` |
| `./vv add <file>` | Add file to staging | `./vv add index.html` |
| `./vv commit <msg>` | Commit staged changes | `./vv commit "Update"` |
| `./vv status` | Show repository status | `./vv status` |
| `./vv log` | Show commit history | `./vv log` |
| `./vv branch` | List branches | `./vv branch` |
| `./vv branch <name>` | Create new branch | `./vv branch dev` |
| `./vv checkout <branch>` | Switch branch | `./vv checkout main` |
| `./vv merge <branch>` | Merge branch | `./vv merge feature` |
| `./vv lock <file>` | Lock file | `./vv lock image.png` |
| `./vv unlock <file>` | Unlock file | `./vv unlock image.png` |

## Tips

1. **Always commit regularly** - Don't wait too long between commits
2. **Use descriptive commit messages** - Future you will thank you
3. **Create branches for features** - Keep main branch stable
4. **Lock binary files** - Prevents conflicts with images, videos, etc.
5. **Check status often** - Know what's staged and what's not

## Troubleshooting

### "Not a VersionVault repository"
**Solution**: Run `./vv init` first

### "Nothing to commit"
**Solution**: Use `./vv add <file>` to stage files first

### "User not configured"
**Solution**: Initialize repository with `./vv init` and enter user details

### "Branch does not exist"
**Solution**: Create the branch first with `./vv branch <name>`

## For Your Exam

Quick demo script:
```bash
cd "/home/deep/Desktop/Code/Projects/Version Control"
mkdir exam-demo
cd exam-demo

../vv init
echo "# Demo" > README.md
../vv add README.md
../vv commit "Initial commit"
../vv branch feature
../vv checkout feature
echo "New content" >> README.md
../vv add README.md
../vv commit "Added feature"
../vv checkout main
../vv merge feature
../vv log
```

## Project Location

```
/home/deep/Desktop/Code/Projects/Version Control/
```

To run from anywhere, use the full path:
```bash
"/home/deep/Desktop/Code/Projects/Version Control/vv" init
```

Or add to PATH (optional):
```bash
export PATH="$PATH:/home/deep/Desktop/Code/Projects/Version Control"
# Then you can just use: vv init
```

---

**That's it! You're ready to use VersionVault! 🚀**
