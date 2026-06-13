#!/bin/bash

# ============================================================
# Script Auto Compile LaTeX khi có file thay đổi
# Output: outputs/main.pdf
# ============================================================

OUTPUT_DIR="outputs"

compile() {
    echo "========================================="
    echo "Dang compile LaTeX..."
    echo "========================================="

    mkdir -p "$OUTPUT_DIR"

    xelatex -interaction=nonstopmode -halt-on-error \
        -output-directory="$OUTPUT_DIR" main.tex || return 1
    biber --output-directory "$OUTPUT_DIR" main || return 1
    xelatex -interaction=nonstopmode -halt-on-error \
        -output-directory="$OUTPUT_DIR" main.tex || return 1
    xelatex -interaction=nonstopmode -halt-on-error \
        -output-directory="$OUTPUT_DIR" main.tex || return 1

    echo "========================================="
    echo "Compile hoan tat: $OUTPUT_DIR/main.pdf"
    echo "========================================="
}

# Chay lan dau tien
compile

# Theo doi thay doi (bo qua thu muc build outputs/)
# Luu y: inotifywait khong cho dung --include va --exclude cung luc
while inotifywait -r -e modify,move,create,delete . \
    --exclude "(^|/)${OUTPUT_DIR}/"; do
    compile
done
