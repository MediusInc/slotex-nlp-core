package si.slotex.nlp.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import si.slotex.nlp.entity.DocTag;

/**
 * Repository for saving tagged documents to the MongoDB database.
 *
 * @author Mitja Kotnik
 * @version 1.0
 */
public interface DocTagRepository extends MongoRepository<DocTag, Long>
{

    DocTag findByTitle(String title);

    DocTag findByDocumentId(Long id);
}
