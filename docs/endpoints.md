# pantry-chef: danh sách endpoint theo feature

Quy ước đặt tên, DTO, mã lỗi: xem `README.md` (mục 3 và 4). Tài liệu này chỉ liệt kê **cần làm gì**.

## 0. Quy ước chung

| Hạng mục | Quy ước |
|---|---|
| Base URL | `/api` (context path) + `/v1/...`. Ví dụ đầy đủ: `/api/v1/pantry-items` |
| Xác thực | Header `Authorization: Bearer <accessToken>`. Mặc định mọi endpoint cần đăng nhập; endpoint công khai phải thêm vào `PUBLIC_ENDPOINTS_*` trong `SecurityConfig` |
| Quyền | **P** = công khai, **U** = `ROLE_USER` (đã đăng nhập), **A** = `ROLE_ADMIN` (`@PreAuthorize("hasRole('ADMIN')")`) |
| Phản hồi thành công | `ApiResponse<T>`: `{ code, message, data }`, code/message lấy từ `<Feature>ResponseCode` |
| Phản hồi lỗi | `ErrorResponse`: `{ timestamp, status, error, message, path, errors? }` (do `GlobalExceptionHandler` tạo) |
| Phân trang | Cursor: `?cursor=<id>&size=20` (size tối đa 50). Trả `CursorResponse { content, hasNext, nextCursor }` |
| Dữ liệu cá nhân | Luôn lọc theo người dùng hiện tại (`appUtil.userIdFromAuthentication()`); bản ghi của người khác trả 404 |
| Xóa | Entity kế thừa `SoftDeletableEntity`: xóa mềm. Các entity còn lại: xóa cứng |
| Upload | `multipart/form-data`, field `image`, tối đa 10MB (theo `application.properties`), lưu qua `MediaStorageService` |
| Cache offline | Mobile chỉ gọi API; dữ liệu đã tải được cache ở máy để xem lại khi mất mạng. Response của chi tiết công thức và công thức đã lưu phải chứa đủ dữ liệu (nguyên liệu, các bước, URL ảnh) để cache được |

Ký hiệu ưu tiên: **★** = lõi (đáp ứng yêu cầu bắt buộc: camera/file, API/phân trang, AI, offline); **☆** = mở rộng, làm sau.
Trạng thái: **Đã có** = đã viết trong project; **Cần làm** = chưa có.

---

## 1. `auth` (đã có: user, role)

### 1.1 Xác thực: `/v1/auth`

| Method | Path | Mô tả | Quyền | Request → Response | Ưu tiên | Trạng thái |
|---|---|---|---|---|---|---|
| POST | `/v1/auth/register` | Đăng ký | P | `UserCreateRequest` → `UserResponse` | ★ | Đã có |
| POST | `/v1/auth/login` | Đăng nhập | P | `LoginRequest` → `AuthenticateResponse` | ★ | Đã có |
| POST | `/v1/auth/refresh-token` | Làm mới token | P | `RefreshTokenRequest` → `AuthenticateResponse` | ★ | Đã có |
| POST | `/v1/auth/logout` | Đăng xuất (blacklist access token, xóa refresh token) | P | `LogoutRequest` → `Void` | ★ | Đã có |
| GET | `/v1/auth/test` | Endpoint thử quyền | U | → `String` | | Đã có, nên xóa |
| POST | `/v1/auth/forgot-password` | Gửi email đặt lại mật khẩu | P | `ForgotPasswordRequest {email}` → `Void` | ☆ | Cần làm (đã có trong danh sách public) |
| POST | `/v1/auth/reset-password` | Đặt lại mật khẩu bằng token | P | `ResetPasswordRequest {token, newPassword}` → `Void` | ☆ | Cần làm (đã có trong danh sách public) |
| POST | `/v1/auth/change-password` | Đổi mật khẩu, vô hiệu token cũ | U | `ChangePasswordRequest {oldPassword, newPassword}` → `Void` | ☆ | Cần làm (dùng `ChangePasswordService`) |

### 1.2 Hồ sơ người dùng: `/v1/users/me`

