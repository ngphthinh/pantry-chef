# pantry-chef: hướng dẫn phát triển feature theo mẫu `auth`

**Yêu cầu**

- JDK 21
- Maven 3.10+
- Docker (chỉ để chạy PostgreSQL + Redis, bắt buộc)
- [Quy tắc commit code](docs/tuan02.md)
- [Tài liệu về cách class](docs/y-nghia-cac-class.md)
- [Class diagram](docs/class-diagram.puml)
- [Endpoints](docs/endpoints.md)
- [Phân công thành viên](docs/phan-cong.md)

Tài liệu này rút ra từ code hiện có (feature `auth` + package `common`) để mọi feature mới (`pantry`, `recipe`,
`cookbook`, `mealplan`, `shopping`, `ingredient`, `ai`) viết cùng một kiểu.

Phần ví dụ ở mục 7 chưa được biên dịch (môi trường đọc code không có Maven). Khi chép vào project, build thử và sửa lỗi
import nếu có.

---

## 1. Stack và cách chạy

| Hạng mục             | Công nghệ (theo `pom.xml`)                                                                                          |
|----------------------|---------------------------------------------------------------------------------------------------------------------|
| Ngôn ngữ / framework | Java 21, Spring Boot 4.0.8 (web, validation, data-jpa, security, oauth2-resource-server, websocket, actuator, mail) |
| CSDL                 | PostgreSQL (driver `postgresql`), `ddl-auto` mặc định `create`                                                      |
| Cache / token        | Redis (`spring-boot-starter-data-redis`)                                                                            |
| Mapper               | MapStruct 1.6.3 (`componentModel=spring`), Lombok 1.18.34 + `lombok-mapstruct-binding`                              |
| JWT                  | Nimbus JOSE JWT 9.48, thuật toán HS512                                                                              |
| ID                   | `uuid-creator` (UUIDv7, sắp xếp theo thời gian)                                                                     |
| Upload               | Cloudinary                                                                                                          |
| Tài liệu API         | springdoc-openapi (Swagger UI)                                                                                      |

Chạy local:

```bash
docker compose up -d          # postgres:17 + redis:7
mvn spring-boot:run           # profile mặc định: dev (project chưa có mvnw)
```

- Context path là `/api`, nên endpoint đầy đủ có dạng `/api/v1/...`.
- Biến môi trường (xem `application.properties`): `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASS`, `JWT_SECRET`,
  `REDIS_HOST`, `REDIS_PASSWORD`, `CLOUDINARY_CLOUD_NAME`, `CLOUDINARY_API_KEY`, `CLOUDINARY_API_SECRET`,
  `CORS_ALLOWED_ORIGINS`.
- `REDIS_PASSWORD` và các biến `CLOUDINARY_*` không có giá trị mặc định trong `application.properties`. `CLOUDINARY_*`
  được đọc bằng `@Value`, nên thiếu biến sẽ báo lỗi placeholder khi khởi động. Riêng `REDIS_PASSWORD`, nếu không đặt thì
  Spring có thể dùng nguyên chuỗi `${REDIS_PASSWORD}` làm mật khẩu và Redis (docker-compose không đặt mật khẩu) sẽ từ
  chối. Cách xử lý: đặt biến môi trường, hoặc thêm mặc định rỗng (`${REDIS_PASSWORD:}`) cho dev. Chưa chạy thử project
  để xác nhận hành vi này.
- `ddl-auto=create` xóa và tạo lại bảng mỗi lần chạy. Muốn giữ dữ liệu dev: `SPRING_JPA_HIBERNATE_DDL_AUTO=update`.
  Production nên dùng Flyway/Liquibase thay vì `ddl-auto`.
- Dữ liệu khởi tạo (role, tài khoản admin) nằm ở `common/config/InitData.java`, chỉ chạy ở profile `dev`.

---

## 2. Cấu trúc package

```
org.group12
├── PantryChefApplication
├── common                        # dùng chung, KHÔNG chứa nghiệp vụ của feature
│   ├── config                    # BeanManager, JpaConfig, AsyncConfig, CloudinaryConfig, InitData
│   ├── dto/response              # ApiResponse, ErrorResponse, CursorResponse, UploadMediaResponse
│   ├── entity                    # BaseEntity, SoftDeletableEntity
│   ├── enums                     # ResponseCode, CommonResponseCode, MediaType, Unit
│   ├── exception                 # AppException, BaseErrorCode, ErrorCode, GlobalExceptionHandler, ...
│   ├── security/rest             # SecurityConfig, CustomJwtDecoder, handler 401/403
│   ├── security/ws, ws           # WebSocket + DomainEvent/EventHandler
│   ├── service (+ impl)          # MediaStorageService, CloudinaryMediaStorageService
│   └── utils                     # AppUtil, MessageUtil
└── feature
    └── <feature>
        ├── controller
        ├── dto
        │   ├── request
        │   └── response
        ├── entity
        ├── enums                 # enum nghiệp vụ + <Feature>ResponseCode
        ├── exception             # <Thing>NotFoundException... + ErrorCode<Feature>
        ├── mapper
        ├── repository
        └── service               # (+ impl nếu cần nhiều cài đặt)
```

Luồng một request:

