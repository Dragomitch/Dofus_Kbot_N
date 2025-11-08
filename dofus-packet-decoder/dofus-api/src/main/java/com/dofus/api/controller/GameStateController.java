package com.dofus.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Game state API endpoints
 */
@RestController
@RequestMapping("/api/v1/game/state")
@Tag(name = "Game State", description = "Game state tracking and information endpoints")
public class GameStateController {

    @GetMapping
    @Operation(summary = "Get current game state", description = "Returns the current overall game state")
    public ResponseEntity<GameStateResponse> getGameState() {
        GameStateResponse response = new GameStateResponse();
        response.setConnected(false);
        response.setMessage("Game state tracking not yet implemented - placeholder endpoint");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/player")
    @Operation(summary = "Get player state", description = "Returns the current player state information")
    public ResponseEntity<String> getPlayerState() {
        return ResponseEntity.ok("Player state endpoint - to be implemented");
    }

    @GetMapping("/map")
    @Operation(summary = "Get current map state", description = "Returns the current map state and entities")
    public ResponseEntity<String> getMapState() {
        return ResponseEntity.ok("Map state endpoint - to be implemented");
    }

    @GetMapping("/combat")
    @Operation(summary = "Get combat state", description = "Returns the current combat state if in combat")
    public ResponseEntity<String> getCombatState() {
        return ResponseEntity.ok("Combat state endpoint - to be implemented");
    }

    @Data
    public static class GameStateResponse {
        private boolean connected;
        private String message;
    }
}
