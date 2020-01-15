package org.socialhistoryservices.delivery.api;

import org.socialhistoryservices.delivery.record.entity.ArchiveHoldingInfo;
import org.socialhistoryservices.delivery.record.entity.ExternalHoldingInfo;
import org.socialhistoryservices.delivery.record.entity.ExternalRecordInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.socialhistoryservices.delivery.record.entity.Holding;
import org.w3c.dom.Node;

/**
 * Mocks a lookup service for testing (so tests won't fail when the real lookup service is down).
 */
public class MockRecordLookupService implements RecordLookupService {
    @Override
    public PageChunk getRecordsByTitle(String title, int nrResultsPerCall, int resultStart) {
        Map<String, String> result = new HashMap<>();
        result.put("12345", "Open Archive");
        PageChunk pc = new PageChunk(1, 1);
        pc.setResults(result);
        pc.setTotalResultCount(1);
        return pc;
    }

    @Override
    public ExternalRecordInfo getRecordMetaDataByPid(String pid) throws NoSuchPidException {
        ExternalRecordInfo externalInfo = new ExternalRecordInfo();
        externalInfo.setTitle("Open Archive");
        externalInfo.setMaterialType(ExternalRecordInfo.MaterialType.ARCHIVE);
        return externalInfo;
    }

    @Override
    public List<ArchiveHoldingInfo> getArchiveHoldingInfoByPid(String pid) {
        ArchiveHoldingInfo ahi = new ArchiveHoldingInfo();
        ahi.setId(1);
        ahi.setShelvingLocation("Location");
        ahi.setMeter("12");
        ahi.setNumbers("45");
        ahi.setFormat("box");
        ahi.setNote("A note.");

        List<ArchiveHoldingInfo> ahis = new ArrayList<>();
        ahis.add(ahi);
        return ahis;
    }

    @Override
    public Map<String, ExternalHoldingInfo> getHoldingMetadataByPid(String pid) throws NoSuchPidException {
        ExternalHoldingInfo ehi = new ExternalHoldingInfo();
        ehi.setBarcode("N1234567890");

        Map<String, ExternalHoldingInfo> ehis = new HashMap<>();
        ehis.put("IISG 12345", ehi);
        return ehis;
    }

    @Override
    public String getUnitIdsFromContainer(String pid, String container, boolean includeCurrentContainer) throws NoSuchPidException {
        return null;
    }
}