```
Client -> SecurityConfig (JWT) -> Controller (@Valid DTO) -> Service (@Transactional)
       -> Repository (JPA) / Mapper (MapStruct) -> ApiResponse<DTO>
Lỗi ở bất kỳ tầng nào: throw AppException -> GlobalExceptionHandler -> ErrorResponse
```

Quy tắc phụ thuộc:

- `controller` chỉ gọi `service`; `service` gọi `repository`, `mapper`, service khác.
- Controller không bao giờ nhận hoặc trả entity, luôn dùng DTO.
- Feature này đọc entity của feature khác bằng repository của feature đó (đọc đơn giản) hoặc service của feature đó (khi
  có logic). Không chui vào package `controller` hay `dto` của feature khác.
- `common` không phụ thuộc ngược vào `feature`

---

## 3. Quy tắc đặt tên

| Thành phần             | Quy tắc                                                                            | Ví dụ (có trong code)                              | Ví dụ cho feature mới                                |
|------------------------|------------------------------------------------------------------------------------|----------------------------------------------------|------------------------------------------------------|
| Package                | chữ thường, số ít, không gạch                                                      | `feature.auth`, `feature.mealplan`                 | `feature.pantry`                                     |
| Entity                 | danh từ số ít, PascalCase                                                          | `User`, `Role`                                     | `PantryItem`                                         |
| Bảng                   | snake_case, số nhiều (`@Table(name = ...)`)                                        | `users`, `roles`                                   | `pantry_items`                                       |
| Cột                    | camelCase trong entity, tự đổi snake_case (`CamelCaseToUnderscoresNamingStrategy`) | `displayName` -> `display_name`                    | `expiryDate` -> `expiry_date`                        |
| Controller             | `<Entity>Controller`                                                               | `AuthenticationController`                         | `PantryItemController`                               |
| Service                | `<Entity>Service`                                                                  | `AuthenticationService`                            | `PantryItemService`                                  |
| Repository             | `<Entity>Repository`                                                               | `UserRepository`                                   | `PantryItemRepository`                               |
| Mapper                 | `<Entity>Mapper`                                                                   | `UserMapper`                                       | `PantryItemMapper`                                   |
| Request DTO            | `<Entity><Action>Request` (hoặc `<Action>Request` khi không gắn entity)            | `UserCreateRequest`, `LoginRequest`                | `PantryItemCreateRequest`, `PantryItemUpdateRequest` |
| Response DTO           | `<Entity>Response`                                                                 | `UserResponse`                                     | `PantryItemResponse`                                 |
| Enum nghiệp vụ         | PascalCase, giá trị IN_HOA                                                         | `RoleName.ROLE_USER`                               | `StorageLocation.FRIDGE`                             |
| Mã phản hồi thành công | `<Feature>ResponseCode`                                                            | `AuthResponseCode`                                 | `PantryResponseCode`                                 |
| Mã lỗi theo feature    | `ErrorCode<Feature>`                                                               | `ErrorCodeAuth`                                    | `ErrorCodePantry`                                    |
| Exception              | `<Thing>NotFoundException`, `<Thing>DuplicateException`                            | `UserNotFoundException`, `EmailDuplicateException` | `PantryItemNotFoundException`                        |
| URL                    | `/v1/<tài-nguyên-số-nhiều-kebab-case>`                                             | `/v1/auth/login`                                   | `/v1/pantry-items`                                   |
| Phương thức service    | động từ + đối tượng                                                                | `register`, `authenticate`, `refreshToken`         | `create`, `update`, `delete`, `getMyItems`           |
| Phương thức mapper     | `toXxx`, `updateXxx`                                                               | `toUserResponse`, `toUser`                         | `toResponse`, `toEntity`, `updateEntity`             |
| Phương thức repository | theo Spring Data                                                                   | `findByEmail`, `existsByEmail`                     | `findByIdAndUserId`                                  |
| Hằng số                | `UPPER_SNAKE_CASE`                                                                 | `SCOPE_CLAIM`                                      | `MAX_PAGE_SIZE`                                      |
| Redis key              | prefix kết thúc bằng `:`                                                           | `refresh:`                                         | `pantry:expiring:`                                   |

Mã lỗi/mã phản hồi: dạng `<FEATURE>_<số>`, duy nhất toàn project (không trùng giữa hai enum).

---

## 4. Từng thành phần theo mẫu `auth`

### 4.1 Entity

Mẫu: `auth/entity/User.java`.

```java

@Entity
@Table(name = "pantry_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PantryItem extends SoftDeletableEntity {   // BaseEntity nếu không cần xóa mềm
    @Id
    private UUID id;                                    // KHÔNG dùng @GeneratedValue, service tự gán
    ...
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;
}
```

- Kế thừa `BaseEntity` (`createdAt`, `updatedAt` tự cập nhật bằng `@CreationTimestamp` / `@UpdateTimestamp`) hoặc
  `SoftDeletableEntity` (thêm `deletedAt`).
- `id` là UUIDv7, sinh bằng `appUtil.generateUUID()` trong service. Không dùng `UUID.randomUUID()` cho khóa chính, vì
  phân trang cursor ở mục 4.8 dựa vào việc ID tăng theo thời gian.
- Quan hệ luôn `FetchType.LAZY`. Cần dữ liệu liên quan khi liệt kê thì dùng `@EntityGraph` hoặc `JOIN FETCH` trong
  repository.
