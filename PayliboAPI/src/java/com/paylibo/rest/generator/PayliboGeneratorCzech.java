/**
 *  Copyright (c) 2012, Paylibo (www.paylibo.com).
 */
package com.paylibo.rest.generator;

import com.paylibo.qr.PayliboQRUtils;
import com.paylibo.account.CzechBankAccount;
import com.paylibo.rest.utils.JsonErrorSerializer;
import com.paylibo.string.Paylibo;
import com.paylibo.string.PayliboMap;
import com.paylibo.string.PayliboParameters;
import com.paylibo.utilities.PayliboValidationError;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

/**
 *
 * @author petrdvorak
 */
@Controller
@RequestMapping(value = "generator/czech")
public class PayliboGeneratorCzech {
    
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        dateFormat.setLenient(false);
        binder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat, false));
    }
    
    @ExceptionHandler(Exception.class)
    public String handleException(Exception exception, HttpServletRequest request, HttpServletResponse response) {
        response.setStatus(400);
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        List<PayliboValidationError> errors = new LinkedList<PayliboValidationError>();
        PayliboValidationError error = new PayliboValidationError();
        error.setErrorCode(PayliboValidationError.ERROR_REQUEST_GENERIC);
        error.setErrorDescription(exception.getMessage()!=null?exception.getMessage():exception.toString());
        errors.add(error);
        try {
            JsonErrorSerializer.serializeErrorsInStream(response.getOutputStream(), errors);
        } catch (IOException ex) {
            Logger.getLogger(PayliboGeneratorCzech.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    private String payliboFromParameters(
            String accountNumber,
            String accountPrefix,
            String bankCode,
            Number amount,
            String currency,
            String vs,
            String ks,
            String ss,
            String identifier,
            Date date,
            String message,
            Map xmap,
            boolean transliterate) {
        // prepare the generic bank account
        CzechBankAccount account = new CzechBankAccount(accountPrefix, accountNumber, bankCode);
        // prepare the common parameters
        PayliboParameters parameters = new PayliboParameters();
        parameters.setAmount(amount);
        parameters.setCurrency(currency);
        parameters.setDate(date);
        parameters.setIdentifier(identifier);
        parameters.setMessage(message);

        // prepare the extended parameters
        PayliboMap map = new PayliboMap(xmap);
        map.put("X-VS", vs);
        map.put("X-SS", ss);
        map.put("X-KS", ks);

        return Paylibo.payliboStringFromAccount(account, parameters, map, transliterate);
    }

    @RequestMapping(value = "string", method = RequestMethod.GET)
    public String payliboStringFromAccountCzech(HttpServletRequest request, HttpServletResponse response,
            @RequestParam(value = "accountNumber", required = true) String accountNumber,
            @RequestParam(value = "accountPrefix", required = false) String accountPrefix,
            @RequestParam(value = "bankCode", required = true) String bankCode,
            @RequestParam(value = "amount", required = false) Number amount,
            @RequestParam(value = "currency", required = false) String currency,
            @RequestParam(value = "vs", required = false) String vs,
            @RequestParam(value = "ks", required = false) String ks,
            @RequestParam(value = "ss", required = false) String ss,
            @RequestParam(value = "identifier", required = false) String identifier,
            @RequestParam(value = "date", required = false) Date date,
            @RequestParam(value = "message", required = false) String message,
            @RequestParam(value = "compress", required = false, defaultValue = "true") boolean transliterate) throws IOException {

        // flush the output
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/plain");
        response.getWriter().print(this.payliboFromParameters(
                accountNumber, 
                accountPrefix, 
                bankCode, 
                amount, 
                currency, 
                vs, 
                ks, 
                ss, 
                identifier, 
                date, 
                message, 
                request.getParameterMap(),
                transliterate));
        response.getWriter().flush();
        return null;
    }
    
    @RequestMapping(value = "image", method = RequestMethod.GET)
    public String payliboImageFromAccountCzech(HttpServletRequest request, HttpServletResponse response,
            @RequestParam(value = "accountNumber", required = true) String accountNumber,
            @RequestParam(value = "accountPrefix", required = false) String accountPrefix,
            @RequestParam(value = "bankCode", required = false) String bankCode,
            @RequestParam(value = "amount", required = false) Number amount,
            @RequestParam(value = "currency", required = false) String currency,
            @RequestParam(value = "vs", required = false) String vs,
            @RequestParam(value = "ks", required = false) String ks,
            @RequestParam(value = "ss", required = false) String ss,
            @RequestParam(value = "identifier", required = false) String identifier,
            @RequestParam(value = "date", required = false) Date date,
            @RequestParam(value = "message", required = false) String message,
            @RequestParam(value = "size", required = false) Integer size,
            @RequestParam(value = "compress", required = false, defaultValue = "true") boolean transliterate) throws IOException {

        // flush the output
        response.setContentType("image/png");
        String payliboString = this.payliboFromParameters(
                accountNumber, 
                accountPrefix, 
                bankCode, 
                amount, 
                currency, 
                vs, 
                ks, 
                ss, 
                identifier, 
                date, 
                message, 
                request.getParameterMap(),
                transliterate);
        BufferedImage qrCode = PayliboQRUtils.getQRCode(size, payliboString);
        ImageIO.write(qrCode, "PNG", response.getOutputStream());
        response.getOutputStream().flush();
        return null;
    }
}
