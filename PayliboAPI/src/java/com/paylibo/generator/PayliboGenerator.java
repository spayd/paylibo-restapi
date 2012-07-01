/**
 *  Copyright (c) 2012, Paylibo (www.paylibo.com).
 */
package com.paylibo.generator;

import com.paylibo.account.BankAccount;
import com.paylibo.qr.PayliboQRUtils;
import com.paylibo.string.Paylibo;
import com.paylibo.string.PayliboMap;
import com.paylibo.string.PayliboParameters;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Date;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 *
 * @author petrdvorak
 */
@Controller
@RequestMapping(value = "generator")
public class PayliboGenerator {

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
            Map xmap) {
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

        return Paylibo.payliboStringFromAccount(account, parameters, map);
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
            @RequestParam(value = "message", required = false) String message) throws IOException {
        // flush the output
        response.getOutputStream().print(this.payliboFromParameters(
                iban,
                bic,
                amount,
                currency,
                sendersReference,
                recipientName,
                identifier,
                date,
                message,
                request.getParameterMap()));
        response.getOutputStream().flush();
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
            @RequestParam(value = "size", required = false) Integer size) throws IOException {
        // flush the output
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
                request.getParameterMap());
        BufferedImage qrCode = PayliboQRUtils.getQRCode(size, payliboString);
        ImageIO.write(qrCode, "PNG", response.getOutputStream());
        response.getOutputStream().flush();
        return null;
    }
}
