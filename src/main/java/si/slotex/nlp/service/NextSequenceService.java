package si.slotex.nlp.service;

import static org.springframework.data.mongodb.core.FindAndModifyOptions.options;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import si.slotex.nlp.entity.CustomSequences;

/**
 * Used as a utility for generating IDs for mongoDB for sequences
 *
 * @author Mitja Kotnik
 * @version 1.0
 */
@Service
public class NextSequenceService {

    @Autowired
    private MongoOperations mongo;

    /**
     * When saving new data to database we can use this method
     * to retrieve new ID for a specified {@code seqName}. Each time
     * it returns a new value for the provided parameter and increments
     * it each time by 1.
     *
     * @param seqName value of the sequence which we want to get
     * @return new ID for the sequence generated
     */
    public Long getNextSequence(String seqName)
    {
        CustomSequences counter = mongo.findAndModify(
                query(where("_id").is(seqName)),
                new Update().inc("seq",1),
                options().returnNew(true).upsert(true),
                CustomSequences.class);
        return counter.getSeq();
    }
}
