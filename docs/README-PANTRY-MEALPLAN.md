# HƯỚNG DẪN TEST POSTMAN: PHÂN HỆ PANTRY & MEAL PLAN

> **Tài liệu kiểm thử dành riêng cho 2 phân hệ phụ trách:**
> - **Phần 4: Pantry (Quản lý tủ đồ / tủ nguyên liệu cá nhân)** - 9 Endpoints
> - **Phần 7: Meal Plan (Kế hoạch bữa ăn)** - 9 Endpoints  
> - **Tổng cộng**: 18 Endpoints

---

## 1. Thiết lập môi trường & Biến trong Postman

### 1.1. Cấu hình Base URL và Context Path
- Backend Spring Boot cấu hình: `server.servlet.context-path=/api`.
- Biến môi trường Postman đặt: `baseUrl = http://localhost:8080`.
- Do đó, mọi request gửi đến backend đều có dạng chuẩn:
  ```text
  {{baseUrl}}/api/v1/...
  ```
  *(Ví dụ: `{{baseUrl}}/api/v1/pantry-items/01a15eb1-532d-74f1-90b2-f2e12b53aa27`)*

### 1.2. Danh sách biến môi trường (Environment / Collection Variables)

| Tên biến | Giá trị mẫu | Mục đích sử dụng |
| :--- | :--- | :--- |
| `baseUrl` | `http://localhost:8080` | Địa chỉ máy chủ Backend |
| `accessToken` | `eyJhbGciOi...` | JWT Bearer token sau khi đăng nhập |
| `pantryItemId` | `01a15eb1-532d-74f1-90b2-f2e12b53aa27` | UUID của item trong tủ nguyên liệu |
| `mealPlanId` | `01a15ed9-1111-7ca2-b200-f0f184f7b92d` | UUID của thực đơn tuần |
| `mealPlanEntryId` | `01a15eda-2222-7ca2-b200-f0f184f7b92d` | UUID của món ăn trong một ô bữa |

---

## 2. Tiền đề: Đăng nhập lấy Token (Auth)

Toàn bộ các endpoint của **Pantry** và **Meal Plan** đều được phân quyền `ROLE_USER` và tự động cô lập dữ liệu theo người dùng đang đăng nhập (`userIdFromAuthentication()`). Bạn cần có token trước khi gửi request.

### `POST login`
- **Method**: `POST`
- **URL**: `{{baseUrl}}/api/v1/auth/login`
- **Headers**: `Content-Type: application/json`
- **Body (raw JSON)**:
  ```json
  {
    "email": "user@gmail.com",
    "password": "Password123@"
  }
  ```
- **Response**:
  ```json
  {
    "code": "AUTH_200",
    "message": "Login successful",
    "data": {
      "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
      "refreshToken": "4892cfa7-c990-449e-b530-eb34d47b5212"
    }
  }
  ```
> *Lưu `accessToken` vào Postman Variable để header Authorization của thư mục tự động áp dụng `Bearer {{accessToken}}`.*

---

## 3. PHẦN 4: PANTRY (QUẢN LÝ TỦ NGUYÊN LIỆU) - 9 ENDPOINTS

### 3.1. `POST /v1/pantry-items` - Thêm nguyên liệu vào tủ
- **Tên request**: `POST create_pantry`
- **Method**: `POST`
- **URL**: `{{baseUrl}}/api/v1/pantry-items`
- **Headers**:
  - `Content-Type: application/json`
  - `Authorization: Bearer {{accessToken}}`
- **Body (raw JSON)**:
  ```json
  {
    "ingredientId": "01a15ed4-1ee9-7bae-82ef-1436ca387822",
    "quantity": 2.0,
    "unit": "G",
    "location": "FRIDGE",
    "expiryDate": "2026-12-25",
    "photoUri": null
  }
  ```
  - `unit`: `MG`, `G`, `KG`, `ML`, `L`, `PCS`, `TBSP`, `TSP`, `CUP`, `PINCH`
  - `location`: `FRIDGE` (Ngăn mát), `FREEZER` (Ngăn đông), `PANTRY_SHELF` (Kệ bếp)
