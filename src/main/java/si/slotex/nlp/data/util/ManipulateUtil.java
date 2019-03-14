package si.slotex.nlp.data.util;

import java.util.Arrays;

public class ManipulateUtil
{

    public static String mappingPos(String pos)
    {
        String mapping = "";

        switch (pos.charAt(0))
        {
            case 'S':
                return "NOUN";
            case 'G':
                return "VERB";
            case 'P':
                return "ADJ";
            case 'R':
                return "ADV";
            case 'Z':
                return "PRUN";
            case 'K':
                return "NUM";
            case 'D':
                return "PREP";
            case 'V':
                return "CONJ";
            case 'L':
                return "PRT";
            case 'M':
                return "INT";
            case 'O':
                return "ADDB";
            case 'N':
                return "RES";
        }

        return mapping;
    }

    public static boolean checkValidSigns(String word)
    {
        return word == null || Arrays.asList("(", ")", "\"", "-", "»", "«", "*", "=", "/", "×", "<", "°", "µ",
                "_", "¯", "'", "‘", "’", "”", "“", "[", "]", "{", "}", "©", "@", "$", "&", "#", "%", "+", "—", "–", "•", ">", "…").contains(word);
    }
}
