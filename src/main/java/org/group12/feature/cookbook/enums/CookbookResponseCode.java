package org.group12.feature.cookbook.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CookbookResponseCode {
    SAVE_RECIPE_SUCCESS("1000", "Lưu công thức thành công"),
    LIST_SAVED_RECIPES_SUCCESS("1001", "Lấy danh sách công thức đã lưu thành công"),
    GET_SAVED_RECIPE_SUCCESS("1002", "Lấy chi tiết công thức đã lưu thành công"),
    UPDATE_SAVED_RECIPE_SUCCESS("1003", "Cập nhật công thức đã lưu thành công"),
    LIST_COLLECTIONS_SUCCESS("1004", "Lấy danh sách bộ sưu tập thành công"),
    CREATE_COLLECTION_SUCCESS("1005", "Tạo bộ sưu tập thành công"),
    UPDATE_COLLECTION_SUCCESS("1006", "Đổi tên bộ sưu tập thành công"),
    ADD_RECIPE_TO_COLLECTION_SUCCESS("1007", "Thêm công thức vào bộ sưu tập thành công"),
    REMOVE_RECIPE_FROM_COLLECTION_SUCCESS("1008", "Bỏ công thức khỏi bộ sưu tập thành công");

    private final String code;
    private final String message;
}