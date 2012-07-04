/**
 *  Copyright (c) 2012, Paylibo (www.paylibo.com).
 */
package com.paylibo.rest.generator;

import com.paylibo.qr.PayliboQRUtils;
import com.paylibo.account.CzechBankAccount;
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
@RequestMapping(value = "generator/czech")
public class PayliboGeneratorCzech {

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
            Map xmap) {
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

        return Paylibo.payliboStringFromAccount(account, parameters, map);
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
            @RequestParam(value = "message", required = false) String message) throws IOException {

        // flush the output
        response.getOutputStream().print(this.payliboFromParameters(
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
                request.getParameterMap()));
        response.getOutputStream().flush();
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
            @RequestParam(value = "size", required = false) Integer size) throws IOException {

        // flush the output
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
                request.getParameterMap());
        BufferedImage qrCode = PayliboQRUtils.getQRCode(size, payliboString);
        ImageIO.write(qrCode, "PNG", response.getOutputStream());
        response.getOutputStream().flush();
        return null;
    }
}
