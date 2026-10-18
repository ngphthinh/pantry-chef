# BÁO CÁO TUẦN 1: ĐỀ XUẤT VẤN ĐỀ VÀ PHẠM VI DỰ ÁN
**Môn học:** Lập trình thiết bị di động (Mã học phần: 2101454)
**Tên đề tài khung:** Mobile Solution Studio
**Đề tài chọn (Số 10):** Tủ công thức từ nguyên liệu sẵn có (Smart Recipe & Pantry Assistant)
**Nền tảng triển khai:** React Native (Expo, TypeScript)
**Quy mô:** Nhóm 6 sinh viên

---

## I. BỐI CẢNH VÀ TUYÊN BỐ VẤN ĐỀ (PROBLEM STATEMENT)

### 1. Bối cảnh thực tế
Trong sinh hoạt hằng ngày, việc quản lý thực phẩm tại gia đình hoặc phòng trọ sinh viên thường gặp nhiều bất cập:
- Thực phẩm trong tủ lạnh dễ bị lãng quên dẫn đến hư hỏng, quá hạn sử dụng.
- Người nội trợ/sinh viên thường tốn nhiều thời gian suy nghĩ câu hỏi "Hôm nay ăn gì?" với những nguyên liệu đang có sẵn.
- Khi nấu ăn theo một công thức trên mạng, người dùng thường thiếu 1–2 nguyên liệu phụ/gia vị và không biết nguyên liệu nào có thể thay thế tương đương mà không làm hỏng vị món ăn.

### 2. Tuyên bố vấn đề (Problem Statement)
"Người nấu ăn tại gia đình và sinh viên tự nấu ăn thường gặp khó khăn trong quản lý nguyên liệu tồn dư và lựa chọn món ăn phù hợp, dẫn đến lãng phí thực phẩm và tốn thời gian. Họ cần một ứng dụng di động thông minh giúp ghi nhận nhanh nguyên liệu sẵn có, gợi ý món ăn phù hợp và trợ lý AI tư vấn nguyên liệu thay thế linh hoạt."

---

## II. KHẢO SÁT NGƯỜI DÙNG VÀ BẰNG CHỨNG THỰC TẾ

Nhóm đã tiến hành khảo sát phỏng vấn sâu 3 người dùng đại diện cho nhóm người dùng mục tiêu:

### 1. Danh sách người tham gia khảo sát
* **Người dùng 1 (Sinh viên ở trọ):** Nguyễn Văn A - Nam, 21 tuổi. Tự nấu ăn 1 bữa/ngày, thường xuyên dư thừa rau củ nhỏ lẻ.
* **Người dùng 2 (Nhân viên văn phòng):** Trần Thị B - Nữ, 26 tuổi. Thường nấu ăn buổi tối, bận rộn, muốn tận dụng tối đa thực phẩm mua từ cuối tuần.
* **Người dùng 3 (Người nội trợ):** Lê Thị C - Nữ, 45 tuổi. Quản lý bếp ăn gia đình 4 người, hay quên các nguyên liệu còn lại ở góc tủ lạnh.

### 2. Kết quả tổng hợp khảo sát
- Phần lớn người dùng gặp tình trạng thực phẩm bị hết hạn hoặc hư hỏng do không nhớ rõ những gì còn lại trong tủ lạnh.
- Hầu hết mọi người đều từng bỏ qua một công thức nấu ăn chỉ vì thiếu một vài nguyên liệu phụ mà không biết cách thay thế phù hợp.
- Hầu hết người dùng mong muốn có một công cụ hỗ trợ tìm kiếm món ăn linh hoạt dựa trên danh sách nguyên liệu có sẵn (thông qua chế độ tích chọn).

### 3. Chân dung người dùng điển hình (User Persona - Ngắn)
* **Tên:** Minh Anh (22 tuổi - Sinh viên năm cuối)
* **Đặc điểm:** Tự nấu ăn để tiết kiệm chi phí, quỹ thời gian hạn hẹp.
* **Nhu cầu:** Mở ứng dụng, chọn các thứ còn trong tủ lạnh ➔ Hiển thị ngay món ăn làm được. Nếu thiếu gia vị/nguyên liệu phụ ➔ Cần biết món đó có thể thay bằng gì mà không cần đi siêu thị mua thêm.
* **Trăn trở:** Sợ lãng phí thực phẩm, sợ món ăn thất bại do thay thế nguyên liệu sai cách hoặc gây dị ứng.

