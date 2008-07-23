/******************************************************************************
 * SiteRoleProducer.java - created by Sakai App Builder -AZ
 * 
 * Copyright (c) 2008 Sakai Project/Sakai Foundation
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 * 
 *****************************************************************************/

package org.sakaiproject.adminsiteperms.tool.producers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.FunctionManager;
import org.sakaiproject.authz.api.GroupNotDefinedException;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.site.api.SiteService;

import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UISelect;
import uk.org.ponder.rsf.components.UISelectChoice;
import uk.org.ponder.rsf.components.UISelectLabel;
import uk.org.ponder.rsf.components.decorators.UILabelTargetDecorator;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.DefaultView;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.ViewParameters;

/**
 * This is the view producer for the HelloWorld html template
 * @author Sakai App Builder -AZ
 */
public class SiteRoleProducer implements ViewComponentProducer, DefaultView {

	// The VIEW_ID must match the html template (without the .html)
	public static final String VIEW_ID = "SiteRole";
	public String getViewID() {
		return VIEW_ID;
	}

   private SiteService siteService;
   public void setSiteService(SiteService siteService) {
      this.siteService = siteService;
   }

   private AuthzGroupService authzGroupService;
   public void setAuthzGroupService(AuthzGroupService authzGroupService) {
      this.authzGroupService = authzGroupService;
   }

   private FunctionManager functionManager;
   public void setFunctionManager(FunctionManager functionManager) {
      this.functionManager = functionManager;
   }

   private SecurityService securityService;
   public void setSecurityService(SecurityService securityService) {
      this.securityService = securityService;
   }

	@SuppressWarnings("unchecked")
   public void fillComponents(UIContainer tofill, ViewParameters viewparams,
			ComponentChecker checker) {
	   if (! securityService.isSuperUser()) {
	      throw new SecurityException("This is only accessible by super users");
	   }

      UIForm form = UIForm.make(tofill, "actionForm");
      
      UICommand.make(form, "addPerms", UIMessage.make("siterole.add.command"), "actionBean.addSiteRolePerms");
      UICommand.make(form, "removePerms", UIMessage.make("siterole.remove.command"), "actionBean.removeSiteRolePerms");

      String[] selectIds;
      int counter = 0;
      UISelect radios;
      String selectID;

      // fill checkboxes for the site types
      List<String> siteTypes = siteService.getSiteTypes();
      Collections.sort(siteTypes);
      selectIds = new String[siteTypes.size()];
      counter = 0;
      for (String type : siteTypes) {
         selectIds[counter] = type;
         counter++;
      }
      
      radios = UISelect.makeMultiple(form, "siteTypeHolder", selectIds, selectIds, "actionBean.types", null);
      selectID = radios.getFullID();
      
      for (int i = 0; i < selectIds.length; i++) {
         UIBranchContainer siteTypeRow = UIBranchContainer.make(form, "siteTypeRow:");
         UISelectChoice choice = UISelectChoice.make(siteTypeRow, "siteTypeSelect", selectID, i);
         UISelectLabel.make(siteTypeRow, "siteTypeName", selectID, i).decorate(new UILabelTargetDecorator(choice) );
      }

      // fill checkboxes for the roles
      List<String> roles = getValidRoles();
      selectIds = new String[roles.size()];
      counter = 0;
      for (String type : roles) {
         selectIds[counter] = type;
         counter++;
      }

      radios = UISelect.makeMultiple(form, "roleHolder", selectIds, selectIds, "actionBean.roles", null);
      selectID = radios.getFullID();

      for (int i = 0; i < selectIds.length; i++) {
         UIBranchContainer roleRow = UIBranchContainer.make(form, "roleRow:");
         UISelectChoice choice = UISelectChoice.make(roleRow, "roleSelect", selectID, i);
         UISelectLabel.make(roleRow, "roleName", selectID, i).decorate(new UILabelTargetDecorator(choice) );
      }

      // fill checkboxes for the perms
      List<String> perms = functionManager.getRegisteredFunctions();
      Collections.sort(perms);
      selectIds = new String[perms.size()];
      counter = 0;
      for (String type : perms) {
         selectIds[counter] = type;
         counter++;
      }

      radios = UISelect.makeMultiple(form, "permHolder", selectIds, selectIds, "actionBean.perms", null);
      selectID = radios.getFullID();

      for (int i = 0; i < selectIds.length; i++) {
         UIBranchContainer permRow = UIBranchContainer.make(form, "permRow:");
         UISelectChoice choice = UISelectChoice.make(permRow, "permSelect", selectID, i);
         UISelectLabel.make(permRow, "permName", selectID, i).decorate(new UILabelTargetDecorator(choice) );
      }

	}

	public static String[] templates = {
	   "!site.template",
      "!site.template.course",
      "!site.template.portfolio",
      "!site.user"
	};

	@SuppressWarnings("unchecked")
   private List<String> getValidRoles() {
	   HashSet<String> roleIds = new HashSet<String>();
      for (String templateRef : templates) {
         AuthzGroup ag;
         try {
            ag = authzGroupService.getAuthzGroup(templateRef);
            Set<Role> agRoles = ag.getRoles();
            for (Role role : agRoles) {
               roleIds.add(role.getId());
            }
         } catch (GroupNotDefinedException e) {
            // nothing to do here but continue really
         }
      }
      ArrayList<String> roles = new ArrayList<String>(roleIds);
      Collections.sort(roles);
	   return roles;
	}

}
