package org.group12.feature.cuisine.service;

import org.group12.common.utils.AppUtil;
import org.group12.feature.cuisine.dto.request.CuisineRequest;
import org.group12.feature.cuisine.dto.response.CuisineResponse;
import org.group12.feature.cuisine.exception.CuisineCodeDuplicateException;
import org.group12.feature.cuisine.exception.CuisineInUseException;
import org.group12.feature.cuisine.exception.CuisineNotFoundException;
import org.group12.feature.cuisine.mapper.CuisineMapper;
import org.group12.feature.cuisine.repository.CuisineRepository;
import org.group12.feature.recipe.entity.Cuisine;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CuisineServiceTest {

    private static final UUID ID = UUID.randomUUID();

    @Mock
    private CuisineRepository cuisineRepository;
    @Mock
    private CuisineMapper cuisineMapper;
    @Spy
    private AppUtil appUtil = new AppUtil();

    @InjectMocks
    private CuisineService service;

    @Test
    void getAll_returnsMappedCuisines() {
        Cuisine cuisine = Cuisine.builder().id(ID).code("VIETNAMESE").name("Vietnamese").build();
        CuisineResponse response = CuisineResponse.builder().id(ID).code("VIETNAMESE").name("Vietnamese").build();
        when(cuisineRepository.findAllByOrderByNameAsc()).thenReturn(List.of(cuisine));
        when(cuisineMapper.toResponse(cuisine)).thenReturn(response);

        assertThat(service.getAll()).containsExactly(response);
    }

    @Test
    void create_withNewCode_savesWithGeneratedIdAndTrimmedName() {
        CuisineRequest request = new CuisineRequest("JAPANESE", "  Japanese ");
        Cuisine mapped = Cuisine.builder().code("JAPANESE").name("  Japanese ").build();
        CuisineResponse response = CuisineResponse.builder().code("JAPANESE").name("Japanese").build();

        when(cuisineRepository.existsByCode("JAPANESE")).thenReturn(false);
        when(cuisineMapper.toEntity(request)).thenReturn(mapped);
        when(cuisineRepository.save(any(Cuisine.class))).thenAnswer(inv -> inv.getArgument(0));
        when(cuisineMapper.toResponse(any(Cuisine.class))).thenReturn(response);

        assertThat(service.create(request)).isSameAs(response);

        ArgumentCaptor<Cuisine> captor = ArgumentCaptor.forClass(Cuisine.class);
        verify(cuisineRepository).save(captor.capture());
        assertThat(captor.getValue().getId()).isNotNull();
        assertThat(captor.getValue().getName()).isEqualTo("Japanese");
    }

    @Test
    void create_withExistingCode_throwsDuplicate() {
        when(cuisineRepository.existsByCode("ITALIAN")).thenReturn(true);

        assertThatThrownBy(() -> service.create(new CuisineRequest("ITALIAN", "Italian")))
                .isInstanceOf(CuisineCodeDuplicateException.class);
        verify(cuisineRepository, never()).save(any(Cuisine.class));
    }

    @Test
    void update_missingCuisine_throwsNotFound() {
        when(cuisineRepository.findById(ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(ID, new CuisineRequest("ITALIAN", "Italian")))
                .isInstanceOf(CuisineNotFoundException.class);
    }

    @Test
    void update_codeOfAnotherCuisine_throwsDuplicate() {
        Cuisine cuisine = Cuisine.builder().id(ID).code("ASIAN").name("Asian").build();
        when(cuisineRepository.findById(ID)).thenReturn(Optional.of(cuisine));
        when(cuisineRepository.existsByCodeAndIdNot("ITALIAN", ID)).thenReturn(true);

        assertThatThrownBy(() -> service.update(ID, new CuisineRequest("ITALIAN", "Italian")))
                .isInstanceOf(CuisineCodeDuplicateException.class);
        verify(cuisineMapper, never()).updateEntity(any(CuisineRequest.class), any(Cuisine.class));
    }

    @Test
    void delete_cuisineUsedByRecipes_throwsInUseAndDeletesNothing() {
        Cuisine cuisine = Cuisine.builder().id(ID).code("ASIAN").name("Asian").build();
        when(cuisineRepository.findById(ID)).thenReturn(Optional.of(cuisine));
        when(cuisineRepository.isUsedByRecipe(ID)).thenReturn(true);

        assertThatThrownBy(() -> service.delete(ID)).isInstanceOf(CuisineInUseException.class);
        verify(cuisineRepository, never()).delete(any(Cuisine.class));
    }

    @Test
    void delete_unusedCuisine_deletesIt() {
        Cuisine cuisine = Cuisine.builder().id(ID).code("ASIAN").name("Asian").build();
        when(cuisineRepository.findById(ID)).thenReturn(Optional.of(cuisine));
        when(cuisineRepository.isUsedByRecipe(ID)).thenReturn(false);

        service.delete(ID);

        verify(cuisineRepository).delete(cuisine);
    }
}
