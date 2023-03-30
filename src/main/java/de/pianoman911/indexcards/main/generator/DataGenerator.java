package de.pianoman911.indexcards.main.generator;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.concurrent.atomic.AtomicInteger;

public record DataGenerator(String sourcePath, String targetPath, String mediaServerUrl, String tableName, String answerTable, String question, AtomicInteger id) {


    public static void main(String[] args) throws Exception {
        System.out.println("Starting data generation...");
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("Enter the path to the source folder: ");
        String sourcePath = br.readLine();
        System.out.println("Enter the path of the target output file: ");
        String targetPath = br.readLine();
        System.out.println("Enter the url of the media server: ");
        String mediaServerUrl = br.readLine();
        System.out.println("Enter the SQL table name: ");
        String tableName = br.readLine();
        System.out.println("Enter the SQL answer table name: ");
        String answerTable = br.readLine();
        System.out.println("Enter the Question ");
        String question = br.readLine();

        System.out.println("Generating data...");
        StringBuilder sql = new StringBuilder();
        DataGenerator generator = new DataGenerator(sourcePath, targetPath, mediaServerUrl, tableName, answerTable, question, new AtomicInteger(0));
        generator.generateAndSave();
    }

    private void generateAndSave() {
        StringBuilder sql = new StringBuilder();
        generateRecursive(sql, new File(sourcePath), mediaServerUrl);
        System.out.println("Generated data: ");
        System.out.println(sql);
    }

    public void generateRecursive(StringBuilder sql, File folder, String mediaServerUrl) {
        File[] files = folder.listFiles();
        assert files != null;
        for (File file : files) {
            if (file.isDirectory()) {
                generateRecursive(sql, file, mediaServerUrl);
            } else {
                String fileName = file.getName();
                String filePath = file.getPath().substring(sourcePath.length());
                String fileUrl = mediaServerUrl + filePath.replace("\\", "/");
                int i = id.getAndIncrement();
                sql.append("INSERT INTO ")
                        .append(tableName)
                        .append(" (id, question, `group`) VALUES (")
                        .append(i)
                        .append(", '").append("<img width=\"800\" height=\"600\" src=\"").append(fileUrl).append("\" alt=\"Dein Browser unterstützt wohl keine Bilder :(\">\n").append("<br>\n").append(question)
                        .append("', '")
                        .append(file.getParentFile().getPath().substring(sourcePath.length()).replace("\\", "/"))
                        .append("');")
                        .append(System.lineSeparator());
                sql.append("INSERT INTO ")
                        .append(answerTable)
                        .append(" (card, answer) VALUES (")
                        .append(i)
                        .append(", '")
                        .append(fileName,0, fileName.lastIndexOf('.'))
                        .append("');")
                        .append(System.lineSeparator());
            }
        }
    }
}
