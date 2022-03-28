package sg.nus.iss.workshop17.currencyconverter.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import sg.nus.iss.workshop17.currencyconverter.service.CurrencyService;

@Controller
@PropertySource("classpath:local.properties")
public class CurrencyController {
    
    @Autowired
    private CurrencyService service;

    @Value("${apiKey}")
    private String API_KEY;

    private static final String BASE_URL = "https://free.currconv.com/api/v7/";
    

    @GetMapping("/")
    public String showConversionForm(Model model){
        JsonObject currencyApi = service.getCurrencyApi(BASE_URL, API_KEY);
        List<JsonObject> countryList = service.getCountryValues(currencyApi);
        List<String> currencyList = service.getCurrencyList(countryList);
        model.addAttribute("currencyList", currencyList);
    
        return "index";
    }

    @PostMapping(path="/conversion", consumes=MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public String conversionResults(
            @RequestParam String currentCurrency, 
            @RequestParam String targetCurrency, 
            @RequestParam int amount, Model model){

        JsonObject currencyApi = service.getCurrencyApi(BASE_URL, API_KEY);
        List<JsonObject> countryList = service.getCountryValues(currencyApi);
        double conversionRate = 
                service.getConversionRate(currentCurrency, targetCurrency, 
                BASE_URL, API_KEY, countryList);
        double result = amount * conversionRate;
        JsonObject object = Json.createObjectBuilder()
            .add("conversionRate", conversionRate)
            .add("amount", amount)
            .add("result", result)
            .build();

     
        model.addAttribute("currentCurrency", currentCurrency);
        model.addAttribute("targetCurrency", targetCurrency);
        model.addAttribute("object", object);
        return "conversionResults";
    }


}
