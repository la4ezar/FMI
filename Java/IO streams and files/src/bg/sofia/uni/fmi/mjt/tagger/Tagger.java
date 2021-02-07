package bg.sofia.uni.fmi.mjt.tagger;

import bg.sofia.uni.fmi.mjt.city.City;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;
import java.util.Collection;
import java.util.List;
import java.util.LinkedList;
import java.util.Comparator;
import java.util.ArrayList;


public class Tagger {
    private Map<City, Integer> tags;

    /**
     * Creates a new instance of Tagger for a given list of city/country pairs
     *
     * @param citiesReader a java.io.Reader input stream containing list of cities and countries
     *                     in the specified CSV format
     */
    public Tagger(Reader citiesReader) throws IOException {
        tags = new HashMap<>();
        BufferedReader br = new BufferedReader(citiesReader);
        String line;
        while ((line = br.readLine()) != null) {
            String[] cityAndCountry = line.split(",");
            City city = new City(cityAndCountry[0], cityAndCountry[1]);
            tags.put(city, 0);
        }
        br.close();
    }

    /**
     * Processes an input stream of a text file, tags any cities and outputs result
     * to a text output stream.
     *
     * @param text   a java.io.Reader input stream containing text to be processed
     * @param output a java.io.Writer output stream containing the result of tagging
     */
    public void tagCities(Reader text, Writer output) throws IOException {
        BufferedReader br = new BufferedReader(text);
        BufferedWriter bw = new BufferedWriter(output);
        String line;
        boolean isFirstLine = true;
        while ((line = br.readLine()) != null) {
            if (!isFirstLine) {
                bw.append('\n');
            } else {
                isFirstLine = false;
            }
            String[] words = line.split(" ");
            for (int i = 0; i < words.length; ++i) {
                for (Map.Entry<City, Integer> entry : tags.entrySet()) {
                    String curWordToLowerCase = words[i].toLowerCase();
                    String curCityToLowerCase = entry.getKey().getCity().toLowerCase();
                    if (curWordToLowerCase.contains(curCityToLowerCase)) {
                        String cityWithTag = "<city country=\"" +
                                entry.getKey().getCountry() +
                                "\">" +
                                entry.getKey().getCity() +
                                "</city>";
                        words[i] = words[i].replace(entry.getKey().getCity(), cityWithTag);
                        tags.replace(entry.getKey(), entry.getValue() + 1);
                    }
                }
            }

            for (int i = 0; i < words.length; ++i) {
                if (i == 0) {
                    bw.write(words[i]);
                    if (i != words.length - 1) {
                        bw.append(' ');
                    }
                } else {
                    bw.append(words[i]);
                    if (i != words.length - 1) {
                        bw.append(' ');
                    }
                }
            }
        }
        bw.close();
        br.close();
    }

    /**
     * Returns a collection the top @n most tagged cities' unique names
     * from the last tagCities() invocation. Note that if a particular city has been tagged
     * more than once in the text, just one occurrence of its name should appear in the result.
     * If @n exceeds the total number of cities tagged, return as many as available
     * If tagCities() has not been invoked at all, return an empty collection.
     *
     * @param n the maximum number of top tagged cities to return
     * @return a collection the top @n most tagged cities' unique names
     * from the last tagCities() invocation.
     */
    public Collection<String> getNMostTaggedCities(int n) {
        boolean tagCitiesInvoked = false;
        for (Map.Entry<City, Integer> entry : tags.entrySet()) {
            if (entry.getValue() > 0) {
                tagCitiesInvoked = true;
                break;
            }
        }

        if (!tagCitiesInvoked) {
            return new LinkedList<String>();
        }

        List<Map.Entry<City, Integer>> sortedTags =
                new LinkedList<Map.Entry<City, Integer>>(tags.entrySet());

        Collections.sort(sortedTags, new Comparator<Map.Entry<City, Integer>>() {
            public int compare(Map.Entry<City, Integer> o1,
                               Map.Entry<City, Integer> o2) {
                return (o2.getValue()).compareTo(o1.getValue());
            }
        });

        List<String> result = new LinkedList<>();

        if (n >= tags.size()) {
            for (Map.Entry<City, Integer> entry : sortedTags) {
                result.add(entry.getKey().getCity());
            }
        } else {
            int cnt = 0;
            for (Map.Entry<City, Integer> entry : sortedTags) {
                if (cnt == n) {
                    break;
                }
                result.add(entry.getKey().getCity());
                ++cnt;
            }
        }

        return result;
    }

    /**
     * Returns a collection of all tagged cities' unique names
     * from the last tagCities() invocation. Note that if a particular city has been tagged
     * more than once in the text, just one occurrence of its name should appear in the result.
     * If tagCities() has not been invoked at all, return an empty collection.
     *
     * @return a collection of all tagged cities' unique names
     * from the last tagCities() invocation.
     */
    public Collection<String> getAllTaggedCities() {
        List<String> result = new ArrayList<>();

        for (Map.Entry<City, Integer> entry : tags.entrySet()) {
            if (entry.getValue() > 0) {
                result.add(entry.getKey().getCity());
            }
        }


        return result;
    }

    /**
     * Returns the total number of tagged cities in the input text
     * from the last tagCities() invocation
     * In case a particular city has been taged in several occurences, all must be counted.
     * If tagCities() has not been invoked at all, return 0.
     *
     * @return the total number of tagged cities in the input text
     */
    public long getAllTagsCount() {
        long result = 0;

        for (Map.Entry<City, Integer> entry : tags.entrySet()) {
            if (entry.getValue() > 0) {
                result += entry.getValue();
            }
        }

        return result;
    }
}