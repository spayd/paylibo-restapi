/**
 * Copyright (c) 2012, SPAYD (www.spayd.org).
 */
package org.spayd.rest.controller.generator;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
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
import org.spayd.model.account.CzechBankAccount;
import org.spayd.model.error.SpaydValidationError;
import org.spayd.rest.utils.JsonErrorSerializer;
import org.spayd.string.SpaydExtendedPaymentAttributeMap;
import org.spayd.string.SpaydPayment;
import org.spayd.string.SpaydPaymentAttributes;
import org.spayd.utilities.SpaydQRUtils;
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
public class SpaydGeneratorCzechController {

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
        List<SpaydValidationError> errors = new LinkedList<SpaydValidationError>();
        SpaydValidationError error = new SpaydValidationError();
        error.setErrorCode(SpaydValidationError.ERROR_REQUEST_GENERIC);
        error.setErrorDescription(exception.getMessage() != null ? exception.getMessage() : exception.toString());
        errors.add(error);
        try {
            JsonErrorSerializer.serializeErrorsInStream(response.getOutputStream(), errors);
        } catch (IOException ex) {
            Logger.getLogger(SpaydGeneratorCzechController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    /**
     * Generates the SPAYD string based on the Czech account information.
     *
     * @param accountNumber A recipient account number
     * @param accountPrefix A recipient account number prefix
     * @param bankCode A recipient account bank code
     * @param amount Amount to be transferred
     * @param currency A currency of the payment
     * @param vs Variable symbol
     * @param ks Constant symbol
     * @param ss Specific symbol
     * @param date Due date in the YYYY-MM-DD format.
     * @param message A message to be attached with the payment
     * @param xmap A map of extended parameters
     * @param transliterate A flag that indicates the characters should be
     * transliterated to ASCII
     * @return A SPAYD string with the payment information.
     * @throws UnsupportedEncodingException
     */
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
        SpaydPaymentAttributes parameters = new SpaydPaymentAttributes();
        parameters.setBankAccount(account);
        parameters.setAmount(amount);
        parameters.setCurrency(currency);
        parameters.setDate(date);
        parameters.setMessage(message);

        // prepare the extended parameters
        SpaydExtendedPaymentAttributeMap map = new SpaydExtendedPaymentAttributeMap(xmap);
        map.put("X-VS", vs);
        map.put("X-SS", ss);
        map.put("X-KS", ks);

        return SpaydPayment.paymentStringFromAccount(parameters, map, transliterate);
    }

    @RequestMapping(value = "string", method = RequestMethod.GET)
    public String paymentStringFromAccountCzech(HttpServletRequest request, HttpServletResponse response,
            @RequestParam(value = "accountPrefix", required = false) String accountPrefix,
            @RequestParam(value = "accountNumber", required = true) String accountNumber,
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
        response.reset();
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/plain");
        response.setHeader("Access-Control-Allow-Origin", "*");
        final String paymentString = this.paymentStringFromParameters(
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
        response.getWriter().print(paymentString);
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
        response.reset();
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/x-shortpaymentdescriptor");
        response.setHeader("Content-Disposition", "attachment; filename=\"payment_info.spayd\"");
        response.setHeader("Access-Control-Allow-Origin", "*");
        final String paymentString = this.paymentStringFromParameters(
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
        response.getWriter().print(paymentString);
        response.getWriter().flush();
        return null;
    }

    @RequestMapping(value = "image", method = RequestMethod.GET)
    public @ResponseBody byte[] paymentImageFromAccountCzech(HttpServletRequest request, HttpServletResponse response,
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
        response.reset();
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
        BufferedImage qrCode = SpaydQRUtils.getQRCode(size, paymentString, branding);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(qrCode, "PNG", baos);
        byte[] byteArray = baos.toByteArray();
        response.setContentLength(byteArray.length);
        response.getOutputStream().flush();
        return byteArray;
    }
}
