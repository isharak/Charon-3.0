package org.wso2.charon.core.protocol.endpoints;

import org.wso2.charon.core.attributes.Attribute;
import org.wso2.charon.core.attributes.ComplexAttribute;
import org.wso2.charon.core.attributes.MultiValuedAttribute;
import org.wso2.charon.core.encoder.JSONDecoder;
import org.wso2.charon.core.encoder.JSONEncoder;
import org.wso2.charon.core.exceptions.BadRequestException;
import org.wso2.charon.core.exceptions.CharonException;
import org.wso2.charon.core.exceptions.InternalErrorException;
import org.wso2.charon.core.exceptions.NotFoundException;
import org.wso2.charon.core.extensions.UserManager;
import org.wso2.charon.core.objects.AbstractSCIMObject;
import org.wso2.charon.core.protocol.ResponseCodeConstants;
import org.wso2.charon.core.protocol.SCIMResponse;
import org.wso2.charon.core.schema.SCIMConstants;
import org.wso2.charon.core.schema.SCIMResourceSchemaManager;
import org.wso2.charon.core.schema.SCIMResourceTypeSchema;
import org.wso2.charon.core.utils.CopyUtil;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 *The "ResourceType" schema specifies the metadata about a resource type.
 */
public class ResourceTypeResourceManager extends AbstractResourceManager{

    /**
     * Retrieves a resource type
     *
     * @return SCIM response to be returned.
     */
    @Override
    public SCIMResponse get(String id, UserManager userManager, String attributes, String excludeAttributes) {
        return getResourceType();
    }

    private SCIMResponse getResourceType(){
        JSONEncoder encoder = null;
        try {
            //obtain the json encoder
            encoder = getEncoder();
            //obtain the json decoder
            JSONDecoder decoder = getDecoder();

            // get the service provider config schema
            SCIMResourceTypeSchema schema = SCIMResourceSchemaManager.getInstance().getResourceTypeResourceSchema();
            //create a string in json format for user resource type with relevant values
            String scimUserObjectString = encoder.buildUserResourceTypeJsonBody();
            //create a string in json format for group resource type with relevant values
            String scimGroupObjectString = encoder.buildGroupResourceTypeJsonBody();
            //build the user abstract scim object
            AbstractSCIMObject userResourceTypeObject = (AbstractSCIMObject) decoder.decodeResource(
                    scimUserObjectString, schema, new AbstractSCIMObject());
            //build the group abstract scim object
            AbstractSCIMObject groupResourceTypeObject = (AbstractSCIMObject) decoder.decodeResource(
                    scimGroupObjectString, schema, new AbstractSCIMObject());
            //build the root abstract scim object
            AbstractSCIMObject ResourceTypeObject = buildCombinedResourceType(userResourceTypeObject,
                    groupResourceTypeObject);
            //encode the newly created SCIM Resource Type object.
            String encodedObject;
            Map<String, String> ResponseHeaders = new HashMap<String, String>();

            if (ResourceTypeObject != null) {
                //create a deep copy of the resource type object since we are going to change it.
                AbstractSCIMObject copiedObject = (AbstractSCIMObject) CopyUtil.deepCopy(ResourceTypeObject);
                encodedObject = encoder.encodeSCIMObject(copiedObject);
                //add location header
                ResponseHeaders.put(SCIMConstants.LOCATION_HEADER, getResourceEndpointURL(
                        SCIMConstants.RESOURCE_TYPE_ENDPOINT));
                ResponseHeaders.put(SCIMConstants.CONTENT_TYPE_HEADER, SCIMConstants.APPLICATION_JSON);

            } else {
                String error = "Newly created User resource is null.";
                throw new InternalErrorException(error);
            }
            //put the URI of the resource type object in the response header parameter.
            return new SCIMResponse(ResponseCodeConstants.CODE_OK,
                    encodedObject, ResponseHeaders);
        }
        catch (CharonException e) {
            return AbstractResourceManager.encodeSCIMException(e);
        } catch (BadRequestException e) {
            return AbstractResourceManager.encodeSCIMException(e);
        } catch (InternalErrorException e) {
            return AbstractResourceManager.encodeSCIMException(e);
        } catch (NotFoundException e) {
            return AbstractResourceManager.encodeSCIMException(e);
        }
    }

