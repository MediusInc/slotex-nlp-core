package si.slotex.nlp.api.info;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class ModelsExistHealthIndicator implements HealthIndicator
{

    @Value("${file.models}")
    private String modelFilePath;

    @Override
    public Health health()
    {
        try
        {
            Health.Builder healthBuilder;
            healthBuilder = Health.up();

            int sloNerModelsNum = FileUtils.listFiles(new File(modelFilePath), new RegexFileFilter("slo-ner+(.*).bin"), null).size();
            int engNerModelsNum = FileUtils.listFiles(new File(modelFilePath), new RegexFileFilter("eng-ner+(.*).bin"), null).size();

            healthBuilder.withDetail("sloNerModelsNum", sloNerModelsNum);
            healthBuilder.withDetail("engNerModelsNum", engNerModelsNum);

            return healthBuilder.build();
        }
        catch (Exception e)
        {
            return Health.down(e).build();
        }
    }
}
