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
    public ModelAndView acceptRequestFromEmail(@RequestParam String token) {
        return handleEmailRequestAction(token, OrganizationAction.ACCEPT);
    }

    @GetMapping("/reject")
    public ModelAndView rejectRequestFromEmail(@RequestParam String token) {
        return handleEmailRequestAction(token, OrganizationAction.REJECT);
    }

    @GetMapping("/invitation/accept")
    public ModelAndView acceptInvitationFromEmail(@RequestParam String token) {
        return handleEmailInvitationAction(token, OrganizationAction.ACCEPT);
    }

    @GetMapping("/invitation/reject")
    public ModelAndView rejectInvitationFromEmail(@RequestParam String token) {
        return handleEmailInvitationAction(token, OrganizationAction.REJECT);
    }

    private ModelAndView handleEmailRequestAction(String token, OrganizationAction action) {
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

    private ModelAndView handleEmailInvitationAction(String token, OrganizationAction action) {
        ModelAndView mav = new ModelAndView("invitation-result");

        try {
            membershipService.processEmailInvitationAction(token, action);

            mav.addObject("status", action == OrganizationAction.ACCEPT ? "accepted" : "rejected");
            mav.addObject("message", action == OrganizationAction.ACCEPT
                    ? "The invitation has been accepted."
                    : "The invitation has been rejected.");
        } catch (Exception e) {
            mav.addObject("status", "error");
            mav.addObject("message", "This link is invalid or has already been used.");
        }

        return mav;
    }
}
