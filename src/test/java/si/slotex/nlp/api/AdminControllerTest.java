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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

import si.slotex.nlp.entity.Document;

@SpringBootTest
@RunWith(SpringRunner.class)
public class AdminControllerTest
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
    public void shouldReturnStatusMessage() throws Exception
    {
        this.mockMvc.perform(RestDocumentationRequestBuilders
                .get("/admin/status"))
                .andExpect(status().isOk())
                .andExpect(content()
                        .contentType(MediaType.APPLICATION_JSON_UTF8))
                .andDo(document("{ClassName}/{methodName}",
                        preprocessRequest(prettyPrint()), preprocessResponse(prettyPrint()),
                        responseFields(
                                fieldWithPath("waitQueueSize")
                                        .description("Number of documents in the wait queue."),
                                fieldWithPath("workQueueSize")
                                        .description("Number of documents in the work queue."),
                                fieldWithPath("waitDocumentIds")
                                        .description("List of document IDs that are currently in the wait queue."),
                                fieldWithPath("workDocumentIds")
                                        .description("List of document IDs that are currently in the work queue."))
                        )
                );
    }

    @Test
    public void shouldPushDocumentToQueue() throws Exception
    {
        Document doc = new Document();
        doc.setDocumentId("3212");
        doc.setTitle("Nobelovi nagrajenci");
        doc.setContent("Niz razglasitev letošnjih prejemnikov Nobelovih nagrad so začeli v ponedeljek z nagrado za medicino, "
                + "ki jo letos za imunoterapijo za zdravljenje raka prejmeta ameriški in japonski imunolog James P. Allison in Tasuku Honjo. "
                + "V sredo bo znan nagrajenec za kemijo, v petek pa bodo razglasili prejemnika nagrade za mir. "
                + "Dobitnik nagrade za dosežke na področju ekonomije bo znan naslednji ponedeljek.");

        this.mockMvc.perform(RestDocumentationRequestBuilders
                .post("/admin/push")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(this.objectMapper.writeValueAsString(doc)))
                .andExpect(status().isCreated())
                .andDo(document("{ClassName}/{methodName}",
                        preprocessRequest(prettyPrint()), preprocessResponse(prettyPrint()),
                        requestFields(
                                fieldWithPath("documentId")
                                        .description("Number ID of the document."),
                                fieldWithPath("insertDate")
                                        .description("Date of the insertion to the queue. If not provided it defines the current one."),
                                fieldWithPath("title")
                                        .description("Title of the document to process"),
                                fieldWithPath("content")
                                        .description("Content of the document that has to be processed and tagged."))
                        )
                );
    }

    @Test
    public void shouldProcessDocumentInQueue() throws Exception
    {
        this.mockMvc.perform(RestDocumentationRequestBuilders
                .get("/admin/process/{docNum}", 1)
                .contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isCreated())
                .andDo(document("{ClassName}/{methodName}",
                        preprocessRequest(prettyPrint()), preprocessResponse(prettyPrint()),
                        pathParameters(parameterWithName("docNum")
                                .description("Number of documents to be processed.")),
                        responseFields(
                                fieldWithPath("numOfProcessed")
                                        .description("Number of how many documents were processed."),
                                fieldWithPath("processedIds")
                                        .description("List of document IDs that were processed in this batch."),
                                fieldWithPath("processStartTime")
                                        .description("Time when the processing of documents in the queue has started."),
                                fieldWithPath("processEndTime")
                                        .description("Time when the processing of documents in the queue has ended."))
                        )
                );
    }

    @Test
    public void shouldReturnDocumentToWaitQueue() throws Exception
    {
        this.mockMvc.perform(RestDocumentationRequestBuilders
                .get("/admin/return")
                .contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isCreated())
                .andDo(document("{ClassName}/{methodName}")
                );
    }
}
