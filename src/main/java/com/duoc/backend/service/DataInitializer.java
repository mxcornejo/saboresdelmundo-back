package com.duoc.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Carga datos iniciales en la BD al arrancar la aplicación si las tablas están
 * vacías. Delega las operaciones transaccionales a {@link DataInitService} para
 * que Spring pueda aplicar el proxy AOP correctamente.
 */
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final DataInitService dataInitService;

    @Value("${app.init.admin-password}")
    private String adminPassword;

    @Value("${app.init.chef-password}")
    private String chefPassword;

    @Value("${app.init.user-password}")
    private String userPassword;

    @Override
    public void run(String... args) {
        dataInitService.initUsers(adminPassword, chefPassword, userPassword);
        dataInitService.initRecipes();
    }
}
