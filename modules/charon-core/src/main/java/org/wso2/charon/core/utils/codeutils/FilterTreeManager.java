package org.wso2.charon.core.utils.codeutils;

import org.wso2.charon.core.exceptions.BadRequestException;
import org.wso2.charon.core.protocol.ResponseCodeConstants;
import org.wso2.charon.core.schema.SCIMConstants;

import java.io.IOException;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 *
 */
public class FilterTreeManager {

    private StreamTokenizer input;
    public static List<String> tokenList=null;
    private String symbol;
    private Node root;
    private String filterString;

    public void setFilterString(String filterString){
        this.filterString=filterString;
    }

    public FilterTreeManager(String filterString) throws IOException {
        setFilterString(filterString);
        input = new StreamTokenizer(new StringReader(filterString));
        tokenList = new ArrayList<String>();
        String concatenatedString="";

        while(input.nextToken() != StreamTokenizer.TT_EOF){
            //ttype 40 is for the '('
            if(input.ttype == 40){
                tokenList.add("(");
            }
            //ttype 40 is for the ')'
            else if(input.ttype == 41){
                concatenatedString=concatenatedString.trim();
                tokenList.add(concatenatedString);
                concatenatedString="";
                tokenList.add(")");
            }else if(input.ttype == StreamTokenizer.TT_WORD){
                if(!(input.sval.equalsIgnoreCase(SCIMConstants.OperationalConstants.AND)
                        ||input.sval.equalsIgnoreCase(SCIMConstants.OperationalConstants.OR) ||
                        input.sval.equalsIgnoreCase(SCIMConstants.OperationalConstants.NOT)))
                    //concatenate the string by adding spaces in between
                    concatenatedString+=" "+input.sval;
                else{
                    concatenatedString=concatenatedString.trim();
                    tokenList.add(concatenatedString);
                    concatenatedString="";
                    tokenList.add(input.sval);
                }
            }
        }
        //Add to the list, if the filter is a simple filter
        if(!(concatenatedString.equals(""))){
            tokenList.add(concatenatedString);
        }
    }

    public Node buildTree() throws BadRequestException {
        expression();
        return root;
    }

    private void expression() throws BadRequestException {
        term();
        while (symbol.equals(String.valueOf(SCIMConstants.OperationalConstants.OR))) {
            OperationNode or = new OperationNode(SCIMConstants.OperationalConstants.OR);
            or.setLeftNode(root);
            term();
            or.setRightNode(root);
            root = or;
        }
    }

    private void term() throws BadRequestException {
        factor();
        while (symbol.equals(String.valueOf(SCIMConstants.OperationalConstants.AND))) {
            OperationNode and = new OperationNode(SCIMConstants.OperationalConstants.AND);
            and.setLeftNode(root);
            factor();
            and.setRightNode(root);
            root = and;
        }
    }

    private void factor() throws BadRequestException {
        symbol = nextSymbol();
        if (symbol.equals(String.valueOf(SCIMConstants.OperationalConstants.NOT))) {
            OperationNode not = new OperationNode(SCIMConstants.OperationalConstants.NOT);
            symbol = nextSymbol();
            factor();
            not.setRightNode(root);
            root = not;
        } else if (symbol.equals(String.valueOf(SCIMConstants.OperationalConstants.LEFT))) {
            expression();
            symbol = nextSymbol(); // we don't care about ')'
        } else {
            if(!(symbol.equals(String.valueOf(SCIMConstants.OperationalConstants.RIGHT)))){
                ExpressionNode expressionNode = new ExpressionNode();
                validateAndBuildFilterExpression(symbol,expressionNode);
                root= expressionNode;
                symbol = nextSymbol();
            }
            else{
            }

        }
    }

