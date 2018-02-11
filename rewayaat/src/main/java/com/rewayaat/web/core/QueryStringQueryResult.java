package com.rewayaat.web.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rewayaat.web.config.ClientProvider;
import com.rewayaat.web.data.hadith.HadithObject;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Represents a processed query for narrations.
 */
public class QueryStringQueryResult implements RewayaatQueryResult {

    private final int pageSize = 10;
    private String userQuery;
    private int page;
    private final ObjectMapper mapper = new ObjectMapper();
    private String fuzziedUserQuery;
    private long totalResultsSize;

    public QueryStringQueryResult(String userQuery, int page) {
        this.userQuery = userQuery;
        this.fuzziedUserQuery = new RewayaatQuery(userQuery).query();
        this.page = page;
    }

    @Override
    public HadithObjectCollection result() throws Exception {
        List<HadithObject> hadithes = new ArrayList<HadithObject>();

        HighlightBuilder highlightBuilder = new HighlightBuilder().field("english").field("notes").field("arabic")
                .field("book").field("section").field("part").field("chapter").field("publisher").field("source")
                .field("volume").postTags("</span>").preTags("<span class=\"highlight\">")
                .highlightQuery(QueryBuilders.queryStringQuery(userQuery).field("english").field("section")
                        .field("part").field("chapter").field("volume").field("arabic").field("book").field("arabic")
                        .field("tags").field("publisher").field("source"))
                .highlightQuery(QueryBuilders.queryStringQuery(fuzziedUserQuery).field("english").field("section")
                        .field("part").field("chapter").field("publisher").field("source").field("volume")
                        .field("arabic").field("book").field("arabic").field("tags"))
                .numOfFragments(0);

        SearchResponse resp = ClientProvider.instance().getClient().prepareSearch(ClientProvider.INDEX)
                .setTypes(ClientProvider.TYPE).setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                .setQuery(QueryBuilders.boolQuery().must(QueryBuilders.queryStringQuery(fuzziedUserQuery))
                        .should(QueryBuilders.queryStringQuery(userQuery).boost(10)))
                .highlighter(highlightBuilder).setFrom(page * pageSize).setSize(pageSize).setExplain(true)
                .addSort("_score", SortOrder.DESC).execute().get();

        totalResultsSize = resp.getHits().getTotalHits();

        SearchHit[] results = resp.getHits().getHits();
        System.out.println("Current results: " + results.length);
        for (SearchHit hit : results) {
            System.out.println("------------------------------");
            Map<String, Object> result = hit.getSource();
            result.put("_id", hit.getId());
            for (Entry<String, HighlightField> entry : hit.getHighlightFields().entrySet()) {
                // Add the highlighted fragment if it is not a Qur'anic verse.
                // Front-end will take care of highlighting Qur'anic verses.
                if (!entry.getValue().fragments()[0].toString().matches(".*[0-114]:[0-300].*")) {
                    result.put(entry.getKey(), entry.getValue().fragments()[0].toString());
                }
            }
            hadithes.add(mapper.convertValue(result, HadithObject.class));
            System.out.println(result);
        }
        return new HadithObjectCollection(hadithes, resp.getHits().getTotalHits());
    }

    public long totalResultSize() {
        return this.totalResultsSize;
    }
}
