# TÀI LIỆU HƯỚNG DẪN TEST API (POSTMAN / BRUNO) - PANTRY CHEF

Tài liệu này hướng dẫn chi tiết cách thiết lập, cấu hình môi trường và thực hiện kiểm thử (test) các API của hệ thống **Pantry Chef**, tập trung vào 2 phân hệ cốt lõi: **4. Pantry (Quản lý tủ đồ)** và **7. Meal Plan (Kế hoạch bữa ăn)** cùng với luồng xác thực (**Auth**).

---

## 1. Môi trường & Thiết lập (Environment Setup)

### 1.1. Biến môi trường (Environment Variables)

Thiết lập các biến môi trường trong Postman hoặc Bruno:

| Tên biến | Giá trị mẫu | Mô tả |
| :--- | :--- | :--- |
| `baseUrl` | `http://localhost:8080` | Địa chỉ máy chủ backend |
| `accessToken` | *(Token tự động lưu sau khi login)* | Chuỗi JWT Bearer token |
| `pantryItemId` | `01a15ed8-5086-7ca2-b200-f0f184f7b92d` | UUID của nguyên liệu trong tủ |
| `mealPlanId` | `01a15ed9-1111-7ca2-b200-f0f184f7b92d` | UUID của kế hoạch bữa ăn |
| `mealPlanEntryId` | `01a15eda-2222-7ca2-b200-f0f184f7b92d` | UUID của một món ăn trong bữa |

> [!NOTE]  
> - Đường dẫn gốc của hệ thống có servlet context-path là `/api`.
> - Do đó, toàn bộ request có tiền tố: `{{baseUrl}}/api/v1/...`

### 1.2. Quy ước phân quyền (Role Matrix)

| Ký hiệu | Tên Quyền | Phân quyền Spring Security | Ghi chú |
| :---: | :--- | :--- | :--- |
| **P** | Public (Công khai) | Không cần token | Các API đăng ký, đăng nhập, quên mật khẩu |
| **U** | `ROLE_USER` | `@PreAuthorize("hasRole('USER')")` | Người dùng đã đăng nhập (toàn bộ Pantry & Meal Plan) |
| **A** | `ROLE_ADMIN` | `@PreAuthorize("hasRole('ADMIN')")` | Quản trị viên hệ thống |

---

## 2. Bước khởi tạo: Đăng nhập lấy Token (Auth)

Trước khi gọi các API thuộc quyền **`U` (ROLE_USER)**, bạn cần có `accessToken`.

### 2.1. Request: `POST login`
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
> Copy giá trị `accessToken` gán vào biến môi trường `{{accessToken}}` hoặc cấu hình **Inherit Auth from Parent** là `Bearer {{accessToken}}`.

---

## 3. PHẦN 4: PANTRY (QUẢN LÝ TỦ ĐỒ)

Tất cả các endpoint trong phân hệ Pantry thao tác trên dữ liệu tủ cá nhân của user hiện tại (`ROLE_USER`).

### Danh mục Request trong Collection:

```text
pantry-chef/
├── Pantry/
│   ├── POST create_pantry
│   ├── POST batch_create_pantry
│   ├── GET getPantryCursor
│   ├── GET GetById
│   ├── PUT update_pantry
│   ├── DELETE delete_pantry
│   ├── GET getExpiringItems
│   ├── POST uploadPhoto
│   └── POST consume_pantry
```

---

### 3.1. `POST create_pantry` - Thêm nguyên liệu vào tủ
- **Method**: `POST`
- **URL**: `{{baseUrl}}/api/v1/pantry-items`
- **Headers**:
  - `Content-Type: application/json`
  - `Authorization: Bearer {{accessToken}}`
- **Body**:
```json
{
  "ingredientId": "01a15ed4-1ee9-7bae-82ef-1436ca387822",
  "quantity": 2.0,
  "unit": "G",
  "location": "FRIDGE",
  "expiryDate": "2026-12-25",
  "photoUri": "https://res.cloudinary.com/demo/image/upload/sample.jpg"
}
```
*(Ghi chú: `photoUri` là tùy chọn, có thể truyền URL ảnh hoặc `null`)*
- **Response (200 OK)**:
```json
{
  "code": "PANTRY_201",
  "message": "Pantry item created successfully",
  "data": {
    "id": "01a15ed8-5086-7ca2-b200-f0f184f7b92d",
    "ingredientId": "01a15ed4-1ee9-7bae-82ef-1436ca387822",
    "ingredientName": "Thịt bò",
    "categoryId": "01a15ed4-1edd-71f4-8815-9b59b623cb92",
    "categoryName": "Thịt & Trứng",
    "quantity": 2.0,
    "unit": "G",
    "location": "FRIDGE",
    "expiryDate": "2026-12-25",
    "photoUri": "https://res.cloudinary.com/demo/image/upload/sample.jpg",
    "createdAt": "2026-10-21T14:35:10.123456Z",
    "updatedAt": "2026-10-21T14:35:10.123456Z"
  }
}
```

---

