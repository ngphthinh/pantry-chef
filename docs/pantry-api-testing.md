# HƯỚNG DẪN TEST API PANTRY (POSTMAN / BRUNO)

- **Đường dẫn gốc (Base URL)**: `{{baseUrl}}/api/v1`
- **Xác thực (Authentication)**: 
  - Header: `Authorization: Bearer {{accessToken}}`
  - Đã phân quyền: `ROLE_USER` (`@PreAuthorize("hasRole('USER')")`)

---

## 📌 Danh sách các Request trong Collection `Pantry`

| STT | Tên Request (Đặt tên trong Postman/Bruno) | Method | Endpoint URL | Mô tả |
| :--- | :--- | :---: | :--- | :--- |
| 1 | `POST create_pantry` | **POST** | `{{baseUrl}}/api/v1/pantry-items` | Thêm mới 1 nguyên liệu vào tủ |
| 2 | `POST batch_create_pantry` | **POST** | `{{baseUrl}}/api/v1/pantry-items/batch` | Thêm hàng loạt nguyên liệu (sau khi quét AI) |
| 3 | `GET getPantryCursor` | **GET** | `{{baseUrl}}/api/v1/pantry-items` | Danh sách nguyên liệu phân trang bằng Cursor, hỗ trợ lọc và tìm kiếm |
| 4 | `GET GetById` | **GET** | `{{baseUrl}}/api/v1/pantry-items/{{pantryItemId}}` | Xem chi tiết 1 nguyên liệu trong tủ theo ID |
| 5 | `PUT update_pantry` | **PUT** | `{{baseUrl}}/api/v1/pantry-items/{{pantryItemId}}` | Cập nhật thông tin nguyên liệu (số lượng, hạn sử dụng, vị trí...) |
| 6 | `DELETE delete_pantry` | **DELETE** | `{{baseUrl}}/api/v1/pantry-items/{{pantryItemId}}` | Xóa mềm nguyên liệu khỏi tủ |
| 7 | `GET getExpiringItems` | **GET** | `{{baseUrl}}/api/v1/pantry-items/expiring?days=3` | Lấy danh sách các nguyên liệu sắp hết hạn trong vòng N ngày |
| 8 | `POST uploadPhoto` | **POST** | `{{baseUrl}}/api/v1/pantry-items/{{pantryItemId}}/photo` | Upload ảnh thực tế của nguyên liệu lên Cloudinary |
| 9 | `POST consume_pantry` | **POST** | `{{baseUrl}}/api/v1/pantry-items/{{pantryItemId}}/consume` | Trừ bớt số lượng nguyên liệu khi đem ra nấu ăn |

---

## 1. POST `create_pantry` - Thêm nguyên liệu vào tủ

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
  "photoUri": "https://res.cloudinary.com/demo/image/upload/sample.jpg"
}
```
*(Ghi chú: `photoUri` là trường tùy chọn, nếu chưa có ảnh có thể bỏ qua hoặc để null)*

- **Response thành công (200 OK)**:
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

## 2. POST `batch_create_pantry` - Thêm hàng loạt nguyên liệu

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

---

## 3. GET `getPantryCursor` - Danh sách, lọc, tìm kiếm

- **Method**: `GET`
- **URL**: 
  - Lấy trang đầu: `{{baseUrl}}/api/v1/pantry-items?size=20`
  - Kèm bộ lọc: `{{baseUrl}}/api/v1/pantry-items?size=10&location=FRIDGE&keyword=bò`
  - Phân trang tiếp: `{{baseUrl}}/api/v1/pantry-items?cursor={{nextCursor}}&size=10`
- **Query Params**:
  - `cursor`: UUID của item cuối trang trước (để lấy trang kế tiếp)
  - `size`: số lượng item / trang (mặc định 20, tối đa 50)
  - `categoryId`: UUID danh mục nguyên liệu (tùy chọn)
  - `location`: `FRIDGE` | `FREEZER` | `PANTRY_SHELF` (tùy chọn)
  - `keyword`: từ khóa tìm kiếm theo tên nguyên liệu (tùy chọn)
- **Headers**:
  - `Authorization: Bearer {{accessToken}}`

---

## 4. GET `GetById` - Chi tiết nguyên liệu trong tủ

- **Method**: `GET`
- **URL**: `{{baseUrl}}/api/v1/pantry-items/01a15eb1-532d-74f1-90b2-f2e12b53aa27`
- **Headers**:
  - `Authorization: Bearer {{accessToken}}`

---

## 5. PUT `update_pantry` - Chỉnh sửa nguyên liệu

- **Method**: `PUT`
- **URL**: `{{baseUrl}}/api/v1/pantry-items/01a15eb1-532d-74f1-90b2-f2e12b53aa27`
- **Headers**:
  - `Content-Type: application/json`
  - `Authorization: Bearer {{accessToken}}`
- **Body (raw JSON)**:
```json
{
  "quantity": 3.5,
  "unit": "KG",
  "location": "FREEZER",
  "expiryDate": "2027-01-15",
  "photoUri": "https://res.cloudinary.com/demo/image/upload/sample.jpg"
}
```

---

## 6. DELETE `delete_pantry` - Xóa mềm nguyên liệu

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

---

## 7. GET `getExpiringItems` - Danh sách sắp hết hạn

- **Method**: `GET`
- **URL**: `{{baseUrl}}/api/v1/pantry-items/expiring?days=3`
- **Query Params**:
  - `days`: Số ngày ngưỡng hết hạn (mặc định 3 ngày)
- **Headers**:
  - `Authorization: Bearer {{accessToken}}`

---

## 8. POST `uploadPhoto` - Upload ảnh cho nguyên liệu

- **Method**: `POST`
- **URL**: `{{baseUrl}}/api/v1/pantry-items/01a15eb1-532d-74f1-90b2-f2e12b53aa27/photo`
- **Headers**:
  - `Authorization: Bearer {{accessToken}}`
- **Body**: Chọn `multipart/form-data`
  - Key: `image` (loại File) -> Chọn 1 file ảnh (.png, .jpg, .jpeg)

---

## 9. POST `consume_pantry` - Trừ số lượng khi dùng nấu

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
    "location": "FRIDGE",
    "updatedAt": "2026-10-21T14:40:00.000000Z"
  }
}
```
*(Nếu số lượng trừ lớn hơn số lượng hiện có, server sẽ trả về lỗi `PANTRY_402: Insufficient quantity in pantry item`)*
