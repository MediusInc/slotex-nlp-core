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

import java.util.Arrays;
import java.util.List;

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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import si.medius.nlp.model.DocTrain;
import si.medius.nlp.model.Sentence;
import si.medius.nlp.model.Token;

@SpringBootTest
@RunWith(SpringRunner.class)
public class TrainControllerTest
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
    public void shouldGetTrainingModels() throws Exception
    {
        this.mockMvc.perform(RestDocumentationRequestBuilders
                .get("/models")
                .contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andDo(document("{ClassName}/{methodName}",
                        preprocessRequest(prettyPrint()), preprocessResponse(prettyPrint()),
                        responseFields(
                                fieldWithPath("[].versionName")
                                        .description("Unique identifier of the trained version [currentMilliseconds when trained]."),
                                fieldWithPath("[].timeStamp")
                                        .description("Timestamp of when the training was done."),
                                fieldWithPath("[].modelName")
                                        .description("Name of the model."),
                                fieldWithPath("[].trainDataFile")
                                        .description("Name of the training data file that was used with the new provided data."),
                                fieldWithPath("[].additionalTrainData")
                                        .description("Data that was used for additional training of the model in the specific version."))
                        )
                );
    }

    @Test
    public void shouldTrainModelWithAdditinalData() throws Exception
    {
        Sentence sentence = new Sentence();
        sentence.setSentence("Košarkarji Heliosa so našli menjavo za centra Aleksandra Šaškova.");
        sentence.setNumberOfTokens(10);
        String jsonTokens = "[{ \"word\" : \"Košarkarji\", \"posTag\" : \"NOUN\", \"lemma\" : \"košarkar\", \"nerTag\" : null }, { \"word\" : \"Heliosa\", \"posTag\" : \"NOUN\", \"lemma\" : \"helios\", \"nerTag\" : null }, { \"word\" : \"so\", \"posTag\" : \"VERB\", \"lemma\" : \"biti\", \"nerTag\" : null },"
                + "{ \"word\" : \"našli\", \"posTag\" : \"PRUN\", \"lemma\" : \"našel\", \"nerTag\" : null }, { \"word\" : \"menjavo\", \"posTag\" : \"NOUN\", \"lemma\" : \"menjava\", \"nerTag\" : null }, { \"word\" : \"za\", \"posTag\" : \"PREP\", \"lemma\" : \"za\", \"nerTag\" : null },"
                + "{ \"word\" : \"centra\", \"posTag\" : \"NOUN\", \"lemma\" : \"center\", \"nerTag\" : null }, { \"word\" : \"Aleksandra\", \"posTag\" : \"NOUN\", \"lemma\" : \"aleksander\", \"nerTag\" : \"osebno\" }, { \"word\" : \"Šaškova\", \"posTag\" : \"NOUN\", \"lemma\" : \"šiško\", \"nerTag\" : \"osebno\" },"
                + "{ \"word\" : \".\", \"posTag\" : \".\", \"lemma\" : \"o\", \"nerTag\" : null }]";
        List<Token> tokenList = this.objectMapper.readValue(jsonTokens, new TypeReference<List<Token>>()
        {
        });
        Token[] tokens = new Token[sentence.getNumberOfTokens()];
        for (int i = 0; i < tokenList.size(); i++)
        {
            tokens[i] = tokenList.get(i);
        }
        sentence.setTokens(Arrays.asList(tokens));

        DocTrain docTrain = new DocTrain();
        docTrain.setModelsToTrain("ner[person]");
        docTrain.setLanguage("slo");
        docTrain.setNumOfSentences(1);
        Sentence[] sentences = new Sentence[1];
        sentences[0] = sentence;
        docTrain.setTrainSentences(Arrays.asList(sentences));

        this.mockMvc.perform(RestDocumentationRequestBuilders
                .post("/train")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(this.objectMapper.writeValueAsString(docTrain)))
                .andExpect(status().isCreated())
                .andDo(document("{ClassName}/{methodName}",
                        preprocessRequest(prettyPrint()), preprocessResponse(prettyPrint()),
                        requestFields(
                                fieldWithPath("modelsToTrain")
                                        .description("Specified models which will be trained with the additional training data. Example of training three models: 'lang,pos,ner[per,loc,org]'."),
                                fieldWithPath("language")
                                        .description("Defines the language of the additional data provided."),
                                fieldWithPath("numOfSentences")
                                        .description("Number of sentences on which we will be training our model."),
                                fieldWithPath("trainSentences")
                                        .description("List of tagged sentences which will be added to the training data used for training before."),
                                fieldWithPath("trainSentences[].sentence")
                                        .description("Tagged sentences which will be added to the training data used for training before."),
                                fieldWithPath("trainSentences[].numberOfTokens")
                                        .description("Number of tokens in sentence. A token is a word or a punctuation in the sentence."),
                                fieldWithPath("trainSentences[].tokens")
                                        .description("List of all the tokens in the sentence."),
                                fieldWithPath("trainSentences[].tokens[].word")
                                        .description("Word in a sentence."),
                                fieldWithPath("trainSentences[].tokens[].posTag")
                                        .description("Part-of-speech tag for the provided word.").optional(),
                                fieldWithPath("trainSentences[].tokens[].lemma")
                                        .description("Lemma tag for the provided word.").optional(),
                                fieldWithPath("trainSentences[].tokens[].nerTag")
                                        .description("Named-entity tag for the provided word").optional()
                        ),
                        responseFields(
                                fieldWithPath("versionName")
                                        .description("Unique identifier of the trained version [currentMilliseconds when trained]."),
                                fieldWithPath("timeStamp")
                                        .description("Timestamp of when the training was done."),
                                fieldWithPath("modelName")
                                        .description("Name of the model."),
                                fieldWithPath("trainDataFile")
                                        .description("Name of the training data file that was used with the new provided data."),
                                fieldWithPath("additionalTrainData")
                                        .description("Data that was used for additional training of the model in the specific version."))
                        )
                );
    }

    @Test
    public void shouldGetRetrained() throws Exception
    {
        this.mockMvc.perform(RestDocumentationRequestBuilders
                .get("/retrain/{modelName}", "person")
                .contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andDo(document("{ClassName}/{methodName}",
                        pathParameters(parameterWithName("modelName")
                                .description("Name of the entity model that is going to be retrained."))
                ));
    }
}
