package org.socialhistoryservices.delivery.api;

import org.socialhistoryservices.delivery.api.NoSuchPidException;
import org.socialhistoryservices.delivery.api.RecordLookupService;
import org.socialhistoryservices.delivery.record.entity.ExternalHoldingInfo;
import org.socialhistoryservices.delivery.record.entity.ExternalRecordInfo;

import java.util.HashMap;
import java.util.Map;

/**
 * Mocks a lookup service for testing (so tests won't fail when the real
 * lookup service is down).
 */
public class MockRecordLookupService implements RecordLookupService {

    public String getRecordTitleByPid(String pid) throws NoSuchPidException {
        // Note: Do not depend on the exact return value when using.
        return "Open Archive";
    }

    @Override
    public PageChunk getRecordsByTitle(String title, int nrResultsPerCall, int resultStart) {
        return getRecordsByTitle(title);  //To change body of implemented methods use File | Settings | File Templates.
    }

    public PageChunk getRecordsByTitle(String title) {
        // Note: Do not depend on the exact return value when using.
        Map<String, String> result = new HashMap<String, String>();
        result.put("12345", "Open Archive");
        PageChunk pc = new PageChunk(1, 1);
        pc.setResults(result);
        pc.setTotalResultCount(1);
        return pc;
    }

    /**
     * Maps a PID to metadata of a record.
     *
     * @param pid The PID to lookup.
     * @return The metadata of the record, if found.
     * @throws NoSuchPidException Thrown when the PID is not found.
     */
    public ExternalRecordInfo getRecordMetaDataByPid(String pid) throws NoSuchPidException {
        ExternalRecordInfo externalInfo = new ExternalRecordInfo();
        externalInfo.setTitle("Open Archive");
        externalInfo.setMaterialType(ExternalRecordInfo.MaterialType.ARCHIVE);
        return externalInfo;
    }

    public Map<String, ExternalHoldingInfo> getHoldingMetadataByPid(String pid) throws NoSuchPidException {
        return new HashMap<String, ExternalHoldingInfo>();
    }
}
