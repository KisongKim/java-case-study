package com.trivago.mp.casestudy;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * TODO: Implement this class.
 * Your task will be to implement two functions, one for loading the data which is stored as .csv files in the ./data
 * folder and one for performing the actual search.
 */
public class HotelSearchEngineImpl implements HotelSearchEngine {

    // cities.csv
    Map<Integer, String> cities;
    // reverse cities, this study case we can sure city name are not duplicate. also performSearch
    // use the cityName as a search parameter.
    Map<String, Integer> reverseCities;

    // hotels.csv, HotelWithCityId class extend Hotel class, added city-id,
    // in performSearch filtering with city-id more convenient.
    List<HotelWithCityId> hotelsWithCityIds = null;

    // advertisers.csv
    List<Advertiser> advertisersList = null;

    // hotel_advertiser.csv
    // I am going hotel-id as a key and advertiser-id list as a value
    Map<Integer, List<Integer>> hotelAdvertisersMap = new HashMap<>();

    @Override
    public void initialize() {
        // TODO: IMPLEMENT ME

        //TODO: read csv files can be implemented in thread. no dependencies between files.

        // read hotels with city ids;
        try (InputStream inputFS = new FileInputStream(new File("./data/hotels.csv"));
            BufferedReader br = new BufferedReader(new InputStreamReader(inputFS))) {
            hotelsWithCityIds = br.lines().skip(1).map(mapToHotelWithCityId).collect(Collectors.toList());
        } catch (IOException e) {
            throw new UnsupportedOperationException(e);
        }

        // read advertisers
        try (InputStream inputFS = new FileInputStream(new File("./data/advertisers.csv"));
            BufferedReader br = new BufferedReader(new InputStreamReader(inputFS))) {
            advertisersList = br.lines().skip(1).map(mapToAdvertiser).collect(Collectors.toList());
        } catch (IOException e) {
            throw new UnsupportedOperationException(e);
        }

        // read cities
        try (InputStream inputFS = new FileInputStream(new File("./data/cities.csv"));
            BufferedReader br = new BufferedReader(new InputStreamReader(inputFS))) {
            cities = br.lines().skip(1).map(s -> s.split(","))
                    .collect(Collectors.toMap(e -> Integer.valueOf(e[0]), e -> e[1]));
            reverseCities = cities.entrySet()
                    .stream()
                    .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
        } catch (IOException e) {
            throw new UnsupportedOperationException(e);
        }

        // read hotel_advertiser.csv
        try (InputStream inputFS = new FileInputStream(new File("./data/hotel_advertiser.csv"));
             BufferedReader br = new BufferedReader(new InputStreamReader(inputFS))) {
            List<String[]> saList = br.lines().skip(1).map(s -> s.split(",")).collect(Collectors.toList());
            for (String[] sa : saList) {
                Integer advertiserId = Integer.parseInt(sa[0]);
                Integer hotelId = Integer.parseInt(sa[1]);
                if (hotelAdvertisersMap.containsKey(hotelId)) {
                    hotelAdvertisersMap.get(hotelId).add(advertiserId);
                    hotelAdvertisersMap.put(hotelId, hotelAdvertisersMap.get(hotelId));
                } else {
                    List<Integer> list = new ArrayList<>();
                    list.add(advertiserId);
                    hotelAdvertisersMap.put(hotelId, list);
                }
            }
        } catch (IOException e) {
            throw new UnsupportedOperationException(e);
        }
        //throw new UnsupportedOperationException();
    }

    private Function<String, HotelWithCityId> mapToHotelWithCityId = (line) -> {
        String[] p = line.split(",");
        int id = Integer.valueOf(p[0]);
        int rating = Integer.valueOf(p[5]);
        int stars = Integer.valueOf(p[6]);
        int cityId = Integer.valueOf(p[1]);
        return new HotelWithCityId(id, p[4], rating, stars, cityId);
    };

    private Function<String, Advertiser> mapToAdvertiser = (line) -> {
        String[] p = line.split(",");
        int id = Integer.valueOf(p[0]);
        return new Advertiser(id, p[1]);
    };

    @Override
    public List<HotelWithOffers> performSearch(String cityName, DateRange dateRange, OfferProvider offerProvider) {
        // TODO: IMPLEMENT ME

        // TODO: I think cityName, dateRange, result of getOffersFromAdvertiser can be cashed
        //       if cityName, dataRange were same reuse the result.

        // city id to search
        Integer cityId = reverseCities.get(cityName);

        // hotel ids in city
        List<Integer> hotelIds = hotelsWithCityIds.stream().filter(i -> i.getCityId() == cityId)
                .map(HotelWithCityId::getId)
                .collect(Collectors.toList());

        // advertiser ids for hotel ids
        List<Advertiser> advertisers = new ArrayList<>();
        for (Integer hotelId : hotelIds) {
            // advertisers ids for hotel
            List<Integer> advertiserIds = hotelAdvertisersMap.get(hotelId);

            // find the advertiser name
            for (Integer advertiserId : advertiserIds) {
                Optional<String> name = advertisersList.stream()
                        .filter(i -> i.getId() == advertiserId.intValue())
                        .map(Advertiser::getName)
                        .findAny();
                if (name.isPresent()) {
                    advertisers.add(new Advertiser(advertiserId, name.get()));
                }
            }
        }

        List<HotelWithOffers> hotelWithOffersList = new ArrayList<>();
        for (Advertiser advertiser : advertisers) {
            // request
            Map<Integer, Offer> returns = offerProvider.getOffersFromAdvertiser(advertiser, hotelIds, dateRange);

            // handling a returns
            for (Map.Entry<Integer, Offer> entry : returns.entrySet()) {
                Integer hotelId = entry.getKey();

                Optional<HotelWithCityId> hotel = hotelsWithCityIds.stream()
                        .filter(i -> i.getId() == hotelId.intValue()).findFirst();
                if (hotel.isPresent()) {
                    HotelWithOffers hotelWithOffers = new HotelWithOffers(hotel.get().getHotel());
                    if (hotelWithOffersList.contains(hotelWithOffers)) {
                        // hotel already has an Offer by other advertiser
                        hotelWithOffersList.get(hotelWithOffersList.indexOf(hotelWithOffers)).getOffers().add(entry.getValue());
                    } else {
                        List<Offer> offerList = new ArrayList<>();
                        offerList.add(entry.getValue());
                        hotelWithOffers.setOffers(offerList);
                    }
                    hotelWithOffersList.add(hotelWithOffers);
                }
            }
        }
        return hotelWithOffersList;
    }

}
