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
    static ArchRule NO_GENERIC_EXCEPTIONS = NO_CLASSES_SHOULD_THROW_GENERIC_EXCEPTIONS;

    @ArchTest
    static ArchRule NO_JAVA_UTIL_LOGGING = NO_CLASSES_SHOULD_USE_JAVA_UTIL_LOGGING;

    @ArchTest
    static ArchRule NO_FIELD_INJECTION = NO_CLASSES_SHOULD_USE_FIELD_INJECTION;

    @ArchTest
    static ArchRule NO_ACCESS_TO_STANDARD_OUTPUT_STREAMS = NO_CLASSES_SHOULD_ACCESS_STANDARD_STREAMS;

    @ArchTest
    static ArchRule CONTROLLERS_SHOULD_NOT_DEPEND_ON_EACH_OTHER =
            classes().that().areAnnotatedWith(RestController.class)
                    .should().onlyDependOnClassesThat().areNotAnnotatedWith(RestController.class);

    @ArchTest
    static ArchRule SERVICES_SHOULD_NOT_DEPEND_ON_EACH_OTHER =
            noClasses().that().areAnnotatedWith(Service.class)
                    .should().accessClassesThat().haveSimpleNameEndingWith("Service");

    @ArchTest
    static ArchRule REST_CONTROLLERS_ARE_STATELESS_AND_DEPEND_ON_INTERFACES =
            fields().that().areDeclaredInClassesThat().areAnnotatedWith(RestController.class)
                    .should().beFinal()
                    .andShould().bePrivate()
                    .andShould().haveRawType(INTERFACES);

    @ArchTest
    static ArchRule CONTROLLERS_ARE_STATELESS_AND_DEPEND_ON_INTERFACES =
            fields().that().areDeclaredInClassesThat().areAnnotatedWith(Controller.class)
                    .should().beFinal()
                    .andShould().bePrivate()
                    .andShould().haveRawType(INTERFACES);

    @ArchTest
    static ArchRule SERVICES_ARE_STATELESS_AND_DEPEND_ON_INTERFACES =
            fields().that().areDeclaredInClassesThat().areAnnotatedWith(Service.class)
                    .should().beFinal()
                    .andShould().bePrivate()
                    .andShould().haveRawType(INTERFACES);
}