### 3.2. `POST batch_create_pantry` - Thêm hàng loạt (xác nhận kết quả quét AI)
- **Method**: `POST`
- **URL**: `{{baseUrl}}/api/v1/pantry-items/batch`
- **Headers**:
  - `Content-Type: application/json`
  - `Authorization: Bearer {{accessToken}}`
- **Body**:
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

### 3.3. `GET getPantryCursor` - Danh sách, phân trang, lọc và tìm kiếm
- **Method**: `GET`
- **URL**: `{{baseUrl}}/api/v1/pantry-items`
- **Query Parameters**:
  - `cursor`: UUID phần tử cuối cùng trang trước (để lấy trang sau)
  - `size`: Số phần tử/trang (mặc định: `20`, tối đa: `50`)
  - `categoryId`: UUID danh mục (tùy chọn)
  - `location`: `FRIDGE` | `FREEZER` | `PANTRY_SHELF` (tùy chọn)
  - `keyword`: Tìm kiếm tên nguyên liệu (tùy chọn)
- **Ví dụ gọi**:
  - Lấy trang đầu: `{{baseUrl}}/api/v1/pantry-items?size=10`
  - Lọc theo tủ lạnh và tìm kiếm: `{{baseUrl}}/api/v1/pantry-items?size=10&location=FRIDGE&keyword=bò`
  - Lấy trang tiếp theo: `{{baseUrl}}/api/v1/pantry-items?cursor=01a15ed8-5086-7ca2-b200-f0f184f7b92d&size=10`
- **Response**:
```json
{
  "code": "PANTRY_200",
  "message": "Pantry items fetched successfully",
  "data": {
    "data": [
      {
        "id": "01a15ed8-5086-7ca2-b200-f0f184f7b92d",
        "ingredientName": "Thịt bò",
        "quantity": 2.0,
        "unit": "G",
        "location": "FRIDGE"
      }
    ],
    "nextCursor": "01a15ed8-5086-7ca2-b200-f0f184f7b92d",
    "hasNext": false
  }
}
```

---

### 3.4. `GET GetById` - Chi tiết nguyên liệu trong tủ
- **Method**: `GET`
- **URL**: `{{baseUrl}}/api/v1/pantry-items/{{pantryItemId}}`
- **Response**: Trả về chi tiết `PantryItemResponse`.

---

### 3.5. `PUT update_pantry` - Chỉnh sửa nguyên liệu
- **Method**: `PUT`
- **URL**: `{{baseUrl}}/api/v1/pantry-items/{{pantryItemId}}`
- **Body**:
```json
{
  "quantity": 3.0,
  "unit": "KG",
  "location": "FREEZER",
  "expiryDate": "2027-01-10",
  "photoUri": "https://res.cloudinary.com/demo/image/upload/sample.jpg"
}
```

---

### 3.6. `DELETE delete_pantry` - Xóa mềm nguyên liệu
- **Method**: `DELETE`
- **URL**: `{{baseUrl}}/api/v1/pantry-items/{{pantryItemId}}`
- **Response**:
```json
{
  "code": "PANTRY_200",
  "message": "Pantry item deleted successfully",
  "data": null
}
```

---

### 3.7. `GET getExpiringItems` - Danh sách nguyên liệu sắp hết hạn
- **Method**: `GET`
- **URL**: `{{baseUrl}}/api/v1/pantry-items/expiring?days=3`
- **Query Parameters**:
  - `days`: Số ngày tính từ hôm nay (mặc định: `3`)

---

### 3.8. `POST uploadPhoto` - Upload ảnh cho nguyên liệu
- **Method**: `POST`
- **URL**: `{{baseUrl}}/api/v1/pantry-items/{{pantryItemId}}/photo`
- **Body**: Chọn `multipart/form-data`
  - Key: `image` (loại File) $\rightarrow$ Chọn 1 file ảnh thực tế từ máy tính.
- **Response**: Cập nhật và trả về `photoUri` mới được lưu trên Cloudinary.

---

### 3.9. `POST consume_pantry` - Trừ bớt số lượng khi nấu
- **Method**: `POST`
- **URL**: `{{baseUrl}}/api/v1/pantry-items/{{pantryItemId}}/consume`
- **Body**:
```json
{
  "amount": 0.5
}
```
- **Response**: Số lượng được trừ đi tương ứng (`quantity = quantity - amount`).
- **Lưu ý**: Nếu `amount > quantity`, server trả về lỗi `PANTRY_402: Insufficient quantity in pantry item`.

---

## 4. PHẦN 7: MEAL PLAN (KẾ HOẠCH BỮA ĂN)

Tất cả các endpoint trong phân hệ Meal Plan thuộc quyền **`U` (ROLE_USER)**.

### Danh mục Request trong Collection:

```text
pantry-chef/
├── MealPlan/
│   ├── GET getMealPlanByWeekStart
│   ├── GET getMealPlanById
│   ├── POST create_meal_plan
│   ├── DELETE delete_meal_plan
│   ├── POST add_meal_plan_entry
│   ├── PATCH update_meal_plan_entry
│   ├── DELETE delete_meal_plan_entry
│   ├── POST copy_meal_plan
│   └── GET getMealPlanShoppingList
```

---

