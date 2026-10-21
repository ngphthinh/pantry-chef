package org.group12.common.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.group12.common.utils.AppUtil;
import org.group12.feature.auth.entity.Role;
import org.group12.feature.auth.entity.User;
import org.group12.feature.auth.enums.MeasurementSystem;
import org.group12.feature.auth.enums.RoleName;
import org.group12.feature.auth.repository.RoleRepository;
import org.group12.feature.auth.repository.UserRepository;
import org.group12.feature.recipe.repository.CuisineRepository;
import org.group12.feature.recipe.entity.Cuisine;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

@Slf4j
@Profile("dev")
@RequiredArgsConstructor
@Component
public class InitData implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final CuisineRepository cuisineRepository;
    private final PasswordEncoder passwordEncoder;
    private final AppUtil appUtil;

    @Override
    public void run(String... args) throws Exception {
        initRoles();
        initAdmin();
        initCuisines();
    }

    private void initRoles() {
        if (roleRepository.count() == 0) {
            List<Role> roles = List.of(Role.builder().id(appUtil.generateUUID()).name(RoleName.ROLE_ADMIN).build(), Role.builder().id(appUtil.generateUUID()).name(RoleName.ROLE_USER).build());
            roleRepository.saveAll(roles);
        }
    }

    private void initCuisines() {
        if (cuisineRepository.count() == 0) {
            List<Cuisine> cuisines = Stream.of("VIETNAMESE:Vietnamese", "ASIAN:Asian", "WESTERN:Western", "ITALIAN:Italian", "OTHER:Other")
                    .map(s -> s.split(":"))
                    .map(p -> Cuisine.builder().id(appUtil.generateUUID()).code(p[0]).name(p[1]).build())
                    .toList();
            cuisineRepository.saveAll(cuisines);
        }
    }

    private void initAdmin() {
        if (userRepository.count() == 0) {
            User user = User.builder()
                    .email("admin@gmail.com")
                    .password(passwordEncoder.encode("Admin@123"))
                    .measurementSystem(MeasurementSystem.METRIC)
                    .id(appUtil.generateUUID())
                    .themeMode(org.group12.feature.auth.enums.ThemeMode.LIGHT)
                    .roles(Set.copyOf(roleRepository.findAll()))
                    .build();
            userRepository.save(user);
        }

    }
}