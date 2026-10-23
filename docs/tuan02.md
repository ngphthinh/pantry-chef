# BÁO CÁO TUẦN 2: KẾ HOẠCH BÀI TẬP LỚN VÀ PHÂN CÔNG NHÓM
**Môn học:** Lập trình thiết bị di động (Mã học phần: 2101454)  
**Tên đề tài khung:** Mobile Solution Studio  
**Đề tài chọn (Số 10):** Tủ công thức từ nguyên liệu sẵn có (Smart Recipe & Pantry Assistant)  
**Nền tảng triển khai:** React Native (Expo, TypeScript)  
**Quy mô:** Nhóm 6 sinh viên  

---

## I. LỰA CHỌN CÔNG NGHỆ VÀ LÝ DO KỸ THUẬT

### 1. Nền tảng và Framework chính
- **React Native với Expo (SDK 54) & TypeScript:**
  - *Lý do:* Giúp phát triển nhanh, đồng bộ trải nghiệm trên iOS và Android; TypeScript cung cấp type safety chặt chẽ cho mô hình dữ liệu (Nguyên liệu, Công thức, AI Response).
- **Điều hướng (Navigation):**
  - **Expo Router:** Dùng file-based routing để tổ chức các màn hình Stack, Tabs trực quan, hỗ trợ cấu hình Deep Linking chuẩn xác cho tính năng chia sẻ công thức.

### 2. Quản lý trạng thái và Kết nối API
- **Quản lý trạng thái dùng chung (Global State):**
  - **Redux Toolkit (RTK):** Quản lý danh sách nguyên liệu trong tủ (Pantry), danh sách món ăn yêu thích/xem offline (Favorites/Cache). Giải trình: Dữ liệu tủ đồ và danh sách yêu thích cần chia sẻ phức tạp giữa nhiều màn hình (Màn hình Tủ lạnh, Màn hình Lọc công thức, Màn hình Chi tiết).
  - **Local State (React Hooks):** Dùng `useState`, `useReducer`, `useCallback`, `useMemo` cho các trạng thái màn hình tĩnh, form nhập liệu và trạng thái UI ngắn hạn.
- **Kết nối API Dữ liệu:**
  - **Axios:** Thực hiện gửi HTTP Request tới API công khai (*Spoonacular API* / *TheMealDB API*).
  - Tích hợp cơ chế Retry khi mất kết nối, hiển thị các trạng thái UI: `Loading`, `Error`, `Empty`.

### 3. Thư viện UI, Animation và Expo SDK
- **Giao diện (UI Framework):**
  - **NativeWind:** Sử dụng cú pháp Tailwind CSS cho React Native, giúp viết style nhanh, nhất quán, dễ maintain theme (Light/Dark hoặc "Warm Kitchen") thông qua config `tailwind.config.js` (màu sắc, spacing, typography dùng chung).
- **Animation & Cử chỉ:**
  - **React Native Reanimated & Gesture Handler:** Triển khai cử chỉ vuốt (`Swipeable`) để xóa/đã dùng nguyên liệu, hiệu ứng chuyển cảnh mượt giữa danh sách và chi tiết món ăn.
- **Expo SDK tích hợp:**
  - **Expo Camera:** Chụp ảnh nguyên liệu thực tế nhập vào tủ.
  - **Expo FileSystem:** Lưu đệm (cache) công thức nấu ăn và hình ảnh để phục vụ chế độ xem offline.

### 4. Tính năng AI
- **Cloud-based AI API (Gemini API / OpenAI API):**
  - Đảm nhận vai trò trợ lý tư vấn nguyên liệu thay thế khi thiếu. Bắt buộc kiểm soát timeout, trạng thái chờ và hiển thị cảnh báo dị ứng/an toàn thực phẩm.

### 5. Xử lý lỗi, Offline và Bảo mật

**5.1. Fallback khi AI API lỗi/hết quota**
- Khi gọi Gemini/OpenAI API thất bại (timeout, rate limit, hết quota):
  - Hiển thị thông báo rõ ràng: "Không thể lấy gợi ý AI lúc này, vui lòng thử lại sau" kèm nút **Retry**.
  - Giới hạn số lần retry tự động (VD: 1 lần), tránh gọi lặp gây tốn quota.
  - Fallback: hiển thị danh sách gợi ý thay thế nguyên liệu **tĩnh** (rule-based, lưu sẵn trong database/JSON cục bộ) cho các nguyên liệu phổ biến, để người dùng vẫn có gợi ý dù AI lỗi.
  - Log lỗi (loại lỗi, thời điểm) để theo dõi tần suất fail của AI API.

