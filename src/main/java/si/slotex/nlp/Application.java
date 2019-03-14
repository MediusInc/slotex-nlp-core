package si.slotex.nlp;

import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import si.slotex.nlp.config.FileStorageProperties;
import si.medius.nlp.model.CorpusSentenceDiff;
import si.slotex.nlp.service.TrainService;

@SpringBootApplication
@EnableConfigurationProperties({
                                       FileStorageProperties.class
                               })
public class Application
{
    private static Logger logger = LoggerFactory.getLogger(Application.class);

    @Value("${init.tag.model}")
    private boolean tagCorpusData;

    @Autowired
    private TrainService trainService;

    public static void main(String[] args)
    {
        SpringApplication.run(Application.class, args);
    }

    @PostConstruct
    public void postConstruct()
    {

        if (tagCorpusData)
        {
            logger.info("Checking if models are already tagged in the MongoDB database...");
            CorpusSentenceDiff sentenceDiffPer = trainService.findModelsFirstLine("person");
            CorpusSentenceDiff sentenceDiffLoc = trainService.findModelsFirstLine("location");
            CorpusSentenceDiff sentenceDiffOrg = trainService.findModelsFirstLine("organization");

            Calendar cal = new GregorianCalendar();
            cal.add(Calendar.YEAR, -1);

            if (sentenceDiffPer == null || sentenceDiffPer.getTagDate().before(cal.getTime()))
            {
                logger.info("Initial dictionary and statistical tagging...");
                logger.info("Tagging for entity type: person");
                trainService.startTaggingDictAndStat("person");
                logger.info("Finished with initial tagging!");
            }

            if (sentenceDiffLoc == null || sentenceDiffLoc.getTagDate().before(cal.getTime()))
            {
                logger.info("Initial dictionary and statistical tagging...");
                logger.info("Tagging for entity type: location");
                trainService.startTaggingDictAndStat("location");
                logger.info("Finished with initial tagging!");
            }

            if (sentenceDiffOrg == null || sentenceDiffOrg.getTagDate().before(cal.getTime()))
            {
                logger.info("Initial dictionary and statistical tagging...");
                logger.info("Tagging for entity type: organization");
                trainService.startTaggingDictAndStat("organization");
                logger.info("Finished with initial tagging!");
            }
        }
    }
}
