package si.slotex.nlp.repository;

import java.math.BigInteger;
import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import si.medius.nlp.model.CorpusSentenceDiff;

/**
 * Repository for saving corpus sentence differences when tagging with statistical and dictonary.
 *
 * @author Mitja Kotnik
 * @version 1.2
 */
public interface CorpusSentenceDiffRepository extends MongoRepository<CorpusSentenceDiff, BigInteger>
{

    CorpusSentenceDiff findByModelAndCorpusLine(String model, Integer corpusLine);

    List<CorpusSentenceDiff> findByModelAndCorpusLineBetween(String model, Integer corpusLineStart, Integer corpusLineEnd);

    List<CorpusSentenceDiff> findAllByModelOrderByCorpusLine(String model);

    void removeByModel(String model);
}
