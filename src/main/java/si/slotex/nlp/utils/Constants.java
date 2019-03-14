package si.slotex.nlp.utils;

/**
 * Constant values for our files that are stored in our local storage.
 *
 * @author Mitja Kotnik
 * @version 1.0
 */
public class Constants
{

    public static final String langModelFile = "/dual-lang.bin";
    public static final String sentenceModelFile = "-sentence.bin";
    public static final String tokenizeModelFile = "-token.bin";
    public static final String posModelFile = "-pos.bin";
    public static final String lemmaModelFile = "-lemma.bin";
    public static final String nerPersonModelFile = "-ner-person.bin";
    public static final String nerOrganizationModelFile = "-ner-organization.bin";
    public static final String nerLocationModelFile = "-ner-location.bin";

    // Person
    public static final String corpusDictFile = "/corpus-person-dict.train";
    public static final String corpusStatFile = "/corpus-person-stat.train";
    public static final String correctedCorpus = "/slo-ner-person.train";
    public static final String corpusToTag = "/slo-ner-person-all.data";

    // Location
    public static final String corpusLocDictFile = "/corpus-location-dict.train";
    public static final String corpusLocStatFile = "/corpus-location-stat.train";
    public static final String correctedLocCorpus = "/slo-ner-location.train";
    public static final String corpusLocToTag = "/slo-ner-location.data";

    // Organization
    public static final String corpusOrgDictFile = "/corpus-organization-dict.train";
    public static final String corpusOrgStatFile = "/corpus-organization-stat.train";
    public static final String correctedOrgCorpus = "/slo-ner-organization.train";
    public static final String corpusOrgToTag = "/slo-ner-organization.data";
}
