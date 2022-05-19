package com.heindrich.tado;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;

import lombok.Data;
import lombok.Getter;
import org.json.JSONObject;

@Getter
public class TimerTermination extends Termination {
    private final int durationInSeconds;
    private final LocalDateTime expiry;
    private final Integer remainingTimeInSeconds;



    public TimerTermination(String typeSkillBasedApp, int durationInSeconds, LocalDateTime expiry, int remainingTimeInSeconds,
                            LocalDateTime projectedExpiry) {
        super("TIMER", typeSkillBasedApp, projectedExpiry);
        this.durationInSeconds = durationInSeconds;
        this.expiry = expiry;
        this.remainingTimeInSeconds = remainingTimeInSeconds;
    }

    public TimerTermination(int durationInSeconds) {
        super("TIMER");
        this.durationInSeconds = durationInSeconds;

        //TODO find better defaults
        this.remainingTimeInSeconds = 0;
        this.expiry = LocalDateTime.now();
    }

    @Override
    public JSONObject toJSONObject() {
        JSONObject root = new JSONObject();
        root.put("type", this.getType());
        root.put("durationInSeconds", this.durationInSeconds);
        if (this.getTypeSkillBasedApp() != null)
            root.put("typeSkillBasedApp", this.getTypeSkillBasedApp());
        if (this.expiry != null)
            root.put("expiry", this.expiry.format(this.getFormatter()));
        if (this.remainingTimeInSeconds != null)
            root.put("remainingTimeInSeconds", this.remainingTimeInSeconds);
        if (this.getProjectedExpiry() != null)
            root.put("projectedExpiry", this.getProjectedExpiry().format(this.getFormatter()));
        return root;
    }

    @Override
    public String toString() {
        return "TimerTermination [durationInSeconds=" + durationInSeconds + ", expiry=" + expiry
                + ", remainingTimeInSeconds=" + remainingTimeInSeconds + "]";
    }
}
