package com.macquarie.ofr.dawn;

import com.macquarie.ofr.dawn.domain.Case;
import com.macquarie.ofr.dawn.domain.Document;

public interface MarkLogicClient {
    String getDocumentContent(String uri);

    Document[] searchDocuments(String caseId);

    Document[] searchDocuments(String domainCollection, String filterValues);

    Case newCaseId(String createdUser, String description);

    Case[] getAllCases();

    void tagDocument(String uri, String caseId);

    void unTagDocument(String uri, String caseId);
}
