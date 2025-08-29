package com.airoom.airoom.aichat.model.service;

import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Component
@Profile("ingest-csv")
@RequiredArgsConstructor
public class IngestCsvRunner implements CommandLineRunner {
    private final ResourceLoader resources;
    private final OpenAiService openAi;
    private final QdrantClient qdrant;

    @Override public void run(String... args) throws Exception {
        String descPath = getenv("DESC_PATH", "classpath:data/0824_jobs_description.csv");
        String qaPath   = getenv("QA_PATH",   "classpath:data/0824_jobs_qa.csv");
        Charset enc = Charset.forName(getenv("CSV_ENCODING", StandardCharsets.UTF_8.name()));

        // 1) QA 맵
        Map<String,List<String>> qaMap = new HashMap<>();
        try (Reader r = new InputStreamReader(open(qaPath).getInputStream(), enc);
             var p = CSVFormat.DEFAULT.withFirstRecordAsHeader().withTrim().parse(r)) {
            for (var rec : p) {
                String id = safe(rec, "job");            // 업로드 파일 헤더 기준
                String q  = safe(rec, "question");
                if (!id.isEmpty() && !q.isEmpty()) qaMap.computeIfAbsent(id, k -> new ArrayList<>()).add(q);
            }
        } catch (FileNotFoundException e) {
            System.out.println("QA file not found, skip QA merge");
        }

        // 2) 설명 CSV 인덱싱
        List<QdrantClient.Point> batch = new ArrayList<>(64);
        int count = 0;

        try (Reader r = new InputStreamReader(open(descPath).getInputStream(), enc);
             var p = CSVFormat.DEFAULT.withFirstRecordAsHeader().withTrim().parse(r)) {
            for (var rec : p) {
                String job = safe(rec, "job");
                if (job.isEmpty()) continue;
                String id = job; // 문자열 ID 그대로 사용(안전하고 재실행 시 upsert id 고정)

                // 업로드 파일 구조에 맞춰 body 구성
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
                count++;

                if (batch.size() >= 64) { qdrant.upsert(batch); batch.clear(); }
            }
        }
        if (!batch.isEmpty()) qdrant.upsert(batch);
        System.out.println("Ingest done. total=" + count);
    }

    private org.springframework.core.io.Resource open(String path) {
        if (path.startsWith("classpath:")) return resources.getResource(path);
        return resources.getResource("file:" + path);
    }
    private static String getenv(String k, String def){ var v = System.getenv(k); return (v==null||v.isBlank())?def:v; }
    private static String safe(CSVRecord r, String c){ try { return Optional.ofNullable(r.get(c)).orElse("").trim(); } catch(Exception e){ return ""; } }
    private static String joinNonEmpty(String sep, String... xs){
        return Arrays.stream(xs).filter(s -> s!=null && !s.isBlank()).reduce((a,b)->a+sep+b).orElse("");
    }
}
