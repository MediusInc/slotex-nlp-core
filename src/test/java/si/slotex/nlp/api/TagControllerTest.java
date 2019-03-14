package si.slotex.nlp.api;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Calendar;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.restdocs.JUnitRestDocumentation;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;

import si.medius.nlp.model.Document;

@SpringBootTest
@RunWith(SpringRunner.class)
public class TagControllerTest
{
    @Rule
    public JUnitRestDocumentation restDocumentation = new JUnitRestDocumentation();

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @Before
    public void setup()
    {
        mockMvc = MockMvcBuilders.webAppContextSetup(this.context)
                .apply(documentationConfiguration(this.restDocumentation)
                        .uris()
                        .withPort(8100))
                .build();
    }

    @Test
    public void shouldTagProvidedDocument() throws Exception
    {
        Document doc = new Document();
        doc.setDocumentId("3212");
        doc.setInsertDate(Calendar.getInstance().getTime());
        doc.setTitle("Nobelovi nagrajenci");
        doc.setContent("Niz razglasitev letošnjih prejemnikov Nobelovih nagrad so začeli v ponedeljek z nagrado za medicino, "
                + "ki jo letos za imunoterapijo za zdravljenje raka prejmeta ameriški in japonski imunolog James P. Allison in Tasuku Honjo. "
                + "V sredo bo znan nagrajenec za kemijo, v petek pa bodo razglasili prejemnika nagrade za mir. "
                + "Dobitnik nagrade za dosežke na področju ekonomije bo znan naslednji ponedeljek.");

        this.mockMvc.perform(RestDocumentationRequestBuilders
                .post("/tag")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(this.objectMapper.writeValueAsString(doc)))
                .andExpect(status().isCreated())
                .andDo(document("{ClassName}/{methodName}",
                        preprocessRequest(prettyPrint()), preprocessResponse(prettyPrint()),
                        requestFields(
                                fieldWithPath("documentId")
                                        .description("Number ID of the document."),
                                fieldWithPath("insertDate")
                                        .description("Date of the insertion to the queue. If not provided it defines the current one.").optional(),
                                fieldWithPath("title")
                                        .description("Title of the document to process"),
                                fieldWithPath("content")
                                        .description("Content of the document that has to be processed and tagged.")),
                        responseFields(
                                fieldWithPath("documentId")
                                        .description("Unique identifier of the document that was processed from queue."),
                                fieldWithPath("language")
                                        .description("Evaluated language of the processed document."),
                                fieldWithPath("languageProb")
                                        .description("The probability that the evaluated language is the correct one."),
                                fieldWithPath("numOfSentences")
                                        .description("Number of evaluated sentences by our MediusNLP sentence detection module."),
                                fieldWithPath("numOfTokens")
                                        .description("Number of evaluated tokens by our MediusNLP Tokenizer module."),
                                fieldWithPath("numOfEntities")
                                        .description("Number of found entites by our MediusNLP NER module."),
                                fieldWithPath("entities")
                                        .description("List of the found entities in the sent document."),
                                fieldWithPath("entities[].id")
                                        .description("Id of the entity saved to our persistance unit."),
                                fieldWithPath("entities[].word")
                                        .description("Word that was found as an entity in our document."),
                                fieldWithPath("entities[].type")
                                        .description("Type of the entity in our document [person/location/organization]."),
                                fieldWithPath("entities[].documentIds")
                                        .description("List of IDs of all the documents where the entity has been seen."))
                        )
                );
    }

