package bg.sofia.uni.fmi.mjt.spotify;

import org.junit.Before;
import org.junit.Test;


import java.io.StringReader;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class SpotifyExplorerTest {
    SpotifyExplorer se;

    @Before
    public void setUp() {
        String cvg = "id,artists,name,year,popularity,duration_ms,tempo,loudness,valence," +
                "acousticness,danceability,energy,liveness,speechiness,explicit\n" +
                "4BJqT0PrAfrxzMOxytFOIz,['Sergei Rachmaninoff'; 'James Levine'; 'Berliner Philharmoniker']," +
                "Piano Concerto No. 3 in D Minor Op. 30: III. Finale. Alla breve,1921,4,831667,80.954," +
                "-20.096,0.0594,0.982,0.279,0.211,0.665,0.0366,0\n" +
                "7xPhfUan2yNtyFG0cUWkt8,['Dennis Day'],Clancy Lowered the Boom,1921," +
                "5,180533,60.936,-12.441,0.963,0.732,0.819,0.341,0.16,0.415,0\n" +
                "1o6I8BglA6ylDMrIELygv1,['KHP Kridhamardawa Karaton Ngayogyakarta Hadiningrat']," +
                "Gati Bali,1921,5,500062,110.339,-14.85,0.0394,0.961,0.328,0.166,0.101,0.0339,1\n" +
                "3ftBPsC5vPBKxYSee08FDH,['Frank Parker'],Danny Boy,1921,3,210000,100.109,-9.316,0.165," +
                "0.967,0.275,0.309,0.381,0.0354,0\n" +
                "4d6HGyGT8e121BsdKmw9v6,['Phil Regan'; 'James Levine'],When Irish Eyes Are Smiling,1925,2,166693," +
                "101.665,-10.096,0.253,0.957,0.418,0.193,0.229,0.038,0\n" +
                "0mb6y9FwHzTOw20lPIKmj1,['Warren Zevon'],Lawyers Guns and Money,1986,37,210093," +
                "94.478,-7.227,0.762,0.0542,0.633,0.752,0.0432,0.0323,0\n" +
                "1B1w0fZM7BKsgqa12dK9Gv,['Eddie Money'],Endless Nights,1986,38,203760,111.55," +
                "-9.935,0.688,0.238,0.633,0.61,0.0696,0.0316,0\n" +
                "03d3DCqwvt65Orfdomjs6e,['Talk Talk'],Living in Another World - 1997 Remaster," +
                "1986,54,418173,110.544,-8.453,0.938,0.00366,0.56,0.857,0.225,0.0324,0\n" +
                "6XLobzCdi98lFcxG3eGYNr,['Juan Gabriel'],Hasta Que Te Conocí,1986,52,435493,172.395," +
                "-15.949,0.218,0.609,0.322,0.214,0.161,0.0345,0\n" +
                "7LZwFnHW2oWzFqtsmfrWw7,['Temple Of The Dog'],All Night Thing,1990,40,231760,126.876," +
                "-10.618,0.285,0.391,0.605,0.448,0.0956,0.0337,0\n" +
                "2J7rsrL3EcFMNIg284xb9F,['Jerry Rivera'],Ese - Balada,1990,55,137187,147.745,-7.52,0.511," +
                "0.791,0.614,0.442,0.144,0.0361,0\n" +
                "1mYU1xNK2THM44DpukZGY4,['Sammy Davis Jr.'],I Ain't Got Nobody,1990,45,144800,200.098," +
                "-10.283,0.738,0.885,0.53,0.408,0.0555,0.279,0";

        se = new SpotifyExplorer(new StringReader(cvg));
    }

    @Test
    public void testSpotifyExplorerGetAllSpotifyTracks() {
        List<SpotifyTrack> tracks = new ArrayList<>();
        tracks.addAll(se.getAllSpotifyTracks());

        assertEquals("Piano Concerto No. 3 in D Minor Op. 30: III. Finale. Alla breve", tracks.get(0).name());
        assertEquals("Clancy Lowered the Boom", tracks.get(1).name());
        assertEquals("Gati Bali", tracks.get(2).name());
        assertEquals("Danny Boy", tracks.get(3).name());
        assertEquals("When Irish Eyes Are Smiling", tracks.get(4).name());

    }

    @Test
    public void testSpotifyExplorerGetExplicitSpotifyTracks() {
        List<SpotifyTrack> tracks = new ArrayList<>();
        tracks.addAll(se.getExplicitSpotifyTracks());

        assertEquals(1, tracks.size());
        assertEquals("Gati Bali", tracks.get(0).name());
    }

    @Test
    public void testSpotifyExplorerGroupSpotifyTracksByYear() {
        Map<Integer, Set<SpotifyTrack>> tracksByYear = new HashMap<>();
        tracksByYear.putAll(se.groupSpotifyTracksByYear());

        assertEquals(4, tracksByYear.get(1921).size());
        assertEquals(1, tracksByYear.get(1925).size());
        assertEquals(4, tracksByYear.size());
    }

    @Test
    public void testSpotifyExplorerGetArtistActiveYears() {
        assertEquals(5, se.getArtistActiveYears("James Levine"));
        assertEquals(1, se.getArtistActiveYears("Phil Regan"));
        assertEquals(0, se.getArtistActiveYears("Lachezar Bogomilov"));
    }

    @Test
    public void testSpotifyExplorerGetTopNHighestValenceTracksFromThe80s() {
        List<SpotifyTrack> tracks = new ArrayList<>();
        tracks.addAll(se.getTopNHighestValenceTracksFromThe80s(2));

        assertEquals(2, tracks.size());
        assertEquals("Living in Another World - 1997 Remaster", tracks.get(0).name());
        assertEquals("Lawyers Guns and Money", tracks.get(1).name());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSpotifyExplorerGetTopNHighestValenceTracksFromThe80sWithNegativeN() {
        List<SpotifyTrack> tracks = new ArrayList<>();
        tracks.addAll(se.getTopNHighestValenceTracksFromThe80s(-2));
    }


    @Test
    public void testSpotifyExplorerGetMostPopularTrackFromThe90s() {
        assertEquals("Ese - Balada", se.getMostPopularTrackFromThe90s().name());
    }

    @Test
    public void testSpotifyExplorerGetNumberOfLongerTracksBeforeYear() {
        assertEquals(4, se.getNumberOfLongerTracksBeforeYear(3, 1930));
        assertEquals(2, se.getNumberOfLongerTracksBeforeYear(4, 1930));
    }

    @Test
    public void testSpotifyExplorerGetTheLoudestTrackInYear() {
        assertEquals("Lawyers Guns and Money", se.getTheLoudestTrackInYear(1986).get().name());
    }
}


