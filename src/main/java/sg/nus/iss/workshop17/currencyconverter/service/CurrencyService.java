package sg.nus.iss.workshop17.currencyconverter.service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import java.util.logging.Level;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.json.stream.JsonParser;
import jakarta.json.stream.JsonParser.Event;


@Service
public class CurrencyService {
    private Logger logger = Logger.getLogger(CurrencyService.class.getName());

    public JsonObject getCurrencyApi(String BASE_URL, String API_KEY) {
        RestTemplate template = new RestTemplate();
        ResponseEntity<String> resp = template.getForEntity(BASE_URL  + "countries?apiKey=" + API_KEY, String.class);

        InputStream is = new ByteArrayInputStream(resp.getBody().getBytes());
        JsonReader reader = Json.createReader(is);
        JsonObject data = reader.readObject();
        JsonObject countryValues = data.getJsonObject("results"); 

        return countryValues;
        
    }

   
    public List<JsonObject> getCountryValues(JsonObject apiObject){

        InputStream newIs = new ByteArrayInputStream(apiObject.toString().getBytes());
        JsonParser parser = Json.createParser(newIs);      
        
        Event event = parser.next(); // START_OBJECT

        List<JsonObject> countryList = new ArrayList<>();
        while (parser.hasNext()){
            parser.next();
            JsonObject object = null;
            try {
                if (parser.hasNext())
                parser.next();
                object = (JsonObject) parser.getValue();
                logger.log(Level.INFO,object.getString("id") + " >>> " + object.toString());

                countryList.add(object);

            } catch (IllegalStateException e){
                // logger.log(Level.INFO,"Reached end of parser");
                parser.close();
                break;
            }
        }
        return countryList;
    }

    public ArrayList<String> getCurrencyList (List<JsonObject> countryList){
        
        Set<String> currencySet = new HashSet<>();

        for (int i=0; i < countryList.size() ; i++) {
            currencySet.add(countryList.get(i).getString("currencyName"));
        }

        ArrayList<String> currencyList = new ArrayList<>(currencySet);
        Collections.sort(currencyList);

        // for (int j = 0; j < currencyList.size() ; j++) {
        //     logger.log(Level.INFO,j+1 + ": " + currencyList.get(j));
        // }

        return currencyList;
    }

    public String getCurrencyId (String currencyName, List<JsonObject> countryList){

        String currencyId = countryList.stream().filter(v -> v.getString("currencyName").equals(currencyName)).findFirst().get().getString("currencyId");
        logger.log(Level.INFO, "CurrencyID >>> " + currencyId);
        return currencyId;
    }

    public double getConversionRate (String currentCurrency, String targetCurrency, String BASE_URL, String API_KEY, List<JsonObject> countryList){
        RestTemplate template = new RestTemplate();
        String currentCurrencyId = getCurrencyId(currentCurrency, countryList);
        String targetCurrencyId = getCurrencyId(targetCurrency, countryList);
    
        ResponseEntity<String> resp = 
            template.getForEntity(BASE_URL  + "convert?q=" + 
            currentCurrencyId + "_" + targetCurrencyId + 
            "&compact=ultra&apiKey=" + API_KEY, String.class);

        InputStream is = new ByteArrayInputStream(resp.getBody().getBytes());
        JsonReader reader = Json.createReader(is);
        JsonObject data = reader.readObject();
        
        System.out.println(data.toString());
        String conversion = String.valueOf(data.get(currentCurrencyId + "_" + targetCurrencyId)) ;
        // .get(currentCurrencyId + "_" + targetCurrencyId);
        System.out.println(conversion.toString());


        Double conversionRate = Double.parseDouble(conversion);
        System.out.println(conversionRate);

        return conversionRate;
    }

}
