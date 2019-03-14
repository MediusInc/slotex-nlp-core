package si.slotex.nlp.stanford.tasks;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.process.PTBTokenizer;

public class TokenTask
{

    public DocumentPreprocessor tokenizeDocument(String fileName)
    {
        DocumentPreprocessor dp = new DocumentPreprocessor(fileName);

        for(List<HasWord> sentence :  dp)
        {
            System.out.println(sentence);
        }

        return dp;
    }

    public Set tokenizePTB(String fileName) throws FileNotFoundException
    {
        Set labels = new HashSet<>();
        PTBTokenizer ptbt = new PTBTokenizer<>(new FileReader(fileName), new CoreLabelTokenFactory(), "");

        while (ptbt.hasNext())
        {
            CoreLabel label = (CoreLabel) ptbt.next();
            System.out.println(label);
            labels.add(label);
        }

        return labels;
    }

    public void train()
    {

    }
}