- **Response (200 OK / Code PANTRY_201)**:
  ```json
  {
    "code": "PANTRY_201",
    "message": "Pantry item created successfully",
    "data": {
      "id": "01a15eb1-532d-74f1-90b2-f2e12b53aa27",
      "ingredientId": "01a15ed4-1ee9-7bae-82ef-1436ca387822",
      "ingredientName": "Thịt bò",
      "categoryId": "01a15ed4-1edd-71f4-8815-9b59b623cb92",
      "categoryName": "Thịt & Trứng",
      "quantity": 2.0,
      "unit": "G",
      "location": "FRIDGE",
      "expiryDate": "2026-12-25",
      "photoUri": null,
      "createdAt": "2026-10-22T13:30:00Z",
      "updatedAt": "2026-10-22T13:30:00Z"
    }
  }
  ```

---

### 3.2. `POST /v1/pantry-items/batch` - Thêm hàng loạt (sau quét AI)
- **Tên request**: `POST batch_create_pantry`
- **Method**: `POST`
- **URL**: `{{baseUrl}}/api/v1/pantry-items/batch`
- **Headers**:
  - `Content-Type: application/json`
  - `Authorization: Bearer {{accessToken}}`
- **Body (raw JSON)**:
  ```json
  {
    "items": [
      {
        "ingredientId": "01a15ed4-1ee9-7bae-82ef-1436ca387822",
        "quantity": 500.0,
        "unit": "G",
        "location": "FREEZER",
        "expiryDate": "2026-11-30",
        "photoUri": null
      },
      {
        "ingredientId": "01a15ed4-1ee9-7bae-82ef-1436ca387823",
        "quantity": 1.0,
        "unit": "L",
        "location": "FRIDGE",
        "expiryDate": "2026-10-28",
        "photoUri": null
      }
    ]
  }
  ```
- **Response**: Trả về `List<PantryItemResponse>`.

---

### 3.3. `GET /v1/pantry-items` - Danh sách, phân trang con trỏ, lọc & tìm kiếm
- **Tên request**: `GET getPantryCursor`
- **Method**: `GET`
- **URL**: `{{baseUrl}}/api/v1/pantry-items`
- **Query Parameters**:
  - `cursor`: UUID của item cuối trang trước (để load trang tiếp theo)
  - `size`: Số phần tử/trang (mặc định: `20`, tối đa: `50`)
  - `categoryId`: Lọc theo danh mục nguyên liệu (tùy chọn)
  - `location`: Lọc theo vị trí lưu trữ: `FRIDGE` | `FREEZER` | `PANTRY_SHELF` (tùy chọn)
  - `keyword`: Tìm kiếm tương đối theo tên nguyên liệu (tùy chọn)
- **Ví dụ gọi**:
  - Lấy trang 1: `{{baseUrl}}/api/v1/pantry-items?size=20`
  - Lọc theo tủ lạnh và tìm kiếm: `{{baseUrl}}/api/v1/pantry-items?size=10&location=FRIDGE&keyword=bò`
  - Lấy trang sau với cursor: `{{baseUrl}}/api/v1/pantry-items?cursor=01a15eb1-532d-74f1-90b2-f2e12b53aa27&size=20`
- **Response**:
  ```json
  {
    "code": "PANTRY_200",
    "message": "Pantry items fetched successfully",
    "data": {
      "data": [
        {
          "id": "01a15eb1-532d-74f1-90b2-f2e12b53aa27",
          "ingredientName": "Thịt bò",
          "quantity": 2.0,
          "unit": "G",
          "location": "FRIDGE"
        }
      ],
      "nextCursor": "01a15eb1-532d-74f1-90b2-f2e12b53aa27",
      "hasNext": false
    }
  }
  ```

---

