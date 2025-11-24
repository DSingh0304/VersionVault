#!/bin/bash

echo "Creating VersionVault executable..."

cat > vv << 'EOF'
#!/bin/bash
java -cp bin com.versionvault.cli.VersionVaultCLI "$@"
EOF

chmod +x vv

echo "Executable created: ./vv"
echo ""
echo "Usage: ./vv <command> [options]"
echo "Try: ./vv init"
