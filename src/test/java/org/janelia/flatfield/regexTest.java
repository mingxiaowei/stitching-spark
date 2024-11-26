package org.janelia.flatfield;

import org.junit.Assert;
import org.junit.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class regexTest {
    
    @Test
    public void testRegex() {
        final String text = "/something/c123/c7_n5/c8/c9-n5.json/c6_M28C_LHA_S1.czi_tile18";
        final String regex = "\\bc(\\d)(\\b|_)";
        final Pattern pattern = Pattern.compile(regex);
        final Matcher matcher = pattern.matcher(text);

        String lastMatch = null;
        String lastDigit = null;
        while (matcher.find()) {
            lastMatch = matcher.group();
            lastDigit = matcher.group(1); // Capture the digit
            System.out.println("match = " + lastMatch + ", extracted digit = " + lastDigit);
        }
        final int digit = Integer.parseInt(lastDigit);
        Assert.assertEquals(6, digit);
    }
}
