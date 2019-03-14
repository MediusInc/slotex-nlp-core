package si.slotex.nlp.opennlp;

import static si.slotex.nlp.utils.NLPUtils.getDataFile;
import static si.slotex.nlp.utils.NLPUtils.saveModel;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import opennlp.tools.dictionary.Dictionary;
import opennlp.tools.ml.EventTrainer;
import opennlp.tools.namefind.BioCodec;
import opennlp.tools.namefind.DictionaryNameFinder;
import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.NameSample;
import opennlp.tools.namefind.NameSampleDataStream;
import opennlp.tools.namefind.TokenNameFinder;
import opennlp.tools.namefind.TokenNameFinderEvaluator;
import opennlp.tools.namefind.TokenNameFinderFactory;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.Span;
import opennlp.tools.util.StringList;
import opennlp.tools.util.TrainingParameters;
import opennlp.tools.util.eval.FMeasure;
import si.medius.nlp.model.CorpusSentenceDiff;
import si.medius.nlp.model.DocTag;
import si.medius.nlp.model.Sentence;
import si.slotex.nlp.utils.Constants;
import si.slotex.nlp.utils.NLPUtils;

/**
 * Class for management all related to the named-entity-recognition processing in the NLP.
 * It can train new training models on the additional data provided or
 * detect entities that are present in the processed document.
 *
 * @author Mitja Kotnik
 * @version 1.0
 */
public class NERTask implements Task
{
    private static Logger logger = LoggerFactory.getLogger(NERTask.class);

    public static void tag(String modelFilePath, DocTag docTag) throws IOException
    {
        findNameModel(modelFilePath, docTag);
    }

    private static void findNameModel(String modelFilePath, DocTag docTag) throws IOException
    {
        logger.info("Executing NameFinder method .findNameModel");
        InputStream modelIn = new FileInputStream(modelFilePath);
        TokenNameFinderModel model = new TokenNameFinderModel(modelIn);

        TokenNameFinder nameFinder = new NameFinderME(model);

        for (Sentence sentence : docTag.getSentences())
        {
            String[] tokens = new String[sentence.getNumberOfTokens()];
            for (int i = 0; i < sentence.getNumberOfTokens(); i++)
            {
                tokens[i] = sentence.getTokens().get(i).getWord();
            }

            Span nameSpans[] = nameFinder.find(tokens);
            printNERFinds(nameSpans, tokens, sentence);
        }
        logger.info("Statistical tagging was successful!");
    }

    private static String buildCorpusStat(String modelFilePath, String sentence) throws IOException
    {
        logger.info("Executing NameFinder method .buildCorpusStat");
        InputStream modelIn = new FileInputStream(modelFilePath);
        TokenNameFinderModel model = new TokenNameFinderModel(modelIn);

        TokenNameFinder nameFinder = new NameFinderME(model);

        String[] tokens = TokenTask.tokenize(sentence);

        Span nerSpans[] = nameFinder.find(tokens);

        return buildSentence(nerSpans, tokens, sentence);
    }

    private static String buildCorpusDict(String dictionaryDir, String sentence) throws IOException
    {
        logger.info("Executing NameFinder method .buildCorpusDict");
        Dictionary dictionary = new Dictionary();

        try (Stream<String> stream = Files.lines(Paths.get(dictionaryDir + "/lexicons_stat.si_moska_imena.list")))
        {
            stream.forEach(line -> dictionary.put(new StringList(line)));
        }

        TokenNameFinder dictionaryNER = new DictionaryNameFinder(dictionary, "person");

        String[] tokens = TokenTask.tokenize(sentence);

        Span nerSpans[] = dictionaryNER.find(tokens);

        return buildSentence(nerSpans, tokens, sentence);
    }

    private static String buildSentence(Span[] nerSpans, String[] tokens, String sentence)
    {
        if (nerSpans.length == 0)
        {
            return sentence;
        }

        List<String> newSentence = new ArrayList<>();
        int span = 0;
        for (int i = 0; i < tokens.length; i++)
        {
            if (span < nerSpans.length)
            {
                if (nerSpans[span].getStart() == i && Character.isUpperCase(tokens[i].charAt(0)))
                {
                    newSentence.add("<START:" + nerSpans[span].getType() + "> " + tokens[i] + " <END>");
                    span++;
                }
                else
                {
                    newSentence.add(tokens[i]);
                }
            }
            else
            {
                newSentence.add(tokens[i]);
            }
        }

        return StringUtils.join(newSentence, " ");
    }

