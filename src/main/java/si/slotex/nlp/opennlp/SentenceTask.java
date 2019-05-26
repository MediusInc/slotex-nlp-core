package si.slotex.nlp.opennlp;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import opennlp.tools.sentdetect.SentenceDetectorFactory;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.sentdetect.SentenceSample;
import opennlp.tools.sentdetect.SentenceSampleStream;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.TrainingParameters;
import si.slotex.nlp.entity.DocTag;
import si.slotex.nlp.entity.Sentence;
import si.slotex.nlp.utils.NLPUtils;

/**
 * Class for management all related to the sentence processing in the NLP.
 * It can train new training models on the additional data provided or
 * detect sentences that are present in the processed document.
 *
 * @author Mitja Kotnik
 * @version 1.0
 */
public class SentenceTask implements Task
{

    private static Logger logger = LoggerFactory.getLogger(SentenceTask.class);

    public void train(String modelFilePath, String dataFilePath, String language) throws IOException
    {
        modelFilePath = (modelFilePath.length() > 0 ? modelFilePath : "./models/sl-sentence.bin");

        ObjectStream<String> lineStream = NLPUtils.getDataFile(dataFilePath);

        ObjectStream<SentenceSample> sampleStream = new SentenceSampleStream(lineStream) {};

        TrainingParameters mlParams = new TrainingParameters();
        mlParams.put(TrainingParameters.ITERATIONS_PARAM, "50");
        mlParams.put(TrainingParameters.CUTOFF_PARAM, 5);

        SentenceModel model = SentenceDetectorME.train(language, sampleStream, new SentenceDetectorFactory("sl", true, null, null), mlParams);

        NLPUtils.saveModel(model, modelFilePath);

        lineStream.close();
        sampleStream.close();
    }

    public static void detect(String modelFilePath, String content, DocTag docTag) throws IOException
    {

        InputStream inputStreamSentence = new FileInputStream(modelFilePath);

        SentenceModel model = new SentenceModel(inputStreamSentence);

        SentenceDetectorME sentDetector = new SentenceDetectorME(model);

        logger.info("Content of document: " + content);

        String[] sents = sentDetector.sentDetect(content);
        logger.info("---------Sentences Detected by the SentenceDetector ME class using the generated model-------");
        docTag.setNumOfSentences(sents.length);
        docTag.setSentences(new ArrayList<>(sents.length));

        for (int i = 0; i < sents.length; i++)
        {
            Sentence sentence = new Sentence();
            sentence.setSentence(sents[i]);
            docTag.getSentences().add(i, sentence);
            logger.info("Sentence " + (i + 1) + " : " + sents[i]);
        }

        inputStreamSentence.close();
    }
}