| Method | Path | Mô tả | Quyền | Request → Response | Ưu tiên | Trạng thái |
|---|---|---|---|---|---|---|
| GET | `/v1/users/me` | Lấy hồ sơ | U | → `UserResponse` | ★ | Cần làm |
| PUT | `/v1/users/me` | Cập nhật tên hiển thị, hệ đo, giao diện | U | `UserUpdateRequest` → `UserResponse` | ★ | Cần làm |
| POST | `/v1/users/me/avatar` | Upload avatar | U | multipart `image` → `UserResponse` | ☆ | Cần làm |
| DELETE | `/v1/users/me` | Xóa tài khoản | U | → `Void` | ☆ | Cần làm (`User` hiện kế thừa `BaseEntity`; muốn xóa mềm phải đổi sang `SoftDeletableEntity`) |

### 1.3 Quản trị (tùy chọn)

| Method | Path | Mô tả | Quyền | Request → Response | Ưu tiên |
|---|---|---|---|---|---|
| GET | `/v1/admin/users?cursor=&size=&keyword=` | Danh sách người dùng | A | → `CursorResponse<UserResponse>` | ☆ |

---

## 2. `ingredient`

Entity: `Ingredient`, `IngredientCategory`, `ExcludedIngredient`.

### 2.1 Nguyên liệu (danh mục chung)

| Method | Path | Mô tả | Quyền | Request → Response | Ưu tiên |
|---|---|---|---|---|---|
| GET | `/v1/ingredients/search?q=&categoryId=&cursor=&size=` | Tìm / gợi ý tên khi gõ | U | → `CursorResponse<IngredientResponse>` | ★ |
| GET | `/v1/ingredients/{id}` | Chi tiết | U | → `IngredientResponse` | ★ |
| POST | `/v1/ingredients` | Thêm nguyên liệu vào danh mục | A | `IngredientCreateRequest` → `IngredientResponse` | ★ |
| PUT | `/v1/ingredients/{id}` | Sửa | A | `IngredientUpdateRequest` → `IngredientResponse` | ★ |
| DELETE | `/v1/ingredients/{id}` | Xóa (mềm) | A | → `Void` | ★ |
| GET | `/v1/ingredients/barcode/{code}` | Tra theo mã vạch | U | → `IngredientResponse` | ☆ (cần thêm cột `barcode` vào `Ingredient`) |

### 2.2 Nhóm nguyên liệu (bảng tra cứu)

| Method | Path | Mô tả | Quyền | Request → Response | Ưu tiên |
|---|---|---|---|---|---|
| GET | `/v1/ingredient-categories` | Danh sách nhóm | P | → `List<IngredientCategoryResponse>` | ★ |
| POST | `/v1/ingredient-categories` | Thêm nhóm | A | `IngredientCategoryRequest` → `IngredientCategoryResponse` | ☆ |
| PUT | `/v1/ingredient-categories/{id}` | Sửa nhóm | A | `IngredientCategoryRequest` → `IngredientCategoryResponse` | ☆ |
| DELETE | `/v1/ingredient-categories/{id}` | Xóa nhóm (hard delete, chặn khi còn nguyên liệu dùng) | A | → `Void` | ☆ |

### 2.3 Nguyên liệu loại trừ (sở thích cá nhân)

| Method | Path | Mô tả | Quyền | Request → Response | Ưu tiên |
|---|---|---|---|---|---|
| GET | `/v1/users/me/excluded-ingredients` | Danh sách | U | → `List<ExcludedIngredientResponse>` | ★ |
| POST | `/v1/users/me/excluded-ingredients` | Thêm | U | `ExcludedIngredientCreateRequest {name}` → `ExcludedIngredientResponse` | ★ |
| DELETE | `/v1/users/me/excluded-ingredients/{id}` | Xóa | U | → `Void` | ★ |

---

## 3. `sync`

Entity: `SyncStatus` (bảng tra cứu). Package hiện đang tên `sysn`, nên đổi thành `sync`.

| Method | Path | Mô tả | Quyền | Request → Response | Ưu tiên |
|---|---|---|---|---|---|
| GET | `/v1/sync-statuses` | Danh sách trạng thái | U | → `List<SyncStatusResponse>` | ☆ |

Nếu sau này cho phép ghi dữ liệu khi offline rồi đồng bộ (hiện thiết kế là chỉ xem dữ liệu đã cache), thêm:

