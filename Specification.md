# FocusMate — Product Specification v1.1

Android · Kotlin · Jetpack Compose · Room · Offline-first

2 chức năng chính

Không cần tài khoản

Không cần backend

# 1. Tổng quan sản phẩm

## 1.1. Giới thiệu

FocusMate là ứng dụng di động giúp người dùng quản lý công việc cá nhân và tập trung làm việc bằng phương pháp Pomodoro. Ứng dụng cung cấp một danh sách công việc đơn giản cùng bộ đếm thời gian tập trung, hướng đến trải nghiệm tối giản, dễ sử dụng và không yêu cầu đăng nhập.

## 1.2. Mục tiêu

- Quản lý công việc hằng ngày một cách trực quan.
- Hỗ trợ tập trung theo các phiên Pomodoro.
- Liên kết phiên tập trung với công việc cần thực hiện.
- Lưu dữ liệu cục bộ trên thiết bị.
- Giữ phạm vi phát triển phù hợp với một dự án Android cá nhân hoặc đồ án môn học.

## 1.3. Phạm vi chức năng

| Mã | Chức năng | Phạm vi |
| --- | --- | --- |
| F01 | To-do List | Tạo, sửa, xóa, xem và hoàn thành công việc |
| F02 | Pomodoro Timer | Đếm ngược, tạm dừng, tiếp tục, đặt lại và quản lý phiên tập trung |

# 2. Đặc tả chức năng F01 — To-do List

Cho phép người dùng quản lý danh sách công việc cá nhân, theo dõi trạng thái thực hiện và chọn công việc để bắt đầu phiên Pomodoro.

| Mã | Yêu cầu | Mô tả |
| --- | --- | --- |
| FR01 | Xem danh sách | Hiển thị các công việc đã lưu |
| FR02 | Thêm công việc | Tạo công việc với tiêu đề bắt buộc |
| FR03 | Chỉnh sửa | Cập nhật thông tin công việc |
| FR04 | Xóa công việc | Xóa công việc sau khi xác nhận hoặc xóa công việc khi không cần làm nữa |
| FR05 | Đánh dấu hoàn thành | Chuyển trạng thái công việc |
| FR07 | Chọn công việc tập trung | Chọn một công việc để gắn với phiên Pomodoro |
| FR08 | Lưu dữ liệu | Giữ nguyên dữ liệu khi đóng và mở lại ứng dụng |
- **Thông tin công việc**


    | Thuộc tính | Kiểu dữ liệu | Bắt buộc |
    | --- | --- | --- |
    | taskId | Int | Có |
    | title | String | Có |
    | description | String? | Không |
    | dueDate | Long? | Không |
    | priority | Enum | Có |
    | status | Enum | Có |
    | createdAt | Long | Có |
    | completedAt | Long? | Không |
- **Giá trị mặc định**:
    - `priority = LOW`
    - `status = TODO`
    - `createdAt = thời điểm tạo`
    - `completedAt = null`
- **Quy tắc nghiệp vụ**:
    - Tiêu đề không được để trống.
    - Mã công việc được hệ thống tự sinh.
    - Công việc đã hoàn thành vẫn được hiển thị trong bộ lọc Completed.
    - Người dùng có thể bắt đầu Pomodoro mà không cần chọn công việc.
    - Nếu xóa công việc đang gắn với timer, phiên hiện tại không được tự động bị lỗi. Ứng dụng cần bỏ liên kết công việc hoặc yêu cầu người dùng xác nhận trước khi xóa.

# 3. Đặc tả chức năng F02 — Pomodoro Timer

Cho phép người dùng thực hiện các phiên tập trung và nghỉ theo phương pháp Pomodoro, đồng thời có thể gắn phiên tập trung với một công việc trong To-do List.

- **Tham số mặc định**:


    | Loại phiên | Thời lượng |
    | --- | --- |
    | Focus | 25 phút |
    | Short Break | 5 phút |
    | Long Break | 15 phút |
    | Chu kỳ nghỉ dài | Sau 4 phiên Focus hoàn thành |
