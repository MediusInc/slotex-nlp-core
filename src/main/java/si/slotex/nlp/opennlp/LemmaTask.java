package si.slotex.nlp.opennlp;

import static si.slotex.nlp.utils.NLPUtils.getDataFile;
import static si.slotex.nlp.utils.NLPUtils.saveModel;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import opennlp.tools.lemmatizer.LemmaSampleStream;
import opennlp.tools.lemmatizer.LemmatizerFactory;
import opennlp.tools.lemmatizer.LemmatizerME;
import opennlp.tools.lemmatizer.LemmatizerModel;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.TrainingParameters;
import si.slotex.nlp.entity.DocTag;
import si.slotex.nlp.entity.Sentence;

/**
 * Class for management all related to the lemmatization processing in the NLP.
 * It can train new training models on the additional data provided or
 * tag different lemmas for the specified words in the document processed.
 *
 * @author Mitja Kotnik
 * @version 1.0
 */
public class LemmaTask implements Task
{
    private static Logger logger = LoggerFactory.getLogger(LemmaTask.class);

    public void train(String modelFilePath, String dataFilePath, String language) throws IOException
    {
        ObjectStream<String> lineStream = getDataFile(dataFilePath);

        LemmaSampleStream lemmaStream = new LemmaSampleStream(lineStream);

        TrainingParameters mlParams = new TrainingParameters();
        mlParams.put(TrainingParameters.ITERATIONS_PARAM, "100");
        mlParams.put(TrainingParameters.CUTOFF_PARAM, "5");

        LemmatizerFactory lemmatizerFactory = new LemmatizerFactory();
        LemmatizerModel model = LemmatizerME.train(language, lemmaStream, mlParams, lemmatizerFactory);

        saveModel(model, modelFilePath);

        lineStream.close();
        lemmaStream.close();
    }

    public static void tag(String modelFilePath, DocTag docTag) throws IOException
    {
        InputStream modelIn = new FileInputStream(modelFilePath);
        LemmatizerModel model = new LemmatizerModel(modelIn);

        LemmatizerME lemmatizer = new LemmatizerME(model);

        for (Sentence sentence : docTag.getSentences())
        {
            String[] tokens = new String[sentence.getNumberOfTokens()];
            String[] tags = new String[sentence.getNumberOfTokens()];

            for(int i = 0; i < sentence.getNumberOfTokens(); i++)
            {
                tokens[i] = sentence.getTokens().get(i).getWord();
                tags[i] = sentence.getTokens().get(i).getPosTag();
            }

            String[] lemmas = lemmatizer.lemmatize(tokens, tags);
            double[] probs = lemmatizer.probs();

            logger.info("Saving lemmas for the given sentence...");
            logger.debug("WORD -POSTAG : LEMMA    PROBS");
            for(int i=0;i< tokens.length;i++){
                logger.debug(tokens[i]+" -"+tags[i]+" : "+lemmas[i] + "\t" + probs[i]);
                sentence.getTokens().get(i).setLemma(lemmas[i]);
            }
        }

        modelIn.close();
    }
}