- Enum lưu dạng chữ: `@Enumerated(EnumType.STRING)`.
- Entity xóa mềm cần cả hai annotation (đổi tên bảng cho đúng):

```java
@SQLDelete(sql = "UPDATE pantry_items SET deleted_at = now() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
```

- Không để logic nghiệp vụ, không để Jackson annotation trong entity.

### 4.2 DTO

Mẫu: `auth/dto/request/LoginRequest.java`, `auth/dto/response/UserResponse.java`.

```java

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class PantryItemCreateRequest {
    @NotNull(message = "Ingredient is required")
    private UUID ingredientId;

    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be greater than 0")
    private Double quantity;
    ...
}
```

- Request: `@Data @Builder @NoArgsConstructor @AllArgsConstructor` + annotation `jakarta.validation` kèm `message` tiếng
  Anh rõ ràng (kiểu `LoginRequest`: `"Email is required"`). Không dùng mã i18n kiểu `error.accesstoken.required` vì chưa
  có file message tương ứng.
- Response: cùng bộ annotation; không bao giờ chứa `password` hay trường nội bộ (xem `UserResponse`).
- `UserCreateRequest` hiện chưa có validation: đừng sao chép phần đó, hãy thêm `@NotBlank`, `@Email`, `@Size`.
- DTO dùng kiểu thật (`UUID`, `LocalDate`, enum) thay vì `String`, để Jackson và `GlobalExceptionHandler` tự báo lỗi
  định dạng.

### 4.3 Mapper

Mẫu: `auth/mapper/UserMapper.java`.

```java

@Mapper(componentModel = "spring")
public interface PantryItemMapper {
    @Mapping(target = "ingredientId", source = "ingredient.id")
    @Mapping(target = "ingredientName", source = "ingredient.name")
    PantryItemResponse toPantryItemResponse(PantryItem item);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "ingredient", ignore = true)
    @Mapping(target = "syncStatus", ignore = true)
    @Mapping(target = "photoUri", ignore = true)
    PantryItem toPantryItem(PantryItemCreateRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "ingredient", ignore = true)
    @Mapping(target = "syncStatus", ignore = true)
    @Mapping(target = "photoUri", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    void updatePantryItemEntity(PantryItemUpdateRequest request, @MappingTarget PantryItem item);
}
```

- Mapper là interface, `componentModel = "spring"`, inject bằng constructor như bean thường.
- Mapper chỉ chuyển đổi dữ liệu. Những gì cần truy vấn (người dùng hiện tại, entity liên quan, id mới) thì service gán
  sau khi map, giống `register()` đang làm với `setId`, `setRoles`, `setPassword`.
- Trường quan hệ và trường hệ thống (`id`, `user`, ...) khai báo `ignore = true` để không còn cảnh báo "unmapped target
  property".
- Entity dùng `@Builder`, nên với `toEntity` MapStruct gọi builder: không khai báo `ignore` cho `createdAt`/`updatedAt`
  ở method này (builder không có các trường đó). Method `updateEntity` dùng setter nên khai báo được.

### 4.4 Repository

Mẫu: `auth/repository/UserRepository.java`.

```java
public interface PantryItemRepository extends JpaRepository<PantryItem, UUID> {

    Optional<PantryItem> findByIdAndUserId(UUID id, UUID userId);

    @EntityGraph(attributePaths = "ingredient")
    List<PantryItem> findByUserIdOrderByIdDesc(UUID userId, Pageable pageable);

    @EntityGraph(attributePaths = "ingredient")
    List<PantryItem> findByUserIdAndIdLessThanOrderByIdDesc(UUID userId, UUID cursor, Pageable pageable);
}
```

- `extends JpaRepository<Entity, UUID>`. Ưu tiên query theo tên phương thức; câu phức tạp mới dùng `@Query` (JPQL, text
  block).
- Luôn lọc theo chủ sở hữu (`findByIdAndUserId`) cho dữ liệu cá nhân để người này không đọc/sửa dữ liệu người khác.
- Hai phương thức trang đầu và trang kế tiếp tách riêng, vì với PostgreSQL việc truyền tham số UUID `null` trong
  `(:cursor IS NULL OR ...)` dễ gây lỗi kiểu dữ liệu.

### 4.5 Exception và mã lỗi

Mẫu: `common/exception/*`, `auth/exception/*`.

Cơ chế:

```
BaseErrorCode (interface: getMessage, getHttpStatus)
  ├── ErrorCode        (common: lỗi dùng chung, xác thực, validation)
  └── ErrorCode<Feature>   (mỗi feature một enum riêng)
AppException(BaseErrorCode)                 <- cha của mọi lỗi nghiệp vụ
  └── <Thing>NotFoundException, <Thing>DuplicateException, ...
GlobalExceptionHandler  -> ErrorResponse {timestamp, status, error, message, path, errors?}
```

Tạo lỗi mới cho feature `pantry` gồm hai bước:

