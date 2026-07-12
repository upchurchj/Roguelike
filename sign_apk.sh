#!/data/data/com.termux/files/usr/bin/bash
# sign_apk.sh - Build and sign APK with jarsigner

set -e  # Exit on any error

# Configuration
GRADLE_PATH="/data/data/com.termux/files/home/gradle-8.6/bin/gradle"
PROJECT_DIR="/storage/3531-6539/Android/data/com.termux/files/roguelike-game"
UNSIGNED_APK="$PROJECT_DIR/build/outputs/apk/release/roguelike-game-release-unsigned.apk"
KEYSTORE="$PROJECT_DIR/knightlight.jks"
KEYALIAS="knightlight"
SIGNED_UNALIGNED="${UNSIGNED_APK%.apk}-signed-unaligned.apk"
SIGNED_APK="$(dirname "$UNSIGNED_APK")/roguelike-game-release-signed.apk"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'  # No Color

echo -e "${YELLOW}[1/4] Building unsigned APK...${NC}"
$GRADLE_PATH -p "$PROJECT_DIR" assembleRelease --no-daemon

if [ ! -f "$UNSIGNED_APK" ]; then
    echo -e "${RED}❌ Error: Unsigned APK not found at $UNSIGNED_APK${NC}"
    exit 1
fi
echo -e "${GREEN}✅ Unsigned APK built successfully${NC}"

echo -e "${YELLOW}[2/4] Signing APK (you will be prompted for passwords)...${NC}"
jarsigner -verbose -sigalg SHA256withRSA -digestalg SHA-256 \
  -keystore "$KEYSTORE" \
  -signedjar "$SIGNED_UNALIGNED" \
  "$UNSIGNED_APK" \
  "$KEYALIAS"

if [ $? -ne 0 ]; then
    echo -e "${RED}❌ Signing failed${NC}"
    exit 1
fi
echo -e "${GREEN}✅ APK signed successfully${NC}"

echo -e "${YELLOW}[3/4] Aligning APK...${NC}"
rm -f "$SIGNED_APK"
zipalign -v 4 "$SIGNED_UNALIGNED" "$SIGNED_APK"

if [ ! -f "$SIGNED_APK" ]; then
    echo -e "${RED}❌ Error: Signed APK not found at $SIGNED_APK${NC}"
    exit 1
fi
echo -e "${GREEN}✅ APK aligned successfully${NC}"

echo ""
echo "[4/4] Verifying APK alignment and signature..."

if zipalign -c -v 4 "$SIGNED_APK" > /dev/null 2>&1; then
    echo "✅ APK alignment verified"
else
    echo "❌ Alignment verification failed!"
    exit 1
fi

jarsigner -verify -verbose -certs "$SIGNED_APK"

if [ $? -eq 0 ]; then
    echo ""
    echo "✅ APK signing and verification complete!"
    echo "Output: $SIGNED_APK"
else
    echo "❌ Signature verification failed!"
    exit 1
fi

# Final summary
echo ""
echo "════════════════════════════════════════════"
echo "✓ APK signed successfully"
echo "✓ APK aligned successfully"
echo "✓ APK verified successfully"
echo "✓ Ready for deployment"
echo "════════════════════════════════════════════"
echo "Output: $SIGNED_APK"

# Clean up intermediate file
rm -f "$SIGNED_UNALIGNED"
