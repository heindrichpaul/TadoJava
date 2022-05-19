package com.heindrich.tado;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.json.JSONObject;

@RequiredArgsConstructor
@Getter
@EqualsAndHashCode
@ToString
public abstract class Termination {
    private final String type;
    private final String typeSkillBasedApp;
    private final LocalDateTime projectedExpiry;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");


    protected Termination(String type) {
        this.type = type;
        this.typeSkillBasedApp = "";
        this.projectedExpiry = LocalDateTime.now();
    }

    public abstract JSONObject toJSONObject();
}
