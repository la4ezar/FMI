package bg.sofia.uni.fmi.mjt.tagger;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class TaggerTest {
    Tagger tagger;

    @Before
    public void setUp() throws IOException {
        String cvg = "Plovdiv,Bulgaria" +
                "\n" +
                "Sofia, Bulgaria";

        tagger = new Tagger(new StringReader(cvg));
    }

    @Test
    public void testTaggerTagCities() throws IOException {
        String reader = "Plovdiv's old town is a major tourist attraction. " +
                        "It is the second largest city in Bulgaria";
        StringWriter writer = new StringWriter();
        writer.close();
        tagger.tagCities(new StringReader(reader), writer);

        assertEquals("<city country=\"Bulgaria\">Plovdiv</city>'s old town is a major tourist attraction. " +
                        "It is the second largest city in Bulgaria", writer.toString());
    }

    @Test
    public void testgetNMostTaggedCitiesIfTagCitiesWasNotInvoced() throws IOException {
        List<String> mostTaggedCities = new LinkedList<>();
        mostTaggedCities.addAll(tagger.getNMostTaggedCities(5));

        assertEquals(0, mostTaggedCities.size());
    }

    @Test
    public void testgetNMostTaggedCitiesIfNExceedesTheTotalNumberOfCitiesTagged() throws IOException {
        String reader = "Plovdiv's old town is a major tourist attraction. " +
                "It is the second largest city in Bulgaria," +
                " after the capital ,Sofia." +
                "Plovdiv is so beautiful.";
        StringWriter writer = new StringWriter();

        tagger.tagCities(new StringReader(reader), writer);

        List<String> mostTaggedCities = new LinkedList<>();
        mostTaggedCities.addAll(tagger.getNMostTaggedCities(3));

        assertEquals(2, mostTaggedCities.size());
    }

    @Test
    public void testGetNMostTaggedCitiesWhenNIsSmallerThanTagsSize() throws IOException {
        String reader = "Plovdiv's old town is a major tourist attraction. " +
                "It is the second largest city in Bulgaria," +
                " after the capital ,Sofia." +
                "Plovdiv is so beautiful.";
        StringWriter writer = new StringWriter();

        tagger.tagCities(new StringReader(reader), writer);

        List<String> mostTaggedCities = new LinkedList<>();
        mostTaggedCities.addAll(tagger.getNMostTaggedCities(1));

        assertEquals(1, mostTaggedCities.size());
        assertEquals("Plovdiv", mostTaggedCities.get(0));
    }

    @Test
    public void testGetAllTaggedCities() throws IOException {
        String reader = "Plovdiv's old town is a major tourist attraction. " +
                "It is the second largest city in Bulgaria," +
                "Plovdiv is so beautiful.";
        StringWriter writer = new StringWriter();

        tagger.tagCities(new StringReader(reader), writer);

        List<String> allTaggedCities = new ArrayList<String>();
        allTaggedCities.addAll(tagger.getAllTaggedCities());

        assertEquals(1, allTaggedCities.size());
        assertEquals("Plovdiv", allTaggedCities.get(0));
    }

    @Test
    public void testGetAllTagsCount() throws IOException {
        String str = "Plovdiv's old town is a major tourist attraction. " +
                "It is the second largest city in Bulgaria," +
                "Plovdiv is so beautiful.";
        StringWriter writer = new StringWriter();
        StringReader reader = new StringReader(str);
        tagger.tagCities(reader, writer);

        long count = tagger.getAllTagsCount();

        assertEquals(2, count);
    }

    @Test
    public void testSingleCityWithpunctuation() throws IOException {
        String reader = "Plovdiv.";
        StringWriter writer = new StringWriter();
        writer.close();
        tagger.tagCities(new StringReader(reader), writer);

        assertEquals("<city country=\"Bulgaria\">Plovdiv</city>.", writer.toString());
    }
}