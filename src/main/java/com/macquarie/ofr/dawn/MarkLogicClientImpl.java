package com.macquarie.ofr.dawn;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.macquarie.ofr.dawn.domain.Case;
import com.macquarie.ofr.dawn.domain.Document;
import com.macquarie.ofr.mds.ms.marklogic.MarklogicConnectionFactory;
import com.marklogic.client.DatabaseClient;
import com.marklogic.client.document.*;
import com.marklogic.client.document.DocumentMetadataPatchBuilder.PatchHandle;
import com.marklogic.client.io.*;
import com.marklogic.client.io.DocumentMetadataHandle.DocumentCollections;
import com.marklogic.client.query.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class MarkLogicClientImpl implements MarkLogicClient {

	private static AtomicInteger caseGenerator = new AtomicInteger(100);
	private final static String WORM_CASE_COLLECTION_PREFIX = "/worm/";


	@Autowired
	private MarklogicConnectionFactory marklogicConnectionFactory;

	private final Logger log = LoggerFactory.getLogger(this.getClass().getName());
	private DatabaseClient client;

	private DatabaseClient getClient() {
		if (client == null)
			client = marklogicConnectionFactory.getInstance();

		return client;
	}

	@Override
	public String getDocumentContent(String uri) {
		// read the URI from MarkLogic.

		try {
			XMLDocumentManager docMgr = getClient().newXMLDocumentManager();

			StringHandle handle = new StringHandle();
			docMgr.read(uri, handle);
			String document = handle.get();

			log.debug("Document content is\n {}", document);
			return document;

		} catch (com.marklogic.client.ResourceNotFoundException re) {
			log.error("Document not found in MarkLogic");
			throw re;
		} catch (Exception e) {
			log.error("Failed to read MarkLogic.");
			throw e; // ?
		}
	}

	@Override
	public Document[] searchDocuments(String caseId) {
		String collection = getCaseCollection(caseId);

		// create a handle for the search results
		List<Document> docs = new ArrayList<Document>();

		try {

			QueryManager queryMgr = getClient().newQueryManager();

			SearchHandle resultsHandle = new SearchHandle();

			// create a search definition
			StructuredQueryBuilder qb = new StructuredQueryBuilder();
			StructuredQueryDefinition querydef = qb.collection(collection);


			// run the search
			querydef.setOptionsName("cases");
			queryMgr.search(querydef,resultsHandle);

			MatchDocumentSummary[] results = resultsHandle.getMatchResults();

			log.info("Found {} results for {}", results.length, caseId);

			for (MatchDocumentSummary match : results) {
				Document d = new Document();

				d.setUri(match.getUri());

				log.info("Extracted has results: {}", match.getExtracted().hasNext());;
				ExtractedItem extract = match.getExtracted().next();

				if(extract == null) {
					log.error("No metadata found for {}",d.getUri());
					continue;
				}

				DOMHandle handle = extract.get(new DOMHandle());
				org.w3c.dom.Document xml = handle.get();

				d.setDomain(xml.getElementsByTagNameNS("*", "name").item(0).getTextContent());
				d.setId(xml.getElementsByTagNameNS("*", "id").item(0).getTextContent());
				d.setIdType(xml.getElementsByTagNameNS("*", "id").item(0).getAttributes().item(0).getTextContent());
				d.setSource(xml.getElementsByTagNameNS("*", "source").item(0).getTextContent());

				docs.add(d);

			}



		} catch (Exception e) {
			log.error("Failed to read MarkLogic.");
			throw e; // ?
		}

		Document[] result = docs.toArray(new Document[0]);
		addMissingDynamicFields(result);
		return result;
	}

	public void addMissingDynamicFields(Document[] documents) {
		Set<String> allFields = new HashSet<>();
		for (Document d : documents) {
			allFields.addAll(d.getDynamicFields().keySet());
		}

		for (Document d : documents) {
			for (String key : allFields) {
				d.getDynamicFields().putIfAbsent(key, "");
			}
		}
	}

	@Override
	public Document[] searchDocuments(String domainCollection, String filterValues) {

		long page = 1;

		String searchOptions = domainCollection.replace('/', '_').toLowerCase().substring(1);


		// create a handle for the search results
		List<Document> docs = new ArrayList<Document>();

		try {

			QueryManager queryMgr = getClient().newQueryManager();

			SearchHandle resultsHandle = new SearchHandle();

			// create a search definition

			StringQueryDefinition querydef = queryMgr.newStringDefinition(searchOptions);
			querydef.setCollections(domainCollection);
			querydef.setCriteria(filterValues);

			log.info("Search criteria is {} and collection is {}", filterValues, domainCollection);

			queryMgr.search(querydef,resultsHandle,(page-1)*10+1);

			MatchDocumentSummary[] results = resultsHandle.getMatchResults();

			log.info("Found {} results for collection {} and criteria {}", results.length, domainCollection, filterValues);

			for (MatchDocumentSummary match : results) {
				Document d = new Document();

				d.setUri(match.getUri());

				log.info("Extracted has results: {}", match.getExtracted().hasNext());;
				ExtractedResult extract = match.getExtracted();

				if(extract == null) {
					log.error("No metadata found for {}",d.getUri());
					continue;
				}


				d.setDomain(getNodeValue(extract.next()).get("name"));

				Map<String,String> id = getNodeValue(extract.next());
				d.setId(id.get("id"));
				d.setIdType(id.get("idScheme"));

				d.setSource(getNodeValue(extract.next()).get("source"));

				Map<String, String> map = new HashMap<String, String>();

				while(extract.hasNext()) {
					getNodeValue(extract.next(),map);
				}

				//come back and solve attributes for dynamic fields...
				d.setDynamicFields(map);

				docs.add(d);

			}



		} catch (Exception e) {
			log.error("Failed to read MarkLogic.");
			throw e; // ?
		}


		Document[] result = docs.toArray(new Document[0]);
		addMissingDynamicFields(result);
		return result;
	}


	private Map<String, String> getNodeValue(ExtractedItem extract) {

		Map<String, String> map = new HashMap<String, String>();
		getNodeValue(extract, map);

		return getNodeValue(extract, map);
	}

	private Map<String, String> getNodeValue(ExtractedItem extract, Map<String, String> map) {

		DOMHandle handle = extract.get(new DOMHandle());
		org.w3c.dom.Document xml = handle.get();
		org.w3c.dom.Element e = xml.getDocumentElement();
		String nodeValue = xml.getDocumentElement().getTextContent();

		log.debug("{} node value is: {}", e.getLocalName(),nodeValue);

		map.put(e.getLocalName(), nodeValue);

		getAttributeValues(e, map);

		return map;
	}

	private Map<String, String> getAttributeValues(org.w3c.dom.Element e, Map<String, String> map) {

		NamedNodeMap nnm = e.getAttributes();
		int length = nnm.getLength();

		for (int i=0; i<length;i++) {
			Node n = nnm.item(i);

			String localName = n.getLocalName();
			if(localName =="xmlns" || localName.startsWith("zdef")) {
				continue;
			}


			log.debug("{} node {} attribute value is: {}", e.getLocalName(),localName,n.getNodeValue());
			map.put(localName, n.getNodeValue());

		}

		return map;
	}






	@Override
	public Case newCaseId(String createdUser, String description) {

		int id = caseGenerator.getAndIncrement();
		String caseId = "CASE-"+id;

		while(caseExists(caseId)) {
			log.debug("CASE-{} already exists, incrementing...",caseId);
			id = caseGenerator.getAndIncrement();
			caseId = "CASE-"+id;
		}

		Case c = new Case();

		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
		Date date = new Date();

		c.setCaseId(caseId);
		c.setCreatedUser(createdUser);
		c.setDescription(description);
		c.setCreatedDate(dateFormat.format(date));

		try {
			JSONDocumentManager docMgr = getClient().newJSONDocumentManager();

			//content
			JacksonHandle content = new JacksonHandle();
			ObjectMapper mapper = new ObjectMapper();
			JsonNode node = mapper.valueToTree(c);
			content.set(node);

			docMgr.write(getCaseCollection(caseId), content);


		} catch (Exception e) {
			log.error("Failed to read MarkLogic.");
			throw e; // ?
		}

		return c;
	}

	@Override
	public Case[] getAllCases() {
		// create a handle for the search results
		List<Case> cases = new ArrayList<Case>();

		try {

			JSONDocumentManager docMgr = getClient().newJSONDocumentManager();

			// create a search definition
			StructuredQueryBuilder qb = new StructuredQueryBuilder();
			StructuredQueryDefinition querydef = qb.directory(true, WORM_CASE_COLLECTION_PREFIX);

			// run the search
			DocumentPage resultsPage = docMgr.search(querydef, 1);

			for (DocumentRecord record : resultsPage) {

				JacksonHandle content = new JacksonHandle();
				record.getContent(content);

				JsonNode node = content.get();

				ObjectMapper mapper = content.getMapper();
				Case c = mapper.treeToValue(node, Case.class);

				cases.add(c);
			}


		} catch (JsonProcessingException e) {
			log.error("Error reading case from MarkLogic - {}", e.getMessage());
		} catch (Exception e) {
			log.error("Failed to read MarkLogic.");
			throw e; // ?
		}

		return cases.toArray(new Case[0]);
	}

	private String getCaseCollection(String caseId) {
		return WORM_CASE_COLLECTION_PREFIX + caseId;
	}

	private boolean caseExists(String caseId) {
		String collection = getCaseCollection(caseId);

		try {
			JSONDocumentManager docMgr = getClient().newJSONDocumentManager();

			DocumentDescriptor descriptor = docMgr.exists(collection);

			if(descriptor == null) {
				return false;
			}
			else {
				return true;
			}


		} catch (Exception e) {
			log.error("Failed to read MarkLogic.");
			throw e; // ?
		}
	}


	@Override
	public void tagDocument(String uri, String caseId) {
		String collection = getCaseCollection(caseId);

		try {
			XMLDocumentManager docMgr = getClient().newXMLDocumentManager();

			DocumentMetadataHandle metadataHandle = new DocumentMetadataHandle();
			docMgr.readMetadata(uri, metadataHandle);


			DocumentCollections collections = metadataHandle.getCollections();
			if(collections.contains(collection)) {
				log.info("Document {} was already in {}",uri,caseId);
				return;
			}

			DocumentMetadataPatchBuilder metadataBuilder = docMgr.newPatchBuilder(Format.XML);
			metadataBuilder.addCollection(collection);

			PatchHandle handle = metadataBuilder.build();
			docMgr.patch(uri, handle);



		} catch(com.marklogic.client.ResourceNotFoundException re) {
			log.error("Document not found in MarkLogic");
			throw re;
		} catch (Exception e) {
			log.error("Failed to read MarkLogic.");
			throw e; // ?
		}
	}

	@Override
	public void unTagDocument(String uri, String caseId) {
		String collection = getCaseCollection(caseId);

		try {
			XMLDocumentManager docMgr = getClient().newXMLDocumentManager();

			DocumentMetadataHandle metadataHandle = new DocumentMetadataHandle();
			docMgr.readMetadata(uri, metadataHandle);


			DocumentCollections collections = metadataHandle.getCollections();
			if(!collections.contains(collection)) {
				log.info("Unable to untag document - Document {} was not in {}",uri,caseId);
				return;
			}

			DocumentMetadataPatchBuilder metadataBuilder = docMgr.newPatchBuilder(Format.XML);
			metadataBuilder.deleteCollection(collection);
			PatchHandle handle = metadataBuilder.build();
			docMgr.patch(uri, handle);



		} catch(com.marklogic.client.ResourceNotFoundException re) {
			log.error("Document not found in MarkLogic");
			throw re;
		} catch (Exception e) {
			log.error("Failed to update MarkLogic.");
			throw e; // ?
		}
	}

}