### 4.1. `GET getMealPlanByWeekStart` - Lấy thực đơn của một tuần
- **Method**: `GET`
- **URL**: `{{baseUrl}}/api/v1/meal-plans?weekStart=2026-10-19`
- **Query Params**:
  - `weekStart`: Ngày đầu tuần định dạng `YYYY-MM-DD` (ví dụ thứ 2)
- **Response**: Trả về `MealPlanResponse` chứa danh sách các `entries` của tuần đó.

---

### 4.2. `GET getMealPlanById` - Chi tiết thực đơn theo ID
- **Method**: `GET`
- **URL**: `{{baseUrl}}/api/v1/meal-plans/{{mealPlanId}}`

---

### 4.3. `POST create_meal_plan` - Tạo thực đơn tuần
- **Method**: `POST`
- **URL**: `{{baseUrl}}/api/v1/meal-plans`
- **Body**:
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
*(Ghi chú: `entries` là tùy chọn, có thể tạo khung tuần rỗng rồi thêm món sau)*

---

### 4.4. `DELETE delete_meal_plan` - Xóa mềm thực đơn
- **Method**: `DELETE`
- **URL**: `{{baseUrl}}/api/v1/meal-plans/{{mealPlanId}}`

---

### 4.5. `POST add_meal_plan_entry` - Thêm món vào một ô bữa ăn
- **Method**: `POST`
- **URL**: `{{baseUrl}}/api/v1/meal-plans/{{mealPlanId}}/entries`
- **Body**:
```json
{
  "date": "2026-10-20",
  "mealType": "LUNCH",
  "recipeId": "01a15ec9-1234-7000-8000-000000000003",
  "servings": 2
}
```
- **Enum `mealType`**: `BREAKFAST` | `LUNCH` | `DINNER` | `SNACK`

---

### 4.6. `PATCH update_meal_plan_entry` - Đổi ngày, bữa, khẩu phần hoặc đánh dấu đã nấu
- **Method**: `PATCH`
- **URL**: `{{baseUrl}}/api/v1/meal-plans/{{mealPlanId}}/entries/{{mealPlanEntryId}}`
- **Body**:
```json
{
  "date": "2026-10-21",
  "mealType": "DINNER",
  "servings": 3,
  "cooked": true
}
```

---

### 4.7. `DELETE delete_meal_plan_entry` - Xóa món khỏi ô
- **Method**: `DELETE`
- **URL**: `{{baseUrl}}/api/v1/meal-plans/{{mealPlanId}}/entries/{{mealPlanEntryId}}`

---

### 4.8. `POST copy_meal_plan` - Sao chép thực đơn sang tuần khác
- **Method**: `POST`
- **URL**: `{{baseUrl}}/api/v1/meal-plans/{{mealPlanId}}/copy?toWeekStart=2026-10-26`
- **Query Params**:
  - `toWeekStart`: Ngày bắt đầu của tuần đích muốn sao chép đến (`YYYY-MM-DD`).

---

### 4.9. `GET getMealPlanShoppingList` - Xem danh sách nguyên liệu cần mua
- **Method**: `GET`
- **URL**: `{{baseUrl}}/api/v1/meal-plans/{{mealPlanId}}/shopping-list`
- **Mô tả**: Hệ thống tự động tổng hợp tất cả nguyên liệu cần thiết trong kế hoạch bữa ăn của tuần và **tự động trừ đi số lượng đang có sẵn trong tủ (Pantry)** để đưa ra danh sách cần mua.
- **Response**: Trả về `List<ShoppingItemResponse>`.

---

## 5. Danh mục mã lỗi thường gặp (Common Error Codes)

| Mã lỗi HTTP | Code trả về | Nguyên nhân | Hướng xử lý |
| :---: | :--- | :--- | :--- |
| **401** | `AUTH_401` | Chưa đính kèm hoặc token hết hạn | Gọi `POST /v1/auth/login` để lấy token mới |
| **403** | `AUTH_403` | Thiếu role (không phải `ROLE_USER`) | Đảm bảo tài khoản có quyền `ROLE_USER` |
| **404** | `PANTRY_404` | Không tìm thấy pantry item | Kiểm tra lại UUID của item |
| **404** | `MEAL_PLAN_404` | Không tìm thấy thực đơn | Kiểm tra lại UUID của meal plan |
| **400** | `PANTRY_402` | `Insufficient quantity in pantry item` | Số lượng trừ trong `consume` lớn hơn số lượng tồn trong tủ |
| **400** | `VALIDATION_FAILED` | Sai dữ liệu đầu vào | Kiểm tra lại định dạng ngày `YYYY-MM-DD` hoặc các trường bắt buộc |

---

## 6. Import Collection vào Postman / Bruno

1. Tải hoặc trỏ file: [docs/pantry-chef.postman_collection.json](file:///e:/backup/pantry-chef/docs/pantry-chef.postman_collection.json).
2. Mở **Postman** hoặc **Bruno** $\rightarrow$ Chọn **Import**.
3. Chọn file `pantry-chef.postman_collection.json`.
4. Thiết lập biến môi trường `baseUrl = http://localhost:8080`.
5. Thực hiện request đăng nhập và bắt đầu test các luồng.