| Method | Path | Mô tả | Quyền | Request → Response |
|---|---|---|---|---|
| GET | `/v1/sync/changes?since=` | Lấy thay đổi từ một thời điểm (dựa `updatedAt`, `deletedAt`) | U | → `SyncChangesResponse` |
| POST | `/v1/sync/push` | Đẩy thay đổi local lên | U | `SyncPushRequest` → `SyncPushResponse` |

---

## 4. `pantry`

Entity: `PantryItem`. Mọi endpoint chỉ thao tác trên dữ liệu của người dùng hiện tại.

| Method | Path | Mô tả | Quyền | Request → Response | Ưu tiên |
|---|---|---|---|---|---|
| POST | `/v1/pantry-items` | Thêm nguyên liệu vào tủ | U | `PantryItemCreateRequest` → `PantryItemResponse` | ★ |
| POST | `/v1/pantry-items/batch` | Thêm hàng loạt (sau khi xác nhận kết quả quét AI) | U | `PantryItemBatchCreateRequest` → `List<PantryItemResponse>` | ★ |
| GET | `/v1/pantry-items?cursor=&size=&categoryId=&location=&keyword=` | Danh sách, lọc, tìm | U | → `CursorResponse<PantryItemResponse>` | ★ |
| GET | `/v1/pantry-items/{id}` | Chi tiết | U | → `PantryItemResponse` | ★ |
| PUT | `/v1/pantry-items/{id}` | Sửa | U | `PantryItemUpdateRequest` → `PantryItemResponse` | ★ |
| DELETE | `/v1/pantry-items/{id}` | Xóa (mềm) | U | → `Void` | ★ |
| GET | `/v1/pantry-items/expiring?days=3` | Sắp hết hạn | U | → `List<PantryItemResponse>` | ★ |
| POST | `/v1/pantry-items/{id}/photo` | Upload ảnh | U | multipart `image` → `PantryItemResponse` | ★ |
| POST | `/v1/pantry-items/{id}/consume` | Trừ số lượng khi dùng | U | `PantryItemConsumeRequest {amount}` → `PantryItemResponse` | ☆ |

---

## 5. `recipe`

Entity: `Recipe`, `RecipeIngredient`, `RecipeStep`, `Cuisine`.
Công thức lấy từ API bên ngoài (qua `RecipeProviderClient`) hoặc do người dùng tự tạo (`custom = true`).

Với kết quả từ API bên ngoài, `nextCursor` là offset dạng chuỗi (do `CursorResponse.nextCursor` là `String`); `AppUtil.buildCursorResponse` chỉ dùng cho cursor là UUID của dữ liệu trong DB.

### 5.1 Tìm kiếm và xem công thức

| Method | Path | Mô tả | Quyền | Request → Response | Ưu tiên |
|---|---|---|---|---|---|
| GET | `/v1/recipes/search?ingredients=&cursor=&size=&cuisineId=&maxMinutes=&difficulty=` | Tìm theo nguyên liệu (có điểm khớp, danh sách thiếu) | U | → `CursorResponse<RecipeSummaryResponse>` | ★ |
| GET | `/v1/recipes/suggestions?cursor=&size=` | Gợi ý dựa trên tủ của tôi (tự lấy nguyên liệu, bỏ nguyên liệu loại trừ) | U | → `CursorResponse<RecipeSummaryResponse>` | ★ |
| GET | `/v1/recipes/{externalId}` | Chi tiết (đủ nguyên liệu, các bước, ảnh) | U | → `RecipeDetailResponse` | ★ |
| GET | `/v1/recipes/by-name?q=&cursor=&size=` | Tìm theo tên món | U | → `CursorResponse<RecipeSummaryResponse>` | ☆ |
| GET | `/v1/recipes/random?count=` | Ngẫu nhiên | U | → `List<RecipeSummaryResponse>` | ☆ |
| GET | `/v1/recipes/popular?cursor=&size=` | Phổ biến | U | → `CursorResponse<RecipeSummaryResponse>` | ☆ |
| GET | `/v1/recipes/{externalId}/similar` | Món tương tự | U | → `List<RecipeSummaryResponse>` | ☆ |
| GET | `/v1/recipes/{externalId}/scale?servings=` | Quy đổi lượng theo khẩu phần | U | → `RecipeDetailResponse` | ☆ |

