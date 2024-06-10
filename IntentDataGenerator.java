package com.virus.chatbot;

import com.github.javafaker.Faker;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class IntentDataGenerator {
    private static final Faker faker = new Faker();
    private static final Random random = new Random();

    public static List<String> generateGreetings(int amount) {
        List<String> greetings = new ArrayList<>();
        for (int i = 0; i < amount; i++) {
            greetings.add(faker.name().firstName() + ", how are you? How can i help you?");
        }
        return greetings;
    }

    public static List<String> generateSubjectExam(int amount) {
        List<String> subjectExams = new ArrayList<>();
        for (int i = 0; i < amount; i++) {
            subjectExams.add("The subject of the exam:"+faker.educator().course());
        }
        return subjectExams;
    }

    public static List<String> generateExamDate(int amount) {
        List<String> examDates = new ArrayList<>();
        for (int i = 0; i < amount; i++) {
            examDates.add("Exam date :"+generateRandomExamDate());
        }
        return examDates;
    }

    private static String generateRandomExamDate() {
        // Generate random exam date
        return faker.date().future(30, TimeUnit.DAYS).toString(); // Random future date
    }

    public static List<String> generateExamTimeDuration(int amount) {
        List<String> examTimeDurations = new ArrayList<>();
        for (int i = 0; i < amount; i++) {
            examTimeDurations.add("Time duration for exam is: "+generateRandomExamTimeDuration());
        }
        return examTimeDurations;
    }

    private static String generateRandomExamTimeDuration() {
        // Generate random time with duration
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
        StringBuilder builder = new StringBuilder();
        builder.append(timeFormat.format(faker.date().future(1, TimeUnit.DAYS))); // Random time
        builder.append(" for ");
        builder.append(random.nextInt(4) + 1); // Random duration (1 to 4 hours)
        builder.append(" hours");
        return builder.toString();
    }
    

    public static List<String> generateConversationComplete(int amount) {
        List<String> conversationCompletes = new ArrayList<>();
        for (int i = 0; i < amount; i++) {
            conversationCompletes.add(faker.yoda().quote());
        }
        return conversationCompletes;
    }
}
