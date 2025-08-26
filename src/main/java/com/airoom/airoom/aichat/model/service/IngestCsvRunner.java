package com.airoom.airoom.aichat.model.service;

import com.fasterxml.jackson.databind.ObjectMapper;
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

/**
 * 프로필: ingest-csv
 * - DESC_PATH: 직업 설명 CSV 경로 (기본: data/0824_jobs_description.csv)
 * - QA_PATH:   직업 QA CSV 경로     (기본: data/0824_jobs_qa.csv)
 * - ID_COL / TITLE_COL / BODY_COL: 설명 CSV의 컬럼명 (기본: id/title/body)
 * - QA_ID_COL / QA_Q_COL: QA CSV의 컬럼명 (기본: id/question)
 * - CSV_ENCODING: UTF-8, EUC-KR 등 (기본: UTF-8)
 *
 * 레코드 payload 예:
 * { id, title, body, qa: ["질문1","질문2"...], grade:[1,2], lang:"ko" }
 */
@Component
@Profile("ingest-csv")
@RequiredArgsConstructor
public class IngestCsvRunner implements CommandLineRunner {

    private final OpenAiService openAi;
    private final QdrantClient qdrant;
    private final ObjectMapper om = new ObjectMapper();

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

        // 1) QA 파일 읽어서 id -> 질문들 맵으로
        Map<String, List<String>> qaMap = new HashMap<>();
        File qaFile = new File(qaPath);
        if (qaFile.exists()) {
            try (Reader r = new InputStreamReader(new FileInputStream(qaFile), enc);
                 CSVParser p = CSVFormat.DEFAULT
                         .withFirstRecordAsHeader()
                         .withTrim()
                         .parse(r)) {
                for (CSVRecord rec : p) {
                    String id = rec.get(QA_ID_COL).trim();
                    String q  = rec.get(QA_Q_COL).trim();
                    if (!id.isEmpty() && !q.isEmpty()) {
                        qaMap.computeIfAbsent(id, k -> new ArrayList<>()).add(q);
                    }
                }
            }
        } else {
            System.out.println("QA file not found, skip QA merge");
        }
        System.out.println("QA rows: " + qaMap.size());

        // 2) 설명 CSV 읽어서 포인트 배치 업서트
        List<Map<String,Object>> batch = new ArrayList<>(64);
        int count = 0;

        try (Reader r = new InputStreamReader(new FileInputStream(descPath), enc);
             CSVParser p = CSVFormat.DEFAULT
                     .withFirstRecordAsHeader()
                     .withTrim()
                     .parse(r)) {

            for (CSVRecord rec : p) {
                String id    = asText(rec, ID_COL);
                String title = asText(rec, TITLE_COL);
                String body  = asText(rec, BODY_COL);

                if (id.isEmpty() || (title + body).isEmpty()) {
                    continue;
                }

                // payload 구성
                Map<String,Object> payload = new LinkedHashMap<>();
                payload.put("id", id);
                payload.put("title", title);
                payload.put("body", body);
                payload.put("lang", "ko");
                payload.put("grade", Arrays.asList(1,2));
                List<String> qas = qaMap.getOrDefault(id, Collections.emptyList());
                if (!qas.isEmpty()) payload.put("qa", qas);

                // 임베딩
                List<Double> vec = openAi.embed(title + "\n" + body).block();

                Map<String,Object> point = new LinkedHashMap<>();
                point.put("id", id);
                point.put("vector", vec);
                point.put("payload", payload);

                batch.add(point);
                count++;

                if (batch.size() >= 64) {
                    qdrant.upsertBatch(batch).block();
                    System.out.println("Upserted: " + count);
                    batch.clear();
                }
            }
        }

        if (!batch.isEmpty()) {
            qdrant.upsertBatch(batch).block();
        }

        System.out.println("Ingest done. total=" + count);
    }

    private static String getenv(String k, String def) {
        String v = System.getenv(k);
        return (v == null || v.isBlank()) ? def : v;
    }

    private static String asText(CSVRecord rec, String col) {
        try { return Optional.ofNullable(rec.get(col)).orElse("").trim(); }
        catch (IllegalArgumentException e) { return ""; }
    }
}