    @Test
    public void shouldGetTaggedById() throws Exception
    {
        this.mockMvc.perform(RestDocumentationRequestBuilders
                .get("/doc/{id}", 3212)
                .contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andDo(document("{ClassName}/{methodName}",
                        preprocessRequest(prettyPrint()), preprocessResponse(prettyPrint()),
                        pathParameters(parameterWithName("id")
                                .description("ID of the tagged document we want to retrieve.")),
                        responseFields(
                                fieldWithPath("documentId")
                                        .description("Unique identifier of the document that was processed from queue."),
                                fieldWithPath("modelsToTrain")
                                        .description("List of models for training with new data. If it is null there were no new models trained with this data."),
                                fieldWithPath("title")
                                        .description("Title of the processed document from the queue."),
                                fieldWithPath("language")
                                        .description("Evaluated language of the processed document."),
                                fieldWithPath("languageProb")
                                        .description("The probability that the evaluated language is the correct one."),
                                fieldWithPath("numOfSentences")
                                        .description("Number of evaluated sentences by our MediusNLP in the processed document."),
                                fieldWithPath("sentences")
                                        .description("List of all the evaluated sentences in the processed document."),
                                fieldWithPath("sentences[].sentence")
                                        .description("Evaluated sentence from the document with its content."),
                                fieldWithPath("sentences[].numberOfTokens")
                                        .description("Number of how many tokens were evaluated in the sentence by our MediusNLP framework."),
                                fieldWithPath("sentences[].tokens")
                                        .description("List of all the evaluated tokens in the processed sentence."),
                                fieldWithPath("sentences[].tokens[].word")
                                        .description("Word that was evaluated for tagging."),
                                fieldWithPath("sentences[].tokens[].posTag")
                                        .description("Evaluated value for part-of-speech tagging."),
                                fieldWithPath("sentences[].tokens[].lemma")
                                        .description("Evaluated value for lemmalization tagging."),
                                fieldWithPath("sentences[].tokens[].nerTag")
                                        .description("Evaluated value for named-entity-recognition tagging.").optional())
                        )
                );
    }

    @Test
    public void shouldGetTaggedDocs() throws Exception
    {
        this.mockMvc.perform(RestDocumentationRequestBuilders
                .get("/docs")
                .contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andDo(document("{ClassName}/{methodName}",
                        preprocessRequest(prettyPrint()), preprocessResponse(prettyPrint()),
                        responseFields(
                                fieldWithPath("[].documentId")
                                        .description("Unique identifier of the document that was processed from queue."),
                                fieldWithPath("[].modelsToTrain")
                                        .description("List of models for training with new data. If it is null there were no new models trained with this data."),
                                fieldWithPath("[].title")
                                        .description("Title of the processed document from the queue."),
                                fieldWithPath("[].language")
                                        .description("Evaluated language of the processed document."),
                                fieldWithPath("[].languageProb")
                                        .description("The probability that the evaluated language is the correct one."),
                                fieldWithPath("[].numOfSentences")
                                        .description("Number of evaluated sentences by our MediusNLP in the processed document."),
                                fieldWithPath("[].sentences")
                                        .description("List of all the evaluated sentences in the processed document."),
                                fieldWithPath("[].sentences[].sentence")
                                        .description("Evaluated sentence from the document with its content."),
                                fieldWithPath("[].sentences[].numberOfTokens")
                                        .description("Number of how many tokens were evaluated in the sentence by our MediusNLP framework."),
                                fieldWithPath("[].sentences[].tokens")
                                        .description("List of all the evaluated tokens in the processed sentence."),
                                fieldWithPath("[].sentences[].tokens[].word")
                                        .description("Word that was evaluated for tagging."),
                                fieldWithPath("[].sentences[].tokens[].posTag")
                                        .description("Evaluated value for part-of-speech tagging."),
                                fieldWithPath("[].sentences[].tokens[].lemma")
                                        .description("Evaluated value for lemmalization tagging."),
                                fieldWithPath("[].sentences[].tokens[].nerTag")
                                        .description("Evaluated value for named-entity-recognition tagging.").optional())
                        )
                );
    }

    @Test
    public void shouldGetTaggedEntities() throws Exception
    {
        this.mockMvc.perform(RestDocumentationRequestBuilders
                .get("/entities")
                .contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andDo(document("{ClassName}/{methodName}",
                        preprocessRequest(prettyPrint()), preprocessResponse(prettyPrint()),
                        responseFields(
                                fieldWithPath("[].id")
                                        .description("Id of the entity persisted to the storage."),
                                fieldWithPath("[].word")
                                        .description("Word on which the entity recognition was executed."),
                                fieldWithPath("[].type")
                                        .description("Entity type that was found [per/loc/org]."),
                                fieldWithPath("[].documentIds")
                                        .description("List of all the document IDs the entity was found."))
                        )
                );
    }
}
