# Quy chuẩn viết báo cáo — Thành viên nhóm

Tài liệu này bổ sung cho [`requirement.md`](requirement.md) (tiêu chí CLO từ giảng viên). **Mọi thành viên phải tuân thủ** khi viết nội dung LaTeX trong `latex/`.

Tham khảo format mô tả tính năng: [`example.md`](example.md).

---

## 1. Phân công và phạm vi sửa file

| Chương | File | Phụ trách | Không được sửa |
|--------|------|-----------|----------------|
| 1 | `latex/chapter1_system/chapter1.tex` | Trưởng nhóm | `main.tex`, `preamble.tex` |
| 2 | `latex/chapter2_auth_profile/chapter2.tex` | Thành viên 2 | File chapter khác |
| 3 | `latex/chapter3_post_search/chapter3.tex` | Thành viên 3 | File chapter khác |
| 4 | `latex/chapter4_chat_videocall/chapter4.tex` | Thành viên 4 | File chapter khác |
| 5 | `latex/chapter5_admin/chapter5.tex` | Trưởng nhóm | File chapter khác |

**Quy tắc chung:**

- Chỉ sửa **file chapter được giao**. Cần đổi style chung → nhắn trưởng nhóm sửa `preamble.tex`.
- Điền **tên + MSSV** vào header comment đầu file chapter (thay `[Thành viên X]`).
- Viết bằng **tiếng Việt**, văn phong báo cáo (không copy code comment tiếng Anh nguyên văn).
- Không xóa `\section` / `\subsection` đã có trong outline — chỉ **điền nội dung** bên dưới. Muốn thêm mục mới → thống nhất với trưởng nhóm trước.

---

## 2. Khung bắt buộc cho mỗi `\section` (Chương 2–4)

Mỗi **section tính năng lớn** (ví dụ: Đăng nhập, Home Feed, Danh sách hội thoại) phải có đủ **4 phần** theo thứ tự sau.

Nếu outline hiện tại chỉ có `\subsection` rời, **gom lại** hoặc đổi tên subsection cho khớp 4 phần này.

### 2.1. Yêu cầu chức năng

- Mô tả người dùng làm được gì (người thuê / chủ trọ).
- Nêu input, output, điều kiện (đã đăng nhập, có mạng, …).

### 2.2. Thiết kế giao diện

- **Bắt buộc:** ít nhất **1 hình screenshot** màn hình (đặt tên theo mục 3).
- Liệt kê **tên widget** dùng trong layout XML, ví dụ:
  - `TextView`, `EditText`, `MaterialButton`
  - `RecyclerView` (ghi rõ: thay thế ListView theo chuẩn Android hiện đại)
  - `GridLayoutManager` / gallery (ghi rõ: tương đương GridView nếu có)
- Không chụp màn hình mờ, che status bar lỗi, hoặc dữ liệu cá nhân thật.

### 2.3. Luồng xử lý

- Trình bày **5–8 bước** (View → ViewModel → Repository → API / Local).
- Tính năng phức tạp (chat, video call): thêm **1 sơ đồ** (Mermaid `.mmd` trong `latex/images/`, export PNG vào `latex/images/outputs/`).

### 2.4. Triển khai trên Android

- Ghi **class chính** (Activity / Fragment / ViewModel / Repository).
- Ghi **package** (đã có gợi ý trong header file chapter).
- Nếu gọi API: ghi **endpoint** (method + path), **request/response** model (`LoginRequest`, …).
- Có thể trích **đoạn code ngắn**  bằng `\begin{lstlisting}...\end{lstlisting}` — không paste cả file.

---

## 3. Hình ảnh và sơ đồ

| Loại | Đặt tại | Quy tắc đặt tên |
|------|---------|-----------------|
| Screenshot UI | `latex/images/outputs/` | `c2_login_screen.png`, `c3_home_feed.png`, … |
| Sơ đồ Mermaid (nguồn) | `latex/images/` | `c4_chat_flow.mmd` |
| PNG export từ Mermaid | `latex/images/outputs/` | cùng tên với `.mmd` |

Trong `.tex`:

```latex
\begin{figure}[H]
    \centering
    \includegraphics[width=0.75\textwidth]{images/outputs/c2_login_screen.png}
    \caption{Màn hình đăng nhập}
    \label{fig:c2_login}
\end{figure}
```

- Tiền tố `c1_` … `c5_` **đúng với số chương** — tránh ghi đè ảnh người khác.
- Mỗi chương (2–4): tối thiểu **2 hình** (screenshot hoặc sơ đồ).

---

## 4. Build và kiểm tra trước khi merge

```bash
cd docs/reports/latex
./comp.sh          # hoặc chạy một lần rồi Ctrl+C
```

- PDF output: `latex/outputs/main.pdf` (không commit thư mục `outputs/`).
- Trước khi báo xong chapter: **tự build**, mở PDF, kiểm tra:
  - Không lỗi LaTeX (missing image, undefined reference).
  - Hình hiển thị đúng, caption có tiếng Việt.
  - Không tràn layout quá nặng (overfull hbox — chỉnh wording hoặc `width` ảnh).

**Không commit:** `latex/outputs/`, `*.aux`, `*.log` ở thư mục gốc latex.

---

## 5. Trích dẫn và tài liệu tham khảo

- Kiến trúc Android, Retrofit, Room, WebRTC: thêm entry vào `latex/references.bib` nếu trích dẫn lần đầu.
- Trong text dùng `\cite{key}` — không dán URL trần trong paragraph.
- Thành viên **không** tự ý đổi style bibliography trong `preamble.tex`.

---

## 6. Checklist nộp chapter (tự kiểm tra)

```
[ ] Đã điền tên + MSSV ở header file .tex
[ ] Mỗi section lớn đủ 4 phần: chức năng / giao diện / luồng / triển khai
[ ] ≥ 2 hình ảnh, đặt tên đúng c{N}_*
[ ] Đã nhắc tên widget (TextView, RecyclerView, …) và class Java chính
[ ] Đã ghi API (nếu section có gọi server)
[ ] Có câu "Đáp ứng CLO…" ở cuối mỗi section lớn
[ ] Build thành công, đã xem outputs/main.pdf
[ ] Không sửa file chapter / main / preamble của người khác
```

---

## 7. Điều cấm

- Copy nguyên văn từ `ARCHITECTURE.md` / `CHAT_ARCHITECTURE.md` không chỉnh sửa.
- Viết tính năng **không có trong app** (ví dụ: Google Sign-In, admin web đầy đủ) trừ khi đã implement.
- Để subsection trống khi nộp bản gần hoàn thiện.
- Đổi cấu trúc `main.tex` hoặc đổi thứ tự chapter khi chưa thống nhất nhóm.

---

## 8. Liên hệ

- Style LaTeX, merge conflict, build lỗi → **trưởng nhóm**.
- Nội dung nghiệp vụ / API sai → chủ feature trong code + người viết chapter cùng rà soát.

**Phiên bản:** 1.0 — áp dụng đồ án Trọ Tốt Android, học phần PTUD thiết bị di động.
