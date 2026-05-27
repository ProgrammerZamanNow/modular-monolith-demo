package com.khannedy.ecommerce;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

public class ArchitectureTest {

    @Test
    void testModularMonolithBoundaries() {
        JavaClasses importedClasses = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("com.khannedy.ecommerce");

        // Rule 1: Order implementation should NEVER depend on other implementations directly.
        noClasses().that().resideInAPackage("..ecommerce.order..")
                .and().resideOutsideOfPackage("..ecommerce.order.client..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "..ecommerce.product.controller..", "..ecommerce.product.service..", "..ecommerce.product.entity..", "..ecommerce.product.repository..",
                        "..ecommerce.customer.controller..", "..ecommerce.customer.service..", "..ecommerce.customer.entity..", "..ecommerce.customer.repository..",
                        "..ecommerce.notification.service..", "..ecommerce.notification.entity..", "..ecommerce.notification.repository.."
                ).check(importedClasses);

        // Rule 2: Client packages should NEVER depend on implementation packages.
        noClasses().that().resideInAPackage("..client..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "..controller..", "..service..", "..entity..", "..repository.."
                ).check(importedClasses);
    }
}
