package com.airoom.airoom.aichat.model.service;

import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Component
@Profile("ingest-csv")
@RequiredArgsConstructor
public class IngestCsvRunner implements CommandLineRunner {

    private final OpenAiService openAi;
    private final QdrantClient qdrant;

    @Override
    public void run(String... args) throws Exception {
        // ----------- 설정 -----------
        String descPath = getenv("DESC_PATH", "data/0824_jobs_description.csv");
        String qaPath   = getenv("QA_PATH",   "data/0824_jobs_qa.csv");

        String ID_COL    = getenv("ID_COL",    "id");
        String TITLE_COL = getenv("TITLE_COL", "title");
        String BODY_COL  = getenv("BODY_COL",  "body");

        String QA_ID_COL = getenv("QA_ID_COL", "id");
        String QA_Q_COL  = getenv("QA_Q_COL",  "question");

        Charset enc = Charset.forName(getenv("CSV_ENCODING", StandardCharsets.UTF_8.name()));
        // ----------------------------

        System.out.println("DESC_PATH=" + descPath);
        System.out.println("QA_PATH=" + qaPath);
        System.out.println("ENCODING=" + enc.displayName());

        // 1) QA 파일: id -> 질문 리스트
        Map<String, List<String>> qaMap = new HashMap<>();
        File qaFile = new File(qaPath);
        if (qaFile.exists()) {
            try (Reader r = new InputStreamReader(new FileInputStream(qaFile), enc);
                 CSVParser p = CSVFormat.DEFAULT.withFirstRecordAsHeader().withTrim().parse(r)) {
                for (CSVRecord rec : p) {
                    String id = safe(rec, QA_ID_COL);
                    String q  = safe(rec, QA_Q_COL);
                    if (!id.isEmpty() && !q.isEmpty()) {
                        qaMap.computeIfAbsent(id, k -> new ArrayList<>()).add(q);
                    }
                }
            }
        } else {
            System.out.println("QA file not found, skip QA merge");
        }
        System.out.println("QA rows: " + qaMap.size());

        // 2) 설명 CSV → 배치 업서트
        List<QdrantClient.Point> batch = new ArrayList<>(64);
        int count = 0;

        try (Reader r = new InputStreamReader(new FileInputStream(descPath), enc);
             CSVParser p = CSVFormat.DEFAULT.withFirstRecordAsHeader().withTrim().parse(r)) {

            for (CSVRecord rec : p) {
                String id    = safe(rec, ID_COL);
                String title = safe(rec, TITLE_COL);
                String body  = safe(rec, BODY_COL);
                if (id.isEmpty() || (title + body).isEmpty()) continue;

                Map<String, Object> payload = new LinkedHashMap<>();
                payload.put("id", id);
                payload.put("title", title);
                payload.put("body", body);
                payload.put("lang", "ko");
                payload.put("grade", Arrays.asList(1, 2));
                List<String> qas = qaMap.getOrDefault(id, Collections.emptyList());
                if (!qas.isEmpty()) payload.put("qa", qas);

                // 동기 임베딩
                List<Double> vec = openAi.embed(title + "\n" + body);

                batch.add(new QdrantClient.Point(id, vec, payload));
                count++;

                if (batch.size() >= 64) {
                    qdrant.upsert(batch);
                    System.out.println("Upserted: " + count);
                    batch.clear();
                }
            }
        }

        if (!batch.isEmpty()) qdrant.upsert(batch);
        System.out.println("Ingest done. total=" + count);
    }

    private static String getenv(String k, String def) {
        String v = System.getenv(k);
        return (v == null || v.isBlank()) ? def : v;
    }

    private static String safe(CSVRecord rec, String col) {
        try { return Optional.ofNullable(rec.get(col)).orElse("").trim(); }
        catch (IllegalArgumentException e) { return ""; }
    }
}
