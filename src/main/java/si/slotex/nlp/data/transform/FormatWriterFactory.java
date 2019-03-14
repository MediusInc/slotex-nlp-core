package si.slotex.nlp.data.transform;

public class FormatWriterFactory
{

    public FormatWriter getWriterFormat(String writerFormat)
    {
        if (writerFormat == null)
        {
            return null;
        }
        switch (writerFormat)
        {
            case "OpenNLP":
                return new OpenNLPWriter();
            case "Tsv":
                return new TsvWriter();
        }

        return null;
    }
}