### 3.4. `GET /v1/pantry-items/{id}` - Chi tiết nguyên liệu trong tủ
- **Tên request**: `GET GetById`
- **Method**: `GET`
- **URL**: `{{baseUrl}}/api/v1/pantry-items/01a15eb1-532d-74f1-90b2-f2e12b53aa27`
- **Headers**:
  - `Authorization: Bearer {{accessToken}}`
- **Response**: Trả về chi tiết `PantryItemResponse` với đầy đủ thông tin: ID, nguyên liệu, vị trí, hạn sử dụng, số lượng, ngày tạo/cập nhật.

---

### 3.5. `PUT /v1/pantry-items/{id}` - Cập nhật nguyên liệu
- **Tên request**: `PUT update_pantry`
- **Method**: `PUT`
- **URL**: `{{baseUrl}}/api/v1/pantry-items/01a15eb1-532d-74f1-90b2-f2e12b53aa27`
- **Headers**:
  - `Content-Type: application/json`
  - `Authorization: Bearer {{accessToken}}`
- **Body (raw JSON)**:
  ```json
  {
    "quantity": 3.0,
    "unit": "G",
    "location": "FREEZER",
    "expiryDate": "2026-12-30",
    "photoUri": null
  }
  ```
- **Response**: `PantryItemResponse` đã cập nhật.

---

### 3.6. `DELETE /v1/pantry-items/{id}` - Xóa mềm nguyên liệu
- **Tên request**: `DELETE delete_pantry`
- **Method**: `DELETE`
- **URL**: `{{baseUrl}}/api/v1/pantry-items/01a15eb1-532d-74f1-90b2-f2e12b53aa27`
- **Headers**:
  - `Authorization: Bearer {{accessToken}}`
- **Response**:
  ```json
  {
    "code": "PANTRY_200",
    "message": "Pantry item deleted successfully",
    "data": null
  }
  ```
  *(Ghi chú: Bản ghi được gắn `deleted_at = now()`, không xóa vĩnh viễn khỏi DB).*

---

### 3.7. `GET /v1/pantry-items/expiring?days=3` - Danh sách sắp hết hạn
- **Tên request**: `GET getExpiringItems`
- **Method**: `GET`
- **URL**: `{{baseUrl}}/api/v1/pantry-items/expiring?days=3`
- **Query Parameters**:
  - `days`: Ngưỡng số ngày sắp hết hạn tính từ hôm nay (mặc định: `3`)
- **Headers**:
  - `Authorization: Bearer {{accessToken}}`
- **Response**: `List<PantryItemResponse>` gồm các nguyên liệu có hạn sử dụng `<= today + days`.

---

### 3.8. `POST /v1/pantry-items/{id}/photo` - Upload ảnh thực tế
- **Tên request**: `POST uploadPhoto`
- **Method**: `POST`
- **URL**: `{{baseUrl}}/api/v1/pantry-items/01a15eb1-532d-74f1-90b2-f2e12b53aa27/photo`
- **Headers**:
  - `Authorization: Bearer {{accessToken}}`
- **Body**: Chọn `form-data`
  - Key: `image` (chuyển loại từ Text sang **File**)
  - Value: Chọn 1 file ảnh (.png, .jpg, .jpeg) từ máy tính
- **Response**: Trả về `PantryItemResponse` với URL ảnh mới được lưu trữ trên Cloudinary.

---

### 3.9. `POST /v1/pantry-items/{id}/consume` - Trừ bớt số lượng khi dùng nấu
- **Tên request**: `POST consume_pantry`
- **Method**: `POST`
- **URL**: `{{baseUrl}}/api/v1/pantry-items/01a15eb1-532d-74f1-90b2-f2e12b53aa27/consume`
- **Headers**:
  - `Content-Type: application/json`
  - `Authorization: Bearer {{accessToken}}`
- **Body (raw JSON)**:
  ```json
  {
    "amount": 0.5
  }
  ```
