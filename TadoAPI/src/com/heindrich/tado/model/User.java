package com.heindrich.tado.model;

import lombok.*;

import java.util.List;
import java.util.Map;

@Getter
@RequiredArgsConstructor
@ToString
@EqualsAndHashCode
public class User {
	private final String name;
	private final String email;
	private final String username;
	private final Map<Integer, String> homes;
	private final String locale;
	private final List<MobileDevice> mobileDevices;
}
