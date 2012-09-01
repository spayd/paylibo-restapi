/**
 *  Copyright (c) 2012, SmartPayment (www.SmartPayment.com).
 */
package com.smartpaymentformat.rest.utils;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.smartpaymentformat.utilities.SmartPaymentValidationError;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

/**
 *
 * @author petrdvorak
 */
public class JsonErrorSerializer {

    public static void serializeErrorsInStream(OutputStream stream, List<SmartPaymentValidationError> errors) throws IOException {
        JsonFactory f = new JsonFactory();
        JsonGenerator g = f.createJsonGenerator(stream);

        g.writeStartObject();
        g.writeStringField("description", "Errors occurred during the object validation");
        g.writeArrayFieldStart("errors");
        for (SmartPaymentValidationError error : errors) {
            g.writeStartObject();
            g.writeStringField("code", error.getErrorCode());
            g.writeStringField("description", error.getErrorDescription());
            g.writeEndObject();
        }
        g.writeEndArray();
        g.writeEndObject();
        g.close();
    }
}