### 5.2 Công thức tự tạo

| Method | Path | Mô tả | Quyền | Request → Response | Ưu tiên |
|---|---|---|---|---|---|
| POST | `/v1/recipes/custom` | Tạo | U | `RecipeCreateRequest` (kèm nguyên liệu, các bước) → `RecipeDetailResponse` | ☆ |
| GET | `/v1/recipes/custom?cursor=&size=` | Danh sách của tôi | U | → `CursorResponse<RecipeSummaryResponse>` | ☆ |
| GET | `/v1/recipes/custom/{id}` | Chi tiết | U | → `RecipeDetailResponse` | ☆ |
| PUT | `/v1/recipes/custom/{id}` | Sửa | U | `RecipeUpdateRequest` → `RecipeDetailResponse` | ☆ |
| DELETE | `/v1/recipes/custom/{id}` | Xóa (mềm) | U | → `Void` | ☆ |
| POST | `/v1/recipes/custom/{id}/image` | Upload ảnh món | U | multipart `image` → `RecipeDetailResponse` | ☆ |

### 5.3 Ẩm thực (bảng tra cứu)

| Method | Path | Mô tả | Quyền | Request → Response | Ưu tiên |
|---|---|---|---|---|---|
| GET | `/v1/cuisines` | Danh sách | P | → `List<CuisineResponse>` | ★ |
| POST | `/v1/cuisines` | Thêm | A | `CuisineRequest` → `CuisineResponse` | ☆ |
| PUT | `/v1/cuisines/{id}` | Sửa | A | `CuisineRequest` → `CuisineResponse` | ☆ |
| DELETE | `/v1/cuisines/{id}` | Xóa (hard delete, chặn khi còn công thức dùng) | A | → `Void` | ☆ |

---

## 6. `cookbook`

Entity: `SavedRecipe`, `Collection`. Đây là nhóm dữ liệu chính để mobile cache và xem offline: response chi tiết phải kèm đầy đủ thông tin công thức.

### 6.1 Công thức đã lưu

| Method | Path | Mô tả | Quyền | Request → Response | Ưu tiên |
|---|---|---|---|---|---|
| POST | `/v1/saved-recipes` | Lưu công thức | U | `SavedRecipeCreateRequest {externalId hoặc recipeId}` → `SavedRecipeResponse` | ★ |
| GET | `/v1/saved-recipes?cursor=&size=&favorite=&collectionId=` | Danh sách | U | → `CursorResponse<SavedRecipeResponse>` | ★ |
| GET | `/v1/saved-recipes/{id}` | Chi tiết (kèm `RecipeDetailResponse`) | U | → `SavedRecipeDetailResponse` | ★ |
| PATCH | `/v1/saved-recipes/{id}` | Sửa sao, ghi chú, yêu thích | U | `SavedRecipeUpdateRequest` → `SavedRecipeResponse` | ☆ |
| DELETE | `/v1/saved-recipes/{id}` | Bỏ lưu (mềm) | U | → `Void` | ★ |

### 6.2 Bộ sưu tập

| Method | Path | Mô tả | Quyền | Request → Response | Ưu tiên |
|---|---|---|---|---|---|
| GET | `/v1/collections` | Danh sách | U | → `List<CollectionResponse>` | ☆ |
| POST | `/v1/collections` | Tạo | U | `CollectionCreateRequest {name}` → `CollectionResponse` | ☆ |
| PUT | `/v1/collections/{id}` | Đổi tên | U | `CollectionUpdateRequest {name}` → `CollectionResponse` | ☆ |
| DELETE | `/v1/collections/{id}` | Xóa (mềm) | U | → `Void` | ☆ |
| POST | `/v1/collections/{id}/recipes/{savedRecipeId}` | Thêm món vào bộ sưu tập | U | → `CollectionResponse` | ☆ |
| DELETE | `/v1/collections/{id}/recipes/{savedRecipeId}` | Bỏ món khỏi bộ sưu tập | U | → `CollectionResponse` | ☆ |

---

