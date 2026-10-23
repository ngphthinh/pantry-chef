# Tủ công thức: ý nghĩa từng class

Phạm vi: entity và enum trong class diagram.

## Quy ước chung

- **Nguồn dữ liệu là service (Spring + DB).** Mobile chỉ gọi API; không giữ dữ liệu nghiệp vụ riêng.
- **Xem offline** chỉ dùng cho dữ liệu **đã được cache** (đã tải qua API trước đó, kèm ảnh trong FileSystem). Không cache thì không xem được khi mất mạng.
- **Mọi entity đều kế thừa `BaseEntity`**: `id` (UUID), `createdAt`, `updatedAt`.
- **Entity cần xóa mềm** kế thừa `SoftDeletableEntity` (con của `BaseEntity`), thêm `deletedAt`. `deletedAt = null` là còn hiệu lực; truy vấn mặc định lọc `deletedAt IS NULL`.
- `Cuisine`, `IngredientCategory`, `SyncStatus` là **entity tra cứu** lưu trên service; mobile lấy qua API, không hard-code.
- Enum chỉ còn các tập giá trị cố định, không đổi theo dữ liệu.

| Nhóm | Class |
|---|---|
| Lớp nền | `BaseEntity`, `SoftDeletableEntity` |
| Entity xóa mềm | `User`, `Ingredient`, `PantryItem`, `Recipe`, `SavedRecipe`, `Collection`, `MealPlan`, `MealPlanEntry`, `ShoppingList`, `ShoppingItem` |
| Entity xóa cứng | `ExcludedIngredient`, `RecipeIngredient`, `RecipeStep`, `SubstitutionSuggestion`, `Cuisine`, `IngredientCategory`, `SyncStatus` |

---

## A. Lớp nền

### `BaseEntity` (abstract)
Lớp cha của mọi entity.
- `id: UUID`: khóa chính, sinh ở service (UUID v4).
- `createdAt`: thời điểm tạo.
- `updatedAt`: thời điểm sửa gần nhất, cập nhật mỗi lần ghi. Dùng để client biết dữ liệu cache đã cũ chưa.

### `SoftDeletableEntity` (abstract, extends `BaseEntity`)
Thêm `deletedAt: Date` (null = chưa xóa). Dùng cho entity cần giữ lại sau khi xóa (khôi phục, tránh mất liên kết, audit).

---

## B. Enum

| Enum | Giá trị | Ý nghĩa / dùng ở đâu |
|---|---|---|
| `Unit` | G, KG, ML, L, PCS, TBSP, TSP, CUP, PINCH | Đơn vị đo. Dùng cho `PantryItem`, `RecipeIngredient`, `ShoppingItem`, `Ingredient.defaultUnit`. PCS = cái/quả/củ; PINCH = một nhúm |
| `MealType` | BREAKFAST, LUNCH, DINNER, SNACK | Bữa trong ngày của `MealPlanEntry` |
| `Difficulty` | EASY, MEDIUM, HARD | Độ khó công thức, dùng để lọc |
| `StorageLocation` | FRIDGE, FREEZER, PANTRY_SHELF | Nơi cất nguyên liệu |
| `ShoppingItemStatus` | PENDING, BOUGHT | Mục trong danh sách mua đã mua chưa |
| `MeasurementSystem` | METRIC, IMPERIAL | Hệ đo người dùng ưa thích |
| `ThemeMode` | LIGHT, DARK, SYSTEM | Giao diện sáng / tối / theo hệ thống |

---

## C. Entity tra cứu (lưu trên service)

Cùng cấu trúc: `code` (ổn định, dùng trong code), `name` (hiển thị). Thêm / sửa giá trị bằng dữ liệu, không cần phát hành lại app. Client gọi API lấy danh sách rồi cache.

### `Cuisine`
Ẩm thực của công thức (Việt Nam, Á, Âu, Ý, ...). `Recipe` trỏ tới một `Cuisine`; dùng để lọc tìm kiếm.
- Trường: `code`, `name`.

