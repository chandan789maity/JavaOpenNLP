package com.virus.chatbot;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import opennlp.tools.doccat.BagOfWordsFeatureGenerator;
import opennlp.tools.doccat.DoccatFactory;
import opennlp.tools.doccat.DoccatModel;
import opennlp.tools.doccat.DocumentCategorizerME;
import opennlp.tools.doccat.DocumentSample;
import opennlp.tools.doccat.DocumentSampleStream;
import opennlp.tools.doccat.FeatureGenerator;
import opennlp.tools.lemmatizer.LemmatizerME;
import opennlp.tools.lemmatizer.LemmatizerModel;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.InputStreamFactory;
import opennlp.tools.util.InvalidFormatException;
import opennlp.tools.util.MarkableFileInputStreamFactory;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.PlainTextByLineStream;
import opennlp.tools.util.TrainingParameters;
import opennlp.tools.util.model.ModelUtil;
import org.springframework.stereotype.Component;
@Component
public class OpenNLPChatBot {

    private static final Map<String, List<String>> questionAnswer = new HashMap<>();

    static {
        questionAnswer.put("greeting", IntentDataGenerator.generateGreetings(1));
        questionAnswer.put("subject-exam", IntentDataGenerator.generateSubjectExam(1));
        questionAnswer.put("exam-date", IntentDataGenerator.generateExamDate(1));
        questionAnswer.put("exam-duration", IntentDataGenerator.generateExamTimeDuration(1));
        questionAnswer.put("conversation-complete", IntentDataGenerator.generateConversationComplete(1));
    }

    private final DoccatModel model;

    public OpenNLPChatBot() throws IOException {
        this.model = trainCategorizerModel();
    }

    public String processInput(String userInput) throws IOException {
        String[] sentences = breakSentences(userInput);
        StringBuilder responseBuilder = new StringBuilder();
        for (String sentence : sentences) {
            String[] tokens = tokenizeSentence(sentence);
            String[] posTags = detectPOSTags(tokens);
            String[] lemmas = lemmatizeTokens(tokens, posTags);
            String category = detectCategory(lemmas);
            List<String> answersForCategory = questionAnswer.get(category);
            if (answersForCategory != null && !answersForCategory.isEmpty()) {
                responseBuilder.append(String.join(" ", answersForCategory));
            }
            if ("conversation-complete".equals(category)) {
                break;
            }
        }
        return responseBuilder.toString();
    }

    private DoccatModel trainCategorizerModel() throws FileNotFoundException, IOException {
        InputStreamFactory inputStreamFactory = new MarkableFileInputStreamFactory(new File("faq-categorizer.txt"));
        ObjectStream<String> lineStream = new PlainTextByLineStream(inputStreamFactory, StandardCharsets.UTF_8);
        ObjectStream<DocumentSample> sampleStream = new DocumentSampleStream(lineStream);
        DoccatFactory factory = new DoccatFactory(new FeatureGenerator[] { new BagOfWordsFeatureGenerator() });
        TrainingParameters params = ModelUtil.createDefaultTrainingParameters();
        params.put(TrainingParameters.CUTOFF_PARAM, 0);
        return DocumentCategorizerME.train("en", sampleStream, params, factory);
    }

    private String[] breakSentences(String data) throws FileNotFoundException, IOException {
        try (InputStream modelIn = new FileInputStream("en-sent.bin")) {
            SentenceDetectorME sentenceDetector = new SentenceDetectorME(new SentenceModel(modelIn));
            return sentenceDetector.sentDetect(data);
        }
    }

    private String[] tokenizeSentence(String sentence) throws FileNotFoundException, IOException {
        try (InputStream modelIn = new FileInputStream("en-token.bin")) {
            TokenizerME tokenizer = new TokenizerME(new TokenizerModel(modelIn));
            return tokenizer.tokenize(sentence);
        }
    }

    private String[] detectPOSTags(String[] tokens) throws IOException {
        try (InputStream modelIn = new FileInputStream("en-pos-maxent.bin")) {
            POSTaggerME posTagger = new POSTaggerME(new POSModel(modelIn));
            return posTagger.tag(tokens);
        }
    }

    private String[] lemmatizeTokens(String[] tokens, String[] posTags) throws IOException {
        try (InputStream modelIn = new FileInputStream("en-lemmatizer.bin")) {
            LemmatizerME lemmatizer = new LemmatizerME(new LemmatizerModel(modelIn));
            return lemmatizer.lemmatize(tokens, posTags);
        }
    }

    private String detectCategory(String[] finalTokens) throws IOException {
        DocumentCategorizerME categorizer = new DocumentCategorizerME(model);
        double[] probabilitiesOfOutcomes = categorizer.categorize(finalTokens);
        return categorizer.getBestCategory(probabilitiesOfOutcomes);
    }
}

