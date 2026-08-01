#!/bin/bash
set -euo pipefail
umask 0077

# =====================================================================
#  KnightLight Source-to-HTML Converter v5.0
#  - Java source files (app/src/main/java/com/knightlight/game)
#  - All XML files (manifest, layouts, values, drawables, mipmaps)
#  - ProGuard rules
#  - Sectioned output with navigation
# =====================================================================

JAVA_SRC_DIR="app/src/main/java/com/knightlight/game"
APP_SRC_DIR="app/src/main"
DOCS_DIR="./docs"
LOG_FILE=".conversion-audit.log"

if [[ ! -d "$JAVA_SRC_DIR" ]]; then
    echo "[ERROR] Java source directory not found: $JAVA_SRC_DIR"
    exit 1
fi

if [[ ! -d "$APP_SRC_DIR" ]]; then
    echo "[ERROR] App source directory not found: $APP_SRC_DIR"
    exit 1
fi

mkdir -p "$DOCS_DIR"
chmod 755 "$DOCS_DIR" 2>/dev/null || true

generate_combined_page() {
    local output="${DOCS_DIR}/ALL_SOURCE.html"
    local total_java=0
    local total_xml=0
    local total_lines=0
    local java_nav=""
    local xml_nav=""
    local xml_idx=0

    echo "[INFO] Generating combined source view..."

    # ---- Collect XML/PRO files into array ----
    local xml_files=()
    while IFS= read -r -d '' f; do
        xml_files+=("$f")
    done < <(find "$APP_SRC_DIR" \( -name "*.xml" -o -name "*.pro" \) -type f -print0 | sort -z)

    # ---- FIRST PASS: Java nav + stats ----
    for java_file in "$JAVA_SRC_DIR"/*.java; do
        [[ ! -f "$java_file" ]] && continue
        filename=$(basename "$java_file" .java)
        class_id=$(echo "${filename,,}" | tr -cd '[:alnum:]')
        line_count=$(wc -l < "$java_file")
        total_java=$((total_java + 1))
        total_lines=$((total_lines + line_count))
        java_nav="${java_nav}<a href=\"#${class_id}\">${filename}</a> "
    done

    if [[ $total_java -eq 0 ]]; then
        echo "[ERROR] No Java files found in $JAVA_SRC_DIR"
        exit 1
    fi

    # ---- FIRST PASS: XML nav + stats ----
    for xml_file in "${xml_files[@]}"; do
        xml_idx=$((xml_idx + 1))
        rel_path="${xml_file#$APP_SRC_DIR/}"
        class_id="xml${xml_idx}"
        line_count=$(wc -l < "$xml_file")
        total_xml=$((total_xml + 1))
        total_lines=$((total_lines + line_count))
        xml_nav="${xml_nav}<a href=\"#${class_id}\">${rel_path}</a> "
    done

    # ---- WRITE HEADER ----
    cat > "$output" << HEADER
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>KnightLight - All Source</title>
<style>
body { font-family: monospace; background: #1a1a2e; color: #eaeaea; margin: 0; }
header { background: #6d4aff; padding: 20px; position: sticky; top: 0; z-index: 100; }
header h1 { color: white; margin: 0; }
.stats { color: #ccc; font-size: 0.85em; margin-top: 5px; }
nav { background: #2a2a4a; padding: 12px; position: sticky; top: 65px; z-index: 99; overflow-x: auto; white-space: nowrap; }
nav a { color: #aaaaff; text-decoration: none; margin-right: 15px; padding: 4px 8px; border-radius: 4px; }
nav a:hover { background: #4a4a8a; }
.nav-label { color: #8a8acc; font-size: 0.75em; text-transform: uppercase; margin-right: 10px; }
.nav-br { display: block; height: 6px; }
.section-divider { background: #6d4aff; color: white; padding: 15px 20px; font-size: 1.1em; margin: 10px 20px; border-radius: 8px; }
.file { border: 1px solid #4a4a8a; margin: 20px; background: #1e1e3e; border-radius: 8px; overflow: hidden; }
.file-header { background: #2a2a5a; padding: 12px 18px; border-bottom: 2px solid #6d4aff; display: flex; justify-content: space-between; align-items: center; }
.file-header h2 { color: #d0d0ff; font-size: 1.2em; margin: 0; }
.file-meta { color: #888; font-size: 0.8em; }
pre { padding: 18px; overflow-x: auto; background: #0f0f1e; margin: 0; white-space: pre; }
code { font-family: "Courier New", monospace; color: #d0d0ff; font-size: 0.9em; }
footer { padding: 25px; color: #666; text-align: center; border-top: 1px solid #4a4a8a; }
</style>
</head>
<body>
<header>
<h1>KnightLight - Complete Source Code</h1>
<div class="stats">${total_java} Java files | ${total_xml} XML/Config files | ${total_lines} total lines | $(date '+%Y-%m-%d %H:%M')</div>
</header>
<nav>
<span class="nav-label">Java:</span>${java_nav}
<span class="nav-br"></span>
<span class="nav-label">XML/Config:</span>${xml_nav}
</nav>
<div id="files">
HEADER

    # ---- SECOND PASS: Write Java files ----
    for java_file in "$JAVA_SRC_DIR"/*.java; do
        [[ ! -f "$java_file" ]] && continue
        filename=$(basename "$java_file" .java)
        class_id=$(echo "${filename,,}" | tr -cd '[:alnum:]')
        line_count=$(wc -l < "$java_file")
        file_size=$(wc -c < "$java_file")
        escaped=$(sed 's/&/\&amp;/g; s/</\&lt;/g; s/>/\&gt;/g' "$java_file")
        cat >> "$output" << FILEBLOCK
<div class="file" id="${class_id}">
<div class="file-header"><h2>${filename}.java</h2><span class="file-meta">${line_count} lines | ${file_size} bytes</span></div>
<pre><code>${escaped}</code></pre>
</div>
FILEBLOCK
    done

    # ---- SECTION DIVIDER ----
    cat >> "$output" << DIVIDER
<div class="section-divider">Android Configuration &amp; Resources</div>
DIVIDER

    # ---- SECOND PASS: Write XML/PRO files ----
    xml_idx=0
    for xml_file in "${xml_files[@]}"; do
        xml_idx=$((xml_idx + 1))
        class_id="xml${xml_idx}"
        rel_path="${xml_file#$APP_SRC_DIR/}"
        line_count=$(wc -l < "$xml_file")
        file_size=$(wc -c < "$xml_file")
        escaped=$(sed 's/&/\&amp;/g; s/</\&lt;/g; s/>/\&gt;/g' "$xml_file")
        cat >> "$output" << FILEBLOCK
<div class="file" id="${class_id}">
<div class="file-header"><h2>${rel_path}</h2><span class="file-meta">${line_count} lines | ${file_size} bytes</span></div>
<pre><code>${escaped}</code></pre>
</div>
FILEBLOCK
    done

    # ---- CLOSE DOCUMENT ----
    cat >> "$output" << FOOTER
</div>
<footer>Auto-generated by KnightLight Documentation Pipeline v5.0</footer>
<script>
document.querySelectorAll('nav a').forEach(function(a){
    a.addEventListener('click',function(e){
        e.preventDefault();
        var t=document.querySelector(this.getAttribute('href'));
        if(t) window.scrollTo({top:t.offsetTop-85,behavior:'smooth'});
    });
});
</script>
</body>
</html>
FOOTER

    chmod 644 "$output"
    echo "[OK] Created combined view: ALL_SOURCE.html (${total_java} Java + ${total_xml} XML/Config = $((total_java + total_xml)) files, ${total_lines} lines)" | tee -a "$LOG_FILE"
}

# =====================================================================
# MAIN EXECUTION
# =====================================================================

{
    echo "=========================================================================="
    echo "Conversion Session: $(date '+%Y-%m-%d %H:%M:%S')"
    echo "Script Version: 5.0"
    echo "Java Source: $(cd "$JAVA_SRC_DIR" && pwd)"
    echo "App Source: $(cd "$APP_SRC_DIR" && pwd)"
    echo "Java Files: $(find "$JAVA_SRC_DIR" -maxdepth 1 -name "*.java" -type f | wc -l)"
    echo "XML/Config Files: $(find "$APP_SRC_DIR" \( -name "*.xml" -o -name "*.pro" \) -type f | wc -l)"
    echo "=========================================================================="
} >> "$LOG_FILE"

echo "=========================================================================="
echo "  Source-to-HTML Converter v5.0"
echo "  Output: $(cd "$DOCS_DIR" && pwd)"
echo "=========================================================================="
echo ""

generate_combined_page

echo ""
echo "=========================================================================="
echo "  Conversion complete. Check .conversion-audit.log for details."
echo "=========================================================================="
echo ""
echo "Generated files:"
echo "  docs/ALL_SOURCE.html - Combined view (Java + XML + ProGuard)"
echo ""
echo "------------------------------------------------------------"
echo "Deploy to GitHub (copy paste this single line):"
echo ""
echo "git add docs/ && git commit -m \"Regenerate ALL_SOURCE.html with XML+PRO\" && git push origin main"
echo ""
echo "------------------------------------------------------------"
echo "NOTE: Do NOT auto-push from this script."
echo "      Review changes manually before deploying."
echo "============================================================"
