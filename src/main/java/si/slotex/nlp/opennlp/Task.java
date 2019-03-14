package si.slotex.nlp.opennlp;

import java.io.IOException;

public interface Task
{

    void train(String modelFilePath, String dataFilePath, String language) throws IOException;
}
