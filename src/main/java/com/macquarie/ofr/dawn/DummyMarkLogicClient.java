package com.macquarie.ofr.dawn;

import com.macquarie.ofr.dawn.domain.Case;
import com.macquarie.ofr.dawn.domain.Document;
import com.macquarie.ofr.dawn.util.JSONConverter;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Profile("dummyClient")
@Primary
@Service
public class DummyMarkLogicClient implements MarkLogicClient {

	private static AtomicInteger caseGenerator = new AtomicInteger(1000);

	private static final String BASE_DATA_PATH = System.getProperty("user.dir") + "/static/dummy/";

	@Override
	public String getDocumentContent(String uri) {
		String folderName = "";
		if (uri.contains("central-customer")) {
			folderName = "counterparty/";
		} else if (uri.contains("settlement")) {
			folderName = "settlement/";
		} else if (uri.contains("globe")) {
			folderName = "globe/";
		}

		String fileName = uri.substring(uri.lastIndexOf('/') + 1, uri.length());
		return readFile(folderName + fileName);
	}

	@Override
	public Document[] searchDocuments(String caseId) {
		String jsonContent = readFile(caseId + "-docs.json");
		List<Document> list = JSONConverter.fromJsonArray(jsonContent, Document.class);
		return list.toArray(new Document[0]);
	}

	@Override
	public Document[] searchDocuments(String domainCollection, String filterValues) {
		String resultFile;
		switch (domainCollection) {
			case "/canon/domain/settlement/transfer":
				resultFile = "search-settlement.json";
				break;
			case "/canon/domain/legacy/central-customer/customer":
				resultFile = "search-counterParty.json";
				break;
			case "/canon/domain/legacy/globe/aml/basic-entity-data":
				resultFile = "search-globe.json";
				break;
			default:
				resultFile = null;
		}

		List<Document> result = new ArrayList<>();
		if (resultFile != null) {
			String jsonContent = readFile(resultFile);
			result = JSONConverter.fromJsonArray(jsonContent, Document.class);
		}
		return result.toArray(new Document[0]);
	}

	@Override
	public Case newCaseId(String createdUser, String description) {
		return new Case(Integer.toString(caseGenerator.incrementAndGet()), "dummy desc", "dummy", "", "", "");
	}

	@Override
	public Case[] getAllCases() {
		String jsonString = readFile("cases.json");
		List<Case> result = JSONConverter.fromJsonArray(jsonString, Case.class);
		return result.toArray(new Case[0]);
	}

	@Override
	public void tagDocument(String uri, String caseId) {
		try {
			Thread.sleep(1000);
		} catch (InterruptedException ignored) {
		}
	}

	@Override
	public void unTagDocument(String uri, String caseId) {
		try {
			Thread.sleep(1000);
		} catch (InterruptedException ignored) {
		}
	}

	private String readFile(String fileName) {
		Resource resource = new FileSystemResource(BASE_DATA_PATH + fileName);
		try {
			return FileCopyUtils.copyToString(new InputStreamReader(resource.getInputStream()));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
