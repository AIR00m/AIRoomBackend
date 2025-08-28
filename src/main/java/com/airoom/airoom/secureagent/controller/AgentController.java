package com.airoom.airoom.secureagent.controller;

import com.airoom.airoom.secureagent.model.dto.AgentVerifyRequest;
import com.airoom.airoom.secureagent.model.dto.AgentVerifyResponse;
import com.airoom.airoom.secureagent.model.service.AgentVerifyService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/agent")
//@CrossOrigin("*")
public class AgentController {

    private final AgentVerifyService agentVerifyService;
    private static final ObjectMapper M = new ObjectMapper();

    public AgentController(AgentVerifyService s) { this.agentVerifyService = s; }

    @GetMapping("/verify")
    public ResponseEntity<AgentVerifyResponse> verifyGet(
            @RequestParam String version,
            @RequestParam String sha256,
            HttpSession session
    ) {
        return doVerify(version, sha256, session);
    }

    @PostMapping(value="/verify", consumes= MediaType.ALL_VALUE, produces=MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AgentVerifyResponse> verifyPost(
            @RequestBody(required = false) String body,
            @RequestParam(required = false) String version,
            @RequestParam(required = false) String sha256,
            HttpServletRequest request,
            HttpSession session
    ) {
        try {
            if (body != null && !body.isBlank()) {
                try {
                    Map<?,?> m = M.readValue(body, Map.class);
                    if (version == null && m.get("version") != null) version = String.valueOf(m.get("version"));
                    if (sha256  == null && m.get("sha256")  != null) sha256  = String.valueOf(m.get("sha256"));
                } catch (Exception ignore) {}
            }
            if (version == null) version = request.getParameter("version");
            if (sha256  == null) sha256  = request.getParameter("sha256");

            if (version == null || sha256 == null) {
                session.removeAttribute("AGENT_VERIFIED");
                return ResponseEntity.badRequest().body(new AgentVerifyResponse(false, "bad-request"));
            }
            return doVerify(version, sha256, session);

        } catch (Exception e) {
            session.removeAttribute("AGENT_VERIFIED");
            return ResponseEntity.badRequest().body(new AgentVerifyResponse(false, "bad-request"));
        }
    }

    private ResponseEntity<AgentVerifyResponse> doVerify(String version, String sha256, HttpSession session) {
        boolean ok = agentVerifyService.isAllowed(version, sha256);
        if (ok) {
            session.setAttribute("AGENT_VERIFIED", true);
            return ResponseEntity.ok(new AgentVerifyResponse(true, null));
        }
        session.removeAttribute("AGENT_VERIFIED");
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new AgentVerifyResponse(false, "not-allowed"));
    }

    @PostMapping("/offline")
    public AgentVerifyResponse offline(HttpSession session){
        session.removeAttribute("AGENT_VERIFIED");
        return new AgentVerifyResponse(true, "session-flag-cleared");
    }
}

