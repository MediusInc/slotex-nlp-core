package si.slotex.nlp.data;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import si.slotex.nlp.data.reader.Doc;
import si.slotex.nlp.data.reader.DocReaders;
import si.slotex.nlp.data.transform.FormatWriter;
import si.slotex.nlp.data.transform.FormatWriterFactory;

public class TransformData
{

    public static void main(String[] args) throws IOException, XMLStreamException
    {
        if (args.length != 4)
        {
            throw new RuntimeException("\nrun: TransformData outType fileIn fileOut NLP-tool\n" +
                    "\toutType - type for which the format of output file has to be:\n" +
                    "\t\tTsv\n" +
                    "\t\tOpenNLP\n" +
                    "\tfileIn - path to the file that comes in\n" +
                    "\tfileOut - path of the fiel that is generated\n" +
                    "\tNLP-tool - available tools for NLP manipulation are:\n" +
                    "\t\tLanguageDetector\n" +
                    "\t\tSentenceDetector\n" +
                    "\t\tTokenizer\n" +
                    "\t\tPOSTagger\n" +
                    "\t\tLemmatizer\n" +
                    "\t\tNameEntityRecognition\n");
        }
        String outType = args[0];
        String fileIn = args[1];
        String fileOut = args[2];
        String type = args[3];

        System.out.println("Opening " + fileIn);
        System.out.println("Transforming data for output of type " + outType);
        //        TsvWriter wri = new TsvWriter();
        //        OpenNLPWriter wri = new OpenNLPWriter();
        FormatWriterFactory writerFactory = new FormatWriterFactory();
        FormatWriter wri = writerFactory.getWriterFormat(outType);

        List<Doc> docs = DocReaders.openFile(new File(fileIn));
        FileOutputStream fos = new FileOutputStream(fileOut);
        System.out.println("Writing to " + fileOut);
        for (Doc d : docs)
        {
            wri.write(d, fos, type);
        }
        fos.flush();
        fos.close();
        System.out.println("Done!");
    }
}
