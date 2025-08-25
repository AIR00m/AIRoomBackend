package com.airoom.airoom.presence;

import com.airoom.airoom.presence.dto.PresenceListItem;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/presence")
public class PresenceRestController {

    private final PresenceService presenceService;

    /** 교사 패널 최초 로딩용 스냅샷 */
    @GetMapping("/{classNo}")
    public ResponseEntity<List<PresenceListItem>> snapshot(@PathVariable long classNo) {
        return ResponseEntity.ok(presenceService.snapshot(classNo));
    }
}