    private static void printNERFinds(Span[] nameSpans, String[] tokens, Sentence sentence)
    {

        logger.info("Saving NER tags for the given sentence...");
        for (int i = 0; i < nameSpans.length; i++)
        {
            logger.debug("Token\t:\tNER\t:\tProbability");
            logger.debug("---------------------------------------------");
            for (int j = nameSpans[i].getStart(); j < nameSpans[i].getEnd(); j++)
            {
                logger.debug(tokens[j] + "\t:" + nameSpans[i].getType() + "\t:" + nameSpans[i].getProb());
                sentence.getTokens().get(j).setNerTag(nameSpans[i].getType());
            }
        }
    }

    public static void evaluate(String modelFilePath, String dataFilePath) throws IOException
    {
        InputStream modelIn = new FileInputStream(modelFilePath);
        TokenNameFinderModel model = new TokenNameFinderModel(modelIn);

        TokenNameFinderEvaluator evaluator = new TokenNameFinderEvaluator(new NameFinderME(model));

        ObjectStream<String> lineStream = getDataFile(dataFilePath);

        ObjectStream<NameSample> sampleStream = new NameSampleDataStream(lineStream);

        evaluator.evaluate(sampleStream);

        FMeasure result = evaluator.getFMeasure();

        logger.info("\n" + result.toString());
    }

    public void train(String modelFilePath, String dataFilePath, String language) throws IOException
    {
        ObjectStream<String> lineStream = getDataFile(dataFilePath);

        ObjectStream<NameSample> sampleStream = new NameSampleDataStream(lineStream);

        TrainingParameters mlParams = new TrainingParameters();
        mlParams.put(TrainingParameters.ALGORITHM_PARAM, "MAXENT");
        mlParams.put(TrainingParameters.TRAINER_TYPE_PARAM, EventTrainer.EVENT_VALUE);
        mlParams.put(TrainingParameters.ITERATIONS_PARAM, "100");
        mlParams.put(TrainingParameters.CUTOFF_PARAM, "5");

        String type = modelFilePath.substring(modelFilePath.lastIndexOf('-') + 1, modelFilePath.lastIndexOf('.'));
        TokenNameFinderModel model;

        try
        {
            model = NameFinderME.train(language, type, sampleStream, mlParams,
                    TokenNameFinderFactory.create(null, null, Collections.<String, Object>emptyMap(), new BioCodec()));
        }
        finally
        {
            sampleStream.close();
        }

        saveModel(model, modelFilePath);
    }

    public List<CorpusSentenceDiff> executeNewModelTagging(String dataDir, String dictonaryDir, String modelType, String modelFilePath)
    {
        List<CorpusSentenceDiff> corpusSentenceDiffs = new ArrayList<>();
        try
        {
            List<String> lines = FileUtils.readLines(new File(dataDir + "/OpenNLP/ner/slo-ner-person.data"), "UTF-8");

            logger.info("Starting to tag new dictionary file...");

            int i = 1;
            for (String line : lines)
            {
                line = normalizeText(line);
                String dictSentence = buildCorpusDict(dictonaryDir, line);
                String statSentence = buildCorpusStat(modelFilePath + "/slo" + Constants.nerPersonModelFile, line);
                logger.info("Tagging line {}/{}", i, lines.size());

                ArrayList<String> longestCommonSubsequenceList = NLPUtils.longestCommonSubsequence(dictSentence, statSentence);
                String result = NLPUtils.markTextDifferences(dictSentence, statSentence, longestCommonSubsequenceList);

                CorpusSentenceDiff sentenceDiff = new CorpusSentenceDiff();
                sentenceDiff.setTagDate(Calendar.getInstance().getTime());
                sentenceDiff.setModel(modelType);
                sentenceDiff.setCorpusLine(i);
                sentenceDiff.setSentence(result);
                sentenceDiff.setEdit(false);
                sentenceDiff.setDiff((result.contains("<STAT>") || result.contains("<DICT>")));
                corpusSentenceDiffs.add(sentenceDiff);

                i++;
            }
            logger.info("Finnished tagging new dictionary file!");

        }
        catch (IOException ex)
        {
            logger.warn("There was an exception reading from files on disk!");
        }

        return corpusSentenceDiffs;
    }

    private String normalizeText(String text)
    {

        text = text.trim();
        //        text = text.replace("\n", " ");
        //        text = text.replace("\t", " ");

        while (text.contains("  "))
        {
            text = text.replace("  ", " ");
        }
        return text;
    }
}
