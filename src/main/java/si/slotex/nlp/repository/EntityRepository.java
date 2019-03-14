package si.slotex.nlp.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import si.medius.nlp.model.Entity;

/**
 * Repository that saves all found entities in the processed document to the
 * MongoDB database. It contains also a list of all the specified documents
 * where the entity was found.
 *
 * @author Mitja Kotnik
 * @version 1.0
 */
public interface EntityRepository extends MongoRepository<Entity, Long>
{

    Entity findByWord(String word);
}