```java
// pantry/exception/ErrorCodePantry.java
@Getter
public enum ErrorCodePantry implements BaseErrorCode {
    PANTRY_ITEM_NOT_FOUND("PANTRY_404", "Pantry item not found", HttpStatus.NOT_FOUND);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;

    ErrorCodePantry(String code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }
}

// pantry/exception/PantryItemNotFoundException.java
public class PantryItemNotFoundException extends AppException {
    public PantryItemNotFoundException() {
        super(ErrorCodePantry.PANTRY_ITEM_NOT_FOUND);
    }
}
```

Quy tắc:

- Ném `throw new PantryItemNotFoundException()`; không ném `RuntimeException`, không trả `null`, không tự build
  `ResponseEntity` lỗi trong controller.
- Lỗi chỉ dùng trong một feature: đặt vào `ErrorCode<Feature>`. Lỗi dùng chung nhiều nơi (xác thực, validation, upload):
  đặt vào `ErrorCode` của `common`.
- Cần chèn giá trị vào thông báo (ví dụ `"... '{parameter}'"`): ném
  `ParameterizedException(errorCode, Map.of("parameter", ...))`.
- Dữ liệu của người khác: trả `NotFound` thay vì `Forbidden` để không lộ sự tồn tại của bản ghi.
- Không `try/catch` rồi nuốt lỗi; chỉ catch khi cần chuyển đổi (như `ParseException` -> `UnauthenticatedException` trong
  `logout`).
- Lỗi validate `@Valid` đã được `GlobalExceptionHandler` gom vào `errors` theo từng trường, không cần xử lý thêm.

### 4.6 Mã phản hồi thành công

Mẫu: `auth/enums/AuthResponseCode.java`.

```java

@Getter
@RequiredArgsConstructor
public enum PantryResponseCode implements ResponseCode {
    PANTRY_ITEM_CREATED("PANTRY_201", "Pantry item created successfully"),
    PANTRY_ITEM_UPDATED("PANTRY_200", "Pantry item updated successfully"),
    PANTRY_ITEM_DELETED("PANTRY_200", "Pantry item deleted successfully"),
    PANTRY_ITEMS_FETCHED("PANTRY_200", "Pantry items fetched successfully");

    private final String code;
    private final String message;
}
```

Controller bọc kết quả trong `ApiResponse<T>` với code và message lấy từ enum này (mục 4.9). Không viết chuỗi message
trực tiếp trong controller.

### 4.7 Service

Mẫu: `auth/service/AuthenticationService.java`.

```java

@Slf4j
@Service
@RequiredArgsConstructor
public class PantryItemService {
    private final PantryItemRepository pantryItemRepository;
    private final PantryItemMapper pantryItemMapper;
    private final AppUtil appUtil;

    @Transactional                    // ghi
    public PantryItemResponse create(PantryItemCreateRequest request) { ...}

    @Transactional(readOnly = true)   // đọc
    public CursorResponse<PantryItemResponse> getMyItems(UUID cursor, int size) { ...}
}
```

- Annotation: `@Service @RequiredArgsConstructor @Slf4j`. Phụ thuộc là `private final` (Lombok sinh constructor), không
  dùng `@Autowired` trên field.
- `@Transactional` đặt ở service (không ở controller, không ở repository); ghi: `@Transactional`; đọc:
  `@Transactional(readOnly = true)`.
- Thứ tự trong một method: lấy người dùng hiện tại -> kiểm tra điều kiện/trùng lặp -> map -> gán các trường hệ thống
  (`id`, `user`, ...) -> lưu -> map ra response.
- Lấy người dùng hiện tại: `appUtil.userIdFromAuthentication()` (trả `UUID` từ claim `sub` của JWT). Không nhận `userId`
  từ request body hay path cho dữ liệu cá nhân.
- Nhận `Entity` ngoài service chỉ khi cùng feature; trả ra ngoài luôn là DTO.
- Phân quyền theo vai trò: `@PreAuthorize("hasRole('ADMIN')")` (đã bật bằng `@EnableMethodSecurity`). Vai trò hiện có:
  `ROLE_USER`, `ROLE_ADMIN`.
- Log bằng `log.info("... {}", value)` với placeholder; không nối chuỗi, không log mật khẩu/token.
- Một method làm một việc; method quá dài thì tách `private` helper (như `buildAuthResponse`,
  `getSecondsUntilExpiration`).

### 4.8 Phân trang

Dùng `CursorResponse<T>` và `AppUtil.buildCursorResponse(...)` (đã có sẵn), không tự tạo `Page` hay kiểu phân trang
khác.

```java
private static final int MAX_PAGE_SIZE = 50;

@Transactional(readOnly = true)
public CursorResponse<PantryItemResponse> getMyItems(UUID cursor, int size) {
    UUID userId = appUtil.userIdFromAuthentication();
    int limit = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
    Pageable pageable = PageRequest.of(0, limit + 1);          // lấy dư 1 để biết còn trang sau

    List<PantryItem> items = cursor == null
            ? pantryItemRepository.findByUserIdOrderByIdDesc(userId, pageable)
            : pantryItemRepository.findByUserIdAndIdLessThanOrderByIdDesc(userId, cursor, pageable);

    return appUtil.buildCursorResponse(items, limit, PantryItem::getId, pantryItemMapper::toResponse);
}
```

- Client gửi `?cursor=<nextCursor>&size=20`; response có `content`, `hasNext`, `nextCursor`.
- Giới hạn `size` ngay trong service. Không dùng `@Min/@Max` trên `@RequestParam`: `GlobalExceptionHandler` chưa có
  handler cho `ConstraintViolationException` nên lỗi đó sẽ thành 500.
