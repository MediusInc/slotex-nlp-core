package si.slotex.nlp.api;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import si.slotex.nlp.exception.BadRequestException;
import si.slotex.nlp.entity.DocsProcessed;
import si.slotex.nlp.entity.Document;
import si.slotex.nlp.entity.QueueStatus;
import si.slotex.nlp.entity.TaggedData;
import si.slotex.nlp.service.DocumentService;
import si.slotex.nlp.service.TagService;

@RestController
@RequestMapping("admin")
public class AdminController
{
    private static Logger logger = LoggerFactory.getLogger(AdminController.class);

    @Autowired
    private DocumentService documentService;

    @Autowired
    private TagService tagService;

    @GetMapping("/status")
    public QueueStatus getStatusProcess()
    {
        return documentService.getStatusProcess();
    }

    @GetMapping("/process/{docNum}")
    @ResponseStatus(HttpStatus.CREATED)
    public DocsProcessed processDocumentsFromWaitQueue(@PathVariable Integer docNum)
    {
        DocsProcessed docsProcessed = new DocsProcessed();
        docsProcessed.setProcessStartTime(Calendar.getInstance().getTime());

        if (docNum < 0)
        {
            if(docNum == -1)
            {
                QueueStatus status = documentService.getStatusProcess();
                docNum = status.getWaitQueueSize().intValue();
            }
            else
            {
                throw new BadRequestException("Value of the has to be positive. In case you want to process all documents provide -1.");
            }
        }

        List<Document> documents = documentService.transferBetweenQueuesMultiple(docNum, 0);
        logger.info("Got back {}/{} of documents to tag!", documents.size(), docNum);

        List<TaggedData> taggedData = new ArrayList<>();
        List<String> processedIds = new ArrayList<>();
        if (documents.size() > 0)
        {
            logger.info("Start of tagging {} documents...", documents.size());
            for (Document document : documents)
            {
                logger.info("Tagging document with ID " + document.getDocumentId() + "...");
                taggedData.add(tagService.tagDocument(document));
                processedIds.add(document.getDocumentId());
                documentService.removeFinnishedDocument(document);
                logger.info("Tagging  of document with ID {} was successful!", document.getDocumentId());
            }
            logger.info("Tagging of documents was finnished!");
        }
        else
        {
            logger.warn("There were no documents returned to process.");
        }

        docsProcessed.setProcessedIds(processedIds);
        docsProcessed.setNumOfProcessed(processedIds.size());
        docsProcessed.setProcessEndTime(Calendar.getInstance().getTime());
        return docsProcessed;
    }

    @GetMapping("/return")
    @ResponseStatus(HttpStatus.CREATED)
    public void returnDocumentsToWaitQueue()
    {
        logger.info("Starting to return documents back for processing.");
        List<Document> documents = documentService.transferBetweenQueuesMultiple(-1, 1);
        logger.info("Returned {} of documents for processing!", documents.size());
    }

    @PostMapping(value = "/push", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity pushDocToRedis(@RequestBody Document document)
    {
        logger.info("Check if document with ID {} is in the REDIS queue...", document.getDocumentId());
        if(documentService.checkIfIdInQueue(document.getDocumentId()))
        {
            logger.info("Document with ID {} is already in the REDIS queue...", document.getDocumentId());
            return new ResponseEntity(HttpStatus.CONFLICT);
        }
        else
        {
            logger.info("Pushing document with ID {} to REDIS queue...", document.getDocumentId());
            documentService.pushToRedis(document);
            return new ResponseEntity(HttpStatus.CREATED);
        }
    }

    @GetMapping(value = "/remove-queue", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeDocsFromWaitingQueue()
    {
        logger.info("Removing all documents from the waiting queue...");
        if (documentService.checkIfQueueEmtpy())
        {
            logger.info("Documents exists in waiting queue.");
            documentService.removeFromWaitingQueue();
            logger.info("Successfully removed documents from wait queue.");
        }
    }
}
