# TÀI LIỆU BÁO CÁO NGHIỆP VỤ: PHÂN HỆ PANTRY & MEAL PLAN
**Dự án**: Pantry Chef  
**Người phụ trách**: Thành viên nhóm phụ trách phân hệ Pantry (Tủ đồ) & Meal Plan (Kế hoạch bữa ăn)  
**Khối lượng**: 18 Endpoints (9 Pantry + 9 Meal Plan)

---

## 1. Bức tranh tổng quan: Pantry và Meal Plan giải quyết bài toán gì?

Trong cuộc sống bận rộn, người nội trợ và các gia đình thường đối mặt với **3 nỗi đau (Pain Points)** lớn:
1. **Lãng phí thực phẩm**: Mua thực phẩm về nhưng quên mất trong tủ lạnh, đến khi nhớ ra thì đã hết hạn hoặc hư hỏng. Hoặc đi chợ mua trùng món ở nhà vẫn còn.
2. **Áp lực "Hôm nay ăn gì?"**: Mỗi ngày mất 15-30 phút suy nghĩ thực đơn cho các bữa ăn, dẫn đến việc ăn uống lặp lại, mất cân đối dinh dưỡng.
3. **Đi chợ không có kế hoạch**: Mua thừa những thứ không cần, nhưng lại quên mua những gia vị, nguyên liệu cốt lõi để nấu món muốn ăn.

👉 **Hai phân hệ Pantry & Meal Plan chính là "Trái tim" giải quyết trọn vẹn vòng đời thực phẩm của ứng dụng Pantry Chef**:
- **Pantry (Tủ đồ)**: Giúp người dùng **biết rõ mình đang CÓ GÌ** trong nhà (ngăn mát, ngăn đông, kệ bếp) và **khi nào hết hạn**.
- **Meal Plan (Kế hoạch bữa ăn)**: Giúp người dùng **chủ động định hình mình SẼ ĂN GÌ** trong suốt tuần.
- **Sự kết hợp giữa Pantry và Meal Plan**: Hệ thống tự động so khớp thực đơn tuần với tủ đồ để chỉ ra người dùng **CẦN MUA THÊM GÌ**, và khi nấu xong thì tự động trừ kho nguyên liệu.

---

## 2. Phần 4: PANTRY (Quản lý tủ đồ cá nhân) có nghĩa là gì?

### 2.1. Bản chất nghiệp vụ
- **Pantry** trong tiếng Anh nghĩa là "tủ chứa thực phẩm / phòng để thức ăn".
- Trong hệ thống, `PantryItem` đại diện cho **1 món nguyên liệu cụ thể mà người dùng hiện đang lưu trữ** trong nhà.

> **💡 Điểm cốt lõi cần nhớ khi báo cáo: Phân biệt `Ingredient` và `PantryItem`**:
> - `Ingredient` là **danh mục dùng chung** của toàn hệ thống (Master Data): Ví dụ "Thịt bò", "Cà chua", "Nước mắm" (chỉ có tên, loại thực phẩm, đơn vị mặc định).
> - `PantryItem` là **tài sản cụ thể của người dùng**: Ví dụ người dùng A đang có "500 gram Thịt bò" cất ở "Ngăn đông (FREEZER)", hạn dùng "30/11/2026", kèm "ảnh chụp gói thịt".

### 2.2. Các tính năng chính bạn phụ trách (9 Endpoints)
1. **Thêm nguyên liệu lẻ (`POST /v1/pantry-items`)**: Người dùng tự nhập thực phẩm mới mua vào tủ.
2. **Thêm hàng loạt (`POST /v1/pantry-items/batch`)**: Nhập nhiều nguyên liệu cùng lúc (tính năng này kết nối trực tiếp với kết quả nhận diện hóa đơn hoặc ảnh chụp từ module AI).
3. **Danh sách & Lọc phân trang con trỏ (`GET /v1/pantry-items`)**:
   - Phân loại vị trí rõ ràng qua enum `StorageLocation`: `FRIDGE` (Tủ lạnh - ngăn mát), `FREEZER` (Ngăn đông), `PANTRY_SHELF` (Kệ bếp/tủ khô).
   - Lọc theo danh mục thực phẩm (`categoryId`) và tìm kiếm tương đối (`keyword`).
   - Sử dụng **Cursor Pagination** thay vì Offset để tăng tốc độ truy vấn cơ sở dữ liệu khi số lượng món đồ lớn.