- Sắp xếp theo `id DESC` chỉ đúng thứ tự thời gian khi mọi `id` sinh từ `appUtil.generateUUID()`.

### 4.9 Controller

Mẫu: `auth/controller/AuthenticationController.java`.

```java

@RestController
@RequestMapping("/v1/pantry-items")
@RequiredArgsConstructor
public class PantryItemController {

    private final PantryItemService pantryItemService;

    // POST /v1/pantry-items
    @PostMapping
    public ApiResponse<PantryItemResponse> create(@Valid @RequestBody PantryItemCreateRequest request) {
        return ApiResponse.<PantryItemResponse>builder()
                .code(PantryResponseCode.PANTRY_ITEM_CREATED.getCode())
                .message(PantryResponseCode.PANTRY_ITEM_CREATED.getMessage())
                .data(pantryItemService.create(request))
                .build();
    }
}
```

- Controller mỏng: `@Valid` -> gọi service -> bọc `ApiResponse`. Không có `if`, không `try/catch`, không truy cập
  repository.
- Mọi response thành công là `ApiResponse<T>`; trả không dữ liệu thì dùng `ApiResponse<Void>` như `logout`.
- Ghi chú một dòng URL phía trên mỗi method (`// POST /v1/auth/login`) như file mẫu.
- Path variable là `UUID`: id sai định dạng sẽ được `GlobalExceptionHandler` chuyển thành 400.

### 4.10 Bảo mật

Mẫu: `common/security/rest/SecurityConfig.java`.

- Mặc định mọi endpoint cần JWT (`anyRequest().authenticated()`). Feature mới không cần sửa gì.
- Endpoint cho khách (không đăng nhập): thêm đường dẫn vào `PUBLIC_ENDPOINTS_GET` / `PUBLIC_ENDPOINTS_POST` (ví dụ danh
  mục `Cuisine`, `IngredientCategory`).
- Token có claim `scope` chứa tên role (`ROLE_USER ROLE_ADMIN`), không có tiền tố `SCOPE_`, nên `hasRole('ADMIN')` hoạt
  động.
- Mỗi request đi qua `CustomJwtDecoder` (gọi `introspect`: kiểm tra chữ ký, hạn, blacklist trong Redis).

### 4.11 Upload ảnh

Dùng `common/service/MediaStorageService`, không gọi Cloudinary trực tiếp.

```java
UploadMediaResponse media = mediaStorageService.uploadSync(file, "pantry-chef/pantry-items", MediaType.IMAGE);
item.

setPhotoUri(media.getUrl());
// khi thay/xóa ảnh: mediaStorageService.delete(publicId);
```

Muốn xóa được ảnh cũ thì phải lưu thêm `publicId` (hiện `PantryItem`, `Recipe` mới có `photoUri`/`imageUrl`; thêm cột
`photoPublicId` khi làm chức năng này). Lỗi upload đã có `UploadMediaException`.

### 4.12 Sự kiện / WebSocket (tùy chọn)

Khi cần đẩy thông báo thời gian thực: tạo `class XxxEvent implements DomainEvent`, publish bằng
`ApplicationEventPublisher`, và viết `@Component class XxxEventHandler implements EventHandler<XxxEvent>`.
`GlobalEventListener` chạy sau commit và `EventDispatcher` chọn handler theo class của event.

---

## 5. Checklist tạo một feature mới

1. Entity đã có trong `feature/<tên>/entity`; kiểm tra đúng cha (`BaseEntity` / `SoftDeletableEntity`) và `@SQLDelete` +
   `@SQLRestriction` nếu xóa mềm.
2. `enums/<Feature>ResponseCode`.
3. `exception/ErrorCode<Feature>` + các `<Thing>NotFoundException` / `<Thing>DuplicateException`.
4. `dto/request/*` (có validation) và `dto/response/*`.
5. `mapper/<Entity>Mapper`.
6. `repository/<Entity>Repository` (có điều kiện chủ sở hữu).
7. `service/<Entity>Service` (`@Transactional`, gán `id` bằng `appUtil.generateUUID()`).
8. `controller/<Entity>Controller` (`/v1/...`, `ApiResponse`).
9. Nếu có endpoint công khai: cập nhật `SecurityConfig`.
10. Nếu có dữ liệu danh mục (lookup): thêm seed vào `InitData` (kiểu `if (repo.count() == 0)`).
11. Chạy Swagger (`/api/swagger-ui.html`) thử đủ: thành công, thiếu trường, sai id, không có quyền, dữ liệu của người
    khác.
12. Viết test cho service (mock repository) và controller (`@WebMvcTest`).

---

## 6. Quy tắc clean code chung

- Mỗi class một trách nhiệm; method ngắn, tên động từ rõ nghĩa; tránh viết tắt (`req` chỉ chấp nhận trong tham số cục bộ
  rất ngắn).
