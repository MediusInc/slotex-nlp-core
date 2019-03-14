package si.slotex.nlp.utils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import opennlp.tools.util.InputStreamFactory;
import opennlp.tools.util.MarkableFileInputStreamFactory;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.PlainTextByLineStream;
import opennlp.tools.util.model.BaseModel;

/**
 * A utility class that helps us with training file manipulation.
 *
 * @author Mitja Kotnik
 * @version 1.0
 */
public class NLPUtils
{
    private static Logger logger = LoggerFactory.getLogger(NLPUtils.class);

    /**
     * Method is used for saving additional sentences to our training corpus. The sentences
     * are tagged by the OpenNLP standard.
     *
     * @param lines has all the additional tagged sentences for new training
     * @param model has the name of the model to be trained
     * @param timeVersion version that is represented by the time in milliseconds
     * @return
     */
    public static String saveTrainFile(List<String> lines, String model, Long timeVersion)
    {
        String filePath = model.contains("-") ? "./data/OpenNLP/ner" : model;

        logger.info("Saving train data to folder: " + filePath);
        String fileName = "slo-" + model + ".train";
        String fileNameBckp = "slo-" + model + "-" + timeVersion + ".bckp";
        logger.info("Saving backup copy of " + fileName + " to " + fileNameBckp);

        try
        {
            Files.copy(Paths.get(filePath + File.separator + fileName), Paths.get(filePath + File.separator + fileNameBckp));
        }
        catch (IOException e)
        {
            logger.warn("Cannot create a backup of the following file: " + fileName);
            logger.warn("Error: " + e.toString());
        }

        Path file = Paths.get(filePath + File.separator + fileName);

        try
        {
            Files.write(file, lines, Charset.forName("UTF-8"), StandardOpenOption.APPEND);
        }
        catch (IOException e)
        {
            logger.warn("Cannot write to the following file: " + file.toString());
            logger.warn("Error: " + e.toString());
        }

        logger.info("Successful write additional train data to: " + fileName);

        return filePath + File.separator + fileName;
    }

    /**
     * Method used for saving the newly created model.
     *
     * @param model newly generated model
     * @param modelFile file to which the newly generated model is saved
     * @throws IOException is thrown when we can not open the specified {@code modelFile}
     */
    public static void saveModel(BaseModel model, String modelFile) throws IOException
    {
        BufferedOutputStream modelOut = null;

        try
        {
            modelOut = new BufferedOutputStream(new FileOutputStream(modelFile));
            model.serialize(modelOut);
        }
        finally
        {
            if (modelOut != null)
                modelOut.close();
        }
    }

    /**
     * Before we start training a new model it creates a backup of the old model. It appends the
     * versionTime which is represented by time in milliseconds.
     *
     * @param modelName for which we're going to create a backup
     * @param versionTime is the current time in milliseconds when the model training started
     */
    public static void saveModelBackup(String modelName, String modelFilePath, String modelBckpFilePath, Long versionTime)
    {
        File modelFile = new File(modelFilePath + modelName);

        String modelBckpPath = modelBckpFilePath + modelName.replaceFirst("\\.bin$", "") + "-" + versionTime + ".bin.bckp";
        File modelFileBckp = new File(modelBckpPath);

        logger.info("Creating backup for model: " + modelFile + " with name: " + modelBckpPath);

        if (!modelFile.exists())
        {
            logger.warn("Old model to create backup does not exist: " + modelFile);
            return;
        }

        if (modelFileBckp.exists())
        {
            logger.warn("Backup file for model already exists: " + modelBckpPath);
        }

        if (modelFile.renameTo(modelFileBckp))
        {
            logger.info("Backup for model was sucessfully created: " + modelBckpPath);
        }
        else
        {
            logger.warn("There was an error when creating backup for model: " + modelName);
        }
    }