## 7. `mealplan`

Entity: `MealPlan`, `MealPlanEntry`.

| Method | Path | Mô tả | Quyền | Request → Response | Ưu tiên |
|---|---|---|---|---|---|
| GET | `/v1/meal-plans?weekStart=` | Lấy thực đơn của một tuần (rỗng nếu chưa có) | U | → `MealPlanResponse` | ★ |
| GET | `/v1/meal-plans/{id}` | Chi tiết | U | → `MealPlanResponse` | ★ |
| POST | `/v1/meal-plans` | Tạo thực đơn (có thể kèm các ô, dùng để lưu bản AI tạo) | U | `MealPlanCreateRequest {weekStart, entries?}` → `MealPlanResponse` | ★ |
| DELETE | `/v1/meal-plans/{id}` | Xóa (mềm) | U | → `Void` | ☆ |
| POST | `/v1/meal-plans/{id}/entries` | Thêm món vào một ô | U | `MealPlanEntryCreateRequest {date, mealType, recipeId, servings}` → `MealPlanEntryResponse` | ★ |
| PATCH | `/v1/meal-plans/{id}/entries/{entryId}` | Đổi ngày, bữa, khẩu phần, đánh dấu đã nấu | U | `MealPlanEntryUpdateRequest` → `MealPlanEntryResponse` | ★ |
| DELETE | `/v1/meal-plans/{id}/entries/{entryId}` | Xóa món khỏi ô (mềm) | U | → `Void` | ★ |
| POST | `/v1/meal-plans/{id}/copy?toWeekStart=` | Sao chép sang tuần khác | U | → `MealPlanResponse` | ☆ |
| GET | `/v1/meal-plans/{id}/shopping-list` | Xem trước nguyên liệu cần mua (đã trừ đồ trong tủ) | U | → `List<ShoppingItemResponse>` | ☆ |

---

## 8. `shopping`

Entity: `ShoppingList`, `ShoppingItem`.

| Method | Path | Mô tả | Quyền | Request → Response | Ưu tiên |
|---|---|---|---|---|---|
| GET | `/v1/shopping-lists?cursor=&size=` | Danh sách | U | → `CursorResponse<ShoppingListResponse>` | ☆ |
| POST | `/v1/shopping-lists` | Tạo | U | `ShoppingListCreateRequest {name}` → `ShoppingListResponse` | ☆ |
| GET | `/v1/shopping-lists/{id}` | Chi tiết (kèm các mục) | U | → `ShoppingListDetailResponse` | ☆ |
| PUT | `/v1/shopping-lists/{id}` | Đổi tên | U | `ShoppingListUpdateRequest` → `ShoppingListResponse` | ☆ |
| DELETE | `/v1/shopping-lists/{id}` | Xóa (mềm) | U | → `Void` | ☆ |
| POST | `/v1/shopping-lists/{id}/items` | Thêm mục | U | `ShoppingItemCreateRequest` → `ShoppingItemResponse` | ☆ |
| PATCH | `/v1/shopping-lists/{id}/items/{itemId}` | Sửa, tích đã mua | U | `ShoppingItemUpdateRequest` → `ShoppingItemResponse` | ☆ |
| DELETE | `/v1/shopping-lists/{id}/items/{itemId}` | Xóa mục (mềm) | U | → `Void` | ☆ |
| POST | `/v1/shopping-lists/from-meal-plan/{mealPlanId}` | Tạo từ thực đơn (đã trừ đồ trong tủ) | U | → `ShoppingListDetailResponse` | ☆ |
| POST | `/v1/shopping-lists/from-recipe/{recipeId}` | Tạo từ một công thức | U | → `ShoppingListDetailResponse` | ☆ |
| POST | `/v1/shopping-lists/{id}/move-to-pantry` | Chuyển các mục đã mua vào tủ (gọi `PantryItemService`) | U | → `List<PantryItemResponse>` | ☆ |

---

## 9. `ai`

Entity: `SubstitutionSuggestion`. Mọi endpoint yêu cầu đăng nhập để giới hạn tần suất theo người dùng.
Tất cả đều đi qua `ContentGuard`: system prompt cấm tư vấn y tế, kiểm tra input và lọc output. Vi phạm: lỗi 422 (`AppException`).

