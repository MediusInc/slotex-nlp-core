package si.slotex.nlp.opennlp;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import opennlp.tools.tokenize.TokenSample;
import opennlp.tools.tokenize.TokenSampleStream;
import opennlp.tools.tokenize.TokenizerFactory;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.TrainingParameters;
import si.slotex.nlp.entity.DocTag;
import si.slotex.nlp.entity.Sentence;
import si.slotex.nlp.entity.Token;
import si.slotex.nlp.utils.Constants;
import si.slotex.nlp.utils.NLPUtils;

/**
 * Class for management all related to the tokenizer processing in the NLP.
 * It can train new training models on the additional data provided or
 * detect all the tokens in the provided document. Those tokens are later
 * used for tagging in different units in the NLP workflow.
 *
 * @author Mitja Kotnik
 * @version 1.0
 */
public class TokenTask implements Task
{

    private static Logger logger = LoggerFactory.getLogger(TokenTask.class);

    public static void extract(String modelFilePath, DocTag docTag) throws IOException
    {
        InputStream inputStreamTokenizer = new FileInputStream(modelFilePath);

        TokenizerModel model = new TokenizerModel(inputStreamTokenizer);
        TokenizerME tokenizer = new TokenizerME(model);

        for (int i = 0; i < docTag.getSentences().size(); i++)
        {
            Sentence sentence = docTag.getSentences().get(i);
            String tokens[] = tokenizer.tokenize(sentence.getSentence());

            double tokenProbs[] = tokenizer.getTokenProbabilities();

            Token[] tokens1 = new Token[tokens.length];

            logger.info("Extracting tokens for the given sentence...");
            logger.debug("Token\t: Probability");
            logger.debug("-------------------------------");
            for (int j = 0; j < tokens.length; j++)
            {
                Token token = new Token();
                token.setWord(tokens[j]);
                tokens1[j] = token;
                logger.debug(tokens[j] + "\t: " + tokenProbs[j]);
            }
            sentence.setNumberOfTokens(tokens1.length);
            sentence.setTokens(Arrays.asList(tokens1));
        }

        inputStreamTokenizer.close();
    }

    public static String[] tokenize(String sentence)
    {
        TokenizerModel tokenModel = null;
        try
        {
            InputStream inputStreamTokenizer = new FileInputStream("./models/slo" + Constants.tokenizeModelFile);
            tokenModel = new TokenizerModel(inputStreamTokenizer);
        }
        catch (IOException ex)
        {
            logger.warn("There was an error when opening the tokenizer model files!");
        }
        TokenizerME tokenizer = new TokenizerME(tokenModel);
        return tokenizer.tokenize(sentence);
    }

    public void train(String modelFilePath, String dataFilePath, String language) throws IOException
    {
        ObjectStream<String> lineStream = NLPUtils.getDataFile(dataFilePath);
        ObjectStream<TokenSample> sampleStream = new TokenSampleStream(lineStream);

        TokenizerModel model;
        TokenizerFactory tokenizerFactory = new TokenizerFactory(language, null, false, null);

        try
        {
            model = TokenizerME.train(sampleStream, tokenizerFactory, TrainingParameters.defaultParams());
        }
        finally
        {
            sampleStream.close();
            lineStream.close();
        }

        NLPUtils.saveModel(model, modelFilePath);
    }
}
