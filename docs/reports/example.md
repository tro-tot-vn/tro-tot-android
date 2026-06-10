| **HỌC VIỆN CÔNG NGHỆ BƯU CHÍNH VIỄN THÔNG**<br><br>**KHOA: CÔNG NGHỆ THÔNG TIN 2** | | |
| --- | | | --- |
| Học phần: **Phát triển ứng dụng cho các thiết bị di động**<br><br>Trình độ đào tạo: **Đại học** <br><br>Hình thức đào tạo: **Chính qui** | &nbsp; |

**THÔNG TIN ĐỀ TÀI DỰ ÁN**

**ĐỀ TÀI SỐ 1**

**1\. Tên đề tài:** Xây dựng ứng dụng quản lý tài chính cá nhân Prudential Finance

**2\. Số lượng sinh viên yêu cầu:** 3 sinh viên

- Lê Tự Minh Lợi MSSV: N22DCVT054 &lt;Trưởng nhóm&gt;
- Phạm Lâm Bảo Vinh MSSV: N22DCDK095 &lt;Thành viên&gt;
- Nguyễn Văn Quang MSSV : N22DCVT076 &lt; Thành viên&gt;

**3\. Mô tả đề tài**

**Các yêu cầu chính của đề tài:**

#### **Phân hệ Xác thực và Bảo mật**

Tại màn hình đăng nhập, ứng dụng tích hợp thư viện Google Sign-In để tối giản thao tác cho người dùng. Đối với việc đăng nhập truyền thống, mật khẩu của người dùng được mã hóa một chiều bằng thuật toán **bcrypt** trước khi lưu xuống cơ sở dữ liệu, đảm bảo an toàn tuyệt đối ngay cả khi dữ liệu bị rò rỉ. Token JWT sau khi nhận được từ Server sẽ được lưu trữ an toàn trong SharedPreferences của Android để duy trì phiên đăng nhập.

#### **Phân hệ Quản lý Giao dịch (Dashboard)**

#### Giao diện chính (Dashboard) được thiết kế với triết lý cung cấp thông tin quan trọng ngay lập tức. Phần trên cùng hiển thị tổng số dư hiện tại của tất cả các tài khoản. Ngay bên dưới là biểu đồ tóm tắt tình hình thu chi trong 7 ngày gần nhất. Danh sách giao dịch được hiển thị thông qua RecyclerView, tích hợp khả năng cuộn vô tận (infinite scroll) để tải dữ liệu mượt mà. Đặc biệt, nhóm đã cài đặt tính năng vuốt sang trái hoặc phải (Swipe gestures) trên mỗi dòng giao dịch để thực hiện nhanh thao tác sửa hoặc xóa, mang lại trải nghiệm người dùng hiện đại và tiện lợi

#### **Phân hệ Báo cáo và Thống kê**

#### Đây là tính năng thể hiện rõ nhất giá trị của ứng dụng. Nhóm sử dụng thư viện MPAndroidChart để xây dựng các biểu đồ trực quan. Biểu đồ tròn (Pie Chart) được dùng để thể hiện cơ cấu chi tiêu, giúp người dùng nhận biết danh mục nào đang chiếm tỷ trọng lớn nhất (ví dụ: 40% cho Ăn uống). Biểu đồ cột (Bar Chart) được dùng để so sánh đối chiếu giữa tổng thu và tổng chi theo từng tháng. Ngoài ra, chức năng xuất báo cáo ra file PDF cũng được tích hợp, cho phép người dùng lưu trữ hoặc in ấn dữ liệu tài chính định kỳ

#### **Phân hệ Quản lý Mục tiêu (Goals)**

