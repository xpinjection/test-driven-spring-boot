package com.xpinjection.library;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;

import static com.tngtech.archunit.core.domain.JavaClass.Predicates.INTERFACES;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.*;
import static com.tngtech.archunit.library.GeneralCodingRules.*;

@AnalyzeClasses(packages = "com.xpinjection.library",
        importOptions = {ImportOption.DoNotIncludeTests.class, ImportOption.DoNotIncludeJars.class, ImportOption.DoNotIncludeArchives.class}
)
public class CodingConventionRules {
    @ArchTest
    ArchRule generic_exceptions_are_forbidden = NO_CLASSES_SHOULD_THROW_GENERIC_EXCEPTIONS;

    @ArchTest
    ArchRule java_util_logging_is_forbidden = NO_CLASSES_SHOULD_USE_JAVA_UTIL_LOGGING;

    @ArchTest
    ArchRule field_injection_should_not_be_used = NO_CLASSES_SHOULD_USE_FIELD_INJECTION;

    @ArchTest
    ArchRule standard_output_streams_should_not_be_used = NO_CLASSES_SHOULD_ACCESS_STANDARD_STREAMS;

    @ArchTest
    ArchRule deprecated_api_should_not_be_used = DEPRECATED_API_SHOULD_NOT_BE_USED;

    @ArchTest
    ArchRule test_classes_should_be_located_in_the_same_package = testClassesShouldResideInTheSamePackageAsImplementation();

    @ArchTest
    ArchRule controllers_should_not_depend_on_each_other =
            classes().that().areAnnotatedWith(RestController.class)
                    .should().onlyDependOnClassesThat().areNotAnnotatedWith(RestController.class);

    @ArchTest
    ArchRule services_should_not_depend_on_each_other =
            noClasses().that().areAnnotatedWith(Service.class)
                    .should().accessClassesThat().haveSimpleNameEndingWith("Service");

    @ArchTest
    ArchRule rest_controllers_are_stateless_and_depend_on_interfaces =
            fields().that().areDeclaredInClassesThat().areAnnotatedWith(RestController.class)
                    .should().beFinal()
                    .andShould().bePrivate()
                    .andShould().haveRawType(INTERFACES);

    @ArchTest
    ArchRule controllers_are_stateless_and_depend_on_interfaces =
            fields().that().areDeclaredInClassesThat().areAnnotatedWith(Controller.class)
                    .should().beFinal()
                    .andShould().bePrivate()
                    .andShould().haveRawType(INTERFACES);

    @ArchTest
    ArchRule services_are_stateless_and_depend_on_interfaces =
            fields().that().areDeclaredInClassesThat().areAnnotatedWith(Service.class)
                    .should().beFinal()
                    .andShould().bePrivate()
                    .andShould().haveRawType(INTERFACES);
}
