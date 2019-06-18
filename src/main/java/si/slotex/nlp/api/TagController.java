package si.slotex.nlp.api;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import si.slotex.nlp.entity.DocTag;
import si.slotex.nlp.entity.Document;
import si.slotex.nlp.entity.Entity;
import si.slotex.nlp.entity.TaggedData;
import si.slotex.nlp.service.TagService;

@RestController
public class TagController
{

    private static Logger logger = LoggerFactory.getLogger(TagController.class);

    @Autowired
    private TagService tagService;

    @PostMapping("/tag")
    @ResponseStatus(HttpStatus.CREATED)
    public TaggedData nlpTagDocument(@RequestBody Document document)
    {
        logger.info("Got document to process with ID: " + document.getDocumentId());
        return tagService.tagDocument(document);
    }

    @GetMapping("/entities")
    public List<Entity> nlpGetTaggedEntities()
    {
        logger.info("Got request for getting all tagged entities..");
        return tagService.findAllEntities();
    }

    @GetMapping("/docs")
    public List<DocTag> nlpGetDocuments()
    {
        logger.info("Got request for getting all tagged documents..");
        return tagService.findAllDocs();
    }

    @GetMapping("/doc/{id}")
    public DocTag nlpGetDocumentById(@PathVariable Long id)
    {
        logger.info("Got request for finding document with ID: " + id);
        return tagService.findDocTagById(id);
    }
}
