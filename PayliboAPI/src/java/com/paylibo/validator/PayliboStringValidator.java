/**
 *  Copyright (c) 2012, Paylibo (www.paylibo.com).
 */
package com.paylibo.validator;

import com.paylibo.utilities.PayliboValidationError;
import com.paylibo.utilities.PayliboValidator;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 *
 * @author petrdvorak
 */
@Controller
@RequestMapping(value="validator")
public class PayliboStringValidator {
    
    @RequestMapping(value="string")
    public String validatePayliboString(@RequestParam(value="payliboString") String payliboString) {
        List<PayliboValidationError> validationResult = PayliboValidator.validatePayliboString(payliboString);
        return null;
    }
    
}
