package com.trivago.mp.casestudy;

public class HotelWithCityId extends Hotel {

    private final int cityId;

    public HotelWithCityId(int id, String name, int rating, int stars, int cityId) {
        super(id, name, rating, stars);
        this.cityId = cityId;
    }

    public int getCityId() {
        return cityId;
    }

    public Hotel getHotel() {
        return this;
    }

}
