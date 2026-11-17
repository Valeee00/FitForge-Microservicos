package br.fitforge.progresso.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.springframework.web.bind.annotation.RestController;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.onionArchitecture;

/**
 * Testes de arquitetura: garantem as regras da Arquitetura Hexagonal (Ports & Adapters).
 * Se alguém fizer o domínio depender do Spring ou do JPA, o build quebra.
 */
@AnalyzeClasses(packages = "br.fitforge.progresso", importOptions = ImportOption.DoNotIncludeTests.class)
class HexagonalArchitectureTest {

    @ArchTest
    static final ArchRule camadas_hexagonais = onionArchitecture()
            .domainModels("..domain.model..")
            .domainServices("..domain.exception..")
            .applicationServices("..application..")
            .adapter("web", "..adapter.in.web..")
            .adapter("mensageria-entrada", "..adapter.in.messaging..")
            .adapter("mensageria-saida", "..adapter.out.messaging..")
            .adapter("persistencia", "..adapter.out.persistence..");

    @ArchTest
    static final ArchRule dominio_nao_conhece_frameworks = noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "org.springframework..", "jakarta.persistence..", "org.bson..", "com.mongodb..",
                    "com.azure..", "com.fasterxml..");

    @ArchTest
    static final ArchRule aplicacao_nao_conhece_frameworks = noClasses()
            .that().resideInAPackage("..application..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "org.springframework..", "org.bson..", "com.mongodb..", "com.azure..", "com.fasterxml..");

    @ArchTest
    static final ArchRule aplicacao_nao_depende_de_adaptadores = noClasses()
            .that().resideInAPackage("..application..")
            .should().dependOnClassesThat().resideInAnyPackage("..adapter..", "..config..");

    @ArchTest
    static final ArchRule portas_sao_interfaces = classes()
            .that().resideInAPackage("..application.port..")
            .and().haveSimpleNameEndingWith("Port")
            .or().haveSimpleNameEndingWith("UseCase")
            .should().beInterfaces();

    @ArchTest
    static final ArchRule controllers_ficam_no_adaptador_web = classes()
            .that().areAnnotatedWith(RestController.class)
            .should().resideInAPackage("..adapter.in.web..");

    @ArchTest
    static final ArchRule jpa_so_no_adaptador_de_persistencia = noClasses()
            .that().resideOutsideOfPackage("..adapter.out.persistence..")
            .should().dependOnClassesThat().resideInAnyPackage("org.springframework.data.jpa..", "jakarta.persistence..");

    @ArchTest
    static final ArchRule service_bus_so_nos_adaptadores_de_mensageria = noClasses()
            .that().resideOutsideOfPackages("..adapter.in.messaging..", "..adapter.out.messaging..", "..config..")
            .should().dependOnClassesThat().resideInAnyPackage("com.azure.messaging..");
}
