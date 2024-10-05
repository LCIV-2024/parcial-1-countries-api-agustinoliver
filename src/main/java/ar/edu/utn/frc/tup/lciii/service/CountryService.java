package ar.edu.utn.frc.tup.lciii.service;

import ar.edu.utn.frc.tup.lciii.dtos.common.CountryAmountDTO;
import ar.edu.utn.frc.tup.lciii.dtos.common.CountryDTO;
import ar.edu.utn.frc.tup.lciii.entities.CountryEntity;
import ar.edu.utn.frc.tup.lciii.model.Country;
import ar.edu.utn.frc.tup.lciii.repository.CountryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CountryService {

        private final CountryRepository countryRepository;

        private final RestTemplate restTemplate;

        public List<Country> getAllCountries() {
                String url = "https://restcountries.com/v3.1/all";
                List<Map<String, Object>> response = restTemplate.getForObject(url, List.class);
                return response.stream().map(this::mapToCountry).collect(Collectors.toList());
        }

        /**
         * Agregar mapeo de campo cca3 (String)
         * Agregar mapeo campos borders ((List<String>))
         */


        private Country mapToCountry(Map<String, Object> countryData) {
                Map<String, Object> nameData = (Map<String, Object>) countryData.get("name");

                String cca3 = (String) countryData.get("cca3");

                List<String> borders = countryData.containsKey("borders")
                        ? (List<String>) countryData.get("borders")
                        : List.of();

                return Country.builder()
                        .code(cca3)
                        .name((String) nameData.get("common"))
                        .population(((Number) countryData.get("population")).longValue())
                        .area(((Number) countryData.get("area")).doubleValue())
                        .region((String) countryData.get("region"))
                        .languages((Map<String, String>) countryData.get("languages"))
                        .borders(borders)
                        .build();
        }


        private CountryDTO mapToDTO(Country country) {
                return new CountryDTO(country.getCode(), country.getName());
        }


        public List<CountryDTO> getAllCountriesDTO() {

                List<Country> countries = getAllCountries();

                return countries.stream()
                        .map(this::mapToDTO)
                        .collect(Collectors.toList());
        }

        public List<CountryDTO> getCountriesByNameOrCode(String name, String code) {

                List<CountryDTO> countries = getAllCountriesDTO();

                return countries.stream()
                        .filter(country -> (name == null || country.getName().equalsIgnoreCase(name)) &&
                                (code == null || country.getCode().equalsIgnoreCase(code)))
                        .collect(Collectors.toList());
        }

        public List<CountryDTO> getCountriesByContinent(String continent) {
                List<Country> countries = getAllCountries();


                List<Country> filteredCountries = countries.stream()
                        .filter(country -> country.getRegion().equalsIgnoreCase(continent))
                        .collect(Collectors.toList());


                return filteredCountries.stream()
                        .map(this::mapToDTO)
                        .collect(Collectors.toList());
        }

        public List<CountryDTO> getCountriesByLanguage(String language) {
                List<Country> countries = getAllCountries();

                List<Country> filteredCountries = countries.stream()
                        .filter(country -> country.getLanguages() != null && country.getLanguages().containsValue(language))
                        .collect(Collectors.toList());

                return filteredCountries.stream()
                        .map(this::mapToDTO)
                        .collect(Collectors.toList());
        }

        public CountryDTO getCountryWithMostBorders() {
                List<Country> countries = getAllCountries();

                Country countryWithMostBorders = countries.stream()
                        .max(Comparator.comparingInt(country -> country.getBorders().size()))
                        .orElse(null);

                if (countryWithMostBorders != null) {
                        return mapToDTO(countryWithMostBorders);
                } else {
                        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No paises encontrados");
                }
        }


        public List<CountryDTO> saveRandomCountries(CountryAmountDTO amountDTO) {
                int amountToSave = amountDTO.getAmountOfCountryToSave();

                List<Country> countries = getAllCountries();

                if (amountToSave > 10) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se pueden guardar más de 10 países");
                }


                List<Country> selectedCountries = countries.stream()
                        .collect(Collectors.toList())
                        .subList(0, Math.min(amountToSave, countries.size()));


                List<CountryEntity> countryEntities = selectedCountries.stream()
                        .map(country -> new CountryEntity())
                        .collect(Collectors.toList());

                countryRepository.saveAll(countryEntities);


                return countryEntities.stream()
                        .map(countryEntity -> mapToDTO(Country.builder().build()))
                        .collect(Collectors.toList());
        }



}