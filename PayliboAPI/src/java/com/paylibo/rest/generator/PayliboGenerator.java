/**
 * Copyright (c) 2012, Paylibo (www.paylibo.com).
 */
package com.paylibo.rest.generator;

import com.paylibo.account.BankAccount;
import com.paylibo.qr.PayliboQRUtils;
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
@RequestMapping(value = "generator")
public class PayliboGenerator {

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
            String iban,
            String bic,
            Number amount,
            String currency,
            String sendersReference,
            String recipientName,
            String identifier,
            Date date,
            String message,
            Map xmap,
            boolean transliterate) {
        // prepare the generic bank account
        BankAccount account = new BankAccount(iban) {

            @Override
            public String getIBAN() {
                return iban;
            }

            @Override
            public void setIBAN(String iban) {
                this.iban = iban;
            }
        };
        // prepare the common parameters
        PayliboParameters parameters = new PayliboParameters();
        parameters.setBic(bic);
        parameters.setAmount(amount);
        parameters.setCurrency(currency);
        parameters.setDate(date);
        parameters.setIdentifier(identifier);
        parameters.setMessage(message);
        parameters.setRecipientName(recipientName);
        parameters.setSendersReference(sendersReference);

        // prepare the extended parameters
        PayliboMap map = new PayliboMap((Map<String, String[]>) xmap);

        return Paylibo.payliboStringFromAccount(account, parameters, map, transliterate);
    }

    @RequestMapping(value = "string", method = RequestMethod.GET)
    public String payliboStringFromAccount(HttpServletRequest request, HttpServletResponse response,
            @RequestParam(value = "iban", required = true) String iban,
            @RequestParam(value = "bic", required = false) String bic,
            @RequestParam(value = "amount", required = false) Number amount,
            @RequestParam(value = "currency", required = false) String currency,
            @RequestParam(value = "sendersReference", required = false) String sendersReference,
            @RequestParam(value = "recipientName", required = false) String recipientName,
            @RequestParam(value = "identifier", required = false) String identifier,
            @RequestParam(value = "date", required = false) Date date,
            @RequestParam(value = "message", required = false) String message,
            @RequestParam(value = "compress", required = false) boolean transliterate) throws IOException {
        // flush the output
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/plain");
        response.getWriter().print(this.payliboFromParameters(
                iban,
                bic,
                amount,
                currency,
                sendersReference,
                recipientName,
                identifier,
                date,
                message,
                request.getParameterMap(),
                transliterate));
        response.getWriter().flush();
        return null;
    }

    @RequestMapping(value = "image", method = RequestMethod.GET)
    public String payliboImageFromAccount(HttpServletRequest request, HttpServletResponse response,
            @RequestParam(value = "iban", required = true) String iban,
            @RequestParam(value = "bic", required = false) String bic,
            @RequestParam(value = "amount", required = false) Number amount,
            @RequestParam(value = "currency", required = false) String currency,
            @RequestParam(value = "sendersReference", required = false) String sendersReference,
            @RequestParam(value = "recipientName", required = false) String recipientName,
            @RequestParam(value = "identifier", required = false) String identifier,
            @RequestParam(value = "date", required = false) Date date,
            @RequestParam(value = "message", required = false) String message,
            @RequestParam(value = "size", required = false) Integer size,
            @RequestParam(value = "compress", required = false) boolean transliterate) throws IOException {
        // flush the output
        response.setContentType("image/png");
        String payliboString = this.payliboFromParameters(
                iban,
                bic,
                amount,
                currency,
                sendersReference,
                recipientName,
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
