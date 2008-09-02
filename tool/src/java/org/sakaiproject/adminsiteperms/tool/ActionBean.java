/**
 * $Id$
 * $URL$
 * ActionBean.java - admin-site-perms - Jul 23, 2008 10:39:02 AM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Aaron Zeckoski
 * Licensed under the Apache License, Version 2.0
 * 
 * A copy of the Apache License has been included in this 
 * distribution and is available at: http://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * Aaron Zeckoski (azeckoski @ gmail.com) (aaronz @ vt.edu) (aaron @ caret.cam.ac.uk)
 */

package org.sakaiproject.adminsiteperms.tool;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.AuthzPermissionException;
import org.sakaiproject.authz.api.GroupNotDefinedException;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.SiteService.SelectionType;
import org.sakaiproject.site.api.SiteService.SortType;

import uk.org.ponder.messageutil.TargettedMessage;
import uk.org.ponder.messageutil.TargettedMessageList;


/**
 * This performs the actual actions
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
public class ActionBean {

    private static Log log = LogFactory.getLog(ActionBean.class);

    private SiteService siteService;
    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }

    private AuthzGroupService authzGroupService;
    public void setAuthzGroupService(AuthzGroupService authzGroupService) {
        this.authzGroupService = authzGroupService;
    }

    private SecurityService securityService;
    public void setSecurityService(SecurityService securityService) {
        this.securityService = securityService;
    }

    private TargettedMessageList messages;
    public void setMessages(TargettedMessageList messages) {
        this.messages = messages;
    }

    /**
     * The types of sites to add perms to (course/project/workspace/etc.)
     */
    public String[] types;
    /**
     * The perms to add/remove to the sites
     */
    public String[] perms;
    /**
     * The roles to have the perms added or removed
     */
    public String[] roles;

    public String addSiteRolePerms() {
        try {
            checkInputs();
        } catch (IllegalArgumentException e) {
            messages.addMessage( new TargettedMessage("user.message.illegal.submission",
                    new Object[] { }, TargettedMessage.SEVERITY_ERROR));
            return "failed";
        }
        // now add the perms to all matching roles in all matching sites
        setSiteRolePerms(true);
        return "success";
    }

    public String removeSiteRolePerms() {
        try {
            checkInputs();
        } catch (IllegalArgumentException e) {
            messages.addMessage( new TargettedMessage("user.message.illegal.submission",
                    new Object[] { }, TargettedMessage.SEVERITY_ERROR));
            return "failed";
        }
        // now remove the perms from all matching roles in all matching sites
        setSiteRolePerms(false);
        return "success";
    }

    @SuppressWarnings("unchecked")
    protected void setSiteRolePerms(boolean add) {
        String permsString = makeStringFromArray(perms);
        String typesString = makeStringFromArray(types);
        String rolesString = makeStringFromArray(roles);
        List<String> permsList = Arrays.asList(perms);
        // now add the perms to all matching roles in all matching sites
        List<Site> sites = siteService.getSites(SelectionType.ANY, types, null, null, SortType.NONE, null);
        int successCount = 0;
        for (Site site : sites) {
            String siteRef = site.getReference();
            try {
                AuthzGroup ag = authzGroupService.getAuthzGroup(siteRef);
                if (authzGroupService.allowUpdate(ag.getId())) {
                    boolean updated = false;
                    for (String role : roles) {
                        Role r = ag.getRole(role);
                        // if role not found in this group then move on
                        if (r != null) {
                            // get the current perms so we can possibly avoid an update
                            Set<String> current = r.getAllowedFunctions();
                            if (add) {
                                if (! current.containsAll(permsList)) {
                                    // only update if the perms are not already there
                                    r.allowFunctions(permsList);
                                    updated = true;
                                }
                            } else {
                                boolean found = false;
                                for (String perm : permsList) {
                                    if (current.contains(perm)) {
                                        found = true;
                                        break;
                                    }
                                }
                                if (found) {
                                    // only update if at least one perm needs to be removed
                                    r.disallowFunctions(permsList);
                                    updated = true;
                                }
                            }
                        }
                    }
                    if (updated) {
                        // only save if the group was updated
                        authzGroupService.save(ag);
                        log.info("Added Permissions ("+permsString+") for roles ("+rolesString+") to group:" + siteRef);
                    }
                    successCount++;
                } else {
                    log.warn("Cannot update authz group: " + siteRef + ", unable to apply any perms change");
                }
            } catch (GroupNotDefinedException e) {
                log.error("Could not find authz group: " + siteRef + ", unable to apply any perms change");
            } catch (AuthzPermissionException e) {
                log.error("Could not save authz group: " + siteRef + ", unable to apply any perms change");
            }
        }
        int failureCount = sites.size() - successCount;
        String operation = (add ? "adding" : "removing");
        messages.addMessage( new TargettedMessage("user.message.permissions." + (add ? "added" : "removed"),
                new Object[] { typesString, rolesString, permsString, sites.size(), successCount, failureCount }, 
                TargettedMessage.SEVERITY_INFO));
        log.info("Completed "+operation+" permissions ("+permsString+") in "+sites.size()
                + " sites of types ("+typesString+") for the following roles ("+rolesString+"), "
                + "there were "+successCount+" successful updates and "+failureCount+" failures");
    }

    protected String makeStringFromArray(String[] array) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < array.length; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(array[i]);
        }
        return sb.toString();
    }

    protected void checkInputs() {
        if (types == null || types.length == 0) {
            throw new IllegalArgumentException("site types cannot be null or empty");
        }
        if (perms == null || perms.length == 0) {
            throw new IllegalArgumentException("perms to add/remove cannot be null or empty");
        }
        if (roles == null || roles.length == 0) {
            throw new IllegalArgumentException("roles to add/remove perms for cannot be null or empty");
        }
        // TODO validate the perms list?
        if (! securityService.isSuperUser()) {
            throw new SecurityException("Only the super user can do mass updates of permissions");
        }
    }
}