- **Yêu cầu chức năng**


    | Mã | Yêu cầu | Mô tả |
    | --- | --- | --- |
    | FR09 | Bắt đầu timer | Khởi chạy bộ đếm ngược |
    | FR10 | Tạm dừng | Tạm dừng phiên đang chạy |
    | FR11 | Tiếp tục | Tiếp tục phiên đang tạm dừng |
    | FR12 | Đặt lại | Đưa bộ đếm về thời lượng ban đầu |
    | FR13 | Chọn loại phiên | Focus, Short Break, Long Break |
    | FR14 | Chọn công việc | Liên kết phiên Focus với một task |
    | FR15 | Thông báo kết thúc | Thông báo khi bộ đếm về 0 |
    | FR16 | Lưu trạng thái timer | Khôi phục phiên khi ứng dụng bị đóng hoặc mở lại |
    | FR17 | Điều hướng phiên | Đề xuất phiên nghỉ sau Focus hoặc Focus sau phiên nghỉ |
    |  |  |  |
- Trạng thái timer:
    - Focus Session
    - Idle / Running
    - Paused
    - Completed
    - Reset

- Quy tắc nghiệp vụ:
    - Chỉ phiên Focus được tính là phiên tập trung hoàn thành; phiên nghỉ không được tính.
    - Reset trước khi hoàn thành sẽ hủy phiên hiện tại, không ghi nhận là phiên hoàn thành.
    - Phiên hoàn thành chỉ được ghi nhận một lần.
    - Timer phải tính thời gian dựa trên timestamp hoặc thời điểm kết thúc dự kiến, tránh sai lệch khi ứng dụng chạy nền.
    - Khi timer kết thúc, ứng dụng phát thông báo nếu người dùng đã cấp quyền cần thiết.
    - Phiên Focus đang chạy không bị mất chỉ vì người dùng chuyển sang màn hình Tasks.
    - Không bắt buộc tự động chuyển sang phiên kế tiếp; người dùng có thể xác nhận để bắt đầu phiên tiếp theo.

## 4. Điều hướng

Đề xuất sử dụng Bottom Navigation gồm 3 mục:

- Home
- Focus

Khi người dùng nhấn Start Focusing từ Home hoặc chọn Focus từ Bottom Navigation, ứng dụng mở màn hình Focus. Nếu đã có phiên đang chạy, màn hình phải khôi phục đúng trạng thái của phiên đó.

# 5. Thiết kế dữ liệu

Sử dụng Room Database cho dữ liệu công việc và lịch sử phiên tập trung. Trạng thái timer có thể lưu bằng DataStore hoặc Room.

## 5.1. Bảng Task

| Trường | Kiểu dữ liệu | Mô tả |
| --- | --- | --- |
| taskId | Int | Khóa chính tự tăng |
| title | String | Tiêu đề |
| description | String? | Mô tả |
| dueDate | Long? | Hạn hoàn thành |
| priority | String | LOW, MEDIUM, HIGH |
| status | String | TODO, IN_PROGRESS, COMPLETED |
| createdAt | Long | Thời điểm tạo |
| completedAt | Long? | Thời điểm hoàn thành |

## 5.2. Bảng FocusSession

| Trường | Kiểu dữ liệu | Mô tả |
| --- | --- | --- |
| sessionId | Int | Khóa chính |
| taskId | Int? | Công việc liên quan |
| sessionType | String | FOCUS, SHORT_BREAK, LONG_BREAK |
| plannedDuration | Int | Thời lượng dự kiến, giây |
| actualDuration | Int | Thời lượng thực tế, giây |
| startedAt | Long | Thời điểm bắt đầu |
| endedAt | Long? | Thời điểm kết thúc |
| status | String | RUNNING, PAUSED, COMPLETED, CANCELLED |

Bảng `FocusSession` vẫn được giữ lại để lưu trạng thái và lịch sử timer, không dùng để tạo Report trong phiên bản này.

## 5.3. Lưu trạng thái timer

Ứng dụng cần lưu:

- Loại phiên đang thực hiện.
- Số giây còn lại hoặc thời điểm dự kiến kết thúc.
- Trạng thái timer.
- Công việc đang được liên kết.
- Mã phiên hiện tại.

# 6. Kiến trúc phần mềm

Đề xuất sử dụng MVVM kết hợp Repository Pattern.

