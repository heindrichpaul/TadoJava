package com.heindrich.tado;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.heindrich.tado.model.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.FormBody;
import okhttp3.FormBody.Builder;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class TadoConnector {
    public static final String REFRESH_TOKEN = "refresh_token";
    public static final String ERROR = "error";
    public static final String INITIALIZE_CONNECTOR_ERROR = "You must initialize the TadoConnector";
    public static final String AUTHORIZATION = "Authorization";
    public static final String BEARER = "Bearer ";
    public static final String HOMES_KEY = "homes";
    public static final String TADO_API_URL = "https://my.tado.com/api/v2/homes/";
    private String username;
    private char[] password;
    private String clientSecret;
    private String bearer;
    private String refreshToken;
    private OkHttpClient client;
    private boolean initialized = false;
    private boolean debug = false;
    public static final MediaType FORM = MediaType.parse("multipart/form-data");

    public TadoConnector(String username, String password, String clientSecret) {
        this.username = username;
        this.password = password.toCharArray();
        this.clientSecret = clientSecret;
    }

    public TadoConnector(String username, String password) {
        this.username = username;
        this.password = password.toCharArray();
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public void initialize() throws TadoException {
        if (!initialized) {
            client = new OkHttpClient();
            if (this.clientSecret == null)
                this.clientSecret = getClientSecret();
            getTokens();
            this.initialized = true;
        }
    }

    public void refresh() throws TadoException {
        if (initialized) {
            refreshTokens();
        }
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public char[] getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password.toCharArray();
    }

    public String getBearer() {
        return bearer;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    // Get requests

    private String getClientSecret() {
        try {
            String jsonResponse = doGetRequest("https://my.tado.com/webapp/env.js", null);
            debugMessage("getClientSecret response: " + jsonResponse);
            jsonResponse = jsonResponse.substring(9).trim();
            jsonResponse = jsonResponse.substring(0, jsonResponse.length() - 1).trim();
            JSONObject json = new JSONObject(jsonResponse);
            return json.getJSONObject("config").getJSONObject("oauth").optString("clientSecret");
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void getTokens() throws TadoException {
        Map<String, String> body = new HashMap<>();
        body.put("client_id", "tado-web-app");
        body.put("grant_type", "password");
        body.put("scope", "home.user");
        body.put("username", this.username);
        body.put("password", String.valueOf(this.password));
        body.put("client_secret", this.clientSecret);
        try {
            String response = doPostRequest("https://auth.tado.com/oauth/token", body, null);
            debugMessage("getBearerTokens response: " + response);
            JSONObject json = new JSONObject(response);
            checkException(json);
            this.bearer = json.optString("access_token");
            this.refreshToken = json.optString(REFRESH_TOKEN);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void refreshTokens() throws TadoException {
        Map<String, String> body = new HashMap<>();
        body.put("client_id", "tado-web-app");
        body.put("grant_type", "refresh_token");
        body.put("scope", "home.user");
        body.put("refresh_token", this.refreshToken);
        body.put("client_secret", this.clientSecret);
        try {
            String response = doPostRequest("https://auth.tado.com/oauth/token", body, null);
            debugMessage("getBearerTokens response: " + response);
            JSONObject json = new JSONObject(response);
            checkException(json);
            this.bearer = json.optString("access_token");
            this.refreshToken = json.optString("refresh_token");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<Integer> getHomesIDs() throws TadoException {
        if (!this.initialized)
            throw new TadoException(ERROR, INITIALIZE_CONNECTOR_ERROR);
        return getHomesIds(0);
    }

    private List<Integer> getHomesIds(int attempt) throws TadoException {
        List<Integer> toReturn = new ArrayList<>();
        try {
            Map<String, String> headers = new HashMap<>();
            headers.put(AUTHORIZATION, BEARER + this.bearer);
            String jsonResponse = doGetRequest("https://my.tado.com/api/v2/me", headers);
            debugMessage("getHomesIDs response: " + jsonResponse);
            JSONObject json = new JSONObject(jsonResponse);
            checkException(json);
            for (Object o : json.getJSONArray(HOMES_KEY)) {
                if (o instanceof JSONObject) {
                    JSONObject home = (JSONObject) o;
                    toReturn.add(home.getInt("id"));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TadoException e) {
            if (attempt > 1) {
                throw e;
            } else {
                refresh();
                toReturn = getHomesIds(attempt + 1);
            }
        }
        return toReturn;
    }

    public List<TadoHome> getHomes() throws TadoException {
        if (!this.initialized)
            throw new TadoException("error", "You must initialize the TadoConnector");
        return getHomes(0);
    }

    private List<TadoHome> getHomes(int attempt) throws TadoException {
        List<TadoHome> toReturn = new ArrayList<>();
        try {
            Map<String, String> headers = new HashMap<>();
            headers.put("Authorization", "Bearer " + this.bearer);
            String jsonResponse = doGetRequest("https://my.tado.com/api/v2/me", headers);
            debugMessage("getHomes response: " + jsonResponse);
            JSONObject json = new JSONObject(jsonResponse);
            checkException(json);
            for (Object o : json.getJSONArray("homes")) {
                if (o instanceof JSONObject) {
                    JSONObject jsonHome = (JSONObject) o;
                    toReturn.add(getHome(jsonHome.getInt("id"), 0));

                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TadoException e) {
            if (attempt > 1) {
                throw e;
            } else {
                refresh();
                toReturn = getHomes(attempt + 1);
            }
        }
        return toReturn;
    }

    public TadoHome getHome(int id) throws TadoException {
        if (!this.initialized)
            throw new TadoException("error", "You must initialize the TadoConnector");
        return getHome(id, 0);
    }

    private TadoHome getHome(int id, int attempt) throws TadoException {
        TadoHome toReturn = null;
        try {
            Map<String, String> headers = new HashMap<>();
            headers.put("Authorization", "Bearer " + this.bearer);
            String jsonResponse = doGetRequest(TADO_API_URL + id, headers);
            debugMessage("getHome response: " + jsonResponse);
            JSONObject json = new JSONObject(jsonResponse);
            checkException(json);
            Date dateCreated = Date.from(Instant.parse(json.optString("dateCreated")));
            JSONObject jsonContactDetails = json.getJSONObject("contactDetails");
            ContactDetails contactDetails = new ContactDetails(jsonContactDetails.optString("name"),
                    jsonContactDetails.optString("email"), jsonContactDetails.optString("phone"));
            JSONObject jsonAddress = json.getJSONObject("address");
            Address address = new Address(jsonAddress.optString("addressLine1"), jsonAddress.optString("addressLine2"),
                    jsonAddress.optString("zipCode"), jsonAddress.optString("city"), jsonAddress.optString("state"),
                    jsonAddress.optString("country"));
            JSONObject jsonGeolocation = json.getJSONObject("geolocation");
            Geolocation geolocation = new Geolocation(jsonGeolocation.optDouble("latitude"),
                    jsonGeolocation.optDouble("longitude"));
            toReturn = new TadoHome(json.getInt("id"), json.optString("name"), json.optString("dateTimeZone"),
                    dateCreated, json.optString("temperatureUnit"), json.optBoolean("installationCompleted"),
                    json.optBoolean("simpleSmartScheduleEnabled"), json.optDouble("awayRadiusInMeters"),
                    json.optBoolean("usePreSkillsApps"), json.optBoolean("christmasModeEnabled"), contactDetails,
                    address, geolocation, json.optBoolean("consentGrantSkippable"));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TadoException e) {
            if (attempt > 1) {
                throw e;
            } else {
                refresh();
                toReturn = getHome(id, attempt + 1);
            }
        }
        return toReturn;
    }

    public List<TadoZone> getZones(TadoHome tadoHome) throws TadoException {
        return getZones(tadoHome.getId());
    }

    public List<TadoZone> getZones(int homeId) throws TadoException {
        if (!this.initialized)
            throw new TadoException("error", "You must initialize the TadoConnector");
        return getZones(homeId, 0);
    }

    private List<TadoZone> getZones(int homeId, int attempt) throws TadoException {
        List<TadoZone> toReturn = new ArrayList<>();
        try {
            Map<String, String> headers = new HashMap<>();
            headers.put("Authorization", "Bearer " + this.bearer);
            String jsonResponse = doGetRequest("https://my.tado.com/api/v2/homes/" + homeId + "/zones", headers);
            debugMessage("getZones response: " + jsonResponse);
            try {
                // IF IT CAN PARSE THE JSONOBJECT IT WILL PROBABLY BE AN EXCEPTION
                JSONObject json = new JSONObject(jsonResponse);
                checkException(json);
            } catch (JSONException e) {
                // IF IT CANNOT PARSE THE JSONOBJECT IT WILL BE AN ARRAY OF ZONES, WHICH IS THE
                // EXPECTED RESULT
                JSONArray jsonArray = new JSONArray(jsonResponse);
                for (Object obj : jsonArray) {
                    if (obj instanceof JSONObject) {
                        JSONObject jsonZone = (JSONObject) obj;
                        Date dateCreated = Date.from(Instant.parse(jsonZone.optString("dateCreated")));
                        JSONArray jsonDeviceTypes = jsonZone.getJSONArray("deviceTypes");
                        List<String> deviceTypes = new ArrayList<>();
                        for (Object deviceType : jsonDeviceTypes) {
                            if (deviceType instanceof String)
                                deviceTypes.add((String) deviceType);
                        }
                        JSONArray jsonDevices = jsonZone.getJSONArray("devices");
                        List<TadoDevice> devices = new ArrayList<>();
                        for (Object jsonDevice : jsonDevices) {
                            if (jsonDevice instanceof JSONObject) {
                                JSONObject device = (JSONObject) jsonDevice;
                                JSONObject jsonConnectionState = device.getJSONObject("connectionState");
                                LocalDateTime timestamp = getLocalDateTimeOf(Date.from(Instant.parse(jsonConnectionState.optString("timestamp"))));
                                TadoConnectionState connectionState = new TadoConnectionState(
                                        jsonConnectionState.getBoolean("value"), timestamp);
                                JSONArray jsonCapabilities = device.getJSONObject("characteristics")
                                        .getJSONArray("capabilities");
                                List<String> capabilities = new ArrayList<>();
                                for (Object capability : jsonCapabilities) {
                                    if (capability instanceof String)
                                        capabilities.add((String) capability);
                                }
                                JSONArray jsonDuties = device.getJSONArray("duties");
                                List<String> duties = new ArrayList<>();
                                for (Object duty : jsonDuties) {
                                    if (duty instanceof String)
                                        duties.add((String) duty);
                                }
                                TadoDevice toAdd = new TadoDevice(device.optString("deviceType"),
                                        device.optString("serialNo"), device.optString("shortSerialNo"),
                                        device.optString("currentFwVersion"), connectionState, capabilities,
                                        device.optBoolean("inPairingMode"), device.optString("batteryState"), duties);
                                devices.add(toAdd);
                            }
                        }
                        JSONObject jsonDazzleMode = jsonZone.getJSONObject("dazzleMode");
                        TadoDazzleMode dazzleMode = new TadoDazzleMode(jsonDazzleMode.getBoolean("supported"),
                                jsonDazzleMode.getBoolean("enabled"));
                        JSONObject jsonOpenWindowDetection = jsonZone.getJSONObject("openWindowDetection");
                        OpenWindowDetection openWindowDetection = new OpenWindowDetection(
                                jsonOpenWindowDetection.getBoolean("supported"),
                                jsonOpenWindowDetection.getBoolean("enabled"),
                                jsonOpenWindowDetection.getInt("timeoutInSeconds"));
                        TadoZone zone = new TadoZone(homeId, jsonZone.getInt("id"), jsonZone.optString("name"),
                                jsonZone.optString("type"), dateCreated, deviceTypes, devices,
                                jsonZone.getBoolean("reportAvailable"), jsonZone.getBoolean("supportsDazzle"),
                                jsonZone.getBoolean("dazzleEnabled"), dazzleMode, openWindowDetection);
                        toReturn.add(zone);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TadoException e) {
            if (attempt > 1) {
                throw e;
            } else {
                refresh();
                toReturn = getZones(homeId, attempt + 1);
            }
        }
        return toReturn;
    }

    public TadoState getHomeState(TadoHome tadoHome) throws TadoException {
        return getHomeState(tadoHome.getId());
    }

    public TadoState getHomeState(int homeId) throws TadoException {
        if (!this.initialized)
            throw new TadoException("error", "You must initialize the TadoConnector");
        return getHomeState(homeId, 0);
    }

    private TadoState getHomeState(int homeId, int attempt) throws TadoException {
        TadoState toReturn = null;
        try {
            Map<String, String> headers = new HashMap<>();
            headers.put("Authorization", "Bearer " + this.bearer);
            String jsonResponse = doGetRequest("https://my.tado.com/api/v2/homes/" + homeId + "/state", headers);
            debugMessage("getHomeState response: " + jsonResponse);
            JSONObject json = new JSONObject(jsonResponse);
            checkException(json);
            toReturn = new TadoState(json.optString("presence"));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TadoException e) {
            if (attempt > 1) {
                throw e;
            } else {
                refresh();
                toReturn = getHomeState(homeId, attempt + 1);
            }
        }
        return toReturn;
    }

    public TadoZoneState getZoneState(TadoZone tadoZone) throws TadoException {
        return getZoneState(tadoZone.getHomeId(), tadoZone.getId());
    }

    public TadoZoneState getZoneState(int homeId, int idZone) throws TadoException {
        if (!this.initialized)
            throw new TadoException("error", "You must initialize the TadoConnector");
        return getZoneState(homeId, idZone, 0);
    }

    private TadoZoneState getZoneState(int homeId, int idZone, int attempt) throws TadoException {
        TadoZoneState toReturn = null;
        try {
            Map<String, String> headers = new HashMap<>();
            headers.put("Authorization", "Bearer " + this.bearer);
            String jsonResponse = doGetRequest(
                    "https://my.tado.com/api/v2/homes/" + homeId + "/zones/" + idZone + "/state", headers);
            debugMessage("getZoneState response: " + jsonResponse);
            JSONObject json = new JSONObject(jsonResponse);
            checkException(json);
            Date geolocationOverrideDisableTime;
            if (json.optString("geolocationOverrideDisableTime").isEmpty())
                geolocationOverrideDisableTime = null;
            else
                geolocationOverrideDisableTime = Date
                        .from(Instant.parse(json.optString("geolocationOverrideDisableTime")));
            JSONObject jsonSetting = json.getJSONObject("setting");
            JSONObject jsonTemperature = jsonSetting.getJSONObject("temperature");
            Temperature temperature = new Temperature(jsonTemperature.optDouble("celsius"),
                    jsonTemperature.optDouble("fahrenheit"));
            TadoSetting setting = new TadoSetting(jsonSetting.optString("type"),
                    jsonSetting.optString("power").equals("ON"), temperature);
            JSONObject jsonScheduleChange = json.optJSONObject("nextScheduleChange");
            TadoScheduleChange nextScheduleChange = null;
            if (jsonScheduleChange != null) {
                Date start = Date.from(Instant.parse(jsonScheduleChange.optString("start")));
                JSONObject jsonSetting2 = jsonScheduleChange.getJSONObject("setting");
                JSONObject jsonTemperature2 = jsonSetting2.getJSONObject("temperature");
                Temperature temperature2 = new Temperature(jsonTemperature2.optDouble("celsius"),
                        jsonTemperature2.optDouble("fahrenheit"));
                TadoSetting setting2 = new TadoSetting(jsonSetting2.optString("type"),
                        jsonSetting2.optString("power").equals("ON"), temperature2);
                nextScheduleChange = new TadoScheduleChange(start, setting2);
            }
            JSONObject jsonActivityDataPoints = json.optJSONObject("activityDataPoints");
            List<TadoDataPoint> activityDataPoints = new ArrayList<>();
            if (jsonActivityDataPoints != null) {
                Iterator<String> keys = jsonActivityDataPoints.keys();
                while (keys.hasNext()) {
                    String name = keys.next();
                    JSONObject datapoint = null;
                    if (jsonActivityDataPoints.get(name) instanceof JSONObject) {
                        datapoint = jsonActivityDataPoints.getJSONObject(name);
                    }
                    activityDataPoints.add(new TadoDataPoint(name, datapoint));
                }
            }
            JSONObject jsonSensorDataPoints = json.optJSONObject("sensorDataPoints");
            List<TadoDataPoint> sensorDataPoints = new ArrayList<>();
            if (jsonActivityDataPoints != null) {
                Iterator<String> keys = jsonSensorDataPoints.keys();
                while (keys.hasNext()) {
                    String name = keys.next();
                    JSONObject datapoint = null;
                    if (jsonSensorDataPoints.get(name) instanceof JSONObject) {
                        datapoint = jsonSensorDataPoints.getJSONObject(name);
                    }
                    sensorDataPoints.add(new TadoDataPoint(name, datapoint));
                }
            }
            toReturn = new TadoZoneState(json.optString("tadoMode"), json.getBoolean("geolocationOverride"),
                    geolocationOverrideDisableTime, setting, nextScheduleChange,
                    json.getJSONObject("link").getString("state"), activityDataPoints, sensorDataPoints);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TadoException e) {
            if (attempt > 1) {
                throw e;
            } else {
                refresh();
                toReturn = getZoneState(homeId, idZone, attempt + 1);
            }
        }
        return toReturn;
    }

    public TadoWeather getWeather(TadoHome tadoHome) throws TadoException {
        return getWeather(tadoHome.getId());
    }

    public TadoWeather getWeather(int homeId) throws TadoException {
        if (!this.initialized)
            throw new TadoException("error", "You must initialize the TadoConnector");
        return getWeather(homeId, 0);
    }

    private TadoWeather getWeather(int homeId, int attempt) throws TadoException {
        TadoWeather toReturn = null;
        try {
            Map<String, String> headers = new HashMap<>();
            headers.put("Authorization", "Bearer " + this.bearer);
            String jsonResponse = doGetRequest("https://my.tado.com/api/v2/homes/" + homeId + "/weather", headers);
            debugMessage("getWeather response: " + jsonResponse);
            JSONObject json = new JSONObject(jsonResponse);
            checkException(json);
            JSONObject jsonSolarIntensity = json.getJSONObject("solarIntensity");
            SolarIntensity solarIntensity = new SolarIntensity(jsonSolarIntensity.optString("type"),
                    jsonSolarIntensity.optDouble("percentage"),
                    Date.from(Instant.parse(jsonSolarIntensity.optString("timestamp"))));
            JSONObject jsonOutsideTemperature = json.getJSONObject("outsideTemperature");
            OutsideTemperature outsideTemperature = new OutsideTemperature(jsonOutsideTemperature.optDouble("celsius"),
                    jsonOutsideTemperature.optDouble("fahrenheit"),
                    Date.from(Instant.parse(jsonOutsideTemperature.optString("timestamp"))),
                    jsonOutsideTemperature.optString("type"),
                    jsonOutsideTemperature.getJSONObject("precision").optDouble("celsius"),
                    jsonOutsideTemperature.getJSONObject("precision").optDouble("fahrenheit"));
            JSONObject jsonWeatherState = json.getJSONObject("weatherState");
            WeatherState weatherState = new WeatherState(jsonWeatherState.optString("type"),
                    jsonWeatherState.optString("value"),
                    Date.from(Instant.parse(jsonWeatherState.optString("timestamp"))));
            toReturn = new TadoWeather(solarIntensity, outsideTemperature, weatherState);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TadoException e) {
            if (attempt > 1) {
                throw e;
            } else {
                refresh();
                toReturn = getWeather(homeId, attempt + 1);
            }
        }
        return toReturn;
    }

    public List<TadoDevice> getDevices(TadoHome tadoHome) throws TadoException {
        return getDevices(tadoHome.getId());
    }

    public List<TadoDevice> getDevices(int homeId) throws TadoException {
        if (!this.initialized)
            throw new TadoException("error", "You must initialize the TadoConnector");
        return getDevices(homeId, 0);
    }

    private List<TadoDevice> getDevices(int homeId, int attempt) throws TadoException {
        List<TadoDevice> toReturn = new ArrayList<>();
        try {
            Map<String, String> headers = new HashMap<>();
            headers.put("Authorization", "Bearer " + this.bearer);
            String jsonResponse = doGetRequest("https://my.tado.com/api/v2/homes/" + homeId + "/devices", headers);
            debugMessage("getDevices response: " + jsonResponse);
            try {
                // IF IT CAN PARSE THE JSONOBJECT IT WILL PROBABLY BE AN EXCEPTION
                JSONObject json = new JSONObject(jsonResponse);
                checkException(json);
            } catch (JSONException e) {
                // IF IT CANNOT PARSE THE JSONOBJECT IT WILL BE AN ARRAY OF DEVICES, WHICH IS
                // THE EXPECTED RESULT
                JSONArray jsonDevices = new JSONArray(jsonResponse);
                for (Object o : jsonDevices) {
                    if (o instanceof JSONObject) {
                        JSONObject device = (JSONObject) o;
                        JSONObject jsonConnectionState = device.getJSONObject("connectionState");
                        LocalDateTime timestamp = getLocalDateTimeOf(Date.from(Instant.parse(jsonConnectionState.optString("timestamp"))));
                        TadoConnectionState connsectionState = new TadoConnectionState(
                                jsonConnectionState.getBoolean("value"), timestamp);
                        JSONArray jsonCapabilities = device.getJSONObject("characteristics")
                                .getJSONArray("capabilities");
                        List<String> capabilities = new ArrayList<>();
                        for (Object capability : jsonCapabilities) {
                            if (capability instanceof String)
                                capabilities.add((String) capability);
                        }
                        TadoDevice toAdd = new TadoDevice(device.optString("deviceType"), device.optString("serialNo"),
                                device.optString("shortSerialNo"), device.optString("currentFwVersion"),
                                connsectionState, capabilities, device.optBoolean("inPairingMode"),
                                device.optString("batteryState"), new ArrayList<String>());
                        toReturn.add(toAdd);
                    }
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TadoException e) {
            if (attempt > 1) {
                throw e;
            } else {
                refresh();
                toReturn = getDevices(homeId, attempt + 1);
            }
        }
        return toReturn;
    }

    public List<TadoInstallation> getInstallations(TadoHome tadoHome) throws TadoException {
        return getInstallations(tadoHome.getId());
    }

    public List<TadoInstallation> getInstallations(int homeId) throws TadoException {
        if (!this.initialized)
            throw new TadoException("error", "You must initialize the TadoConnector");
        return getInstallations(homeId, 0);
    }

    private List<TadoInstallation> getInstallations(int homeId, int attempt) throws TadoException {
        List<TadoInstallation> toReturn = new ArrayList<>();
        try {
            Map<String, String> headers = new HashMap<>();
            headers.put("Authorization", "Bearer " + this.bearer);
            String jsonResponse = doGetRequest("https://my.tado.com/api/v2/homes/" + homeId + "/installations",
                    headers);
            debugMessage("getInstallations response: " + jsonResponse);
            try {
                // IF IT CAN PARSE THE JSONOBJECT IT WILL PROBABLY BE AN EXCEPTION
                JSONObject json = new JSONObject(jsonResponse);
                checkException(json);
            } catch (JSONException e) {
                // IF IT CANNOT PARSE THE JSONOBJECT IT WILL BE AN ARRAY OF INSTALLATIONS, WHICH
                // IS THE EXPECTED RESULT
                JSONArray jsonInstallations = new JSONArray(jsonResponse);
                for (Object o : jsonInstallations) {
                    if (o instanceof JSONObject) {
                        JSONObject installation = (JSONObject) o;
                        JSONArray jsonDevices = installation.getJSONArray("devices");
                        List<TadoDevice> devices = new ArrayList<>();
                        for (Object o2 : jsonDevices) {
                            if (o2 instanceof JSONObject) {
                                JSONObject device = (JSONObject) o2;
                                JSONObject jsonConnectionState = device.getJSONObject("connectionState");
                                LocalDateTime timestamp = getLocalDateTimeOf(Date.from(Instant.parse(jsonConnectionState.optString("timestamp"))));
                                TadoConnectionState connsectionState = new TadoConnectionState(
                                        jsonConnectionState.getBoolean("value"), timestamp);
                                JSONArray jsonCapabilities = device.getJSONObject("characteristics")
                                        .getJSONArray("capabilities");
                                List<String> capabilities = new ArrayList<>();
                                for (Object capability : jsonCapabilities) {
                                    if (capability instanceof String)
                                        capabilities.add((String) capability);
                                }
                                TadoDevice toAdd = new TadoDevice(device.optString("deviceType"),
                                        device.optString("serialNo"), device.optString("shortSerialNo"),
                                        device.optString("currentFwVersion"), connsectionState, capabilities,
                                        device.optBoolean("inPairingMode"), device.optString("batteryState"),
                                        new ArrayList<String>());
                                devices.add(toAdd);
                            }
                        }
                        TadoInstallation toAdd = new TadoInstallation(installation.getInt("id"),
                                installation.optString("type"), installation.getInt("revision"),
                                installation.getString("state"), devices);
                        toReturn.add(toAdd);
                    }
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TadoException e) {
            if (attempt > 1) {
                throw e;
            } else {
                refresh();
                toReturn = getInstallations(homeId, attempt + 1);
            }
        }
        return toReturn;
    }

    public List<User> getUsers(TadoHome tadoHome) throws TadoException {
        return getUsers(tadoHome.getId());
    }

    public List<User> getUsers(int homeId) throws TadoException {
        if (!this.initialized)
            throw new TadoException("error", "You must initialize the TadoConnector");
        return getUsers(homeId, 0);
    }

    private List<User> getUsers(int homeId, int attempt) throws TadoException {
        List<User> toReturn = new ArrayList<>();
        try {
            Map<String, String> headers = new HashMap<>();
            headers.put("Authorization", "Bearer " + this.bearer);
            String jsonResponse = doGetRequest("https://my.tado.com/api/v2/homes/" + homeId + "/users", headers);
            debugMessage("getUsers response: " + jsonResponse);
            try {
                // IF IT CAN PARSE THE JSONOBJECT IT WILL PROBABLY BE AN EXCEPTION
                JSONObject json = new JSONObject(jsonResponse);
                checkException(json);
            } catch (JSONException e) {
                // IF IT CANNOT PARSE THE JSONOBJECT IT WILL BE AN ARRAY OF INSTALLATIONS, WHICH
                // IS THE EXPECTED RESULT
                JSONArray jsonUsers = new JSONArray(jsonResponse);
                for (Object o : jsonUsers) {
                    if (o instanceof JSONObject) {
                        JSONObject user = (JSONObject) o;
                        JSONArray jsonHomes = user.getJSONArray("homes");
                        Map<Integer, String> homes = new HashMap<>();
                        for (Object o2 : jsonHomes) {
                            if (o2 instanceof JSONObject) {
                                JSONObject home = (JSONObject) o2;
                                homes.put(home.getInt("id"), home.optString("name"));
                            }
                        }
                        JSONArray jsonDevices = user.getJSONArray("mobileDevices");
                        List<MobileDevice> mobileDevices = new ArrayList<>();
                        for (Object o2 : jsonDevices) {
                            if (o2 instanceof JSONObject) {
                                JSONObject device = (JSONObject) o2;
                                mobileDevices.add(parseMobileDevice(homeId, device));
                            }
                        }
                        User toAdd = new User(user.optString("name"), user.optString("email"),
                                user.optString("username"), homes, user.optString("locale"), mobileDevices);
                        toReturn.add(toAdd);
                    }
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TadoException e) {
            if (attempt > 1) {
                throw e;
            } else {
                refresh();
                toReturn = getUsers(homeId, attempt + 1);
            }
        }
        return toReturn;
    }

    public List<MobileDevice> getMobileDevices(TadoHome tadoHome) throws TadoException {
        return getMobileDevices(tadoHome.getId());
    }

    public List<MobileDevice> getMobileDevices(int homeId) throws TadoException {
        if (!this.initialized)
            throw new TadoException("error", "You must initialize the TadoConnector");
        return getMobileDevices(homeId, 0);
    }

    private List<MobileDevice> getMobileDevices(int homeId, int attempt) throws TadoException {
        List<MobileDevice> toReturn = new ArrayList<>();
        try {
            Map<String, String> headers = new HashMap<>();
            headers.put("Authorization", "Bearer " + this.bearer);
            String jsonResponse = doGetRequest("https://my.tado.com/api/v2/homes/" + homeId + "/mobileDevices",
                    headers);
            debugMessage("getMobileDevices response: " + jsonResponse);
            try {
                // IF IT CAN PARSE THE JSONOBJECT IT WILL PROBABLY BE AN EXCEPTION
                JSONObject json = new JSONObject(jsonResponse);
                checkException(json);
            } catch (JSONException e) {
                // IF IT CANNOT PARSE THE JSONOBJECT IT WILL BE AN ARRAY OF INSTALLATIONS, WHICH
                // IS THE EXPECTED RESULT
                JSONArray jsonDevices = new JSONArray(jsonResponse);
                for (Object o : jsonDevices) {
                    if (o instanceof JSONObject) {
                        JSONObject device = (JSONObject) o;
                        toReturn.add(parseMobileDevice(homeId, device));
                    }
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TadoException e) {
            if (attempt > 1) {
                throw e;
            } else {
                refresh();
                toReturn = getMobileDevices(homeId, attempt + 1);
            }
        }
        return toReturn;
    }

    public MobileDevice getMobileDevice(int deviceId, TadoHome tadoHome) throws TadoException {
        return getMobileDevice(deviceId, tadoHome.getId());
    }

    public MobileDevice getMobileDevice(int deviceId, int homeId) throws TadoException {
        if (!this.initialized)
            throw new TadoException("error", "You must initialize the TadoConnector");
        return getMobileDevice(deviceId, homeId, 0);
    }

    private MobileDevice getMobileDevice(int deviceId, int homeId, int attempt) throws TadoException {
        MobileDevice toReturn = null;
        try {
            Map<String, String> headers = new HashMap<>();
            headers.put("Authorization", "Bearer " + this.bearer);
            String jsonResponse = doGetRequest(
                    "https://my.tado.com/api/v2/homes/" + homeId + "/mobileDevices/" + deviceId, headers);
            debugMessage("getMobileDevice response: " + jsonResponse);
            JSONObject json = new JSONObject(jsonResponse);
            checkException(json);
            toReturn = parseMobileDevice(homeId, json);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TadoException e) {
            if (attempt > 1) {
                throw e;
            } else {
                refresh();
                toReturn = getMobileDevice(deviceId, homeId, attempt + 1);
            }
        }
        return toReturn;
    }

    public Map<String, Object> getMobileDeviceSettings(MobileDevice device) throws TadoException {
        return getMobileDeviceSettings(device.getHomeId(), device.getId());
    }

    public Map<String, Object> getMobileDeviceSettings(int homeId, int deviceId) throws TadoException {
        if (!this.initialized)
            throw new TadoException("error", "You must initialize the TadoConnector");
        return getMobileDeviceSettings(homeId, deviceId, 0);
    }

    private Map<String, Object> getMobileDeviceSettings(int homeId, int deviceId, int attempt) throws TadoException {
        Map<String, Object> toReturn = null;
        try {
            Map<String, String> headers = new HashMap<>();
            headers.put("Authorization", "Bearer " + this.bearer);
            String jsonResponse = doGetRequest(
                    "https://my.tado.com/api/v2/homes/" + homeId + "/mobileDevices/" + deviceId + "/settings", headers);
            debugMessage("getMobileDeviceSettings response: " + jsonResponse);
            JSONObject json = new JSONObject(jsonResponse);
            checkException(json);
            toReturn = new HashMap<>();
            for (String key : json.keySet()) {
                toReturn.put(key, json.get(key));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TadoException e) {
            if (attempt > 1) {
                throw e;
            } else {
                refresh();
                toReturn = getMobileDeviceSettings(homeId, deviceId, attempt + 1);
            }
        }
        return toReturn;
    }

    public Capability getZoneCapabilities(TadoZone tadoZone) throws TadoException {
        return getZoneCapabilities(tadoZone.getHomeId(), tadoZone.getId());
    }

    public Capability getZoneCapabilities(int homeId, int zoneId) throws TadoException {
        if (!this.initialized)
            throw new TadoException("error", "You must initialize the TadoConnector");
        return getZoneCapabilities(homeId, zoneId, 0);
    }

    private Capability getZoneCapabilities(int homeId, int zoneId, int attempt) throws TadoException {
        Capability toReturn = null;
        try {
            Map<String, String> headers = new HashMap<>();
            headers.put("Authorization", "Bearer " + this.bearer);
            String jsonResponse = doGetRequest(
                    "https://my.tado.com/api/v2/homes/" + homeId + "/zones/" + zoneId + "/capabilities", headers);
            debugMessage("getZoneCapabilities response: " + jsonResponse);
            JSONObject json = new JSONObject(jsonResponse);
            checkException(json);
            String type = null;
            String key = null;
            Object value = null;
            for (String key1 : json.keySet()) {
                if (key1.equals("type"))
                    type = json.getString(key1);
                else {
                    key = key1;
                    value = json.get(key1);
                }
            }
            if (type == null && key == null && value == null)
                toReturn = null;
            else
                toReturn = new Capability(type, key, value);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TadoException e) {
            if (attempt > 1) {
                throw e;
            } else {
                refresh();
                toReturn = getZoneCapabilities(homeId, zoneId, attempt + 1);
            }
        }
        return toReturn;
    }

    public boolean getZoneEarlyStart(TadoZone tadoZone) throws TadoException {
        return getZoneEarlyStart(tadoZone.getHomeId(), tadoZone.getId());
    }

    public boolean getZoneEarlyStart(int homeId, int zoneId) throws TadoException {
        if (!this.initialized)
            throw new TadoException("error", "You must initialize the TadoConnector");
        return getZoneEarlyStart(homeId, zoneId, 0);
    }

    private boolean getZoneEarlyStart(int homeId, int zoneId, int attempt) throws TadoException {
        boolean toReturn = false;
        try {
            Map<String, String> headers = new HashMap<>();
            headers.put("Authorization", "Bearer " + this.bearer);
            String jsonResponse = doGetRequest(
                    "https://my.tado.com/api/v2/homes/" + homeId + "/zones/" + zoneId + "/earlyStart", headers);
            debugMessage("getZoneEarlyStart response: " + jsonResponse);
            JSONObject json = new JSONObject(jsonResponse);
            checkException(json);
            toReturn = json.getBoolean("enabled");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TadoException e) {
            if (attempt > 1) {
                throw e;
            } else {
                refresh();
                toReturn = getZoneEarlyStart(homeId, zoneId, attempt + 1);
            }
        }
        return toReturn;
    }

    public TadoOverlay getZoneOverlay(TadoZone tadoZone) throws TadoException {
        return getZoneOverlay(tadoZone.getHomeId(), tadoZone.getId());
    }

    public TadoOverlay getZoneOverlay(int homeId, int zoneId) throws TadoException {
        if (!this.initialized)
            throw new TadoException("error", "You must initialize the TadoConnector");
        return getZoneOverlay(homeId, zoneId, 0);
    }

    private TadoOverlay getZoneOverlay(int homeId, int zoneId, int attempt) throws TadoException {
        TadoOverlay toReturn = null;
        try {
            Map<String, String> headers = new HashMap<>();
            headers.put("Authorization", "Bearer " + this.bearer);
            String jsonResponse = doGetRequest(
                    "https://my.tado.com/api/v2/homes/" + homeId + "/zones/" + zoneId + "/overlay", headers);
            debugMessage("getZoneOverlay response: " + jsonResponse);
            JSONObject json = new JSONObject(jsonResponse);
            checkException(json);
            toReturn = parseTadoOverlay(json);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TadoException e) {
            if (attempt > 1) {
                throw e;
            } else {
                refresh();
                toReturn = getZoneOverlay(homeId, zoneId, attempt + 1);
            }
        }
        return toReturn;
    }

    // Put requests

    public boolean setGeoTracking(MobileDevice device, boolean enabled) throws TadoException {
        return setGeoTracking(device.getHomeId(), device.getId(), enabled);
    }

    public boolean setGeoTracking(int homeId, int deviceId, boolean enabled) throws TadoException {
        if (!this.initialized)
            throw new TadoException("error", "You must initialize the TadoConnector");
        return setGeoTracking(homeId, deviceId, enabled, 0);
    }

    private boolean setGeoTracking(int homeId, int deviceId, boolean enabled, int attempt) throws TadoException {
        boolean toReturn = false;
        try {
            Map<String, String> headers = new HashMap<>();
            headers.put("Authorization", "Bearer " + this.bearer);
            headers.put("Content-type", "application/json;charset=UTF-8");
            JSONObject toPut = new JSONObject();
            toPut.put("geoTrackingEnabled", enabled);
            String jsonResponse = doPutRequest(
                    "https://my.tado.com/api/v2/homes/" + homeId + "/mobileDevices/" + deviceId + "/settings",
                    toPut.toString(), headers);
            debugMessage("setGeoTracking response: " + jsonResponse);
            JSONObject json = new JSONObject(jsonResponse);
            checkException(json);
            toReturn = true;
        } catch (IOException e) {
            e.printStackTrace();
            toReturn = false;
        } catch (TadoException e) {
            if (attempt > 1) {
                throw e;
            } else {
                refresh();
                toReturn = setGeoTracking(homeId, deviceId, enabled, attempt + 1);
            }
        }
        return toReturn;
    }

    public boolean setZoneEarlyStart(TadoZone zone, boolean enabled) throws TadoException {
        return setZoneEarlyStart(zone.getHomeId(), zone.getId(), enabled);
    }

    public boolean setZoneEarlyStart(int homeId, int zoneId, boolean enabled) throws TadoException {
        if (!this.initialized)
            throw new TadoException("error", "You must initialize the TadoConnector");
        return setZoneEarlyStart(homeId, zoneId, enabled, 0);
    }

    private boolean setZoneEarlyStart(int homeId, int zoneId, boolean enabled, int attempt) throws TadoException {
        boolean toReturn = false;
        try {
            Map<String, String> headers = new HashMap<>();
            headers.put("Authorization", "Bearer " + this.bearer);
            headers.put("Content-type", "application/json;charset=UTF-8");
            JSONObject toPut = new JSONObject();
            toPut.put("enabled", enabled);
            String jsonResponse = doPutRequest(
                    "https://my.tado.com/api/v2/homes/" + homeId + "/zones/" + zoneId + "/earlyStart", toPut.toString(),
                    headers);
            debugMessage("setZoneEarlyStart response: " + jsonResponse);
            JSONObject json = new JSONObject(jsonResponse);
            checkException(json);
            toReturn = true;
        } catch (IOException e) {
            e.printStackTrace();
            toReturn = false;
        } catch (TadoException e) {
            if (attempt > 1) {
                throw e;
            } else {
                refresh();
                toReturn = setZoneEarlyStart(homeId, zoneId, enabled, attempt + 1);
            }
        }
        return toReturn;
    }

    public TadoOverlay setZoneOverlay(TadoZone zone, TadoOverlay overlay) throws TadoException {
        return setZoneOverlay(zone.getHomeId(), zone.getId(), overlay);
    }

    public TadoOverlay setZoneOverlay(int homeId, int zoneId, TadoOverlay overlay) throws TadoException {
        if (!this.initialized)
            throw new TadoException("error", "You must initialize the TadoConnector");
        return setZoneOverlay(homeId, zoneId, overlay, 0);
    }

    private TadoOverlay setZoneOverlay(int homeId, int zoneId, TadoOverlay overlay, int attempt) throws TadoException {
        TadoOverlay toReturn = null;
        try {
            Map<String, String> headers = new HashMap<>();
            headers.put("Authorization", "Bearer " + this.bearer);
            headers.put("Content-type", "application/json;charset=UTF-8");
            String jsonResponse = doPutRequest(
                    "https://my.tado.com/api/v2/homes/" + homeId + "/zones/" + zoneId + "/overlay",
                    overlay.toJSONObject().toString(), headers);
            debugMessage("setZoneOverlay response: " + jsonResponse);
            JSONObject json = new JSONObject(jsonResponse);
            checkException(json);
            toReturn = parseTadoOverlay(json);
        } catch (IOException e) {
            e.printStackTrace();
            toReturn = null;
        } catch (TadoException e) {
            if (attempt > 1) {
                throw e;
            } else {
                refresh();
                toReturn = setZoneOverlay(homeId, zoneId, overlay, attempt + 1);
            }
        }
        return toReturn;
    }

    public boolean setHomeState(TadoHome home, TadoState state) throws TadoException {
        return setHomeState(home.getId(), state.getPresence());
    }

    public boolean setHomeState(int homeId, String presence) throws TadoException {
        if (!this.initialized)
            throw new TadoException("error", "You must initialize the TadoConnector");
        return setHomeState(homeId, presence, 0);
    }

    private boolean setHomeState(int homeId, String presence, int attempt) throws TadoException {
        boolean toReturn = false;
        try {
            Map<String, String> headers = new HashMap<>();
            headers.put("Authorization", "Bearer " + this.bearer);
            headers.put("Content-type", "application/json;charset=UTF-8");
            JSONObject toPut = new JSONObject();
            toPut.put("homePresence", presence);
            String jsonResponse = doPutRequest("https://my.tado.com/api/v2/homes/" + homeId + "/presence",
                    toPut.toString(), headers);
            if (jsonResponse != null && !jsonResponse.trim().isEmpty()) {
                JSONObject json = new JSONObject(jsonResponse);
                checkException(json);
            }
            debugMessage("setHomeState response: " + jsonResponse);
            toReturn = true;
        } catch (IOException e) {
            e.printStackTrace();
            toReturn = false;
        } catch (TadoException e) {
            if (attempt > 1) {
                throw e;
            } else {
                refresh();
                toReturn = setHomeState(homeId, presence, attempt + 1);
            }
        }
        return toReturn;
    }

    // Delete requests

    public void deleteZoneOverlay(TadoZone zone) throws TadoException {
        deleteZoneOverlay(zone.getHomeId(), zone.getId());
    }

    public void deleteZoneOverlay(int homeId, int zoneId) throws TadoException {
        if (!this.initialized)
            throw new TadoException("error", "You must initialize the TadoConnector");
        deleteZoneOverlay(homeId, zoneId, 0);
    }

    private void deleteZoneOverlay(int homeId, int zoneId, int attempt) throws TadoException {
        try {
            Map<String, String> headers = new HashMap<>();
            headers.put("Authorization", "Bearer " + this.bearer);
            headers.put("Content-type", "application/json;charset=UTF-8");
            String jsonResponse = doDeleteRequest(
                    "https://my.tado.com/api/v2/homes/" + homeId + "/zones/" + zoneId + "/overlay", headers);
            debugMessage("deleteZoneOverlay response: " + jsonResponse);
            try {
                // IF IT CAN PARSE THE JSONOBJECT PROBABLY IT WILL BE AN EXCEPTION BECAUSE THE
                // DELETE METHOD DOESN'T RETURN ANYTHING
                JSONObject json = new JSONObject(jsonResponse);
                checkException(json);
            } catch (JSONException ignored) {

            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TadoException e) {
            if (attempt > 1) {
                throw e;
            } else {
                refresh();
                deleteZoneOverlay(homeId, zoneId, attempt + 1);
            }
        }
    }

    private MobileDevice parseMobileDevice(int homeId, JSONObject device) {
        JSONObject jsonSettings = device.getJSONObject("settings");
        Map<String, Object> settings = new HashMap<>();
        for (String key : jsonSettings.keySet()) {
            settings.put(key, jsonSettings.get(key));
        }
        MobileLocation location = null;
        if (!device.isNull("location")) {
            JSONObject jsonLocation = device.getJSONObject("location");
            location = new MobileLocation(jsonLocation.getBoolean("stale"), jsonLocation.getBoolean("atHome"),
                    jsonLocation.getJSONObject("bearingFromHome").getDouble("degrees"),
                    jsonLocation.getJSONObject("bearingFromHome").getDouble("radians"),
                    jsonLocation.getDouble("relativeDistanceFromHomeFence"));
        }
        DeviceMetadata deviceMetadata = null;
        if (!device.isNull("deviceMetadata")) {
            JSONObject jsonMetadata = device.getJSONObject("deviceMetadata");
            deviceMetadata = new DeviceMetadata(jsonMetadata.getString("platform"), jsonMetadata.getString("osVersion"),
                    jsonMetadata.getString("model"), jsonMetadata.getString("locale"));
        }
        return new MobileDevice(homeId, device.optString("name"), device.getInt("id"), settings, location,
                deviceMetadata);
    }

    private TadoOverlay parseTadoOverlay(JSONObject json) throws TadoException {
        JSONObject jsonSetting = json.getJSONObject("setting");
        Temperature temperature = new Temperature(jsonSetting.getJSONObject("temperature").getDouble("celsius"),
                jsonSetting.getJSONObject("temperature").getDouble("fahrenheit"));
        TadoSetting setting = new TadoSetting(jsonSetting.getString("type"),
                (jsonSetting.getString("power").equals("ON")), temperature);
        JSONObject jsonTermination = json.getJSONObject("termination");
        Termination termination;
        switch (jsonTermination.getString("type")) {
            case "TIMER": {
                LocalDateTime expiry = getLocalDateTimeOf(Date.from(Instant.parse(jsonTermination.optString("expiry"))));
                LocalDateTime projectedExpiry = getLocalDateTimeOf(Date.from(Instant.parse(jsonTermination.optString("projectedExpiry"))));

                termination = new TimerTermination(jsonTermination.getString("typeSkillBasedApp"),
                        jsonTermination.getInt("durationInSeconds"), expiry,
                        jsonTermination.getInt("remainingTimeInSeconds"), projectedExpiry);
                break;
            }
            case "MANUAL":
            case "TADO_MODE": {
                LocalDateTime projectedExpiry = getLocalDateTimeOf(Date.from(Instant.parse(jsonTermination.optString("projectedExpiry"))));
                termination = new ManualTermination(jsonTermination.getString("typeSkillBasedApp"), projectedExpiry);
                break;
            }
            default: {
                throw new TadoException("error",
                        "The termination type \"" + jsonTermination.getString("type") + "\" is not valid.");
            }
        }
        return new TadoOverlay(json.getString("type"), setting, termination);
    }

    private void checkException(JSONObject json) throws TadoException {
        if (json.has("errors")) {
            JSONArray errorsJson = json.getJSONArray("errors");
            JSONObject errorJson = errorsJson.getJSONObject(0);
            throw new TadoException(errorJson.optString("code"), errorJson.optString("title"));
        }
    }

    private void debugMessage(String message) {
        if (this.debug) {
            System.out.println("[TADO_JAVA_DEBUG] " + message);
        }
    }

    private String doGetRequest(String url, Map<String, String> headers) throws IOException {
        Request request;
        if (headers != null)
            request = new Request.Builder().url(url).headers(Headers.of(headers)).build();
        else
            request = new Request.Builder().url(url).build();
        Response response = client.newCall(request).execute();
        return response.body().string();
    }

    private String doPostRequest(String url, Map<String, String> body, Map<String, String> headers) throws IOException {
        Builder builder = new FormBody.Builder();
        for (Map.Entry<String, String> entry : body.entrySet()) {
            builder.add(entry.getKey(), entry.getValue());
        }
        FormBody formBody = builder.build();
        Request request;
        if (headers != null)
            request = new Request.Builder().url(url).post(formBody).headers(Headers.of(headers)).build();
        else
            request = new Request.Builder().url(url).post(formBody).build();
        Response response = client.newCall(request).execute();
        return response.body().string();
    }

    private String doPutRequest(String url, String jsonBody, Map<String, String> headers) throws IOException {
        MediaType mediaType = MediaType.parse("application/json;charset=UTF-8");
        RequestBody body = RequestBody.create(jsonBody, mediaType);
        Request request;
        if (headers != null)
            request = new Request.Builder().url(url).put(body).headers(Headers.of(headers)).build();
        else
            request = new Request.Builder().url(url).put(body).build();
        Response response = client.newCall(request).execute();
        return response.body().string();
    }

    private String doDeleteRequest(String url, Map<String, String> headers) throws IOException {
        Request request;
        if (headers != null)
            request = new Request.Builder().url(url).delete().headers(Headers.of(headers)).build();
        else
            request = new Request.Builder().url(url).delete().build();
        Response response = client.newCall(request).execute();
        return response.body().string();
    }

    private LocalDateTime getLocalDateTimeOf(Date dateToConvert){
        return Instant.ofEpochMilli(dateToConvert.getTime())
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }
}
