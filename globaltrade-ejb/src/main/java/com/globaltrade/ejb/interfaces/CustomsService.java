package com.globaltrade.ejb.interfaces;

import jakarta.ejb.Local;
import com.globaltrade.entity.CustomsDocument;
import com.globaltrade.exception.CustomsException;

@Local
public interface CustomsService {
    CustomsDocument fileCustomsDeclaration(Long shipmentId, String documentType, String hsCode) throws CustomsException;
    CustomsDocument inspectAndApprove(Long documentId, String inspectorName) throws CustomsException;
}
