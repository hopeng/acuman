package com.macquarie.ofr.dawn;

import com.macquarie.ofr.dawn.domain.Case;
import com.macquarie.ofr.dawn.domain.Document;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RestController
@RequestMapping("/v1")
public class CaseManagerController {
	private static final Logger log = LoggerFactory.getLogger(CaseManagerController.class);
	private static final String MEDIA_TYPE_XML_UTF_8 = "application/xml; charset=UTF-8";

	public enum DocumentAction {Tag, UnTag}
	public enum DocumentStorage {MarkLogic, Worm}

	@Autowired
	private MarkLogicClient markLogicClient;

	@Autowired
	private WormClient wormClient;

	@RequestMapping(value = "documents", method = GET, produces = MEDIA_TYPE_XML_UTF_8)
	public String getDocumentContent(
			@RequestParam String uri,
			@RequestParam(required = false) String storage) {
		log.info("getDocumentContent called: uri={}, storage={}", uri, storage);
		DocumentStorage storageEnum = EnumUtils.getEnum(DocumentStorage.class, storage);
		storageEnum = storageEnum == null ? DocumentStorage.MarkLogic : DocumentStorage.Worm;

		switch (storageEnum) {
			case MarkLogic:
				return markLogicClient.getDocumentContent(uri);

			case Worm:
				return wormClient.getDocumentContent(uri);

			default:
				throw new IllegalArgumentException("invalid input");
		}
	}

	@RequestMapping(value = "search", method = GET)
	public Document[] searchDocuments(
			@RequestParam(required = false) String caseId,
			@RequestParam(required = false) String domain,
			@RequestParam(required = false) String filterValues) {
		log.info("searchDocument called: caseId={}, domain={}, filterValues={}", caseId, domain, filterValues);

		if (StringUtils.isNotEmpty(caseId)) {
			return markLogicClient.searchDocuments(caseId);

		} else if (StringUtils.isNotEmpty(domain)) {
			return markLogicClient.searchDocuments(domain, filterValues);

		} else {
			throw new IllegalArgumentException("no caseId or domain supplied to the query");
		}
	}

	@RequestMapping(value = "cases", method = POST)
	public Case newCaseId(@RequestParam String description) {
		// todo createdUser should be populated from SecurityContextHolder once we have spring security
		String createdUser = "anonymous";
		return markLogicClient.newCaseId(createdUser, description);
	}

	@RequestMapping(value = "cases", method = GET)
	public Case[] getAllCases() {
		return markLogicClient.getAllCases();
	}

	@RequestMapping(value = "documents", method = POST)
	public void updateDocument(            
			@RequestParam String uri,
			@RequestParam String caseId,
			@RequestParam DocumentAction action) {
		
		log.info("updateDocument called: uri={}, caseId={}, action={}", uri, caseId, action);

		switch (action) {
		case Tag:                
			markLogicClient.tagDocument(uri, caseId);
			break;

		case UnTag:                
			markLogicClient.unTagDocument(uri, caseId);
			break;

		default:
		}
	}
}
