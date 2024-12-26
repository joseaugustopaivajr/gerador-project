package org.joseaugusto.languages;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class GoGenerator implements LanguageGenerator {
    @Override
    public void generateProject(String projectName, String mainClassName, List<String> libraries) {
        try {
            Path projectPath = Path.of(projectName);
            Files.createDirectories(projectPath);

            String mainContent = """
                    package main

                    import "fmt"

                    func main() {
                        fmt.Println("Projeto gerado com sucesso!")
                    }
                    """;
            Files.writeString(projectPath.resolve(mainClassName + ".go"), mainContent);

            // Criar arquivo go.mod
            StringBuilder goModContent = new StringBuilder("module " + projectName + "\n\n");
            goModContent.append("go 1.19\n");
            for (String library : libraries) {
                goModContent.append("require ").append(library.trim()).append(" latest\n");
            }
            Files.writeString(projectPath.resolve("go.mod"), goModContent);

        } catch (IOException e) {
            throw new RuntimeException("Erro ao gerar projeto Go: " + e.getMessage());
        }
    }
}
