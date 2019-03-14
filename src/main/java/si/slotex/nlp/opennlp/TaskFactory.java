package si.slotex.nlp.opennlp;

public class TaskFactory
{

    public static Task createTask(String task)
    {
        switch (task)
        {
            case "stc":
                return new SentenceTask();
            case "lang":
                return new LangTask();
            case "token":
                return new TokenTask();
            case "pos":
                return new POSTask();
            case "lemma":
                return new LemmaTask();
            case "ner":
                return new NERTask();
        }
        return null;
    }
}