4. **Chi tiết nguyên liệu (`GET /v1/pantry-items/{id}`)**: Xem đầy đủ trạng thái của một nguyên liệu.
5. **Cập nhật nguyên liệu (`PUT /v1/pantry-items/{id}`)**: Đổi số lượng, hạn sử dụng, chuyển vị trí (ví dụ dọn từ ngăn đông xuống ngăn mát để rã đông).
6. **Xóa mềm (`DELETE /v1/pantry-items/{id}`)**: Ẩn nguyên liệu khỏi danh sách hiển thị nhưng vẫn lưu dấu vết (`deleted_at`) để phục vụ thống kê hoặc khôi phục.
7. **Cảnh báo sắp hết hạn (`GET /v1/pantry-items/expiring?days=3`)**:
   - Tự động lọc ra những nguyên liệu có `expiryDate <= hôm nay + N ngày`.
   - Giúp ứng dụng đẩy thông báo nhắc người dùng ưu tiên nấu các món này trước để không bị bỏ phí.
8. **Upload ảnh thực tế (`POST /v1/pantry-items/{id}/photo`)**: Chụp ảnh gói đồ ăn và upload lưu trữ trực tiếp trên dịch vụ Cloudinary.
9. **Trừ kho khi nấu (`POST /v1/pantry-items/{id}/consume`)**: Khi người dùng lấy 200g thịt ra nấu, gọi API này để trừ bớt số lượng trong tủ. Hệ thống có kiểm tra chặn nếu số lượng trừ vượt quá lượng tồn (`InsufficientQuantityException`).

---

## 3. Phần 7: MEAL PLAN (Kế hoạch bữa ăn) có nghĩa là gì?

### 3.1. Bản chất nghiệp vụ
- **Meal Plan** là kế hoạch dinh dưỡng được sắp xếp theo từng tuần (`weekStart`).
- Một gia đình thường lên thực đơn trước cho cả tuần (từ Thứ Hai đến Chủ Nhật).
- Cấu trúc dữ liệu:
  - `MealPlan` (Bao bì tuần): Quản lý theo `weekStart` (mỗi user chỉ có 1 meal plan duy nhất cho 1 tuần, tránh trùng lặp).
  - `MealPlanEntry` (Từng ô bữa ăn): Mỗi ô xác định cụ thể:
    - Ngày nào trong tuần (`date`).
    - Bữa nào (`mealType`): `BREAKFAST` (Sáng), `LUNCH` (Trưa), `DINNER` (Tối), `SNACK` (Ăn nhẹ).
    - Món gì (`Recipe`): Liên kết đến công thức nấu ăn.
    - Số khẩu phần (`servings`): Ví dụ 2 người ăn hay 4 người ăn.
    - Trạng thái (`cooked`): Đã nấu món này chưa.

### 3.2. Các tính năng chính bạn phụ trách (9 Endpoints)
1. **Lấy thực đơn tuần (`GET /v1/meal-plans?weekStart=`)**: Hiển thị bảng lịch ăn uống 7 ngày trong tuần.
2. **Chi tiết thực đơn (`GET /v1/meal-plans/{id}`)**: Lấy thông tin tuần kèm tất cả các ô bữa ăn.
3. **Tạo thực đơn tuần (`POST /v1/meal-plans`)**: Tạo tuần mới (có thể tạo khung rỗng hoặc kèm sẵn các món).
4. **Xóa thực đơn (`DELETE /v1/meal-plans/{id}`)**: Xóa toàn bộ kế hoạch tuần (xóa mềm).
5. **Thêm món vào một ô (`POST /v1/meal-plans/{id}/entries`)**: Gán một công thức vào bữa trưa thứ Ba hoặc bữa tối thứ Bảy.
6. **Cập nhật ô bữa ăn (`PATCH /v1/meal-plans/{id}/entries/{entryId}`)**:
   - Dời lịch sang ngày khác hoặc bữa khác.
   - Tăng/giảm số khẩu phần (`servings`).
   - Đánh dấu đã nấu (`cooked: true`).
7. **Xóa món khỏi ô (`DELETE /v1/meal-plans/{id}/entries/{entryId}`)**: Bỏ món khỏi lịch ăn.
8. **Sao chép sang tuần khác (`POST /v1/meal-plans/{id}/copy?toWeekStart=`)**:
   - Tính năng cực kỳ tiện lợi: Nếu tuần tới gia đình muốn lặp lại thực đơn tuần này, chỉ cần bấm Copy.
   - Backend tự động tính độ lệch số ngày (`ChronoUnit.DAYS.between`) và tịnh tiến ngày của toàn bộ các món sang tuần mới, đồng thời reset trạng thái `cooked = false`.
