package com.trivago.mp.casestudy;

import java.util.List;

/**
 * Wraps a hotel and a list of {@link Offer Offers}.
 */
public class HotelWithOffers {
    private final Hotel hotel;

    /**
     * A list of concrete advertiser offers for this hotel
     */
    List<Offer> offers;

    public HotelWithOffers(Hotel hotel) {
        this.hotel = hotel;
    }

    public Hotel getHotel() {
        return hotel;
    }

    public List<Offer> getOffers() {
        return offers;
    }

    public void setOffers(List<Offer> offers) {
        this.offers = offers;
    }

    @Override
    public String toString() {
        return "HotelWithOffers{" + "hotel=" + hotel + ", offers=" + offers + '}';
    }

    @Override
    public int hashCode() {
        int hash = 7;
        return 31 * hash + hotel.hashCode();
    }

}
