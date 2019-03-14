package si.slotex.nlp.stanford.tasks;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import edu.stanford.nlp.tagger.maxent.TaggerConfig;
import edu.stanford.nlp.util.StringUtils;

public class POSTask
{

    public void train() throws Exception
    {
        String prop = "./data/Stanford/props/sl-pos.prop";
        Properties props = StringUtils.propFileToProperties(prop);

        int size = props.size();
        String[] args = new String[size*2];

        Map<String, String> properties = new HashMap<>();

        int i = 0;
        for (final String name : props.stringPropertyNames())
        {
            properties.put(name, props.getProperty(name));
            args[i++] = name;
            args[i++] = props.getProperty(name);
        }

        TaggerConfig taggerConfig = new TaggerConfig(props);

        MaxentTagger.main(args);

    }

    public void tag(String text)
    {
        MaxentTagger maxentTagger = new MaxentTagger("model.tagger");
        String tag = maxentTagger.tagString(text);
        String[] eachTag = tag.split("\\s+");

        System.out.println("Word\t" + "Stanford tag");
        System.out.println("------------------------------------");
        for (int i = 0; i < eachTag.length; i++)
        {
            System.out.println(eachTag[i].split("_")[0] + "\t" + eachTag[i].split("_")[1]);
        }
    }
}
