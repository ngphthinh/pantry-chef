package org.group12.common.config;

import com.github.javafaker.Faker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.group12.common.utils.AppUtil;
import org.group12.feature.auth.dto.request.UserCreateRequest;
import org.group12.feature.auth.entity.Role;
import org.group12.feature.auth.entity.User;
import org.group12.feature.auth.enums.MeasurementSystem;
import org.group12.feature.auth.enums.RoleName;
import org.group12.feature.auth.enums.ThemeMode;
import org.group12.feature.auth.repository.RoleRepository;
import org.group12.feature.auth.repository.UserRepository;
import org.group12.feature.auth.service.AuthenticationService;
import org.group12.feature.recipe.entity.Cuisine;
import org.group12.feature.recipe.repository.CuisineRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
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
    private final PasswordEncoder passwordEncoder;
    private final AppUtil appUtil;
    private final AuthenticationService authenticationService;
    private final CuisineRepository cuisineRepository;

    @Value("${app.password-default}")
    private String PASSWORD;

    @Override

    public void run(String... args) throws Exception {
        initRoles();
        initUsers();
        initCuisines();
    }

    private void initUsers() {
        Faker fakeData = new Faker();
        int numberOfUsers = 100;
        Role role = roleRepository.findByName(RoleName.ROLE_USER).orElseThrow();
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

            List<User> users = new ArrayList<>();
            for (int i = 0; i < numberOfUsers; i++) {
                String email = fakeData.internet().emailAddress();
                String password = PASSWORD;
                String displayName = fakeData.name().fullName();
                MeasurementSystem measurementSystem = i % 2 == 0 ? MeasurementSystem.METRIC : MeasurementSystem.IMPERIAL;
                ThemeMode themeMode = i % 2 == 0 ? ThemeMode.LIGHT : ThemeMode.DARK;

                users.add(User.builder()
                        .id(appUtil.generateUUID())
                        .email(email)
                        .password(passwordEncoder.encode(password))
                        .displayName(displayName)
                        .measurementSystem(measurementSystem)
                        .themeMode(themeMode)
                        .roles(Set.of(role))
                        .build());

            }
            userRepository.saveAll(users);
        }
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

}