#!/bin/bash
# Script to package Docker images for production deployment

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
OUTPUT_DIR="${SCRIPT_DIR}/production-images"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)

echo "📦 Packaging Docker images for production deployment..."
echo ""

# Create output directory
mkdir -p "$OUTPUT_DIR"

# Images to package
IMAGES=(
    "deployment-backend:latest"
    "deployment-frontend:latest"
)

# Base images (these can usually be pulled from registry on target, but included for offline deployment)
BASE_IMAGES=(
    "postgres:16-alpine"
    "nginx:alpine"
)

echo "📋 Images to package:"
printf '%s\n' "${IMAGES[@]}"
printf '%s\n' "${BASE_IMAGES[@]}"
echo ""

# Function to save image
save_image() {
    local image=$1
    local filename=$(echo "$image" | sed 's/[^a-zA-Z0-9._-]/_/g')
    local output_file="${OUTPUT_DIR}/${filename}_${TIMESTAMP}.tar"
    
    echo "💾 Saving ${image} to ${output_file}..."
    docker save "$image" -o "$output_file"
    echo "   ✅ Saved: $(du -h "$output_file" | cut -f1)"
    echo ""
}

# Save application images
echo "🔨 Packaging application images..."
for image in "${IMAGES[@]}"; do
    save_image "$image"
done

# Save base images (optional - comment out if you want to pull these from registry)
echo "🔧 Packaging base images..."
for image in "${BASE_IMAGES[@]}"; do
    save_image "$image"
done

# Create a manifest file
MANIFEST_FILE="${OUTPUT_DIR}/manifest_${TIMESTAMP}.txt"
echo "📝 Creating manifest file..."
cat > "$MANIFEST_FILE" << EOF
Docker Images Package Manifest
Generated: $(date)
Project: TestCraft Dashboard

Application Images:
$(for img in "${IMAGES[@]}"; do echo "  - $img"; done)

Base Images:
$(for img in "${BASE_IMAGES[@]}"; do echo "  - $img"; done)

Load Instructions:
1. Transfer all .tar files to the production server
2. Load images: docker load -i <image_file>.tar
3. Verify: docker images
4. Deploy using docker-compose up -d

Files in package:
$(ls -lh "$OUTPUT_DIR"/*.tar 2>/dev/null | awk '{print "  " $9 " (" $5 ")"}' || echo "  (none)")

Total size: $(du -sh "$OUTPUT_DIR" | cut -f1)
EOF

echo "✅ Manifest saved: $MANIFEST_FILE"
echo ""

# Create archive (optional)
ARCHIVE_NAME="testcraft-images-${TIMESTAMP}.tar.gz"
echo "🗜️  Creating compressed archive..."
cd "$OUTPUT_DIR"
tar -czf "${ARCHIVE_NAME}" *.tar manifest_*.txt 2>/dev/null || true
mv "${ARCHIVE_NAME}" "${SCRIPT_DIR}/"
echo "✅ Archive created: ${SCRIPT_DIR}/${ARCHIVE_NAME}"
echo ""

echo "🎉 Image packaging complete!"
echo ""
echo "📦 Files location: $OUTPUT_DIR"
echo "📋 Manifest: $MANIFEST_FILE"
echo "🗜️  Archive: ${SCRIPT_DIR}/${ARCHIVE_NAME}"
echo ""
echo "📤 To transfer to production server:"
echo "   scp ${SCRIPT_DIR}/${ARCHIVE_NAME} user@production:/path/to/destination/"
echo ""
echo "📥 On production server, extract and load:"
echo "   tar -xzf ${ARCHIVE_NAME}"
echo "   docker load -i <image_file>.tar"
echo ""
