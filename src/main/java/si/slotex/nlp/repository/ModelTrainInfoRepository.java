package si.slotex.nlp.repository;

import java.util.Date;

import org.springframework.data.mongodb.repository.MongoRepository;

import si.medius.nlp.model.ModelTrainInfo;

/**
 * Repository for saving data about new training data and new trained models
 * that were executed over our MediusNLP UI.
 *
 * @author Mitja Kotnik
 * @version 1.0
 */
public interface ModelTrainInfoRepository extends MongoRepository<ModelTrainInfo, Long>
{

    ModelTrainInfo findByModelName(String modelName);

    ModelTrainInfo findByVersionName(Long versionName);

    ModelTrainInfo findByTimeStamp(Date timeStamp);

}
