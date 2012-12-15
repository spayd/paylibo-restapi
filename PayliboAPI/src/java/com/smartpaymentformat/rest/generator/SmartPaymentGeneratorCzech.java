/**
 *  Copyright (c) 2012, SmartPayment (www.SmartPayment.com).
 */
package com.smartpaymentformat.rest.generator;

import com.smartpaymentformat.account.CzechBankAccount;
import com.smartpaymentformat.qr.SmartPaymentQRUtils;
import com.smartpaymentformat.rest.utils.JsonErrorSerializer;
import com.smartpaymentformat.string.SmartPayment;
import com.smartpaymentformat.string.SmartPaymentMap;
import com.smartpaymentformat.string.SmartPaymentParameters;
import com.smartpaymentformat.utilities.SmartPaymentValidationError;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
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
public class SmartPaymentGeneratorCzech {
    
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

    private String paymentStringFromParameters(
            String accountNumber,
            String accountPrefix,
            String bankCode,
            Number amount,
            String currency,
            String vs,
            String ks,
            String ss,
            Date date,
            String message,
            Map xmap,
            boolean transliterate) throws UnsupportedEncodingException {
        // prepare the generic bank account
        CzechBankAccount account = new CzechBankAccount(accountPrefix, accountNumber, bankCode);
        // prepare the common parameters
        SmartPaymentParameters parameters = new SmartPaymentParameters();
        parameters.setBankAccount(account);
        parameters.setAmount(amount);
        parameters.setCurrency(currency);
        parameters.setDate(date);
        parameters.setMessage(message);

        // prepare the extended parameters
        SmartPaymentMap map = new SmartPaymentMap(xmap);
        map.put("X-VS", vs);
        map.put("X-SS", ss);
        map.put("X-KS", ks);

        return SmartPayment.paymentStringFromAccount(parameters, map, transliterate);
    }

    @RequestMapping(value = "string", method = RequestMethod.GET)
    public String paymentStringFromAccountCzech(HttpServletRequest request, HttpServletResponse response,
            @RequestParam(value = "accountNumber", required = true) String accountNumber,
            @RequestParam(value = "accountPrefix", required = false) String accountPrefix,
            @RequestParam(value = "bankCode", required = true) String bankCode,
            @RequestParam(value = "amount", required = false) Number amount,
            @RequestParam(value = "currency", required = false) String currency,
            @RequestParam(value = "vs", required = false) String vs,
            @RequestParam(value = "ks", required = false) String ks,
            @RequestParam(value = "ss", required = false) String ss,
            @RequestParam(value = "date", required = false) Date date,
            @RequestParam(value = "message", required = false) String message,
            @RequestParam(value = "compress", required = false, defaultValue = "true") boolean transliterate) throws IOException {

        // flush the output
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/plain");
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.getWriter().print(this.paymentStringFromParameters(
                accountNumber, 
                accountPrefix, 
                bankCode, 
                amount, 
                currency, 
                vs, 
                ks, 
                ss, 
                date, 
                message, 
                request.getParameterMap(),
                transliterate));
        response.getWriter().flush();
        return null;
    }
    
    @RequestMapping(value = "spayd", method = RequestMethod.GET)
    public String paymentFileFromAccount(HttpServletRequest request, HttpServletResponse response,
            @RequestParam(value = "accountNumber", required = true) String accountNumber,
            @RequestParam(value = "accountPrefix", required = false) String accountPrefix,
            @RequestParam(value = "bankCode", required = true) String bankCode,
            @RequestParam(value = "amount", required = false) Number amount,
            @RequestParam(value = "currency", required = false) String currency,
            @RequestParam(value = "vs", required = false) String vs,
            @RequestParam(value = "ks", required = false) String ks,
            @RequestParam(value = "ss", required = false) String ss,
            @RequestParam(value = "date", required = false) Date date,
            @RequestParam(value = "message", required = false) String message,
            @RequestParam(value = "compress", required = false, defaultValue = "true") boolean transliterate) throws IOException {
        // flush the output
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/x-shortpaymentdescriptor");
        response.setHeader("Content-Disposition", "attachment; filename=\"payment_info.spayd\"");
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.getWriter().print(this.paymentStringFromParameters(
                accountNumber, 
                accountPrefix, 
                bankCode, 
                amount, 
                currency, 
                vs, 
                ks, 
                ss, 
                date, 
                message, 
                request.getParameterMap(),
                transliterate));
        response.getWriter().flush();
        return null;
    }
    
    @RequestMapping(value = "image", method = RequestMethod.GET)
    public String paymentImageFromAccountCzech(HttpServletRequest request, HttpServletResponse response,
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
            @RequestParam(value = "compress", required = false, defaultValue = "true") boolean transliterate,
            @RequestParam(value = "branding", required = false, defaultValue = "true") boolean branding) throws IOException {

        // flush the output
        response.setContentType("image/png");
        response.setHeader("Access-Control-Allow-Origin", "*");
        String paymentString = this.paymentStringFromParameters(
                accountNumber, 
                accountPrefix, 
                bankCode, 
                amount, 
                currency, 
                vs, 
                ks, 
                ss, 
                date, 
                message, 
                request.getParameterMap(),
                transliterate);
        BufferedImage qrCode = SmartPaymentQRUtils.getQRCode(size, paymentString, branding);
        ImageIO.write(qrCode, "PNG", response.getOutputStream());
        response.getOutputStream().flush();
        return null;
    }
}
