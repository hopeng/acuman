package com.acuman;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

@RestController
@RequestMapping("/v1")
public class AcuManagerController {
	private static final Logger log = LoggerFactory.getLogger(AcuManagerController.class);
	private static final String MEDIA_TYPE_XML_UTF_8 = "application/xml; charset=UTF-8";
	private static final String MEDIA_TYPE_JSON_UTF_8 = "application/json; charset=UTF-8";

	@Autowired
	private AcuService acuService;

	@RequestMapping(value = "patients", method = GET, produces = MEDIA_TYPE_JSON_UTF_8)
	public String findPatients() {
		log.info("findPatients called");

		return null;
	}


}