    @Override
    public SCIMResponse create(String scimObjectString, UserManager userManager, String attributes, String excludeAttributes) {
        String error= "Request is undefined";
        BadRequestException badRequestException = new BadRequestException(error, ResponseCodeConstants.INVALID_PATH);
        return AbstractResourceManager.encodeSCIMException(badRequestException);
    }

    @Override
    public SCIMResponse delete(String id, UserManager userManager) {
        String error= "Request is undefined";
        BadRequestException badRequestException = new BadRequestException(error, ResponseCodeConstants.INVALID_PATH);
        return AbstractResourceManager.encodeSCIMException(badRequestException);
    }

    @Override
    public SCIMResponse listByFilter(String filterString, UserManager userManager, String attributes, String excludeAttributes) throws IOException {
        String error= "Request is undefined";
        BadRequestException badRequestException = new BadRequestException(error, ResponseCodeConstants.INVALID_PATH);
        return AbstractResourceManager.encodeSCIMException(badRequestException);
    }

    @Override
    public SCIMResponse listBySort(String sortBy, String sortOrder, UserManager usermanager, String attributes, String excludeAttributes) {
        String error= "Request is undefined";
        BadRequestException badRequestException = new BadRequestException(error, ResponseCodeConstants.INVALID_PATH);
        return AbstractResourceManager.encodeSCIMException(badRequestException);
    }

    @Override
    public SCIMResponse listWithPagination(int startIndex, int count, UserManager userManager, String attributes, String excludeAttributes) {
        String error= "Request is undefined";
        BadRequestException badRequestException = new BadRequestException(error, ResponseCodeConstants.INVALID_PATH);
        return AbstractResourceManager.encodeSCIMException(badRequestException);
    }

    @Override
    public SCIMResponse list(UserManager userManager, String attributes, String excludeAttributes) {
        String error= "Request is undefined";
        BadRequestException badRequestException = new BadRequestException(error, ResponseCodeConstants.INVALID_PATH);
        return AbstractResourceManager.encodeSCIMException(badRequestException);
    }

    @Override
    public SCIMResponse updateWithPUT(String existingId, String scimObjectString, UserManager userManager, String attributes, String excludeAttributes) {
        String error= "Request is undefined";
        BadRequestException badRequestException = new BadRequestException(error, ResponseCodeConstants.INVALID_PATH);
        return AbstractResourceManager.encodeSCIMException(badRequestException);
    }

    @Override
    public SCIMResponse updateWithPATCH(String existingId, String scimObjectString, UserManager userManager, String attributes, String excludeAttributes) {
        String error= "Request is undefined";
        BadRequestException badRequestException = new BadRequestException(error, ResponseCodeConstants.INVALID_PATH);
        return AbstractResourceManager.encodeSCIMException(badRequestException);
    }

    /**
     * This combines the user and group resource type AbstractSCIMObjects and build a
     * one root AbstractSCIMObjects
     *
     * @param userObject
     * @param groupObject
     * @return
     * @throws CharonException
     */
    private AbstractSCIMObject buildCombinedResourceType(AbstractSCIMObject userObject, AbstractSCIMObject groupObject)
            throws CharonException {
        Map<String,Attribute> userObjectAttributeList = userObject.getAttributeList();
        Map<String,Attribute> groupObjectAttributeList = groupObject.getAttributeList();

        AbstractSCIMObject rootObject = new AbstractSCIMObject();
        MultiValuedAttribute multiValuedAttribute = new MultiValuedAttribute(
                SCIMConstants.ResourceTypeSchemaConstants.ResourceType);
        ComplexAttribute userComplexAttribute = new ComplexAttribute();

        for(Attribute attribute : userObjectAttributeList.values()){
            userComplexAttribute.setSubAttribute(attribute);
        }
        multiValuedAttribute.setAttributeValue(userComplexAttribute);
        ComplexAttribute groupComplexAttribute = new ComplexAttribute();
        for(Attribute attribute : groupObjectAttributeList.values()){
            groupComplexAttribute.setSubAttribute(attribute);
        }
        multiValuedAttribute.setAttributeValue(groupComplexAttribute);
        rootObject.setAttribute(multiValuedAttribute);
        rootObject.setSchema(SCIMConstants.RESOURCE_TYPE_SCHEMA_URI);
        return rootObject;
    }
}