---

    ## III. MỤC TIÊU VÀ 3 LUỒNG NGƯỜI DÙNG CHÍNH (USER FLOWS)

    ### Luồng 1: Quản lý tủ nguyên liệu (Pantry Management Flow)
    1. **Đầu vào:** Người dùng chụp ảnh nguyên liệu hoặc nhập tên nguyên liệu, chọn phân loại (Rau củ, Thịt, Gia vị...) và ngày hết hạn.
    2. **Xử lý:** Ứng dụng lưu vào cơ sở dữ liệu local/state, tính toán hạn sử dụng.
    3. **Kết quả:** Danh sách nguyên liệu trong tủ lạnh hiển thị trực quan theo nhóm, có trạng thái cảnh báo nguyên liệu sắp hỏng.

    ### Luồng 2: Tìm kiếm & Gợi ý công thức (Recipe Discovery Flow)
    1. **Đầu vào:** Người dùng tích chọn 3–5 nguyên liệu đang có sẵn trong tủ.
    2. **Xử lý:** Gọi API công khai (Spoonacular / TheMealDB) để lọc các công thức chứa các nguyên liệu đó; xử lý phân trang và phân loại theo tỷ lệ khớp nguyên liệu.
    3. **Kết quả:** Hiển thị danh sách các món ăn có thể nấu kèm tỷ lệ đáp ứng nguyên liệu (ví dụ: Khớp 80% - Thiếu 1 nguyên liệu).

    ### Luồng 3: Trợ lý AI gợi ý nguyên liệu thay thế (AI Substitute Assistant Flow)
    1. **Đầu vào:** Tại màn hình chi tiết món ăn, người dùng bấm nút "Thiếu nguyên liệu này" (ví dụ: Hết bơ lạt).
    2. **Xử lý:** Gửi prompt kiểm soát tới AI API (OpenAI/Gemini API) xử lý bối cảnh món ăn và nguyên liệu thiếu.
    3. **Kết quả:** AI trả về gợi ý nguyên liệu thay thế tương đương (ví dụ: Thay bơ lạt bằng dầu dừa hoặc dầu thực vật theo tỉ lệ 1:1), kèm **thông báo cảnh báo an toàn dị ứng**.

    ---

## IV. ÁNH XẠ CHUẨN ĐẦU RA (CLO MAPPING) SƠ BỘ

| Chuẩn đầu ra (CLO) | Yêu cầu thể hiện trong dự án |
| :--- | :--- |
| **CLO1 (Công cụ & Kỹ thuật)** | Lựa chọn Expo framework, TypeScript, cấu trúc thư mục rõ ràng. |
| **CLO2 (UI & React Native Core)** | Màn hình danh sách nguyên liệu, chi tiết công thức, sử dụng `FlatList`/`SectionList`, Hooks (`useState`, `useEffect`, `useContext`/`useReducer`), ... |
| **CLO3 (Tương tác Dữ liệu)** | Gọi API `Axios`/`Fetch` lấy dữ liệu công thức, xử lý phân trang, trạng thái Loading, Error, Empty và Retry. |
| **CLO4 (Animation & Gesture)** | Vuốt (`Swipeable`) để xóa/đã dùng nguyên liệu, hiệu ứng chuyển cảnh mượt giữa danh sách và chi tiết bằng Reanimated. |
| **CLO5 (Thư viện NativeMind)** | Định nghĩa Tokens, 2 Themes (Dark/Light hoặc Warm Kitchen), Responsive UI, 3+ component tái sử dụng (`IngredientBadge`, `RecipeCard`, `EmptyState`). |
| **CLO6 (Expo SDK & Build)** | Sử dụng `Camera` (chụp ảnh nguyên liệu) và `FileSystem` (lưu cache công thức offline). Cấu hình EAS Build ra file APK. Lưu trữ nguyên liệu bằng AsyncStorage/SQLite (`expo-sqlite`). |
| **CLO7 (Tính năng AI)** | Tích hợp AI tư vấn nguyên liệu thay thế, xử lý lỗi timeout, kiểm soát dữ liệu nhạy cảm và đưa ra cảnh báo dị ứng. |

---

## V. ĐÁNH GIÁ RỦI RO VÀ PHƯƠNG ÁN GIẢM THIỂU

| STT | Rủi ro nhận diện | Mức độ | Phương án xử lý / Giảm thiểu |
| :---: | :--- | :---: | :--- |
| 1 | **AI gợi ý sai lệch / Cung cấp thông tin dị ứng nguy hiểm** | Cao | Thêm cảnh báo rõ ràng: *"Gợi ý chỉ mang tính tham khảo"*. Kiểm soát prompt AI chặt chẽ, giới hạn phạm vi trả lời. |
| 2 | **API bên thứ 3 (Spoonacular) bị giới hạn lượt gọi (Rate limit)** | Trung bình | Thiết lập dữ liệu mô phỏng (Mock Data JSON) dự phòng để phục vụ demo khi API quá tải. |
| 3 | **Quyền riêng tư khi dùng Camera** | Thấp | Kiểm tra và yêu cầu cấp quyền trước khi mở Camera, xử lý từ chối quyền mượt mà không crash app. |
| 4 | **Bản quyền hình ảnh / công thức nấu ăn** | Thấp | Trích dẫn nguồn API công khai, không thương mại hóa ứng dụng. |
| 5 | **Lộ API key AI khi gọi trực tiếp từ client (mobile app)** | Cao | Sử dụng backend/proxy trung gian (ví dụ Cloud Function) để giấu key, không nhúng key trực tiếp vào app. |
