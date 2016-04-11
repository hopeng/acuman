'use strict';

var CONF = {
  URL: {
    GET_DOC_CONTENT: 'v1/documents?uri=%s',
    GET_WORM_DOC_CONTENT: 'v1/documents?uri=%s&storage=Worm',
    SEARCH_DOC_BY_CASE: 'v1/search?caseId=%s',
    SEARCH_DOC_BY_DOMAIN: 'v1/search?domain=%s&filterValues=%s',

    ALL_CASES: 'v1/cases',
    NEW_CASE_ID: 'v1/cases?description=%s', // POST
    TAG_DOC: 'v1/documents?uri=%s&caseId=%s&action=Tag', // PUT
    UNTAG_DOC: 'v1/documents?uri=%s&caseId=%s&action=UnTag' // PUT
  }
};
