package si.slotex.nlp.stanford.tasks;

import java.util.List;
import java.util.Properties;

import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.sequences.SeqClassifierFlags;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.PropertiesUtils;
import edu.stanford.nlp.util.StringUtils;

public class NERTask
{

    final String serializedClassifierFilename = "./models/model-ner.ser.gz";

    public void train()
    {
        String prop = "./data/Stanford/props/sl-ner.prop";
        Properties props = StringUtils.propFileToProperties(prop);
        props.setProperty("serializeTo", serializedClassifierFilename);
        System.out.println("Properties for training NER model are set!");


        SeqClassifierFlags flags = new SeqClassifierFlags(props);

        CRFClassifier<CoreLabel> crf = new CRFClassifier<>(flags);

        System.out.println("Training...");
        crf.train();
        System.out.println("Training success!");

        System.out.println("Serialize model!");
        crf.serializeClassifier(serializedClassifierFilename);
        System.out.println("Model saved to: " + serializedClassifierFilename);
    }

    public void tag(String text)
    {
        //        Properties props = new Properties();
        //        props.put("annotators", "tokenize, ssplit, pos, lemma, ner");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(PropertiesUtils.asProperties(
                "annotators", "tokenize,ssplit,pos,lemma,ner",
                "tokenize.model", "./models/model-tokenize.ser.gz",
                "ssplit.model", "./models/model-ssplit.ser.gz",
                "ssplit.isOneSentence", "true",
                "pos.model", "./models/model-pos.ser.gz",
                "lemma.model", "./models/model-lemma.ser.gz",
                "ner.model", "./models/model-ner.ser.gz",
                "ner.useSUTime", "0"));

        Annotation document = new Annotation(text);

        pipeline.annotate(document);

        List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);

    }
}
