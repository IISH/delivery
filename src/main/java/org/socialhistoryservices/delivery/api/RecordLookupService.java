package org.socialhistoryservices.delivery.api;

import org.socialhistoryservices.delivery.record.entity.ArchiveHoldingInfo;
import org.socialhistoryservices.delivery.record.entity.ExternalHoldingInfo;
import org.socialhistoryservices.delivery.record.entity.ExternalRecordInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Interface used to represent an external service to lookup titles of
 * Records by providing PIDs and vice versa.
 */
public interface RecordLookupService {
    public class PageChunk {
        public PageChunk(int resultCountPerChunk, int resultStart) {
            results = new HashMap<String, String>();
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
        private Map <String, String> results;
    }

    /**
     * Search for records with the specified title.
     * @param title The title to search for.
     * @return A map of {pid,title} key-value pairs. (Maximum size defined by implementing service).
     */
    public PageChunk getRecordsByTitle(String title, int resultCountPerChunk, int resultStart);


    /**
     * Maps a PID to metadata of a record.
     * @param pid The PID to lookup.
     * @return The metadata of the record, if found.
     * @throws NoSuchPidException Thrown when the PID is not found.
     */
    public ExternalRecordInfo getRecordMetaDataByPid(String pid) throws NoSuchPidException;

    /**
     * Maps a PID to archive holding info of a record.
     * @param pid The PID to lookup.
     * @return The archive metadata of the record, if found.
     */
    public List<ArchiveHoldingInfo> getArchiveHoldingInfoByPid(String pid);

    /**
     * Get a map of holding signatures associated with this PID (if any
     * found), linking to additional holding info provided by the API.
     * @param pid The PID to search for.
     * @return A map of found (signature,holding info) tuples,
     * or an empty map if none were found.
     * @throws NoSuchPidException Thrown when the PID being searched for is
     * not found in the API.
     */
    public Map<String, ExternalHoldingInfo> getHoldingMetadataByPid(String pid) throws NoSuchPidException;
}
