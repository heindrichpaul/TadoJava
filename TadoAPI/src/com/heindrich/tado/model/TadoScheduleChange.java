package com.heindrich.tado.model;

import lombok.*;

import java.util.Date;

@Getter
@RequiredArgsConstructor
@ToString
@EqualsAndHashCode
public class TadoScheduleChange {
    private final Date start;
    private final TadoSetting setting;
}
