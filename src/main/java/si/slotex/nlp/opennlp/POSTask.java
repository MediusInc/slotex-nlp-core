package si.slotex.nlp.opennlp;

import static si.slotex.nlp.utils.NLPUtils.getDataFile;
import static si.slotex.nlp.utils.NLPUtils.saveModel;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSSample;
import opennlp.tools.postag.POSTaggerFactory;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.postag.WordTagSampleStream;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.TrainingParameters;
import si.slotex.nlp.entity.DocTag;
import si.slotex.nlp.entity.Sentence;

/**
 * Class for management all related to the part-of-speech tagging in the NLP.
 * It can train new training models on the additional data provided or
 * and tag all the available POS tags to the words in the processed document.
 *
 * @author Mitja Kotnik
 * @version 1.0
 */
public class POSTask implements Task
{
    private static Logger logger = LoggerFactory.getLogger(POSTask.class);

    public void train(String modelFilePath, String dataFilePath, String language) throws IOException
    {
        ObjectStream lineStream = getDataFile(dataFilePath);

        ObjectStream<POSSample> sampleStream = new WordTagSampleStream(lineStream);

        TrainingParameters params = new TrainingParameters();
        params.put(TrainingParameters.ITERATIONS_PARAM, 100);
        params.put(TrainingParameters.CUTOFF_PARAM, 5);
        params.put("DataIndexer", "TwoPass");

        POSModel model = POSTaggerME.train(language, sampleStream, params, new POSTaggerFactory());

        saveModel(model, modelFilePath);

        lineStream.close();
        sampleStream.close();
    }

    public static void tag(String modelFilePath, DocTag docTag) throws IOException
    {
        InputStream modelIn = new FileInputStream(modelFilePath);
        POSModel model = new POSModel(modelIn);

        POSTaggerME posTagger = new POSTaggerME(model);

        for (Sentence sentence : docTag.getSentences())
        {
            String[] tokens = new String[sentence.getNumberOfTokens()];
            for(int i = 0; i < sentence.getNumberOfTokens(); i++)
            {
                tokens[i] = sentence.getTokens().get(i).getWord();
            }

            String tags[] = posTagger.tag(tokens);
            double probs[] = posTagger.probs();

            logger.info("Saving POS tags for the given sentence...");
            logger.debug("Token\t:\tTag\t:\tProbability");
            logger.debug("---------------------------------------------");
            for (int i = 0; i < tokens.length; i++)
            {
                logger.debug(tokens[i] + "\t:\t" + tags[i] + "\t:\t" + probs[i]);
                sentence.getTokens().get(i).setPosTag(tags[i]);
            }
        }

        modelIn.close();
    }
}