    private void validateAndBuildFilterExpression(String filterString, ExpressionNode expressionNode)
            throws BadRequestException {
        //verify filter string. validation should be case insensitive
        if (!( Pattern.compile(Pattern.quote(SCIMConstants.OperationalConstants.EQ),
                Pattern.CASE_INSENSITIVE).matcher(filterString).find() ||
                Pattern.compile(Pattern.quote(SCIMConstants.OperationalConstants.NE),
                        Pattern.CASE_INSENSITIVE).matcher(filterString).find()||
                Pattern.compile(Pattern.quote(SCIMConstants.OperationalConstants.CO),
                        Pattern.CASE_INSENSITIVE).matcher(filterString).find()||
                Pattern.compile(Pattern.quote(SCIMConstants.OperationalConstants.SW),
                        Pattern.CASE_INSENSITIVE).matcher(filterString).find()||
                Pattern.compile(Pattern.quote(SCIMConstants.OperationalConstants.EW),
                        Pattern.CASE_INSENSITIVE).matcher(filterString).find()||
                Pattern.compile(Pattern.quote(SCIMConstants.OperationalConstants.PR),
                        Pattern.CASE_INSENSITIVE).matcher(filterString).find()||
                Pattern.compile(Pattern.quote(SCIMConstants.OperationalConstants.GT),
                        Pattern.CASE_INSENSITIVE).matcher(filterString).find()||
                Pattern.compile(Pattern.quote(SCIMConstants.OperationalConstants.GE),
                        Pattern.CASE_INSENSITIVE).matcher(filterString).find()||
                Pattern.compile(Pattern.quote(SCIMConstants.OperationalConstants.LT),
                        Pattern.CASE_INSENSITIVE).matcher(filterString).find()||
                Pattern.compile(Pattern.quote(SCIMConstants.OperationalConstants.LE),
                        Pattern.CASE_INSENSITIVE).matcher(filterString).find())) {

            String message = "Given filter operator is not supported.";
            throw new BadRequestException(message, ResponseCodeConstants.INVALID_FILTER);
        }

        String trimmedFilter = filterString.trim();
        String[] filterParts = null;

        if (Pattern.compile(Pattern.quote(SCIMConstants.OperationalConstants.EQ),
                Pattern.CASE_INSENSITIVE).matcher(filterString).find()) {
            filterParts = trimmedFilter.split(" eq | EQ | eQ | Eq ");
            setExpressionNodeValues(filterParts[0], SCIMConstants.OperationalConstants.EQ, filterParts[1], expressionNode);
        }
        else if(Pattern.compile(Pattern.quote(SCIMConstants.OperationalConstants.NE),
                Pattern.CASE_INSENSITIVE).matcher(filterString).find()){
            filterParts = trimmedFilter.split(" ne | NE | nE | Ne ");
            setExpressionNodeValues(filterParts[0], SCIMConstants.OperationalConstants.NE, filterParts[1], expressionNode);
        }
        else if(Pattern.compile(Pattern.quote(SCIMConstants.OperationalConstants.CO),
                Pattern.CASE_INSENSITIVE).matcher(filterString).find()){
            filterParts = trimmedFilter.split(" co | CO | cO | Co ");
            setExpressionNodeValues(filterParts[0], SCIMConstants.OperationalConstants.CO, filterParts[1], expressionNode);
        }
        else if(Pattern.compile(Pattern.quote(SCIMConstants.OperationalConstants.SW),
                Pattern.CASE_INSENSITIVE).matcher(filterString).find()){
            filterParts = trimmedFilter.split(" sw | SW | sW | Sw ");
            setExpressionNodeValues(filterParts[0], SCIMConstants.OperationalConstants.SW, filterParts[1], expressionNode);
        }
        else if(Pattern.compile(Pattern.quote(SCIMConstants.OperationalConstants.EW),
                Pattern.CASE_INSENSITIVE).matcher(filterString).find()){
            filterParts = trimmedFilter.split(" ew | EW | eW | Ew ");
            setExpressionNodeValues(filterParts[0], SCIMConstants.OperationalConstants.EW, filterParts[1], expressionNode);
        }
        else if(Pattern.compile(Pattern.quote(SCIMConstants.OperationalConstants.PR),
                Pattern.CASE_INSENSITIVE).matcher(filterString).find()){
            //with filter PR, there should not be whitespace after.
            filterParts = trimmedFilter.split(" pr| PR| pR| Pr");
            setExpressionNodeValues(filterParts[0], SCIMConstants.OperationalConstants.PR, null, expressionNode);
        }
        else if(Pattern.compile(Pattern.quote(SCIMConstants.OperationalConstants.GT),
                Pattern.CASE_INSENSITIVE).matcher(filterString).find()){
            filterParts = trimmedFilter.split(" gt | GT | gT | Gt ");
            setExpressionNodeValues(filterParts[0], SCIMConstants.OperationalConstants.GT, filterParts[1], expressionNode);

        }else if(Pattern.compile(Pattern.quote(SCIMConstants.OperationalConstants.GE),
                Pattern.CASE_INSENSITIVE).matcher(filterString).find()){
            filterParts = trimmedFilter.split(" ge | GE | gE | Ge ");
            setExpressionNodeValues(filterParts[0], SCIMConstants.OperationalConstants.GE, filterParts[1], expressionNode);
        }
        else if(Pattern.compile(Pattern.quote(SCIMConstants.OperationalConstants.LT),
                Pattern.CASE_INSENSITIVE).matcher(filterString).find()){
            filterParts = trimmedFilter.split(" lt | LT | lT | Lt ");
            setExpressionNodeValues(filterParts[0], SCIMConstants.OperationalConstants.LT, filterParts[1], expressionNode);
        }
        else if(Pattern.compile(Pattern.quote(SCIMConstants.OperationalConstants.LE),
                Pattern.CASE_INSENSITIVE).matcher(filterString).find()){
            filterParts = trimmedFilter.split(" le | LE | lE | Le ");
            setExpressionNodeValues(filterParts[0], SCIMConstants.OperationalConstants.LE, filterParts[1], expressionNode);
        }
        else{
            throw new BadRequestException(ResponseCodeConstants.INVALID_FILTER);
        }
    }


    private void setExpressionNodeValues(String attributeValue, String operation,
                                         String value, ExpressionNode expressionNode){
        expressionNode.setAttributeValue(attributeValue.trim());
        expressionNode.setOperation(operation.trim());
        if(value !=null){
            expressionNode.setValue(value.trim());
        }
    }

    public String nextSymbol(){
        if(tokenList.size()==0){
            return String.valueOf(-1);
        }
        else{
            String value = tokenList.get(0);
            tokenList.remove(0);
            return value;
        }
    }
}