| Method | Path | Mô tả | Quyền | Request → Response | Ưu tiên |
|---|---|---|---|---|---|
| POST | `/v1/ai/substitutions` | Gợi ý nguyên liệu thay thế (tối đa 3: tên, tỉ lệ, ghi chú, độ tin cậy) | U | `SubstitutionRequest {recipeId/recipeTitle, missingIngredient}` → `List<SubstitutionResponse>` | ★ |
| POST | `/v1/ai/recognize` | Nhận diện nguyên liệu từ ảnh (camera) | U | multipart `image` → `List<RecognizedIngredientResponse>` | ★ |
| POST | `/v1/ai/meal-plan` | Tạo bản nháp thực đơn từ nguyên liệu trong tủ (chưa lưu; lưu bằng `POST /v1/meal-plans`) | U | `AiMealPlanRequest {weekStart, days, cuisineId?, maxMinutes?}` → `MealPlanResponse` | ★ |
| POST | `/v1/ai/recipe-ideas` | Ý tưởng món từ nguyên liệu trong tủ | U | `RecipeIdeasRequest` → `List<RecipeIdeaResponse>` | ☆ |
| POST | `/v1/ai/adapt-recipe` | Điều chỉnh công thức theo nguyên liệu loại trừ | U | `AdaptRecipeRequest {recipeId}` → `RecipeDetailResponse` | ☆ |

Mã lỗi gợi ý: `422` bị `ContentGuard` chặn, `413`/`400` ảnh quá lớn hoặc sai định dạng, `429` vượt giới hạn tần suất, `502` nhà cung cấp AI lỗi.
Tham số AI chỉ gồm sở thích nấu ăn (`excludedIngredients`, `cuisine`, `maxMinutes`), không có trường sức khỏe.

---

## 10. `common` và khác

| Method | Path | Mô tả | Quyền | Response | Ưu tiên |
|---|---|---|---|---|---|
| GET | `/actuator/health` | Health check (đã có `spring-boot-starter-actuator`, cần thêm vào danh sách public nếu dùng) | P | `{status}` | ★ |
| GET | `/v1/meta/units` | Danh sách đơn vị (`Unit`) | U | `List<String>` | ☆ |
| GET | `/v1/meta/enums` | Danh sách enum cho client (`Unit`, `MealType`, `Difficulty`, `StorageLocation`, `ShoppingItemStatus`, `MeasurementSystem`, `ThemeMode`) | U | `MetaEnumsResponse` | ☆ |
| GET | `/v1/units/convert?value=&from=&to=` | Quy đổi đơn vị | U | `UnitConvertResponse` | ☆ |
| POST | `/v1/feedback` | Gửi phản hồi, báo lỗi | U | `Void` | ☆ |
| GET | `/v1/stats/me` | Thống kê cá nhân (số món đã nấu, nguyên liệu bỏ phí) | U | `UserStatsResponse` | ☆ |

---

## 11. Thứ tự triển khai đề xuất

| Giai đoạn | Việc | Endpoint ★ chính |
|---|---|---|
| 1. Nền | Hoàn thiện `auth` + hồ sơ; dữ liệu tra cứu (`ingredient`, `cuisine`, `sync`) và seed | `GET/PUT /users/me`, `GET /ingredient-categories`, `GET /cuisines`, `/ingredients/*` |
| 2. Tủ nguyên liệu | `pantry` (CRUD, ảnh, sắp hết hạn) | `/pantry-items/*` |
| 3. Công thức | `recipe` (API ngoài, phân trang, chi tiết) | `/recipes/search`, `/recipes/suggestions`, `/recipes/{externalId}` |
| 4. Lưu và offline | `cookbook` (response đủ dữ liệu để mobile cache) | `/saved-recipes/*` |
| 5. AI | nhận diện ảnh, thay thế nguyên liệu, `ContentGuard` | `/ai/recognize`, `/ai/substitutions` |
| 6. Thực đơn | `mealplan`, `/ai/meal-plan` | `/meal-plans/*` |
| 7. Mở rộng | `shopping`, bộ sưu tập, công thức tự tạo, thống kê, đồng bộ | các mục ☆ |
