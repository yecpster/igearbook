package net.jforum.view.admin;

import java.util.List;

import net.jforum.dao.CategoryDAO;
import net.jforum.dao.DataAccessDriver;
import net.jforum.dao.ForumDAO;
import net.jforum.dao.GroupDAO;
import net.jforum.dao.GroupSecurityDAO;
import net.jforum.entities.Category;
import net.jforum.entities.Forum;
import net.jforum.entities.Group;
import net.jforum.repository.ForumRepository;
import net.jforum.security.PermissionControl;
import net.jforum.security.Role;
import net.jforum.security.RoleCollection;
import net.jforum.security.RoleValue;
import net.jforum.security.RoleValueCollection;
import net.jforum.security.SecurityConstants;
import net.jforum.util.preferences.TemplateKeys;

/**
 * 
 * @author Chesley
 * 
 */
public class UpgradeAction extends AdminCommand {
    // Listing
    public void list() {
        upgrade();
        this.setTemplateName(TemplateKeys.ADMIN_UPGRADE);
    }

    public void upgrade() {
        CategoryDAO categoryDao = DataAccessDriver.getInstance().newCategoryDAO();
        List<Category> categories = categoryDao.selectAll();
        for (Category category : categories) {
            handleEntitlement(SecurityConstants.PERM_CATEGORY, category.getId());
        }
        ForumDAO forumDao = DataAccessDriver.getInstance().newForumDAO();
        List<Forum> forums = forumDao.selectAll();
        for (Forum forum : forums) {
            handleEntitlement(SecurityConstants.PERM_FORUM, forum.getId());
            handleEntitlement(SecurityConstants.PERM_ANONYMOUS_POST, forum.getId());
            handleEntitlement(SecurityConstants.PERM_REPLY, forum.getId());
            handleEntitlement(SecurityConstants.PERM_NEW_POST, forum.getId());
            handleEntitlement(SecurityConstants.PERM_HTML_DISABLED, forum.getId());
        }
    }

    private void handleEntitlement(String permition, int moduleId) {
        GroupSecurityDAO gmodel = DataAccessDriver.getInstance().newGroupSecurityDAO();
        PermissionControl pc = new PermissionControl();
        pc.setSecurityModel(gmodel);
        GroupDAO groupDao = DataAccessDriver.getInstance().newGroupDAO();
        Group enGroup = groupDao.getEntitlementGroup(permition, moduleId);
        if (enGroup == null) {
            enGroup = groupDao.addNewEntitlementGroup(permition, moduleId);
        }
        RoleCollection roles = gmodel.loadRoles(enGroup.getId());
        if (roles == null || roles.size() == 0) {
            this.addRole(pc, permition, moduleId, enGroup);
        }
    }

    private void addRole(PermissionControl pc, String roleName, int id, Group group) {
        Role role = new Role();
        role.setName(roleName);

        RoleValueCollection roleValues = new RoleValueCollection();

        RoleValue rv = new RoleValue();
        rv.setValue(Integer.toString(id));
        roleValues.add(rv);

        pc.addRoleValue(group.getId(), role, roleValues);
    }
}