**5.2. Offline Mode & Persist dữ liệu**
- **Pantry list (Redux Toolkit)** phải được persist bằng **Redux Persist** kết hợp **AsyncStorage** (hoặc MMKV nếu cần hiệu năng cao hơn), tránh mất dữ liệu tủ đồ khi tắt app.
- **Expo FileSystem** chỉ dùng để cache recipe đã xem + ảnh minh họa, phục vụ xem lại khi mất mạng — không phải nơi lưu trạng thái chính.
- **Khi offline:** pantry list vẫn xem/sửa được (persist local), nhưng tính năng gọi API (tìm công thức mới, gợi ý AI) sẽ bị disable với thông báo "Cần kết nối mạng để dùng tính năng này".

**5.3. Chọn API công thức chính**
- **TheMealDB** là API chính (free, không giới hạn request khắt khe, phù hợp giai đoạn phát triển/demo).
- **Spoonacular** dùng làm backup / mở rộng sau này (khi cần dữ liệu dinh dưỡng chi tiết hơn), lưu ý free-tier giới hạn request/ngày rất thấp — cân nhắc trả phí nếu lên production.
- Cần viết lớp abstraction (adapter/service layer) để chuẩn hóa dữ liệu trả về, tránh phụ thuộc cứng vào cấu trúc response của 1 API cụ thể.

**5.4. Bảo mật API Key AI**
- **Không gọi trực tiếp Gemini/OpenAI API từ client (app).** Toàn bộ request AI phải đi qua **backend proxy** (server riêng của nhóm), backend giữ API key và forward request/response.
- Client chỉ gọi tới endpoint nội bộ (VD: `POST /api/ai/substitute`), không bao giờ chứa API key AI trong mã nguồn app hoặc file `.env` build kèm app.
- Backend proxy cũng là nơi thuận tiện để kiểm soát rate limit, cache response trùng lặp (giảm chi phí gọi AI).
---

## II. MA TRẬN PHÂN CÔNG VÀ TRÁCH NHIỆM (RACI MATRIX)

Nhóm gồm 6 thành viên, mỗi thành viên chịu trách nhiệm chính (Lead) một hạng mục chức năng lõi và tham gia các công việc dùng chung:

| Thành viên | Vai trò | Hạng mục chức năng chính (Lead) | Hạng mục dùng chung |
| :--- | :--- | :--- | :--- |
| **Nguyễn Phước Thịnh** | Team Leader / UI Specialist | Luồng Quản lý Tủ đồ (Pantry Flow) & Tamagui Design System | Quản lý tiến độ Git/Project, Viết báo cáo |
| **Nguyễn Văn Trường** | Data & State Engineer | Luồng Tìm kiếm & Lọc công thức (API + Redux Toolkit) | Xây dựng API Service & Error Handling |
| **Trầm Hồng Viên Thiệu** | Device & Native Feature | Tích hợp Expo SDK (Camera & FileSystem Offline Cache) | Xử lý Quyền truy cập (Permissions) |
| **Lê Thanh Tùng** | AI Specialist | Luồng Trợ lý AI tư vấn thay thế nguyên liệu | Xử lý An toàn dữ liệu & Privacy Prompt |
| **Nguyễn Trần Quốc Việt** |Backend & Database Engineer | Thiết kế Database & xây dựng Server API (Auth, Recipe, Pantry endpoints) | Triển khai môi trường Server (hosting/deploy) |
| **Nguyễn Đức Thuận** | QA & DevOps | Triển khai Animation/Gesture, Testing & Build EAS | Kiểm thử 8+ Test Cases, Cấu hình EAS Build |
---

## III. BACKLOG CÔNG VIỆC VÀ CÁC MỐC THỜI GIAN (10 TUẦN)