9. **XEM TRƯỚC DANH SÁCH ĐI CHỢ (`GET /v1/meal-plans/{id}/shopping-list`) - ĐIỂM SÁNG GIÁ NHẤT**:
   - Thuật toán thông minh liên kết giữa 2 phân hệ:
     1. Quét tất cả các món ăn đã lên lịch trong tuần.
     2. Nhân lượng nguyên liệu của từng món theo tỷ lệ khẩu phần (`entryServings / recipeServings`).
     3. Cộng dồn tổng lượng từng nguyên liệu cần dùng trong cả tuần.
     4. Truy vấn vào **Tủ đồ (Pantry)** của người dùng để lấy lượng tồn kho hiện có.
     5. Tính: `Số lượng cần mua = Lượng cần dùng - Lượng đang có sẵn trong tủ`.
     6. Chỉ trả về những nguyên liệu thực sự còn thiếu (`> 0`).

---

## 4. Chu trình khép kín giữa 2 phân hệ trong ứng dụng

```
┌────────────────────────────────────────────────────────┐
│ 1. NHẬP KHO VÀO TỦ (Pantry)                            │
│    - Quét AI hoặc nhập tay thực phẩm đang có           │
│    - Theo dõi vị trí và cảnh báo hết hạn               │
└──────────────────────────┬─────────────────────────────┘
                           │
                           ▼
┌────────────────────────────────────────────────────────┐
│ 2. LÊN THỰC ĐƠN TUẦN (Meal Plan)                       │
│    - Chọn món cho các bữa Sáng / Trưa / Tối            │
│    - Hệ thống gợi ý món dựa trên đồ đang có trong tủ   │
└──────────────────────────┬─────────────────────────────┘
                           │
                           ▼
┌────────────────────────────────────────────────────────┐
│ 3. SINH DANH SÁCH MUA SẮM (Shopping List Preview)     │
│    - [Tổng nguyên liệu cần] trừ [Đồ sẵn có trong tủ]   │
│    - Tạo danh sách đi chợ chính xác không thừa/thiếu   │
└──────────────────────────┬─────────────────────────────┘
                           │
                           ▼
┌────────────────────────────────────────────────────────┐
│ 4. NẤU ĂN VÀ TRỪ KHO (Consume & Cooked)                │
│    - Nấu xong đánh dấu bữa ăn: cooked = true           │
│    - Gọi POST /consume để tự động trừ kho Pantry       │
└────────────────────────────────────────────────────────┘
```

---

## 5. Các điểm nhấn kỹ thuật dùng để ghi điểm khi báo cáo

1. **Phân quyền và bảo mật đa người dùng (Multi-tenant Data Isolation)**:
   - Sử dụng Spring Security `@PreAuthorize("hasRole('USER')")`.
   - Mọi câu lệnh truy vấn đều lọc qua `userIdFromAuthentication()`, đảm bảo user A tuyệt đối không xem hoặc sửa được tủ đồ / thực đơn của user B.
2. **Kỹ thuật Xóa mềm (Soft Delete)**:
   - Áp dụng qua `@SQLDelete(sql = "UPDATE ... SET deleted_at = now() WHERE id = ?")` kết hợp `@SQLRestriction("deleted_at IS NULL")`.
   - Giữ lại tính toàn vẹn dữ liệu cho các báo cáo thống kê lịch sử ăn uống.
3. **Tối ưu hóa hiệu năng với Cursor-based Pagination**:
   - Áp dụng cho danh sách Pantry items.
   - Thay vì dùng `OFFSET` (chậm dần khi dữ liệu lớn), hệ thống dùng ID con trỏ (`cursor`) kết hợp index để truy vấn trang tiếp theo với độ phức tạp $O(1)$.
4. **Xử lý giao dịch (@Transactional)**:
   - Đảm bảo tính toàn vẹn (ACID) khi thêm hàng loạt (Batch Create), sao chép thực đơn tuần (Copy Meal Plan) hoặc cập nhật kho (Consume).
5. **Lưu trữ đám mây cho hình ảnh**:
   - Tích hợp Cloudinary qua `MediaStorageService` để tải và quản lý ảnh chụp thực phẩm thực tế.

---


