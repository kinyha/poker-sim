package poker.web.dto;

import poker.model.ActionType;

public record ActionRequest(
    ActionType type,
    int amount
) {}
