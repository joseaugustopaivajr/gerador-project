package org.joseaugusto;

import org.joseaugusto.languages.GoGenerator;
import org.joseaugusto.languages.JavaGenerator;
import org.joseaugusto.languages.LanguageGenerator;
import org.joseaugusto.languages.PythonGenerator;
import org.joseaugusto.utils.CustomKeywordHandler;
import org.joseaugusto.utils.MLProcessor;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("### Gerador de Projetos Multilínguas com Aprendizado de Máquina ###");
        System.out.println("Descreva o projeto que deseja criar:");
        String userInput = scanner.nextLine();

        // Processar entrada com Machine Learning
        JSONObject mlResult = MLProcessor.processUserInput(userInput);

        if (!mlResult.has("language")) {
            System.err.println("Erro: A resposta não contém o campo 'language'.");
            System.err.println("Resposta recebida: " + mlResult.toString(4));
            return;
        }

        // Extração de dados do resultado do ML
        String language = mlResult.getString("language");

        // Converter listas genéricas para listas de String
        List<String> dependencies = convertJSONArrayToStringList(mlResult.getJSONArray("dependencies"));
        Set<String> features = new HashSet<>(convertJSONArrayToStringList(mlResult.getJSONArray("features")));

        System.out.println("Linguagem detectada: " + language);
        System.out.println("Funcionalidades: " + features);
        System.out.println("Dependências: " + dependencies);

        // Aprender novas palavras-chave, se necessário
        for (String dependency : dependencies) {
            if (!CustomKeywordHandler.isKnownKeyword(dependency)) {
                System.out.println("Nova dependência detectada: " + dependency);
                System.out.print("Qual linguagem esta funcionalidade utiliza? ");
                String languageForKeyword = scanner.nextLine();

                System.out.print("Quais bibliotecas estão associadas a esta funcionalidade? (separadas por vírgula) ");
                String librariesInput = scanner.nextLine();
                String[] libraries = librariesInput.split(",\\s*");

                System.out.print("Forneça um ou mais exemplos de uso dessa dependência (separados por linha, finalize com ENTER duas vezes): ");
                List<String> examples = new ArrayList<>();
                while (scanner.hasNextLine()) {
                    String example = scanner.nextLine();
                    if (example.isBlank()) break;
                    examples.add(example);
                }

                CustomKeywordHandler.addKeyword(dependency, languageForKeyword, libraries, examples.toArray(new String[0]));
                System.out.println("Nova palavra-chave salva com sucesso!");
            }
        }


        System.out.println("Exemplos gerados para as dependências:");
        for (String dependency : dependencies) {
            String[] examples = CustomKeywordHandler.getExamplesForKeyword(dependency);
            if (examples.length > 0) {
                System.out.println("Exemplos para " + dependency + ":");
                for (String example : examples) {
                    System.out.println(example);
                }
            }
        }

        // Gerar projeto com o gerador apropriado
        LanguageGenerator generator;
        switch (language.toLowerCase()) {
            case "java" -> generator = new JavaGenerator();
            case "python" -> generator = new PythonGenerator();
            case "go" -> generator = new GoGenerator();
            default -> throw new IllegalArgumentException("Linguagem não suportada.");
        }

        System.out.print("Digite o nome do projeto: ");
        String projectName = scanner.nextLine();

        System.out.print("Digite o nome da classe/arquivo principal: ");
        String mainClassName = scanner.nextLine();

        generator.generateProject(projectName, mainClassName, dependencies);

        System.out.println("Projeto gerado com sucesso!");
    }

    /**
     * Converte um JSONArray em uma lista de Strings.
     *
     * @param jsonArray O JSONArray a ser convertido
     * @return Uma lista de Strings
     */
    private static List<String> convertJSONArrayToStringList(JSONArray jsonArray) {
        return jsonArray.toList().stream()
                .map(Object::toString) // Converter cada objeto para String
                .collect(Collectors.toList());
    }
}
