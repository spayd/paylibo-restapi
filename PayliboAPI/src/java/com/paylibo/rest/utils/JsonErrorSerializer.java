/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.paylibo.rest.utils;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.paylibo.utilities.PayliboValidationError;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

/**
 *
 * @author petrdvorak
 */
public class JsonErrorSerializer {

    public static void serializeErrorsInStream(OutputStream stream, List<PayliboValidationError> errors) throws IOException {
        JsonFactory f = new JsonFactory();
        JsonGenerator g = f.createJsonGenerator(stream);

        g.writeStartObject();
        g.writeStringField("description", "Errors occurred during the object validation");
        g.writeArrayFieldStart("errors");
        for (PayliboValidationError error : errors) {
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
