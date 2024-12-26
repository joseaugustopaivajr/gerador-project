package org.joseaugusto.languages;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class PythonGenerator implements LanguageGenerator {

    @Override
    public void generateProject(String projectName, String mainClassName, List<String> libraries) {
        try {
            Path projectPath = Path.of(projectName);
            Files.createDirectories(projectPath);

            String mainContent = """
                    def main():
                        print("Projeto gerado com sucesso!")

                    if __name__ == "__main__":
                        main()
                    """;
            Files.writeString(projectPath.resolve(mainClassName + ".py"), mainContent);

            // Adicionar dependências ao requirements.txt
            StringBuilder requirements = new StringBuilder();
            for (String library : libraries) {
                requirements.append(library.trim()).append("\n");
            }
            Files.writeString(projectPath.resolve("requirements.txt"), requirements);

        } catch (IOException e) {
            throw new RuntimeException("Erro ao gerar projeto Python: " + e.getMessage());
        }
    }
}

