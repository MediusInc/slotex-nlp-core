package si.slotex.nlp.stanford;

import si.slotex.nlp.stanford.tasks.POSTask;

public class StanfordCLI
{

    public static void main(String[] args) throws Exception
    {
//        NERTask nerTask = new NERTask();

//        System.out.println("Started with training of NER model!");
//        nerTask.train();
//
//        System.out.println("Finnished with training of NER model!");

//        nerTask.tag("Dogodek v Ankaranu je bila dramatična nesreča.");


        POSTask posTask = new POSTask();

        posTask.train();
    }
}
