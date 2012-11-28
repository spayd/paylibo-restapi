/**
 *  Copyright (c) 2012, SmartPayment (www.SmartPayment.com).
 */
package com.smartpaymentformat.rest.validator;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.smartpaymentformat.rest.generator.SmartPaymentGeneratorCzech;
import com.smartpaymentformat.rest.utils.JsonErrorSerializer;
import com.smartpaymentformat.utilities.SmartPaymentValidationError;
import com.smartpaymentformat.utilities.SmartPaymentValidator;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 *
 * @author petrdvorak
 */
@Controller
@RequestMapping(value = "validator")
public class SmartPaymentStringValidator {
    
    @ExceptionHandler(Exception.class)
    public String handleException(Exception exception, HttpServletRequest request, HttpServletResponse response) {
        response.setStatus(400);
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        response.setHeader("Access-Control-Allow-Origin", "*");
        List<SmartPaymentValidationError> errors = new LinkedList<SmartPaymentValidationError>();
        SmartPaymentValidationError error = new SmartPaymentValidationError();
        error.setErrorCode(SmartPaymentValidationError.ERROR_REQUEST_GENERIC);
        error.setErrorDescription(exception.getMessage()!=null?exception.getMessage():exception.toString());
        errors.add(error);
        try {
            JsonErrorSerializer.serializeErrorsInStream(response.getOutputStream(), errors);
        } catch (IOException ex) {
            Logger.getLogger(SmartPaymentGeneratorCzech.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    @RequestMapping(value = "string")
    public String validatePaymentString(@RequestParam(value = "paymentString") String paymentString, HttpServletRequest request, HttpServletResponse response) {
        List<SmartPaymentValidationError> validationResult = SmartPaymentValidator.validatePaymentString(paymentString);
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        response.setHeader("Access-Control-Allow-Origin", "*");
        if (validationResult == null || validationResult.isEmpty()) {
            try {
                response.getOutputStream().print("\"OK\"");
            } catch (IOException ex) {
                Logger.getLogger(SmartPaymentStringValidator.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            try {
                response.setStatus(400);
                JsonErrorSerializer.serializeErrorsInStream(response.getOutputStream(), validationResult);
            } catch (IOException ex) {
                Logger.getLogger(SmartPaymentStringValidator.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return null;
    }

    @RequestMapping(value = "iban")
    public String validateIBANNumber(@RequestParam(value = "iban") String iban, HttpServletRequest request, HttpServletResponse response) {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        response.setHeader("Access-Control-Allow-Origin", "*");
        try {
            JsonFactory f = new JsonFactory();
            JsonGenerator g = f.createJsonGenerator(response.getOutputStream());
            g.writeStartObject();
            //TODO:Add persistence API here to fetch the status of the IBAN
            g.writeStringField("result", "unknown");
            g.writeStringField("iban", iban);
            g.writeEndObject();
            g.close();

        } catch (IOException ex) {
            Logger.getLogger(SmartPaymentStringValidator.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
}
