package org.socialhistoryservices.delivery.reproduction.util;

import org.socialhistoryservices.delivery.record.entity.Record;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Copies {
    private static final int NO_NUMBER_OF_COPIES = 0;
    private static final int DEFAULT_NUMBER_OF_COPIES = 1;

    private static final Pattern PATTERN_CONTAINS_COPIES = Pattern.compile("(([0-9]+) ex)");

    private Record record;
    private int numberOfCopies;

    public Copies(Record record){
        this.record = record;
        this.numberOfCopies = determineNumberOfCopies();
    }

    public int getNumberOfCopies() {
        return numberOfCopies;
    }

    public boolean containsNumberOfCopies(){
        return numberOfCopies > NO_NUMBER_OF_COPIES;
    }

    private int determineNumberOfCopies() {
        String copies = "";

        // Do we have a physical description?
        String physicalDescription = record.getPhysicalDescription();
        if ((physicalDescription == null) || physicalDescription.isEmpty())
            return DEFAULT_NUMBER_OF_COPIES;

        // First count the number of copies found
        int count = NO_NUMBER_OF_COPIES;
        Matcher containsPagesMatcher = PATTERN_CONTAINS_COPIES.matcher(physicalDescription);
        while (containsPagesMatcher.find()) {
            count++;
            copies = containsPagesMatcher.group(2);
        }

        // If no copies were found, or more than one, then stop here
        if ((count == 0) || (count > 1))
            return DEFAULT_NUMBER_OF_COPIES;

        // Otherwise we have found the number of pages
        return Integer.parseInt(copies);
    }
}
