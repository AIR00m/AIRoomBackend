package com.airoom.airoom.aichat.model.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class CsvIngestService {

    private final ResourceLoader resources;
    private final OpenAiService openAi;
    private final QdrantClient qdrant;

    /** 컬렉션이 비어 있으면 인덱싱 */
    public void ingestIfEmpty() throws Exception {
        int count = 0;
        try {
            count = qdrant.count();
        } catch (Exception ignore) {}
        if (count == 0) {
            log.info("[CsvIngestService] Qdrant empty → ingest start");
            ingest();
        } else {
            log.info("[CsvIngestService] Qdrant already populated: count=" + count + " → skip ingest");
        }
    }

    /** CSV → 임베딩 → Qdrant 업서트 */
    public void ingest() throws Exception {
        String descPath = getenv("DESC_PATH", "classpath:data/0824_jobs_description.csv");
        String qaPath   = getenv("QA_PATH",   "classpath:data/0824_jobs_qa.csv");
        Charset enc = Charset.forName(getenv("CSV_ENCODING", StandardCharsets.UTF_8.name()));

        // 1) QA 맵 (id=job, question)
        Map<String, List<String>> qaMap = new HashMap<>();
        try (Reader r = new InputStreamReader(open(qaPath).getInputStream(), enc);
             CSVParser p = CSVFormat.DEFAULT.withFirstRecordAsHeader().withTrim().parse(r)) {
            for (CSVRecord rec : p) {
                String id = safe(rec, "job");
                String q  = safe(rec, "question");
                if (!id.isEmpty() && !q.isEmpty()) {
                    qaMap.computeIfAbsent(id, k -> new ArrayList<>()).add(q);
                }
            }
        } catch (FileNotFoundException e) {
            log.error("[CsvIngestService] QA file not found, skip QA merge");
        }

        // 2) 설명 CSV 인덱싱
        List<QdrantClient.Point> batch = new ArrayList<>(64);
        int total = 0;

        try (Reader r = new InputStreamReader(open(descPath).getInputStream(), enc);
             CSVParser p = CSVFormat.DEFAULT.withFirstRecordAsHeader().withTrim().parse(r)) {

            for (CSVRecord rec : p) {
                String job = safe(rec, "job");
                if (job.isEmpty()) continue;
                String id = job; // 문자열 ID 그대로 사용(재실행 시 upsert id 고정)

                String body = String.join("\n",
                        "한줄정의: " + safe(rec, "definition"),
                        "하는 일: " + joinNonEmpty("; ",
                                safe(rec, "what_1"), safe(rec, "what_2"), safe(rec, "what_3")),
                        "어디서 일해요: " + safe(rec, "where"),
                        "누구를 도와요: " + safe(rec, "who"),
                        "재미있는 사실: " + safe(rec, "fun_fact"),
                        "키워드: " + safe(rec, "keywords")
                );

                Map<String,Object> payload = new LinkedHashMap<>();
                payload.put("id", id);
                payload.put("title", job);
                payload.put("body", body);
                payload.put("lang", "ko");
                payload.put("grade", List.of(1,2));
                payload.put("keywords", safe(rec, "keywords"));
                payload.put("type", "job_description");

                var qas = qaMap.getOrDefault(id, List.of());
                if (!qas.isEmpty()) payload.put("qa", qas);

                var vec = openAi.embed(job + "\n" + body);
                batch.add(new QdrantClient.Point(id, vec, payload));
                total++;

                if (batch.size() >= 64) {
                    qdrant.upsert(batch);
                    batch.clear();
                }
            }
        }

        if (!batch.isEmpty()) qdrant.upsert(batch);
        log.info("[CsvIngestService] Ingest done. total=" + total);
    }

    // ---------- helpers ----------
    private org.springframework.core.io.Resource open(String path) {
        if (path.startsWith("classpath:")) return resources.getResource(path);
        return resources.getResource("file:" + path);
    }

    private static String getenv(String k, String def) {
        String v = System.getenv(k);
        return (v == null || v.isBlank()) ? def : v;
    }

    private static String safe(CSVRecord r, String c) {
        try { return Optional.ofNullable(r.get(c)).orElse("").trim(); }
        catch (Exception e) { return ""; }
    }

    private static String joinNonEmpty(String sep, String... xs) {
        return Arrays.stream(xs)
                .filter(s -> s != null && !s.isBlank())
                .reduce((a,b) -> a + sep + b).orElse("");
    }
}
