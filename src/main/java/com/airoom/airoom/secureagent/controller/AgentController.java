package com.airoom.airoom.secureagent.controller;

import com.airoom.airoom.secureagent.model.dto.AgentVerifyRequest;
import com.airoom.airoom.secureagent.model.dto.AgentVerifyResponse;
import com.airoom.airoom.secureagent.model.service.AgentVerifyService;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/agent")
@CrossOrigin("*")
public class AgentController {

    private final AgentVerifyService agentVerifyService;
    public AgentController(AgentVerifyService s) { this.agentVerifyService = s; }

    @PostMapping("/verify")
    public AgentVerifyResponse verify(@RequestBody AgentVerifyRequest req, HttpSession session){
        boolean ok = agentVerifyService.isAllowed(req.getVersion(), req.getSha256());
        if(ok){
            session.setAttribute("AGENT_VERIFIED", true);
            return new AgentVerifyResponse(true, null);
        }
        session.removeAttribute("AGENT_VERIFIED");
        return new AgentVerifyResponse(false, "not-allowed");
    }

    @PostMapping("/offline")
    public AgentVerifyResponse offline(HttpSession session){
        session.removeAttribute("AGENT_VERIFIED");
        return new AgentVerifyResponse(true, "session-flag-cleared");
    }
}