### `IngredientCategory`
Nhóm nguyên liệu (rau củ, trái cây, thịt, hải sản, sữa, ngũ cốc, gia vị, nước chấm, đồ uống, khác). `Ingredient` trỏ tới một nhóm; dùng để lọc và nhóm hiển thị trong tủ và danh sách mua.
- Trường: `code`, `name`.

### `SyncStatus`
Trạng thái đồng bộ của bản ghi, lưu trên service. `PantryItem` và `SavedRecipe` trỏ tới một trạng thái. Client chỉ đọc qua API, không tự đặt.
- Trường: `code`, `name`.
- Giá trị gợi ý (seed): `SYNCED`, `PENDING`, `CONFLICT`.
- Lưu ý: vì offline chỉ để xem dữ liệu đã cache (không ghi offline), các giá trị `LOCAL_ONLY` / `PENDING_UPLOAD` ở bản trước có thể không cần. Chốt lại danh sách giá trị khi quyết định có cho ghi offline hay không.

---

## D. Entity

### Nhóm người dùng

#### `User` (xóa mềm)
Tài khoản người dùng, chủ sở hữu mọi dữ liệu cá nhân. Chứa cài đặt: ngôn ngữ, hệ đo, giao diện.
- Quan hệ: 1 `User` có nhiều `PantryItem`, `SavedRecipe`, `Collection`, `MealPlan`, `ShoppingList`, `ExcludedIngredient`.
- Xóa tài khoản = đặt `deletedAt`; dữ liệu con bị ẩn theo, xóa vật lý bằng job dọn dẹp.

#### `ExcludedIngredient`
Nguyên liệu người dùng không muốn dùng (sở thích cá nhân). Dùng để lọc kết quả tìm kiếm và làm đầu vào cho AI. Chỉ là sở thích, không phải dữ liệu y tế. Xóa cứng.

### Nhóm nguyên liệu

#### `Ingredient` (xóa mềm)
Danh mục nguyên liệu chung (master data): tên, nhóm (`IngredientCategory`), đơn vị mặc định. Dùng cho autocomplete và chuẩn hóa tên. Xóa mềm để các `PantryItem` cũ vẫn trỏ được.

#### `PantryItem` (xóa mềm)
Một món nguyên liệu đang có trong tủ: số lượng, đơn vị, nơi cất, hạn dùng, ảnh. Thực thể trung tâm của quản lý nguyên liệu và là đầu vào của tìm công thức.
- Quan hệ: thuộc `User`; trỏ tới `Ingredient` và `SyncStatus`.
- `expiryDate` dùng cho cảnh báo sắp hết hạn; `photoUri` là ảnh (URL trên service, bản cache nằm trong FileSystem).

### Nhóm công thức

#### `Recipe` (xóa mềm)
Công thức nấu ăn: tiêu đề, ảnh, khẩu phần, thời gian, ẩm thực (`Cuisine`), độ khó, nguồn. Có thể lấy từ API bên ngoài (`externalId` có giá trị) hoặc do người dùng tự nhập (`isCustom = true`).
- `imageUrl`: ảnh gốc; `localImageUri`: ảnh đã cache để xem offline.
- Chứa danh sách `RecipeIngredient` và `RecipeStep`.

#### `RecipeIngredient`
Một dòng nguyên liệu của công thức: tên, lượng, đơn vị. Là đồ **cần** để nấu, khác `PantryItem` (đồ đang **có**). So sánh hai bên để ra có / thiếu. Xóa cứng cùng công thức.

#### `RecipeStep`
Một bước nấu theo thứ tự (`order`), kèm nội dung và thời gian hẹn giờ tùy chọn (`timerSeconds`). Xóa cứng cùng công thức.

#### `SavedRecipe` (xóa mềm)
Công thức người dùng đã lưu, kèm dữ liệu cá nhân: sao, ghi chú, yêu thích, ngày lưu. Tách khỏi `Recipe` để một công thức dùng chung có ghi chú / đánh giá riêng theo từng người.
- Quan hệ: thuộc `User`; trỏ tới `Recipe` và `SyncStatus`; có thể nằm trong nhiều `Collection`.
- Đây là nhóm dữ liệu chính được cache để **xem offline** (kèm `Recipe`, `RecipeIngredient`, `RecipeStep` và ảnh).

