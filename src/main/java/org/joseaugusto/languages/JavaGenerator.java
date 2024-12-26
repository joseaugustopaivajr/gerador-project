package org.joseaugusto.languages;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.joseaugusto.utils.CustomKeywordHandler;
import org.joseaugusto.utils.MLProcessor;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class JavaGenerator implements LanguageGenerator {
    @Override
    public void generateProject(String projectName, String mainClassName, List<String> libraries) {
        try {
            Scanner scanner = new Scanner(System.in);

            // Perguntar ao usuário se deseja usar exemplos do MLProcessor
            System.out.print("Deseja incluir exemplos gerados pelo MLProcessor? (s/n): ");
            String useMLProcessor = scanner.nextLine().trim().toLowerCase();

            boolean includeExamples = useMLProcessor.equals("s");

            // Perguntar ao usuário se deseja usar Maven ou Gradle
            System.out.print("Deseja usar Maven ou Gradle para gerenciar o projeto? (maven/gradle): ");
            String buildTool = scanner.nextLine().trim().toLowerCase();

            if (!buildTool.equals("maven") && !buildTool.equals("gradle")) {
                throw new IllegalArgumentException("Ferramenta de build não reconhecida: " + buildTool);
            }

            // Criar diretório do projeto
            Path projectPath = Path.of(projectName);
            Files.createDirectories(projectPath.resolve("src/main/java/com/example"));

            // Criar arquivo principal
            StringBuilder mainContent = new StringBuilder("""
                package com.example;

                public class %s {
                    public static void main(String[] args) {
                        System.out.println("Projeto gerado com sucesso!");
                    }
                }
                """.formatted(mainClassName));

            // Adicionar exemplos usando bibliotecas padrão
            for (String library : libraries) {
                String[] examples = CustomKeywordHandler.getExamplesForKeyword(library);
                if (examples.length > 0) {
                    mainContent.append("\n// Exemplos para ").append(library).append(":\n");
                    for (String example : examples) {
                        mainContent.append("// ").append(example).append("\n");
                    }
                }
            }

            // Adicionar exemplos do MLProcessor, se solicitado
            if (includeExamples) {
                System.out.print("Digite uma descrição do projeto para o MLProcessor: ");
                String userInput = scanner.nextLine();

                JSONObject mlResponse = MLProcessor.processUserInput(userInput);
                mainContent.append("\n// Exemplos gerados pelo MLProcessor:\n");
                mainContent.append("// Linguagem principal: ").append(mlResponse.getString("language")).append("\n");

                mainContent.append("// Funcionalidades desejadas:\n");
                for (Object feature : mlResponse.getJSONArray("features")) {
                    mainContent.append("// - ").append(feature.toString()).append("\n");
                }

                mainContent.append("// Bibliotecas recomendadas:\n");
                for (Object dependency : mlResponse.getJSONArray("dependencies")) {
                    mainContent.append("// - ").append(dependency.toString()).append("\n");
                }
            }

            Files.writeString(projectPath.resolve("src/main/java/com/example/" + mainClassName + ".java"), mainContent.toString());

            // Criar arquivo de build
            if (buildTool.equals("maven")) {
                createMavenBuildFile(projectPath, libraries);
            } else {
                createGradleBuildFile(projectPath, libraries);
            }

            System.out.println("Projeto Java gerado com sucesso em " + projectPath.toAbsolutePath());
        } catch (IOException e) {
            throw new RuntimeException("Erro ao gerar projeto Java: " + e.getMessage());
        }
    }

    /**
     * Gera o arquivo pom.xml para um projeto Maven.
     */
    private void createMavenBuildFile(Path projectPath, List<String> libraries) throws IOException {
        StringBuilder dependencies = new StringBuilder();
        Scanner scanner = new Scanner(System.in);

        for (String library : libraries) {
            String[] libs = CustomKeywordHandler.getLibrariesForKeyword(library);

            for (String lib : libs) {
                String[] parts = lib.split(":");
                if (parts.length >= 2) {
                    String groupId = parts[0];
                    String artifactId = parts[1];

                    // Buscar versões disponíveis
                    List<String> versions = fetchAvailableVersions(groupId, artifactId);

                    // Mostrar versões disponíveis para o usuário
                    System.out.println("Dependência encontrada: " + groupId + ":" + artifactId);
                    if (versions.isEmpty()) {
                        System.out.println("Nenhuma versão disponível encontrada. Usando versão padrão: 1.0.0");
                        dependencies.append("""
                                <dependency>
                                    <groupId>%s</groupId>
                                    <artifactId>%s</artifactId>
                                    <version>1.0.0</version>
                                </dependency>
                                """.formatted(groupId, artifactId));
                        continue;
                    }

                    System.out.println("Versões disponíveis:");
                    for (int i = 0; i < versions.size(); i++) {
                        System.out.printf("%d. %s%n", i + 1, versions.get(i));
                    }

                    // Permitir escolha da versão
                    int choice;
                    do {
                        System.out.print("Escolha a versão desejada (número): ");
                        while (!scanner.hasNextInt()) {
                            System.out.println("Por favor, insira um número válido.");
                            scanner.next();
                        }
                        choice = scanner.nextInt();
                    } while (choice < 1 || choice > versions.size());

                    String selectedVersion = versions.get(choice - 1);

                    // Adicionar a dependência ao pom.xml
                    dependencies.append("""
                            <dependency>
                                <groupId>%s</groupId>
                                <artifactId>%s</artifactId>
                                <version>%s</version>
                            </dependency>
                            """.formatted(groupId, artifactId, selectedVersion));
                } else {
                    System.err.println("Dependência malformada encontrada: " + lib);
                    System.out.print("Deseja corrigir esta dependência agora? (s/n): ");
                    String fix = scanner.next().trim().toLowerCase();

                    if (fix.equals("s")) {
                        System.out.print("Informe o groupId: ");
                        String groupId = scanner.next().trim();

                        System.out.print("Informe o artifactId: ");
                        String artifactId = scanner.next().trim();

                        System.out.print("Informe a versão (ou pressione Enter para buscar versões disponíveis): ");
                        scanner.nextLine(); // Limpa o buffer
                        String version = scanner.nextLine().trim();

                        if (version.isEmpty()) {
                            // Buscar versões disponíveis
                            List<String> versions = fetchAvailableVersions(groupId, artifactId);

                            System.out.println("Versões disponíveis:");
                            for (int i = 0; i < versions.size(); i++) {
                                System.out.printf("%d. %s%n", i + 1, versions.get(i));
                            }

                            int choice;
                            do {
                                System.out.print("Escolha a versão desejada (número): ");
                                while (!scanner.hasNextInt()) {
                                    System.out.println("Por favor, insira um número válido.");
                                    scanner.next();
                                }
                                choice = scanner.nextInt();
                            } while (choice < 1 || choice > versions.size());

                            version = versions.get(choice - 1);
                        }

                        dependencies.append("""
                                <dependency>
                                    <groupId>%s</groupId>
                                    <artifactId>%s</artifactId>
                                    <version>%s</version>
                                </dependency>
                                """.formatted(groupId, artifactId, version));
                    } else {
                        System.err.println("Dependência ignorada: " + lib);
                    }
                }
            }
        }

        String pomContent = """
                <project xmlns="http://maven.apache.org/POM/4.0.0"
                         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                         xsi:schemaLocation="http://www.apache.org/xsd/maven-4.0.0.xsd">
                    <modelVersion>4.0.0</modelVersion>
                    <groupId>com.example</groupId>
                    <artifactId>%s</artifactId>
                    <version>1.0-SNAPSHOT</version>
                    <dependencies>
                        %s
                    </dependencies>
                </project>
                """.formatted(projectPath.getFileName(), dependencies);

        Files.writeString(projectPath.resolve("pom.xml"), pomContent);
    }

    /**
     * Gera o arquivo build.gradle para um projeto Gradle.
     */
    private void createGradleBuildFile(Path projectPath, List<String> libraries) throws IOException {
        StringBuilder dependencies = new StringBuilder();
        for (String library : libraries) {
            String[] libs = CustomKeywordHandler.getLibrariesForKeyword(library);
            for (String lib : libs) {
                dependencies.append("implementation '%s'\n".formatted(lib));
            }
        }

        String gradleContent = """
                plugins {
                    id 'java'
                }

                group = 'com.example'
                version = '1.0-SNAPSHOT'

                repositories {
                    mavenCentral()
                }

                dependencies {
                    %s
                }
                """.formatted(dependencies);

        Files.writeString(projectPath.resolve("build.gradle"), gradleContent);
    }

    /**
     * Busca as versões disponíveis de uma dependência usando a Maven Central API.
     */
    private List<String> fetchAvailableVersions(String groupId, String artifactId) {
        OkHttpClient client = new OkHttpClient();
        String url = String.format("https://search.maven.org/solrsearch/select?q=g:\"%s\"+AND+a:\"%s\"&rows=20&wt=json", groupId, artifactId);

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                String body = response.body().string();
                JSONObject jsonResponse = new JSONObject(body);
                JSONArray docs = jsonResponse.getJSONObject("response").getJSONArray("docs");

                List<String> versions = new ArrayList<>();
                for (int i = 0; i < docs.length(); i++) {
                    versions.add(docs.getJSONObject(i).getString("latestVersion"));
                }

                return versions;
            } else {
                System.err.println("Falha ao buscar versões para " + groupId + ":" + artifactId);
                return List.of("1.0.0"); // Retorna uma versão padrão em caso de falha
            }
        } catch (IOException e) {
            throw new RuntimeException("Erro ao conectar à API Maven Central: " + e.getMessage(), e);
        }
    }
}
