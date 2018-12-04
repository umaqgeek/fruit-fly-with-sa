/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controllers;

import helpers.Func;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Random;
import models.Bee;
import models.City;

/**
 *
 * @author umarmukhtar
 */
public class FruitFlySmellRadius {
    
    private static final float MULTIPLIER = 1f;
    private static final float LATITUTE_RANGE = MULTIPLIER * 0.005f; // latitute range of bee around current place.
    private static final float LONGITUTE_RANGE = MULTIPLIER * 0.0025f; // longitute range of bee around current place.
    
    public void process(boolean isWithSwap, Object initCities[][], int numBees, double temperature, double rate) {
        
        FruitFlySmellRadius ffsr = new FruitFlySmellRadius();
        
        // init set cities
        City cities[] = new City[initCities.length];
        for (int cityIndex = 0; cityIndex < initCities.length; cityIndex++) {
            String cityName = (String) initCities[cityIndex][0];
            float cityX = (float) initCities[cityIndex][1];
            float cityY = (float) initCities[cityIndex][2];
            cities[cityIndex] = new City(cityName, cityX, cityY);
        }
        
        // set bees
        Bee bee = new Bee(cities[0].getxAxis(), cities[0].getyAxis(), 3);
        
        // init fly
        Properties propPath = ffsr.getCityPath(isWithSwap, bee, cities, temperature);
        bee = (Bee) propPath.get(Func.INNER_BEE_TAG);
        
        Bee bestBee = new Bee(cities[0].getxAxis(), cities[0].getyAxis());
        double bestTotalDistance = 10000.00;
        int bestIndex = 0;
        
        // process search fly
        for (int index = 0; temperature > 1.0; index++) {
            
            for (int beeIndex = 0; beeIndex < numBees; beeIndex++) {
                
                // start back at city 1 for each iteration
                bee = new Bee(cities[0].getxAxis(), cities[0].getyAxis());

                // go fly
                propPath = ffsr.getCityPath(isWithSwap, bee, cities, temperature);
                bee = (Bee) propPath.get(Func.INNER_BEE_TAG);
                
                // process to ONLY calculate the distance between their cities, exclude the bee distances.
                ArrayList<City> beePath = new ArrayList<City>();
                for (int cityIndex = 0; cityIndex < bee.paths().size(); cityIndex++) {
                    City beeCity = new City();
                    beeCity.setName((String) bee.paths().get(cityIndex).get(Func.CITY_NAME_TAG));
                    beeCity.setxAxis((float) bee.paths().get(cityIndex).get(Func.CITY_X_TAG));
                    beeCity.setyAxis((float) bee.paths().get(cityIndex).get(Func.CITY_Y_TAG));
                    beePath.add(beeCity);
                }
                Properties propBee = ffsr.getCitiesTotalDistances(bee, beePath);
                bee = (Bee) propBee.get(Func.INNER_BEE_TAG);
                bee.setTotalDistance((double) propBee.get(Func.DISTANCE_TAG));
                
                // compare best distance with current distance
                if (bee.getTotalDistance() < bestTotalDistance) {
                    bestTotalDistance = bee.getTotalDistance();
                    bestBee.setxAxis(bee.getxAxis());
                    bestBee.setyAxis(bee.getyAxis());
                    bestBee.setTotalDistance(bee.getTotalDistance());
                    bestBee.setPaths(bee.paths());
                    bestIndex = index;
                }
            }
            
            // decrement
            temperature = (1 - rate) * temperature;
        }
        
        // view the best path
        System.out.println("BEST BEE:");
        System.out.println("-------------");
        Func.viewAllPath(bestIndex+1, bestBee);
    }
    