    /**
     * Is used to get the training file which contains all our training data for a specific model.
     *
     * @param filePath path to the training file
     * @return text that is contained in the training file
     * @throws IOException if the training file does not exist
     */
    public static ObjectStream getDataFile(String filePath) throws IOException
    {
        InputStreamFactory inputStreamFactory = new MarkableFileInputStreamFactory(
                new File(filePath));

        return new PlainTextByLineStream(inputStreamFactory, "UTF-8");
    }

    /**
     *
     *
     * @param sentenceDict
     * @param sentenceStat
     * @return
     */
    public static ArrayList<String> longestCommonSubsequence(String sentenceDict, String sentenceStat)
    {
        String[] sentenceDictWords = sentenceDict.split(" ");
        String[] sentenceStatWords = sentenceStat.split(" ");
        int text1WordCount = sentenceDictWords.length;
        int text2WordCount = sentenceStatWords.length;

        int[][] solutionMatrix = new int[text1WordCount + 1][text2WordCount + 1];

        for (int i = text1WordCount - 1; i >= 0; i--)
        {
            for (int j = text2WordCount - 1; j >= 0; j--)
            {
                if (sentenceDictWords[i].equals(sentenceStatWords[j]))
                {
                    solutionMatrix[i][j] = solutionMatrix[i + 1][j + 1] + 1;
                }
                else
                {
                    solutionMatrix[i][j] = Math.max(solutionMatrix[i + 1][j],
                            solutionMatrix[i][j + 1]);
                }
            }
        }

        int i = 0, j = 0;
        ArrayList<String> lcsResultList = new ArrayList<>();
        while (i < text1WordCount && j < text2WordCount)
        {
            if (sentenceDictWords[i].equals(sentenceStatWords[j]))
            {
                lcsResultList.add(sentenceStatWords[j]);
                i++;
                j++;
            }
            else if (solutionMatrix[i + 1][j] >= solutionMatrix[i][j + 1])
            {
                i++;
            }
            else
            {
                j++;
            }
        }
        return lcsResultList;
    }

    public static String markTextDifferences(String dictSentence, String corpusStat,
            ArrayList<String> lcsList)
    {
        StringBuffer stringBuffer = new StringBuffer();
        if (dictSentence != null && lcsList != null)
        {
            String[] text1Words = dictSentence.split(" ");
            String[] text2Words = corpusStat.split(" ");
            int i = 0, j = 0, word1LastIndex = 0, word2LastIndex = 0;
            for (int k = 0; k < lcsList.size(); k++)
            {
                for (i = word1LastIndex, j = word2LastIndex;
                     i < text1Words.length && j < text2Words.length; )
                {
                    if (text1Words[i].equals(lcsList.get(k)) &&
                            text2Words[j].equals(lcsList.get(k)))
                    {
                        stringBuffer.append(lcsList.get(k) + " ");
                        word1LastIndex = i + 1;
                        word2LastIndex = j + 1;
                        i = text1Words.length;
                        j = text2Words.length;
                    }
                    else if (!text1Words[i].equals(lcsList.get(k)))
                    {
                        for (; i < text1Words.length &&
                                !text1Words[i].equals(lcsList.get(k)); i++)
                        {
                            stringBuffer.append("<DICT>" + text1Words[i] + "</DICT> ");
                        }
                    }
                    else if (!text2Words[j].equals(lcsList.get(k)))
                    {
                        for (; j < text2Words.length &&
                                !text2Words[j].equals(lcsList.get(k)); j++)
                        {
                            stringBuffer.append("<STAT>" + text2Words[j] + "</STAT> ");
                        }
                    }
                }
            }
            for (; word1LastIndex < text1Words.length; word1LastIndex++)
            {
                stringBuffer.append("<DICT>" + text1Words[word1LastIndex] + " </DICT>");
            }
            for (; word2LastIndex < text2Words.length; word2LastIndex++)
            {
                stringBuffer.append("<STAT>" + text2Words[word2LastIndex] + " </STAT>");
            }
        }
        return stringBuffer.toString();
    }
}
