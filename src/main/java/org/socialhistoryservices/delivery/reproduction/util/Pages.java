package org.socialhistoryservices.delivery.reproduction.util;

import org.socialhistoryservices.delivery.record.entity.ExternalRecordInfo;
import org.socialhistoryservices.delivery.record.entity.Record;

import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class to determine the amount of pages for books/brochures based on the record metadata.
 */
public class Pages {
    private static final int NO_NUMBER_OF_PAGES = 0;

    private static final String[] PAGES_CODES = new String[]{"p", "pages", "bl", "L", "l", "S", "s", "Seiten"};

    private static final Pattern PATTERN_CONTAINS_PAGES = Pattern.compile(createPagesPattern(PAGES_CODES));
    private static final Pattern PATTERN_BETWEEN_BRACKETS = Pattern.compile("\\(([^)]+)\\)");
    private static final Pattern PATTERN_LEFT_AND_PAGES = Pattern.compile("(.*?)(?:" + createPagesPattern(PAGES_CODES) + ")");
    private static final Pattern PATTERN_SEPERATORS = Pattern.compile("[\\p{Punct}\\s]+");
    private static final Pattern PATTERN_NUMBERS = Pattern.compile("[0-9]+");

    private final Record record;
    private final int numberOfPages;

    public Pages(Record record) {
        this.record = record;
        this.numberOfPages = determineNumberOfPages();
    }

    /**
     * Returns the number of pages,
     *
     * @return The number of pages.
     */
    public int getNumberOfPages() {
        return numberOfPages;
    }

    /**
     * Returns whether the record contains information about the number of pages.
     *
     * @return Whether info about the number of pages is available.
     */
    public boolean containsNumberOfPages() {
        return (numberOfPages > NO_NUMBER_OF_PAGES);
    }

    /**
     * Determines the number of pages based on the physical description of the record.
     * 0 is returned when multiple page numbers are found or when no page numbers can be found.
     *
     * @return The number of pages recorded for the record.
     */
    private int determineNumberOfPages() {
        String left = "";
        String pages = "";

        // Is the record actually a book or brochure?
        if (record.getExternalInfo().getMaterialType() != ExternalRecordInfo.MaterialType.BOOK)
            return NO_NUMBER_OF_PAGES;

        // Do we have a physical description?
        String physicalDescription = record.getPhysicalDescription();
        if ((physicalDescription == null) || physicalDescription.isEmpty())
            return NO_NUMBER_OF_PAGES;

        // First count the number of pages found
        int count = NO_NUMBER_OF_PAGES;
        Matcher containsPagesMatcher = PATTERN_CONTAINS_PAGES.matcher(physicalDescription);
        while (containsPagesMatcher.find())
            count++;

        // If no pages were found, or more than one, then stop here
        if ((count == 0) || (count > 1))
            return NO_NUMBER_OF_PAGES;

        // Get the part between brackets containing the number of pages
        Matcher betweenBracketsMatcher = PATTERN_BETWEEN_BRACKETS.matcher(physicalDescription);
        while (betweenBracketsMatcher.find()) {
            String betweenBrackets = betweenBracketsMatcher.group(1);
            Matcher groupContainsPagesMatcher = PATTERN_CONTAINS_PAGES.matcher(betweenBrackets);
            if (groupContainsPagesMatcher.find()) {
                physicalDescription = betweenBrackets;
                break;
            }
        }

        // Then divide in two parts: the page numbering and whatever is on the left
        Matcher leftAndPagesMatcher = PATTERN_LEFT_AND_PAGES.matcher(physicalDescription);
        while (leftAndPagesMatcher.find()) {
            left = leftAndPagesMatcher.group(1);
            for (int i = 2; i <= leftAndPagesMatcher.groupCount(); i++) {
                if (leftAndPagesMatcher.group(i) != null)
                    pages = leftAndPagesMatcher.group(i);
            }
        }

        // Attempt to divide whatever is on the left into separate parts and get the last one
        String[] leftParts = PATTERN_SEPERATORS.split(left);
        String lastLeftPart = leftParts[leftParts.length - 1];

        // If the last part contains only numbers, this might be part of the number of pages, so stop here
        Matcher numbersMatcher = PATTERN_NUMBERS.matcher(lastLeftPart);
        if (numbersMatcher.matches())
            return NO_NUMBER_OF_PAGES;

        // Otherwise we have found the number of pages
        return Integer.parseInt(pages);
    }

    /**
     * Create pattern for all page codes.
     *
     * @param codes The page codes.
     * @return The pattern.
     */
    private static String createPagesPattern(String... codes) {
        StringJoiner joiner = new StringJoiner("|");
        for (String code : codes)
            joiner.add("(?:([0-9]+) " + code + "(?:[^A-Za-z]|$))");
        return joiner.toString();
    }
}
