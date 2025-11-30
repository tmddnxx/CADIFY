package com.cadify.cadifyWAS.util.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;

@Component
public class HolidayAPI {
    @Value("${government.api.url}")
    private String holidayApiUrl;

    @Value("${government.api.key}")
    private String holidayApiKey;

    @Getter
    private static final Set<LocalDate> holidays = new HashSet<>();

    @PostConstruct
    public void getHolidaysByGovernment() throws IOException {
        StringBuilder urlBuilder = new StringBuilder(holidayApiUrl);
        urlBuilder.append("?" + URLEncoder.encode("serviceKey", StandardCharsets.UTF_8) + "=" +holidayApiKey); /*Service Key*/
        urlBuilder.append("&" + URLEncoder.encode("solYear", StandardCharsets.UTF_8) + "=" + URLEncoder.encode("2025", StandardCharsets.UTF_8)); /*연*/
        urlBuilder.append("&" + URLEncoder.encode("_type", StandardCharsets.UTF_8) + "=" + URLEncoder.encode("json", StandardCharsets.UTF_8)); /*연*/
        urlBuilder.append("&" + URLEncoder.encode("numOfRows", StandardCharsets.UTF_8) + "=" + URLEncoder.encode("100", StandardCharsets.UTF_8)); /*연*/

        URL url = new URL(urlBuilder.toString());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Content-type", "application/json");
        System.out.println("Response code: " + conn.getResponseCode());
        BufferedReader rd;
        if(conn.getResponseCode() >= 200 && conn.getResponseCode() <= 300) {
            rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        } else {
            rd = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
        }
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = rd.readLine()) != null) {
            sb.append(line);
        }
        rd.close();
        conn.disconnect();

        System.out.println("공휴일 가져오기 성공");

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode response = objectMapper.readTree(sb.toString());
        JsonNode items = response.path("response").path("body").path("items").path("item");

        if (items.isArray()) {
            for (JsonNode item : items) {
                int locdate = item.path("locdate").asInt();
                String dateString = String.valueOf(locdate);
                LocalDate holiday = LocalDate.parse(dateString,
                        DateTimeFormatter.ofPattern("yyyyMMdd"));
                holidays.add(holiday);
            }
        }
    }

    public static boolean isHoliday(LocalDate date) {
        return holidays.contains(date) || isWeekend(date);
    }

    private static boolean isWeekend(LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        return dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY;
    }
}
