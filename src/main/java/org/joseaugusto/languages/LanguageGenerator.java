package org.joseaugusto.languages;

import java.util.List;

public interface LanguageGenerator {

    void generateProject(String projectName, String mainClassName, List<String> libraries);
}
