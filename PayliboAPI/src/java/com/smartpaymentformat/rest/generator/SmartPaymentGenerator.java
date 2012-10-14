/**
 *  Copyright (c) 2012, SmartPayment (www.SmartPayment.com).
 */
package com.smartpaymentformat.rest.generator;

import com.smartpaymentformat.account.BankAccount;
import com.smartpaymentformat.qr.SmartPaymentQRUtils;
import com.smartpaymentformat.rest.utils.JsonErrorSerializer;
import com.smartpaymentformat.string.SmartPayment;
import com.smartpaymentformat.string.SmartPaymentMap;
import com.smartpaymentformat.string.SmartPaymentParameters;
import com.smartpaymentformat.utilities.SmartPaymentValidationError;
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
@RequestMapping(value = "generator")
public class SmartPaymentGenerator {

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
            String iban,
            String bic,
            Number amount,
            String currency,
            String sendersReference,
            String recipientName,
            Date date,
            String message,
            Map xmap,
            boolean transliterate) {
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
        SmartPaymentParameters parameters = new SmartPaymentParameters();
        parameters.setBankAccount(account);
        parameters.setAmount(amount);
        parameters.setCurrency(currency);
        parameters.setDate(date);
        parameters.setMessage(message);
        parameters.setRecipientName(recipientName);
        parameters.setSendersReference(sendersReference);

        // prepare the extended parameters
        SmartPaymentMap map = new SmartPaymentMap((Map<String, String[]>) xmap);

        return SmartPayment.paymentStringFromAccount(parameters, map, transliterate);
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
        response.getWriter().print(this.paymentStringFromParameters(
                iban,
                bic,
                amount,
                currency,
                sendersReference,
                recipientName,
                date,
                message,
                request.getParameterMap(),
                transliterate));
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
            @RequestParam(value = "compress", required = false, defaultValue = "true") boolean transliterate) throws IOException {
        // flush the output
        response.setContentType("image/png");
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
        BufferedImage qrCode = SmartPaymentQRUtils.getQRCode(size, paymentString);
        ImageIO.write(qrCode, "PNG", response.getOutputStream());
        response.getOutputStream().flush();
        return null;
    }
}