- Không "magic string/number": đưa vào hằng số hoặc enum (`MAX_PAGE_SIZE`, `ErrorCode...`, `ResponseCode...`).
- Dùng `Optional.orElseThrow(Exception::new)` thay vì `get()` hoặc kiểm tra `null`.
- Dùng `List.of`, `Set.of`, stream gọn; không trả `null` cho tập hợp, trả rỗng.
- Thời gian: `Instant` cho dấu thời gian hệ thống (`createdAt`), `LocalDate` cho ngày nghiệp vụ (`expiryDate`,
  `weekStart`). DB/JVM chạy UTC (`hibernate.jdbc.time_zone=UTC`).
- Không đưa secret vào code hay file properties dùng chung; chỉ để giá trị mặc định cho dev.
- Import tường minh, không `import xxx.*` cho class của project (Lombok/JPA wildcard như trong file mẫu thì chấp nhận
  được); xóa import thừa.
- Một commit một thay đổi; tên nhánh `feature/<tên>`; commit message mô tả hành động ("Add pantry item CRUD").

---

## 7. Ví dụ hoàn chỉnh: feature `pantry`

Gồm 4 endpoint, đều cần đăng nhập, chỉ thao tác trên dữ liệu của chính người dùng:

| Method | Path                                 | Mô tả                         |
|--------|--------------------------------------|-------------------------------|
| POST   | `/api/v1/pantry-items`               | Thêm nguyên liệu vào tủ       |
| GET    | `/api/v1/pantry-items?cursor=&size=` | Danh sách (phân trang cursor) |
| PUT    | `/api/v1/pantry-items/{id}`          | Sửa                           |
| DELETE | `/api/v1/pantry-items/{id}`          | Xóa mềm                       |

Giả định feature `ingredient` đã có `IngredientRepository` và `IngredientNotFoundException` (làm theo checklist mục 5).

**`pantry/enums/PantryResponseCode.java`**

```java
package org.group12.feature.pantry.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.group12.common.enums.ResponseCode;

@Getter
@RequiredArgsConstructor
public enum PantryResponseCode implements ResponseCode {
    PANTRY_ITEM_CREATED("PANTRY_201", "Pantry item created successfully"),
    PANTRY_ITEMS_FETCHED("PANTRY_200", "Pantry items fetched successfully"),
    PANTRY_ITEM_UPDATED("PANTRY_200", "Pantry item updated successfully"),
    PANTRY_ITEM_DELETED("PANTRY_200", "Pantry item deleted successfully");

    private final String code;
    private final String message;
}
```

**`pantry/exception/ErrorCodePantry.java`** và **`PantryItemNotFoundException.java`**: như mục 4.5.

**`pantry/dto/request/PantryItemCreateRequest.java`**

```java
package org.group12.feature.pantry.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;
import org.group12.common.enums.Unit;
import org.group12.feature.pantry.enums.StorageLocation;

import java.time.LocalDate;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class PantryItemCreateRequest {
    @NotNull(message = "Ingredient is required")
    private UUID ingredientId;

    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be greater than 0")
    private Double quantity;

    @NotNull(message = "Unit is required")
    private Unit unit;

    private StorageLocation location;
    private LocalDate expiryDate;
}
```

**`pantry/dto/request/PantryItemUpdateRequest.java`**: giống bản tạo mới nhưng bỏ `ingredientId` (không đổi nguyên liệu
của một món đã có).

**`pantry/dto/response/PantryItemResponse.java`**

```java
package org.group12.feature.pantry.dto.response;

import lombok.*;
import org.group12.common.enums.Unit;
import org.group12.feature.pantry.enums.StorageLocation;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class PantryItemResponse {
    private UUID id;
    private UUID ingredientId;
    private String ingredientName;
    private Double quantity;
    private Unit unit;
    private StorageLocation location;
    private LocalDate expiryDate;
    private String photoUri;
    private Instant createdAt;
    private Instant updatedAt;
}
```

**`pantry/mapper/PantryItemMapper.java`**: như mục 4.3.

**`pantry/repository/PantryItemRepository.java`**: như mục 4.4.

**`pantry/service/PantryItemService.java`**

