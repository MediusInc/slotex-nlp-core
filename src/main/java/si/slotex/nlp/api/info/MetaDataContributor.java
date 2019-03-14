package si.slotex.nlp.api.info;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.stereotype.Component;

import si.slotex.nlp.repository.EntityRepository;

@Component
public class MetaDataContributor implements InfoContributor
{

    @Autowired
    private EntityRepository entityRepository;

    @Override
    public void contribute(Info.Builder builder)
    {
        Map<String, Object> details = new HashMap<>();
        details.put("detectedEntities", entityRepository.count());

        builder.withDetail("details", details);
    }
}