HomeScreen · FocusScreen

ViewModel Layer

TaskViewModel · TimerViewModel

Repository Layer

TaskRepository · SessionRepository

Data Layer

Room Database · DataStore

Cấu trúc thư mục dự kiến:

```
com.example.focusmate/
├── data/
│   ├── local/
│   │   ├── AppDatabase.kt
│   │   ├── TaskDao.kt
│   │   └── FocusSessionDao.kt
│   ├── entity/
│   │   ├── TaskEntity.kt
│   │   └── FocusSessionEntity.kt
│   └── repository/
│       ├── TaskRepository.kt
│       └── SessionRepository.kt
├── ui/
│   ├── home/
│   ├── tasks/
│   ├── timer/
│   └── navigation/
├── viewmodel/
│   ├── TaskViewModel.kt
│   └── TimerViewModel.kt
└── MainActivity.kt
```

# 7. Yêu cầu phi chức năng

| Mã | Yêu cầu | Tiêu chí nghiệm thu |
| --- | --- | --- |
| NFR01 | Hiệu năng | Danh sách công việc hiển thị mượt với dữ liệu thông thường |
| NFR02 | Độ chính xác timer | Thời gian còn lại được tính đúng khi ứng dụng chạy nền |
| NFR03 | Lưu trữ | Task và trạng thái timer được khôi phục sau khi mở lại app |
| NFR04 | Khả dụng | Giao diện đơn giản, thao tác dễ hiểu |
| NFR05 | Quyền riêng tư | Dữ liệu được lưu trên thiết bị |
| NFR06 | Tính ổn định | Timer không tạo phiên hoàn thành trùng lặp |
| NFR07 | Không phụ thuộc mạng | Các chức năng chính hoạt động khi offline |

# 8. Tiêu chí nghiệm thu

| Mã | Kịch bản | Kết quả mong đợi |
| --- | --- | --- |
| TC01 | Tạo task hợp lệ | Task được lưu và hiển thị |
| TC02 | Tạo task không có tiêu đề | Hiển thị thông báo lỗi |
| TC03 | Đánh dấu task hoàn thành | Trạng thái được cập nhật |
| TC04 | Xóa task đang liên kết với timer | Timer vẫn hoạt động an toàn |
| TC05 | Bắt đầu và tạm dừng timer | Timer dừng và tiếp tục đúng |
| TC06 | Reset timer trước khi hết giờ | Không ghi nhận phiên hoàn thành |
| TC07 | Hoàn thành phiên Focus | Lịch sử phiên được lưu một lần |
| TC08 | Đóng rồi mở lại ứng dụng | Dữ liệu task được giữ nguyên |
| TC09 | Mở lại ứng dụng khi timer đang chạy | Timer khôi phục đúng thời gian còn lại |
| TC10 | Sử dụng ứng dụng khi offline | Hai chức năng chính vẫn hoạt động |

# 9. Phạm vi không triển khai

Để giữ dự án đơn giản và không phát sinh yêu cầu backend, phiên bản MVP không bao gồm:

- Đăng nhập, đăng ký, quên mật khẩu.
- Hồ sơ người dùng.
- Đồng bộ dữ liệu giữa nhiều thiết bị.
- Report, dashboard thống kê và biểu đồ năng suất.
- Chia sẻ công việc hoặc cộng tác nhóm.
- API backend hoặc cơ sở dữ liệu đám mây.

# 10. Kế hoạch triển khai

1. Giai đoạn 1 — UI và Navigation: Xây dựng Home, Tasks, Focus và điều hướng bằng Jetpack Compose.
2. Giai đoạn 2 — To-do List: Xây dựng Entity, DAO, Repository và các thao tác CRUD.
3. Giai đoạn 3 — Pomodoro: Triển khai timer, quản lý trạng thái, lưu phiên và xử lý vòng đời ứng dụng.
4. Giai đoạn 4 — Tích hợp: Liên kết task với timer, hoàn thiện thông báo và khôi phục trạng thái.
5. Giai đoạn 5 — Kiểm thử: Kiểm tra nghiệp vụ, dữ liệu cục bộ, timer khi chạy nền và các trường hợp lỗi.