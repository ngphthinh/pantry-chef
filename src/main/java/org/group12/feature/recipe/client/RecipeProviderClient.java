package org.group12.feature.recipe.client;

import org.group12.feature.recipe.client.model.ExternalRecipeDetail;
import org.group12.feature.recipe.client.model.ExternalRecipePage;
import org.group12.feature.recipe.client.model.ExternalRecipeSummary;
import org.group12.feature.recipe.client.model.RecipeSearchCriteria;

import java.util.List;

/**
 * Lớp trừu tượng cho API công thức bên ngoài. Service chỉ làm việc với interface này,
 * đổi nhà cung cấp (Spoonacular, TheMealDB, ...) chỉ cần viết một cài đặt mới.
 * <p>
 * Lỗi nhà cung cấp được chuyển thành {@code AppException}: không tìm thấy -> RecipeNotFoundException,
 * hết quota -> RecipeProviderQuotaExceededException, lỗi khác -> RecipeProviderException.
 */
public interface RecipeProviderClient {

    /** Nhà cung cấp có lọc được theo ẩm thực này không (so khớp không phân biệt hoa thường). */
    boolean supportsCuisine(String cuisineName);

    ExternalRecipePage search(RecipeSearchCriteria criteria, int offset, int number);

    ExternalRecipeDetail getDetail(String externalId);

    List<ExternalRecipeSummary> getSimilar(String externalId, int number);

    List<ExternalRecipeSummary> getRandom(int number);
}
