package si.slotex.nlp.opennlp;

import static si.slotex.nlp.utils.NLPUtils.getDataFile;
import static si.slotex.nlp.utils.NLPUtils.saveModel;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import opennlp.tools.langdetect.Language;
import opennlp.tools.langdetect.LanguageDetector;
import opennlp.tools.langdetect.LanguageDetectorFactory;
import opennlp.tools.langdetect.LanguageDetectorME;
import opennlp.tools.langdetect.LanguageDetectorModel;
import opennlp.tools.langdetect.LanguageDetectorSampleStream;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.TrainingParameters;
import si.medius.nlp.model.DocTag;
import si.medius.nlp.model.Document;

/**
 * Class for management all related to the language processing in the NLP.
 * It can train new training models on the additional data provided or
 * detect different languages that could be used in the processed document.
 *
 * @author Mitja Kotnik
 * @version 1.0
 */
public class LangTask implements Task
{

    private static Logger logger = LoggerFactory.getLogger(LangTask.class);

    public void train(String modelFilePath, String dataFilePath, String language) throws IOException
    {
        ObjectStream lineStream = getDataFile(dataFilePath);

        logger.info("Training language model on train data from: " + dataFilePath);

        LanguageDetectorSampleStream sampleStream = new LanguageDetectorSampleStream(lineStream);

        TrainingParameters params = new TrainingParameters();
        params.put(TrainingParameters.ITERATIONS_PARAM, 100);
        params.put(TrainingParameters.CUTOFF_PARAM, 2);
        params.put("DataIndexer", "TwoPass");

        LanguageDetectorModel model = LanguageDetectorME.train(sampleStream, params, new LanguageDetectorFactory());

        saveModel(model, modelFilePath);

        lineStream.close();
        sampleStream.close();

        logger.info("Saving newly created language model to file: " + modelFilePath);
    }

    public static DocTag detect(String modelFilePath, Document document) throws IOException
    {
        InputStream modelIn = new FileInputStream(modelFilePath);
        LanguageDetectorModel model = new LanguageDetectorModel(modelIn);
        LanguageDetector ld = new LanguageDetectorME(model);

        Language[] languages = ld.predictLanguages(document.getContent());
        Language bestLanguage = ld.predictLanguage(document.getContent());

        logger.info("Detecting language...");

        for (Language language : languages)
        {
            logger.info(language.getLang() + " reliability: " + language.getConfidence());
        }

        logger.info("We assume with certainty " + bestLanguage.getConfidence() + ", that the selected text is in " + bestLanguage.getLang() + " language for document with ID: " + document.getDocumentId());

        DocTag docTag = new DocTag();
        docTag.setDocumentId(document.getDocumentId());
        docTag.setTitle(document.getTitle());
        docTag.setLanguage(bestLanguage.getLang());
        docTag.setLanguageProb(bestLanguage.getConfidence());

        return docTag;
    }
}
