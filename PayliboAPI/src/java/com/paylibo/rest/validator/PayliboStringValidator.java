/**
 * Copyright (c) 2012, Paylibo (www.paylibo.com).
 */
package com.paylibo.rest.validator;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.paylibo.rest.utils.JsonErrorSerializer;
import com.paylibo.utilities.PayliboValidationError;
import com.paylibo.utilities.PayliboValidator;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 *
 * @author petrdvorak
 */
@Controller
@RequestMapping(value = "validator")
public class PayliboStringValidator {

    @RequestMapping(value = "string")
    public String validatePayliboString(@RequestParam(value = "payliboString") String payliboString, HttpServletRequest request, HttpServletResponse response) {
        List<PayliboValidationError> validationResult = PayliboValidator.validatePayliboString(payliboString);
        if (validationResult == null || validationResult.isEmpty()) {
            try {
                response.getOutputStream().print("\"OK\"");
            } catch (IOException ex) {
                Logger.getLogger(PayliboStringValidator.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            try {
                response.setStatus(400);
                JsonErrorSerializer.serializeErrorsInStream(response.getOutputStream(), validationResult);
            } catch (IOException ex) {
                Logger.getLogger(PayliboStringValidator.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return null;
    }

    @RequestMapping(value = "iban")
    public String validateIBANNumber(@RequestParam(value = "iban") String iban, HttpServletRequest request, HttpServletResponse response) {
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
            Logger.getLogger(PayliboStringValidator.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
}
