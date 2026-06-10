#!/bin/bash

# ============================================================
# Script Auto Compile LaTeX khi có file thay đổi
# ============================================================

compile() {
    echo "========================================="
    echo "🔄 Phát hiện thay đổi! Đang compile LaTeX..."
    echo "========================================="
    
    # Chạy xelatex lần 1
    xelatex -interaction=nonstopmode -halt-on-error main.tex
    
    # Chạy biber (nếu có references)
    # biber main
    
    # Chạy xelatex lần 2 để update mục lục và reference
    # xelatex -interaction=nonstopmode -halt-on-error main.tex
    
    # Xóa các file rác sinh ra trong quá trình compile
    rm -f *.aux *.log *.out *.toc *.lof *.lot *.fls *.fdb_latexmk *.bbl *.bcf *.blg *.run.xml
    
    echo "========================================="
    echo "✅ Compile hoàn tất! Đang theo dõi thay đổi..."
    echo "========================================="
}

# Chạy lần đầu tiên
compile

# Dùng inotifywait để lắng nghe sự thay đổi của bất kỳ file .tex hoặc .bib nào
# (Tối ưu hơn lệnh 'watch' vì chỉ compile khi thực sự có file được save)
while inotifywait -r -e modify,move,create,delete . --include '.*\.(tex|bib)$'; do
    compile
done
