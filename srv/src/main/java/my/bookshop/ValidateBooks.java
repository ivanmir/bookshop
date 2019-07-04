package my.company;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sap.cloud.sdk.service.prov.api.EntityData;
import com.sap.cloud.sdk.service.prov.api.EntityDataBuilder;
import com.sap.cloud.sdk.service.prov.api.ExtensionHelper;
import com.sap.cloud.sdk.service.prov.api.MessageContainer;
import com.sap.cloud.sdk.service.prov.api.annotations.BeforeCreate;
import com.sap.cloud.sdk.service.prov.api.annotations.BeforeUpdate;
import com.sap.cloud.sdk.service.prov.api.annotations.Function;
import com.sap.cloud.sdk.service.prov.api.exits.BeforeCreateResponse;
import com.sap.cloud.sdk.service.prov.api.exits.BeforeUpdateResponse;
import com.sap.cloud.sdk.service.prov.api.operations.Query;
import com.sap.cloud.sdk.service.prov.api.request.CreateRequest;
import com.sap.cloud.sdk.service.prov.api.request.OperationRequest;
import com.sap.cloud.sdk.service.prov.api.request.QueryRequest;
import com.sap.cloud.sdk.service.prov.api.request.UpdateRequest;
import com.sap.cloud.sdk.service.prov.api.response.ErrorResponse;
import com.sap.cloud.sdk.service.prov.api.response.OperationResponse;
import com.sap.cloud.sdk.service.prov.api.response.QueryResponse;
import com.sap.cloud.sdk.service.prov.v2.rt.core.EntityDataV2;
import com.sap.gateway.core.api.enums.HttpStatus;

public class ValidateBooks {

	Logger logger = LoggerFactory.getLogger(ValidateBooks.class);

	@BeforeCreate(entity = "Books", serviceName = "CatalogService")
	public BeforeCreateResponse beforeCreate(CreateRequest req, ExtensionHelper eh) {
		if (!isISBNValid(req.getData(), req.getMessageContainer())) {
			return BeforeCreateResponse.setError(ErrorResponse.getBuilder().setMessage("Validation before Create Error")
					.addContainerMessages().setStatusCode(HttpStatus.CONFLICT.getStatusCode()).response());
		} else {
			return BeforeCreateResponse.setSuccess().response();
		}
	}

	@BeforeUpdate(entity = "Books", serviceName = "CatalogService")
	public BeforeUpdateResponse beforeUpdate(UpdateRequest req, ExtensionHelper eh) {
		if (!isISBNValid(req.getData(), req.getMessageContainer())) {
			return BeforeUpdateResponse.setError(ErrorResponse.getBuilder().setMessage("Validation before Update error")
					.addContainerMessages().setStatusCode(HttpStatus.CONFLICT.getStatusCode()).response());
		} else {
			return BeforeUpdateResponse.setSuccess().response();
		}
	}

	/*
	 * Validate the check sum of the ISBN
	 */
	protected boolean isISBNValid(EntityData data, MessageContainer messageContainer) {
		boolean isValid = true;

		String isbn = (String) data.getElementValue("isbn");
		isbn = isbn.replaceAll("-", "");

		if (isbn.length() == 10) {
			// Check 10 digits ISBN
			int checkSum = 0;
			for (int i = 1; i <= 10; i++) {
				checkSum += i * Integer.parseInt(isbn.substring(i - 1, i));
			}

			if (checkSum % 11 != 0) {
				messageContainer.addErrorMessage("ISBN Number with 10 digits not valid", "Book");
				isValid = false;
			}
		} else if (isbn.length() == 13) {
			// Check 13 digits ISBN
			int checkSum = 0;
			for (int i = 1; i < 13; i++) {
				if ((i - 1) % 2 == 0) {
					checkSum += Integer.parseInt(isbn.substring(i - 1, i));
				} else {
					checkSum += Integer.parseInt(isbn.substring(i - 1, i)) * 3;
				}
			}

			if (Integer.parseInt(isbn.substring(12)) != 10 - (checkSum % 10)) {
				messageContainer.addErrorMessage("ISBN Number with 13 digits not valid", "Book");
				isValid = false;
			}
		} else {
			messageContainer.addErrorMessage("ISBN Number does not match 10 / 13 character length", "Book");
			isValid = false;
		}

		return isValid;
	}

	@Function(Name = "findBooks2", serviceName = "CatalogService")
	public OperationResponse getSupplierOfOrder(OperationRequest functionRequest, ExtensionHelper extensionHelper) {
		OperationResponse opResponse;
		try {

			EntityDataBuilder builder1 = EntityData.getBuilder();
			builder1.addElement("ID", 1);
			builder1.addElement("title", "Dummy Title 1");
			builder1.addElement("author_ID", 1);
			builder1.addElement("stock", 100);
			builder1.addElement("isbn", "DXXX-XXX-XXXX");
			EntityData dummyEntity1 = builder1.buildEntityData("Books");

			EntityDataBuilder builder2 = EntityData.getBuilder();
			builder2.addElement("ID", 2);
			builder2.addElement("title", "Dummy Title 2");
			builder2.addElement("author_ID", 1);
			builder2.addElement("stock", 100);
			builder2.addElement("isbn", "DXXX-XXX-XXXX");
			EntityData dummyEntity2 = builder2.buildEntityData("Books");

			final List<EntityData> dummyList = Arrays.asList(dummyEntity1, dummyEntity2);

			opResponse = OperationResponse.setSuccess().setEntityData(dummyList).response();

		} catch (Exception e) {
			logger.error("Error in GetSupplier: " + e.getMessage());
			// Return an instance of OperationResponse containing the error in
			// case of failure
			ErrorResponse errorResponse = ErrorResponse.getBuilder().setMessage(e.getMessage()).setCause(e).response();

			opResponse = OperationResponse.setError(errorResponse);
		}
		return opResponse;
	}

	@Function(Name = "myAuthors", serviceName = "CatalogService")
	public OperationResponse getMyAuthors(OperationRequest functionRequest, ExtensionHelper extensionHelper) {

		List<EntityData> result = getEntityList();
		return OperationResponse.setSuccess().setEntityData(result).response();
	}

	private List<EntityData> getEntityList() {
		List<EntityData> myEntityList = new ArrayList<EntityData>();
		myEntityList.add(createEntity(900, "My Dummy Author 1", "Authors"));
		myEntityList.add(createEntity(910, "My Dummy Author 2", "Authors"));
		myEntityList.add(createEntity(920, "My Dummy Author 3", "Authors"));
		return myEntityList;
	}

	private EntityData createEntity(Integer entityId, String name, String entityType) {
		EntityData anEntity = EntityData.getBuilder()
			.addKeyElement("ID", entityId)
			.addElement("name", name)
			.buildEntityData(entityType);
		return anEntity;
	}

}
