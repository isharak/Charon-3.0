package org.wso2.charon.core.schema;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Attr;
import org.wso2.charon.core.attributes.*;
import org.wso2.charon.core.exceptions.BadRequestException;
import org.wso2.charon.core.exceptions.CharonException;
import org.wso2.charon.core.objects.AbstractSCIMObject;
import org.wso2.charon.core.protocol.ResponseCodeConstants;

import java.util.*;

public abstract class AbstractValidator {

    private static Log logger= LogFactory.getLog(AbstractValidator.class);

    /**
     * Validate SCIMObject for required attributes given the object and the corresponding schema.
     *
     * @param scimObject
     * @param resourceSchema
     */
    public static void validateSCIMObjectForRequiredAttributes(AbstractSCIMObject scimObject,
                                                               ResourceTypeSchema resourceSchema)
            throws BadRequestException, CharonException {
        //get attributes from schema.
        List<AttributeSchema> attributeSchemaList = resourceSchema.getAttributesList();
        //get attribute list from scim object.
        Map<String, Attribute> attributeList = scimObject.getAttributeList();
        for (AttributeSchema attributeSchema : attributeSchemaList) {
            //check for required attributes.
            if (attributeSchema.getRequired()) {
                if (!attributeList.containsKey(attributeSchema.getName())) {
                    String error = "Required attribute " + attributeSchema.getName() + " is missing in the SCIM Object.";
                    throw new BadRequestException(error,ResponseCodeConstants.INVALID_VALUE);
                }
            }
            //check for required sub attributes.
            AbstractAttribute attribute = (AbstractAttribute) attributeList.get(attributeSchema.getName());
            if (attribute != null) {
                List<SCIMAttributeSchema> subAttributesSchemaList =
                        ((SCIMAttributeSchema) attributeSchema).getSubAttributes();

                if (subAttributesSchemaList != null) {
                    for (SCIMAttributeSchema subAttributeSchema : subAttributesSchemaList) {
                        if (subAttributeSchema.getRequired()) {

                            if (attribute instanceof ComplexAttribute) {
                                if (attribute.getSubAttribute(subAttributeSchema.getName()) == null) {
                                    String error = "Required sub attribute: " + subAttributeSchema.getName()
                                            + " is missing in the SCIM Attribute: " + attribute.getName();
                                    throw new BadRequestException(error,ResponseCodeConstants.INVALID_VALUE);
                                }
                            } else if (attribute instanceof MultiValuedAttribute) {
                                List<Attribute> values =
                                        ((MultiValuedAttribute) attribute).getAttributeValues();
                                for (Attribute value : values) {
                                    if (value instanceof ComplexAttribute) {
                                        if (value.getSubAttribute(subAttributeSchema.getName()) == null) {
                                            String error = "Required sub attribute: " + subAttributeSchema.getName()
                                                    + ", is missing in the SCIM Attribute: " + attribute.getName();
                                            throw new BadRequestException(error,ResponseCodeConstants.INVALID_VALUE);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    /**
     * Validate SCIMObject for schema list
     *
     * @param scimObject
     * @param resourceSchema
     */
    public static void validateSchemaList(AbstractSCIMObject scimObject,
                                          SCIMResourceTypeSchema resourceSchema) throws CharonException {
        //get resource schema list
        List<String> resourceSchemaList = resourceSchema.getSchemasList();
        //get the scim object schema list
        List<String> objectSchemaList = scimObject.getSchemaList();
        for (String schema : resourceSchemaList) {
            //check for schema.
            if (!objectSchemaList.contains(schema)) {
               throw new CharonException("Not all schemas are set");
            }
        }
    }

    /**
     *Check for readonlyAttributes and remove them if they have been modified. - (create method)
     *
     * @param scimObject
     * @param resourceSchema
     * @throws CharonException
     */
    public static void removeAnyReadOnlyAttributes(AbstractSCIMObject scimObject,
                                                   SCIMResourceTypeSchema resourceSchema) throws CharonException {
        //No need to check for immutable as immutable attributes can be defined at resource creation

        //get attributes from schema.
        List<AttributeSchema> attributeSchemaList = resourceSchema.getAttributesList();
        //get attribute list from scim object.
        Map<String, Attribute> attributeList = scimObject.getAttributeList();
        for (AttributeSchema attributeSchema : attributeSchemaList) {
            //check for read-only attributes.
            if (attributeSchema.getMutability()==SCIMDefinitions.Mutability.READ_ONLY) {
                if (attributeList.containsKey(attributeSchema.getName())) {
                    String error = "Read only attribute: " + attributeSchema.getName() +
                            " is set from consumer in the SCIM Object. " + "Removing it.";
                    logger.debug(error);
                    scimObject.deleteAttribute(attributeSchema.getName());
                }
            }
            //check for readonly sub attributes.
            AbstractAttribute attribute = (AbstractAttribute) attributeList.get(attributeSchema.getName());
            if (attribute != null) {
                List<SCIMAttributeSchema> subAttributesSchemaList =
                        ((SCIMAttributeSchema) attributeSchema).getSubAttributes();

                if (subAttributesSchemaList != null && !subAttributesSchemaList.isEmpty()) {
                    for (SCIMAttributeSchema subAttributeSchema : subAttributesSchemaList) {
                        if (subAttributeSchema.getMutability()==SCIMDefinitions.Mutability.READ_ONLY) {
                            if (attribute instanceof ComplexAttribute) {
                                if (attribute.getSubAttribute(subAttributeSchema.getName()) != null) {
                                    String error = "Readonly sub attribute: " + subAttributeSchema.getName()
                                            + " is set in the SCIM Attribute: " + attribute.getName() +
                                            ". Removing it.";
                                    ((ComplexAttribute) attribute).removeSubAttribute(subAttributeSchema.getName());
                                }
                            } else if (attribute instanceof MultiValuedAttribute) {
                                List<Attribute> values =
                                        ((MultiValuedAttribute) attribute).getAttributeValues();
                                for (Attribute value : values) {
                                    if (value instanceof ComplexAttribute) {
                                        if (value.getSubAttribute(subAttributeSchema.getName()) != null) {
                                            String error = "Readonly sub attribute: " + subAttributeSchema.getName()
                                                    + " is set in the SCIM Attribute: " + attribute.getName() +
                                                    ". Removing it.";
                                            ((ComplexAttribute) value).removeSubAttribute(subAttributeSchema.getName());

                                        }
                                    }
                                }
                            }
                        }
                    }
                }

            }
        }

    }

    /**
     * This method is to remove any defined and requested attributes.
     *
     * @param scimObject
     * @param requestedAttributes
     * @param requestedExcludingAttributes
     */
    public static void removeAttributesOnReturn(AbstractSCIMObject scimObject, String requestedAttributes,
                                                String requestedExcludingAttributes) {
        List<String> requestedAttributesList = null;
        List<String> requestedExcludingAttributesList = null;

        if(requestedAttributes!=null ){
            //make a list from the comma separated requestedAttributes
            requestedAttributesList = Arrays.asList(requestedAttributes.split(","));
        }
        if(requestedExcludingAttributes!=null){
            //make a list from the comma separated requestedExcludingAttributes
            requestedExcludingAttributesList = Arrays.asList(requestedExcludingAttributes.split(","));
        }
        Map<String, Attribute> attributeList = scimObject.getAttributeList();
        ArrayList<Attribute> attributeTemporyList= new ArrayList<Attribute>();
        for (Attribute attribute : attributeList.values()) {
            attributeTemporyList.add(attribute);
        }
        for(Attribute attribute : attributeTemporyList){
            //check for never/request attributes.
            if (attribute.getReturned().equals(SCIMDefinitions.Returned.NEVER)) {
                scimObject.deleteAttribute(attribute.getName());
            }
            //if the returned property is request, need to check whether is it specifically requested by the user.
            // If so return it.
           if(requestedAttributes ==null && requestedExcludingAttributes == null){
                 if (attribute.getReturned().equals(SCIMDefinitions.Returned.REQUEST)){
                        scimObject.deleteAttribute(attribute.getName());
                }
            }
            else{
                //A request should only contains either attributes or exclude attribute params. Not the both
                if(requestedAttributes !=null){
                    //if attributes are set, delete all the request and default attributes
                    //and add only the requested attributes
                    if ((attribute.getReturned().equals(SCIMDefinitions.Returned.DEFAULT)
                            || attribute.getReturned().equals(SCIMDefinitions.Returned.REQUEST))
                            && (!requestedAttributesList.contains(attribute.getName())
                            && !isSubAttributeExistsInList(requestedAttributesList,attribute))){
                        scimObject.deleteAttribute(attribute.getName());
                    }
                }
                else if(requestedExcludingAttributes !=null){
                    //removing attributes which has returned as request. This is because no request is made
                    if (attribute.getReturned().equals(SCIMDefinitions.Returned.REQUEST)) {
                        scimObject.deleteAttribute(attribute.getName());
                    }
                    //if exclude attribute is set, set of exclude attributes need to be
                    // removed from the default set of attributes
                    if ((attribute.getReturned().equals(SCIMDefinitions.Returned.DEFAULT))
                            && requestedExcludingAttributesList.contains(attribute.getName())){
                        scimObject.deleteAttribute(attribute.getName());
                    }
                }
            }
            // If the Returned type ALWAYS : no need to check and it will be not affected by
            // requestedExcludingAttributes parameter

            //check the same for sub attributes
            if(attribute.getType().equals(SCIMDefinitions.DataType.COMPLEX)){
                if(attribute.getMultiValued()){
                    List<Attribute> valuesList = ((MultiValuedAttribute)attribute).getAttributeValues();

                    for (Attribute subAttribute : valuesList) {
                        Map<String,Attribute> valuesSubAttributeList=((ComplexAttribute)subAttribute).getSubAttributesList();
                        ArrayList<Attribute> valuesSubAttributeTemporyList= new ArrayList<Attribute>();
                        //as we are deleting the attributes form the list, list size will change,
                        //hence need to traverse on a copy
                        for (Attribute subSimpleAttribute : valuesSubAttributeList.values()) {
                            valuesSubAttributeTemporyList.add(subSimpleAttribute);
                        }
                        for(Attribute subSimpleAttribute : valuesSubAttributeTemporyList){
                            if(subSimpleAttribute.getReturned().equals(SCIMDefinitions.Returned.NEVER)){
                                scimObject.deleteValuesSubAttribute(attribute.getName(),
                                        subAttribute.getName(),subSimpleAttribute.getName());
                            }
                            if(requestedAttributes ==null && requestedExcludingAttributes == null){
                                if (attribute.getReturned().equals(SCIMDefinitions.Returned.REQUEST)){
                                    scimObject.deleteValuesSubAttribute(attribute.getName(),
                                            subAttribute.getName(), subSimpleAttribute.getName());
                                }
                            }
                            else{
                                //A request should only contains either attributes or exclude attribute params. Not the both
                                if(requestedAttributes !=null){
                                    //if attributes are set, delete all the request and default attributes
                                    // and add only the requested attributes
                                    if ((subSimpleAttribute.getReturned().equals(SCIMDefinitions.Returned.DEFAULT)
                                            || subSimpleAttribute.getReturned().equals(SCIMDefinitions.Returned.REQUEST))
                                            && (!requestedAttributesList.contains(
                                            attribute.getName()+"."+subSimpleAttribute.getName()) &&
                                            !requestedAttributesList.contains(attribute.getName()))){
                                        scimObject.deleteValuesSubAttribute(attribute.getName(),
                                                subAttribute.getName(), subSimpleAttribute.getName());
                                    }
                                }
                                else if(requestedExcludingAttributes !=null){
                                    //removing attributes which has returned as request. This is because no request is made
                                    if (subSimpleAttribute.getReturned().equals(SCIMDefinitions.Returned.REQUEST)) {
                                        scimObject.deleteValuesSubAttribute(attribute.getName(),
                                                subAttribute.getName(), subSimpleAttribute.getName());
                                    }
                                    //if exclude attribute is set, set of exclude attributes need to be
                                    // removed from the default set of attributes
                                    if ((subSimpleAttribute.getReturned().equals(SCIMDefinitions.Returned.DEFAULT))
                                            && requestedExcludingAttributesList.contains(
                                            attribute.getName()+"."+subSimpleAttribute.getName())){
                                        System.out.println(subAttribute.getName()+"-"+subSimpleAttribute.getName());
                                        scimObject.deleteValuesSubAttribute(attribute.getName(),
                                                subAttribute.getName(),subSimpleAttribute.getName());
                                    }
                                }
                            }
                        }
                    }
                }
                else{
                    Map<String, Attribute> subAttributeList = ((ComplexAttribute)attribute).getSubAttributesList();
                    ArrayList<Attribute> subAttributeTemporyList= new ArrayList<Attribute>();
                    for (Attribute subAttribute : subAttributeList.values()) {
                        subAttributeTemporyList.add(subAttribute);
                    }
                    for(Attribute subAttribute : subAttributeTemporyList){
                        //check for never/request attributes.
                        if (subAttribute.getReturned().equals(SCIMDefinitions.Returned.NEVER)) {
                            scimObject.deleteSubAttribute(attribute.getName(),subAttribute.getName());
                        }
                        //if the returned property is request, need to check whether is it specifically requested by the user.
                        // If so return it.
                        if(requestedAttributes ==null && requestedExcludingAttributes == null){
                            if (subAttribute.getReturned().equals(SCIMDefinitions.Returned.REQUEST)){
                                scimObject.deleteSubAttribute(attribute.getName(),subAttribute.getName());
                            }
                        }
                        else{
                            //A request should only contains either attributes or exclude attribute params. Not the both
                            if(requestedAttributes !=null){
                                //if attributes are set, delete all the request and default attributes
                                // and add only the requested attributes
                                if ((subAttribute.getReturned().equals(SCIMDefinitions.Returned.DEFAULT)
                                        || subAttribute.getReturned().equals(SCIMDefinitions.Returned.REQUEST))
                                        && (!requestedAttributesList.contains(
                                                attribute.getName()+"."+subAttribute.getName()) &&
                                        !requestedAttributesList.contains(attribute.getName()))){
                                    scimObject.deleteSubAttribute(attribute.getName(),subAttribute.getName());
                                }
                            }
                            else if(requestedExcludingAttributes !=null){
                                //removing attributes which has returned as request. This is because no request is made
                                if (subAttribute.getReturned().equals(SCIMDefinitions.Returned.REQUEST)) {
                                    scimObject.deleteSubAttribute(attribute.getName(),subAttribute.getName());
                                }
                                //if exclude attribute is set, set of exclude attributes need to be
                                // removed from the default set of attributes
                                if ((subAttribute.getReturned().equals(SCIMDefinitions.Returned.DEFAULT))
                                        && requestedExcludingAttributesList.contains(
                                                attribute.getName()+"."+subAttribute.getName())){
                                    scimObject.deleteSubAttribute(attribute.getName(),subAttribute.getName());
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * This checks whether, within the 'requestedAttributes', is there a sub attribute of the 'attribute'.
     * If so we should not delete the 'attribute'
     *
     * @param requestedAttributes
     * @param attribute
     * @return boolean
     */
    private static boolean isSubAttributeExistsInList(List<String> requestedAttributes, Attribute attribute) {
        ArrayList<Attribute> subAttributes = null;
        if(attribute instanceof MultiValuedAttribute){
            subAttributes = (ArrayList<Attribute>)
                    ((MultiValuedAttribute)attribute).getAttributeValues();
            if(subAttributes != null){
                for(Attribute subAttribute : subAttributes){
                    ArrayList<Attribute> subSimpleAttributes =new ArrayList<Attribute>((
                            (ComplexAttribute)subAttribute).getSubAttributesList().values());
                    for(Attribute subSimpleAttribute : subSimpleAttributes){
                        if(requestedAttributes.contains(attribute.getName()+"."+subSimpleAttribute.getName())){
                            return true;
                        }
                    }

                }
            }

        }
        else if(attribute instanceof ComplexAttribute){
            //complex attributes have sub attribute map, hence need conversion to arraylist
            subAttributes = new ArrayList<Attribute>
                    (((HashMap)(((ComplexAttribute)attribute).getSubAttributesList())).values());
            if(subAttributes != null){
                for(Attribute subAttribute : subAttributes){
                    if(requestedAttributes.contains(attribute.getName()+"."+subAttribute.getName())){
                        return true;
                    }
                }
            }
            else{
                return false;
            }
        }
        return false;
    }


    protected static AbstractSCIMObject checkIfReadOnlyAndImmutableAttributesModified(
            AbstractSCIMObject oldObject, AbstractSCIMObject newObject, SCIMResourceTypeSchema resourceSchema)
            throws BadRequestException, CharonException {

        //get attributes from schema.
        List<AttributeSchema> attributeSchemaList = resourceSchema.getAttributesList();
        //get attribute list from old scim object.
        Map<String, Attribute> oldAttributeList = oldObject.getAttributeList();
        //get attribute list from new scim object.
        Map<String, Attribute> newAttributeList = newObject.getAttributeList();

        for (AttributeSchema attributeSchema : attributeSchemaList) {
            //check for immutable attributes
            if(attributeSchema.getMutability()== SCIMDefinitions.Mutability.IMMUTABLE){

                if((oldAttributeList.containsKey(attributeSchema.getName())
                        && newAttributeList.containsKey(attributeSchema.getName())) &&
                        ((SimpleAttribute)oldAttributeList.get(attributeSchema.getName())).getValue()!=
                                ((SimpleAttribute)newAttributeList.get(attributeSchema.getName())).getValue()){
                    String error ="Immutable value is trying to be set.";
                    throw new BadRequestException(ResponseCodeConstants.MUTABILITY);
                }
                else if ((oldAttributeList.containsKey(attributeSchema.getName())
                        && !newAttributeList.containsKey(attributeSchema.getName()))){
                    newAttributeList.put(attributeSchema.getName(),
                            oldAttributeList.get(attributeSchema.getName()));
                    }
            }
            //check for read-only attributes.
            else if (attributeSchema.getMutability() == SCIMDefinitions.Mutability.READ_ONLY) {
                if (!oldAttributeList.containsKey(attributeSchema.getName())
                        && newAttributeList.containsKey(attributeSchema.getName())) {
                    String error = "Read only attribute: " + attributeSchema.getName() +
                            " is set from consumer in the SCIM Object. " + "Removing it.";
                    logger.debug(error);
                    newObject.deleteAttribute(attributeSchema.getName());
                }
                else if(oldAttributeList.containsKey(attributeSchema.getName())
                        && newAttributeList.containsKey(attributeSchema.getName())){
                    String error = "Read only attribute: " + attributeSchema.getName() +
                            " is set from consumer in the SCIM Object. " + "Removing it and replacing with the old value.";
                    logger.debug(error);
                    newAttributeList.remove(attributeSchema.getName());
                    newAttributeList.put(attributeSchema.getName(),
                            oldAttributeList.get(attributeSchema.getName()));
                }
                else if(oldAttributeList.containsKey(attributeSchema.getName())
                        && !newAttributeList.containsKey(attributeSchema.getName())){
                    newAttributeList.put(attributeSchema.getName(),
                            oldAttributeList.get(attributeSchema.getName()));
                }
            }
            //check for sub attributes.
            AbstractAttribute newAttribute = (AbstractAttribute) newAttributeList.get(attributeSchema.getName());
            AbstractAttribute oldAttribute = (AbstractAttribute) oldAttributeList.get(attributeSchema.getName());
            if (newAttribute!=null) {
                List<SCIMAttributeSchema> subAttributesSchemaList =
                        ((SCIMAttributeSchema) attributeSchema).getSubAttributes();

                if (subAttributesSchemaList != null && !subAttributesSchemaList.isEmpty()) {
                    for (SCIMAttributeSchema subAttributeSchema : subAttributesSchemaList) {

                        if(attributeSchema.getMutability()== SCIMDefinitions.Mutability.IMMUTABLE){
                            if (newAttribute instanceof ComplexAttribute) {
                                if ((oldAttribute.getSubAttribute(subAttributeSchema.getName()) != null
                                        && newAttribute.getSubAttribute(subAttributeSchema.getName()) != null) &&
                                        ((SimpleAttribute) (oldAttribute.getSubAttribute(subAttributeSchema.getName()))).getValue()
                                                != ((SimpleAttribute) (newAttribute.getSubAttribute(subAttributeSchema.getName()))).getValue()) {
                                    String error = "Immutable value is trying to be set.";
                                    throw new BadRequestException(ResponseCodeConstants.MUTABILITY);
                                } else if ((oldAttribute.getSubAttribute(subAttributeSchema.getName()) != null
                                        && newAttribute.getSubAttribute(subAttributeSchema.getName()) == null)) {
                                    ((ComplexAttribute) newAttribute).setSubAttribute(
                                            oldAttribute.getSubAttribute(subAttributeSchema.getName()));
                                }
                            }
                            else if (newAttribute instanceof MultiValuedAttribute) {

                            }
                        }
                        else if (subAttributeSchema.getMutability() == SCIMDefinitions.Mutability.READ_ONLY) {
                            if (newAttribute instanceof ComplexAttribute) {
                                if (newAttribute.getSubAttribute(subAttributeSchema.getName()) != null
                                        && oldAttribute.getSubAttribute(subAttributeSchema.getName()) == null) {
                                    String error = "Read only attribute: " + subAttributeSchema.getName() +
                                            " is set from consumer in the SCIM Object. " + "Removing it.";
                                    logger.debug(error);
                                    ((ComplexAttribute) newAttribute).removeSubAttribute(subAttributeSchema.getName());
                                } else if (newAttribute.getSubAttribute(subAttributeSchema.getName()) != null
                                        && oldAttribute.getSubAttribute(subAttributeSchema.getName()) != null) {
                                    String error = "Read only attribute: " + subAttributeSchema.getName() +
                                            " is set from consumer in the SCIM Object. " +
                                            "Removing it and replacing with the old value.";
                                    logger.debug(error);
                                    ((ComplexAttribute) newAttribute).removeSubAttribute(subAttributeSchema.getName());
                                    ((ComplexAttribute) newAttribute).setSubAttribute(
                                            oldAttribute.getSubAttribute(subAttributeSchema.getName()));
                                } else if (newAttribute.getSubAttribute(subAttributeSchema.getName()) == null
                                        && oldAttribute.getSubAttribute(subAttributeSchema.getName()) != null) {
                                    ((ComplexAttribute) newAttribute).setSubAttribute(
                                            oldAttribute.getSubAttribute(subAttributeSchema.getName()));
                                }
                            }
                            else if (newAttribute instanceof MultiValuedAttribute) {
                                List<Attribute> newValues =
                                        ((MultiValuedAttribute) newAttribute).getAttributeValues();
                                List<Attribute> oldValues =
                                        ((MultiValuedAttribute) oldAttribute).getAttributeValues();
                                for (Attribute value : newValues) {
                                    if (value instanceof ComplexAttribute) {
                                        if (value.getSubAttribute(subAttributeSchema.getName()) != null) {
                                            String error = "Readonly sub attribute: " + subAttributeSchema.getName()
                                                    + " is set in the SCIM Attribute: " + newAttribute.getName() +
                                                    ". Removing it.";
                                            ((ComplexAttribute) value).removeSubAttribute(subAttributeSchema.getName());
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return null;
    }
}