- **Response**:
  ```json
  {
    "code": "PANTRY_200",
    "message": "Pantry item consumed successfully",
    "data": {
      "id": "01a15eb1-532d-74f1-90b2-f2e12b53aa27",
      "quantity": 1.5,
      "unit": "G",
      "location": "FRIDGE"
    }
  }
  ```
  *(Lưu ý: Nếu `amount > quantity` trong tủ, hệ thống trả về HTTP 400 cùng mã lỗi `PANTRY_400: Insufficient quantity in pantry item`).*

---

## 4. PHẦN 7: MEAL PLAN (KẾ HOẠCH BỮA ĂN) - 9 ENDPOINTS

### 4.1. `GET /v1/meal-plans?weekStart=` - Lấy thực đơn của tuần
- **Tên request**: `GET getMealPlanByWeekStart`
- **Method**: `GET`
- **URL**: `{{baseUrl}}/api/v1/meal-plans?weekStart=2026-10-19`
- **Query Parameters**:
  - `weekStart`: Ngày bắt đầu tuần theo định dạng `YYYY-MM-DD` (ví dụ: ngày Thứ Hai `2026-10-19`)
- **Headers**:
  - `Authorization: Bearer {{accessToken}}`
- **Response**: `MealPlanResponse` chứa mảng `entries` các bữa ăn trong tuần (hoặc `data: null` nếu tuần đó chưa tạo).

---

### 4.2. `GET /v1/meal-plans/{id}` - Chi tiết thực đơn theo ID
- **Tên request**: `GET getMealPlanById`
- **Method**: `GET`
- **URL**: `{{baseUrl}}/api/v1/meal-plans/01a15ed9-1111-7ca2-b200-f0f184f7b92d`
- **Headers**:
  - `Authorization: Bearer {{accessToken}}`
- **Response**: Trả về `MealPlanResponse` chi tiết của thực đơn tuần thuộc user hiện tại.

---

### 4.3. `POST /v1/meal-plans` - Tạo thực đơn tuần
- **Tên request**: `POST create_meal_plan`
- **Method**: `POST`
- **URL**: `{{baseUrl}}/api/v1/meal-plans`
- **Headers**:
  - `Content-Type: application/json`
  - `Authorization: Bearer {{accessToken}}`
- **Body (raw JSON)**:
  ```json
  {
    "weekStart": "2026-10-19",
    "entries": [
      {
        "date": "2026-10-19",
        "mealType": "BREAKFAST",
        "recipeId": "01a15ec9-1234-7000-8000-000000000001",
        "servings": 2
      },
      {
        "date": "2026-10-19",
        "mealType": "DINNER",
        "recipeId": "01a15ec9-1234-7000-8000-000000000002",
        "servings": 4
      }
    ]
  }
  ```
  *(Trường `entries` có thể để rỗng `[]` để tạo khung thực đơn rỗng cho tuần trước).*
- **Response (200 OK / Code MEALPLAN_201)**:
  ```json
  {
    "code": "MEALPLAN_201",
    "message": "Meal plan created successfully",
    "data": {
      "id": "01a15ed9-1111-7ca2-b200-f0f184f7b92d",
      "weekStart": "2026-10-19",
      "entries": [ ... ]
    }
  }
  ```

---

### 4.4. `DELETE /v1/meal-plans/{id}` - Xóa mềm thực đơn
- **Tên request**: `DELETE delete_meal_plan`
- **Method**: `DELETE`
- **URL**: `{{baseUrl}}/api/v1/meal-plans/01a15ed9-1111-7ca2-b200-f0f184f7b92d`
- **Headers**:
  - `Authorization: Bearer {{accessToken}}`
- **Response**:
  ```json
  {
    "code": "MEALPLAN_200",
    "message": "Meal plan deleted successfully",
    "data": null
  }
  ```

---

