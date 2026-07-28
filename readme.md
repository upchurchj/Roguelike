# Java to HTML Converter Script
**Purpose:** Convert Java source files to styled HTML documentation for easy AI viewing via GitHub Pages.

## Overview
`convert-java-to-html.sh` transforms Java class files into beautifully formatted HTML documents hosted on GitHub Pages—making your codebase easily accessible for AI models, documentation review, and collaborative analysis.

### Features
- Converts all Java files in `src/main/java/com/knightlight/game/` to individual HTML pages
- Generates `index.html` with a clickable grid of all converted classes
- Syntax highlighting and readable formatting for AI/human review
- Atomic replacements with backup copies in `.backups/`
- Safety checks: HTML escaping, 1MB file size limit
- Outputs to `docs/` directory (ready for GitHub Pages)

### Quick Setup
1. Add to `~/.bashrc`:
```bash
alias bhtml='bash $PWD/convert-java-to-html.sh'
```
2. Run: `source ~/.bashrc` then `bhtml`

### GitHub Pages Hosting
- Push `docs/` to your repository
- Go to Settings → Pages
- Set source to main branch `/docs` folder
- Live at: `https://upchurchj.github.io/Roguelike/`

### Output
- Class files: `docs/ClassName.html`
- Index: `docs/index.html` (grid view with class count and timestamp)
- Backups: `.backups/ClassName.html`

### Git Workflow
```bash
git add docs/
git commit -m "Add HTML documentation for all classes"
git push origin feature/integrate-new-systems

```

✅ Alias uses $PWD for portability — production-ready