| Tuần | Mục tiêu chính | Hạng mục công việc chi tiết | Sản phẩm bàn giao (Deliverables) |
| :---: | :--- | :--- | :--- |
| **1** | Xác định vấn đề | Khảo sát 3 người dùng, chốt 3 luồng chính, lập báo cáo đề xuất. | File PDF/MD Đề xuất vấn đề (Đã hoàn thành) |
| **2** | Kế hoạch & Phân công | Chốt stack công nghệ, tạo Repo Git, thiết lập GitHub Projects, phân công RACI. | File PDF/MD Kế hoạch & Backlog (Báo cáo hiện tại) |
| **3** | Hồ sơ thiết kế | Thiết kế Wireframe, User Flow, Navigation Map, Tamagui Tokens/Themes, State Map. | Hồ sơ thiết kế (PDF 8–12 trang / Figma Link) |
| **4** | Sprint 1: Lát cắt dọc | Triển khai Router/Navigation, Tamagui components, RTK store, gọi API công thức đơn giản. | Bản Demo Sprint 1 (Chạy thành công 1 luồng end-to-end) |
| **5** | Đánh giá Giữa kỳ | Hoàn thiện 50% phạm vi, tích hợp 1 Expo SDK, làm Mock Data dự phòng, Vấn đáp cá nhân. | Báo cáo Giữa kỳ (PDF 4-6 trang) + Demo runnable |
| **6** | Sprint 2: UI & Device | Tích hợp đủ 2 Expo SDK (Camera + FileSystem), Responsive UI trên 2 màn hình. | Demo Sprint 2 (Hoàn thiện cấp quyền & UI responsive) |
| **7** | Integration: AI & Animation | Tích hợp Reanimated Swipe Gesture, tích hợp Gemini AI cho nguyên liệu thay thế, thêm warning dị ứng. | Demo Animation + AI Feature kèm Safety Check |
| **8** | Kiểm thử & Cải tiến | Viết & chạy 8+ ca kiểm thử (Success/Fail), phỏng vấn lại 5 lượt người dùng, test Deep Link. | Báo cáo kiểm thử & Danh sách sửa lỗi (Bug log) |
| **9** | Đóng gói & Release Candidate | Cấu hình EAS Build APK, viết hướng dẫn README chi tiết, lập nhật ký AI & trích dẫn nguồn. | File APK/AAB Release Candidate, README hoàn chỉnh |
| **10** | Hoàn thiện & Bảo vệ | Đánh tag v1.0.0, hoàn thiện Báo cáo tổng kết (18-25 trang), Slide (12-16 trang), Video Demo 5-7 phút. | Bộ nộp cuối kỳ hoàn chỉnh & Bảo vệ đồ án |

---

## IV. QUY TRÌNH QUẢN LÝ NGUỒN VÀ QUY ƯỚC LÀM VIỆC (GIT WORKFLOW)

### 1. Quy ước Phân nhánh (Git Branching Strategy)
- `main`: Nhánh chạy ổn định, chỉ merge khi có release tag hoặc đánh mốc lớn.
- `develop`: Nhánh tích hợp chính cho các sprint.
- `dev/<tên-thành-viên>`: Mỗi thành viên làm việc trên 1 nhánh riêng, đặt theo tên của mình (Ví dụ: `dev/thinh`, `dev/viet`). Mọi task (tính năng mới, sửa lỗi, việc phụ trợ...) đều thực hiện trên nhánh này, sau đó tạo Pull Request để merge vào `develop`.

### 2. Quy tắc Commit & Pull Request (PR)
- **Commit Message Standard:** `<type>: <description>` (Ví dụ: `feat: add camera component for pantry`, `fix: resolve recipe api pagination timeout`).
- Vì không tách nhánh theo loại công việc, commit message là căn cứ chính để phân biệt tính năng/lỗi/việc phụ trợ trong nhánh cá nhân — luôn ghi đúng `type`.
- Nên `pull`/`rebase` từ `develop` về nhánh cá nhân trước khi commit tiếp, để giảm conflict.
- Mọi Pull Request muốn merge vào `develop` phải có ít nhất **1 thành viên khác review** (Code Review) và xác nhận không dính API key/secrets trong mã nguồn.
- Nên tạo PR và merge thường xuyên (cuối ngày hoặc sau mỗi task nhỏ), tránh để nhánh cá nhân lệch quá xa `develop`.

### 3. Danh sách Commit Type

- **feat**: Thêm tính năng mới (VD: `feat: add camera component for pantry`)
- **fix**: Sửa lỗi (VD: `fix: resolve recipe api pagination timeout`)
- **docs**: Thay đổi tài liệu (README, comment, hướng dẫn...)
- **style**: Thay đổi format code, không ảnh hưởng logic (dấu cách, dấu chấm phẩy, indent...)
- **refactor**: Tái cấu trúc code, không thêm tính năng hay sửa lỗi
- **perf**: Cải thiện hiệu năng
- **test**: Thêm hoặc sửa test (unit test, integration test...)
- **chore**: Việc phụ trợ, không ảnh hưởng đến source code chính (update dependencies, config build...)
- **build**: Thay đổi liên quan đến build system hoặc dependencies bên ngoài (npm, webpack, docker...)
- **ci**: Thay đổi liên quan đến CI/CD pipeline (GitHub Actions, Jenkins...)
- **revert**: Revert lại một commit trước đó
*Báo cáo được khởi tạo và hoàn thiện vào Cuối Tuần 2.*

