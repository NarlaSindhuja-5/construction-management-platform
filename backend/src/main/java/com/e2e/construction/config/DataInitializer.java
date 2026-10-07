package com.e2e.construction.config;

import com.e2e.construction.entity.Role;
import com.e2e.construction.repository.RoleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    private final RoleRepository roleRepository;

    public DataInitializer(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public void run(String... args) {
        Map<String, String> defaultRoles = Map.of(
                "ADMIN", "System Administrator with full management and governance privileges",
                "CONTRACTOR", "Licensed contractor managing bids, site execution, laborers, and materials",
                "SITE_MANAGER", "Site manager supervising daily construction activities, work logs, and site progress",
                "LABORER", "Skilled/unskilled worker offering on-site construction trade services",
                "MACHINERY_OWNER", "Owner/supplier of heavy construction machinery and equipment rentals",
                "MATERIAL_SUPPLIER", "Vendor/distributor supplying raw construction materials and goods",
                "CLIENT", "Project owner, property developer, or individual commissioning construction"
        );

        defaultRoles.forEach((name, description) -> {
            if (roleRepository.findByName(name).isEmpty()) {
                roleRepository.save(new Role(name, description));
                logger.info("Initialized system role: {}", name);
            }
        });
    }
}
