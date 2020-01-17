package org.socialhistoryservices.delivery.api;

import java.util.Map;
import java.util.HashMap;

/**
 * Interface used to represent an external service to lookup titles of records by providing PIDs and vice versa.
 */
public interface RecordLookupService {
    class PageChunk {
        public PageChunk(int resultCountPerChunk, int resultStart) {
            results = new HashMap<>();
            this.resultCountPerChunk = resultCountPerChunk;
            this.resultStart = resultStart;
        }

        public int getTotalResultCount() {
            return resultCount;
        }

        protected void setTotalResultCount(int resultCount) {
            this.resultCount = resultCount;
        }

        public int getResultStart() {
            return resultStart;
        }

        public int getResultCountPerChunk() {
            return resultCountPerChunk;
        }

        public Map<String, String> getResults() {
            return results;
        }

        public void setResults(Map<String, String> results) {
            this.results = results;
        }

        private int resultCount;
        private int resultStart;
        private int resultCountPerChunk;
        private Map<String, String> results;
    }

    /**
     * Search for records with the specified title.
     *
     * @param title The title to search for.
     * @return A map of {pid,title} key-value pairs. (Maximum size defined by implementing service).
     */
    PageChunk getRecordsByTitle(String title, int resultCountPerChunk, int resultStart);

    /**
     * Maps a PID to a record metadata extractor.
     *
     * @param pid The PID to lookup.
     * @return The metadata extractor of the record, if found.
     * @throws NoSuchPidException Thrown when the PID is not found.
     */
    MetadataRecordExtractor getRecordExtractorByPid(String pid) throws NoSuchPidException;
}