    private Properties getCityPath(boolean isWithSwap, Bee bee, City cities[], double temperature) {
        Properties prop = new Properties();
        ArrayList<City> cityPath = new ArrayList<City>();
        ArrayList<Properties> paths = new ArrayList<Properties>();
        bee.setPaths(paths);
        try {
            
            double beeTotalDistance = 0.00;
            
            for (int cityIndex = 0; cityIndex < cities.length; cityIndex++) {
                
                boolean isMainCity = (cityIndex == 0);
                
                FruitFlySmellRadius ffsr = new FruitFlySmellRadius();
                Properties propBest = ffsr.getNearestCity(isWithSwap, bee, cities, isMainCity, temperature);
                
                City bestCity = (City) propBest.get(Func.BEST_CITY_TAG);
                bee = (Bee) propBest.get(Func.INNER_BEE_TAG);
                
                float XIndex = Math.abs(bestCity.getxAxis() + (Func.getRandom() * (LATITUTE_RANGE * 2)) - LATITUTE_RANGE);
                float YIndex = Math.abs(bestCity.getyAxis() + (Func.getRandom() * (LONGITUTE_RANGE * 2)) - LONGITUTE_RANGE);
                
                bee.paths().get(cityIndex).put(Func.CITY_NAME_TAG, bestCity.getName());
                bee.paths().get(cityIndex).put(Func.CITY_X_TAG, bestCity.getxAxis());
                bee.paths().get(cityIndex).put(Func.CITY_Y_TAG, bestCity.getyAxis());
                bee.paths().get(cityIndex).put(Func.X_TAG, bee.getxAxis());
                bee.paths().get(cityIndex).put(Func.Y_TAG, bee.getyAxis());
                
                bee.setxAxis(XIndex);
                bee.setyAxis(YIndex);
                
                if (cityIndex < cities.length - 1) {
                    float locA[] = {bestCity.getxAxis(), bestCity.getyAxis()};
                    float locB[] = {XIndex, YIndex};
                    double beeShortDistance = (isMainCity) ? (0.00) : (Func.getDistanceInKM(locA, locB));
//                    double beeShortDistance = 0.00;
                    double currentDistance = (double) bee.paths().get(cityIndex).get(Func.DISTANCE_TAG);
                    currentDistance += beeShortDistance;
                    bee.paths().get(cityIndex).put(Func.DISTANCE_TAG, currentDistance);
                }
                
                beeTotalDistance += (double) bee.paths().get(cityIndex).get(Func.DISTANCE_TAG);
                
                cityPath.add(bestCity);
            }
            
            FruitFlySmellRadius ffsr = new FruitFlySmellRadius();
            Properties propBest = ffsr.getNearestCity(isWithSwap, bee, cities, true, temperature);
            City bestCity = (City) propBest.get(Func.BEST_CITY_TAG);
            bee = (Bee) propBest.get(Func.INNER_BEE_TAG);
            float XIndex = Math.abs(bestCity.getxAxis() + (Func.getRandom() * (LATITUTE_RANGE * 2)) - LATITUTE_RANGE);
            float YIndex = Math.abs(bestCity.getyAxis() + (Func.getRandom() * (LONGITUTE_RANGE * 2)) - LONGITUTE_RANGE);
            bee.paths().get(cities.length).put(Func.CITY_NAME_TAG, bestCity.getName());
            bee.paths().get(cities.length).put(Func.CITY_X_TAG, bestCity.getxAxis());
            bee.paths().get(cities.length).put(Func.CITY_Y_TAG, bestCity.getyAxis());
            bee.paths().get(cities.length).put(Func.X_TAG, XIndex);
            bee.paths().get(cities.length).put(Func.Y_TAG, YIndex);
            bee.setxAxis(XIndex);
            bee.setyAxis(YIndex);
            
            beeTotalDistance += (double) bee.paths().get(cities.length).get(Func.DISTANCE_TAG);
            
            bee.setTotalDistance(beeTotalDistance);
            
            cityPath.add(bestCity);
            
        } catch (Exception e) {
            cityPath.removeAll(cityPath);
            cityPath = new ArrayList<City>();
        }
        prop.put(Func.INNER_BEE_TAG, bee);
        prop.put(Func.BEE_CITY_PATH, cityPath);
        return prop;
    }
    