```java
package org.group12.feature.pantry.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.group12.common.dto.response.CursorResponse;
import org.group12.common.utils.AppUtil;
import org.group12.feature.auth.repository.UserRepository;
import org.group12.feature.ingredient.entity.Ingredient;
import org.group12.feature.ingredient.exception.IngredientNotFoundException;
import org.group12.feature.ingredient.repository.IngredientRepository;
import org.group12.feature.pantry.dto.request.PantryItemCreateRequest;
import org.group12.feature.pantry.dto.request.PantryItemUpdateRequest;
import org.group12.feature.pantry.dto.response.PantryItemResponse;
import org.group12.feature.pantry.entity.PantryItem;
import org.group12.feature.pantry.exception.PantryItemNotFoundException;
import org.group12.feature.pantry.mapper.PantryItemMapper;
import org.group12.feature.pantry.repository.PantryItemRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PantryItemService {

    private static final int MAX_PAGE_SIZE = 50;

    private final PantryItemRepository pantryItemRepository;
    private final IngredientRepository ingredientRepository;
    private final UserRepository userRepository;
    private final PantryItemMapper pantryItemMapper;
    private final AppUtil appUtil;

    @Transactional
    public PantryItemResponse create(PantryItemCreateRequest request) {
        UUID userId = appUtil.userIdFromAuthentication();

        Ingredient ingredient = ingredientRepository.findById(request.getIngredientId())
                .orElseThrow(IngredientNotFoundException::new);

        PantryItem item = pantryItemMapper.toEntity(request);
        item.setId(appUtil.generateUUID());
        item.setUser(userRepository.getReferenceById(userId));
        item.setIngredient(ingredient);

        item = pantryItemRepository.save(item);
        log.info("User {} added pantry item {}", userId, item.getId());
        return pantryItemMapper.toResponse(item);
    }

    @Transactional(readOnly = true)
    public CursorResponse<PantryItemResponse> getMyItems(UUID cursor, int size) {
        UUID userId = appUtil.userIdFromAuthentication();
        int limit = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        Pageable pageable = PageRequest.of(0, limit + 1);

        List<PantryItem> items = cursor == null
                ? pantryItemRepository.findByUserIdOrderByIdDesc(userId, pageable)
                : pantryItemRepository.findByUserIdAndIdLessThanOrderByIdDesc(userId, cursor, pageable);

        return appUtil.buildCursorResponse(items, limit, PantryItem::getId, pantryItemMapper::toResponse);
    }

    @Transactional
    public PantryItemResponse update(UUID id, PantryItemUpdateRequest request) {
        PantryItem item = findOwnedItem(id);
        pantryItemMapper.updateEntity(request, item);   // dirty checking tự ghi, không cần save()
        return pantryItemMapper.toResponse(item);
    }

    @Transactional
    public void delete(UUID id) {
        pantryItemRepository.delete(findOwnedItem(id)); // @SQLDelete -> xóa mềm
    }

    private PantryItem findOwnedItem(UUID id) {
        UUID userId = appUtil.userIdFromAuthentication();
        return pantryItemRepository.findByIdAndUserId(id, userId)
                .orElseThrow(PantryItemNotFoundException::new);
    }
}
```

**`pantry/controller/PantryItemController.java`**

```java
package org.group12.feature.pantry.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.group12.common.dto.response.ApiResponse;
import org.group12.common.dto.response.CursorResponse;
import org.group12.feature.pantry.dto.request.PantryItemCreateRequest;
import org.group12.feature.pantry.dto.request.PantryItemUpdateRequest;
import org.group12.feature.pantry.dto.response.PantryItemResponse;
import org.group12.feature.pantry.enums.PantryResponseCode;
import org.group12.feature.pantry.service.PantryItemService;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/v1/pantry-items")
@RequiredArgsConstructor
public class PantryItemController {

    private final PantryItemService pantryItemService;

    // POST /v1/pantry-items
    @PostMapping
    public ApiResponse<PantryItemResponse> create(@Valid @RequestBody PantryItemCreateRequest request) {
        return ApiResponse.<PantryItemResponse>builder()
                .code(PantryResponseCode.PANTRY_ITEM_CREATED.getCode())
                .message(PantryResponseCode.PANTRY_ITEM_CREATED.getMessage())
                .data(pantryItemService.create(request))
                .build();
    }

    // GET /v1/pantry-items?cursor=&size=
    @GetMapping
    public ApiResponse<CursorResponse<PantryItemResponse>> list(
            @RequestParam(required = false) UUID cursor,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.<CursorResponse<PantryItemResponse>>builder()
                .code(PantryResponseCode.PANTRY_ITEMS_FETCHED.getCode())
                .message(PantryResponseCode.PANTRY_ITEMS_FETCHED.getMessage())
                .data(pantryItemService.getMyItems(cursor, size))
                .build();
    }

    // PUT /v1/pantry-items/{id}
    @PutMapping("/{id}")
    public ApiResponse<PantryItemResponse> update(@PathVariable UUID id,
                                                  @Valid @RequestBody PantryItemUpdateRequest request) {
        return ApiResponse.<PantryItemResponse>builder()
                .code(PantryResponseCode.PANTRY_ITEM_UPDATED.getCode())
                .message(PantryResponseCode.PANTRY_ITEM_UPDATED.getMessage())
                .data(pantryItemService.update(id, request))
                .build();
    }

    // DELETE /v1/pantry-items/{id}
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable UUID id) {
        pantryItemService.delete(id);
        return ApiResponse.<Void>builder()
                .code(PantryResponseCode.PANTRY_ITEM_DELETED.getCode())
                .message(PantryResponseCode.PANTRY_ITEM_DELETED.getMessage())
                .build();
    }
}
```

---

## 8. Kế hoạch các feature còn lại

Entity của các feature này đã có sẵn. Mỗi feature làm đủ checklist mục 5. Cột "Endpoint" là phiên bản tối thiểu; danh
sách đầy đủ xem tài liệu endpoint đã thống nhất.

