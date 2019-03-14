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

public class OpenNLPWriter implements FormatWriter
{
    public OpenNLPWriter()
    {
    }

    public void write(Doc doc, OutputStream os, String nlpType) throws IOException
    {
        BufferedWriter wri = new BufferedWriter(new OutputStreamWriter(os));
        for (List<Token> sentence : doc.getSentences())
        {
            switch (nlpType)
            {
                case "POSTagger":
                    writeSentencePoS(wri, sentence);
                    break;
                case "NameEntityRecognition":
                    writeSentenceNER(wri, sentence);
                    break;
                case "Tokenizer":
                    writeSentenceToken(wri, sentence);
                    break;
                case "SentenceDetector":
                    writeSentence(wri, sentence);
                    break;
                case "LanguageDetector":
                    writeSentenceLang(wri, sentence);
                    break;
                case "Lemmatizer":
                    writeSentenceLemma(wri, sentence);
            }
        }
        wri.flush();
        wri.close();
    }


    public static void writeSentence(Writer wri, List<Token> sentence)
            throws IOException
    {
        int st = 0;
        for (Token tok : sentence)
        {
            if (ManipulateUtil.checkValidSigns(tok.getLiteral()))
                continue;

            if (tok.getLiteral() != null)
            {
                if (!(tok.getLiteral().equals(",") || tok.getLiteral().equals(".")) && st > 0)
                {
                    wri.write(' ');
                }
                wri.write(tok.getLiteral());
            }
            st++;
        }
        wri.write('\n');
    }

    public static void writeSentenceLang(Writer wri, List<Token> sentence)
            throws IOException
    {
        wri.write("slo\t");

        for (Token tok : sentence)
        {
            if (ManipulateUtil.checkValidSigns(tok.getLiteral()))
                continue;

            if (tok.getLiteral() != null)
            {
                if (!(tok.getLiteral().equals(",") || tok.getLiteral().equals(".")))
                {
                    wri.write(' ');
                }
                wri.write(tok.getLiteral());
            }
        }
        wri.write('\n');
    }

    public static void writeSentencePoS(Writer wri, List<Token> sentence)
            throws IOException
    {
        for (Token tok : sentence)
        {
            if (ManipulateUtil.checkValidSigns(tok.getLiteral()))
                continue;

            if (tok.getLiteral() != null)
            {
                wri.write(tok.getLiteral());
                wri.write('_');
            }

            if (Arrays.asList(",", ".", "!", "?").contains(tok.getLiteral()))
            {
                wri.write(tok.getLiteral());
                wri.write(' ');
            }

            if (tok.getPos() != null)
            {
                wri.write(ManipulateUtil.mappingPos(tok.getPos()));
                wri.write(' ');
            }
        }
        wri.write('\n');
    }

    public static void writeSentenceLemma(Writer wri, List<Token> sentence)
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

            if (Arrays.asList(",", ".", "!", "?").contains(tok.getLiteral()))
            {
                wri.write(tok.getLiteral());
                wri.write('\t');
            }

            if (tok.getPos() != null)
            {
                wri.write(ManipulateUtil.mappingPos(tok.getPos()));
                wri.write('\t');
            }

            if (tok.getLemma() != null)
            {
                wri.write(tok.getLemma());
            }
            else
            {
                wri.write('O');
            }

            wri.write('\n');
        }
        wri.write('\n');
    }

    public static void writeSentenceNER(Writer wri, List<Token> sentence)
            throws IOException
    {
        for (Token tok : sentence)
        {
            if (ManipulateUtil.checkValidSigns(tok.getLiteral()))
                continue;

            if (tok.getTokenClass() != null && tok.getTokenClass().equals("stvarno"))
            {
                wri.write("<START: " + tok.getTokenClass() + ">");
            }

            if (tok.getLiteral() != null)
            {
                wri.write(tok.getLiteral());
                wri.write(' ');
            }

            if (Arrays.asList(",", ".", "!", "?").contains(tok.getLiteral()))
            {
                wri.write(' ');
            }

            if (tok.getTokenClass() != null && tok.getTokenClass().equals("stvarno"))
            {
                wri.write("<END> ");
            }
        }
        wri.write('\n');
    }

    public static void writeSentenceToken(Writer wri, List<Token> sentence)
            throws IOException
    {
        for (Token tok : sentence)
        {
            if (ManipulateUtil.checkValidSigns(tok.getLiteral()))
                continue;

            if (Arrays.asList(",", ".", "!", "?").contains(tok.getLiteral()))
            {
                wri.write("<SPLIT>");
            }

            if (tok.getLiteral() != null)
            {
                wri.write(tok.getLiteral());
                wri.write(' ');
            }
        }
        wri.write('\n');
    }
}
