/**
 *  Copyright (c) 2012, SPAYD (www.spayd.org).
 */
package org.spayd.rest.utils;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import org.spayd.model.error.SpaydValidationError;

/**
 *
 * @author petrdvorak
 */
public class JsonErrorSerializer {

    public static void serializeErrorsInStream(OutputStream stream, List<SpaydValidationError> errors) throws IOException {
        JsonFactory f = new JsonFactory();
        JsonGenerator g = f.createJsonGenerator(stream);

        g.writeStartObject();
        g.writeStringField("description", "Errors occurred during the object validation");
        g.writeArrayFieldStart("errors");
        for (SpaydValidationError error : errors) {
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