| Feature                 | Entity                                                   | Endpoint chính (`/api/v1/...`)                                                                            | Lưu ý riêng                                                                                                                                                                                                                                                               |
|-------------------------|----------------------------------------------------------|-----------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `ingredient`            | `Ingredient`, `IngredientCategory`, `ExcludedIngredient` | `GET /ingredients/search`, `GET /ingredient-categories`, `GET/POST/DELETE /users/me/excluded-ingredients` | `IngredientCategory` là bảng tra cứu: seed trong `InitData`, đọc công khai. `Ingredient` thêm/sửa/xóa chỉ ADMIN (`@PreAuthorize`)                                                                                                                                         |
| `sync` (hiện là `sysn`) | `SyncStatus`                                             | `GET /meta/sync-statuses` (nếu client cần)                                                                | Bảng tra cứu, seed trong `InitData`. Đổi tên package `sysn` thành `sync`                                                                                                                                                                                                  |
| `pantry`                | `PantryItem`                                             | CRUD `/pantry-items`, `GET /pantry-items/expiring?days=`, `POST /pantry-items/{id}/photo`                 | Mục 7. Ảnh qua `MediaStorageService`                                                                                                                                                                                                                                      |
| `recipe`                | `Recipe`, `RecipeIngredient`, `RecipeStep`, `Cuisine`    | `GET /recipes/search`, `GET /recipes/{externalId}`, CRUD `/recipes/custom`                                | Gọi API công thức bên ngoài: tạo `recipe/client/RecipeProviderClient` (interface) + `SpoonacularClient` (dùng `RestClient`), khóa API đặt trong properties qua biến môi trường. Có thể cache kết quả bằng `@Cacheable` (cần bật `@EnableCaching` và cấu hình cache Redis) |
| `cookbook`              | `SavedRecipe`, `Collection`                              | `/saved-recipes`, `/collections`                                                                          | Phân trang cursor cho danh sách. Kiểm tra trùng (user, recipe) trong service                                                                                                                                                                                              |
| `mealplan`              | `MealPlan`, `MealPlanEntry`                              | `/meal-plans`, `/meal-plans/{id}/entries`, `.../shopping-list`                                            | `weekStart` là `LocalDate`. Mỗi `MealPlan` thuộc một `User`                                                                                                                                                                                                               |
| `shopping`              | `ShoppingList`, `ShoppingItem`                           | `/shopping-lists`, `/shopping-lists/{id}/items`, `POST /shopping-lists/{id}/move-to-pantry`               | `move-to-pantry` ghi sang `pantry`: gọi `PantryItemService`, không ghi thẳng repository của pantry                                                                                                                                                                        |
| `ai`                    | `SubstitutionSuggestion`                                 | `POST /ai/substitutions`, `POST /ai/recognize`, `POST /ai/meal-plan`                                      | Tạo `ai/client/LlmClient` (interface) + một cài đặt. Tạo `ContentGuard` chặn nội dung y tế (system prompt + lọc kết quả); vi phạm thì ném `AppException` với mã 422. Giới hạn kích thước ảnh (đã có `FILE_SIZE_EXCEEDED`) và rate limit                                   |

Thứ tự đề xuất: `ingredient` + `sync` (dữ liệu nền) -> `pantry` -> `recipe` -> `cookbook` -> `mealplan` -> `shopping` ->
`ai`.

---

## 9. Seed dữ liệu tra cứu

Thêm vào `InitData` cùng kiểu với `initRoles()`:

```java
private void initCuisines() {
    if (cuisineRepository.count() == 0) {
        List<Cuisine> cuisines = Stream.of("VIETNAMESE:Vietnamese", "ASIAN:Asian", "WESTERN:Western", "ITALIAN:Italian", "OTHER:Other")
                .map(s -> s.split(":"))
                .map(p -> Cuisine.builder().id(appUtil.generateUUID()).code(p[0]).name(p[1]).build())
                .toList();
        cuisineRepository.saveAll(cuisines);
    }
}
```

Làm tương tự cho `IngredientCategory` và `SyncStatus`, rồi gọi trong `run(...)`. `InitData` chỉ chạy ở profile `dev`;
production dùng migration SQL.

---

**Lưu ý**

- Bắt buộc dùng @PreAuthorize cho các endpoint cần phân quyền (ADMIN, USER). Không để lỗ hổng bảo mật.
- Không dùng @Autowired trên field; chỉ dùng constructor injection (Lombok @RequiredArgsConstructor).
- Không để logic nghiệp vụ trong entity, controller, hoặc mapper; chỉ service mới có logic nghiệp vụ.
- Tạo các exception riêng , không dùng chung exception AppException.
- Không dùng UUID.randomUUID () cho khóa chính; dùng appUtil.generateUUID () để tạo UUIDv7 tăng theo thời gian.
- Không để thông tin nhạy cảm (password, token) trong log.
- Const để trong application.properties hoặc enum, không để magic string/number trong code.
- Ưu tiên viết query bằng @Query thay vì viết logic trong service. Nếu query phức tạp, hãy viết JPQL hoặc native query
  trong repository.
- Tránh bị lỗi N+1 khi truy vấn dữ liệu liên quan; sử dụng @EntityGraph hoặc JOIN FETCH trong repository.
- Test api đầy đủ: thành công, thiếu trường, sai id, không có quyền, dữ liệu của người khác.
- Test api app Bruno: clone repo, open collections, tạo folder cùng tên feature, tạo request
- Đặt tên endpoints theo chuẩn RESTful, ví dụ: /v1/pantry-items, /v1/recipes/search, /v1/meal-plans/{id}/entries.
- Request Bruno: đặt tên folder theo feature, ví dụ: pantry, recipe, mealplan. Tạo request trong folder đó. Đặt tên request theo action, ví dụ: create-pantry-item, get-my-items, update-pantry-item, delete-pantry-item.

