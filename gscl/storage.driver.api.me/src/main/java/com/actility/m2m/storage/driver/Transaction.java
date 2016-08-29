/*******************************************************************************
 * Copyright   Actility, SA. All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License version
 * 2 only, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License version 2 for more details (a copy is
 * included at /legal/license.txt).
 *
 * You should have received a copy of the GNU General Public License
 * version 2 along with this work; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA
 *
 * Please contact Actility, SA.,  4, rue Ampere 22300 LANNION FRANCE
 * or visit www.actility.com if you need additional
 * information or have any questions.
 *******************************************************************************/
package com.actility.m2m.storage.driver;

import java.util.List;
import java.util.Map;

import com.actility.m2m.storage.Batch;
import com.actility.m2m.storage.Condition;
import com.actility.m2m.storage.Document;
import com.actility.m2m.storage.StorageException;

/**
 * Represents a storage driver transaction
 */
public interface Transaction {

    /**
     * Prepares the transaction.
     * <p>
     * According to real storage implementation, operations happen here and may return an error through its return value.
     *
     * @return An integer which described the preparation result. ({@link Batch#BATCH_OK}, {@link Batch#BATCH_CREATE_FAILED},
     *         {@link Batch#BATCH_UPDATE_FAILED}, {@link Batch#BATCH_DELETE_FAILED})
     * @throws StorageException if transaction cannot be prepared
     */
    int prepareTransaction() throws StorageException;

    /**
     * Raises an exception if transaction cannot be committed
     *
     * @throws StorageException if transaction cannot be committed
     */
    void commitTransaction() throws StorageException;

    /**
     * Raises an exception if transaction cannot be rollbacked
     *
     * @throws StorageException if transaction cannot be rollbacked
     */
    void rollbackTransaction() throws StorageException;

    /**
     * Updates an existing document.
     * <p>
     * Raises an exception if the document does not exist.
     *
     * @param config storage configuration to apply
     * @param document the new representation of the document
     * @param condition condition that must be fulfilled in order to perform the operation
     * @return Whether the update has succeeded. If the update fails, it means the document does not exist in the storage or the
     *         condition is not fulfilled
     * @throws StorageException if any problem occurs while retrieving the document
     */
    boolean update(Map/* <String, String> */config, Document document, Condition condition) throws StorageException;

    /**
     * Updates partially an existing document.
     * <p>
     * Raises an exception if the document does not exist.
     *
     * @param config storage configuration to apply
     * @param document the document to partially update. Only id or path are used
     * @param content the new content for the document (if <code>null</code>, do not update the content)
     * @param attrOps operations to perform on the document attributes (if <code>null</code> or empty, do not update attributes)
     * @param condition condition that must be fulfilled in order to perform the operation
     * @return Whether the partial update has succeeded. If the partial update fails, it means the document does not exist or
     *         the condition is not fulfilled
     * @throws StorageException if any problem occurs while retrieving the document
     */
    boolean partialUpdate(Map/* <String, String> */config, Document document, byte[] content,
            List/* <AttributeOperation> */attrOps, Condition condition) throws StorageException;

    /**
     * Creates a new document.
     * <p>
     * Raises an exception if the document already exists.
     *
     * @param config storage configuration to apply
     * @param document the representation of the document
     * @return Whether the create has succeeded. If the create fails, it means the document already exists
     * @throws StorageException if any problem occurs while retrieving the document
     */
    boolean create(Map/* <String, String> */config, Document document) throws StorageException;

    /**
     * Deletes a document.
     * <p>
     * Raises an exception if the document does not exist.
     *
     * @param config storage configuration to apply
     * @param document The document to delete. Only id or path are used
     * @param condition condition that must be fulfilled in order to perform the operation
     * @return Whether the delete has succeeded. If the delete fails, it means the document does not exist or the condition is
     *         not fulfilled
     * @throws StorageException if any problem occurs while retrieving the document
     */
    boolean delete(Map/* <String, String> */config, Document document, Condition condition) throws StorageException;
}