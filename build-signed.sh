#!/bin/sh
set -e

printf "Enter keystore password: "
stty -echo
read KEYSTORE_PASSWORD
stty echo
echo

printf "Enter key password: "
stty -echo
read KEY_PASSWORD
stty echo
echo

/data/data/com.termux/files/home/gradle-8.6/bin/gradle assembleRelease \
  -PKEYSTORE_PASSWORD="$KEYSTORE_PASSWORD" \
  -PKEY_PASSWORD="$KEY_PASSWORD"

echo
echo "✓ Build complete. APK: build/outputs/apk/release/app-release.apk"
