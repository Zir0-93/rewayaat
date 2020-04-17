package com.rewayaat.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rewayaat.config.ClientProvider;
import com.rewayaat.core.data.HadithObject;

/**
 * Updates a given hadith in the hadith database.
 */
public class UpdateRequest {

    private final HadithObject newHadithObject;
    private final String hadithId;

    private final ObjectMapper mapper = new ObjectMapper();

    public UpdateRequest(HadithObject newHadithObject, String hadithId) {
        this.newHadithObject = newHadithObject;
        this.hadithId = hadithId;
    }

    public void execute() throws Exception {
        org.elasticsearch.action.update.UpdateRequest updateRequest = new org.elasticsearch.action.update.UpdateRequest();
        updateRequest.index(ClientProvider.INDEX);
        updateRequest.type(ClientProvider.TYPE);
        updateRequest.id(hadithId);
        updateRequest.doc(mapper.writeValueAsBytes(newHadithObject));
        ClientProvider.instance().getClient().update(updateRequest).get();
    }
}
