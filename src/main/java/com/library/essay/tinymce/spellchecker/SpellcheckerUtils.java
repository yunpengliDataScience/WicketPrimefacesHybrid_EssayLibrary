package com.library.essay.tinymce.spellchecker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * SpellcheckerUtils.java
 * Created on 10 11, 2013 by Andrey Chorniy
 */
public class SpellcheckerUtils {

    /**
     * @param concreteLanguage language with sub-components delimited by dash "-"
     * @return list of languages, starting from most concrete "en-us", followed by more generic "es"
     */
    public static List<String> resolveLanguages(String concreteLanguage) {
        List<String> result = new ArrayList<String>(3);
        StringBuilder langBuilder = new StringBuilder();
        for (String part : concreteLanguage.split("-")) {
            langBuilder.append(part);
            result.add(langBuilder.toString());
            langBuilder.append("-");
        }
        Collections.reverse(result);
        return result;
    }
}
