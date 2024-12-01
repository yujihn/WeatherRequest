package org.example;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;
import org.json.JSONArray;
import org.json.JSONObject;


public class WeatherRequest {
    private static final String API_KEY = "6ff6fa8f-9bcd-40e1-9557-33daf1f82e0c";
    private static final String API_URL = "https://api.weather.yandex.ru/v2/forecast";
    private static final String HTTP_HEADER ="X-Yandex-Weather-Key";

    public static void main(String[] args) {
        System.out.println("\n\t\t\t\t\t\t\tВас приветствует сервис погоды!" +
                "\n\t\t\tВыполните запрос средней и текущей температуры в интересующем Вас месте!" +
                "\n   Используйте формат ввода координат из Google maps: 54.75391077737814, 20.49462905957915" +
                "\nКоличество дней для подсчета средней температуры должно быть натуральным положительныи числом\n");

        Scanner input = new Scanner(System.in);
        double lat = getLatitude(input);
        double lon = getLongitude(input);
        int limit = getLimit(input);

        try {
            String response = sendGetRequest(lat, lon);
            JSONObject jsonResponse = new JSONObject(response);

            // Вывод ответа в формате JSON
            System.out.println("Response from API: " + jsonResponse.toString(4));

            // Текущая температура
            JSONObject fact = jsonResponse.getJSONObject("fact");
            int currentTemp = fact.getInt("temp");
            System.out.println("Current temperature: " + currentTemp + "°C");

            // Вычисление средней температуры
            double averageTemp = calculateAverageTemperature(jsonResponse, limit);
            System.out.println("Average temperature over " + limit + " days: " + averageTemp + "°C");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static double getLatitude(Scanner scanner) {
        double lat;
        while (true) {
            System.out.print("Введите широту: ");
            String input = scanner.nextLine();
            try {
                lat = Double.parseDouble(input);
                if (lat >= 0 && input.matches("\\d{1,2}\\.\\d{14}")) {
                    break;
                } else {
                    System.out.println("Широта должна быть положительным числом с форматом 00.00000000000000.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Некорректный ввод. Пожалуйста, введите число.");
            }
        }
        return lat;
    }

    private static double getLongitude(Scanner scanner) {
        double lon;
        while (true) {
            System.out.print("Введите долготу: ");
            String input = scanner.nextLine();
            try {
                lon = Double.parseDouble(input);
                if (lon >= 0 && input.matches("\\d{1,3}\\.\\d{14}")) {
                    break;
                } else {
                    System.out.println("Долгота должна быть положительным числом 00.00000000000000.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Некорректный ввод. Пожалуйста, введите число.");
            }
        }
        return lon;
    }

    private static int getLimit(Scanner scanner) {
        int limit;
        while (true) {
            System.out.print("Введите количество дней для прогноза: ");
            String input = scanner.nextLine();
            try {
                limit = Integer.parseInt(input);
                if (limit >= 1) {
                    break;
                } else {
                    System.out.println("Количество дней должно быть >= 1.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Некорректный ввод. Пожалуйста, введите целое число.");
            }
        }
        return limit;
    }

    private static String sendGetRequest(double lat, double lon) throws Exception {
        String urlString = API_URL + "?lat=" + lat + "&lon=" + lon;
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty(HTTP_HEADER, API_KEY);

        if (conn.getResponseCode() != 200) {
            throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
        }

        BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
        StringBuilder responseBuilder = new StringBuilder();
        String output;
        while ((output = br.readLine()) != null) {
            responseBuilder.append(output);
        }
        conn.disconnect();
        return responseBuilder.toString();
    }

    private static double calculateAverageTemperature(JSONObject jsonResponse, int limit) {
        JSONArray forecasts = jsonResponse.getJSONArray("forecasts");
        double totalTemp = 0;
        int count = 0;

        for (int i = 0; i < forecasts.length() && i < limit; i++) {
            JSONObject forecast = forecasts.getJSONObject(i);
            JSONObject day = forecast.getJSONObject("parts").getJSONObject("day");
            totalTemp += day.getInt("temp_avg");
            count++;
        }
        return count > 0 ? totalTemp / count : 0;
    }
}