### 4.5. `POST /v1/meal-plans/{id}/entries` - Thêm món vào một ô bữa ăn
- **Tên request**: `POST add_meal_plan_entry`
- **Method**: `POST`
- **URL**: `{{baseUrl}}/api/v1/meal-plans/01a15ed9-1111-7ca2-b200-f0f184f7b92d/entries`
- **Headers**:
  - `Content-Type: application/json`
  - `Authorization: Bearer {{accessToken}}`
- **Body (raw JSON)**:
  ```json
  {
    "date": "2026-10-20",
    "mealType": "LUNCH",
    "recipeId": "01a15ec9-1234-7000-8000-000000000003",
    "servings": 2
  }
  ```
  - `mealType`: `BREAKFAST` (Sáng), `LUNCH` (Trưa), `DINNER` (Tối), `SNACK` (Ăn nhẹ)
- **Response**: Trả về `MealPlanEntryResponse` đã tạo.

---

### 4.6. `PATCH /v1/meal-plans/{id}/entries/{entryId}` - Sửa ô bữa (ngày, bữa, khẩu phần, đã nấu)
- **Tên request**: `PATCH update_meal_plan_entry`
- **Method**: `PATCH`
- **URL**: `{{baseUrl}}/api/v1/meal-plans/01a15ed9-1111-7ca2-b200-f0f184f7b92d/entries/01a15eda-2222-7ca2-b200-f0f184f7b92d`
- **Headers**:
  - `Content-Type: application/json`
  - `Authorization: Bearer {{accessToken}}`
- **Body (raw JSON)**:
  ```json
  {
    "date": "2026-10-21",
    "mealType": "DINNER",
    "servings": 3,
    "cooked": true
  }
  ```
- **Response**: Trả về `MealPlanEntryResponse` sau khi cập nhật.

---

### 4.7. `DELETE /v1/meal-plans/{id}/entries/{entryId}` - Xóa món khỏi ô bữa
- **Tên request**: `DELETE delete_meal_plan_entry`
- **Method**: `DELETE`
- **URL**: `{{baseUrl}}/api/v1/meal-plans/01a15ed9-1111-7ca2-b200-f0f184f7b92d/entries/01a15eda-2222-7ca2-b200-f0f184f7b92d`
- **Headers**:
  - `Authorization: Bearer {{accessToken}}`
- **Response**:
  ```json
  {
    "code": "MEALPLAN_200",
    "message": "Meal plan entry deleted successfully",
    "data": null
  }
  ```

---

### 4.8. `POST /v1/meal-plans/{id}/copy?toWeekStart=` - Sao chép thực đơn sang tuần khác
- **Tên request**: `POST copy_meal_plan`
- **Method**: `POST`
- **URL**: `{{baseUrl}}/api/v1/meal-plans/01a15ed9-1111-7ca2-b200-f0f184f7b92d/copy?toWeekStart=2026-10-26`
- **Query Parameters**:
  - `toWeekStart`: Ngày bắt đầu tuần mới muốn sao chép đến (ví dụ: `2026-10-26`)
- **Headers**:
  - `Authorization: Bearer {{accessToken}}`
- **Nghiệp vụ tự động**: Hệ thống sao chép toàn bộ các món trong tuần cũ và tự động cộng thêm số ngày độ lệch (`daysDiff = toWeekStart - fromWeekStart`), reset trạng thái `cooked = false`.
- **Response**: `MealPlanResponse` của tuần mới.

---

### 4.9. `GET /v1/meal-plans/{id}/shopping-list` - Xem trước danh sách mua sắm
- **Tên request**: `GET getMealPlanShoppingList`
- **Method**: `GET`
- **URL**: `{{baseUrl}}/api/v1/meal-plans/01a15ed9-1111-7ca2-b200-f0f184f7b92d/shopping-list`
- **Headers**:
  - `Authorization: Bearer {{accessToken}}`
