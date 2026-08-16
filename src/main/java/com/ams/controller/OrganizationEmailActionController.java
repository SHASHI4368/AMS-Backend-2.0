package com.ams.controller;

import com.ams.enums.OrganizationAction;
import com.ams.service.MembershipService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

@RestController
@RequestMapping("${api.base-path}/organization-email-actions")
@RequiredArgsConstructor
public class OrganizationEmailActionController {
    private final MembershipService membershipService;

    @GetMapping("/accept")
    public ModelAndView acceptFromEmail(@RequestParam String token) {
        return handleEmailAction(token, OrganizationAction.ACCEPT);
    }

    @GetMapping("/reject")
    public ModelAndView rejectFromEmail(@RequestParam String token) {
        return handleEmailAction(token, OrganizationAction.REJECT);
    }

    private ModelAndView handleEmailAction(String token, OrganizationAction action) {
        ModelAndView mav = new ModelAndView("join-request-result");

        try {
            membershipService.processEmailJoinRequestAction(token, action);

            mav.addObject("status", action == OrganizationAction.ACCEPT ? "accepted" : "rejected");
            mav.addObject("message", action == OrganizationAction.ACCEPT
                    ? "The join request has been accepted."
                    : "The join request has been rejected.");
        } catch (Exception e) {
            mav.addObject("status", "error");
            mav.addObject("message", "This link is invalid or has already been used.");
        }

        return mav;
    }
}
