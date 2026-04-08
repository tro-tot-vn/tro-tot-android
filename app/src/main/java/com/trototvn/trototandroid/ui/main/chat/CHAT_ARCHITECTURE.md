# Kiến Trúc Chat - Tính năng gửi Hình Ảnh

Tài liệu này giải thích cách triển khai tính năng gửi hình ảnh trong tính năng Chat dựa theo chuẩn kiến trúc: Clean Architecture, MVVM, RxJava và Room của dự án.

## 1. Luồng xử lý gửi file (Từ lúc chọn ảnh tới khi lưu Room)

Quá trình tải lên hình ảnh và gửi tin nhắn là atomic (gộp chung một luồng xử lý) với các bước sau:

1. **Giao diện (UI)**:
   - File `ChatFragment.java` sử dụng `ActivityResultContracts.GetContent()` để gọi Intent `image/*`.
   - Khi người dùng chọn hình ảnh, nhận được `Uri`.
   - Gọi hàm `sendImage(Context, Uri)` trong `ChatViewModel`.

2. **ViewModel (Presentation Layer)**:
   - `ChatViewModel` thiết lập trạng thái Loading bằng `Resource.loading()`.
   - Gọi sang layer Repository, thực thi Single stream `uploadMediaAndSendMessage()`.
   - Đăng ký nhận kết quả (subscribe) tại luồng main để xử lý trạng thái SUCCESS và ERROR của UI thong qua LiveData. Cập nhật `MutableLiveData<Resource<MessageEntity>>`.

3. **Repository (Data Layer)**:
   - Mở `InputStream` từ `Uri` thông qua `ContentResolver`.
   - Chép dữ liệu từ luồng vào một file tạm (Temporary File) trong cơ sở lưu trữ phân vùng cache của ứng dụng thông qua `context.getCacheDir()`.
   - Xây dựng `MultipartBody.Part` từ file tạm đã lập, và một `RequestBody` cho chuỗi caption (hiện tại caption="").
   - Gửi file qua API POST `/chat/conversations/{id}/files` thông qua interface retrofit `ApiService`.
   - Khi API Response trả về (`MessageDto` chứa thông tin file), nó map qua `MessageEntity` và `MessageAttachmentEntity`.
   - Insert vào local database thông qua thẻ phòng (Room db) là `ChatDao` (`insertMessage` và `insertAttachments`).
   - Xóa bỏ file tạm để giải phóng bộ nhớ. Gửi Object thực thể (`MessageEntity`) về lại ViewModel.

4. **Trạng thái Màn Hình (Render Data)**:
   - Do stream tin nhắn đã được theo dõi chung cho phần hội thoại trước đó (SOT - Single Source of Truth thông qua Flowable của Room `chatMessagesLiveData`), UI sẽ tự động đẩy tin nhắn hình ảnh mới này vào danh sách tin nhắn để hiển thị ra cho người dùng.

## 2. Áp dụng chuẩn Resource<T> để quản lý State

Chuẩn `Resource<T>` được sử dụng để gói gọn và truyền tải trạng thái Data/Network đến UI cho tính năng Upload.

- **Vòng đời State Upload**:
  1. `LOADING`: Giao diện bị khóa chức năng (disable) với `btnAttach` và `btnSend` để tránh gửi lặp khi mạng chậm.
  2. `SUCCESS`: Khôi phục nút và xóa Input Text sau khi file được upload thành công.
  3. `ERROR`: Khôi phục nút bấm kèm theo Toast hiển thị mã lỗi (VD: do thiết lập mạng, giới hạn kích thước).

Triển khai tại View Layer:

```java
// Trong ViewModel:
private final MutableLiveData<Resource<MessageEntity>> uploadState = new MutableLiveData<>();

// Khi thực thi (Bao quanh bởi Disposable):
handleLoading(uploadState); // Đặt trạng thái vào LOADING
addDisposable(repository.action()
    .subscribe(
         entity -> handleSuccess(uploadState, entity), // Thành công => Status.SUCCESS
         error -> handleError(uploadState, "Lỗi: " + error.getMessage()) // Thất bại => Status.ERROR
    ));
```

Pattern trên thỏa mãn tiêu chuẩn Clean qua nguyên tắc Isolation (chia tách), State management an toàn và tuân thủ tuyệt đối SSOT bằng RxJava.
