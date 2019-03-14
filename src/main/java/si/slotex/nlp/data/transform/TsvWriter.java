package si.slotex.nlp.data.transform;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.List;

import si.slotex.nlp.data.model.Token;
import si.slotex.nlp.data.reader.Doc;
import si.slotex.nlp.data.util.ManipulateUtil;

public class TsvWriter implements FormatWriter
{

    public TsvWriter()
    {

    }

    public void write(Doc doc, OutputStream os, String nlpType) throws IOException
    {
        BufferedWriter wri = new BufferedWriter(new OutputStreamWriter(os));
        for (List<Token> sentence : doc.getSentences())
        {
            switch (nlpType)
            {
                case "NameEntityRecognition":
                    writeSentenceNer(wri, sentence);
                    break;
            }
        }
        wri.flush();
        wri.close();
    }

    public static void writeSentence(Writer wri, List<Token> sentence)
            throws IOException
    {
        int count = 1;
        for (Token tok : sentence)
        {
            if (ManipulateUtil.checkValidSigns(tok.getLiteral()))
                continue;

            wri.write(Integer.toString(count++));
            wri.write('\t');

            if (tok.getLiteral() != null)
            {
                wri.write(tok.getLiteral());
                wri.write('\t');
            }

            if (tok.getLemma() != null)
            {
                wri.write(tok.getLemma());
                wri.write('\t');
            }

            if (tok.getPos() != null)
            {
                wri.write(ManipulateUtil.mappingPos(tok.getPos()));
                wri.write('\t');
            }
            else
            {
                if (Arrays.asList(".", ",", "!", "?", ":", ";", "…").contains(tok.getLiteral()))
                {
                    wri.write("PUNC");
                    wri.write('\t');
                    wri.write('-');
                    wri.write('\t');
                }
            }

            if (tok.getTokenClass() != null)
            {
                wri.write(tok.getTokenClass());
            }
            else
            {
                wri.write('-');
            }
            wri.write('\n');
        }
        wri.write('\n');
    }

    public static void writeSentenceNer(Writer wri, List<Token> sentence)
            throws IOException
    {
        for (Token tok : sentence)
        {
            if (ManipulateUtil.checkValidSigns(tok.getLiteral()))
                continue;


            if (tok.getLiteral() != null)
            {
                wri.write(tok.getLiteral());
                wri.write('\t');
            }

            if (tok.getTokenClass() != null)
            {
                wri.write(tok.getTokenClass());
            }
            else
            {
                wri.write('O');
            }
            wri.write('\n');
        }
        wri.write('\n');
    }
}
