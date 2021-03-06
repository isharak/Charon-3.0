/*
 * Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.charon.core.v2.exceptions;

import org.wso2.charon.core.v2.protocol.ResponseCodeConstants;

/**
 * The specified version number does not match the resource's
 "latest version number, or a service provider refused to create a new, duplicate resource.
 */
public class ConflictException extends AbstractCharonException  {

    public ConflictException() {
        status = ResponseCodeConstants.CODE_CONFLICT;
        detail = ResponseCodeConstants.DESC_CONFLICT;
    }

    public ConflictException(String detail) {
        status = ResponseCodeConstants.CODE_CONFLICT;
        this.detail = detail;
    }
}