    private Properties getNearestCity(boolean isWithSwap, Bee bee, City cities[], boolean isMainCity, double temperature) {
        Properties prop = new Properties();
        City bestCity = null;
        City worstCity = null;
        try {
            
            float XIndex = bee.getxAxis();
            float YIndex = bee.getyAxis();
            double bestSmell = 0.00;
            int bestCityIndex = 0;
            double bestDIndex = Func.MAX_DOUBLE_VALUE;
            double worstSmell = Func.MAX_DOUBLE_VALUE;
            int worstCityIndex = cities.length - 1;
            double worstDIndex = 0.00;
            
            if (isMainCity) {
                
                int cityIndex = 0;
                float locA[] = {cities[cityIndex].getxAxis(), cities[cityIndex].getyAxis()};
                float locB[] = {XIndex, YIndex};
                double DIndex = Func.getDistanceInKM(locA, locB);
                double SIndex = 1 / DIndex;
                double SmellIndex = SIndex;
                
                bestSmell = SmellIndex;
                bestCityIndex = cityIndex;
                bestDIndex = DIndex;
                bestCity = cities[bestCityIndex];
                
                worstSmell = SmellIndex;
                worstCityIndex = cityIndex;
                worstDIndex = DIndex;
                worstCity = cities[worstCityIndex];
                
                Properties propBee = new Properties();
                propBee.put(Func.DISTANCE_TAG, bestDIndex);
                propBee.put(Func.SMELL_CONCENTRATION_TAG, bestSmell);
                bee.paths().add(propBee);
                
            } else {
                
                // To ask all cities, which one is the best closest city to the bee.
                for (int cityIndex = 0; cityIndex < cities.length; cityIndex++) {
                    FruitFlySmellRadius ffsr = new FruitFlySmellRadius();
                    if (!ffsr.isHasCity(bee, cities[cityIndex])) {
                        float locA[] = {cities[cityIndex].getxAxis(), cities[cityIndex].getyAxis()};
                        float locB[] = {XIndex, YIndex};
                        double DIndex = Func.getDistanceInKM(locA, locB);
                        double SIndex = 1 / DIndex; // Smell concentration = 1 over distance.
                        double SmellIndex = SIndex; // here doesn't use any fitness function, just measure distances.
                        if (SmellIndex > bestSmell) {
                            bestSmell = SmellIndex;
                            bestCityIndex = cityIndex;
                            bestDIndex = DIndex;
                        }
                        if (SmellIndex < worstSmell) {
                            worstSmell = SmellIndex;
                            worstCityIndex = cityIndex;
                            worstDIndex = DIndex;
                        }
                    }
                }
                bestCity = cities[bestCityIndex];
                worstCity = cities[worstCityIndex];
                
                // this using swap technique to choose far city (SA's Concept).
                if (isWithSwap) {
                    Object solution[][] = {
                        {bestCity, bestSmell, bestCityIndex, bestDIndex}, 
                        {worstCity, worstSmell, worstCityIndex, worstDIndex}
                    };
                    Random dice = new Random();
                    int currentDice = dice.nextInt(2);
                    int newDice = (currentDice == 1) ? (0) : (1);
                    Object currentSolution[] = solution[currentDice];
                    Object newSolution[] = solution[newDice];
                    double rand = Func.getRandomDouble();
                    if (Func.acceptanceProbability((double) currentSolution[3], (double) newSolution[3], temperature) > rand) {
                        currentSolution = newSolution.clone();
                        bestSmell = (double) currentSolution[1];
                        bestCityIndex = (int) currentSolution[2];
                        bestDIndex = (double) currentSolution[3];
                        bestCity = (City) currentSolution[0];
                    }
                }
                
                Properties propBee = new Properties();
                propBee.put(Func.DISTANCE_TAG, bestDIndex);
                propBee.put(Func.SMELL_CONCENTRATION_TAG, bestSmell);
                bee.paths().add(propBee);
            }
            
        } catch (Exception e) {
            bestCity = null;
            System.out.println("Error: "+e.getMessage());
        }
        prop.put(Func.BEST_CITY_TAG, bestCity);
        prop.put(Func.INNER_BEE_TAG, bee);
        return prop;
    }
    
    private boolean isHasCity(Bee bee, City city) {
        boolean status = false;
        try {
            
            ArrayList<Properties> paths = bee.paths();
            if (paths.size() > 0) {
                for (int index = 0; index < paths.size(); index++) {
                    Properties path = paths.get(index);
                    if (path.get(Func.CITY_NAME_TAG) != null) {
                        String cityName = (String) path.get(Func.CITY_NAME_TAG);
                        if (cityName.contains(city.getName())) {
                            status = true;
                            break;
                        }
                    }
                }
            }
            
        } catch (Exception e) {
            status = false;
            System.out.println("Error: "+e.getMessage());
        }
        return status;
    }
    
    private Properties getCitiesTotalDistances(Bee bee, ArrayList<City> cities) {
        double totalDistance = 0.00;
        Properties prop = new Properties();
        try {

            for (int cityIndex = 0; cityIndex < cities.size() - 1; cityIndex++) {

                float locA[] = {cities.get(cityIndex).getxAxis(), cities.get(cityIndex).getyAxis()};
                float locB[] = {cities.get(cityIndex + 1).getxAxis(), cities.get(cityIndex + 1).getyAxis()};
                double distance = Func.getDistanceInKM(locA, locB);
                totalDistance += distance;
                
                Properties propBee = new Properties();
                propBee.put(Func.DISTANCE_TAG, distance);
                propBee.put(Func.CITY_NAME_TAG, cities.get(cityIndex + 1).getName());
                propBee.put(Func.CITY_X_TAG, locB[0]);
                propBee.put(Func.CITY_Y_TAG, locB[1]);
                propBee.put(Func.X_TAG, locB[0]);
                propBee.put(Func.Y_TAG, locB[1]);
                bee.paths().set(cityIndex + 1, propBee);
            }

        } catch (Exception e) {
            totalDistance = 0.00;
            System.out.println("Error: "+e.getMessage());
        }
        prop.put(Func.DISTANCE_TAG, totalDistance);
        prop.put(Func.INNER_BEE_TAG, bee);
        return prop;
    }
}
