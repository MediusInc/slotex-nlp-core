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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import si.medius.nlp.model.CorpusSentenceDiff;
import si.medius.nlp.model.DocTrain;
import si.medius.nlp.model.ModelTrainInfo;
import si.slotex.nlp.service.TrainService;

@RestController
public class TrainController
{

    private static Logger logger = LoggerFactory.getLogger(TrainController.class);

    @Autowired
    private TrainService trainService;

    @PostMapping("/train")
    @ResponseStatus(HttpStatus.CREATED)
    public ModelTrainInfo nlpTrainDocument(@RequestBody DocTrain docTrain)
    {
        logger.info("Got tagged data to train models: " + docTrain.getModelsToTrain());
        return trainService.trainDocument(docTrain);
    }

    @GetMapping("/models")
    public List<ModelTrainInfo> nlpGetAllModels()
    {
        logger.info("Got request for finding all models");
        return trainService.findAllModels();
    }

    @GetMapping("/retrain/{modelName}")
    public void nerRetrainWithCorrected(@PathVariable String modelName)
    {
        logger.info("Got request for retraining with corrected corpus!");
        trainService.retrainNerModel(modelName);
    }

    @GetMapping("/corpus/{modelName}")
    public List<CorpusSentenceDiff> nerCorrectCorpus(@PathVariable String modelName, @RequestParam("startLine") Integer startLine, @RequestParam("endLine") Integer endLine)
    {
        logger.info("Got request for tagging corpuses with corrected corpus! Paging between " + startLine + " and " + endLine);
        return trainService.modelSentencesBetween(modelName, startLine, endLine);
    }

    @PostMapping("/corpus/{modelName}")
    @ResponseStatus(HttpStatus.CREATED)
    public void corpusCorrection(@PathVariable String modelName, @RequestBody List<CorpusSentenceDiff> corpusSentenceDiff)
    {
        logger.info("Saving correction of corpus into database...");
        trainService.saveCorrectionOfCorpus(corpusSentenceDiff);
    }

    @PostMapping("/corpus/sentence")
    @ResponseStatus(HttpStatus.CREATED)
    public CorpusSentenceDiff corpusSentenceCorrection(@RequestBody CorpusSentenceDiff corpusSentenceDiff)
    {
        logger.info("Saving correction of corpus sentence into database...");
        CorpusSentenceDiff sentenceDiff = trainService.saveCorpusSentenceCorrection(corpusSentenceDiff);
        logger.info("Correction has been saved to MongoDB!");
        return sentenceDiff;
    }
}