#### `Collection` (xóa mềm)
Bộ sưu tập do người dùng tạo để nhóm công thức đã lưu (ví dụ "Món nhanh"). Quan hệ nhiều-nhiều với `SavedRecipe`.

### Nhóm lập bữa ăn

#### `MealPlan` (xóa mềm)
Thực đơn một tuần, xác định bởi `weekStart` (thường là thứ Hai). Container của các `MealPlanEntry`.

#### `MealPlanEntry` (xóa mềm)
Một ô trong thực đơn: ngày, bữa (`MealType`), món (`Recipe`), khẩu phần, đã nấu chưa (`isCooked`).

### Nhóm mua sắm

#### `ShoppingList` (xóa mềm)
Danh sách mua sắm có tên. Tạo thủ công, từ một công thức (trừ đồ đã có) hoặc từ thực đơn tuần.

#### `ShoppingItem` (xóa mềm)
Một mục cần mua: tên, số lượng, đơn vị, trạng thái (`ShoppingItemStatus`). Mua xong có thể chuyển thành `PantryItem`.

### Nhóm AI

#### `SubstitutionSuggestion`
Gợi ý nguyên liệu thay thế do AI tạo cho một `RecipeIngredient` còn thiếu: tên thay thế, tỉ lệ, ghi chú, độ tin cậy (0 đến 1). Kết quả tạm, xóa cứng. Nội dung đã qua kiểm soát, không có tư vấn y tế.

---

## E. Quan hệ chính

| Quan hệ | Loại | Ý nghĩa |
|---|---|---|
| `SoftDeletableEntity` kế thừa `BaseEntity` | Generalization | Thêm `deletedAt` |
| `User` 1 — * `PantryItem` / `SavedRecipe` / `Collection` / `MealPlan` / `ShoppingList` / `ExcludedIngredient` | Composition | Dữ liệu thuộc về người dùng |
| `Cuisine` 1 — * `Recipe` | Association | Mỗi công thức thuộc một ẩm thực |
| `IngredientCategory` 1 — * `Ingredient` | Association | Mỗi nguyên liệu thuộc một nhóm |
| `SyncStatus` 1 — * `PantryItem` / `SavedRecipe` | Association | Trạng thái đồng bộ của bản ghi |
| `Ingredient` 1 — * `PantryItem` | Association | Món trong tủ trỏ về nguyên liệu chuẩn |
| `Recipe` 1 — * `RecipeIngredient` / `RecipeStep` | Composition | Không tồn tại độc lập |
| `SavedRecipe` * — 1 `Recipe` | Association | Bản lưu cá nhân trỏ tới công thức |
| `Collection` * — * `SavedRecipe` | Aggregation | Một món nằm trong nhiều bộ sưu tập |
| `MealPlan` 1 — * `MealPlanEntry` | Composition | Ô thực đơn thuộc thực đơn tuần |
| `MealPlanEntry` * — 1 `Recipe` | Association | Ô thực đơn trỏ tới món sẽ nấu |
| `ShoppingList` 1 — * `ShoppingItem` | Composition | Mục mua thuộc danh sách |
| `SubstitutionSuggestion` * — 1 `RecipeIngredient` | Association | Gợi ý thay thế cho nguyên liệu còn thiếu |

## F. Phân biệt các class dễ nhầm

| Cặp | Khác nhau |
|---|---|
| `Ingredient` vs `PantryItem` | `Ingredient`: danh mục chung. `PantryItem`: món cụ thể đang có (số lượng, hạn dùng) |
| `PantryItem` vs `RecipeIngredient` | `PantryItem`: đang **có**. `RecipeIngredient`: cần **dùng** |
| `Recipe` vs `SavedRecipe` | `Recipe`: nội dung công thức. `SavedRecipe`: việc người dùng lưu nó + ghi chú, sao, yêu thích |
| `ShoppingItem` vs `PantryItem` | `ShoppingItem`: cần mua. Mua xong chuyển thành `PantryItem` |
| `Cuisine` / `IngredientCategory` vs enum | Entity tra cứu: thêm giá trị bằng dữ liệu trên service. Enum: tập cố định trong code |