- **Nghiệp vụ liên kết thông minh giữa Pantry và Meal Plan**:
  1. Backend tổng hợp toàn bộ nguyên liệu cần dùng từ các Recipe trong Meal Plan của tuần (tính theo tỷ lệ khẩu phần `servings`).
  2. Lấy danh sách nguyên liệu hiện có trong **Tủ đồ (Pantry)** của người dùng đó.
  3. Lấy: `Lượng cần mua = Lượng món cần - Lượng đang có trong tủ`.
  4. Nếu sau khi trừ mà lượng cần mua `> 0`, mục đó sẽ xuất hiện trong danh sách cần mua.
- **Response**:
  ```json
  {
    "code": "MEALPLAN_200",
    "message": "Shopping list preview fetched successfully",
    "data": [
      {
        "id": null,
        "name": "Thịt ba chỉ",
        "quantity": 300.0,
        "unit": "G",
        "status": "PENDING"
      },
      {
        "id": null,
        "name": "Trứng gà",
        "quantity": 4.0,
        "unit": "PCS",
        "status": "PENDING"
      }
    ]
  }
  ```

---

## 5. Bảng mã phản hồi & Mã lỗi thường gặp (Error Codes)

### Phân hệ Pantry
| HTTP Status | Code trả về | Mô tả | Xử lý |
| :---: | :--- | :--- | :--- |
| **201** | `PANTRY_201` | Tạo thành công pantry item hoặc batch items | Hoàn tất |
| **200** | `PANTRY_200` | Lấy/Sửa/Xóa/Consume/Upload ảnh thành công | Hoàn tất |
| **404** | `PANTRY_404` | `Pantry item not found` | Kiểm tra lại `id` của pantry item hoặc item thuộc user khác |
| **400** | `PANTRY_400` | `Insufficient quantity in pantry item` | Số lượng trừ `amount` lớn hơn `quantity` đang có trong tủ |
| **400** | `PANTRY_400_1` | `Image file is empty` | File upload ảnh không được để trống |

### Phân hệ Meal Plan
| HTTP Status | Code trả về | Mô tả | Xử lý |
| :---: | :--- | :--- | :--- |
| **201** | `MEALPLAN_201` | Tạo hoặc sao chép meal plan / thêm entry thành công | Hoàn tất |
| **200** | `MEALPLAN_200` | Lấy danh sách/Chi tiết/Sửa/Xóa/Shopping list thành công | Hoàn tất |
| **404** | `MEALPLAN_404` | `Meal plan not found` | Kiểm tra lại `id` của Meal Plan |
| **404** | `MEALPLAN_ENTRY_404` | `Meal plan entry not found` | Kiểm tra lại `entryId` trong meal plan |
| **409** | `MEALPLAN_409` | `Meal plan for this week already exists` | Tuần đó đã được tạo thực đơn, không thể tạo trùng `weekStart` |

### Mã chung (Auth & Validation)
| HTTP Status | Code trả về | Mô tả | Xử lý |
| :---: | :--- | :--- | :--- |
| **401** | `AUTH_401` | Token thiếu hoặc hết hạn | Đăng nhập lại qua `POST /api/v1/auth/login` |
| **403** | `AUTH_403` | Không đủ quyền | Đảm bảo tài khoản có `ROLE_USER` |
| **400** | `VALIDATION_FAILED` | Sai định dạng dữ liệu (ngày, số âm, trường bắt buộc null) | Kiểm tra body request |

---

## 6. Hướng dẫn Test nhanh trên Postman

1. Mở Postman, nhấn **Import** $\rightarrow$ Chọn file: [docs/pantry-chef.postman_collection.json](file:///e:/backup/pantry-chef/docs/pantry-chef.postman_collection.json).
2. Collection đã có sẵn biến `baseUrl = http://localhost:8080`.
3. Chạy request trong folder **Auth** $\rightarrow$ `POST login`. Script sẽ tự động bắt `accessToken` gán vào biến dùng chung.
4. Chạy các request trong folder **Pantry** và **MealPlan**. Các request đã thiết lập sẵn kế thừa Authorization Bearer Token.
