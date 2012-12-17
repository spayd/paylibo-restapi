/**
 *  Copyright (c) 2012, SPAYD (www.spayd.org).
 */
package org.spayd.rest.controller.generator;

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
import org.spayd.model.account.BankAccount;
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
@RequestMapping(value = "generator")
public class SpaydGeneratorController {

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
        error.setErrorDescription(exception.getMessage()!=null?exception.getMessage():exception.toString());
        errors.add(error);
        try {
            JsonErrorSerializer.serializeErrorsInStream(response.getOutputStream(), errors);
        } catch (IOException ex) {
            Logger.getLogger(SpaydGeneratorCzechController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    /**
     * Build the payment string based on the passed "flat" parameters
     * @param iban An IBAN number of the recipient account
     * @param bic A BIC number of the recipient bank
     * @param amount Amount to be transfered (double with precision of 2 digits)
     * @param currency Currency of the transfer in three letter format (CZK, USD, ...)
     * @param sendersReference A SEPA sender reference ID
     * @param recipientName A name of the recipient
     * @param date Due date in the YYYY-MM-DD format.
     * @param message A message that will be attached to the payment
     * @param xmap A map of extended parameters
     * @param transliterate Flag that indicates if the characters should be transliterated to ASCII
     * @return A SPAYD string with payment information
     * @throws UnsupportedEncodingException 
     */
    private String paymentStringFromParameters(
            String iban,
            String bic,
            Number amount,
            String currency,
            String sendersReference,
            String recipientName,
            Date date,
            String message,
            Map xmap,
            boolean transliterate) throws UnsupportedEncodingException {
        // prepare the generic bank account
        BankAccount account = new BankAccount(iban, bic) {

            @Override
            public String getIBAN() {
                return iban;
            }

            @Override
            public void setIBAN(String iban) {
                this.iban = iban;
            }

            @Override
            public String getBIC() {
                return bic;
            }

            @Override
            public void setBIC(String bic) {
                this.bic = bic;
            }
        };
        // prepare the common parameters
        SpaydPaymentAttributes parameters = new SpaydPaymentAttributes();
        parameters.setBankAccount(account);
        parameters.setAmount(amount);
        parameters.setCurrency(currency);
        parameters.setDate(date);
        parameters.setMessage(message);
        parameters.setRecipientName(recipientName);
        parameters.setSendersReference(sendersReference);

        // prepare the extended parameters
        SpaydExtendedPaymentAttributeMap map = new SpaydExtendedPaymentAttributeMap((Map<String, String>) xmap);

        return SpaydPayment.paymentStringFromAccount(parameters, map, transliterate);
    }

    @RequestMapping(value = "string", method = RequestMethod.GET)
    public String paymentStringFromAccount(HttpServletRequest request, HttpServletResponse response,
            @RequestParam(value = "iban", required = true) String iban,
            @RequestParam(value = "bic", required = false) String bic,
            @RequestParam(value = "amount", required = false) Number amount,
            @RequestParam(value = "currency", required = false) String currency,
            @RequestParam(value = "sendersReference", required = false) String sendersReference,
            @RequestParam(value = "recipientName", required = false) String recipientName,
            @RequestParam(value = "date", required = false) Date date,
            @RequestParam(value = "message", required = false) String message,
            @RequestParam(value = "compress", required = false, defaultValue = "true") boolean transliterate) throws IOException {
        // flush the output
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/plain");
        response.setHeader("Access-Control-Allow-Origin", "*");
        final String paymentString = this.paymentStringFromParameters(
                  iban,
                  bic,
                  amount,
                  currency,
                  sendersReference,
                  recipientName,
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
            @RequestParam(value = "iban", required = true) String iban,
            @RequestParam(value = "bic", required = false) String bic,
            @RequestParam(value = "amount", required = false) Number amount,
            @RequestParam(value = "currency", required = false) String currency,
            @RequestParam(value = "sendersReference", required = false) String sendersReference,
            @RequestParam(value = "recipientName", required = false) String recipientName,
            @RequestParam(value = "date", required = false) Date date,
            @RequestParam(value = "message", required = false) String message,
            @RequestParam(value = "compress", required = false, defaultValue = "true") boolean transliterate) throws IOException {
        // flush the output
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/x-shortpaymentdescriptor");
        response.setHeader("Content-Disposition", "attachment; filename=\"payment_info.spayd\"");
        response.setHeader("Access-Control-Allow-Origin", "*");
        final String paymentString = this.paymentStringFromParameters(
                  iban,
                  bic,
                  amount,
                  currency,
                  sendersReference,
                  recipientName,
                  date,
                  message,
                  request.getParameterMap(),
                  transliterate);
        response.getWriter().print(paymentString);
        response.getWriter().flush();
        return null;
    }

    @RequestMapping(value = "image", method = RequestMethod.GET)
    public String paymentImageFromAccount(HttpServletRequest request, HttpServletResponse response,
            @RequestParam(value = "iban", required = true) String iban,
            @RequestParam(value = "bic", required = false) String bic,
            @RequestParam(value = "amount", required = false) Number amount,
            @RequestParam(value = "currency", required = false) String currency,
            @RequestParam(value = "sendersReference", required = false) String sendersReference,
            @RequestParam(value = "recipientName", required = false) String recipientName,
            @RequestParam(value = "date", required = false) Date date,
            @RequestParam(value = "message", required = false) String message,
            @RequestParam(value = "size", required = false) Integer size,
            @RequestParam(value = "compress", required = false, defaultValue = "true") boolean transliterate,
            @RequestParam(value = "branding", required = false, defaultValue = "true") boolean branding) throws IOException {
        // flush the output
        response.setContentType("image/png");
        response.setHeader("Access-Control-Allow-Origin", "*");
        String paymentString = this.paymentStringFromParameters(
                iban,
                bic,
                amount,
                currency,
                sendersReference,
                recipientName,
                date,
                message,
                request.getParameterMap(),
                transliterate);
        BufferedImage qrCode = SpaydQRUtils.getQRCode(size, paymentString, branding);
        ImageIO.write(qrCode, "PNG", response.getOutputStream());
        response.getOutputStream().flush();
        return null;
    }
}
