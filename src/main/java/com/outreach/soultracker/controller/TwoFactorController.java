package com.outreach.soultracker.controller;

import com.outreach.soultracker.entity.AppUser;
import com.outreach.soultracker.repository.UserRepository;
import dev.samstevens.totp.code.CodeVerifier;
import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.code.DefaultCodeVerifier;
import dev.samstevens.totp.code.HashingAlgorithm;
import dev.samstevens.totp.exceptions.QrGenerationException;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.qr.QrGenerator;
import dev.samstevens.totp.qr.ZxingPngQrGenerator;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.secret.SecretGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
import dev.samstevens.totp.time.TimeProvider;
import dev.samstevens.totp.util.Utils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/2fa")
public class TwoFactorController {

    private final UserRepository userRepository;

    public TwoFactorController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/setup")
    public String setup(Model model, HttpServletRequest request) throws QrGenerationException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        AppUser user = userRepository.findByEmail(auth.getName()).orElseThrow();

        SecretGenerator secretGenerator = new DefaultSecretGenerator();
        String secret = secretGenerator.generate();

        // Store secret in session temporarily until confirmed
        request.getSession().setAttribute("temp_2fa_secret", secret);

        QrData data = new QrData.Builder()
                .label(user.getEmail())
                .secret(secret)
                .issuer("Reach System")
                .algorithm(HashingAlgorithm.SHA1)
                .digits(6)
                .period(30)
                .build();

        QrGenerator generator = new ZxingPngQrGenerator();
        byte[] imageData = generator.generate(data);
        String mimeType = generator.getImageMimeType();
        String qrCodeImage = Utils.getDataUriForImage(imageData, mimeType);

        model.addAttribute("qrCodeImage", qrCodeImage);
        model.addAttribute("secret", secret);
        return "2fa-setup";
    }

    @PostMapping("/setup")
    public String confirmSetup(@RequestParam("code") String code, HttpServletRequest request, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        AppUser user = userRepository.findByEmail(auth.getName()).orElseThrow();

        String secret = (String) request.getSession().getAttribute("temp_2fa_secret");
        if (secret == null) {
            return "redirect:/2fa/setup";
        }

        TimeProvider timeProvider = new SystemTimeProvider();
        CodeVerifier verifier = new DefaultCodeVerifier(new DefaultCodeGenerator(), timeProvider);

        if (verifier.isValidCode(secret, code)) {
            user.setTwoFactorSecret(secret);
            user.setTwoFactorEnabled(true);
            userRepository.save(user);
            request.getSession().removeAttribute("temp_2fa_secret");
            return "redirect:/profile?2fa=success";
        } else {
            model.addAttribute("error", "Invalid verification code.");
            return "redirect:/2fa/setup?error";
        }
    }

    @PostMapping("/disable")
    public String disable2Fa() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        AppUser user = userRepository.findByEmail(auth.getName()).orElseThrow();
        user.setTwoFactorEnabled(false);
        user.setTwoFactorSecret(null);
        userRepository.save(user);
        return "redirect:/profile?2fa=disabled";
    }

    @GetMapping("/verify")
    public String verifyPage() {
        return "2fa-verify";
    }

    @PostMapping("/verify")
    public String verifyCode(@RequestParam("code") String code, HttpServletRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/login";
        }

        AppUser user = userRepository.findByEmail(auth.getName()).orElseThrow();

        TimeProvider timeProvider = new SystemTimeProvider();
        CodeVerifier verifier = new DefaultCodeVerifier(new DefaultCodeGenerator(), timeProvider);

        if (verifier.isValidCode(user.getTwoFactorSecret(), code)) {
            request.getSession().setAttribute("passed_2fa", true);
            return "redirect:/";
        } else {
            return "redirect:/2fa/verify?error";
        }
    }
}
