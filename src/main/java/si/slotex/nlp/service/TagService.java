package si.slotex.nlp.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import si.slotex.nlp.exception.ModelNotFoundException;
import si.slotex.nlp.entity.DocTag;
import si.slotex.nlp.entity.Document;
import si.slotex.nlp.entity.Entity;
import si.slotex.nlp.entity.Sentence;
import si.slotex.nlp.entity.TaggedData;
import si.slotex.nlp.entity.Token;
import si.slotex.nlp.opennlp.LangTask;
import si.slotex.nlp.opennlp.LemmaTask;
import si.slotex.nlp.opennlp.NERTask;
import si.slotex.nlp.opennlp.POSTask;
import si.slotex.nlp.opennlp.SentenceTask;
import si.slotex.nlp.opennlp.TokenTask;
import si.slotex.nlp.repository.DocTagRepository;
import si.slotex.nlp.repository.EntityRepository;
import si.slotex.nlp.utils.Constants;

/**
 * Used for tagging of documents that are sent to the endpoint. Also returns
 * data about the documents that were already processed and are saved in MongoDB.
 *
 * @author Mitja Kotnik
 * @version 1.0
 */
@Service
public class TagService
{
    private Logger logger = LoggerFactory.getLogger(TagService.class);

    @Autowired
    private DocTagRepository docTagRepository;

    @Autowired
    private EntityRepository entityRepository;

    @Autowired
    private NextSequenceService nextSequenceService;

    @Value("${file.models}")
    private String modelFilePath;

    private TaggedData taggedData;
    private DocTag docTag;

    /**
     * Processes the provided {@code document} with all the available models.
     *
     * @param document provided for processing and tagging
     * @return all the entities that were found in the document
     */
    public TaggedData tagDocument(Document document)
    {
        taggedData = new TaggedData();
        taggedData.setNumOfTokens(0);
        try
        {
            logger.info("Start of tagging for document with ID: " + document.getDocumentId());

            docTag = LangTask.detect(modelFilePath + Constants.langModelFile, document);
            logger.info("Language detection successful for document with ID: " + docTag.getDocumentId());
            logger.info("Detected language: " + docTag.getLanguage());

            SentenceTask.detect(modelFilePath + "/" + docTag.getLanguage() + Constants.sentenceModelFile, document.getContent(), docTag);
            logger.info("Sentence detection successful for document with ID: " + docTag.getDocumentId());
            logger.info("Number of sentences detected: " + docTag.getNumOfSentences());

            TokenTask.extract(modelFilePath + "/" + docTag.getLanguage() + Constants.tokenizeModelFile, docTag);
            logger.info("Token extraction successful for document with ID: " + docTag.getDocumentId());

            POSTask.tag(modelFilePath + "/" + docTag.getLanguage() + Constants.posModelFile, docTag);
            logger.info("POS tagging successful for document with ID: " + docTag.getDocumentId());

            if (docTag.getLanguage().equals("slo"))
            {
                LemmaTask.tag(modelFilePath + "/" + docTag.getLanguage() + Constants.lemmaModelFile, docTag);
                logger.info("POS tagging successful for document with ID: " + docTag.getDocumentId());
            }

            NERTask.tag(modelFilePath + "/" + docTag.getLanguage() + Constants.nerPersonModelFile, docTag);
            logger.info("NER tagging for persons successful for document with ID: " + docTag.getDocumentId());

            NERTask.tag(modelFilePath + "/" + docTag.getLanguage() + Constants.nerLocationModelFile, docTag);
            logger.info("NER tagging for locations successful for document with ID: " + docTag.getDocumentId());

            NERTask.tag(modelFilePath + "/" + docTag.getLanguage() + Constants.nerOrganizationModelFile, docTag);
            logger.info("NER tagging for organizations successful for document with ID: " + docTag.getDocumentId());
        }
        catch (IOException e)
        {
            logger.error("modelFile was not found!");
            logger.error("Error: " + e.toString());
            throw new ModelNotFoundException("Model file was not found! " + e.toString());
        }

        docTagRepository.save(docTag);

        return saveTaggedData();
    }

    /**
     * Saves the {@code taggedData} to the MongoDB.
     *
     * @return
     */
    private TaggedData saveTaggedData()
    {
        List<Entity> entities = saveEntities();

        taggedData.setDocumentId(docTag.getDocumentId());
        taggedData.setLanguage(docTag.getLanguage());
        taggedData.setLanguageProb(docTag.getLanguageProb());
        taggedData.setNumOfSentences(docTag.getNumOfSentences());
        taggedData.setNumOfEntities(entities.size());
        taggedData.setEntities(entities);

        logger.info("Saved to MongoDB");

        return taggedData;
    }

    /**
     * Saves the entities found in the processed document to the MongoDB.
     *
     * @return
     */
    private List<Entity> saveEntities()
    {
        List<Entity> entities = new ArrayList<>();
        for (Sentence sentence : docTag.getSentences())
        {
            for (Token token : sentence.getTokens())
            {
                if (token.getNerTag() != null)
                {
                    Entity entity = entityRepository.findByWord(token.getWord());
                    if (entity == null)
                    {
                        entity = new Entity();
                        entity.setId(nextSequenceService.getNextSequence("entity_seq"));
                        entity.setWord(token.getWord());
                        entity.setType(token.getNerTag());
                    }
                    if (entity.getDocumentIds() == null)
                    {
                        entity.setDocumentIds(new ArrayList<>());
                    }
                    entity.getDocumentIds().add(docTag.getDocumentId());
                    entityRepository.save(entity);
                    entities.add(entity);
                }
            }
            taggedData.setNumOfTokens(taggedData.getNumOfTokens() + sentence.getTokens().size());
        }
        return entities;
    }

    /**
     * Searches for the {@code DocTag} in the MongoDB by ID.
     *
     * @param id of the DocTag that is searched
     * @return found DocTag
     */
    public DocTag findDocTagById(Long id)
    {
        logger.info("Searching for document with ID: " + id + "...");
        return docTagRepository.findByDocumentId(id);
    }

    /**
     * Searches for all the tagged documents in our MongoDB.
     *
     * @return
     */
    public List<DocTag> findAllDocs()
    {
        logger.info("Searching for documents...");
        return docTagRepository.findAll();
    }

    /**
     * Searches for all the found entities in the documents we've processed.
     *
     * @return
     */
    public List<Entity> findAllEntities()
    {
        logger.info("Searching for entities...");
        return entityRepository.findAll();
    }
}
