package si.slotex.nlp.data.transform;

import java.io.IOException;
import java.io.OutputStream;

import si.slotex.nlp.data.reader.Doc;

public interface FormatWriter
{

    public void write(Doc doc, OutputStream os, String nlpType) throws IOException;
}
