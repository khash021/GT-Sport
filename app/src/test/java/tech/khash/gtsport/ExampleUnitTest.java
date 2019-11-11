package tech.khash.gtsport;

import org.junit.Test;

import java.util.Locale;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void StringFormat() {
        String check = "12,239";

        int number = 12239;
        String numberString = String.valueOf(number);
        String result = String.format(Locale.US, "%,d", number);
        assertEquals(check, result);
    }
}