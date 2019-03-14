package si.slotex.nlp.service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import si.slotex.nlp.api.exceptions.NothingToProcessException;
import si.medius.nlp.model.Document;
import si.medius.nlp.model.QueueStatus;

/**
 * Used for manipulation of documents between data queues in Redis. It can persist documents
 * to {@code waitQueue} and {@code workQueue}. It can move document(s) between the queues and
 * remove documents from {@code workQueue}.
 *
 * @author Mitja Kotnik
 * @version 1.0
 */
@Service
public class DocumentService
{

    private final String waitQueue = "MediusNLP:waitQueue";
    private final String workQueue = "MediusNLP:workQueue";
    private final RedisTemplate<String, Document> redisTemplate;
    private Logger logger = LoggerFactory.getLogger(DocumentService.class);
    private ListOperations<String, Document> listOps;

    @Autowired
    public DocumentService(RedisTemplate<String, Document> redisTemplate)
    {
        this.redisTemplate = redisTemplate;

        listOps = redisTemplate.opsForList();
    }

    /**
     * Pushes the {@code document} that was given to our Redis {@code waitQueue}.
     *
     * @param document that is going to be saved in our {@code waitQueue}
     */
    public void pushToRedis(Document document)
    {
        logger.info("Starting to save document to Redis queue with ID: {} to {}", document.getDocumentId(), waitQueue);
        if (document.getInsertDate() == null)
        {
            document.setInsertDate(Calendar.getInstance().getTime());
        }
        logger.info("Insert time to REDIS for the document is " + document.getInsertDate().toString());

        listOps.leftPush(waitQueue, document);
        logger.info("Document with ID {} was successfuly saved to Redis {}!", document.getDocumentId(), waitQueue);
    }

    /**
     * Transfers one document between Redis {@code waitQueue} and {@code workQueue}.
     *
     * @return {@code document} that was transfered.
     */
    public Document transferBetweenQueues()
    {
        logger.info("Transfering document from {} to {}", waitQueue, workQueue);

        Document document = listOps.rightPopAndLeftPush(waitQueue, workQueue);

        if (document == null)
        {
            logger.warn("There were no documents to transfer between {} and {}!", waitQueue, workQueue);
            return null;
        }

        logger.info("Successfuly pushed document with ID {} from {} to {}", document.getDocumentId(), waitQueue, workQueue);

        return document;
    }

    /**
     * Moves {@code documentsNumToProcess} documents from the Redis {@code waitQueue} to {@code workQueue}.
     *
     * @param documentsNumToProcess if it is -1 it goes through the whole {@code waitQueue} and moves it to {@code workQueue}.
     * @param transferBetween defines the source and destination queue for transfer. In case it's 0 it moves from {@code waitQueue} to {@code workQueue}
     * when it's 1 it  moves from {@code workQueue} to {@code waitQueue}.
     * @return can be {@literal null}. It returns the list of all documents that were moved between queues.
     */
    public List<Document> transferBetweenQueuesMultiple(Integer documentsNumToProcess, Integer transferBetween)
    {
        final String sourceQueue = (transferBetween == 0 ? waitQueue : workQueue);
        final String destQueue = (transferBetween == 0 ? workQueue : waitQueue);

        if (documentsNumToProcess == -1)
        {
            logger.info("Going to move all the documents in queue...");
            documentsNumToProcess = listOps.size(sourceQueue).intValue();
        }
        logger.info("Moving {} documents from {} to {}", documentsNumToProcess, sourceQueue, destQueue);
        List<Document> documents = new ArrayList<>();
        IntStream.range(0, documentsNumToProcess).forEach(i -> {
            Document doc = listOps.rightPopAndLeftPush(sourceQueue, destQueue);
            if (doc == null)
            {
                logger.warn("There are no more documents in {}!", sourceQueue);
                return;
            }
            logger.info("Moved document with ID {} to {}", doc.getDocumentId(), destQueue);
            documents.add(doc);
        });

        if (documents.size() == 0)
        {
            logger.warn("There were no documents in {}!", sourceQueue);
            return documents;
        }

        logger.info("Successfully moved {} documents to {}!", documents.size(), destQueue);

        return documents;
    }

    /**
     * Removes processed object from our {@code workQueue} if it was successful.
     *
     * @param document that is going to be removed from Redis {@code workQueue}.
     */
    public void removeFinnishedDocument(Document document)
    {
        logger.info("Removing finnished document from {} with ID {}", workQueue, document.getDocumentId());
        Long removed = listOps.remove(workQueue, 1, document);

        logger.info("Removed {} of documents with ID {}", removed, document.getDocumentId());
    }

    /**
     * Removes all documents from the {@code waitQueue}.
     */
    public void removeFromWaitingQueue()
    {
        logger.info("Removing all documents in the waiting queue...");

        Integer documentsInWaitQueue = listOps.size(waitQueue).intValue();
        logger.warn("There are {} of documents to delete from {}!", documentsInWaitQueue, waitQueue);
        IntStream.range(0, documentsInWaitQueue).forEach(i -> {
            Document doc = listOps.rightPop(waitQueue);
            if (doc == null)
            {
                logger.warn("There are no more documents in {}!", waitQueue);
                return;
            }
            logger.info("Removed document with ID {} from {}", doc.getDocumentId(), waitQueue);
        });

        logger.info("Removed all documents from {}.", waitQueue);
    }

    /**
     * Returns the number and IDs of documents that are stored in Redis {@code waitQueue} and {@code workQueue}.
     *
     * @return {@code Status} object with information about our proccessing status.
     */
    public QueueStatus getStatusProcess()
    {
        QueueStatus queueStatus = new QueueStatus();

        queueStatus.setWaitQueueSize(listOps.size(waitQueue));
        queueStatus.setWorkQueueSize(listOps.size(workQueue));
        queueStatus.setWaitDocumentIds(listOps.range(waitQueue, 0, queueStatus.getWaitQueueSize())
                .stream()
                .map(doc -> doc.getDocumentId())
                .collect(Collectors.toList()));
        queueStatus.setWorkDocumentIds(listOps.range(workQueue, 0, queueStatus.getWorkQueueSize())
                .stream()
                .map(doc -> doc.getDocumentId())
                .collect(Collectors.toList()));

        return queueStatus;
    }

    /**
     * Checks if the {@code Document} with the provided ID already exists in the queue.
     *
     * @return true if it exists, else false.
     */
    public boolean checkIfIdInQueue(String documentId)
    {
        List<String> documentsInQueue = new ArrayList<>();

        logger.info("Retrieving documents from queue...");
        documentsInQueue.addAll(listOps.range(waitQueue, 0, listOps.size(waitQueue))
                .stream()
                .map(doc -> doc.getDocumentId())
                .collect(Collectors.toList()));

        logger.info("Checking if document with ID {} is in queue...", documentId);
        return documentsInQueue.contains(documentId);
    }

    /**
     * Checks if there are any {@code Document} in the provided queue.
     *
     * @return true if it exists, else false.
     */
    public Boolean checkIfQueueEmtpy()
    {
        logger.info("Retrieving documents from queue {}...", waitQueue);
        List<Document> documentsInQueue = listOps.range(waitQueue, 0, listOps.size(waitQueue));

        logger.info("Checking if documents exist in queue {}...", waitQueue);
        if (documentsInQueue.size() == 0)
        {
            throw new NothingToProcessException();
        }
        return Boolean.TRUE;
    }
}