#### Tính năng này được xây dựng nhằm khuyến khích thói quen tiết kiệm. Khi người dùng tạo một mục tiêu (ví dụ: Mua Laptop), hệ thống sẽ tạo ra một thanh tiến trình (Progress bar). Mỗi khi người dùng thực hiện thao tác "nạp tiền" vào mục tiêu, thanh tiến trình sẽ tự động cập nhật phần trăm hoàn thành. Màu sắc của mục tiêu cũng thay đổi linh hoạt dựa trên trạng thái: màu xanh dương khi đang thực hiện, màu xanh lá khi hoàn thành và màu đỏ khi đã quá hạn deadline nhưng chưa đạt đủ số tiền

#### **Phân hệ Quản lý Danh mục (Category Customization)**

Khác với các ứng dụng cứng nhắc chỉ cung cấp danh mục có sẵn, Prudential Finance trao quyền chủ động cho người dùng thông qua việc tùy biến danh mục. Nhóm phát triển đã tích hợp thư viện chọn màu (Color Picker) và bộ sưu tập icon phong phú, cho phép người dùng tự tạo ra các danh mục thu/chi mang tính cá nhân hóa cao. Hệ thống phân tách rõ ràng giữa hai loại danh mục: "Thu nhập" và "Chi tiêu". Mỗi danh mục được gán một mã màu (Hex code) riêng biệt, mã màu này sẽ được sử dụng xuyên suốt trong ứng dụng, từ icon hiển thị trong lịch sử giao dịch đến các lát cắt màu sắc trên biểu đồ tròn báo cáo, tạo nên sự đồng bộ về mặt thị giác (Visual Consistency)..

#### **Phân hệ Quản lý Ngân sách (Budget Control)**

Đây là chức năng đóng vai trò "người giám sát" tài chính. Người dùng có thể thiết lập hạn mức chi tiêu tối đa cho từng danh mục cụ thể trong một tháng (ví dụ: tối đa 2.000.000 VNĐ cho Ăn uống). Về mặt xử lý logic, hệ thống sử dụng một thuật toán tổng hợp chạy ngầm mỗi khi người dùng mở màn hình Ngân sách. Thuật toán này sẽ tính tổng tất cả các giao dịch chi tiêu thuộc danh mục đó trong tháng hiện tại và so sánh với hạn mức đã đặt. Kết quả được hiển thị dưới dạng thanh tiến trình (Progress Bar). Nếu số tiền chi tiêu vượt quá hạn mức, thanh tiến trình sẽ chuyển sang màu đỏ rực và hiển thị cảnh báo, giúp người dùng điều chỉnh hành vi tiêu dùng kịp thời trước khi "vỡ kế hoạch".

**Quản lý hồ sơ:** Người dùng có thể cập nhật thông tin cá nhân, thay đổi ảnh đại diện (Avatar). Ảnh đại diện được lưu trữ trên Server và được tải về hiển thị mượt mà thông qua thư viện CircleImageView kết hợp với Picasso

**Đa ngôn ngữ**: Hệ thống hỗ trợ song ngữ Tiếng Việt và Tiếng Anh, sử dụng cơ chế Locale của Android để chuyển đổi ngôn ngữ tức thời mà không cần khởi động lại ứng dụng.

**Tài khoản người dùng:**

- Đăng ký, đăng nhập, cập nhật thông tin cá nhân.

**Giao diện người dùng:**

- Giao diện dễ dùng, rõ ràng, tối ưu.
- Hỗ trợ Light/Dark mode.

**4\. Yêu cầu nhóm và học viên**

- Phát triển ứng dụng di động bằng ngôn ngữ lập trình Java đáp ứng các yêu cầu về chức năng giao diện trên. \[CLO1\]
- Áp dụng được các kiến thức đã học như dùng các thành phần TextView, ListView, GridView, gọi API, … \[CLO2\]
- Có phân quyền, và login, bảo vệ tài khoản cá nhân quyền riêng tư để đáp ứng các yêu cầu nghiệp vụ trên. \[CLO2\]
- Thuyết trình và bảo vệ được kết quả công việc của cá nhân, mô hình kiến trúc, các tiêu chuẩn, nguyên tắc áp dụng trong dự án. \[CLO2\]
